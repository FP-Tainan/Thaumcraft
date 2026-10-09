package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.thaumcraft.occulta.EntityLocatorItem;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.PoppetShelfCompassItem;
import net.thaumcraft.occulta.TaglockItem;

import java.util.List;

/**
 * As duas <b>bússolas</b> e a <b>Picareta de Koboldite</b>: a que aponta para uma pessoa, a que esquenta
 * perto de uma prateleira, e a picareta que é só uma picareta.
 */
public class OccultaCompassGameTest {
    /**
     * <b>A agulha aponta para quem o vínculo pega.</b>
     *
     * <p>Com a vaca ao norte e quem olha para o norte, a cara é a do rumo da frente; virando-se, a cara
     * muda. E sem vínculo, a cara é a de «nada».
     */
    @GameTest
    public void theNeedlePointsAtWhoTheTaglockHolds(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(2, 2, 2));
        quem.snapTo(meio.getX() + 0.5, meio.getY(), meio.getZ() + 0.5, 0.0f, 0.0f);

        ItemStack bússola = new ItemStack(OccultaItems.PLAYER_COMPASS);
        if (EntityLocatorItem.cara(bússola) != 0) helper.fail("uma bússola nova não aponta para nada");

        // sem vínculo, continua não apontando
        bate(level, bússola, quem);
        if (EntityLocatorItem.cara(bússola) != 0) helper.fail("e sem vínculo continua assim");

        // presa a uma vaca que está mesmo à frente
        var vaca = helper.spawn(EntityTypes.COW, new BlockPos(2, 2, 10));
        TaglockItem.bind(bússola, vaca);
        bate(level, bússola, quem);
        int deFrente = EntityLocatorItem.cara(bússola);
        if (deFrente < 1 || deFrente > EntityLocatorItem.CARAS - 1) {
            helper.fail("presa a alguém ela aponta, e a cara é " + deFrente);
            return;
        }

        // virando-se meia volta, a agulha vira com quem a traz
        quem.snapTo(meio.getX() + 0.5, meio.getY(), meio.getZ() + 0.5, 180.0f, 0.0f);
        bate(level, bússola, quem);
        int deCostas = EntityLocatorItem.cara(bússola);
        if (deCostas == deFrente) helper.fail("virando-se, a agulha vira: " + deFrente + " e " + deCostas);

