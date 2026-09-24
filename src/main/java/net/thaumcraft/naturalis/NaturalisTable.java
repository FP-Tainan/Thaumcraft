package net.thaumcraft.naturalis;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.thaumcraft.api.ThaumcraftApi;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;
import net.thaumcraft.research.Page;

/**
 * A tabela do Magia Naturalis: as pesquisas e as receitas do 0.5.0, lidas do MNResearch e do MNRecipes do
 * original. Arquivo gerado por {@code scratchpad/mn-tabela.js} — não se escreve à mão.
 */
public final class NaturalisTable {
    private NaturalisTable() {
    }

    /** As pesquisas, na ordem em que o original as registra. */
    public static void research() {
        ThaumcraftApi.research("MN_INTRO", Naturalis.CATEGORY)
                .at(0, 0)
                .icon(() -> new ItemStack(TCResources.get("primal_charm")))
                .round()
                .auto()
                .special()
                .pages(Page.text("tc.research_page.MN_INTRO.1"))
                .register();

        ThaumcraftApi.research("MN_SICKLES", Naturalis.CATEGORY)
                .aspects(new AspectList().add(Aspects.TOOL, 3).add(Aspects.CROP, 3).add(Aspects.HARVEST, 3))
                .at(-4, 3)
                .complexity(1)
                .icon(() -> new ItemStack(NaturalisItems.THAUMIUM_SICKLE))
                .hiddenParents("THAUMIUM")
                .secondary()
                .pages(Page.text("tc.research_page.MN_SICKLES.1"), Page.crafting("ThaumiumSickle"), Page.crafting("VoidSickle"))
                .register();

    }

    /** As receitas: carregam itens, então só se montam quando o mundo abre. */
    public static void recipes() {
        ThaumcraftApi.bookRecipe("ThaumiumSickle", ThaumcraftApi.crafting(() -> new ItemStack(NaturalisItems.THAUMIUM_SICKLE),
                3, 3, java.util.List.of(java.util.List.<ItemStack>of(), java.util.List.of(new ItemStack(TCResources.get("thaumium_ingot"))), java.util.List.<ItemStack>of(), java.util.List.<ItemStack>of(), java.util.List.<ItemStack>of(), java.util.List.of(new ItemStack(TCResources.get("thaumium_ingot"))), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.STICK)), java.util.List.of(new ItemStack(TCResources.get("thaumium_ingot"))), java.util.List.<ItemStack>of())));
        ThaumcraftApi.bookRecipe("VoidSickle", ThaumcraftApi.crafting(() -> new ItemStack(NaturalisItems.VOID_SICKLE),
                3, 3, java.util.List.of(java.util.List.<ItemStack>of(), java.util.List.of(new ItemStack(TCResources.get("void_ingot"))), java.util.List.<ItemStack>of(), java.util.List.<ItemStack>of(), java.util.List.<ItemStack>of(), java.util.List.of(new ItemStack(TCResources.get("void_ingot"))), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.STICK)), java.util.List.of(new ItemStack(TCResources.get("void_ingot"))), java.util.List.<ItemStack>of())));
    }
}
