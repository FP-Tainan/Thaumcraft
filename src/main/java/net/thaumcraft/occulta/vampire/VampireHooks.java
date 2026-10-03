package net.thaumcraft.occulta.vampire;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;

/**
 * Onde a vampirice toca o jogo: o <b>toque</b> que bebe e a <b>tecla</b> que escolhe o poder.
 *
 * <p>São duas costuras e nada mais. Tudo o resto que um vampiro é — a sede, o sol, o sangue que vira comida —
 * corre sozinho no {@linkplain VampireTick relógio dele}, e é essa a diferença entre o vampiro e o lobisomem:
 * o lobisomem <b>reage</b> ao mundo, o vampiro é <b>cobrado</b> por ele.
 */
public final class VampireHooks {
    private VampireHooks() {
    }

    /** O recado de quem apertou a tecla: o poder seguinte, ou um pedido para ligar a visão. */
    public record Escolha(boolean visão) implements CustomPacketPayload {
        public static final Type<Escolha> TYPE = new Type<>(Thaumcraft.id("vampire_power"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Escolha> CODEC =
                StreamCodec.composite(net.minecraft.network.codec.ByteBufCodecs.BOOL, Escolha::visão,
                        Escolha::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(Escolha.TYPE, Escolha.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Escolha.TYPE, (recado, quem) ->
                quem.server().execute(() -> {
                    Player gente = quem.player();
                    if (!Vampire.é(gente)) return;
                    if (recado.visão()) VampirePowers.viraAVisão(gente);
                    else VampirePowers.seguinte(gente);
                }));

        UseEntityCallback.EVENT.register(VampireHooks::toca);
        // e a galinha sacrificada sobre o rito, que enche o Cálice
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register(
                (quemMorreu, fonte) -> {
                    if (!(quemMorreu.level() instanceof ServerLevel mundo)) return;
                    if (!(fonte.getEntity() instanceof Player quem)) return;
                    GobletItem.aGalinha(mundo, quem, quemMorreu);
                });
    }

    /**
     * <b>Tocar num vivo com um poder escolhido</b>: o {@code onEntityInteract} do original.
     *
     * <p>Devolver {@code SUCCESS} tira o toque das mãos do jogo, que é o {@code setCanceled} dele: com um
     * poder na mão, um vampiro não comercia com aldeões — ele os morde.
     */
    private static InteractionResult toca(Player quem, net.minecraft.world.level.Level level,
                                          net.minecraft.world.InteractionHand mão,
                                          net.minecraft.world.entity.Entity emQuem,
                                          net.minecraft.world.phys.EntityHitResult onde) {
        if (!(level instanceof ServerLevel mundo)) return InteractionResult.PASS;
        if (!(emQuem instanceof LivingEntity vivo)) return InteractionResult.PASS;
        if (!Vampire.é(quem)) return InteractionResult.PASS;
        return VampirePowers.bebe(mundo, quem, vivo) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
}
