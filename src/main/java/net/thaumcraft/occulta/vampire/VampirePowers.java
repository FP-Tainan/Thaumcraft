package net.thaumcraft.occulta.vampire;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.wolf.Lycanthropy;

/**
 * O que um vampiro <b>faz</b>: os {@code VampirePower} do {@code ExtendedPlayer}.
 *
 * <p>Um vampiro não tem botões: tem um <b>poder escolhido</b>, e o que ele faz com o mundo depende de qual
 * seja. Enquanto houver um poder escolhido, o clique direito deixa de abrir baús e passa a <b>ser</b> esse
 * poder — e é por isso que escolher <i>nenhum</i> é uma escolha legítima e a que ele passa mais tempo a
 * fazer.
 *
 * <p>Os cinco são: <b>beber</b>, <b>prender pelo olhar</b>, a <b>velocidade</b>, a <b>forma de morcego</b> e
 * o <b>Supremo</b>.
 *
 * <h2>Beber, que é o que o sustenta</h2>
 *
 * <p>Com o poder de beber escolhido, tocar num vivo a <b>um bloco e três décimos</b> — <b>dois e um</b>, se
 * ele estiver paralisado, porque a presa não foge — lhe tira sangue. E o que se tira depende de quem é:
 *
 * <ul>
 *   <li><b>aldeão</b> ou <b>gente</b>: dez de sangue, e é disto que ele vive;</li>
 *   <li><b>bicho</b>: dois, e <b>nunca acima de um quarto do teto</b> — sangue de bicho mantém vivo e não
 *       faz forte;</li>
 *   <li>e <b>lobisomem</b>: nada. Pior: <b>quatro de dor</b> em quem mordeu, e uma labareda. O sangue de um
 *       lobisomem é veneno para um vampiro, e é a única regra do mod em que as duas maldições se encontram.
 *       </li>
 * </ul>
 *
 * <p>Em <b>forma de morcego</b> sai só <b>dois</b>, seja de quem for: um morcego não tem boca para isso.
 *
 * <p>E morder um aldeão <b>tem testemunhas</b> — os guardas que virem passam a caçar quem mordeu.
 *
 * <h2>E os outros quatro, que são os que custam</h2>
 *
 * <p>Os quatro se usam do mesmo jeito: <b>o botão de usar, no ar</b>. Não há tecla para cada um, não há menu
 * e não há roda — há o poder escolhido e um botão. Falhando, o que se ouve é um <b>toque de caixa</b>, e
 * nenhuma palavra: o mod nunca explica por quê, e deixa quem joga descobrir.
 *
 * <p>Repare no que eles <b>não</b> são. Nenhum dos quatro é um golpe. <b>Prender</b> deixa a presa quieta,
 * <b>correr</b> o leva mais depressa, o <b>morcego</b> o leva por cima, e os três <b>Supremos</b> mudam o
 * tempo, chamam bichos ou o põem noutro lugar. Um vampiro de décimo grau tem cinco poderes e nenhum deles
 * serve para vencer uma briga — e é de propósito. Ele vence por <b>chegar antes</b>.
 */
public final class VampirePowers {
    /**
     * O que escolher, e o que cada um custa.
     *
     * <p>Os cinco são <b>escadas</b>: cada um abre a um grau diferente, e o <b>Supremo</b> é o último de
     * todos. A conta de quantos ele tem está no {@link #quantosPode}, e ela é a tabela do original.
     */
    public enum Poder implements net.minecraft.util.StringRepresentable {
        /** Nenhum, que é o estado normal e deixa o mundo funcionar. */
        NENHUM("nenhum", 0, 0, 0),
        /** <b>Beber</b>, que é o que o sustenta — e não custa nada, porque é a comida dele. */
        BEBER("beber", 0, 0, 1),
        /** <b>Prender pelo olhar</b>, e ligar a visão: cinquenta de sangue, do segundo grau. */
        PRENDER("prender", 50, 0, 2),
        /** A <b>velocidade</b>, que dobra de cada vez: dez de sangue, do quarto grau. */
        VELOCIDADE("velocidade", 10, 0, 4),
        /** A <b>forma de morcego</b>: cinquenta para entrar, e <b>um por volta do relógio</b> para ficar. */
        MORCEGO("morcego", 50, 1, 7),
        /** E o <b>Supremo</b>, que é um de três e se escolhe no Crisol de Sangue: só ao décimo grau. */
        SUPREMO("supremo", 50, 0, 10);

