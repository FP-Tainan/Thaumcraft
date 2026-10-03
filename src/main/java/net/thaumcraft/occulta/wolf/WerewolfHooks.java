package net.thaumcraft.occulta.wolf;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;

/**
 * Quem <b>conta os feitos</b> da {@linkplain WerewolfLadder escada dos dez graus}, e o gesto do uivo.
 *
 * <p>A estátua pede, e é aqui que o mundo responde. São cinco ganchos, e cada um deles só conta na
 * <b>forma certa</b> — o que faz de cada degrau um jeito diferente de jogar e não mais do mesmo:
 *
 * <ul>
 *   <li><b>quinto</b>: um monstro morto <b>de lobo e no ar</b>. Matar de pé não conta, e é por isso que o
 *       degrau se chama o do salto;</li>
 *   <li><b>sétimo</b>: um lobo bravo amansado <b>com o focinho</b> — chega-se perto dele de lobo e se toca nele. Uma
 *       vez em três ele cede; das outras, uma vez em dez ele <b>morde</b>;</li>
 *   <li><b>oitavo</b>: um porco-zumbi morto <b>de lobisomem</b>;</li>
 *   <li><b>nono</b>: uma pessoa morta <b>de lobo</b> — um aldeão ou um jogador. Uma só;</li>
 *   <li>e o <b>quarto</b>, que não se conta: o <b>Caçador Cornudo</b> morto.</li>
 * </ul>
 *
 * <p>Todos os cinco pedem que o golpe seja <b>da mão dele</b>: flecha, fogo e queda não contam, e é o
 * {@code getSourceOfDamage} do original a dizê-lo.
 *
 * <h2>E o gesto</h2>
 *
 * <p>O uivo não tem tecla nem item: o cliente olha se a cabeça está <b>direito para cima</b> e se ele está
 * <b>agachado</b>, e manda um recado ao servidor. É o {@code PacketHowl} do original, e é o único pedaço de
 * rede que a licantropia precisa.
 */
public final class WerewolfHooks {
    private WerewolfHooks() {
    }

