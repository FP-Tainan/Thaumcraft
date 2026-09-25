package net.thaumcraft.naturalis;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCComponents;

/**
 * As teclas do Magia Naturalis 0.5.0: o {@code MNKeyBindings} com o {@code PacketKeyInput} e o
 * {@code PacketPickedBlock}.
 *
 * <p>Com o Foco de Construção na varinha, duas teclas mudam o tamanho da área, uma passa à forma seguinte (com o
 * Ctrl, ao jeito seguinte) e uma marca o bloco da mira como o bloco a construir.
 */
public final class NaturalisKeys {
    /** Os números que o original manda no {@code KeyInputMessage}. */
    public static final int DECREASE = 2;
    public static final int INCREASE = 3;
    public static final int MODE = 4;
    public static final int SHAPE = 5;

    /** A tecla apertada, do jeito que o original a manda. */
    public record Key(int id) implements CustomPacketPayload {
        public static final Type<Key> TYPE = new Type<>(Thaumcraft.id("naturalis_key"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Key> CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, Key::id, Key::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** O bloco que a mira marcou. */
    public record Picked(String block) implements CustomPacketPayload {
        public static final Type<Picked> TYPE = new Type<>(Thaumcraft.id("naturalis_picked"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Picked> CODEC =
                StreamCodec.composite(ByteBufCodecs.STRING_UTF8, Picked::block, Picked::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private NaturalisKeys() {
    }

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(Key.TYPE, Key.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(Picked.TYPE, Picked.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Key.TYPE, (payload, context) -> context.server().execute(() -> {
            ItemStack focus = builderFocus(context.player());
            if (focus == null) return;
            switch (payload.id()) {
                case DECREASE -> BuilderFocus.cycleSize(focus, -1);
                case INCREASE -> BuilderFocus.cycleSize(focus, 1);
                case MODE -> BuilderFocus.cycleMode(focus);
                case SHAPE -> BuilderFocus.cycleShape(focus);
                default -> Thaumcraft.LOGGER.warn("tecla do Magia Naturalis que não existe: {}", payload.id());
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(Picked.TYPE, (payload, context) -> context.server().execute(() -> {
            ItemStack focus = builderFocus(context.player());
            if (focus == null) return;
            focus.set(TCComponents.BUILDER_BLOCK, payload.block());
        }));
    }

    /** O Foco de Construção que está na varinha da mão, se estiver. */
    private static ItemStack builderFocus(Player player) {
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof WandItem)) return null;
        ItemStack focus = WandItem.focusStack(held);
        return focus.getItem() instanceof FocusItem item && "build".equals(item.type()) ? focus : null;
    }
}
