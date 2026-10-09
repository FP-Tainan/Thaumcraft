package net.thaumcraft.occulta.demon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.occulta.OccultaEffects;

import java.util.ArrayList;
import java.util.List;

/**
 * O que os três contratos de efeito fazem em quem os apanha: os {@code PlayerEffects} do Witchery.
 *
 * <p>No original eles <b>não são poções</b>: são um punhado de nomes guardados no NBT do jogador com um
 * prazo, pendurados em três ganchos do Forge — a batida, o clique num bloco e a queda de um bloco
 * quebrado. Aqui são <b>efeitos</b> do jogo, pelo motivo de sempre: o efeito já traz o prazo, a morte e o
 * guardar de graça, e o que falta — que o leite não os tire — o mod já sabe fazer.
 *
 * <p><b>Desvio declarado:</b> sendo efeitos, eles <b>aparecem no canto da tela</b> com um ícone, coisa que
 * no original não acontece. É a única diferença, e é a favor de quem joga: saber quanto falta do Toque de
 * Fundir vale mais do que a surpresa de ele acabar.
 */
public final class ImpBlessings {
    /** De quanto em quanto tempo a conta de evaporar corre: o {@code TICKS_PER_UPDATE} do original. */
    public static final int DE_QUANTO_EM_QUANTO = 20;

    /** E a sorte dela: uma em cinco. */
    public static final int EVAPORA_UMA_EM = 5;

    /** O que ela alcança, em raio e ao quadrado. */
    public static final int RAIO = 3;
    public static final double RAIO_AO_QUADRADO = 9.0;

    /** De quanto acima dos pés até quanto abaixo ela olha. */
    public static final int ACIMA = 2;
    public static final int ABAIXO = 1;

    /** A sorte do toque de fogo: uma em cinco. */
    public static final int FOGO_UMA_EM = 5;

    /** E a do um a mais ao fundir: uma em quatro. */
    public static final double UM_A_MAIS = 0.25;

    private ImpBlessings() {
    }

    // ------------------------------------------------------------------ evaporar

    /**
     * <b>Evaporar</b>, de vinte em vinte batidas: o {@code doUpdate} do {@code IMP_EVAPORATION}.
     *
     * <p>Num cubo de três de raio — de dois acima dos pés a um abaixo —, apaga <b>toda a água que tiver ar
     * por cima</b>. É a conta do original, e a guarda do ar é o que faz dela uma coisa de superfície: quem
     * atravessa um lago a pé abre caminho; quem mergulha, não.
     */
    public static void evapora(ServerLevel level, ServerPlayer quem) {
        if (level.getGameTime() % DE_QUANTO_EM_QUANTO != 0) return;
        if (!quem.hasEffect(OccultaEffects.IMP_EVAPORATION)) return;
        if (level.getRandom().nextInt(EVAPORA_UMA_EM) != 0) return;
        seca(level, quem);
    }

    /**
     * E a varredura em si, sem o relógio nem o sorteio: o corpo do {@code doUpdate}.
     *
     * <p>Está à parte para que a prova a chame sem esperar a batida certa.
     */
    public static void seca(ServerLevel level, ServerPlayer quem) {
        BlockPos pés = quem.blockPosition();
        boolean achou = false;
        for (int x = pés.getX() - RAIO; x <= pés.getX() + RAIO; x++) {
            for (int z = pés.getZ() - RAIO; z <= pés.getZ() + RAIO; z++) {
                for (int y = pés.getY() + ACIMA; y >= pés.getY() - ABAIXO; y--) {
                    if (quem.distanceToSqr(x, y, z) > RAIO_AO_QUADRADO) continue;
                    BlockPos casa = new BlockPos(x, y, z);
                    BlockState feitio = level.getBlockState(casa);
                    if (!feitio.is(Blocks.WATER) || !level.getBlockState(casa.above()).isAir()) continue;
                    level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
                    level.sendParticles(ParticleTypes.EXPLOSION, x + 0.5, y + 1.0, z + 0.5,
                            1, 0.0, 0.0, 0.0, 0.0);
                    achou = true;
                }
            }
        }

        if (!achou) return;
        level.playSound(null, pés, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0f,
                2.6f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8f);
    }

    // ------------------------------------------------------------------ o toque de fogo

    /**
     * <b>Toque de Fogo</b>: o {@code doInteract} do {@code IMP_FIRE_TOUCH}.
     *
     * <p>Clicando num bloco que não seja ar, <b>uma vez em cinco</b>, acende-se a face clicada. O original
     * não pergunta o que está na mão nem se o clique fez outra coisa — ele acende e pronto.
     */
    public static InteractionResult toqueDeFogo(net.minecraft.world.entity.player.Player gente,
                                                net.minecraft.world.level.Level mundo,
                                                net.minecraft.world.InteractionHand mão,
                                                net.minecraft.world.phys.BlockHitResult onde) {
        if (!(mundo instanceof ServerLevel level)) return InteractionResult.PASS;
        if (!gente.hasEffect(OccultaEffects.IMP_FIRE_TOUCH)) return InteractionResult.PASS;
        if (level.getRandom().nextInt(FOGO_UMA_EM) != 0) return InteractionResult.PASS;
        if (level.getBlockState(onde.getBlockPos()).isAir()) return InteractionResult.PASS;

        BlockPos face = onde.getBlockPos().relative(onde.getDirection());
        BlockState estava = level.getBlockState(face);
        if (!estava.isAir() && !estava.canBeReplaced()) return InteractionResult.PASS;
        BlockState fogo = BaseFireBlock.getState(level, face);
        if (!fogo.canSurvive(level, face)) return InteractionResult.PASS;

        level.setBlockAndUpdate(face, fogo);
        return InteractionResult.PASS;
    }

    // ------------------------------------------------------------------ o toque de fundir

    /**
     * <b>Toque de Fundir</b>: o {@code doHarvest} do {@code IMP_METLING_TOUCH}.
     *
     * <p>Cada coisa que cai passa pela <b>receita de fornalha</b>; o que tiver uma sai fundido, e <b>uma
     * vez em quatro</b> sai <b>um a mais</b>. O que não tiver nenhuma cai como caía.
     *
     * @return a queda refeita, ou a mesma se não houve nada que fundir
     */
    public static List<ItemStack> funde(ServerLevel level, LivingEntity quem, List<ItemStack> caiu) {
        if (!quem.hasEffect(OccultaEffects.IMP_MELTING_TOUCH)) return caiu;

        List<ItemStack> feito = new ArrayList<>(caiu.size());
        boolean mexeu = false;
        for (ItemStack cada : caiu) {
            var receita = level.recipeAccess()
                    .getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMELTING,
                            new SingleRecipeInput(cada), level);
            if (receita.isEmpty()) {
                feito.add(cada);
                continue;
            }
            ItemStack fundido = receita.get().value().assemble(new SingleRecipeInput(cada));
            if (fundido.isEmpty()) {
                feito.add(cada);
                continue;
            }
            ItemStack saiu = fundido.copy();
            if (level.getRandom().nextDouble() < UM_A_MAIS) saiu.grow(1);
            feito.add(saiu);
            mexeu = true;
        }
        return mexeu ? feito : caiu;
    }
}
