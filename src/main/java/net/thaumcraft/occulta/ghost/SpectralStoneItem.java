package net.thaumcraft.occulta.ghost;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.enslave.Enslavement;
import net.thaumcraft.occulta.infusion.OverworldInfusion;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * A <b>Pedra Espectral</b>: o {@code ItemSpectralStone} do Witchery.
 *
 * <p>Um <b>frasco de fantasmas</b>. Ela guarda um dos três — <b>Espectro</b>, <b>Banshee</b> ou
 * <b>Poltergeist</b> — e até <b>três</b> de cada vez, e solta-os onde se apontar.
 *
 * <p>É o fim de uma linha longa: os três fantasmas chamam-se um a um, por rito, e morrem depressa. A
 * pedra é o que os torna <b>guardáveis</b> — chama-se três de uma vez numa noite tranquila, prendem-se, e
 * soltam-se quando fizerem falta.
 *
 * <p>Segura-se <b>vinte segundos</b>, e ao fim de <b>dois</b> ela faz um <i>pling</i> — que é o aviso de
 * que já se pode largar. Largando-a depois disso, o que ela tem sai no ponto para onde se aponta, a
 * <b>dezesseis</b> blocos, já <b>escravizado</b> por quem a largou. E a pedra fica em branco.
 *
 * <h2>O que ela guarda, e onde</h2>
 *
 * <p>O original mete o bicho e a quantidade no mesmo número do dano: {@code tipo | quantidade << 4}. Aqui
 * é o mesmo número, num componente — e a <b>cara</b> dela sai do tipo, pelo {@code custom_model_data},
 * como as das bússolas.
 */
public class SpectralStoneItem extends Item {
    /** Os três que ela guarda, pelos números do original. */
    public static final int NADA = 0;
    public static final int ESPECTRO = 1;
    public static final int BANSHEE = 2;
    public static final int POLTERGEIST = 3;

    /** Quantos cabem: três, e nem mais um. */
    public static final int CABEM = 3;

    /** Vinte segundos de segurar, e dois até poder largar. */
    public static final int SEGURAR = 400;
    public static final int PRONTA_AOS = 40;

    /** A dezesseis blocos, que é o alcance do olhar dela. */
    public static final double ALCANCE = 16.0;

    public SpectralStoneItem(Properties properties) {
        super(properties);
    }

    // ------------------------------------------------------------------ o que ela guarda

    /** O número inteiro, como o original o escreve. */
    public static int guardado(ItemStack pedra) {
        Integer tem = pedra.get(OccultaComponents.SPECTRAL_STONE);
        return tem == null ? 0 : tem;
    }

    /** Empacota o bicho e a quantidade num número só: o {@code metaFromCreature}. */
    public static int empacota(int bicho, int quantos) {
        return (bicho & 15) | (quantos & 7) << 4;
    }

    /** Qual bicho: os quatro bits de baixo. */
    public static int bicho(int guardado) {
        return guardado & 15;
    }

    /** E quantos: os três de cima, no máximo três. */
    public static int quantos(int guardado) {
        return Math.min(guardado >>> 4 & 7, CABEM);
    }

    /** Uma pedra com aquilo dentro. */
    public static ItemStack cheia(int bicho, int quantos) {
        ItemStack pedra = new ItemStack(net.thaumcraft.occulta.OccultaItems.SPECTRAL_STONE);
        escreve(pedra, empacota(bicho, quantos));
        return pedra;
    }

