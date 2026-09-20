package net.thaumcraft.test.addon;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.api.ThaumcraftApi;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.research.Page;

/**
 * Um mod de mentira, que entra no Thaumcraft pela porta dos mods de fora ({@link ThaumcraftApi}) exatamente como os
 * addons do original entravam pelo {@code ThaumcraftApi}: abre uma aba no livro, põe uma pesquisa nela, registra uma
 * receita de crisol que a página da pesquisa mostra, anota aspectos numa coisa e põe distorção noutra.
 *
 * <p>Ele existe para que {@code AddonApiGameTest} confira que a porta continua aberta — se algum dia ela fechar, os
 * testes quebram aqui, e não no meio do porte de um addon de verdade.
 *
 * <p>Só mexe com peças de criador (a barreira e o vazio de estrutura), que nenhum outro teste olha, para não mudar o
 * que o mod de verdade diz de coisa nenhuma.
 */
public class TestAddon implements ModInitializer {
    public static final String CATEGORY = "TESTADDON";
    public static final String RESEARCH = "TESTADDON_ROOT";
    public static final String RECIPE = "TestAddonCrucible";

    @Override
    public void onInitialize() {
        ThaumcraftApi.category(CATEGORY,
                Identifier.fromNamespaceAndPath("thaumcraft", "textures/misc/r_artifice.png"),
                Identifier.fromNamespaceAndPath("thaumcraft", "textures/gui/gui_researchback.png"));

        // a receita carrega itens, então vai para a fila de montar tarde
        ThaumcraftApi.onSetup(() -> ThaumcraftApi.bookRecipe(RECIPE, ThaumcraftApi.crucible(RESEARCH,
                new ItemStack(Items.STRUCTURE_VOID), Items.BARRIER,
                new AspectList().add(Aspects.MAGIC, 5).add(Aspects.VOID, 5))));

        ThaumcraftApi.research(RESEARCH, CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 3))
                .at(0, 0)
                .complexity(1)
                .icon(() -> new ItemStack(Items.STRUCTURE_VOID))
                .round()
                .auto()
                .pages(Page.text("testaddon.research_page.1"), Page.crucible(RECIPE))
                .warp(1)
                .register();

        ThaumcraftApi.aspects(registrar ->
                registrar.item("minecraft:barrier", new AspectList().add(Aspects.VOID, 7).add(Aspects.ARMOR, 3)));

        ThaumcraftApi.warp(Items.BARRIER, 2);
    }
}
