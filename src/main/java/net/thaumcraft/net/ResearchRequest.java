package net.thaumcraft.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.thaumcraft.Thaumcraft;

/**
 * O pedido que o livro faz ao servidor quando se clica numa pesquisa para destrancá-la.
 *
 * <p>Quem decide é o servidor: ele confere se os pais estão feitos e se há pontos de aspecto bastante, e
 * só então cobra e marca a pesquisa como sabida.
 */
public record ResearchRequest(String key) implements CustomPacketPayload {
    public static final Type<ResearchRequest> TYPE =
            new Type<>(Thaumcraft.id("research_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchRequest> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> ByteBufCodecs.STRING_UTF8.encode(buffer, payload.key()),
            buffer -> new ResearchRequest(ByteBufCodecs.STRING_UTF8.decode(buffer)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
