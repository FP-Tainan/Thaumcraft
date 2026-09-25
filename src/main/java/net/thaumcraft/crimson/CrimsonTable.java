package net.thaumcraft.crimson;

import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.ThaumcraftApi;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCResources;
import net.thaumcraft.research.Page;

/**
 * A aba do Crimson Warfare no Thaumonomicon: o {@code warfare.json} do original.
 *
 * <p>Lá são quatro entradas, e três delas são sombras de pesquisas do Thaumcraft postas ali só para servirem de
 * pais à que importa. Aqui ficam as mesmas quatro, com as sombras a apontarem para as pesquisas do porte.
 */
public final class CrimsonTable {
    private CrimsonTable() {
    }

    public static void research() {
        ThaumcraftApi.proxy("CW_VOIDMETAL", Crimson.CATEGORY, "VOIDMETAL", -2, 0);
        ThaumcraftApi.proxy("CW_ELDRITCH", Crimson.CATEGORY, "ELDRITCHMINOR", 2, 0);
        ThaumcraftApi.proxy("CW_TAINT", Crimson.CATEGORY, "BOTTLETAINT", 0, -2);

        ThaumcraftApi.research("CW_WARFARE", Crimson.CATEGORY)
                .aspects(new AspectList().add(Aspects.VOID, 5).add(Aspects.ELDRITCH, 5).add(Aspects.AURA, 4))
                .at(0, 0)
                .icon(() -> new ItemStack(TCResources.get("void_seed")))
                .parents("CW_VOIDMETAL", "CW_ELDRITCH")
                .hiddenParents("CW_TAINT")
                .round()
                .pages(Page.text("tc.research_page.CW_WARFARE.1"))
                .register();
    }
}