        // as duas caras estão entre as trinta e duas do rumo
        if (deCostas < 1 || deCostas > EntityLocatorItem.CARAS - 1) {
            helper.fail("e a cara de costas é " + deCostas);
        }
        helper.succeed();
    }

    /** Uma batida da bússola, na hora em que ela conta. */
    private static void bate(ServerLevel level, ItemStack bússola, ServerPlayer quem) {
        EntityLocatorItem.conta(bússola, level, quem);
    }

    /**
     * <b>A outra bússola esquenta.</b>
     *
     * <p>Cinco faixas e o «longe demais», e a conta é a do original: oito blocos, dezesseis, trinta e dois,
     * sessenta e quatro, cento e vinte e oito.
     */
    @GameTest
    public void theOtherCompassGetsWarmer(GameTestHelper helper) {
        int[] distâncias = {0, 7, 15, 31, 63, 127, 200};
        int[] esperadas = {5, 5, 4, 3, 2, 1, 0};
        for (int n = 0; n < distâncias.length; n++) {
            int deu = PoppetShelfCompassItem.caraDe(distâncias[n]);
            if (deu != esperadas[n]) {
                helper.fail("a " + distâncias[n] + " blocos a cara é " + esperadas[n] + ", e é " + deu);
            }
        }
        helper.succeed();
    }

    /**
     * <b>E ela acha a prateleira de verdade.</b>
     *
     * <p>Posta uma Prateleira de Bonecas quatro blocos ao lado, a bússola na mão de quem está ali passa à
     * cara mais quente — e levando-a para longe dentro da arena ela esfria um degrau.
     */
    @GameTest
    public void theShelfCompassFindsTheShelf(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        helper.setBlock(new BlockPos(2, 2, 2), OccultaBlocks.POPPET_SHELF);

        BlockPos perto = helper.absolutePos(new BlockPos(5, 2, 2));
        double quanto = PoppetShelfCompassItem.maisPerto(level, perto);
        if (quanto > 8.0) {
            helper.fail("a prateleira está a três blocos, e a bússola a vê a " + quanto);
            return;
        }
        if (PoppetShelfCompassItem.caraDe(quanto) != PoppetShelfCompassItem.PERTO.length) {
            helper.fail("e a essa distância a cara é a mais quente");
        }

        ItemStack bússola = new ItemStack(OccultaItems.SHELF_COMPASS);
        quem.snapTo(perto.getX() + 0.5, perto.getY(), perto.getZ() + 0.5, 0.0f, 0.0f);
        PoppetShelfCompassItem.conta(bússola, level, quem);
        if (PoppetShelfCompassItem.cara(bússola) != PoppetShelfCompassItem.PERTO.length) {
            helper.fail("a bússola na mão esquenta até o fim, e está em "
                    + PoppetShelfCompassItem.cara(bússola));
        }
        helper.succeed();
    }

    /** E as três receitas, que são as do original — a de refazer o vínculo incluída. */
    @GameTest
    public void theRecipesAreTheOriginals(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        var bancada = CraftingInput.of(3, 2, List.of(
                new ItemStack(Items.NETHER_WART), new ItemStack(OccultaItems.TEAR_OF_THE_GODDESS),
                new ItemStack(Items.VINE),
                new ItemStack(Items.SPIDER_EYE), new ItemStack(Items.COMPASS), ItemStack.EMPTY));
        if (!dá(helper, level, bancada, OccultaItems.PLAYER_COMPASS)) return;

        ItemStack ouro = new ItemStack(Items.GOLD_INGOT);
        ItemStack diamante = new ItemStack(Items.DIAMOND);
        var prateleira = CraftingInput.of(3, 3, List.of(
                ouro.copy(), diamante.copy(), ouro.copy(),
                diamante.copy(), new ItemStack(Items.CLOCK), diamante.copy(),
                ouro.copy(), new ItemStack(OccultaItems.NULL_CATALYST), ouro.copy()));
        if (!dá(helper, level, prateleira, OccultaItems.SHELF_COMPASS)) return;

        ItemStack balde = new ItemStack(Items.LAVA_BUCKET);
        ItemStack lingote = new ItemStack(OccultaItems.KOBOLDITE_INGOT);
        var picareta = CraftingInput.of(3, 3, List.of(
                balde.copy(), new ItemStack(OccultaItems.ATTUNED_STONE_CHARGED), balde.copy(),
                lingote.copy(), lingote.copy(), lingote.copy(),
                ItemStack.EMPTY, new ItemStack(Items.STICK), ItemStack.EMPTY));
        if (!dá(helper, level, picareta, OccultaItems.KOBOLDITE_PICKAXE)) return;

        // e o vínculo, que se refaz: uma bússola presa a alguém aceita outro vínculo
        var vaca = helper.spawn(EntityTypes.COW, new BlockPos(2, 2, 2));
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 4));
        ItemStack presa = new ItemStack(OccultaItems.PLAYER_COMPASS);
        TaglockItem.bind(presa, vaca);
        ItemStack frasco = new ItemStack(OccultaItems.TAGLOCK);
        TaglockItem.bind(frasco, porco);

        var refaz = CraftingInput.of(2, 1, List.of(presa, frasco));
        var achada = level.recipeAccess().getRecipeFor(
                net.minecraft.world.item.crafting.RecipeType.CRAFTING, refaz, level);
        if (achada.isEmpty()) {
            helper.fail("uma bússola presa aceita outro vínculo");
            return;
        }
        ItemStack feita = achada.get().value().assemble(refaz);
        if (!TaglockItem.isFor(feita, porco)) helper.fail("e passa a apontar para o outro");
        helper.succeed();
    }

    private static boolean dá(GameTestHelper helper, ServerLevel level, CraftingInput bancada,
                              net.minecraft.world.item.Item oquê) {
        var achada = level.recipeAccess().getRecipeFor(
                net.minecraft.world.item.crafting.RecipeType.CRAFTING, bancada, level);
        if (achada.isEmpty()) {
            helper.fail("não há receita para " + oquê);
            return false;
        }
        ItemStack feito = achada.get().value().assemble(bancada);
        if (!feito.is(oquê)) {
            helper.fail("a receita havia de dar " + oquê + " e deu " + feito);
            return false;
        }
        return true;
    }
}
