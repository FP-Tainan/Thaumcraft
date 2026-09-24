package net.thaumcraft.naturalis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.thaumcraft.registry.TCComponents;

import java.util.List;
import java.util.function.Consumer;

/**
 * O Baú Arcano na mão: o {@code ArcaneChestBlockItem} do Magia Naturalis 0.5.0. Quando a varinha encolhe o baú,
 * o item leva junto o que havia dentro e a lista de quem podia abrir — e a dica avisa isso.
 */
public class ArcaneChestItem extends net.minecraft.world.item.BlockItem {
    /** A lista de acesso gravada: um par de dono e nível, como o {@code UserAccess} do original. */
    public static final Codec<ArcaneChestBlockEntity.Access> ACCESS_ENTRY = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("id").forGetter(ArcaneChestBlockEntity.Access::id),
                    Codec.BYTE.fieldOf("level").forGetter(ArcaneChestBlockEntity.Access::level)
            ).apply(instance, ArcaneChestBlockEntity.Access::new));
    public static final Codec<List<ArcaneChestBlockEntity.Access>> ACCESS_CODEC = ACCESS_ENTRY.listOf();

    public ArcaneChestItem(Block block, Properties properties) {
        super(block, properties);
    }

    /** Guarda no item o que o baú tinha dentro e quem podia abri-lo. */
    public static void store(ItemStack stack, NonNullList<ItemStack> items, List<ArcaneChestBlockEntity.Access> access) {
        if (items.stream().anyMatch(item -> !item.isEmpty())) {
            stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        }
        if (!access.isEmpty()) stack.set(TCComponents.CHEST_ACCESS, List.copyOf(access));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        boolean guardado = stack.has(DataComponents.CONTAINER);
        boolean lista = stack.has(TCComponents.CHEST_ACCESS);
        if (guardado || lista) lines.accept(Component.empty());
        if (guardado) lines.accept(Component.translatable("hint.thaumcraft.chest.items").withStyle(ChatFormatting.DARK_GRAY));
        if (lista) lines.accept(Component.translatable("hint.thaumcraft.chest.access").withStyle(ChatFormatting.DARK_GRAY));
    }
}
