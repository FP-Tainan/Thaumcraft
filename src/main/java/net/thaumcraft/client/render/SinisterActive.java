package net.thaumcraft.client.render;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.item.SinisterStoneItem;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/** A pedra sinistra acesa: a condição do modelo do item ({@code thaumcraft:sinister_active}). */
public record SinisterActive() implements ConditionalItemModelProperty {
    public static final MapCodec<SinisterActive> CODEC = MapCodec.unit(new SinisterActive());

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
        return level != null && entity != null && SinisterStoneItem.active(level, entity);
    }

    @Override
    public MapCodec<SinisterActive> type() {
        return CODEC;
    }

    /** O Fabric não abre a lista das condições; o campo é achado pelo nome, como o da tinta de aspecto. */
    @SuppressWarnings("unchecked")
    public static void register() {
        try {
            Field field = ConditionalItemModelProperties.class.getDeclaredField("ID_MAPPER");
            field.setAccessible(true);
            var mapper = (ExtraCodecs.LateBoundIdMapper<net.minecraft.resources.Identifier, MapCodec<? extends ConditionalItemModelProperty>>) field.get(null);
            mapper.put(Thaumcraft.id("sinister_active"), CODEC);
        } catch (ReflectiveOperationException e) {
            Thaumcraft.LOGGER.error("não consegui registrar a condição da pedra sinistra", e);
        }
    }
}