        /** O que custa para usar, o que custa para manter, e a que grau ele abre. */
        public final int custa;
        public final int mantém;
        public final int grau;

        private final String nome;

        Poder(String nome, int custa, int mantém, int grau) {
            this.nome = nome;
            this.custa = custa;
            this.mantém = mantém;
            this.grau = grau;
        }

        @Override
        public String getSerializedName() {
            return this.nome;
        }
    }

    /**
     * <b>Quantos poderes ele já tem</b>: a tabela {@code levels} do original.
     *
     * <p>Ela não é o grau: é uma escada que para e anda. Do primeiro ao décimo grau ele vai tendo <b>um,
     * dois, dois, três, três, três, quatro, quatro, quatro e cinco</b> — e é por isso que subir de grau nem
     * sempre dá um poder novo, e que o último custa os dez.
     */
    public static final int[] QUANTOS = {0, 1, 2, 2, 3, 3, 3, 4, 4, 4, 5};

    /** Quantos poderes esta pessoa pode escolher. */
    public static int quantosPode(Player quem) {
        return QUANTOS[Math.clamp(Vampire.grauDe(quem), 0, Vampire.TETO)];
    }

    /**
     * Os três <b>Supremos</b>, que se escolhem no Crisol de Sangue e vêm com cinco usos.
     *
     * <p>Repare no que eles são: um chama a <b>tempestade</b>, que é a noite feita à força; outro chama um
     * <b>enxame</b>, que é o vampiro deixando de lutar sozinho; e o terceiro <b>leva para casa</b> — e,
     * estando em casa, leva para onde há gente. Nenhum dos três é um golpe. Os três são <b>maneiras de mudar
     * onde se está</b>, e é isso que um vampiro é.
     */
    public enum Supremo implements net.minecraft.util.StringRepresentable {
        /** Nenhum ainda. */
        NENHUM("nenhum"),
        /** A <b>tempestade</b>, que apaga o sol. */
        TEMPESTADE("tempestade"),
        /** O <b>enxame</b> de quinze morcegos. */
        ENXAME("enxame"),
        /** E o <b>caminho de casa</b>. */
        CASA("casa");

        private final String nome;

        Supremo(String nome) {
            this.nome = nome;
        }

        @Override
        public String getSerializedName() {
            return this.nome;
        }
    }

    /** A que distância ele alcança, e a que distância alcança quem não pode fugir. */
    public static final double ALCANCE = 1.3;
    public static final double ALCANCE_DE_PRESA_PRESA = 2.1;

    /** Quanto ele tira de cada um. */
    public static final int GOLE = 10;
    public static final int GOLE_DE_MORCEGO = 2;
    public static final int GOLE_DE_BICHO = 2;

    /** E o que o sangue de lobisomem lhe faz. */
    public static final float SANGUE_DE_LOBO = 4.0f;

    /** A que distância um guarda vê uma mordida. */
    public static final double TESTEMUNHAS = 16.0;

    /** Quantos usos um Supremo traz quando se escolhe no Crisol. */
    public static final int USOS = 5;

    /** A espera entre dois usos de um poder, que é meio segundo. */
    public static final int ESPERA = 10;

    /** O que o prender dá: o grau da paralisia, e o que ele sobe ao oitavo grau. */
    public static final int PRENDE_GRAU = 4;
    public static final int PRENDE_GRAU_ALTO = 5;
    public static final int PRENDE_AOS = 8;
    public static final int PRENDE_BASE = 5;

    /** A velocidade: quanto dura de novo, e quanto se soma a cada vez. */
    public static final int CORRE = 200;
    public static final int CORRE_MAIS = 60;

    /** A forma de morcego: o tamanho dela, e o dano que ela tira. */
    public static final float MORCEGO_LARGURA = 0.3f;
    public static final float MORCEGO_ALTURA = 0.6f;
    public static final float MORCEGO_OLHOS = 0.8f;
    public static final float MORCEGO_DANO = -6.0f;

