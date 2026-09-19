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
    /**
     * Do servidor para quem examinou: o que ele acabou de aprender.
     *
     * <p>Cada aspecto vem com quanto entrou agora e quanto ele tem no total — os dois números que o mod
     * original mostra no canto da tela.
     */
    public record ScanSummary(String name, List<String> tags, List<Integer> gained, List<Integer> totals)
            implements CustomPacketPayload {
        public static final Type<ScanSummary> TYPE = new Type<>(Thaumcraft.id("scan_summary"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ScanSummary> CODEC = StreamCodec.of(
                (buffer, summary) -> {
                    ByteBufCodecs.STRING_UTF8.encode(buffer, summary.name);
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).encode(buffer, summary.tags);
                    ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()).encode(buffer, summary.gained);
                    ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()).encode(buffer, summary.totals);
                },
                buffer -> new ScanSummary(
                        ByteBufCodecs.STRING_UTF8.decode(buffer),
                        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).decode(buffer),
                        ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()).decode(buffer),
                        ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()).decode(buffer)));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

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

    private TCNetwork() {
    }

    public static void init() {
        ResearchTablePayloads.init();
        PayloadTypeRegistry.clientboundPlay().register(ScanSummary.TYPE, ScanSummary.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BlockSparkle.TYPE, BlockSparkle.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BlockZap.TYPE, BlockZap.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(EntityZap.TYPE, EntityZap.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ObjectAspectsSync.TYPE, ObjectAspectsSync.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ResearchRequest.TYPE, ResearchRequest.STREAM_CODEC);
        // quem decide se a pesquisa se destranca é o servidor, nunca o livro aberto na tela
        ServerPlayNetworking.registerGlobalReceiver(ResearchRequest.TYPE, (payload, context) ->
                context.server().execute(() ->
                        net.thaumcraft.research.ResearchManager.request(context.player(), payload.key())));
    }

    public static void send(ServerPlayer player, ScanSummary summary) {
        ServerPlayNetworking.send(player, summary);
    }
}
