package net.thaumcraft.maleficium;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Items;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.ThaumcraftApi;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;
import net.thaumcraft.research.Page;

/**
 * O <i>Maleficium</i>: o Tainted Magic 8.1.1, de Yulife, dentro do Thaumcraft.
 *
 * <p>No original era um mod à parte, que entrava pelo {@code ThaumcraftApi}. Aqui as coisas dele vão no mesmo jar,
 * a pedido de quem joga, mas continuam entrando pela mesma porta ({@link ThaumcraftApi}) — o ramo é uma aba própria
 * no Thaumonomicon, e o nome dela é o da lore: <b>Maleficium</b>, onde o original dizia <i>Obscura</i>.
 *
 * <p>Porte fiel, como o resto: nada é inventado, e o que muda de lugar muda porque o Minecraft de hoje não tem mais
 * onde o original o punha.
 */
public final class Maleficium {
    /** A aba do ramo no livro. */
    public static final String CATEGORY = "MALEFICIUM";

    private Maleficium() {
    }

    public static void init() {
        MaleficiumBlocks.init();
        MaleficiumItems.init();
        research();
        Thaumcraft.LOGGER.info("Maleficium: {} coisas", MaleficiumItems.count());
    }

    private static void research() {
        ThaumcraftApi.category(CATEGORY,
                Thaumcraft.id("textures/misc/r_maleficium.png"),
                Thaumcraft.id("textures/gui/gui_maleficium_researchback.png"));

        // a página manchada de sangue que começa tudo: já vem sabida, como no original
        ThaumcraftApi.research("MALEFICIUM", CATEGORY)
                .at(2, -1)
                .icon(() -> new ItemStack(TCResources.ALL.get("knowledge_fragment")))
                .round()
                .auto()
                .pages(Page.text("tc.research_page.MALEFICIUM.1"), Page.text("tc.research_page.MALEFICIUM.2"))
                .register();

        ThaumcraftApi.research("SHADOWMETAL", CATEGORY)
                .aspects(new AspectList().add(Aspects.METAL, 1).add(Aspects.DARKNESS, 1).add(Aspects.MAGIC, 1))
                .at(0, 1)
                .complexity(1)
                .icon(() -> new ItemStack(MaleficiumItems.SHADOWMETAL_INGOT))
                .parents("MALEFICIUM")
                .concealed()
                .pages(Page.text("tc.research_page.SHADOWMETAL.1"), Page.crucible("ShadowMetal"),
                        Page.crafting("ShadowmetalPickaxe"), Page.crafting("ShadowmetalShovel"),
                        Page.crafting("ShadowmetalAxe"), Page.crafting("ShadowmetalHoe"),
                        Page.crafting("ShadowmetalSword"))
                .register();

        ThaumcraftApi.research("UNBALANCEDSHARDS", CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 1).add(Aspects.ENTROPY, 1).add(Aspects.CRYSTAL, 1))
                .at(4, 1)
                .complexity(1)
                .icon(() -> new ItemStack(MaleficiumItems.WARPED_SHARD))
                .parents("MALEFICIUM")
                .concealed()
                .pages(Page.text("tc.research_page.UNBALANCEDSHARDS.1"),
                        Page.crucible("WarpedShard"), Page.crucible("TaintedShard"))
                .register();

        ThaumcraftApi.research("HOLLOWDAGGER", CATEGORY)
                .aspects(new AspectList().add(Aspects.WEAPON, 4).add(Aspects.FIRE, 4).add(Aspects.HEAL, 4))
                .at(0, -3)
                .complexity(2)
                .icon(() -> new ItemStack(MaleficiumItems.HOLLOW_DAGGER))
                .parents("MALEFICIUM")
                .hiddenParents("ENCHFABRIC", "ESSENTIACRYSTAL", "ELDRITCHMINOR")
                .pages(Page.text("tc.research_page.HOLLOWDAGGER.1"), Page.arcane("HollowDagger"))
                .register();

        // as receitas carregam itens, então só se montam quando o mundo abre
        ThaumcraftApi.onSetup(Maleficium::recipes);
    }

    private static void recipes() {
        // o ferro que cai no crisol errado e volta metal das sombras
        ThaumcraftApi.bookRecipe("ShadowMetal", ThaumcraftApi.crucible("SHADOWMETAL",
                new ItemStack(MaleficiumItems.SHADOWMETAL_INGOT), Items.IRON_INGOT,
                new AspectList().add(Aspects.DARKNESS, 3).add(Aspects.METAL, 7).add(Aspects.MAGIC, 2)));

        // e os dois fragmentos desequilibrados, que saem do equilibrado
        ThaumcraftApi.bookRecipe("WarpedShard", ThaumcraftApi.crucible("UNBALANCEDSHARDS",
                new ItemStack(MaleficiumItems.WARPED_SHARD), TCItems.SHARD_BALANCED,
                new AspectList().add(Aspects.ELDRITCH, 4)));
        ThaumcraftApi.bookRecipe("TaintedShard", ThaumcraftApi.crucible("UNBALANCEDSHARDS",
                new ItemStack(MaleficiumItems.TAINTED_SHARD), TCItems.SHARD_BALANCED,
                new AspectList().add(Aspects.TAINT, 4)));

        // as cinco ferramentas: as receitas de verdade são de mesa comum (ficam nos arquivos de receita); estas
        // são as figuras que o livro mostra
        tool("ShadowmetalPickaxe", MaleficiumItems.SHADOWMETAL_PICKAXE, "AAA", " B ", " B ");
        tool("ShadowmetalShovel", MaleficiumItems.SHADOWMETAL_SHOVEL, "A", "B", "B");
        tool("ShadowmetalAxe", MaleficiumItems.SHADOWMETAL_AXE, "AA", "AB", " B");
        tool("ShadowmetalHoe", MaleficiumItems.SHADOWMETAL_HOE, "AA", " B", " B");
        tool("ShadowmetalSword", MaleficiumItems.SHADOWMETAL_SWORD, "A", "A", "B");

        // o punhal oco, na bancada arcana: a haste de osso, uma tora de grande-madeira, um graveto e uma pepita
        ThaumcraftApi.bookRecipe("HollowDagger", ThaumcraftApi.arcane("HOLLOWDAGGER",
                new ItemStack(MaleficiumItems.HOLLOW_DAGGER), new AspectList().add(Aspects.ENTROPY, 85),
                java.util.Arrays.asList(
                        null, null, Ingredient.of(TCItems.WAND_RODS.get("bone")),
                        null, Ingredient.of(TCBlocks.GREATWOOD_LOG), Ingredient.of(Items.IRON_NUGGET),
                        Ingredient.of(Items.STICK), null, null)));
    }

    /** A figura de uma receita de mesa para o livro: o lingote é o A, o graveto é o B. */
    private static void tool(String name, net.minecraft.world.item.Item result, String... pattern) {
        java.util.List<java.util.List<ItemStack>> grid = new java.util.ArrayList<>();
        int width = 0;
        for (String row : pattern) width = Math.max(width, row.length());
        for (String row : pattern) {
            for (int column = 0; column < width; column++) {
                char cell = column < row.length() ? row.charAt(column) : ' ';
                grid.add(switch (cell) {
                    case 'A' -> java.util.List.of(new ItemStack(MaleficiumItems.SHADOWMETAL_INGOT));
                    case 'B' -> java.util.List.of(new ItemStack(Items.STICK));
                    default -> java.util.List.<ItemStack>of();
                });
            }
        }
        ThaumcraftApi.bookRecipe(name, ThaumcraftApi.crafting(() -> new ItemStack(result), width, pattern.length, grid));
    }
}
