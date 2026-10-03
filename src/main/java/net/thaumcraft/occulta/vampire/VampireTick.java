package net.thaumcraft.occulta.vampire;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/**
 * <b>O relógio do vampiro</b>: o pedaço do {@code GenericEvents} que, de duas em duas segundos, cobra o que
 * ser vampiro custa.
 *
 * <p>É o mesmo compasso do {@linkplain net.thaumcraft.occulta.wolf.WerewolfTick relógio da lua} — quarenta
 * batidas —, e faz nove coisas, pela ordem do original:
 *
 * <ol>
 *   <li><b>A visão</b>, se ele a ligou: visão noturna enquanto a quiser;</li>
 *   <li><b>o veneno some</b>, como no lobisomem: não há sangue vivo para envenenar;</li>
 *   <li><b>ele não se afoga</b>: na água, o ar dele volta ao cheio. É o que faz do fundo de um lago o único
 *       lugar onde um vampiro está a salvo <b>de dia</b>, e o original não o diz em lugar nenhum;</li>
 *   <li><b>a forma de morcego cobra um</b>: sem sangue, as asas somem onde ele estiver;</li>
 *   <li><b>o fogo queima na mesma</b>: ardendo <i>com</i> Resistência ao Fogo, ele leva dois de uma dor que
 *       é só dele. A poção que salva todo mundo não salva um vampiro;</li>
 *   <li><b>o sangue vira comida</b>: cinco de sangue por um de comida, até encher. É o único jeito de um
 *       vampiro comer;</li>
 *   <li><b>e sem sangue nem comida vem a maldição da sede</b>: Fraqueza IX, Lentidão II e Fadiga II. Não é
 *       um aviso, é um fim de jogo em câmara lenta;</li>
 *   <li><b>o sol</b>, que é a parte mais bonita e tem <b>quatro degraus</b>;</li>
 *   <li>e, para quem <b>não</b> é vampiro, os dois de sangue que o corpo repõe.</li>
 * </ol>
 *
 * <h2>O sol, nos seus quatro degraus</h2>
 *
 * <p>Ao sol, e fora do criativo:
 *
 * <ul>
 *   <li><b>sem sangue</b>, e passados os primeiros vinte segundos de vida, ele <b>morre ali</b> — morte
 *       direta, sem dano, sem armadura que valha;</li>
 *   <li><b>do quinto grau</b> em diante ele <b>aguenta</b>: perde sessenta de sangue e apanha Fraqueza IV,
 *       Lentidão e Fadiga. Um vampiro velho anda de dia, mal e devagar, e <b>pagando</b>;</li>
 *   <li><b>abaixo do quinto</b>, o sol lhe <b>zera o sangue de uma vez</b>. Não há aguentar: há correr;</li>
 *   <li>e, zerado o sangue de um jeito ou de outro, ele <b>pega fogo</b>.</li>
 * </ul>
 *
 * <p>A ordem importa e é do original: o sol zera primeiro, o fogo vem depois — e por isso um vampiro de grau
 * baixo que ponha o nariz ao sol com o sangue cheio <b>arde na mesma</b>, porque o sol lhe tirou tudo antes
 * de perguntar.
 */
public final class VampireTick {
    /** De quanto em quanto se cobra: as quarenta batidas do original. */
    public static final int DE_QUANTO_EM_QUANTO = 40;

    /** O que o sangue vale em comida: cinco por um, com fartura cheia. */
    public static final int SANGUE_POR_COMIDA = 5;
    public static final float FARTURA = 4.0f;

    /** A dor de arder, que a Resistência ao Fogo não apara. */
    public static final float ARDE = 2.0f;

    /** A maldição da sede, e quanto ela dura. */
    public static final int SEDE = 200;
    public static final int FRAQUEZA_DA_SEDE = 8;
    public static final int LENTIDÃO_DA_SEDE = 1;

    /** O sol: o que ele tira, o que ele faz doer, e a partir de que grau se aguenta. */
    public static final int AGUENTA_O_SOL_AOS = 5;
    public static final int O_SOL_TIRA = 60;
    public static final int CASTIGO = 200;
    public static final int FRAQUEZA_DO_SOL = 3;
    public static final int PEGA_FOGO = 5;

    /** E os vinte segundos de graça que todo vampiro recém-nascido tem ao sol. */
    public static final int GRAÇA = 400;

    /** O ar que um vampiro tem sempre: ele não se afoga. */
    public static final int AR = 300;

    private VampireTick() {
    }

