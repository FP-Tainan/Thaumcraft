package net.thaumcraft.occulta.brew;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A <b>perícia de engarrafar</b>: o {@code skillLevelPotionBottling} do Witchery.
 *
 * <p>É a coisa mais escondida do ofício inteiro, e a que mais muda o jogo de quem o pratica. Um caldeirão
 * tem <b>três mil</b> de caldo. Quem nunca engarrafou tira dele <b>um frasco</b> — os três mil de uma vez.
 * Quem já engarrafou muito, e tiver o chapéu, o manto, o sapo e a Perícia de Cozimento a correr, tira
 * <b>trinta</b>.
 *
 * <p>Trinta vezes mais. E o original nunca o diz: a perícia não aparece em lado nenhum, não há barra nem
 * número, e a única coisa que a anuncia é uma frase, três vezes na vida de uma personagem — aos trinta,
 * aos sessenta e aos noventa:
 *
 * <blockquote>Você se sente mais hábil no cozimento; talvez se desperdice menos caldo daqui em diante.</blockquote>
 *
 * <h2>Como se aprende</h2>
 *
 * <p>Um ponto por frasco, até cem — mas <b>só com cozimento à altura</b>. Abaixo de trinta basta um
 * cozimento que gaste mais do que um grau; dos trinta aos sessenta pede-se quatro; dos sessenta aos
 * noventa, seis; e daí até cem, <b>oito</b>, que é o teto do que um caldeirão aguenta. Quem só fizer
 * cozimentos simples empaca nos trinta e nunca entende por quê.
 *
 * <p>E não se aprende com o que <b>não coube</b>: um caldeirão com mais efeitos do que espaço não ensina
 * nada.
 */
public final class Bottling {
    /** O teto da perícia, e de quanto em quanto ela dá um degrau. */
    public static final int TETO = 100;
    public static final int DEGRAU = 30;

    /** O tanque do caldeirão, que é o que se reparte. */
    public static final int TANQUE = net.thaumcraft.occulta.WitchesCauldronBlockEntity.FULL;

    /**
     * Os dez degraus de rendimento: quantos frascos, e quanto cada um <b>drena</b> do tanque.
     *
     * <p>A conta é a do original, e é por aqui que se vê o tamanho da coisa: do primeiro degrau ao último
     * o mesmo caldeirão passa de um frasco a trinta.
     */
    public static final int[] FRASCOS = {1, 2, 3, 4, 5, 6, 8, 10, 15, 30};
    public static final int[] DRENO = {3000, 1500, 1000, 750, 600, 500, 375, 300, 200, 100};

