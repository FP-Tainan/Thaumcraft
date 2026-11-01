package net.thaumcraft.occulta.infusion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;

/**
 * Os <b>Cozimentos Infundidos</b>: o {@code InfusedBrewEffect} do Witchery.
 *
 * <p>São dois, e não se parecem com mais nada que saia de um caldeirão. Um cozimento comum é uma poção:
 * dura minutos, vê-se na lista de efeitos, e o leite tira-o. Um <b>infundido</b> não é uma poção — é uma
 * <b>condição</b> que dura <b>duas horas</b> de jogo, não aparece em lista nenhuma e não se tira de
 * maneira nenhuma a não ser esperando.
 *
 * <p>E não saem do caldeirão: saem de um <b>rito</b>, cada um com um sacrifício vivo. Por isso são caros, e
 * por isso duram tanto.
 *
 * <h2>Os dois</h2>
 *
 * <p>O <b>de Voar</b> não faz nada por si: ele faz a <b>vassoura andar mais depressa</b>, e é só isso. É o
 * irmão da maestria da coruja, e os dois somam.
 *
 * <p>O <b>da Cova</b> faz três coisas, e é o mais estranho do mod inteiro:
 *
 * <ul>
 *   <li>quem o bebeu <b>conta como morto-vivo</b> — o que vale a quem perguntar pelo feitio dele;</li>
 *   <li><b>matar cura</b>: um animal dá um décimo da vida e um pouco de comida, um aldeão quase meia, e
 *       outra pessoa <b>três quintos</b> dela e a barriga cheia;</li>
 *   <li>e com a <b>Pedra Necrótica</b> na mão, olhar para um morto-vivo a quinze blocos <b>escraviza-o</b>,
 *       de dez em dez segundos.</li>
 * </ul>
 *
 * <h2>Uma condição, não uma poção</h2>
 *
 * <p>Guarda-se num apego de jogador — qual dos dois, e <b>desde quando</b>. O que acaba a condição é o
 * relógio do mundo passar das duas horas, e quem o vê passar é o tique do servidor.
 *
 * <p><b>E ela não atravessa a morte.</b> No original o estado vive no {@code PlayerPersisted}, que
 * atravessa; aqui não atravessa, e é uma <b>mudança declarada</b>: um cozimento que faz de alguém um
 * morto-vivo e lhe dá de comer matando devia acabar quando ele morre. O original guarda-o por descuido do
 * lugar onde o pôs, e não por escolha.
 */
public final class InfusedBrews {
    /** Quanto dura: as duas horas de jogo do original. */
    public static final long DURA = 144000L;

    /** Os dois, pelos números do original. */
    public static final int NENHUM = 0;
    public static final int VOAR = 1;
    public static final int COVA = 2;

    /** O que a vassoura ganha com o de Voar: o mesmo décimo que o original lhe soma. */
    public static final double VOAR_EMPURRÃO = 0.1;

    /** De quanto em quanto tempo a Pedra Necrótica escraviza, com o da Cova: dez segundos. */
    public static final long ESPERA_DA_COVA = 200L;

    /** E a quantos blocos ela alcança. */
    public static final double ALCANCE_DA_COVA = 15.0;

    /** O que matar devolve a quem bebeu o da Cova: comida e parte da vida inteira. */
    public static final float DE_GENTE = 0.6f;
    public static final float DE_ALDEÃO = 0.4f;
    public static final float DE_BICHO = 0.1f;

    /**
     * O que um jogador carrega: <b>qual</b> dos dois, <b>desde quando</b>, e quando a pedra escravizou da
     * última vez.
     */
    public record Infundido(int qual, long desde, long últimaPedra) {
        public static final Infundido NADA = new Infundido(NENHUM, 0L, 0L);

        public static final Codec<Infundido> CODEC = RecordCodecBuilder.create(campo -> campo.group(
                Codec.INT.fieldOf("qual").forGetter(Infundido::qual),
                Codec.LONG.fieldOf("desde").forGetter(Infundido::desde),
                Codec.LONG.optionalFieldOf("pedra", 0L).forGetter(Infundido::últimaPedra)
        ).apply(campo, Infundido::new));
    }

    /** O lado de cá também o vê, para saber quanto falta. */
    public static final StreamCodec<RegistryFriendlyByteBuf, Infundido> PELA_REDE =
            ByteBufCodecs.fromCodecWithRegistries(Infundido.CODEC);

    public static final AttachmentType<Infundido> INFUNDIDO = AttachmentRegistry.<Infundido>builder()
            .initializer(() -> Infundido.NADA)
            .persistent(Infundido.CODEC)
            .syncWith(PELA_REDE, AttachmentSyncPredicate.targetOnly())
            .buildAndRegister(Thaumcraft.id("infused_brew"));

    private InfusedBrews() {
    }

    public static Infundido de(Player quem) {
        return quem.getAttachedOrCreate(INFUNDIDO);
    }

    /** Qual deles está a correr, contando já o relógio. */
    public static int qual(Player quem) {
        Infundido tem = de(quem);
        if (tem.qual() == NENHUM) return NENHUM;
        if (quem.level().getGameTime() > tem.desde() + DURA) return NENHUM;
        return tem.qual();
    }

    public static boolean tem(Player quem, int qual) {
        return qual(quem) == qual;
    }

    /** Quantas batidas ainda faltam, ou zero. */
    public static long falta(Player quem) {
        Infundido tem = de(quem);
        if (tem.qual() == NENHUM) return 0L;
        return Math.max(0L, tem.desde() + DURA - quem.level().getGameTime());
    }

    /** Bebeu-se um: o relógio começa agora, e o que houvesse antes é esquecido. */
    public static void bebe(Player quem, int qual) {
        quem.setAttached(INFUNDIDO, new Infundido(qual, quem.level().getGameTime(), 0L));
    }

    /** A pedra escravizou agora: marca-se a hora. */
    public static void marcaAPedra(Player quem) {
        Infundido tem = de(quem);
        quem.setAttached(INFUNDIDO,
                new Infundido(tem.qual(), tem.desde(), quem.level().getGameTime()));
    }

    /**
     * Se a pedra já pode outra vez.
     *
     * <p>O zero quer dizer <b>nunca usou</b>, e não «usou na batida zero»: um mundo recém-nascido tem o
     * relógio abaixo dos dez segundos, e sem esta linha a primeira pedra de um mundo novo não pegava.
     */
    public static boolean aPedraPode(Player quem) {
        long última = de(quem).últimaPedra();
        return última <= 0L || quem.level().getGameTime() - última > ESPERA_DA_COVA;
    }

    /**
     * O relógio: passadas as duas horas, o apego apaga-se.
     *
     * <p>Apaga-se <b>mesmo</b>, e não só por conta: enquanto ele estiver lá o lado de cá desenha o tempo
     * que falta, e um tempo que falta negativo é uma coisa feia de se ver.
     */
    public static void tique(ServerLevel level) {
        for (var quem : level.players()) {
            Infundido tem = de(quem);
            if (tem.qual() == NENHUM) continue;
            if (level.getGameTime() <= tem.desde() + DURA) continue;
            quem.setAttached(INFUNDIDO, Infundido.NADA);
        }
    }

    public static void init() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_LEVEL_TICK
                .register(level -> {
                    if (level.getGameTime() % 100L == 0L) tique(level);
                });
    }
}