    /** O enxame: quantos, a que distância e a que altura. */
    public static final int ENXAME = 15;
    public static final int ENXAME_PERTO = 1;
    public static final int ENXAME_LONGE = 4;
    public static final double ENXAME_ALTO = 3.0;

    /** A tempestade: de cinco a quinze minutos. */
    public static final int TEMPESTADE_BASE = 300;
    public static final int TEMPESTADE_MAIS = 600;

    /** E a casa: a que distância da cama ele conta como estando nela, e a que distância há aldeia. */
    public static final double EM_CASA = 6.0;
    public static final int PROCURA_ALDEIA = 512;

    /** O poder escolhido, que o cliente precisa de saber para desenhar e para mandar o clique. */
    public static final AttachmentType<Poder> ESCOLHIDO = AttachmentRegistry.<Poder>builder()
            .initializer(() -> Poder.NENHUM)
            .persistent(net.minecraft.util.StringRepresentable.fromEnum(Poder::values))
            .copyOnDeath()
            .syncWith(ByteBufCodecs.idMapper(i -> Poder.values()[i], Poder::ordinal).cast(),
                    AttachmentSyncPredicate.all())
            .buildAndRegister(Thaumcraft.id("vampire_power"));

    /** E se a visão está ligada, que é um interruptor e não um poder a usar. */
    public static final AttachmentType<Boolean> VÊ_NO_ESCURO = AttachmentRegistry.<Boolean>builder()
            .initializer(() -> false)
            .persistent(com.mojang.serialization.Codec.BOOL)
            .copyOnDeath()
            .syncWith(ByteBufCodecs.BOOL.cast(), AttachmentSyncPredicate.all())
            .buildAndRegister(Thaumcraft.id("vampire_vision"));

    /** A forma de morcego, que o cliente precisa de saber para desenhar. */
    public static final AttachmentType<Boolean> MORCEGO = AttachmentRegistry.<Boolean>builder()
            .initializer(() -> false)
            .persistent(com.mojang.serialization.Codec.BOOL)
            .copyOnDeath()
            .syncWith(ByteBufCodecs.BOOL.cast(), AttachmentSyncPredicate.all())
            .buildAndRegister(Thaumcraft.id("vampire_bat"));

    /** O Supremo que ele escolheu no Crisol, e quantos usos lhe restam. */
    public static final AttachmentType<Supremo> SUPREMO = AttachmentRegistry.<Supremo>builder()
            .initializer(() -> Supremo.NENHUM)
            .persistent(net.minecraft.util.StringRepresentable.fromEnum(Supremo::values))
            .copyOnDeath()
            .syncWith(ByteBufCodecs.idMapper(i -> Supremo.values()[i], Supremo::ordinal).cast(),
                    AttachmentSyncPredicate.all())
            .buildAndRegister(Thaumcraft.id("vampire_ultimate"));

    public static final AttachmentType<Integer> CARGAS = AttachmentRegistry.<Integer>builder()
            .initializer(() -> 0)
            .persistent(com.mojang.serialization.Codec.INT)
            .copyOnDeath()
            .syncWith(ByteBufCodecs.VAR_INT.cast(), AttachmentSyncPredicate.all())
            .buildAndRegister(Thaumcraft.id("vampire_charges"));

    /**
     * E quando foi o último uso, para a espera de meio segundo.
     *
     * <p><b>Declarado</b>: no original isto é um contador que desce uma vez por batida, num campo que não se
     * guarda. Aqui é a <b>batida em que ele usou</b>, e a espera se mede contra ela — a mesma espera, sem
     * precisar de olhar todo jogador do mundo sessenta vezes por segundo para tirar um do nada.
     */
    public static final AttachmentType<Integer> ÚLTIMO = AttachmentRegistry.<Integer>builder()
            .initializer(() -> 0)
            .buildAndRegister(Thaumcraft.id("vampire_cooldown"));

