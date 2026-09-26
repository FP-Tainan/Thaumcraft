package net.thaumcraft.shattered;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.api.ThaumcraftApi;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.research.Page;

import java.util.List;

/**
 * A aba dos Reinos Fragmentados no Thaumonomicon.
 *
 * <p>As Portas Dimensionais não são addon de Thaumcraft, e a árvore desta aba é <b>do porte</b>. Ela segue os
 * cinco degraus que a lore de quem joga marca: sentir, ver, estabilizar, atravessar e mandar. O que cada pesquisa
 * ensina, porém, é o que o original faz.
 */
public final class ShatteredTable {
    private ShatteredTable() {
    }

    public static void research() {
        // primeiro degrau: sentir
        ThaumcraftApi.research("SR_FRACTURES", ShatteredRealms.CATEGORY)
                .at(6, 0)
                .icon(() -> new ItemStack(ShatteredItems.WORLD_THREAD))
                .parents("ELDRITCHMINOR")
                .round()
                .auto()
                .special()
                .pages(Page.text("tc.research_page.SR_FRACTURES.1"), Page.text("tc.research_page.SR_FRACTURES.2"))
                .register();

        // segundo: ver — o fio do mundo e o tecido estável
        ThaumcraftApi.research("SR_WORLD_THREAD", ShatteredRealms.CATEGORY)
                .aspects(new AspectList().add(Aspects.VOID, 3).add(Aspects.CLOTH, 3).add(Aspects.AURA, 2))
                .at(8, 0)
                .icon(() -> new ItemStack(ShatteredItems.STABLE_FABRIC))
                .parents("SR_FRACTURES")
                .round()
                .pages(Page.text("tc.research_page.SR_WORLD_THREAD.1"), Page.crafting("SRStableFabric"))
                .register();

        // e, ainda no segundo degrau, o que faz o ver valer de facto: os Óculos do Véu
        ThaumcraftApi.research("SR_VEIL_GOGGLES", ShatteredRealms.CATEGORY)
                .aspects(new AspectList().add(Aspects.VOID, 4).add(Aspects.SENSES, 4).add(Aspects.CLOTH, 2))
                .at(8, -2)
                .icon(() -> new ItemStack(ShatteredItems.VEIL_GOGGLES))
                .parents("SR_WORLD_THREAD")
                .round()
                .pages(Page.text("tc.research_page.SR_VEIL_GOGGLES.1"), Page.crafting("SRVeilGoggles"))
                .register();

        // terceiro: rasgar, firmar e cerzir — os três focos, que é o que a varinha faz às fendas
        ThaumcraftApi.research("SR_FOCI", ShatteredRealms.CATEGORY)
                .aspects(new AspectList().add(Aspects.VOID, 5).add(Aspects.MAGIC, 4).add(Aspects.ORDER, 4))
                .at(10, 0)
                .icon(() -> new ItemStack(ShatteredItems.FOCUS_RIFT_HOLD))
                .parents("SR_WORLD_THREAD")
                .pages(Page.text("tc.research_page.SR_FOCI.1"), Page.crafting("SRFocusRiftOpen"),
                        Page.crafting("SRFocusRiftHold"), Page.crafting("SRFocusRiftClose"))
                .register();

        // quarto: atravessar — as portas
        ThaumcraftApi.research("SR_DOORS", ShatteredRealms.CATEGORY)
                .aspects(new AspectList().add(Aspects.VOID, 5).add(Aspects.TRAVEL, 5).add(Aspects.MECHANISM, 3))
                .at(6, 2)
                .icon(() -> new ItemStack(ShatteredBlocks.OAK_DIMENSIONAL_DOOR))
                .parents("SR_WORLD_THREAD")
                .pages(Page.text("tc.research_page.SR_DOORS.1"), Page.text("tc.research_page.SR_DOORS.2"),
                        Page.crafting("SROakDoor"))
                .register();

        // quinto: mandar — os bolsos
        ThaumcraftApi.research("SR_POCKETS", ShatteredRealms.CATEGORY)
                .aspects(new AspectList().add(Aspects.VOID, 6).add(Aspects.ELDRITCH, 5).add(Aspects.CRAFT, 4))
                .at(6, 4)
                .icon(() -> new ItemStack(FabricBlocks.FABRIC.get(net.minecraft.world.item.DyeColor.BLACK)))
                .parents("SR_DOORS")
                .pages(Page.text("tc.research_page.SR_POCKETS.1"), Page.text("tc.research_page.SR_POCKETS.2"))
                .register();

        // e o que há do outro lado quando a travessia corre mal
        ThaumcraftApi.research("SR_LIMBO", ShatteredRealms.CATEGORY)
                .aspects(new AspectList().add(Aspects.VOID, 6).add(Aspects.DEATH, 4).add(Aspects.ENTROPY, 4))
                .at(8, 4)
                .icon(() -> new ItemStack(FabricBlocks.UNRAVELLED))
                .parents("SR_POCKETS")
                .round()
                .pages(Page.text("tc.research_page.SR_LIMBO.1"))
                .register();
    }

