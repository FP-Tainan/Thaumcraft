package net.thaumcraft.occulta;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.thaumcraft.Thaumcraft;

/**
 * Os sons do ofício: os <b>sessenta e quatro</b> do Witchery, os mesmos arquivos.
 *
 * <p>Até esta fatia, o Ars Occulta tocava <b>som emprestado do Minecraft</b> em todo lugar: um devorador
 * rugia no lugar do lobisomem, um bloco de notas estalava no lugar do baú. Era um remendo honesto e estava
 * declarado, mas era um remendo — e o porte só faz sentido se for o original.
 *
 * <p>Agora são os <b>noventa arquivos</b> que vieram no jar de 2014, nos mesmos agrupamentos em que ele os
 * tinha: um nome de evento pode ter mais de um arquivo, e o jogo sorteia. O nome deles aqui é o nome que o
 * original lhes deu, com {@code occulta.} na frente para não se confundirem com os do Thaumcraft.
 *
 * <p>É de propósito que isto venha antes das fatias que faltam: as armadilhas, as minas e a paliçada já
 * nascem com o som certo, em vez de nascerem com um empréstimo que depois alguém teria de vir trocar.
 */
public final class OccultaSounds {

    // ---------------------------------------------------------------- o que se faz com as mãos

    /** O <b>estalo da armadilha</b> fechando: o {@code random.mantrap}. */
    public static final Holder<SoundEvent> MANTRAP = register("random.mantrap");
    /** O <b>clique</b> de armar e desarmar: o {@code random.click}. */
    public static final Holder<SoundEvent> CLICK = register("random.click");
    /** O <b>giz</b> riscando o chão. */
    public static final Holder<SoundEvent> CHALK = register("random.chalk");
    /** O <b>gole</b>: o som de beber do ofício, que não é o do jogo. */
    public static final Holder<SoundEvent> DRINK = register("random.drink");
    /** A <b>hipnose</b> pegando. */
    public static final Holder<SoundEvent> HYPNOSIS = register("random.hypnosis");
    /** A <b>espada saindo</b> da bengala. */
    public static final Holder<SoundEvent> SWORD_DRAW = register("random.sworddraw");
    /** E <b>voltando</b> para dentro dela. */
    public static final Holder<SoundEvent> SWORD_SHEATHE = register("random.swordsheathe");
    /** A <b>corda</b> do que se dá corda. */
    public static final Holder<SoundEvent> WIND_UP = register("random.wind_up");
    /** O <b>chifre da caça</b> chamando o Caçador. */
    public static final Holder<SoundEvent> HORN = register("random.horn");

    // ---------------------------------------------------------------- o que os lugares fazem

    /** O <b>coração batendo</b>, que é o som do Coração de Demônio. */
    public static final Holder<SoundEvent> HEARTBEAT = register("random.heartbeat");
    /** O <b>blop</b> da coisa que afunda no caldeirão. */
    public static final Holder<SoundEvent> BLOP = register("random.blop");
    /** A <b>madeira rangendo</b>: o baú que range, a porta que se recusa. */
    public static final Holder<SoundEvent> WOOD_CREAK = register("random.wood_creak");
    /** O <b>chape</b> do cozimento se espalhando. */
    public static final Holder<SoundEvent> SPLASH = register("random.splash");
    /** O <b>sumiço</b>: o que vai embora sem se despedir. */
    public static final Holder<SoundEvent> POOF = register("random.poof");
    /** O <b>eles vêm</b>: o aviso de que alguma coisa foi chamada. */
    public static final Holder<SoundEvent> THEY_COME = register("random.theycome");
    /** O <b>amado</b>: o som do que foi enfeitiçado para gostar. */
    public static final Holder<SoundEvent> LOVED = register("random.loved");

    // ---------------------------------------------------------------- o lobisomem