    private VampirePowers() {
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela os apegos. */
    public static void init() {
    }

    // ------------------------------------------------------------------ o que ele escolheu

    public static Poder escolhido(Player quem) {
        if (!Vampire.é(quem)) return Poder.NENHUM;
        return quem.getAttachedOrCreate(ESCOLHIDO);
    }

    public static void escolhe(Player quem, Poder qual) {
        quem.setAttached(ESCOLHIDO, qual);
    }

    /**
     * Passa ao poder seguinte, que é o que a tecla faz.
     *
     * <p>E ele <b>para no que o grau dele dá</b>: chegando ao último que tem, a volta seguinte o põe em
     * <i>nenhum</i>. É a conta do original — ela não salta o que falta, ela <b>não chega lá</b>.
     */
    public static void seguinte(Player quem) {
        if (!Vampire.é(quem)) return;
        int tem = escolhido(quem).ordinal();
        int pode = quantosPode(quem);
        escolhe(quem, tem >= pode ? Poder.NENHUM : Poder.values()[tem + 1]);
    }

    public static boolean vêNoEscuro(Player quem) {
        return Vampire.é(quem) && quem.getAttachedOrCreate(VÊ_NO_ESCURO);
    }

    /** Liga e desliga a visão. */
    public static void viraAVisão(Player quem) {
        boolean vai = !vêNoEscuro(quem);
        quem.setAttached(VÊ_NO_ESCURO, vai);
        if (!vai) quem.removeEffect(MobEffects.NIGHT_VISION);
    }

    // ------------------------------------------------------------------ e o que ele faz com isso

    /**
     * <b>Beber</b>: o ramo do {@code onEntityInteract} que é do vampiro.
     *
     * @return se ele bebeu, e então o jogo não tem mais nada a fazer com esse toque
     */
    public static boolean bebe(ServerLevel level, Player quem, LivingEntity deQuem) {
        if (escolhido(quem) != Poder.BEBER) return false;
        double alcance = Blood.desacordado(deQuem) ? ALCANCE_DE_PRESA_PRESA : ALCANCE;
        if (deQuem.distanceToSqr(quem.getX(), deQuem.getY(), quem.getZ()) > alcance * alcance) return false;

        // o sangue de um lobisomem é veneno
        if (Lycanthropy.éMesmoDeGente(deQuem)) {
            quem.hurtServer(level, level.damageSources().indirectMagic(quem, quem), SANGUE_DE_LOBO);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                    deQuem.getX(), deQuem.getY() + deQuem.getBbHeight() * 0.8, deQuem.getZ(),
                    16, 0.5, 0.2, 0.5, 0.0);
            barulho(level, deQuem);
            return true;
        }

        int gole = emMorcego(quem) ? GOLE_DE_MORCEGO : GOLE;
        if (deQuem instanceof Villager || deQuem instanceof Player) {
            Vampire.bebe(quem, Blood.tira(level, deQuem, gole, quem));
            pó(level, deQuem);
            barulho(level, deQuem);
            if (deQuem instanceof Villager aldeão) testemunhas(level, quem, aldeão);
            return true;
        }
        if (deQuem instanceof Animal) {
            Vampire.bebeDeBicho(quem, GOLE_DE_BICHO);
            pó(level, deQuem);
            barulho(level, deQuem);
            return true;
        }
        return false;
    }

    /** Se ele está em <b>forma de morcego</b>, que é a forma em que o gole é pequeno. */
    public static boolean emMorcego(Player quem) {
        return Vampire.é(quem) && quem.getAttachedOrCreate(MORCEGO);
    }

    /**
     * <b>Quem viu, conta</b>: o {@code checkForBloodDrinkingWitnesses}.
     *
     * <p>Todo guarda de aldeia a <b>dezesseis blocos</b> que esteja de olhos abertos — e não paralisado — vem
     * atrás de quem mordeu. Morder dentro de uma aldeia não é um crime sem vítima: é um crime com guardas.
     */
    public static void testemunhas(ServerLevel level, Player quem, LivingEntity vítima) {
        var roda = vítima.getBoundingBox().inflate(TESTEMUNHAS, TESTEMUNHAS / 2.0, TESTEMUNHAS);
        for (var guarda : level.getEntitiesOfClass(
                net.thaumcraft.occulta.village.VillageGuardEntity.class, roda)) {
            if (guarda.hasEffect(net.thaumcraft.occulta.OccultaEffects.PARALYSIS)) continue;
            if (!guarda.getSensing().hasLineOfSight(vítima)) continue;
            guarda.setTarget(quem);
        }
    }

