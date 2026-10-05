package net.thaumcraft.occulta.symbol;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.thaumcraft.Thaumcraft;

/**
 * O recado de que um <b>símbolo foi desenhado</b>: o {@code PacketSpellPrepared} do Witchery.
 *
 * <p>Quem lê o desenho é o lado de cá — é lá que a cabeça do jogador se move —, e ele manda dizer ao
 * servidor <b>qual</b> símbolo e de que <b>grau</b>. O servidor os guarda e espera que a vara seja largada.
 *
 * <p>É o único jeito de o gesto ser confiável: lido do lado de lá, o atraso da rede faria o desenho sair
 * torto. Lido do lado de cá e <b>confirmado</b> do outro, o que se desenha é o que sai.
 */
public final class SpellNetwork {
    public record Preparou(int id, int grau) implements CustomPacketPayload {
        public static final Type<Preparou> TYPE = new Type<>(Thaumcraft.id("spell_prepared"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Preparou> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, Preparou::id,
                        ByteBufCodecs.VAR_INT, Preparou::grau,
                        Preparou::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private SpellNetwork() {
    }

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(Preparou.TYPE, Preparou.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Preparou.TYPE, (recado, quem) ->
                quem.server().execute(() -> {
                    /*
                     * E o servidor <b>confere</b>: um recado que diga um símbolo que não existe é largado
                     * sem mais. O que vem do lado de lá nunca se toma por verdade.
                     */
                    if (Symbols.daquele(recado.id()) == null) return;
                    Spells.prepara(quem.player(), recado.id(), Math.max(0, Math.min(3, recado.grau())));
                }));
    }
}
