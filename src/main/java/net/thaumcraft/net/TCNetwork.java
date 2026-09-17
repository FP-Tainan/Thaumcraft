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

    private TCNetwork() {
    }

    public static void init() {
        PayloadTypeRegistry.clientboundPlay().register(ScanSummary.TYPE, ScanSummary.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ResearchRequest.TYPE, ResearchRequest.STREAM_CODEC);
        // quem decide se a pesquisa se destranca é o servidor, nunca o livro aberto na tela
        ServerPlayNetworking.registerGlobalReceiver(ResearchRequest.TYPE, (payload, context) ->
                context.server().execute(() ->
                        net.thaumcraft.research.ResearchManager.unlock(context.player(), payload.key())));
    }

    public static void send(ServerPlayer player, ScanSummary summary) {
        ServerPlayNetworking.send(player, summary);
    }
}
