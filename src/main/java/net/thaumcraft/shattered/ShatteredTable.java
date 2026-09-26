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
        ThaumcraftApi.proxy("SR_ELDRITCH", ShatteredRealms.CATEGORY, "ELDRITCHMINOR", 0, 2);

        // primeiro degrau: sentir
        ThaumcraftApi.research("SR_FRACTURES", ShatteredRealms.CATEGORY)
                .at(0, 0)
                .icon(() -> new ItemStack(ShatteredItems.WORLD_THREAD))
                .parents("SR_ELDRITCH")
                .round()
                .auto()
                .special()
                .pages(Page.text("tc.research_page.SR_FRACTURES.1"), Page.text("tc.research_page.SR_FRACTURES.2"))
                .register();

        // segundo: ver — o fio do mundo e o tecido estável
        ThaumcraftApi.research("SR_WORLD_THREAD", ShatteredRealms.CATEGORY)
                .aspects(new AspectList().add(Aspects.VOID, 3).add(Aspects.CLOTH, 3).add(Aspects.AURA, 2))
                .at(-2, 0)
                .icon(() -> new ItemStack(ShatteredItems.STABLE_FABRIC))
                .parents("SR_FRACTURES")
                .round()
                .pages(Page.text("tc.research_page.SR_WORLD_THREAD.1"), Page.crafting("SRStableFabric"))
                .register();

        // e, ainda no segundo degrau, o que faz o ver valer de facto: os Óculos do Véu
        ThaumcraftApi.research("SR_VEIL_GOGGLES", ShatteredRealms.CATEGORY)
                .aspects(new AspectList().add(Aspects.VOID, 4).add(Aspects.SENSES, 4).add(Aspects.CLOTH, 2))
                .at(-2, -2)
                .icon(() -> new ItemStack(ShatteredItems.VEIL_GOGGLES))
                .parents("SR_WORLD_THREAD")
                .round()
                .pages(Page.text("tc.research_page.SR_VEIL_GOGGLES.1"), Page.crafting("SRVeilGoggles"))
                .register();

        // terceiro: estabilizar — a assinatura
        ThaumcraftApi.research("SR_SIGNATURE", ShatteredRealms.CATEGORY)
                .aspects(new AspectList().add(Aspects.VOID, 4).add(Aspects.TRAVEL, 4).add(Aspects.EXCHANGE, 3))
                .at(-4, 0)
                .icon(() -> new ItemStack(ShatteredItems.RIFT_SIGNATURE))
                .parents("SR_WORLD_THREAD")
                .pages(Page.text("tc.research_page.SR_SIGNATURE.1"),
                        Page.crafting("SRRiftSignature"), Page.crafting("SRStabilizedRiftSignature"),
                        Page.crafting("SRRiftRemover"))
                .register();

        // quarto: atravessar — as portas
        ThaumcraftApi.research("SR_DOORS", ShatteredRealms.CATEGORY)
                .aspects(new AspectList().add(Aspects.VOID, 5).add(Aspects.TRAVEL, 5).add(Aspects.MECHANISM, 3))
                .at(2, 0)
                .icon(() -> new ItemStack(ShatteredBlocks.OAK_DIMENSIONAL_DOOR))
                .parents("SR_WORLD_THREAD")
                .pages(Page.text("tc.research_page.SR_DOORS.1"), Page.text("tc.research_page.SR_DOORS.2"),
                        Page.crafting("SROakDoor"), Page.crafting("SRIronDoor"))
                .register();

        // quinto: mandar — os bolsos
        ThaumcraftApi.research("SR_POCKETS", ShatteredRealms.CATEGORY)
                .aspects(new AspectList().add(Aspects.VOID, 6).add(Aspects.ELDRITCH, 5).add(Aspects.CRAFT, 4))
                .at(4, 0)
                .icon(() -> new ItemStack(FabricBlocks.FABRIC.get(net.minecraft.world.item.DyeColor.BLACK)))
                .parents("SR_DOORS")
                .pages(Page.text("tc.research_page.SR_POCKETS.1"), Page.text("tc.research_page.SR_POCKETS.2"))
                .register();

        // e o que há do outro lado quando a travessia corre mal
        ThaumcraftApi.research("SR_LIMBO", ShatteredRealms.CATEGORY)
                .aspects(new AspectList().add(Aspects.VOID, 6).add(Aspects.DEATH, 4).add(Aspects.ENTROPY, 4))
                .at(2, -2)
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

        ThaumcraftApi.bookRecipe("SRIronDoor", ThaumcraftApi.crafting(
                () -> new ItemStack(ShatteredBlocks.IRON_DIMENSIONAL_DOOR), 3, 1, List.of(
                        List.of(new ItemStack(Items.IRON_DOOR)),
                        List.of(new ItemStack(ShatteredItems.STABLE_FABRIC)),
                        List.of(new ItemStack(Items.IRON_DOOR)))));

        ThaumcraftApi.bookRecipe("SRRiftSignature", ThaumcraftApi.crafting(
                () -> new ItemStack(ShatteredItems.RIFT_SIGNATURE), 3, 3, List.of(
                        List.of(new ItemStack(Items.IRON_INGOT)), List.<ItemStack>of(), List.of(new ItemStack(Items.IRON_INGOT)),
                        List.<ItemStack>of(), List.of(new ItemStack(ShatteredItems.STABLE_FABRIC)), List.<ItemStack>of(),
                        List.of(new ItemStack(Items.IRON_INGOT)), List.<ItemStack>of(), List.of(new ItemStack(Items.IRON_INGOT)))));

        ThaumcraftApi.bookRecipe("SRStabilizedRiftSignature", ThaumcraftApi.crafting(
                () -> new ItemStack(ShatteredItems.STABILIZED_RIFT_SIGNATURE), 3, 3, List.of(
                        List.of(new ItemStack(ShatteredItems.STABLE_FABRIC)), List.<ItemStack>of(), List.of(new ItemStack(ShatteredItems.STABLE_FABRIC)),
                        List.<ItemStack>of(), List.of(new ItemStack(ShatteredItems.RIFT_SIGNATURE)), List.<ItemStack>of(),
                        List.of(new ItemStack(ShatteredItems.STABLE_FABRIC)), List.<ItemStack>of(), List.of(new ItemStack(ShatteredItems.STABLE_FABRIC)))));

        ThaumcraftApi.bookRecipe("SRRiftRemover", ThaumcraftApi.crafting(
                () -> new ItemStack(ShatteredItems.RIFT_REMOVER), 3, 3, List.of(
                        List.of(new ItemStack(Items.GOLD_INGOT)), List.<ItemStack>of(), List.of(new ItemStack(Items.GOLD_INGOT)),
                        List.<ItemStack>of(), List.of(new ItemStack(ShatteredItems.STABLE_FABRIC)), List.<ItemStack>of(),
                        List.of(new ItemStack(Items.GOLD_INGOT)), List.<ItemStack>of(), List.of(new ItemStack(Items.GOLD_INGOT)))));
    }
}
