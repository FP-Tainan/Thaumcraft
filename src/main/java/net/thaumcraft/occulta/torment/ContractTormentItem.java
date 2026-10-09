package net.thaumcraft.occulta.torment;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.Spawn;
import net.thaumcraft.occulta.infusion.Infusion;
import net.thaumcraft.occulta.infusion.Infusions;

import java.util.function.Consumer;

/**
 * O <b>Contrato do Tormento</b>: o ramo {@code itemContractTorment} do {@code ItemGeneral} do Witchery.
 *
 * <p>É o único papel do mod que <b>chama</b> alguma coisa, e é o que fecha o ramo que o Diabrete abriu: o
 * Diabrete dá o contrato, o contrato traz o <b>Senhor do Tormento</b>, e o Senhor larga o <b>Cozimento de
 * Alma do Tormento</b>, que é o que destranca o Tormentum.
 *
 * <h2>O que ele pede</h2>
 *
 * <p>Catorze segundos de mão erguida, dez de infusão e — isto é o que surpreende — um <b>círculo de
 * pedras</b> de onze por onze à volta de quem o segura. Não é giz: são <b>pedras de pé</b>, oito pilares de
 * três com arquitraves por cima e um anel de nove pedras de uma no meio, com o vão vazio onde a pessoa
 * está. É o único pedido do mod que não se faz com um item — faz-se <b>construindo</b>.
 *
 * <p>Sem o círculo, ele toca o tambor e larga a mão logo na primeira batida, de modo que não se perde
 * nada por tentar.
 *
 * <p>E ele <b>não se gasta no criativo</b>, que é a guarda de sempre.
 */
public class ContractTormentItem extends Item {
    /**
     * Quanto tempo a mão fica erguida: o <b>minuto</b> do original.
     *
     * <p>A leitura dura um minuto, mas o que importa acontece nos primeiros <b>catorze segundos</b>: de
     * quarenta em quarenta batidas ele cobra dez de infusão e chia, e na <b>duocentésima octogésima</b> o
     * Senhor chega. Depois disso a mão continua erguida e não acontece mais nada — é o original, e é a
     * diferença entre uma leitura e um botão.
     */
    public static final int SEGURAR = 1200;

    /** De quantas em quantas batidas ele cobra e chia: quarenta. */
    public static final int CHIA_DE = 40;

    /** E em qual delas ele chega: a oitava, aos catorze segundos. */
    public static final int CHAMA_EM = 280;

    /** O que cada batida de chiado custa de infusão. */
    public static final int CUSTA = 10;

    /** Quantas chamas cada chiado solta, antes e depois do meio da leitura. */
    public static final int CHAMAS = 16;
    public static final int CHAMAS_DEPOIS = 32;

    /** O estouro com que ele chega, e os anéis em que se procura lugar. */
    public static final float ESTOURO = 7.0f;
    public static final int PERTO = 2;
    public static final int LONGE = 4;

    /**
     * O desenho do círculo: o {@code PATTERN} do original, linha a linha, de norte para sul.
     *
     * <p>Zero é «não importa»; <b>um</b> é chão firme com nada em cima; <b>dois</b> é uma pedra de uma;
     * <b>três</b> é uma arquitrave, que é uma pedra <b>a dois de altura e só ela</b>; e <b>quatro</b> é um
     * pilar de três.
     */
    public static final int[][] DESENHO = {
            {0, 0, 0, 0, 4, 3, 4, 0, 0, 0, 0},
            {0, 0, 4, 3, 1, 1, 1, 3, 4, 0, 0},
            {0, 4, 1, 1, 1, 1, 1, 1, 1, 4, 0},
            {0, 3, 1, 1, 1, 1, 1, 1, 1, 3, 0},
            {4, 1, 1, 1, 2, 2, 2, 1, 1, 1, 4},
            {3, 1, 1, 1, 2, 1, 2, 1, 1, 1, 3},
            {4, 1, 1, 1, 2, 2, 2, 1, 1, 1, 4},
            {0, 3, 1, 1, 1, 1, 1, 1, 1, 3, 0},
            {0, 4, 1, 1, 1, 1, 1, 1, 1, 4, 0},
            {0, 0, 4, 3, 1, 1, 1, 3, 4, 0, 0},
            {0, 0, 0, 0, 4, 3, 4, 0, 0, 0, 0},
    };

