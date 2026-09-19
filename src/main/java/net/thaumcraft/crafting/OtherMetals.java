package net.thaumcraft.crafting;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCResources;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Estanho, prata e chumbo: os metais que o Thaumcraft 4.2.3.5 aproveitava quando outro mod os trazia (os
 * {@code Config.foundTinIngot}, {@code foundSilverIngot} e {@code foundLeadIngot}, e o {@code oreTin} do dicionário).
 *
 * <p>São três linhas do {@code ConfigRecipes} e do {@code Config} do jar, iguais a não ser pelo metal: o crisol que
 * purifica o minério em aglomerado ({@code PureTin}: um de metal e um de ordem) e o que multiplica a pepita em três
 * ({@code TransTin}: dois de metal e um do aspecto do metal — cristal, ganância, ordem); a mineração especial, que dá
 * o aglomerado no lugar do minério; e o bônus da fornalha infernal, uma pepita por minério ou aglomerado. Tudo pelas
 * etiquetas {@code c:}, que é o dicionário de hoje: sem mod que traga o metal, as etiquetas ficam vazias e nada disso
 * acontece, como no original.
 */
public final class OtherMetals {
    /** Um metal: a etiqueta do minério, do bruto e da pepita, o aspecto próprio e os itens do Thaumcraft. */
    public record Metal(String name, TagKey<Item> ores, TagKey<Item> raw, TagKey<Item> nuggets, TagKey<Item> ingots, Aspect aspect) {
        public Item nugget() {
            return TCResources.get(this.name + "_nugget");
        }

        public Item cluster() {
            return TCResources.get("native_" + this.name + "_cluster");
        }
    }

    public static final List<Metal> ALL = List.of(
            metal("tin", Aspects.CRYSTAL),
            metal("silver", Aspects.GREED),
            metal("lead", Aspects.ORDER));

    private OtherMetals() {
    }

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
    }

    private static Metal metal(String name, Aspect aspect) {
        return new Metal(name, tag("ores/" + name), tag("raw_materials/" + name), tag("nuggets/" + name), tag("ingots/" + name), aspect);
    }

    /** As receitas de crisol dos três ({@code PureTin} e {@code TransTin} e as irmãs). */
    public static void addCrucibleRecipes(List<CrucibleRecipe> recipes) {
        for (Metal metal : ALL) {
            String upper = metal.name().toUpperCase(java.util.Locale.ROOT);
            recipes.add(new CrucibleRecipe("PURE" + upper, new ItemStack(metal.cluster()), metal.ores(),
                    new AspectList().merge(Aspects.METAL, 1).merge(Aspects.ORDER, 1)));
            recipes.add(new CrucibleRecipe("TRANS" + upper, new ItemStack(metal.nugget(), 3), metal.nuggets(),
                    new AspectList().merge(Aspects.METAL, 2).merge(metal.aspect(), 1)));
        }
    }

    /** O {@code addSpecialMiningResult(minério, aglomerado, 1.0)} do {@code Config}; o bruto de hoje entra junto. */
    public static @Nullable Item specialMining(ItemStack stack) {
        for (Metal metal : ALL) {
            if (stack.is(metal.ores()) || stack.is(metal.raw())) return metal.cluster();
        }
        return null;
    }

    /** O {@code addSmeltingBonus("oreTin", pepita)} e o do aglomerado. */
    public static @Nullable Item smeltingBonus(ItemStack stack) {
        for (Metal metal : ALL) {
            if (stack.is(metal.ores()) || stack.is(metal.raw()) || stack.is(metal.cluster())) return metal.nugget();
        }
        return null;
    }

    /** O primeiro lingote da etiqueta, ou nada: o que o original pegava do dicionário. */
    public static ItemStack firstIngot(Metal metal) {
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(metal.ingots())) return new ItemStack(holder);
        return ItemStack.EMPTY;
    }
}
