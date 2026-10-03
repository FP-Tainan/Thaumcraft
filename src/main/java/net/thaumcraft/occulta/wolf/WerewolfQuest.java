package net.thaumcraft.occulta.wolf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;

import java.util.List;

/**
 * O degrau em que ele está: o {@code wolfmanQuestState}, o {@code wolfmanQuestCounter} e os
 * {@code visitedChunks} do {@code ExtendedPlayer}.
 *
 * <p>A <b>escada dos dez graus</b> não é uma lista de compras: dos quatro primeiros degraus para cima, a
 * Estátua do Lobisomem deixa de pedir coisas e passa a pedir <b>feitos</b> — e um feito precisa de alguém que
 * o conte. É isto.
 *
 * <p>Três números, e nada mais:
 *
 * <ul>
 *   <li>o <b>estado</b>, que diz se a estátua já mandou, se ele está no meio, ou se acabou;</li>
 *   <li>a <b>conta</b>, que sobe a cada feito e é o que o estado olha;</li>
 *   <li>e os <b>lugares</b>, que só o uivo usa: um uivo num pedaço de mundo onde ele já uivou não conta, e é
 *       por isso que o sexto degrau obriga a <b>andar</b>.</li>
 * </ul>
 *
 * <p>Tudo isto se <b>apaga quando o grau muda</b>, e é o original que o faz: cada degrau começa do zero. Não
 * vai sincronizado, porque quem fala dele é sempre a estátua, e a estátua é do servidor.
 */
public record WerewolfQuest(Estado estado, int conta, List<Long> lugares) {
    /** A conta não passa de cem, como no original. */
    public static final int TETO_DA_CONTA = 100;

    /** Em que pé está o pedido do degrau. */
    public enum Estado implements net.minecraft.util.StringRepresentable {
        /** A estátua ainda não mandou nada. */
        NENHUM("nenhum"),
        /** Mandou, e ele está no meio. */
        COMEÇADO("começado"),
        /** E acabou — falta só voltar à estátua. */
        PRONTO("pronto");

        private final String nome;

        Estado(String nome) {
            this.nome = nome;
        }

        @Override
        public String getSerializedName() {
            return this.nome;
        }
    }

    /** Quem não começou nada. */
    public static final WerewolfQuest NADA = new WerewolfQuest(Estado.NENHUM, 0, List.of());

    public static final Codec<WerewolfQuest> CODEC = RecordCodecBuilder.create(i -> i.group(
                    net.minecraft.util.StringRepresentable.fromEnum(Estado::values)
                            .optionalFieldOf("estado", Estado.NENHUM).forGetter(WerewolfQuest::estado),
                    Codec.INT.optionalFieldOf("conta", 0).forGetter(WerewolfQuest::conta),
                    Codec.LONG.listOf().optionalFieldOf("lugares", List.of()).forGetter(WerewolfQuest::lugares))
            .apply(i, WerewolfQuest::new));

    /**
     * O que ele já fez neste degrau.
     *
     * <p><b>Atravessa a morte</b>, como o grau: morrer não desfaz o que ele já fez. É o original.
     */
    public static final AttachmentType<WerewolfQuest> DATA = AttachmentRegistry.<WerewolfQuest>builder()
            .initializer(() -> NADA)
            .persistent(CODEC)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("werewolf_quest"));

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o apego. */
    public static void init() {
    }

    /** Em que pé ele está. */
    public static WerewolfQuest de(Player quem) {
        return quem.getAttachedOrCreate(DATA);
    }

    /** O estado do pedido. */
    public static Estado estadoDe(Player quem) {
        return de(quem).estado();
    }

    /** A conta dos feitos. */
    public static int contaDe(Player quem) {
        return de(quem).conta();
    }

    /** Põe o estado, deixando a conta e os lugares como estão. */
    public static void estado(Player quem, Estado qual) {
        WerewolfQuest era = de(quem);
        if (era.estado() == qual) return;
        quem.setAttached(DATA, new WerewolfQuest(qual, era.conta(), era.lugares()));
    }

    /** Conta um feito. */
    public static void conta(Player quem) {
        WerewolfQuest era = de(quem);
        quem.setAttached(DATA, new WerewolfQuest(era.estado(),
                Math.min(era.conta() + 1, TETO_DA_CONTA), era.lugares()));
    }

    /**
     * Guarda este pedaço de mundo, e diz se ele é <b>novo</b>.
     *
     * <p>O original junta as duas contas num número só — o x nos bits de cima e o z nos de baixo —, e é o que
     * se faz aqui, para o que fica gravado ser o mesmo.
     */
    public static boolean guardaLugar(Player quem, int pedaçoX, int pedaçoZ) {
        long onde = (long) pedaçoX << 32 | (long) pedaçoZ & 0xFFFFFFFFL;
        WerewolfQuest era = de(quem);
        if (era.lugares().contains(onde)) return false;

        var lugares = new java.util.ArrayList<>(era.lugares());
        lugares.add(onde);
        quem.setAttached(DATA, new WerewolfQuest(era.estado(), era.conta(), List.copyOf(lugares)));
        return true;
    }

    /** Apaga tudo: o que o {@code setWerewolfLevel} faz a cada mudança de grau. */
    public static void limpa(Player quem) {
        quem.setAttached(DATA, NADA);
    }
}