    /** Escreve o que ela guarda, e com isso a cara que ela mostra. */
    public static void escreve(ItemStack pedra, int guardado) {
        if (guardado == 0) {
            pedra.remove(OccultaComponents.SPECTRAL_STONE);
            pedra.remove(DataComponents.CUSTOM_MODEL_DATA);
            return;
        }
        pedra.set(OccultaComponents.SPECTRAL_STONE, guardado);
        pedra.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
                List.of((float) bicho(guardado)), List.of(), List.of(), List.of()));
    }

    /** O bicho do jogo que cada número é. */
    public static @Nullable EntityType<? extends SummonedUndeadEntity> deQue(int bicho) {
        return switch (bicho) {
            case ESPECTRO -> OccultaEntities.SPECTRE;
            case BANSHEE -> OccultaEntities.BANSHEE;
            case POLTERGEIST -> OccultaEntities.POLTERGEIST;
            default -> null;
        };
    }

    // ------------------------------------------------------------------ o segurar e o largar

    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        quem.startUsingItem(mão);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack pedra, LivingEntity quem) {
        return SEGURAR;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack pedra) {
        return ItemUseAnimation.BOW;
    }

    /** Ao segundo segundo, o <i>pling</i>: é o aviso de que já se pode largar. */
    @Override
    public void onUseTick(Level level, LivingEntity quemUsa, ItemStack pedra, int falta) {
        if (!(level instanceof ServerLevel mundo)) return;
        if (SEGURAR - falta != PRONTA_AOS) return;
        mundo.playSound(null, quemUsa.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(),
                SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    /**
     * <b>Largando-a, os fantasmas saem.</b>
     *
     * <p>Antes dos dois segundos, nada — e nem com a pedra em branco. Não havendo chão para onde apontar,
     * também nada: eles não nascem no ar.
     */
    @Override
    public boolean releaseUsing(ItemStack pedra, Level level, LivingEntity quemUsa, int falta) {
        if (!(level instanceof ServerLevel mundo) || !(quemUsa instanceof ServerPlayer quem)) return false;

        int passou = SEGURAR - falta;
        int guardado = guardado(pedra);
        int bicho = bicho(guardado);
        int quantos = quantos(guardado);
        if (passou < PRONTA_AOS || bicho == NADA || quantos <= 0) return false;

        EntityType<? extends SummonedUndeadEntity> qual = deQue(bicho);
        BlockPos onde = ondeCai(mundo, quem);
        if (qual == null || onde == null) {
            mundo.playSound(null, quem.blockPosition(), SoundEvents.NOTE_BLOCK_SNARE.value(),
                    SoundSource.PLAYERS, 0.5f, 1.0f);
            return false;
        }

        for (int n = 0; n < quantos; n++) {
            SummonedUndeadEntity saiu = qual.create(mundo, EntitySpawnReason.TRIGGERED);
            if (saiu == null) continue;
            saiu.snapTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5,
                    quem.getYRot(), 0.0f);
            saiu.quemChamou(quem.getUUID());
            mundo.addFreshEntity(saiu);
            Enslavement.escraviza(saiu, quem);
            mundo.sendParticles(net.minecraft.core.particles.SpellParticleOption.create(
                        ParticleTypes.INSTANT_EFFECT, 1.0f, 1.0f, 1.0f, 1.0f), saiu.getX(), saiu.getY() + 1.0,
                    saiu.getZ(), 16, 0.5, 0.5, 0.5, 0.0);
        }
        mundo.playSound(null, onde, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0f, 1.0f);

        gasta(pedra, quem);
        return true;
    }

    /**
     * <b>Gasta uma</b>, e a que fica é branca.
     *
     * <p>Numa pilha, tira uma e devolve uma branca à mochila — ou ao chão, se não couber. Sozinha, apaga-se
     * o que ela guardava. É o que o original faz, e é o que faz uma pilha de pedras cheias valer a pena.
     */
    private static void gasta(ItemStack pedra, ServerPlayer quem) {
        if (quem.hasInfiniteMaterials()) return;
        if (pedra.getCount() <= 1) {
            escreve(pedra, 0);
            return;
        }
        pedra.shrink(1);
        ItemStack branca = new ItemStack(net.thaumcraft.occulta.OccultaItems.SPECTRAL_STONE);
        if (!quem.getInventory().add(branca)) quem.drop(branca, false);
    }

    /** Para onde ela aponta: o bloco à frente do que ela bate, a dezesseis. */
    private static @Nullable BlockPos ondeCai(ServerLevel level, ServerPlayer quem) {
        HitResult onde = OverworldInfusion.olha(level, quem, ALCANCE);
        if (!(onde instanceof BlockHitResult bateu) || bateu.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        return bateu.getBlockPos().relative(bateu.getDirection());
    }

    // ------------------------------------------------------------------ o que ela diz na mão

    @Override
    public void appendHoverText(ItemStack pedra, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, net.minecraft.world.item.TooltipFlag bandeira) {
        linha.accept(Component.translatable("tc.spectralstone.tip").withStyle(ChatFormatting.DARK_GRAY));

        int guardado = guardado(pedra);
        int bicho = bicho(guardado);
        if (bicho == NADA) return;
        EntityType<? extends SummonedUndeadEntity> qual = deQue(bicho);
        if (qual == null) return;
        linha.accept(Component.translatable("tc.spectralstone.holds",
                        Component.translatable(qual.getDescriptionId()), quantos(guardado))
                .withStyle(ChatFormatting.AQUA));
    }
}
