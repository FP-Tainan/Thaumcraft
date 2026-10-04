package net.thaumcraft.occulta.wolf;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.NoDrops;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.Spawn;
import org.jetbrains.annotations.Nullable;

/**
 * O que um lobisomem <b>faz</b>: a parte do {@code Shapeshift} do Witchery que não é a forma nem a tabela.
 *
 * <h2>O uivo, e os três uivos que ele é</h2>
 *
 * <p>Uivar não é um botão nem um item: é um <b>gesto</b>. Olha-se <b>direito para cima</b>, agacha-se, e
 * aperta-se o botão de usar. É a coisa mais bonita do mod, e não está escrita em parte nenhuma do jogo — quem
 * a descobre, a descobre por ter atirado a cabeça para trás.
 *
 * <p>E o mesmo gesto faz <b>três coisas diferentes</b>, pela ordem em que o original as pergunta:
 *
 * <ol>
 *   <li>no <b>sexto grau</b>, de lobo e <b>de noite</b>, ele <b>conta</b>: cada pedaço de mundo novo em que
 *       ele uive é um dos dezesseis que o sexto degrau pede. Uivar duas vezes no mesmo lugar não conta, e ele
 *       é avisado disso em vermelho;</li>
 *   <li>do <b>oitavo</b> em diante, de lobo, ele <b>chama cães</b>: dois mais o que o grau der, já mansos,
 *       com a {@linkplain OccultaEffects#MORTAL_COIL Morte Certa} de dez segundos — eles vêm para morrer, e
 *       não deixam nem corpo nem experiência;</li>
 *   <li>do <b>sétimo</b> em diante, de lobisomem, ele <b>prende</b>: tudo o que não é lobisomem nem vampiro,
 *       a dezesseis blocos, fica <b>paralisado</b> por quatro segundos mais o que o grau der.</li>
 * </ol>
 *
 * <p>Os dois últimos esperam <b>um minuto</b> entre si, e quem está no modo criativo não espera nada. Não
 * tendo passado o minuto, o uivo sai gago — um toque de caixa em vez do uivo.
 *
 * <p>Repare na ordem: um <b>lobo de grau sete</b> não tem uivo nenhum. O primeiro ramo quer grau seis, o
 * segundo quer oito, e o terceiro quer lobisomem. É o original, e é o degrau em que ele está aprendendo.
 *
 * <h2>E o resto, que o grau destranca sozinho</h2>
 *
 * <p>Nenhum deles se escolhe, e é o que os une: não há tecla, não há item, não há menu. Todos vêm do
 * <b>grau</b> e da <b>forma</b>, e todos param sozinhos quando ele volta a ser gente.
 *
 * <ul>
 *   <li><b>O salto</b> — {@link #pula}: um bicho pula mais alto, e correndo o pulo também o atira para a
 *       frente;</li>
 *   <li><b>a queda que perdoa</b> — {@link #queda}: a distância encolhe pelo que o grau perdoa;</li>
 *   <li><b>o que lhe tiram</b> — {@link #apanha}: a resistência subtrai, o teto corta, e a prata soma;</li>
 *   <li><b>a armadura rasgada</b> — {@link #rasga}, do nono grau e só de lobisomem;</li>
 *   <li><b>a fome que a caça mata</b> — {@link #come}, do quarto;</li>
 *   <li><b>o osso que sai da terra</b> — {@link #osso} e {@link #cavaComAsPatas}, do terceiro e só de
 *       lobo;</li>
 *   <li>e <b>o contágio</b> — {@link #contagia}, do décimo, que é o único poder do mod cujo efeito é outro
 *       jogador.</li>
 * </ul>
 *
 * <p>E em forma de bicho a <b>arma na mão não vale nada</b> — {@link #pancada}: o original força a pancada a
 * <b>dois</b> se o que ele tem na mão tem dano próprio, e só soma o dano do grau a <b>mãos vazias e
 * correndo</b>. Um lobo com uma espada de diamante bate menos do que um lobo sem nada, e é de propósito.
 *
 * <p>Três deles — o salto, a queda e o osso — <b>não são chamados por ninguém deste porte</b>: quem passa por
 * eles é o jogo, por três remendos. É por isso que há uma prova que chama o jogo e não o porte.
 */