    /** A perícia de quem joga, que <b>atravessa a morte</b>: aprender não se desaprende morrendo. */
    public static final AttachmentType<Integer> PERÍCIA = AttachmentRegistry.<Integer>builder()
            .initializer(() -> 0)
            .persistent(Codec.INT)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("bottling"));

    private Bottling() {
    }

    public static int perícia(@Nullable Player quem) {
        return quem == null ? 0 : quem.getAttachedOrCreate(PERÍCIA);
    }

    /**
     * Se este cozimento ensina alguma coisa a quem tem esta perícia: o {@code canIncreasePlayerSkill}.
     *
     * <p>O <b>gasto</b> é quanto espaço os efeitos ocuparam e o <b>teto</b> é quanto o caldeirão abriu.
     * Gastar mais do que o teto é um cozimento que não coube, e esse não ensina nada.
     */
    public static boolean ensina(int perícia, BrewCapacity espaço) {
        int gasto = espaço.used();
        int teto = espaço.max();
        if (gasto > teto || teto == 0) return false;
        if (perícia >= TETO) return false;
        if (perícia < DEGRAU) return teto > 1 && gasto > 1;
        if (perícia < DEGRAU * 2) return teto >= 4 && gasto >= 4;
        if (perícia < DEGRAU * 3) return teto >= 6 && gasto >= 6;
        return teto >= 8 && gasto >= 8;
    }

    /**
     * Aprende um ponto, se o cozimento o ensinar — e avisa nos três degraus.
     *
     * @return a perícia depois de aprender
     */
    public static int aprende(Player quem, BrewCapacity espaço) {
        int tinha = perícia(quem);
        if (!ensina(tinha, espaço)) return tinha;
        int agora = Math.min(tinha + 1, TETO);
        quem.setAttached(PERÍCIA, agora);
        if (agora == DEGRAU || agora == DEGRAU * 2 || agora == DEGRAU * 3) {
            quem.sendSystemMessage(Component.translatable("tc.brew.skillincrease")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        return agora;
    }

    /**
     * Em que degrau de rendimento esta pessoa está, com este cozimento: a árvore do
     * {@code getDrainAmount}.
     *
     * <p>Quatro números entram nela — a <b>perícia</b> por degraus de trinta, o <b>equipamento</b> (o
     * chapéu vale um, o da Baba dois, cada manto um), o grau da <b>Perícia de Cozimento</b> a correr, e o
     * <b>sapo</b>. E a árvore não é linear: só a partir da perícia 60 é que o equipamento e a poção
     * começam a valer de verdade.
     */
    public static int degrau(@Nullable Player quem, List<Item> dentro) {
        int perícia = perícia(quem) / DEGRAU;
        int roupas = net.thaumcraft.occulta.clothes.WitchClothes.gearLevel(quem);
        int poção = grauDoCozimento(quem);
        int sapo = net.thaumcraft.occulta.familiar.Familiars.temMaestriaDeCozimento(quem) ? 1 : 0;

        int quanto;
        if (perícia == 0) {
            quanto = 0;
        } else if (perícia == 1) {
            quanto = roupas < 1 && poção < 1 ? 1 : 2;
        } else if (roupas >= 3 && poção >= 3 && sapo >= 1) {
            quanto = 9;
        } else if (roupas >= 2 && poção >= 3 && sapo >= 1) {
            quanto = 8;
        } else if (roupas >= 2 && poção >= 3) {
            quanto = 7;
        } else if (roupas >= 2 && poção >= 2) {
            quanto = 6;
        } else if (roupas >= 2 && poção >= 1) {
            quanto = 5;
        } else if (roupas >= 2 || poção >= 1) {
            quanto = 4;
        } else if (roupas < 1 && poção < 1) {
            quanto = 2;
        } else {
            quanto = 3;
        }
        return Math.max(quanto - penalidade(dentro), 0);
    }

    /** Quanto este caldeirão drena por frasco. */
    public static int dreno(@Nullable Player quem, List<Item> dentro) {
        return DRENO[degrau(quem, dentro)];
    }

    /** E quantos frascos ele dá ao todo, que é a mesma conta pelo outro lado. */
    public static int frascos(@Nullable Player quem, List<Item> dentro) {
        return FRASCOS[degrau(quem, dentro)];
    }

    /**
     * O grau da <b>Perícia de Cozimento</b> que a pessoa tem a correr, ou zero.
     *
     * <p>Ela vem da Gota de Sorte, e é o único jeito de a apanhar.
     */
    public static int grauDoCozimento(@Nullable Player quem) {
        if (quem == null) return 0;
        var tem = quem.getEffect(net.thaumcraft.occulta.OccultaEffects.BREWING_EXPERTISE);
        return tem == null ? 0 : tem.getAmplifier() + 1;
    }

    /**
     * A <b>penalidade de rendimento</b> do que está na panela: o {@code ModifierYield}.
     *
     * <p>É a conta esquisita do original — {@code penalidade - min(bónus, penalidade)} —, que quer dizer
     * «o bónus só serve para apagar a penalidade, e nunca para passar dela». O diamante, que abre espaço,
     * cobra <b>dois</b> degraus por isso; e é a única coisa da tabela que cobra.
     */
    public static int penalidade(List<Item> dentro) {
        int bónus = 0;
        int penalidade = 0;
        for (BrewAction ação : Brew.actions(dentro)) {
            bónus += ação.yieldBonus();
            penalidade += ação.yieldPenalty();
        }
        return penalidade - Math.min(bónus, penalidade);
    }
}
