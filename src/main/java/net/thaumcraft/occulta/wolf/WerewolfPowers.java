package net.thaumcraft.occulta.wolf;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.ChatFormatting;
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
 * <h2>E o salto</h2>
 *
 * <p>Em forma de bicho, <b>correr é saltar</b>: quem corre leva um empurrão para a frente do tamanho do
 * {@linkplain WerewolfStats#arranco() arranco} do grau dele, e é isso que faz do quinto degrau — dez monstros
 * mortos <b>no ar</b> — uma coisa possível.
 *
 * <p>E em forma de bicho a <b>arma na mão não vale nada</b>: o original força a pancada a <b>dois</b> se o
 * que ele tem na mão tem dano próprio, e só soma o dano do grau a <b>mãos vazias e a correr</b>. Um lobo com
 * uma espada de diamante bate menos do que um lobo sem nada — e é de propósito.
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
        level.playSound(null, quem.getX(), quem.getY(), quem.getZ(), SoundEvents.RAVAGER_ROAR,
                SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    // ------------------------------------------------------------------ o salto e a pancada

    /**
     * <b>Correr é saltar</b>: o {@code updatePlayerState} do original, na parte do arranco.
     *
     * <p>Quem corre em forma de bicho leva um empurrão para a frente — e é o que faz do quinto degrau, que
     * pede dez monstros mortos <b>no ar</b>, uma coisa que se consegue.
     */
    public static void salta(Player quem) {
        if (!quem.isSprinting()) return;
        WerewolfStats dá = WerewolfStats.de(quem);
        if (dá == null || dá.arranco() == 0.0) return;

        float rumo = quem.getYRot() * (float) (Math.PI / 180.0);
        quem.setDeltaMovement(quem.getDeltaMovement().add(
                -net.minecraft.util.Mth.sin(rumo) * dá.arranco(), 0.0,
                net.minecraft.util.Mth.cos(rumo) * dá.arranco()));
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
