package net.thaumcraft.forbidden;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Optional;

/**
 * Uma das três caras de uma ferramenta camaleão: os encantamentos e o nome que ela tem naquela fase.
 *
 * <p>No original isso é a lista {@code enchants0..2} e os nomes {@code Name0..2} que o {@code ItemMorph*} guarda
 * no NBT da ferramenta; aqui é um pedaço de dado com o mesmo feitio.
 */
public record MorphSlot(ItemEnchantments enchantments, Optional<Component> name) {
    public static final MorphSlot EMPTY = new MorphSlot(ItemEnchantments.EMPTY, Optional.empty());

    public static final Codec<MorphSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemEnchantments.CODEC.optionalFieldOf("enchantments", ItemEnchantments.EMPTY).forGetter(MorphSlot::enchantments),
            ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(MorphSlot::name)
    ).apply(instance, MorphSlot::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MorphSlot> STREAM_CODEC = StreamCodec.composite(
            ItemEnchantments.STREAM_CODEC, MorphSlot::enchantments,
            ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC), MorphSlot::name,
            MorphSlot::new);
}
