package net.thaumcraft.occulta.wolf;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaItems;

/**
 * A <b>escada dos dez graus</b>: o {@code BlockStatueWerewolf} do Witchery, que é a maior coisa que o mod
 * pede a quem joga.
 *
 * <p>Quem apanha a licantropia fica no <b>grau um</b>, e no grau um a lua manda nele e ele não manda em nada.
 * Daí até ao décimo vão dez degraus, e a <b>Estátua do Lobisomem</b> é quem os dá — um a um, e nunca dois.
 *
 * <h2>O que ela pede, e porquê</h2>
 *
 * <p>Os três primeiros degraus são <b>coisas na mão</b>, e são de propósito os mais fáceis:
 *
 * <ul>
 *   <li><b>três barras de ouro</b> — e o degrau lhe devolve um <b>Amuleto da Lua</b>, que é o que faz dele
 *       dono da própria forma;</li>
 *   <li><b>trinta carnes de carneiro cruas</b>;</li>
 *   <li><b>dez línguas de cachorro</b>.</li>
 * </ul>
 *
 * <p>Do quarto em diante ela deixa de pedir coisas e passa a pedir <b>feitos</b> — e cada feito só conta na
 * <b>forma certa</b>, que é o que faz da escada uma escada e não uma lista:
 *
 * <ul>
 *   <li><b>quarto</b>: ela lhe dá o <b>Chifre da Caça</b> e o manda matar o que vem quando ele sopra;</li>
 *   <li><b>quinto</b>: <b>dez monstros mortos no ar</b>, de lobo — é o degrau do salto;</li>
 *   <li><b>sexto</b>: <b>uivar em dezesseis lugares</b> diferentes, de lobo e de noite — é o degrau que
 *       obriga a andar;</li>
 *   <li><b>sétimo</b>: <b>seis lobos amansados</b> com o focinho, de lobo;</li>
 *   <li><b>oitavo</b>: <b>trinta porcos-zumbis</b>, de lobisomem;</li>
 *   <li><b>nono</b>: <b>uma pessoa</b>, de lobo. Uma só, e é a que custa mais caro.</li>
 * </ul>
 *
 * <p>No <b>décimo</b> a estátua não pede nada: só ruge e diz que acabou.
 *
 * <h2>E o ouro compra sempre</h2>
 *
 * <p>Do <b>segundo grau</b> em diante, chegar à estátua com três barras de ouro na mão compra um <b>Amuleto
 * da Lua</b> — sempre, e <b>antes</b> de ela olhar o degrau. É o original, e tem uma consequência curiosa que
 * vale guardar: um lobisomem de grau dois que chegue com ouro na mão <b>nunca passa do grau dois</b>, porque a
 * estátua lhe vende um amuleto em vez de lhe pedir o carneiro.
 *
 * <p>Quem chega com <b>grau zero</b> — gente, nunca mordido — não é digno: apanha <b>Fadiga de Mineração</b>
 * por um minuto e um pó roxo na cara.
 */
public final class WerewolfLadder {
    /** As três coisas que ela pede na mão. */
    public static final int OURO = 3;
    public static final int CARNEIRO = 30;
    public static final int LÍNGUAS = 10;

    /** E os cinco feitos. */
    public static final int MORTES_NO_AR = 10;
    public static final int LUGARES = 16;
    public static final int LOBOS = 6;
    public static final int PORCOS = 30;
    public static final int PESSOAS = 1;

    /** O que ela faz a quem não é digno. */
    public static final int FADIGA = 1200;

    /** A voz dela: a do senhor dos lobisomens. */
    public static final SoundEvent VOZ = net.thaumcraft.occulta.OccultaSounds.WOLFMAN_LORD.value();

    private WerewolfLadder() {
    }

    /** Quantos feitos este grau pede, ou zero se ele não pede feitos. */
    public static int precisaDe(int grau) {
        return switch (grau) {
            case 5 -> MORTES_NO_AR;
            case 6 -> LUGARES;
            case 7 -> LOBOS;
            case 8 -> PORCOS;
            case 9 -> PESSOAS;
            default -> 0;
        };
    }

