package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.OccultaRituals;

import java.util.List;

/**
 * Os <b>dois cozimentos que não são poção</b>: o de Combustão, que arde, e o de Água sem Fim, que enche.
 */
public class OccultaFuelBrewGameTest {
    /** <b>Os quatro graus ardem</b>, e cada um arde mais que o anterior. */
    @GameTest(maxTicks = 20)
    public void thefourGradesBurnEachLongerThanTheLast(GameTestHelper helper) {
        var quais = OccultaItems.fuelBrews();
        int antes = 0;
        for (int i = 0; i < quais.size(); i++) {
            int quanto = helper.getLevel().fuelValues().burnDuration(new ItemStack(quais.get(i)));
            if (quanto != OccultaItems.FUEL_BREW_TICKS.get(i)) {
                helper.fail("o grau " + (i + 1) + " arde " + OccultaItems.FUEL_BREW_TICKS.get(i)
                        + " batidas, e ardeu " + quanto);
                return;
            }
            if (quanto <= antes) helper.fail("e cada grau arde mais que o anterior");
            antes = quanto;
        }
        // o mais forte vale mais que um balde de lava, que é o que o original quer dele
        int lava = helper.getLevel().fuelValues().burnDuration(new ItemStack(Items.LAVA_BUCKET));
        if (antes <= lava) helper.fail("o quarto grau arde mais que um balde de lava");
        helper.succeed();
    }

    /**
     * <b>As oito receitas saem do caldeirão</b>, e as mais ricas vêm primeiro: o caldeirão fica com a
     * primeira que couber no que está dentro dele, e por isso quem oferece tudo tem de receber o melhor.
     */
    @GameTest(maxTicks = 20)
    public void therichestRecipeIsTheOneTheCauldronTakes(GameTestHelper helper) {
        List<net.minecraft.world.item.Item> tudo = List.of(OccultaItems.MANDRAKE_ROOT,
                Items.GLOWSTONE_DUST, Items.BLAZE_ROD, OccultaItems.ATTUNED_STONE_CHARGED);
        ItemStack melhor = OccultaRituals.result(Items.CHARCOAL, tudo);
        if (!melhor.is(OccultaItems.FUEL_BREW_IV)) {
            helper.fail("oferecendo tudo, o caldeirão dá o quarto grau");
        }
        ItemStack pior = OccultaRituals.result(Items.CHARCOAL, List.of(OccultaItems.MANDRAKE_ROOT));
        if (!pior.is(OccultaItems.FUEL_BREW)) helper.fail("e só com a raiz, o primeiro");

        List<net.minecraft.world.item.Item> água = List.of(Items.NETHER_WART, Items.GLOWSTONE_DUST,
                Items.BLAZE_ROD, OccultaItems.ATTUNED_STONE_CHARGED);
        ItemStack cheio = OccultaRituals.result(Items.SNOW_BLOCK, água);
        if (!cheio.is(OccultaItems.ENDLESS_WATER_BREW)) helper.fail("e a neve dá a Água sem Fim");
        if (cheio.getDamageValue() != 0) helper.fail("cheia, quando se oferece tudo");

        ItemStack magra = OccultaRituals.result(Items.SNOW_BLOCK, List.of(Items.NETHER_WART));
        if (magra.getDamageValue() != 95) {
            helper.fail("e com cinco goles só, quando se oferece a verruga sozinha; deu "
                    + (100 - magra.getDamageValue()));
        }
        helper.succeed();
    }

    /**
     * <b>Um gole põe água</b> onde não havia, e <b>enche a caldeira do jogo</b> de uma vez — e cada gole
     * custa um do frasco.
     */
    @GameTest(maxTicks = 20, padding = 8)
    public void aSipPlacesWaterAndFillsACauldron(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos chão = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(chão, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(chão.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(chão.east(), Blocks.CAULDRON.defaultBlockState());

        ItemStack frasco = new ItemStack(OccultaItems.ENDLESS_WATER_BREW);
        frasco.set(DataComponents.DAMAGE, 0);

        // em cima da pedra: uma casa de água na face de cima
        usa(helper, frasco, chão, net.minecraft.core.Direction.UP);
        if (!level.getBlockState(chão.above()).is(Blocks.WATER)) helper.fail("um gole põe água");
        if (frasco.getDamageValue() != 1) helper.fail("e custa um gole");

        // na caldeira do jogo: ela enche de uma vez
        usa(helper, frasco, chão.east(), net.minecraft.core.Direction.UP);
        var caldeira = level.getBlockState(chão.east());
        if (!caldeira.is(Blocks.WATER_CAULDRON)) helper.fail("e enche a caldeira do jogo");
        else if (caldeira.getValue(LayeredCauldronBlock.LEVEL) != LayeredCauldronBlock.MAX_FILL_LEVEL) {
            helper.fail("e enche-a até acima, de uma vez");
        }
        if (frasco.getDamageValue() != 2) helper.fail("por mais um gole");

        level.setBlockAndUpdate(chão.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(chão.east(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(chão, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** Clica com o frasco numa casa, como quem o usa à mão. */
    private static void usa(GameTestHelper helper, ItemStack frasco, BlockPos onde,
                            net.minecraft.core.Direction face) {
        var level = helper.getLevel();
        var alvo = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(onde), face, onde, false);
        frasco.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(
                level, null, net.minecraft.world.InteractionHand.MAIN_HAND, frasco, alvo));
    }
}
