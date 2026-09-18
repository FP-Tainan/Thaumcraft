package net.thaumcraft.baubles;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.gamerules.GameRules;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.inventory.BaublesMenu;

/**
 * O lado do servidor do Baubles: o {@code PacketOpenBaublesInventory}, o {@code PacketOpenNormalInventory}, o tique
 * das peças vestidas e a queda delas na morte.
 */
public final class BaublesNetwork {
    /** Abrir o inventário expandido (true) ou voltar ao de sempre (false). */
    public record Open(boolean baubles) implements CustomPacketPayload {
        public static final Type<Open> TYPE = new Type<>(Thaumcraft.id("baubles_open"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Open> CODEC = StreamCodec.composite(
                net.minecraft.network.codec.ByteBufCodecs.BOOL, Open::baubles, Open::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private BaublesNetwork() {
    }

    public static void init() {
        Baubles.init();
        PayloadTypeRegistry.serverboundPlay().register(Open.TYPE, Open.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Open.TYPE, (payload, context) -> context.server().execute(() -> {
            ServerPlayer player = context.player();
            if (payload.baubles()) {
                player.openMenu(new SimpleMenuProvider((id, inventory, who) -> new BaublesMenu(id, inventory),
                        Component.translatable("button.thaumcraft.baubles")));
            } else if (player.containerMenu instanceof BaublesMenu) {
                // volta ao inventário de sempre sem mandar fechar a tela: quem joga já abriu a dele
                player.doCloseContainer();
            }
        }));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) Baubles.tick(player);
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer player
                    && !player.level().getGameRules().get(GameRules.KEEP_INVENTORY)) {
                Baubles.dropOnDeath(player);
            }
        });
    }
}