public final class WerewolfPowers {
    /** De quanto em quanto se pode uivar, nos dois uivos que esperam. */
    public static final int ENTRE_UIVOS = 60_000;

    /** O que o uivo de lobo chama, e por quanto tempo eles duram. */
    public static final int CÃES_BASE = 2;
    public static final int CÃES_AOS = 8;
    public static final int MORTE_CERTA = 200;
    public static final int CÃES_PERTO = 1;
    public static final int CÃES_LONGE = 6;

    /** O alcance do uivo que prende, o grau da paralisia e a conta do tempo dela. */
    public static final double PRENDE_A = 16.0;
    public static final int PARALISIA_GRAU = 3;
    public static final int PRENDE_AOS = 7;
    public static final int PRENDE_BASE = 4;

    /** O grau a partir do qual o sexto degrau conta uivos. */
    public static final int CONTA_AOS = 6;

    /** O que a pancada de um bicho com arma na mão vale, e nada mais. */
    public static final float ARMA_NA_MÃO_VALE = 2.0f;

    /** O chão onde a pancada para, mesmo com o teto do grau: o original nunca deixa passar de meio. */
    public static final float NUNCA_MENOS_DE = 0.5f;

    /** O que a prata soma a quem está em forma de bicho. */
    public static final float A_PRATA_SOMA = 5.0f;

    /** A armadura rasgada: do nono grau, de lobisomem, e um quarto da vida dela por golpe. */
    public static final int RASGA_AOS = 9;
    public static final float RASGA = 0.25f;
    public static final int RASGADA_NO_CHÃO = 100;

    /** A fome que a caça mata: do quarto grau, em forma de bicho. */
    public static final int COME_AOS = 4;
    public static final int COMIDA = 8;
    public static final float FARTURA = 0.8f;
    public static final int BARULHO_UMA_EM = 3;

    /** O osso que sai da terra: do terceiro grau, de lobo. */
    public static final int OSSO_AOS = 3;
    public static final int OSSO_UMA_EM = 20;
    public static final int OSSO_DOBRADO_UMA_EM = 5;
    public static final int ENTRE_OSSOS = 60_000;

    /** E o contágio, do décimo: o mesmo quarto de vida e a mesma uma em quatro do bicho. */
    public static final int CONTAGIA_AOS = 10;