    /** Põe o relógio a andar. */
    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % DE_QUANTO_EM_QUANTO != 21) return;
            for (ServerLevel level : server.getAllLevels()) {
                for (Player quem : level.players()) cobra(level, quem);
            }
        });
    }

    /** O que ser vampiro custa a esta pessoa agora. */
    public static void cobra(ServerLevel level, Player quem) {
        if (!Vampire.é(quem)) {
            // quem não é vampiro repõe o sangue que lhe tiraram
            Blood.dá(quem, Blood.RECUPERA);
            return;
        }

        if (VampirePowers.vêNoEscuro(quem)) {
            quem.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false));
        }
        quem.removeEffect(MobEffects.POISON);

        /*
         * Ele não se afoga: o ar dele volta ao cheio a cada volta do relógio enquanto estiver na água. O
         * original não o explica em lugar nenhum, e é o que faz da água um caminho de fuga de um vampiro —
         * o sol não pega debaixo dela, e o fundo de um lago é o único lugar onde ele está a salvo de dia.
         */
        if (quem.isInWater()) quem.setAirSupply(AR);

        /*
         * E a forma de morcego cobra um de sangue por volta. Não há aviso: acabando o sangue, ele deixa
         * de ser morcego onde estiver — e se estiver no ar, cai. Um vampiro que voe com a barra no fim está
         * apostando.
         */
        if (VampirePowers.emMorcego(quem)
                && !Vampire.gasta(quem, VampirePowers.Poder.MORCEGO.mantém, true)) {
            VampirePowers.tiraOMorcego(quem);
        }

        // a Resistência ao Fogo não salva um vampiro que arde
        if (quem.isOnFire() && quem.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            quem.hurtServer(level, VampireHurt.fogo(level), ARDE);
        }

        oSangueVisteDeComida(quem);
        aSede(quem);
        oSol(level, quem);
    }

    /**
     * <b>O sangue vira comida</b>: cinco por um, até encher.
     *
     * <p>Um vampiro não come — ele <b>converte</b>. E por isso a comida dele e a magia dele saem do mesmo
     * lugar: cada pão é poder de sangue que ele deixou de ter.
     */
    private static void oSangueVisteDeComida(Player quem) {
        var fome = quem.getFoodData();
        while (fome.getFoodLevel() < 20 && Vampire.gasta(quem, SANGUE_POR_COMIDA, true)) {
            VampireHunger.alimenta(quem, 1, FARTURA);
        }
    }

    /** E sem sangue nem comida, a maldição da sede. */
    private static void aSede(Player quem) {
        if (Vampire.sangueDe(quem) != 0 || quem.getFoodData().getFoodLevel() != 0) return;
        quem.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, SEDE, FRAQUEZA_DA_SEDE, true, false));
        quem.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SEDE, LENTIDÃO_DA_SEDE, true, false));
        quem.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, SEDE, LENTIDÃO_DA_SEDE, true, false));
    }

    /** O sol, nos seus quatro degraus. */
    private static void oSol(ServerLevel level, Player quem) {
        if (quem.getAbilities().instabuild) return;
        if (!aoSol(level, quem)) return;

        if (Vampire.sangueDe(quem) == 0 && quem.tickCount > GRAÇA) {
            quem.hurtServer(level, VampireHurt.sol(level), Float.MAX_VALUE);
            return;
        }

        if (Vampire.grauDe(quem) >= AGUENTA_O_SOL_AOS) {
            Vampire.gasta(quem, O_SOL_TIRA, false);
            quem.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, CASTIGO, FRAQUEZA_DO_SOL, false, false));
            quem.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, CASTIGO, 0, true, false));
            quem.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, CASTIGO, 0, true, false));
        } else {
            Vampire.sangue(quem, 0);
        }

        if (Vampire.sangueDe(quem) == 0) quem.igniteForSeconds(PEGA_FOGO);
    }

    /**
     * Se o sol lhe pega: o {@code CreatureUtil.isInSunlight}.
     *
     * <p><b>Céu aberto, dia, e sem chuva</b> — e é a chuva que faz do mod uma coisa jogável: num dia de
     * tempestade, um vampiro anda à vontade.
     */
    public static boolean aoSol(ServerLevel level, Player quem) {
        if (!level.isBrightOutside()) return false;
        if (level.isRaining()) return false;
        return level.canSeeSky(net.minecraft.core.BlockPos.containing(
                quem.getX(), quem.getEyeY(), quem.getZ()));
    }

    /** Arruma o corpo para o grau em que ele está. */
    public static void arruma(ServerLevel level, Player quem) {
        VampireStats.põe(quem);
    }
}