    /** O recado do uivo, que não leva nada dentro: o gesto é a mensagem toda. */
    public record Uivo() implements CustomPacketPayload {
        public static final Type<Uivo> TYPE = new Type<>(Thaumcraft.id("werewolf_howl"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Uivo> CODEC =
                StreamCodec.unit(new Uivo());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(Uivo.TYPE, Uivo.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Uivo.TYPE, (recado, quem) -> quem.server().execute(() -> {
            Player gente = quem.player();
            if (gente.level() instanceof ServerLevel level) WerewolfPowers.uiva(level, gente);
        }));

        ServerLivingEntityEvents.AFTER_DEATH.register(WerewolfHooks::morreu);
        ServerLivingEntityEvents.AFTER_DAMAGE.register(
                (quem, fonte, dano, levou, aparado) -> oLoboDaLança(quem, fonte));
        UseEntityCallback.EVENT.register(WerewolfHooks::amansa);
    }

    // ------------------------------------------------------------------ as mortes que contam

    private static void morreu(LivingEntity quemMorreu, DamageSource fonte) {
        if (!(quemMorreu.level() instanceof ServerLevel)) return;

        // o Caçador conta para quem o mandou matar, mesmo de longe
        if (quemMorreu instanceof HornedHuntsmanEntity && fonte.getEntity() instanceof Player mandou) {
            WerewolfLadder.caçadorMorto(mandou);
        }

        if (!(fonte.getDirectEntity() instanceof Player quem)) return;
        int grau = Werewolf.grauDe(quem);
        Werewolf.Forma forma = Werewolf.formaDe(quem);

        if (grau == 5 && forma.éBicho()) {
            if (quemMorreu instanceof Enemy && !quem.onGround()) WerewolfLadder.conta(quem, 5);
        } else if (grau == 8 && forma == Werewolf.Forma.LOBISOMEM) {
            if (quemMorreu instanceof ZombifiedPiglin) WerewolfLadder.conta(quem, 8);
        } else if (grau == 9 && forma.éBicho()) {
            if (quemMorreu instanceof Villager || quemMorreu instanceof Player) {
                WerewolfLadder.conta(quem, 9);
            }
        }
    }

    // ------------------------------------------------------------------ e os lobos que se amansam

    /**
     * O sétimo degrau: <b>seis lobos amansados com o focinho</b>.
     *
     * <p>Um lobo bravo não se amansa com osso: <b>se</b> amansa sendo lobo e tocando nele. Uma vez em três ele
     * cede — e cede inteiro, com a vida cheia e sentado —; das outras duas ele resmunga, e uma vez em dez
     * dessas ele <b>se vira contra</b> quem tentou.
     *
     * <p>É o degrau mais perigoso da escada por uma razão simples: um lobo bravo que morde um lobo de grau
     * sete chama a matilha dele.
     */
    private static InteractionResult amansa(Player quem, net.minecraft.world.level.Level level,
                                            net.minecraft.world.InteractionHand mão, Entity emQuem,
                                            net.minecraft.world.phys.EntityHitResult onde) {
        if (level.isClientSide() || !(level instanceof ServerLevel mundo)) return InteractionResult.PASS;
        if (!(emQuem instanceof Wolf lobo)) return InteractionResult.PASS;
        if (Werewolf.grauDe(quem) != 7) return InteractionResult.PASS;
        if (Werewolf.formaDe(quem) != Werewolf.Forma.LOBO) return InteractionResult.PASS;
        if (WerewolfQuest.estadoDe(quem) != WerewolfQuest.Estado.COMEÇADO) return InteractionResult.PASS;
        if (lobo.isTame() || lobo.isAngry()) return InteractionResult.PASS;

        if (mundo.getRandom().nextInt(3) == 0) {
            lobo.setTame(true, true);
            lobo.setOwner(quem);
            lobo.getNavigation().stop();
            lobo.setTarget(null);
            lobo.setOrderedToSit(true);
            lobo.setHealth(20.0f);
            mundo.broadcastEntityEvent(lobo, (byte) 7);
            WerewolfLadder.conta(quem, 7);
            return InteractionResult.SUCCESS;
        }

        mundo.broadcastEntityEvent(lobo, (byte) 6);
        if (mundo.getRandom().nextInt(10) == 0) {
            lobo.setPersistentAngerTarget(net.minecraft.world.entity.EntityReference.of(quem));
            lobo.startPersistentAngerTimer();
            lobo.setTarget(quem);
        }
        return InteractionResult.SUCCESS;
    }

    // ------------------------------------------------------------------ e o lobo da lança

    /** Uma vez em quatro, e o lobo morre do que lhe fizeram: quinze minutos de Definhamento II. */
    public static final float LOBO_DA_LANÇA = 0.25f;
    public static final int LOBO_DEFINHA = 12_000;

    /**
     * <b>O lobo que a lança chama</b>: o pedaço do {@code onLivingDeath} do original que é da Lança do
     * Caçador.
     *
     * <p>Quem <b>apara com a lança</b> e apanha de alguém vivo chama, uma vez em quatro, um <b>lobo bravo</b>
     * que se vira contra quem bateu. E o lobo vem <b>a morrer</b>: Definhamento II por quinze minutos. Ele
     * não é um servo, é um <b>troco</b>.
     *
     * <p>É a razão de a lança aparar — e é por isso que ela, que é a arma de quem não sai do lugar, premia
     * exatamente quem fica.
     */
    private static void oLoboDaLança(LivingEntity quem, DamageSource fonte) {
        if (!(quem.level() instanceof ServerLevel level)) return;
        if (!(quem instanceof Player gente) || !gente.isBlocking()) return;
        if (!gente.getMainHandItem().is(net.thaumcraft.occulta.OccultaItems.HUNTSMANS_SPEAR)) return;
        if (!(fonte.getDirectEntity() instanceof LivingEntity quemBateu) || !quemBateu.isAlive()) return;
        if (level.getRandom().nextFloat() >= LOBO_DA_LANÇA) return;

        Wolf cão = net.minecraft.world.entity.EntityTypes.WOLF.create(level,
                net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
        if (cão == null) return;
        cão.snapTo(gente.getX(), gente.getY(), gente.getZ(), gente.getYRot(), gente.getXRot());
        cão.setTarget(quemBateu);
        cão.setPersistentAngerTarget(net.minecraft.world.entity.EntityReference.of(quemBateu));
        cão.startPersistentAngerTimer();
        cão.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WITHER, LOBO_DEFINHA, 1));
        level.addFreshEntity(cão);
    }

    // ------------------------------------------------------------------ e a pancada de um bicho

    /**
     * O que a pancada de um bicho vale: o {@code checkForChargeDamage} do original, que corre no mesmo
     * gancho das poções do ofício.
     *
     * <p>Só vale para o <b>golpe da mão</b> de um jogador, como no original.
     */
    public static float pancada(DamageSource fonte, float dano) {
        if (!fonte.is(DamageTypes.PLAYER_ATTACK)) return dano;
        if (!(fonte.getEntity() instanceof Player quem)) return dano;
        return WerewolfPowers.pancada(quem, dano);
    }
}
