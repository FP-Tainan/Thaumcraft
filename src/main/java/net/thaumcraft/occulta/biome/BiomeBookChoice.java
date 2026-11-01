package net.thaumcraft.occulta.biome;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.thaumcraft.Thaumcraft;

/**
 * A <b>folha escolhida</b> do Livro de Biomas: o {@code PacketItemUpdate} do Witchery.
 *
 * <p>O livro abre-se do lado de cá, onde há tela; a folha em que ele ficou tem de ir para o lado de lá, que
 * é onde o item vive de verdade. É o mesmo arranjo da {@linkplain
 * net.thaumcraft.occulta.brew.BrewBagChoice Bolsa de Cozimentos}.
 *
 * <p><b>Desvio declarado:</b> o original manda o <b>número da folha</b> e, com ele, uma cópia do próprio
 * item — e o servidor acredita na cópia. Aqui vai só o <b>bioma</b>, e o servidor escreve-o no livro que
 * quem mandou o recado tem <b>na mão</b>. Nada do que vem do lado de lá vira item: um recado que nomeie um
 * bioma que não existe, ou que chegue de mãos vazias, cai sem mais.
 */
public final class BiomeBookChoice {
    /** O recado: «abri o livro nesta folha». */
    public record Abriu(ResourceKey<Biome> bioma) implements CustomPacketPayload {
        public static final Type<Abriu> TYPE = new Type<>(Thaumcraft.id("biome_book_page"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Abriu> CODEC =
                StreamCodec.composite(ResourceKey.streamCodec(Registries.BIOME), Abriu::bioma, Abriu::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private BiomeBookChoice() {
    }

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(Abriu.TYPE, Abriu.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Abriu.TYPE, (recado, quem) ->
                quem.server().execute(() -> {
                    var gente = quem.player();
                    // o bioma tem de existir neste mundo: o lado de lá não cria biomas
                    if (gente.level().registryAccess().lookupOrThrow(Registries.BIOME)
                            .get(recado.bioma()).isEmpty()) {
                        return;
                    }
                    for (InteractionHand mão : InteractionHand.values()) {
                        ItemStack livro = gente.getItemInHand(mão);
                        if (!(livro.getItem() instanceof BiomeBookItem)) continue;
                        BiomeBookItem.abre(livro, recado.bioma());
                        return;
                    }
                }));
    }
}