    /** As receitas que o livro mostra. */
    public static void recipes() {
        ThaumcraftApi.bookRecipe("SRStableFabric", ThaumcraftApi.crafting(
                () -> new ItemStack(ShatteredItems.STABLE_FABRIC), 3, 1, List.of(
                        List.of(new ItemStack(ShatteredItems.WORLD_THREAD)),
                        List.of(new ItemStack(Items.ENDER_PEARL)),
                        List.of(new ItemStack(ShatteredItems.WORLD_THREAD)))));

        ThaumcraftApi.bookRecipe("SRVeilGoggles", ThaumcraftApi.crafting(
                () -> new ItemStack(ShatteredItems.VEIL_GOGGLES), 3, 2, List.of(
                        List.of(new ItemStack(ShatteredItems.WORLD_THREAD)),
                        List.of(new ItemStack(net.thaumcraft.registry.TCItems.GOGGLES)),
                        List.of(new ItemStack(ShatteredItems.WORLD_THREAD)),
                        List.of(new ItemStack(ShatteredItems.WORLD_THREAD)),
                        List.<ItemStack>of(),
                        List.of(new ItemStack(ShatteredItems.WORLD_THREAD)))));

        ThaumcraftApi.bookRecipe("SROakDoor", ThaumcraftApi.crafting(
                () -> new ItemStack(ShatteredBlocks.OAK_DIMENSIONAL_DOOR), 3, 1, List.of(
                        List.of(new ItemStack(Items.OAK_DOOR)),
                        List.of(new ItemStack(ShatteredItems.STABLE_FABRIC)),
                        List.of(new ItemStack(Items.OAK_DOOR)))));

        // os três focos de fenda, cada um por infusão à volta de um foco em branco
        foco("SRFocusRiftOpen", "SR_FOCI", ShatteredItems.FOCUS_RIFT_OPEN, 4,
                new AspectList().add(Aspects.VOID, 30).add(Aspects.ENTROPY, 20).add(Aspects.TRAVEL, 15));
        foco("SRFocusRiftHold", "SR_FOCI", ShatteredItems.FOCUS_RIFT_HOLD, 3,
                new AspectList().add(Aspects.VOID, 24).add(Aspects.ORDER, 24).add(Aspects.CRYSTAL, 12));
        foco("SRFocusRiftClose", "SR_FOCI", ShatteredItems.FOCUS_RIFT_CLOSE, 3,
                new AspectList().add(Aspects.VOID, 24).add(Aspects.ORDER, 16).add(Aspects.EXCHANGE, 12));
    }

    /**
     * Um foco de fenda: infunde-se à volta de um foco em branco, com Tecido Estável e Fio do Mundo.
     *
     * <p>São os três iguais no feitio — o que muda é o custo de essência e o quanto a infusão treme.
     */
    private static void foco(String chave, String pesquisa, net.minecraft.world.item.Item feito,
                             int treme, AspectList essência) {
        ThaumcraftApi.bookRecipe(chave, ThaumcraftApi.infusion(pesquisa, new ItemStack(feito), treme, essência,
                net.minecraft.world.item.crafting.Ingredient.of(Items.QUARTZ),
                List.of(net.minecraft.world.item.crafting.Ingredient.of(ShatteredItems.STABLE_FABRIC),
                        net.minecraft.world.item.crafting.Ingredient.of(ShatteredItems.WORLD_THREAD),
                        net.minecraft.world.item.crafting.Ingredient.of(ShatteredItems.STABLE_FABRIC),
                        net.minecraft.world.item.crafting.Ingredient.of(ShatteredItems.WORLD_THREAD),
                        net.minecraft.world.item.crafting.Ingredient.of(Items.ENDER_PEARL),
                        net.minecraft.world.item.crafting.Ingredient.of(ShatteredItems.WORLD_THREAD))));
    }
}