    private static void pó(ServerLevel level, LivingEntity deQuem) {
        level.sendParticles(DustParticleOptions.REDSTONE,
                deQuem.getX(), deQuem.getY() + deQuem.getBbHeight() * 0.8, deQuem.getZ(),
                16, 0.5, 0.2, 0.5, 0.0);
    }

    private static void barulho(ServerLevel level, LivingEntity deQuem) {
        level.playSound(null, deQuem.blockPosition(), SoundEvents.GENERIC_DRINK.value(),
                SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    // ------------------------------------------------------------------ e os poderes que se usam

    /**
     * <b>Usar o poder escolhido</b>: o {@code triggerSelectedVampirePower} do original.
     *
     * <p>A <b>espera</b> vem antes de tudo e vale para todos: meio segundo entre dois usos, e quem apertar
     * antes ouve o toque de caixa. Ela se põe <b>mesmo quando o poder falha</b>, como no original — tentar
     * custa, mesmo não dando nada.
     */
    public static void usa(ServerLevel level, Player quem) {
        if (!Vampire.é(quem)) return;

        if (quem.tickCount - quem.getAttachedOrCreate(ÚLTIMO) < ESPERA) {
            gago(level, quem);
            return;
        }
        quem.setAttached(ÚLTIMO, quem.tickCount);

        switch (escolhido(quem)) {
            // o ramo do prender que não toca em ninguém: agachado, ele liga a visão
            case PRENDER -> {
                if (quem.isShiftKeyDown()) viraAVisão(quem);
            }
            case VELOCIDADE -> aVelocidade(level, quem);
            case MORCEGO -> oMorcego(level, quem);
            case SUPREMO -> oSupremo(level, quem);
            default -> {
            }
        }
    }

    /**
     * <b>A velocidade que dobra</b>: o ramo mais curioso do original.
     *
     * <p>Cada uso <b>dobra</b> a Rapidez que ele já tem — dois, quatro, oito — e o grau dele diz até onde:
     * {@code ceil((grau - 3) / 2)} doses. Um vampiro de grau quatro corre uma vez; um de grau dez, quatro.
     *
     * <p>E cada dose vem com <b>Salto</b> ao lado, e <b>soma três segundos</b> ao que já estava correndo em
     * vez de recomeçar. Quem quiser a velocidade cheia tem de a construir, dose a dose, antes de precisar
     * dela — e quem a deixar acabar recomeça do dois.
     *
     * <p>De <b>morcego</b> não há velocidade: ele já voa.
     */
    private static void aVelocidade(ServerLevel level, Player quem) {
        if (emMorcego(quem)) {
            gago(level, quem);
            return;
        }

        var corre = quem.getEffect(MobEffects.SPEED);
        int tem = corre == null ? 0
                : (int) Math.ceil(Math.log(corre.getAmplifier() + 1) / Math.log(2.0));
        int pode = (int) Math.ceil((Vampire.grauDe(quem) - 3) / 2.0f);
        if (Vampire.grauDe(quem) < Poder.VELOCIDADE.grau || tem > pode) {
            gago(level, quem);
            return;
        }
        if (!Vampire.gasta(quem, Poder.VELOCIDADE.custa, true)) {
            gago(level, quem);
            return;
        }

        int vai = corre == null ? 2 : (corre.getAmplifier() + 1) * 2;
        int quanto = corre == null ? CORRE : corre.getDuration() + CORRE_MAIS;
        quem.addEffect(new MobEffectInstance(MobEffects.SPEED, quanto, vai - 1, true, true));
        quem.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, quanto, tem + 1, true, true));
        chiado(level, quem);
    }