    /**
     * Quando ele uivou pela última vez, pelo relógio do mundo de fora.
     *
     * <p>O original guarda o relógio da <b>máquina</b>, e não o do mundo, e por isso o minuto do uivo passa
     * mesmo com o jogo parado. É o que se faz aqui.
     */
    public static final AttachmentType<Long> ÚLTIMO_UIVO = AttachmentRegistry.<Long>builder()
            .initializer(() -> 0L)
            .persistent(com.mojang.serialization.Codec.LONG)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("werewolf_howl"));

    /**
     * E quando ele achou osso pela última vez, pelo mesmo relógio do uivo.
     *
     * <p>Sem ele, cavar terra de lobo seria uma fábrica de ossos: o original guarda a hora e só deixa achar
     * um por minuto.
     */
    public static final AttachmentType<Long> ÚLTIMO_OSSO = AttachmentRegistry.<Long>builder()
            .initializer(() -> 0L)
            .persistent(com.mojang.serialization.Codec.LONG)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("werewolf_bone"));

    private WerewolfPowers() {
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o apego. */
    public static void init() {
    }

    // ------------------------------------------------------------------ o uivo

    /**
     * Alguém atirou a cabeça para trás e uivou: o {@code checkForHowling} do original.
     *
     * <p>Os três ramos são perguntados pela ordem dele, e o primeiro que casar é o único que corre.
     */
    public static void uiva(ServerLevel level, Player quem) {
        int grau = Werewolf.grauDe(quem);
        Werewolf.Forma forma = Werewolf.formaDe(quem);
        if (grau <= 0 || !forma.éBicho()) return;

        if (grau == CONTA_AOS && forma == Werewolf.Forma.LOBO && !level.isBrightOutside()
                && WerewolfQuest.estadoDe(quem) == WerewolfQuest.Estado.COMEÇADO) {
            uivoQueConta(level, quem);
            return;
        }
        if (forma == Werewolf.Forma.LOBO && grau >= CÃES_AOS) {
            uivoQueChama(level, quem, grau);
            return;
        }
        if (forma == Werewolf.Forma.LOBISOMEM && grau >= PRENDE_AOS) {
            uivoQuePrende(level, quem, grau);
        }
    }

    /** O uivo do sexto degrau: conta o lugar, se ele for novo. */
    private static void uivoQueConta(ServerLevel level, Player quem) {
        som(level, quem);
        int pedaçoX = net.minecraft.util.Mth.floor(quem.getX()) >> 4;
        int pedaçoZ = net.minecraft.util.Mth.floor(quem.getZ()) >> 4;
        if (WerewolfQuest.guardaLugar(quem, pedaçoX, pedaçoZ)) {
            WerewolfQuest.conta(quem);
            return;
        }
        quem.sendSystemMessage(Component.translatable("tc.werewolf.chunkvisited")
                .withStyle(ChatFormatting.RED));
    }

    /**
     * O uivo que chama cães, do oitavo grau em diante.
     *
     * <p><b>Dois mais o que o grau der</b> — {@code 2 + sorte(grau - 7)}, que no oitavo é sempre dois e no
     * décimo chega a quatro. Eles nascem mansos, dele, e com a <b>Morte Certa</b> de dez segundos: vêm para
     * morrer e não deixam nem corpo nem experiência.
     */
    private static void uivoQueChama(ServerLevel level, Player quem, int grau) {
        if (!podeUivar(quem)) return;
        som(level, quem);
        marcaOUivo(quem);

        int quantos = CÃES_BASE + level.getRandom().nextInt(grau - (CÃES_AOS - 1));
        for (int n = 0; n < quantos; n++) {
            var bicho = Spawn.perto(level, net.minecraft.world.entity.EntityTypes.WOLF,
                    quem.blockPosition(), CÃES_PERTO, CÃES_LONGE);
            if (!(bicho instanceof Wolf cão)) continue;

            cão.addEffect(new MobEffectInstance(OccultaEffects.MORTAL_COIL, MORTE_CERTA));
            cão.setTame(true, true);
            cão.setOwner(quem);
            ((net.thaumcraft.mixin.MobXpAccessor) cão).thaumcraft$experiência(0);
            NoDrops.marca(cão);

            var meio = Spawn.meio(cão);
            level.sendParticles(ParticleTypes.SMOKE, meio.x, meio.y, meio.z, 10, 1.0, 1.0, 1.0, 0.0);
        }
    }

    /** E o uivo que prende, do sétimo em diante e só de lobisomem. */
    private static void uivoQuePrende(ServerLevel level, Player quem, int grau) {
        if (!podeUivar(quem)) return;
        som(level, quem);
        marcaOUivo(quem);

        int tempo = (PRENDE_BASE + level.getRandom().nextInt(grau - (PRENDE_AOS - 1))) * 20;
        AABB roda = quem.getBoundingBox().inflate(PRENDE_A, PRENDE_A, PRENDE_A);
        for (LivingEntity quemApanha : level.getEntitiesOfClass(LivingEntity.class, roda)) {
            if (Lycanthropy.éMesmoDeGente(quemApanha)) continue;
            if (net.thaumcraft.occulta.vampire.Vampirism.é(quemApanha)) continue;
            quemApanha.addEffect(new MobEffectInstance(OccultaEffects.PARALYSIS, tempo, PARALISIA_GRAU));
        }
    }

    /** Se o minuto passou — ou se ele está no criativo, que não espera nada. */
    private static boolean podeUivar(Player quem) {
        if (quem.getAbilities().instabuild) return true;
        long agora = System.currentTimeMillis();
        if (quem.getAttachedOrCreate(ÚLTIMO_UIVO) + ENTRE_UIVOS < agora) return true;

        quem.level().playSound(null, quem.getX(), quem.getY(), quem.getZ(),
                SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
        return false;
    }

    private static void marcaOUivo(Player quem) {
        quem.setAttached(ÚLTIMO_UIVO, System.currentTimeMillis());
    }

    /** O uivo, que se ouve de longe. */
    private static void som(ServerLevel level, Player quem) {
        level.playSound(null, quem.getX(), quem.getY(), quem.getZ(),
                net.thaumcraft.occulta.OccultaSounds.WOLFMAN_HOWL.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    // ------------------------------------------------------------------ o salto e a pancada

    /**
     * <b>O salto</b>: o {@code updateJump} do original, que corre quando ele <b>pula</b>.
     *
     * <p>Um bicho pula <b>mais alto</b> — é o {@code salto} do grau, somado de uma vez ao impulso do pulo —
     * e, <b>correndo</b>, o pulo também o atira <b>para a frente</b>, na direção em que ele olha, do tamanho
     * do {@code arranco}.
     *
     * <p>É isso que faz do quinto degrau, que pede dez monstros mortos <b>no ar</b>, uma coisa que se
     * consegue: um lobo de grau alto que corra e pule atravessa quatro ou cinco blocos num salto só, e cai
     * em cima do que estiver no caminho.
     *
     * <p>Repare que as duas coisas são <b>uma só batida</b>, e não um empurrão contínuo: o original soma e
     * larga. Um empurrão a cada batida faria um lobo que nunca mais para.
     */
    public static void pula(Player quem) {
        WerewolfStats dá = WerewolfStats.de(quem);
        if (dá == null) return;

        var vai = quem.getDeltaMovement().add(0.0, dá.salto(), 0.0);
        if (quem.isSprinting() && dá.arranco() != 0.0) {
            float rumo = quem.getYRot() * (float) (Math.PI / 180.0);
            vai = vai.add(-net.minecraft.util.Mth.sin(rumo) * dá.arranco(), 0.0,
                    net.minecraft.util.Mth.cos(rumo) * dá.arranco());
        }
        quem.setDeltaMovement(vai);
    }

    /**
     * <b>E a queda perdoa</b>: o {@code updateFallState} do original.
     *
     * <p>A distância da queda <b>encolhe</b> pelo tanto que o grau perdoa, e o que sobrar é que dói. Um lobo
     * de grau dez perdoa cinco blocos; um lobisomem de grau dez, sete.
     *
     * <p>O original tem ainda um caso em que a queda <b>não dói de todo</b> — o {@code fall == -1} —, e esse
     * não é de lobisomem nenhum: é da forma de <b>morcego</b> do vampiro, que este porte ainda não tem.
     *
     * @return a distância que o jogo deve contar, já perdoada
     */
    public static double queda(Player quem, double quanto) {
        WerewolfStats dá = WerewolfStats.de(quem);
        if (dá == null) return quanto;
        return Math.max(0.0, quanto - dá.queda());
    }

    /**
     * O que a pancada dele vale: o {@code updateChargeDamage} do original.
     *
     * <p>Com <b>arma na mão</b> ela vale <b>dois</b>, e nada mais — um lobo com uma espada de diamante bate
     * menos do que um lobo sem nada. A <b>mãos vazias e a correr</b>, soma o dano do grau.
     *
     * @return o dano que a pancada passa a valer, ou o mesmo se ela não é de bicho
     */
    public static float pancada(Player quem, float quanto) {
        if (!Werewolf.emBicho(quem)) return quanto;
        if (temArma(quem.getMainHandItem())) return ARMA_NA_MÃO_VALE;

        WerewolfStats dá = WerewolfStats.de(quem);
        if (dá == null || !quem.isSprinting()) return quanto;
        return quanto + dá.dano();
    }

    /**
     * <b>O que a pancada lhe tira</b>: o {@code getResistance} e o {@code getDamageCap} do original.
     *
     * <p>São dois números e eles trabalham um em cima do outro:
     *
     * <ol>
     *   <li>a <b>resistência</b> <i>subtrai</i> — tira do golpe o que o grau aguenta, e <b>não vale para
     *       fogo</b>;</li>
     *   <li>e o <b>teto</b> <i>corta</i> — nenhuma pancada passa dele. É o único número da tabela que
     *       <b>melhora baixando</b>: quatro no princípio, <b>dois</b> do quinto grau em diante.</li>
     * </ol>
     *
     * <p>Duas pancadas escapam ao teto, e são as duas que fazem sentido: a de <b>outro lobisomem</b>, que
     * bate tão duro quanto ele, e a de <b>prata</b> — que, em vez de ser cortada, <b>soma cinco</b>. É isso
     * que faz da prata a única coisa que mata um lobisomem de grau alto em tempo útil.
     *
     * <p>E quatro danos ficam de fora de tudo: <b>o vazio, a parede, o afogamento e a queda</b>. Um lobisomem
     * de grau dez que caia de cem blocos morre como qualquer um — o que a queda lhe perdoa está na
     * {@link #queda}, e é tudo o que ela lhe perdoa.
     *
     * @return o que a pancada passa a valer
     */
    public static float apanha(Player quem, net.minecraft.world.damagesource.DamageSource fonte,
                               float quanto) {
        if (!Werewolf.emBicho(quem)) return quanto;
        if (foraDaConta(fonte)) return quanto;
        WerewolfStats dá = WerewolfStats.de(quem);
        if (dá == null) return quanto;

        if (!fonte.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            quanto = Math.max(0.0f, quanto - dá.resistência());
        }
        if (Lycanthropy.é(quemBateu(fonte))) return quanto;

        if (Silver.éDePrata(fonte)) return quanto + A_PRATA_SOMA;
        return Math.max(Math.min(quanto, dá.tetoDaPancada()), NUNCA_MENOS_DE);
    }

    /** Os quatro danos que não passam por nada disto. */
    private static boolean foraDaConta(net.minecraft.world.damagesource.DamageSource fonte) {
        return fonte.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD)
                || fonte.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)
                || fonte.is(net.minecraft.world.damagesource.DamageTypes.DROWN)
                || fonte.is(net.minecraft.world.damagesource.DamageTypes.FALL);
    }

    @Nullable
    private static LivingEntity quemBateu(net.minecraft.world.damagesource.DamageSource fonte) {
        return fonte.getDirectEntity() instanceof LivingEntity vivo ? vivo : null;
    }

    /**
     * <b>A armadura rasgada</b>: o {@code rendArmor} do original, do <b>nono grau</b> e só de lobisomem.
     *
     * <p>Cada golpe escolhe <b>uma peça de armadura ao acaso</b> e lhe tira <b>um quarto da vida dela</b>. O
     * que não se gasta — o que não tem durabilidade — é <b>arrancado logo</b>; e o que se gastar até ao fim
     * cai no chão, com <b>cinco segundos</b> antes de se poder apanhar outra vez.
     *
     * <p>Arrancar só vale contra <b>gente</b>, como no original: é um poder feito para o combate entre
     * jogadores, e é o que faz de um lobisomem de grau nove uma coisa contra a qual não adianta vestir ferro.
     */
    public static void rasga(ServerLevel level, Player quem, LivingEntity emQuem) {
        if (Werewolf.formaDe(quem) != Werewolf.Forma.LOBISOMEM) return;
        if (Werewolf.grauDe(quem) < RASGA_AOS) return;

        var casas = java.util.List.of(net.minecraft.world.entity.EquipmentSlot.FEET,
                net.minecraft.world.entity.EquipmentSlot.LEGS,
                net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.HEAD);
        var casa = casas.get(level.getRandom().nextInt(casas.size()));
        var peça = emQuem.getItemBySlot(casa);
        if (peça.isEmpty()) return;

        boolean arranca = !peça.isDamageableItem();
        if (!arranca) {
            int tinha = peça.getDamageValue();
            /*
             * O original gasta a peça em nome de QUEM BATEU — é o jeito da 1.7.10 de gastar uma coisa, que
             * pedia um jogador e não perguntava de quem ela era. Aqui ela se gasta em nome de QUEM A VESTE,
             * que é o jeito de hoje e é o certo: quebrando, é no corpo dele que ela quebra.
             */
            peça.hurtAndBreak((int) Math.ceil(peça.getMaxDamage() * RASGA), emQuem, casa);
            arranca = peça.isEmpty() || peça.getDamageValue() <= tinha;
        }
        if (!arranca || !(emQuem instanceof Player vítima)) return;

        emQuem.setItemSlot(casa, net.minecraft.world.item.ItemStack.EMPTY);
        var caiu = vítima.drop(peça, true, false);
        if (caiu != null) caiu.setPickUpDelay(RASGADA_NO_CHÃO);
    }

    /**
     * <b>A fome que a caça mata</b>: o {@code processCreatureKilled} do original, do <b>quarto grau</b>.
     *
     * <p>Em forma de bicho, cada coisa <b>viva</b> que ele mata o <b>alimenta</b> — oito de comida e quase
     * uma barra inteira de fartura, de uma vez. É mais do que qualquer comida do jogo dá, e é a razão de um
     * lobisomem nunca precisar de cozinhar.
     *
     * <p><b>Morto-vivo não alimenta</b>, e é a única regra: carne podre não sustenta ninguém.
     */
    public static void come(ServerLevel level, Player quem, LivingEntity oquê) {
        if (!Werewolf.emBicho(quem) || Werewolf.grauDe(quem) < COME_AOS) return;
        if (oquê.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) return;

        quem.getFoodData().eat(COMIDA, FARTURA);
        level.sendParticles(net.minecraft.core.particles.DustParticleOptions.REDSTONE,
                oquê.getX(), oquê.getY() + 1.0, oquê.getZ(), 16, 1.0, 2.0, 1.0, 0.0);
        if (level.getRandom().nextInt(BARULHO_UMA_EM) != 0) return;
        level.playSound(null, oquê.blockPosition(), net.thaumcraft.occulta.OccultaSounds.WOLFMAN_EAT.value(),
                SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    /**
     * <b>O osso que sai da terra</b>: o {@code processDigging} do original, do <b>terceiro grau</b> e só de
     * lobo.
     *
     * <p>Cavando <b>terra</b> com as patas, uma vez em vinte sai um <b>osso</b> — dois, se a sorte for de uma
     * em cinco — e depois disso nada mais sai por <b>um minuto</b>. É a menor coisa que um lobisomem
     * faz e a que mais o faz parecer um cão.
     *
     * <p>Só conta a queda que é <b>só terra</b>: um bloco que dê outra coisa, ou mais do que uma, não é um
     * buraco cavado com o focinho.
     *
     * @return o que passa a cair, ou {@code null} se nada muda
     */
    @Nullable
    public static java.util.List<net.minecraft.world.item.ItemStack> osso(
            ServerLevel level, @Nullable net.minecraft.world.entity.Entity quem,
            java.util.List<net.minecraft.world.item.ItemStack> caiu) {
        if (!(quem instanceof Player gente)) return null;
        if (Werewolf.formaDe(gente) != Werewolf.Forma.LOBO) return null;
        if (Werewolf.grauDe(gente) < OSSO_AOS) return null;
        if (caiu.size() != 1 || !caiu.getFirst().is(net.minecraft.world.item.Items.DIRT)) return null;

        long agora = System.currentTimeMillis();
        if (gente.getAttachedOrCreate(ÚLTIMO_OSSO) + ENTRE_OSSOS >= agora) return null;
        if (level.getRandom().nextInt(OSSO_UMA_EM) != 0) return null;

        gente.setAttached(ÚLTIMO_OSSO, agora);
        var mais = new java.util.ArrayList<>(caiu);
        mais.add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BONE,
                level.getRandom().nextInt(OSSO_DOBRADO_UMA_EM) == 0 ? 2 : 1));
        return mais;
    }

    /** O que um lobo cava com as patas: relva, areia, terra, micélio e gravilha. */
    public static boolean cavável(net.minecraft.world.level.block.state.BlockState oquê) {
        return oquê.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)
                || oquê.is(net.minecraft.world.level.block.Blocks.SAND)
                || oquê.is(net.minecraft.world.level.block.Blocks.DIRT)
                || oquê.is(net.minecraft.world.level.block.Blocks.MYCELIUM)
                || oquê.is(net.minecraft.world.level.block.Blocks.GRAVEL);
    }

    /**
     * <b>Cavar com as patas</b>: o ramo do {@code onPlayerInteract} do original que é do lobo.
     *
     * <p>Do <b>terceiro grau</b>, de lobo e <b>agachado</b>, bater numa dessas cinco coisas a <b>tira de uma
     * vez</b> — sem ferramenta, sem demora e sem nada na mão, porque um lobo não tem mãos.
     *
     * <p>É o par do {@link #osso}: um dá o buraco, o outro dá o que estava dentro.
     *
     * @return {@code true} se ele cavou, e então o jogo não tem mais nada a fazer com essa batida
     */
    public static boolean cavaComAsPatas(ServerLevel level, Player quem, BlockPos onde) {
        if (quem.getAbilities().instabuild) return false;
        if (Werewolf.formaDe(quem) != Werewolf.Forma.LOBO) return false;
        if (Werewolf.grauDe(quem) < OSSO_AOS || !quem.isShiftKeyDown()) return false;
        if (!cavável(level.getBlockState(onde))) return false;
        return level.destroyBlock(onde, true, quem);
    }

    /**
     * <b>O contágio</b>: o {@code processWolfInfection} do original, do <b>décimo grau</b>.
     *
     * <p>É o que a escada dá no fim, e é o que faz dela uma coisa que <b>se espalha</b>: em forma de bicho,
     * quem ele derrubar abaixo de <b>um quarto da vida</b> apanha a licantropia, uma vez em quatro. Um
     * <b>aldeão</b> vira lobisomem ali mesmo; uma <b>pessoa</b> fica no grau um, com uma vida inteira de
     * luas pela frente.
     *
     * <p>As contas são as mesmas da mordida do {@link WolfmanEntity}, e as guardas também: o <b>conjunto
     * prateado</b> protege, e quem já é lobisomem não volta ao princípio.
     */
    public static void contagia(ServerLevel level, Player quem, LivingEntity emQuem) {
        if (!Werewolf.emBicho(quem) || Werewolf.grauDe(quem) < CONTAGIA_AOS) return;
        if (emQuem.getHealth() <= 0.0f) return;
        if (emQuem.getHealth() >= emQuem.getMaxHealth() * WolfmanEntity.QUASE_MORTO) return;
        if (level.getRandom().nextInt(WolfmanEntity.PEGA_UMA_EM) != 0) return;

        if (emQuem instanceof net.minecraft.world.entity.npc.villager.Villager aldeão) {
            WolfmanEntity.doAldeão(level, aldeão, false);
            return;
        }
        if (!(emQuem instanceof Player vítima)) return;
        if (net.thaumcraft.occulta.hunter.HunterClothes.protegeDeLobo(vítima)) return;
        if (Werewolf.grauDe(vítima) > 0) return;

        Werewolf.grau(vítima, 1);
        vítima.sendSystemMessage(net.minecraft.network.chat.Component
                .translatable("message.thaumcraft.werewolf_infection")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }

    /** Se o que ele tem na mão tem dano próprio. */
    private static boolean temArma(net.minecraft.world.item.ItemStack oquê) {
        if (oquê.isEmpty()) return false;
        var modificadores = oquê.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS);
        if (modificadores == null) return false;
        for (var entrada : modificadores.modifiers()) {
            if (entrada.attribute().equals(
                    net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)) {
                return true;
            }
        }
        return false;
    }
}
