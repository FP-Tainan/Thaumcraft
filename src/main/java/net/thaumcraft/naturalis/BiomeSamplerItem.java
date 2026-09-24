package net.thaumcraft.naturalis;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.biome.Biome;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.registry.TCComponents;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * O Amostrador de Bioma: o {@code BiomeSamplerItem} do Magia Naturalis 0.5.0.
 *
 * <p>Agachado, num bloco qualquer, ele guarda a terra daquele lugar — o nome, a cor e o que ela cobraria. De pé,
 * num Geo-Pilone, ele passa a terra guardada para o pilone, que é o que o pilone vai escrever em volta.
 */
public class BiomeSamplerItem extends Item {
    public BiomeSamplerItem(Properties properties) {
        super(properties);
    }

    /** A terra guardada, se houver. */
    public static @Nullable ResourceKey<Biome> sampled(ItemStack stack) {
        String name = stack.get(TCComponents.SAMPLED_BIOME);
        return name == null ? null : ResourceKey.create(Registries.BIOME, Identifier.parse(name));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return context.getLevel().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        var player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player != null && player.isShiftKeyDown()) {
            Holder<Biome> biome = level.getBiome(context.getClickedPos());
            var key = biome.unwrapKey().orElse(null);
            if (key == null) return InteractionResult.PASS;
            stack.set(TCComponents.SAMPLED_BIOME, key.identifier().toString());
            stack.set(TCComponents.SAMPLED_COLOUR, biome.value().getFoliageColor());
            stack.set(TCComponents.SAMPLED_COST, GeoPylonBlockEntity.cost(level, key));
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof GeoPylonBlockEntity pylon)) {
            return InteractionResult.PASS;
        }
        pylon.target(sampled(stack));
        return InteractionResult.SUCCESS;
    }

    /** O nome da coisa leva o nome da terra guardada na frente, como no original. */
    @Override
    public Component getName(ItemStack stack) {
        var key = sampled(stack);
        if (key == null) return super.getName(stack);
        return Component.translatable("biome." + key.identifier().getNamespace() + "." + key.identifier().getPath())
                .append(" ").append(super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        AspectList cost = stack.get(TCComponents.SAMPLED_COST);
        if (cost == null || cost.size() == 0) {
            lines.accept(Component.translatable("hint.thaumcraft.empty").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        for (Aspect aspect : cost.getAspects()) {
            lines.accept(Component.literal(cost.getAmount(aspect) + "x ").append(aspect.name()));
        }
    }

    /** A cor da folhagem da terra guardada, para a segunda camada da figura. */
    public static int colour(ItemStack stack, int layer) {
        if (layer == 0) return -1;
        Integer colour = stack.get(TCComponents.SAMPLED_COLOUR);
        return colour == null ? -1 : 0xFF000000 | colour;
    }
}
