package net.thaumcraft.occulta.brew;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>casa escolhida</b> da Bolsa de Cozimentos: o {@code PacketBrewPrepared} do Witchery e o
 * {@code WITCLastBrewIndex} que ele escreve.
 *
 * <p>É o mesmo arranjo dos símbolos: quem lê o gesto é o <b>lado de cá</b>, porque é lá que a cabeça se
 * mexe, e ele manda dizer ao servidor <b>qual casa</b>. O servidor guarda o número e espera que a bolsa
 * seja largada. Lido do outro lado, o atraso da rede faria o traço sair torto.
 *
 * <p>E há um <b>estalo</b> quando a escolha chega: é a única coisa que diz a quem joga que o traço pegou.
 */
public final class BrewBagChoice {
    /**
     * A casa escolhida.
     *
     * <p><b>Não atravessa a morte</b> e não se guarda em disco: uma escolha dura o tempo de largar a
     * bolsa, e desligar o mundo no meio disso é desistir dela.
     */
    public static final AttachmentType<Integer> ESCOLHIDA = AttachmentRegistry.<Integer>builder()
            .buildAndRegister(Thaumcraft.id("brew_bag_choice"));

    /** O recado: «escolhi esta casa». */
    public record Escolheu(int casa) implements CustomPacketPayload {
        public static final Type<Escolheu> TYPE = new Type<>(Thaumcraft.id("brew_bag_prepared"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Escolheu> CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, Escolheu::casa, Escolheu::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private BrewBagChoice() {
    }

    public static @Nullable Integer escolhida(ServerPlayer quem) {
        return quem.getAttached(ESCOLHIDA);
    }

    public static void escolhe(ServerPlayer quem, int casa) {
        quem.setAttached(ESCOLHIDA, casa);
    }

    public static void esquece(ServerPlayer quem) {
        quem.removeAttached(ESCOLHIDA);
    }

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(Escolheu.TYPE, Escolheu.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Escolheu.TYPE, (recado, quem) ->
                quem.server().execute(() -> {
                    /*
                     * E o servidor <b>confere</b>: uma casa que não existe é largada sem mais. O que vem do
                     * lado de lá nunca se toma por verdade.
                     */
                    if (recado.casa() < 0 || recado.casa() >= BrewBagItem.CASAS) return;
                    ServerPlayer gente = quem.player();
                    escolhe(gente, recado.casa());
                    gente.level().playSound(null, gente.getX(), gente.getY(), gente.getZ(),
                            SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                }));
    }
}
