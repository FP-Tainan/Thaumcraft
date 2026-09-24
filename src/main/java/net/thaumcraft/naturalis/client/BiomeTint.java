package net.thaumcraft.naturalis.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.naturalis.BiomeSamplerItem;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * A cor da terra que o Amostrador de Bioma guardou: o {@code getColorFromItemStack} dele, que pinta a segunda
 * camada da figura com a cor da folhagem daquela terra.
 */
public record BiomeTint() implements ItemTintSource {
    public static final MapCodec<BiomeTint> CODEC = MapCodec.unit(new BiomeTint());

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        return BiomeSamplerItem.colour(stack, 1);
    }

    @Override
    public MapCodec<BiomeTint> type() {
        return CODEC;
    }

    /** Como a tinta de aspecto, ela entra na lista do jogo pelo campo que o Fabric não abre. */
    @SuppressWarnings("unchecked")
    public static void register() {
        try {
            Field field = ItemTintSources.class.getDeclaredField("ID_MAPPER");
            field.setAccessible(true);
            var mapper = (ExtraCodecs.LateBoundIdMapper<net.minecraft.resources.Identifier,
                    MapCodec<? extends ItemTintSource>>) field.get(null);
            mapper.put(Thaumcraft.id("biome"), CODEC);
        } catch (ReflectiveOperationException e) {
            Thaumcraft.LOGGER.error("não consegui registrar a tinta de bioma", e);
        }
    }
}