    public ContractTormentItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack papel) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack papel, LivingEntity quem) {
        return SEGURAR;
    }

    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        quem.startUsingItem(mão);
        return InteractionResult.CONSUME;
    }

    /**
     * As <b>oito</b> batidas do original, de quarenta em quarenta.
     *
     * <p>Na <b>primeira</b> ele confere o círculo e desiste se não houver; nas do meio chia e cheira a
     * enxofre; e na <b>oitava</b> — a de duzentas e oitenta — o Senhor chega. Cada uma delas cobra dez de
     * infusão, de modo que a leitura inteira custa <b>oitenta</b>.
     */
    @Override
    public void onUseTick(Level level, LivingEntity quem, ItemStack papel, int falta) {
        if (!(level instanceof ServerLevel server) || !(quem instanceof ServerPlayer gente)) return;
        int passou = SEGURAR - falta;
        if (passou > CHAMA_EM || passou % CHIA_DE != 0) return;
        if (!Infusions.tira(server, gente, CUSTA, false)) return;

        if (passou == 0) {
            if (!círculo(server, gente.blockPosition())) {
                gente.sendSystemMessage(Component.translatable("tc.occulta.contract.torment.nostones")
                        .withStyle(ChatFormatting.RED));
                Infusion.falha(server, gente);
                gente.stopUsingItem();
                return;
            }
            server.playSound(null, gente.blockPosition(), SoundEvents.BLAZE_DEATH, SoundSource.PLAYERS,
                    1.0f, 1.0f);
            return;
        }

        if (passou < CHAMA_EM) {
            // as cinco do meio: o cheiro de enxofre crescendo, com mais chama de cada vez
            server.playSound(null, gente.blockPosition(), SoundEvents.BLAZE_DEATH, SoundSource.PLAYERS,
                    1.0f, 1.0f);
            int quanta = passou >= CHIA_DE * 4 ? CHAMAS_DEPOIS : CHAMAS;
            server.sendParticles(ParticleTypes.FLAME, gente.getX(), gente.getY() + 1.0, gente.getZ(),
                    quanta, 1.0, 2.0, 1.0, 0.0);
            return;
        }

        chama(server, gente, papel);
    }

    /** E ele chega, com um estouro de sete. */
    private void chama(ServerLevel level, ServerPlayer gente, ItemStack papel) {
        gente.stopUsingItem();
        if (!círculo(level, gente.blockPosition())) {
            Infusion.falha(level, gente);
            return;
        }

        var bicho = Spawn.perto(level, OccultaEntities.LORD_OF_TORMENT, gente.blockPosition(), PERTO, LONGE);
        if (!(bicho instanceof LordOfTormentEntity ele)) {
            Infusion.falha(level, gente);
            return;
        }
        ele.setPersistenceRequired();

        level.sendParticles(ParticleTypes.FLAME, ele.getX(), ele.getY() + 1.0, ele.getZ(),
                64, 1.0, 2.0, 1.0, 0.0);
        level.playSound(null, ele.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE,
                1.0f, 1.0f);
        level.explode(ele, ele.getX(), ele.getY() + ele.getEyeHeight(), ele.getZ(), ESTOURO, false,
                level.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MOB_GRIEFING)
                        ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);

        if (!gente.getAbilities().instabuild) papel.shrink(1);
    }

    /**
     * Se há um círculo de pedras à volta daquela casa: o {@code circleNear} do original.
     *
     * <p><b>Fielmente copiado, inclusive o engano:</b> o laço do original vai de zero a
     * {@code PATTERN.length - 1} <b>exclusive</b>, e lê as linhas de trás para a frente — de modo que a
     * <b>primeira linha do desenho nunca é conferida</b>. Como o desenho é simétrico, quem o construir pelo
     * livro constrói as duas pontas na mesma, e o engano não se vê. Fica como está.
     */
    public static boolean círculo(ServerLevel level, BlockPos onde) {
        int meioZ = (DESENHO.length - 1) / 2;
        for (int z = 0; z < DESENHO.length - 1; z++) {
            int mundoZ = onde.getZ() - meioZ + z;
            int meioX = (DESENHO[z].length - 1) / 2;
            for (int x = 0; x < DESENHO[z].length; x++) {
                int mundoX = onde.getX() - meioX + x;
                int quanto = DESENHO[DESENHO.length - 1 - z][x];
                if (quanto == 0) continue;
                if (!pedra(level, mundoX, onde.getY(), mundoZ,
                        quanto == 2 || quanto == 4, quanto == 4, quanto == 3 || quanto == 4)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Uma casa do desenho: o {@code isPost} do original.
     *
     * <p>Debaixo tem de haver chão firme; e as três casas acima dele têm de estar <b>exatamente</b> como o
     * desenho manda — sólidas onde ele pede e vazias onde não pede. É isso que faz uma arquitrave ser uma
     * arquitrave: a casa do meio tem de estar <b>vazia</b>.
     */
    private static boolean pedra(ServerLevel level, int x, int y, int z,
                                 boolean baixo, boolean meio, boolean cima) {
        if (!sólido(level, x, y - 1, z)) return false;
        if (sólido(level, x, y, z) != baixo) return false;
        if (sólido(level, x, y + 1, z) != meio) return false;
        if (sólido(level, x, y + 2, z) != cima) return false;
        // e acima de tudo, nada
        return !sólido(level, x, y + 3, z);
    }

    private static boolean sólido(ServerLevel level, int x, int y, int z) {
        return level.getBlockState(new BlockPos(x, y, z)).isSolid();
    }

    /** Sem uso fora do porte: a prova usa isto para levantar um círculo inteiro. */
    public static int quantoÉ(int linha, int casa) {
        return DESENHO[linha][casa];
    }

    /** A altura do chão daquela casa, que a prova usa para assentar o círculo. */
    public static int chão(ServerLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    @Override
    public void appendHoverText(ItemStack papel, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> linha, TooltipFlag flag) {
        linha.accept(Component.translatable("tc.occulta.contract.torment.tip")
                .withStyle(ChatFormatting.DARK_RED));
    }
}