    /** O <b>uivo</b>, que é o que o faz. */
    public static final Holder<SoundEvent> WOLFMAN_HOWL = register("mob.wolfman.howl");
    /** A <b>fala</b> dele, que são duas. */
    public static final Holder<SoundEvent> WOLFMAN_SAY = register("mob.wolfman.say");
    /** O <b>golpe</b> que ele leva. */
    public static final Holder<SoundEvent> WOLFMAN_HIT = register("mob.wolfman.hit");
    /** A <b>morte</b> dele. */
    public static final Holder<SoundEvent> WOLFMAN_DEATH = register("mob.wolfman.death");
    /** Ele <b>comendo</b>. */
    public static final Holder<SoundEvent> WOLFMAN_EAT = register("mob.wolfman.eat");
    /** E o <b>Senhor dos Lobos</b>: o som que a Armadilha de Prata faz quando apanha o certo. */
    public static final Holder<SoundEvent> WOLFMAN_LORD = register("mob.wolfman.lord");

    // ---------------------------------------------------------------- os bichos do ofício

    /** A <b>coruja</b> piando, que são duas. */
    public static final Holder<SoundEvent> OWL_HOOT = register("mob.owl.owl_hoot");
    /** E a coruja <b>machucada</b>. */
    public static final Holder<SoundEvent> OWL_HURT = register("mob.owl.owl_hurt");
    /** O <b>sapo</b> coaxando, que são dois. */
    public static final Holder<SoundEvent> TOAD_CROAK = register("mob.toad.toad_croak");
    /** E o sapo <b>machucado</b>. */
    public static final Holder<SoundEvent> TOAD_HURT = register("mob.toad.toad_hurt");
    /** O <b>Treefyd</b> falando, que é a árvore que anda. */
    public static final Holder<SoundEvent> TREEFYD_SAY = register("mob.treefyd.treefyd_say");
    /** A <b>Marca Escura</b> parada no lugar dela. */
    public static final Holder<SoundEvent> DARKMARK_IDLE = register("mob.darkmark.idle");

    // ---------------------------------------------------------------- o duende

    /** O <b>duende</b> parado, que são três. */
    public static final Holder<SoundEvent> GOBLIN_IDLE = register("mob.goblin.idle");
    /** Ele <b>regateando</b>, que são três. */
    public static final Holder<SoundEvent> GOBLIN_HAGGLE = register("mob.goblin.haggle");
    /** Ele <b>aceitando</b>, que são três. */
    public static final Holder<SoundEvent> GOBLIN_YES = register("mob.goblin.yes");
    /** E <b>recusando</b>, que são três. */
    public static final Holder<SoundEvent> GOBLIN_NO = register("mob.goblin.no");
    /** O <b>golpe</b> que ele leva, que são quatro. */
    public static final Holder<SoundEvent> GOBLIN_HIT = register("mob.goblin.hit");
    /** A <b>morte</b> dele. */
    public static final Holder<SoundEvent> GOBLIN_DEATH = register("mob.goblin.death");
    /** O <b>Gulg</b>, que é o duende grande. */
    public static final Holder<SoundEvent> GULG_IDLE = register("mob.goblin.gulg_idle");
    /** E o <b>Mog</b>. */
    public static final Holder<SoundEvent> MOG_IDLE = register("mob.goblin.mog_idle");

    // ---------------------------------------------------------------- os que vêm de longe

