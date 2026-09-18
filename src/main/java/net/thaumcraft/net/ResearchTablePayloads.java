package net.thaumcraft.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.entity.ResearchTableBlockEntity;

/**
 * O que a tela da mesa de pesquisa pede ao servidor: o {@code PacketAspectPlaceToServer} e o
 * {@code PacketAspectCombinationToServer} da 4.2.3.5. Quem decide é sempre o servidor, com a mesa dele.
 */
public final class ResearchTablePayloads {
    /** Escrever um aspecto numa casa da nota, ou apagar a casa (aspecto vazio). */
    public record Place(BlockPos pos, int q, int r, String aspect) implements CustomPacketPayload {
        public static final Type<Place> TYPE = new Type<>(Thaumcraft.id("research_place"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Place> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, Place::pos,
                ByteBufCodecs.VAR_INT, Place::q,
                ByteBufCodecs.VAR_INT, Place::r,
                ByteBufCodecs.STRING_UTF8, Place::aspect,
                Place::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Juntar dois aspectos no terceiro que eles formam. */
    public record Combine(BlockPos pos, String first, String second) implements CustomPacketPayload {
        public static final Type<Combine> TYPE = new Type<>(Thaumcraft.id("research_combine"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Combine> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, Combine::pos,
                ByteBufCodecs.STRING_UTF8, Combine::first,
                ByteBufCodecs.STRING_UTF8, Combine::second,
                Combine::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private ResearchTablePayloads() {
    }

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(Place.TYPE, Place.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(Combine.TYPE, Combine.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Place.TYPE, (payload, context) -> context.server().execute(() -> {
            ResearchTableBlockEntity table = table(context.player(), payload.pos());
            if (table == null) return;
            Aspect aspect = payload.aspect().isEmpty() ? null : Aspect.of(payload.aspect());
            if (!payload.aspect().isEmpty() && aspect == null) return;
            table.placeAspect(payload.q(), payload.r(), aspect, context.player());
        }));
        ServerPlayNetworking.registerGlobalReceiver(Combine.TYPE, (payload, context) -> context.server().execute(() -> {
            ResearchTableBlockEntity table = table(context.player(), payload.pos());
            Aspect first = Aspect.of(payload.first()), second = Aspect.of(payload.second());
            if (table == null || first == null || second == null) return;
            table.combine(context.player(), first, second);
        }));
    }

    /** A mesa, se quem pede está perto dela e com a tela dela aberta. */
    private static ResearchTableBlockEntity table(ServerPlayer player, BlockPos pos) {
        if (!(player.containerMenu instanceof net.thaumcraft.inventory.ResearchTableMenu)) return null;
        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64.0) return null;
        return player.level().getBlockEntity(pos) instanceof ResearchTableBlockEntity table ? table : null;
    }
}
