package net.thaumcraft.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.thaumcraft.Thaumcraft;

import java.util.List;

/** As conversas entre o servidor e o cliente. */
public final class TCNetwork {
    /** Do servidor para quem entra: a tabela de aspectos das coisas, que o servidor monta com as receitas. */
    public record ObjectAspectsSync(java.util.Map<net.minecraft.world.item.Item, net.thaumcraft.api.aspects.AspectList> table)
            implements CustomPacketPayload {
        public static final Type<ObjectAspectsSync> TYPE = new Type<>(Thaumcraft.id("object_aspects"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ObjectAspectsSync> CODEC = StreamCodec.composite(
                ByteBufCodecs.map(java.util.HashMap::new,
                        ByteBufCodecs.registry(net.minecraft.core.registries.Registries.ITEM),
                        net.thaumcraft.api.aspects.AspectList.STREAM_CODEC),
                ObjectAspectsSync::table,
                ObjectAspectsSync::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Manda a tabela de aspectos a um jogador. */
    public static void syncAspects(ServerPlayer player) {
        ServerPlayNetworking.send(player, new ObjectAspectsSync(net.thaumcraft.api.aspects.ObjectAspects.snapshot()));
    }

    /** Do servidor para quem está perto: estrelinhas em volta de um bloco, o {@code PacketFXBlockSparkle}. */
    public record BlockSparkle(net.minecraft.core.BlockPos pos, int colour) implements CustomPacketPayload {
        public static final Type<BlockSparkle> TYPE = new Type<>(Thaumcraft.id("block_sparkle"));
        public static final StreamCodec<RegistryFriendlyByteBuf, BlockSparkle> CODEC = StreamCodec.composite(
                net.minecraft.core.BlockPos.STREAM_CODEC, BlockSparkle::pos,
                ByteBufCodecs.INT, BlockSparkle::colour,
                BlockSparkle::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Manda o brilho do bloco a quem estiver a até trinta e dois blocos, como o original. */
    public static void blockSparkle(net.minecraft.server.level.ServerLevel level, net.minecraft.core.BlockPos pos, int colour) {
        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().closerThan(pos, 32.0)) ServerPlayNetworking.send(player, new BlockSparkle(pos, colour));
        }
    }

    /** Do servidor para quem está perto: o {@code PacketBoreDig} (98, o bloco da vez) e o som do bloco que saiu (99). */
    public record BoreDig(net.minecraft.core.BlockPos pos, int id, int param) implements CustomPacketPayload {
        public static final Type<BoreDig> TYPE = new Type<>(Thaumcraft.id("bore_dig"));
        public static final StreamCodec<RegistryFriendlyByteBuf, BoreDig> CODEC = StreamCodec.composite(
                net.minecraft.core.BlockPos.STREAM_CODEC, BoreDig::pos,
                ByteBufCodecs.VAR_INT, BoreDig::id,
                ByteBufCodecs.INT, BoreDig::param,
                BoreDig::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** A quem estiver a até sessenta e quatro blocos da broca, como o original. */
    public static void boreDig(net.minecraft.server.level.ServerLevel level, net.minecraft.core.BlockPos pos, int id, int param) {
        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().closerThan(pos, 64.0)) ServerPlayNetworking.send(player, new BoreDig(pos, id, param));
        }
    }

    /** Do servidor para quem está perto: um raio de um nó a outro, o {@code PacketFXBlockZap}. */
    public record BlockZap(net.minecraft.world.phys.Vec3 from, net.minecraft.world.phys.Vec3 to) implements CustomPacketPayload {
        public static final Type<BlockZap> TYPE = new Type<>(Thaumcraft.id("block_zap"));
        public static final StreamCodec<RegistryFriendlyByteBuf, BlockZap> CODEC = StreamCodec.composite(
                net.minecraft.world.phys.Vec3.STREAM_CODEC, BlockZap::from,
                net.minecraft.world.phys.Vec3.STREAM_CODEC, BlockZap::to,
                BlockZap::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Manda o raio a quem estiver a até trinta e dois blocos do nó. */
    public static void blockZap(net.minecraft.server.level.ServerLevel level, net.minecraft.core.BlockPos near,
                                net.minecraft.world.phys.Vec3 from, net.minecraft.world.phys.Vec3 to) {
        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().closerThan(near, 32.0)) ServerPlayNetworking.send(player, new BlockZap(from, to));
        }
    }

    /** Do servidor para quem está perto: o raio de uma criatura em outra, o {@code PacketFXWispZap}. */
    public record EntityZap(int source, int target) implements CustomPacketPayload {
        public static final Type<EntityZap> TYPE = new Type<>(Thaumcraft.id("entity_zap"));
        public static final StreamCodec<RegistryFriendlyByteBuf, EntityZap> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, EntityZap::source, ByteBufCodecs.VAR_INT, EntityZap::target, EntityZap::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void entityZap(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.Entity source, net.minecraft.world.entity.Entity target) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(source) < 32.0 * 32.0) ServerPlayNetworking.send(player, new EntityZap(source.getId(), target.getId()));
        }
    }

    /**
     * O {@code PacketFXEssentiaSource}: a essência que sai de {@code source} pelo ar até {@code pos} (quem bebe). O
     * cliente guarda o fio por quinze tiques e renova a cada unidade.
     */
    public record EssentiaSource(net.minecraft.core.BlockPos pos, net.minecraft.core.BlockPos source, int colour) implements CustomPacketPayload {
        public static final Type<EssentiaSource> TYPE = new Type<>(Thaumcraft.id("essentia_source"));
        public static final StreamCodec<RegistryFriendlyByteBuf, EssentiaSource> CODEC = StreamCodec.composite(
                net.minecraft.core.BlockPos.STREAM_CODEC, EssentiaSource::pos,
                net.minecraft.core.BlockPos.STREAM_CODEC, EssentiaSource::source,
                ByteBufCodecs.INT, EssentiaSource::colour,
                EssentiaSource::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void essentiaSource(net.minecraft.server.level.ServerLevel level, net.minecraft.core.BlockPos pos,
                                      net.minecraft.core.BlockPos source, int colour) {
        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().closerThan(pos, 32.0)) ServerPlayNetworking.send(player, new EssentiaSource(pos, source, colour));
        }
    }

    /**
     * O {@code PacketFXInfusionSource}: de onde a matriz está puxando — um pedestal ({@code dx, dy, dz} dela até ele) ou,
     * com os três zerados, a experiência de uma criatura ({@code entity}).
     */
    public record InfusionSource(net.minecraft.core.BlockPos pos, int dx, int dy, int dz, int entity) implements CustomPacketPayload {
        public static final Type<InfusionSource> TYPE = new Type<>(Thaumcraft.id("infusion_source"));
        public static final StreamCodec<RegistryFriendlyByteBuf, InfusionSource> CODEC = StreamCodec.composite(
                net.minecraft.core.BlockPos.STREAM_CODEC, InfusionSource::pos,
                ByteBufCodecs.VAR_INT, InfusionSource::dx,
                ByteBufCodecs.VAR_INT, InfusionSource::dy,
                ByteBufCodecs.VAR_INT, InfusionSource::dz,
                ByteBufCodecs.VAR_INT, InfusionSource::entity,
                InfusionSource::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void infusionSource(net.minecraft.server.level.ServerLevel level, net.minecraft.core.BlockPos pos, int dx, int dy, int dz, int entity) {
        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().closerThan(pos, 32.0)) ServerPlayNetworking.send(player, new InfusionSource(pos, dx, dy, dz, entity));
        }
    }

    /** O {@code PacketAspectPool}: entraram {@code amount} pontos do aspecto, e agora são {@code total}. */
    public record AspectPool(String tag, int amount, int total) implements CustomPacketPayload {
        public static final Type<AspectPool> TYPE = new Type<>(Thaumcraft.id("aspect_pool"));
        public static final StreamCodec<RegistryFriendlyByteBuf, AspectPool> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, AspectPool::tag, ByteBufCodecs.VAR_INT, AspectPool::amount,
                ByteBufCodecs.VAR_INT, AspectPool::total, AspectPool::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** O {@code PacketAspectDiscovery}: um aspecto descoberto agora. */
    public record AspectDiscovery(String tag) implements CustomPacketPayload {
        public static final Type<AspectDiscovery> TYPE = new Type<>(Thaumcraft.id("aspect_discovery"));
        public static final StreamCodec<RegistryFriendlyByteBuf, AspectDiscovery> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, AspectDiscovery::tag, AspectDiscovery::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** O {@code PacketWarpMessage}: a distorção mudou ({@code type} 0 permanente, 1 a que gruda, 2 temporária). */
    public record WarpMessage(int kind, int amount) implements CustomPacketPayload {
        public static final Type<WarpMessage> TYPE = new Type<>(Thaumcraft.id("warp_message"));
        public static final StreamCodec<RegistryFriendlyByteBuf, WarpMessage> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, WarpMessage::kind, ByteBufCodecs.INT, WarpMessage::amount, WarpMessage::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** O {@code PacketMiscEvent}: 0 é o susto (a vinheta e o coração), 1 a névoa longa, 2 a névoa curta. */
    public record MiscEvent(int kind) implements CustomPacketPayload {
        public static final Type<MiscEvent> TYPE = new Type<>(Thaumcraft.id("misc_event"));
        public static final StreamCodec<RegistryFriendlyByteBuf, MiscEvent> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, MiscEvent::kind, MiscEvent::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** O {@code PacketResearchComplete}: uma pesquisa (ou pista, com arroba) acabou de se completar. */
    public record ResearchComplete(String key) implements CustomPacketPayload {
        public static final Type<ResearchComplete> TYPE = new Type<>(Thaumcraft.id("research_complete"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ResearchComplete> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, ResearchComplete::key, ResearchComplete::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void miscEvent(ServerPlayer player, int kind) {
        ServerPlayNetworking.send(player, new MiscEvent(kind));
    }

    public static void researchComplete(ServerPlayer player, String key) {
        ServerPlayNetworking.send(player, new ResearchComplete(key));
    }

    public static void aspectPool(ServerPlayer player, net.thaumcraft.api.aspects.Aspect aspect, int amount, int total) {
        ServerPlayNetworking.send(player, new AspectPool(aspect.tag(), amount, total));
    }

    public static void aspectDiscovery(ServerPlayer player, net.thaumcraft.api.aspects.Aspect aspect) {
        ServerPlayNetworking.send(player, new AspectDiscovery(aspect.tag()));
    }

    public static void warpMessage(ServerPlayer player, int kind, int amount) {
        ServerPlayNetworking.send(player, new WarpMessage(kind, amount));
    }

    private TCNetwork() {
    }

    public static void init() {
        ResearchTablePayloads.init();
        PayloadTypeRegistry.clientboundPlay().register(BlockSparkle.TYPE, BlockSparkle.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BoreDig.TYPE, BoreDig.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BlockZap.TYPE, BlockZap.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(EntityZap.TYPE, EntityZap.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ObjectAspectsSync.TYPE, ObjectAspectsSync.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(EssentiaSource.TYPE, EssentiaSource.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(InfusionSource.TYPE, InfusionSource.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(AspectPool.TYPE, AspectPool.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(AspectDiscovery.TYPE, AspectDiscovery.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(WarpMessage.TYPE, WarpMessage.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(MiscEvent.TYPE, MiscEvent.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ResearchComplete.TYPE, ResearchComplete.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ResearchRequest.TYPE, ResearchRequest.STREAM_CODEC);
        // quem decide se a pesquisa se destranca é o servidor, nunca o livro aberto na tela
        ServerPlayNetworking.registerGlobalReceiver(ResearchRequest.TYPE, (payload, context) ->
                context.server().execute(() ->
                        net.thaumcraft.research.ResearchManager.request(context.player(), payload.key())));
    }
}