    /** A <b>Baba Yagá</b> viva. */
    public static final Holder<SoundEvent> BABA_LIVING = register("mob.baba.baba_living");
    /** E a <b>morte</b> dela. */
    public static final Holder<SoundEvent> BABA_DEATH = register("mob.baba.baba_death");
    /** O <b>grito da banshee</b>, que é o que ela é. */
    public static final Holder<SoundEvent> BANSHEE_SCREAM = register("mob.banshee.banshee_scream");
    /** O <b>espectro</b> falando. */
    public static final Holder<SoundEvent> SPECTRE_SAY = register("mob.spectre.spectre_say");
    /** O <b>golpe</b> nele. */
    public static final Holder<SoundEvent> SPECTRE_HIT = register("mob.spectre.spectre_hit");
    /** E ele <b>indo embora</b>. */
    public static final Holder<SoundEvent> SPECTRE_DIE = register("mob.spectre.spectre_die");
    /** O <b>pesadelo</b> vivo. */
    public static final Holder<SoundEvent> NIGHTMARE_LIVE = register("mob.nightmare.nightmare_live");
    /** O <b>golpe</b> nele. */
    public static final Holder<SoundEvent> NIGHTMARE_HIT = register("mob.nightmare.nightmare_hit");
    /** E a <b>morte</b> dele. */
    public static final Holder<SoundEvent> NIGHTMARE_DEAD = register("mob.nightmare.nightmare_dead");
    /** O <b>Reflexo</b> falando. */
    public static final Holder<SoundEvent> REFLECTION_SAY = register("mob.reflection.say");
    /** O <b>golpe</b> nele. */
    public static final Holder<SoundEvent> REFLECTION_HIT = register("mob.reflection.hit");
    /** A <b>morte</b> dele. */
    public static final Holder<SoundEvent> REFLECTION_DEATH = register("mob.reflection.death");
    /** E a <b>fala</b> dele quando tem alguma coisa a dizer. */
    public static final Holder<SoundEvent> REFLECTION_SPEECH = register("mob.reflection.speech");

    // ---------------------------------------------------------------- os que ainda não vieram

    /** O <b>diabrete</b> rindo. */
    public static final Holder<SoundEvent> IMP_LAUGH = register("mob.imp.laugh");
    /** O <b>golpe</b> nele. */
    public static final Holder<SoundEvent> IMP_HIT = register("mob.imp.hit");
    /** E a <b>morte</b> dele. */
    public static final Holder<SoundEvent> IMP_DEATH = register("mob.imp.death");
    /** O <b>macaco de asas</b> falando, que são três. */
    public static final Holder<SoundEvent> MONKEY_SAY = register("mob.monkey.say");
    /** O <b>golpe</b> nele. */
    public static final Holder<SoundEvent> MONKEY_HIT = register("mob.monkey.hit");
    /** E a <b>morte</b> dele. */
    public static final Holder<SoundEvent> MONKEY_DEATH = register("mob.monkey.death");
    /** O <b>Senhor do Tormento</b> rindo. */
    public static final Holder<SoundEvent> TORMENT_LAUGH = register("mob.torment.laugh");
    /** O <b>golpe</b> nele. */
    public static final Holder<SoundEvent> TORMENT_HIT = register("mob.torment.hit");
    /** E a <b>morte</b> dele. */
    public static final Holder<SoundEvent> TORMENT_DEATH = register("mob.torment.death");
    /** <b>Leonardo</b> falando, que são cinco. */
    public static final Holder<SoundEvent> LEONARD_SAY = register("mob.leonard.say");
    /** O <b>golpe</b> nele. */
    public static final Holder<SoundEvent> LEONARD_HIT = register("mob.leonard.hit");
    /** E a <b>morte</b> dele. */
    public static final Holder<SoundEvent> LEONARD_DEATH = register("mob.leonard.death");
    /** <b>Lilith</b> falando, que são cinco. */
    public static final Holder<SoundEvent> LILITH_SAY = register("mob.lilith.say");
    /** O <b>golpe</b> nela. */
    public static final Holder<SoundEvent> LILITH_HIT = register("mob.lilith.hit");
    /** E a <b>morte</b> dela. */
    public static final Holder<SoundEvent> LILITH_DEATH = register("mob.lilith.death");

    private OccultaSounds() {
    }

    private static Holder<SoundEvent> register(String nome) {
        Identifier id = Thaumcraft.id("occulta." + nome);
        return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, id,
                SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {
    }
}
