package net.thaumcraft.maleficium;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.ThaumcraftApi;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
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
                .pages(Page.text("tc.research_page.SHADOWMETAL.1"), Page.crucible("ShadowMetal"))
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
    }
}