    /**
     * Alguém falou com a estátua.
     *
     * @param onde a casa da estátua
     * @param para o lado para que ela olha, que é por onde as coisas saem
     */
    public static void fala(ServerLevel level, Player quem, ItemStack mão, BlockPos onde, Direction para) {
        voz(level, quem);
        int grau = Werewolf.grauDe(quem);

        // o ouro compra sempre, do segundo grau em diante, e antes de ela olhar o degrau
        if (grau >= Werewolf.MANDA_NA_MUDANÇA && mão.is(Items.GOLD_INGOT) && mão.getCount() >= OURO) {
            diz(quem, "mooncharmcrafted");
            mão.shrink(OURO);
            var amuleto = larga(level, onde, para, new ItemStack(OccultaItems.MOON_CHARM));
            pó(level, amuleto.position(), SoundEvents.EXPERIENCE_ORB_PICKUP);
            return;
        }

        switch (grau) {
            case 0 -> {
                level.sendParticles(SpellParticleOption.create(ParticleTypes.EFFECT, 0.6f, 0.2f, 0.8f, 1.0f),
                        quem.getX(), quem.getY() + 1.0, quem.getZ(), 16, 1.0, 1.0, 1.0, 0.0);
                quem.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, FADIGA, 0));
                diz(quem, "notworthy");
            }
            case 1 -> naMão(level, quem, mão, onde, para, 2, Items.GOLD_INGOT, OURO, true);
            case 2 -> naMão(level, quem, mão, onde, para, 3, Items.MUTTON, CARNEIRO, false);
            case 3 -> naMão(level, quem, mão, onde, para, 4, OccultaItems.DOG_TONGUE, LÍNGUAS, false);
            case 4 -> oChifre(level, quem, onde, para);
            case 5, 6, 7, 8, 9 -> oFeito(level, quem, onde, para, grau);
            default -> diz(quem, "level10complete");
        }
    }

    /**
     * Um degrau que se paga com coisas na mão.
     *
     * <p>Com a <b>mão vazia ou errada</b> ela diz o que quer; com a mão a meio, diz quanto falta; e cheia,
     * come o que ela pediu e dá o degrau. O primeiro degrau ainda <b>larga um Amuleto da Lua</b>, que é o que
     * faz dele o degrau que vale mais.
     */
    private static void naMão(ServerLevel level, Player quem, ItemStack mão, BlockPos onde, Direction para,
                              int degrau, net.minecraft.world.item.Item oquê, int quantos, boolean comAmuleto) {
        if (!mão.is(oquê)) {
            diz(quem, "level" + degrau + "begin", String.valueOf(quantos));
            return;
        }
        if (mão.getCount() < quantos) {
            diz(quem, "level" + degrau + "progress",
                    String.valueOf(quantos), String.valueOf(quantos - mão.getCount()));
            return;
        }

        diz(quem, "level" + degrau + "complete");
        mão.shrink(quantos);
        if (comAmuleto) {
            var amuleto = larga(level, onde, para, new ItemStack(OccultaItems.MOON_CHARM));
            pó(level, amuleto.position(), SoundEvents.PLAYER_LEVELUP);
        } else {
            pó(level, saída(onde, para), SoundEvents.PLAYER_LEVELUP);
        }
        Werewolf.sobeUmGrau(quem);
    }

    /**
     * O quarto degrau: o <b>Chifre da Caça</b>, e o que vem quando ele sopra.
     *
     * <p>É o único degrau que ela <b>dá</b> em vez de pedir, e o único que se cumpre longe dela.
     */
    private static void oChifre(ServerLevel level, Player quem, BlockPos onde, Direction para) {
        switch (WerewolfQuest.estadoDe(quem)) {
            case NENHUM -> {
                diz(quem, "level5begin");
                var chifre = larga(level, onde, para, new ItemStack(OccultaItems.HORN_OF_THE_HUNT));
                pó(level, chifre.position(), SoundEvents.FIRE_EXTINGUISH);
                WerewolfQuest.estado(quem, WerewolfQuest.Estado.COMEÇADO);
            }
            case COMEÇADO -> diz(quem, "level5progress");
            case PRONTO -> {
                diz(quem, "level5complete");
                pó(level, saída(onde, para), SoundEvents.PLAYER_LEVELUP);
                Werewolf.sobeUmGrau(quem);
            }
        }
    }

    /**
     * Os cinco degraus de feito, que são todos a mesma conta.
     *
     * <p>Ela <b>olha a conta</b> cada vez que se fala com ela: chegando ao que o degrau pede, o pedido fica
     * pronto ali mesmo. É por isso que não há como ficar preso com a conta cheia.
     */
    private static void oFeito(ServerLevel level, Player quem, BlockPos onde, Direction para, int grau) {
        int precisa = precisaDe(grau);
        if (WerewolfQuest.contaDe(quem) >= precisa) {
            WerewolfQuest.estado(quem, WerewolfQuest.Estado.PRONTO);
        }
        int degrau = grau + 1;

        switch (WerewolfQuest.estadoDe(quem)) {
            case NENHUM -> {
                diz(quem, "level" + degrau + "begin", String.valueOf(precisa));
                WerewolfQuest.estado(quem, WerewolfQuest.Estado.COMEÇADO);
            }
            case COMEÇADO -> diz(quem, "level" + degrau + "progress", String.valueOf(precisa),
                    String.valueOf(precisa - WerewolfQuest.contaDe(quem)));
            case PRONTO -> {
                diz(quem, "level" + degrau + "complete");
                pó(level, saída(onde, para), SoundEvents.PLAYER_LEVELUP);
                Werewolf.sobeUmGrau(quem);
            }
        }
    }

    // ------------------------------------------------------------------ o que ela diz e onde larga

    /** Onde as coisas saem: um bloco à frente da cara dela, à altura do peito. */
    public static Vec3 saída(BlockPos onde, Direction para) {
        return new Vec3(onde.getX() + 0.5 + para.getStepX(), onde.getY() + 1.1,
                onde.getZ() + 0.5 + para.getStepZ());
    }

    /** Larga uma coisa à frente dela, <b>parada</b>: o original não lhe dá velocidade nenhuma. */
    private static ItemEntity larga(ServerLevel level, BlockPos onde, Direction para, ItemStack oquê) {
        Vec3 fora = saída(onde, para);
        var coisa = new ItemEntity(level, fora.x, fora.y, fora.z, oquê);
        coisa.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(coisa);
        return coisa;
    }

    /** O pó vermelho e o barulho de cada degrau. */
    private static void pó(ServerLevel level, Vec3 onde, SoundEvent barulho) {
        level.sendParticles(DustParticleOptions.REDSTONE, onde.x, onde.y, onde.z, 16, 0.2, 0.2, 0.2, 0.0);
        level.playSound(null, onde.x, onde.y, onde.z, barulho, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    /**
     * A voz dela, que <b>só quem falou ouve</b>.
     *
     * <p>É o {@code playOnlyTo} do original, e é de propósito: a mensagem diz que a voz ecoa <i>na cabeça</i>
     * de quem falou. Quem estiver ao lado não ouve nada, e vê o outro a falar com uma pedra.
     */
    private static void voz(ServerLevel level, Player quem) {
        if (!(quem instanceof net.minecraft.server.level.ServerPlayer gente)) return;
        gente.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                net.minecraft.core.Holder.direct(VOZ), SoundSource.BLOCKS,
                gente.getX(), gente.getY(), gente.getZ(), 1.0f, 1.0f, level.getRandom().nextLong()));
    }

    /** E o que ela diz, em dourado, só a quem falou. */
    private static void diz(Player quem, String oquê, String... com) {
        Object[] partes = com;
        quem.sendSystemMessage(Component.translatable("tc.werewolf." + oquê, partes)
                .withStyle(ChatFormatting.GOLD));
    }

    // ------------------------------------------------------------------ quem conta os feitos

    /**
     * Conta um feito, se o degrau em que ele está é este e o pedido está em curso.
     *
     * <p>É a guarda que todos os cinco ganchos partilham, e é ela que faz com que matar trinta porcos-zumbis
     * no grau cinco não valha nada.
     */
    public static void conta(Player quem, int grau) {
        if (Werewolf.grauDe(quem) != grau) return;
        if (WerewolfQuest.estadoDe(quem) != WerewolfQuest.Estado.COMEÇADO) return;
        WerewolfQuest.conta(quem);
    }

    /**
     * E o que diz que o pedido do quarto degrau acabou: o <b>Caçador morto</b>.
     *
     * <p>Repare que ele <b>não olha o grau</b>, e o original também não: matar um Caçador com <i>qualquer</i>
     * pedido a correr dá esse pedido por cumprido. Quem estiver no sexto degrau, uivando pelo mundo, e matar
     * um Caçador que outro chamou, sobe de graça. É uma falha do original, e fica — porque tirá-la seria
     * mudar a escada, e porque ela precisa de duas pessoas e de um chifre que só dá uma vez.
     */
    public static void caçadorMorto(Player quem) {
        if (WerewolfQuest.estadoDe(quem) != WerewolfQuest.Estado.COMEÇADO) return;
        WerewolfQuest.estado(quem, WerewolfQuest.Estado.PRONTO);
    }
}
