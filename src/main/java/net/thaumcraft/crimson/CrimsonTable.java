package net.thaumcraft.crimson;

import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.ThaumcraftApi;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCResources;
import net.thaumcraft.research.Page;

/**
 * A pesquisa do Crimson Warfare no Thaumonomicon: o {@code warfare.json} do original.
 *
 * <p><b>Desvio declarado:</b> lá o ramo tem aba própria, com quatro entradas — e três delas são sombras de
 * pesquisas do Thaumcraft postas ali só para servirem de pais à que importa. Uma aba inteira para uma entrada é
 * muita casa para pouca gente, e a pedido de quem joga a entrada passou para junto de {@code CRIMSON}, o Culto
 * Carmesim, que é de onde a coisa vem na lore: quem leu o conto de advertência é quem chega à guerra.
 *
 * <p>Os pais de verdade do original — o metal do vazio e o eldritch menor, com a mácula por trás — ficam como
 * pais escondidos: continuam a ser precisos para a entrada abrir, mas não puxam linha de outra aba.
 */
public final class CrimsonTable {
    private CrimsonTable() {
    }

    public static void research() {
        ThaumcraftApi.research("CW_WARFARE", Crimson.CATEGORY)
                .aspects(new AspectList().add(Aspects.VOID, 5).add(Aspects.ELDRITCH, 5).add(Aspects.AURA, 4))
                .at(0, 6)
                .icon(() -> new ItemStack(TCResources.get("void_seed")))
                .parents("CRIMSON")
                .hiddenParents("VOIDMETAL", "ELDRITCHMINOR", "BOTTLETAINT")
                .round()
                .pages(Page.text("tc.research_page.CW_WARFARE.1"))
                .register();
    }
}