    /**
     * <b>A forma de morcego</b>: cinquenta para entrar, e um por volta do relógio para ficar.
     *
     * <p>Nela ele <b>voa</b>, não se machuca ao cair, cabe em três décimos de largura por seis de altura — e a
     * pancada dele <b>não vale nada</b>: <b>menos seis</b> de dano, que é o mesmo que dizer que um morcego
     * não bate em ninguém. O gole de sangue dele também passa de dez a <b>dois</b>: um morcego não tem boca
     * para mais.
     *
     * <p>É o poder que mais muda o jogo e o que menos serve para brigar, e é de propósito: ele é para
     * <b>chegar</b>, não para vencer.
     *
     * <p><b>Sair</b> é de graça e não pede nada — nem grau, nem sangue. Quem entrou, sai.
     *
     * <p>E não se é <b>lobo e morcego</b> ao mesmo tempo. No original as duas maldições partilham um único
     * contador de forma, e isso torna a mistura impossível por construção; aqui são dois apegos separados, e
     * por isso a pergunta é feita à mão.
     */
    private static void oMorcego(ServerLevel level, Player quem) {
        if (emMorcego(quem)) {
            tiraOMorcego(quem);
            chiado(level, quem);
            return;
        }
        if (Vampire.grauDe(quem) < Poder.MORCEGO.grau
                || net.thaumcraft.occulta.wolf.Werewolf.emBicho(quem)
                || !Vampire.gasta(quem, Poder.MORCEGO.custa, true)) {
            gago(level, quem);
            return;
        }
        quem.setAttached(MORCEGO, true);
        arruma(quem);
        chiado(level, quem);
    }

    /** Tira a forma de morcego, que é também o que a falta de sangue faz. */
    public static void tiraOMorcego(Player quem) {
        if (!quem.getAttachedOrCreate(MORCEGO)) return;
        quem.setAttached(MORCEGO, false);
        arruma(quem);
    }

    /**
     * Arruma as asas e o corpo: um morcego <b>voa</b> e bate menos, e quem deixa de ser morcego cai.
     *
     * <p>No <b>criativo</b> não se mexe nas asas — elas são dele e não do mod.
     */
    public static void arruma(Player quem) {
        quem.refreshDimensions();
        VampireStats.põe(quem);
        if (quem.getAbilities().instabuild) return;

        boolean voa = emMorcego(quem);
        quem.getAbilities().mayfly = voa;
        if (!voa) quem.getAbilities().flying = false;
        quem.onUpdateAbilities();
    }

    /**
     * E o que o voo pede a cada batida: o {@code Shapeshift.updatePlayerState}.
     *
     * <p>Voando, a <b>queda não conta</b> — e por isso um morcego que se desligue no ar cai de onde estava e
     * não de onde subiu. É o que faz sair da forma, no alto, uma decisão.
     */
    public static void voa(Player quem) {
        if (!emMorcego(quem)) return;
        if (quem.getAbilities().flying) quem.fallDistance = 0.0f;
        if (quem.getAbilities().mayfly || quem.getAbilities().instabuild) return;
        quem.getAbilities().mayfly = true;
        quem.onUpdateAbilities();
    }

    /**
     * <b>Prender pelo olhar</b>: o ramo do {@code MESMERIZE} que toca em alguém.
     *
     * <p>Cinquenta de sangue, e só em <b>gente</b> — aldeão, jogador ou guarda; um bicho não se prende
     * olhando, e um <b>aldeão que vira</b> também não, porque o que corre nele já é outra maldição. Quem
     * apanha fica <b>paralisado</b> por {@code 5 + grau/2 + max(0, (grau-4)/2)} segundos, no grau <b>quatro
     * </b> da poção — ou <b>cinco</b>, do oitavo grau em diante.
     *
     * <p>E é esse grau que faz tudo: a partir do quarto, a {@linkplain Blood#desacordado presa conta como
     * desacordada}, e <b>dá todo o sangue que se lhe pede</b> em vez de dois terços. Prender e beber é o laço
     * inteiro de um vampiro, e é por isso que o prender abre dois graus antes da velocidade.
     *
     * <p><b>Falta, declarado</b>: o original soma três segundos a quem veste as <b>roupas de vampiro</b>, que
     * este porte ainda não tem. Quando elas vierem, é esta conta que leva o mais três.
     *
     * @return se o toque era dele, e então o jogo não tem mais nada a fazer com esse toque
     */
    public static boolean prende(ServerLevel level, Player quem, LivingEntity emQuem) {
        if (escolhido(quem) != Poder.PRENDER) return false;

        if (quem.isShiftKeyDown()) {
            if (Vampire.grauDe(quem) >= Poder.PRENDER.grau) viraAVisão(quem);
            else gago(level, quem);
            return true;
        }
        if (emMorcego(quem) || Vampire.grauDe(quem) < Poder.PRENDER.grau
                || !prendível(emQuem)
                || emQuem.hasEffect(net.thaumcraft.occulta.OccultaEffects.PARALYSIS)
                || !Vampire.gasta(quem, Poder.PRENDER.custa, true)) {
            gago(level, quem);
            return true;
        }

        int grau = Vampire.grauDe(quem);
        int quanto = (PRENDE_BASE + grau / 2 + Math.max(0, (grau - 4) / 2)) * 20;
        emQuem.addEffect(new MobEffectInstance(net.thaumcraft.occulta.OccultaEffects.PARALYSIS, quanto,
                grau >= PRENDE_AOS ? PRENDE_GRAU_ALTO : PRENDE_GRAU));
        level.playSound(null, emQuem.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL,
                SoundSource.PLAYERS, 0.5f, 1.0f);
        return true;
    }

