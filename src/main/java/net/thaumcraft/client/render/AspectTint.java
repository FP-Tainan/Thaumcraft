package net.thaumcraft.client.render;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.CrystalEssenceItem;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * A cor do aspecto que o item carrega: o {@code getColorFromItemStack} da essência cristalizada do original.
 *
 * <p>Sem aspecto — o cristal da aba do criativo —, ela passeia por todos os aspectos, um a cada meio segundo,
 * como o original faz.
 */
public record AspectTint() implements ItemTintSource {
    public static final MapCodec<AspectTint> CODEC = MapCodec.unit(new AspectTint());

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        Aspect aspect = CrystalEssenceItem.aspectOf(stack);
        if (aspect == null) {
            List<Aspect> all = new ArrayList<>(Aspects.all());
            aspect = all.get((int) (System.currentTimeMillis() / 500L % all.size()));
        }
        return 0xFF000000 | aspect.color();
    }

    @Override
    public MapCodec<AspectTint> type() {
        return CODEC;
    }

    /**
     * Põe a tinta na lista do jogo. O Fabric não abre essa lista, e o jogo a guarda num campo privado; como o
     * jogo não é ofuscado, o campo é achado pelo nome.
     */
    @SuppressWarnings("unchecked")
    public static void register() {
        try {
            Field field = ItemTintSources.class.getDeclaredField("ID_MAPPER");
            field.setAccessible(true);
            var mapper = (ExtraCodecs.LateBoundIdMapper<net.minecraft.resources.Identifier, MapCodec<? extends ItemTintSource>>) field.get(null);
            mapper.put(Thaumcraft.id("aspect"), CODEC);
        } catch (ReflectiveOperationException e) {
            Thaumcraft.LOGGER.error("não consegui registrar a tinta de aspecto", e);
        }
    }
}