    /** Quem se prende olhando: gente, e só gente. */
    public static boolean prendível(LivingEntity quem) {
        if (quem instanceof net.thaumcraft.occulta.wolf.WereVillagerEntity) return false;
        return quem instanceof Villager
                || quem instanceof Player
                || quem instanceof net.thaumcraft.occulta.village.VillageGuardEntity;
    }

    // ------------------------------------------------------------------ e os três Supremos

    public static Supremo supremo(Player quem) {
        return quem.getAttachedOrCreate(SUPREMO);
    }

    public static int cargas(Player quem) {
        return quem.getAttachedOrCreate(CARGAS);
    }

    /** O Crisol de Sangue dá um Supremo, com cinco usos. */
    public static void dáOSupremo(Player quem, Supremo qual) {
        quem.setAttached(SUPREMO, qual);
        quem.setAttached(CARGAS, USOS);
    }

    /** E cada uso gasta um — menos no criativo, onde nada se gasta. */
    private static void gastaUmUso(Player quem) {
        if (quem.getAbilities().instabuild) return;
        quem.setAttached(CARGAS, Math.max(0, cargas(quem) - 1));
    }

    private static void oSupremo(ServerLevel level, Player quem) {
        boolean pode = Vampire.grauDe(quem) >= Poder.SUPREMO.grau
                && !emMorcego(quem)
                && (cargas(quem) > 0 || quem.getAbilities().instabuild);
        if (!pode) {
            gago(level, quem);
            return;
        }

        boolean deu = switch (supremo(quem)) {
            case TEMPESTADE -> aTempestade(level);
            case ENXAME -> oEnxame(level, quem);
            case CASA -> oCaminhoDeCasa(level, quem);
            case NENHUM -> false;
        };
        if (!deu) {
            gago(level, quem);
            return;
        }
        gastaUmUso(quem);
        chiado(level, quem);
    }

    /**
     * <b>A tempestade</b>: de cinco a quinze minutos de chuva com trovão.
     *
     * <p>É o Supremo mais calado dos três e o mais útil: um temporal <b>tira o sol</b>, e sem sol um vampiro
     * anda de dia. Ele não ataca ninguém — <b>muda o mundo</b> para caber nele.
     *
     * <p>Chovendo já, não faz nada: não se chama o que já veio.
     */
    private static boolean aTempestade(ServerLevel level) {
        if (level.isRaining()) return false;
        int quanto = (TEMPESTADE_BASE + level.getRandom().nextInt(TEMPESTADE_MAIS)) * 20;
        var tempo = level.getWeatherData();
        tempo.setClearWeatherTime(0);
        tempo.setRainTime(quanto);
        tempo.setRaining(true);
        tempo.setThunderTime(quanto);
        tempo.setThundering(true);
        tempo.setDirty();
        return true;
    }

    /**
     * <b>O enxame</b>: quinze morcegos que vão ao que ele estiver olhando, e morrem no primeiro golpe.
     *
     * <p>Eles nascem de um a quatro blocos dele e três acima, e são {@linkplain
     * net.thaumcraft.occulta.NoDrops marcados} para não deixar nada: quinze morcegos a cada uso seriam uma
     * fábrica de couro se caísse algo deles.
     */
    private static boolean oEnxame(ServerLevel level, Player quem) {
        for (int n = 0; n < ENXAME; n++) {
            var bicho = net.thaumcraft.occulta.Spawn.perto(level,
                    net.thaumcraft.occulta.OccultaEntities.ATTACK_BAT,
                    net.minecraft.core.BlockPos.containing(quem.getX(),
                            quem.getY() + ENXAME_ALTO + level.getRandom().nextDouble(), quem.getZ()),
                    ENXAME_PERTO, ENXAME_LONGE);
            if (!(bicho instanceof AttackBatEntity morcego)) continue;
            morcego.dono(quem);
            morcego.setResting(false);
            net.thaumcraft.occulta.NoDrops.marca(morcego);
        }
        return true;
    }

    /**
     * <b>O caminho de casa</b>: o {@code FARM} do original.
     *
     * <p>Leva-o para a <b>cama</b> dele. E se ele já estiver <b>em casa</b> — a seis blocos dela —, leva-o
     * para a <b>aldeia mais perto</b>, que é onde há gente: o Supremo da colheita leva o vampiro ao rebanho
     * dele.
     *
     * <p>Sem cama, é o <b>nascimento do mundo</b> que serve de casa, como no original.
     *
     * <p>Nenhum dos três Supremos é um golpe. Este é o que diz melhor o que eles são: um vampiro não precisa
     * de ganhar uma briga, precisa de <b>estar noutro lugar</b>.
     */
    private static boolean oCaminhoDeCasa(ServerLevel level, Player quem) {
        if (!(quem instanceof net.minecraft.server.level.ServerPlayer gente)) return false;

        var casa = gente.getRespawnConfig();
        ServerLevel mundo = level;
        net.minecraft.core.BlockPos onde;
        if (casa != null) {
            mundo = level.getServer().getLevel(casa.respawnData().dimension());
            if (mundo == null) return false;
            onde = casa.respawnData().pos();
        } else {
            mundo = level.getServer().overworld();
            onde = mundo.getLevelData().getRespawnData().pos();
        }

        boolean emCasa = mundo.dimension() == level.dimension()
                && quem.distanceToSqr(onde.getX(), quem.getY(), onde.getZ()) <= EM_CASA * EM_CASA;
        if (emCasa) {
            var aldeia = level.findNearestMapStructure(net.minecraft.tags.StructureTags.VILLAGE,
                    quem.blockPosition(), PROCURA_ALDEIA, false);
            if (aldeia == null) return false;
            mundo = level;
            onde = aldeia;
        }

        onde = chão(mundo, onde);
        gente.teleportTo(mundo, onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5,
                java.util.Set.of(), gente.getYRot(), gente.getXRot(), false);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                quem.getX(), quem.getY() + 1.0, quem.getZ(), 48, 0.5, 1.0, 0.5, 0.2);
        return true;
    }

    /**
     * Sobe até caber: o laço do {@code teleportToLocationSafely}.
     *
     * <p>Um vampiro que se ponha dentro de pedra não é um vampiro: é um vampiro preso. O original sobe
     * enquanto o bloco for sólido, e para no céu.
     */
    private static net.minecraft.core.BlockPos chão(ServerLevel mundo, net.minecraft.core.BlockPos daqui) {
        var onde = daqui;
        while (onde.getY() < mundo.getMaxY() && !mundo.isEmptyBlock(onde)) onde = onde.above();
        return onde;
    }

    // ------------------------------------------------------------------ e os dois barulhos

    /** O chiado de quando dá. */
    private static void chiado(ServerLevel level, Player quem) {
        level.playSound(null, quem.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    /**
     * E o toque de caixa de quando não dá, que é tudo o que o mod explica.
     *
     * <p>Ele vai <b>só a quem tentou</b> — o {@code playOnlyTo} do original. Ninguém mais ouve um vampiro
     * falhar.
     */
    private static void gago(ServerLevel level, Player quem) {
        if (!(quem instanceof net.minecraft.server.level.ServerPlayer gente)) return;
        if (gente.connection == null) return;
        gente.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                SoundEvents.NOTE_BLOCK_SNARE, SoundSource.PLAYERS,
                gente.getX(), gente.getY(), gente.getZ(), 1.0f, 0.5f, level.getRandom().nextLong()));
    }
}
