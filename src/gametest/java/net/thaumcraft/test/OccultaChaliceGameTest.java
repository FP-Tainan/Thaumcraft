package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.CandelabraBlock;
import net.thaumcraft.occulta.ChaliceBlock;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;

/**
 * O <b>Candelabro</b> e o <b>Cálice</b>: os dois enfeites de altar que faltavam.
 *
 * <p>Eles são a primeira coisa deste ramo que <b>só serve para estar em cima de outra coisa</b>. Postos no
 * chão não fazem nada — o candelabro dá luz, e é tudo. Postos num altar, um soma à velocidade e o outro ao
 * teto, e a diferença entre um altar com eles e um altar pelado é o dobro do poder.
 */
public class OccultaChaliceGameTest {
    /** O candelabro arde sempre e dá luz cheia. */
    @GameTest
    public void theCandelabraIsAlwaysLit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(onde, OccultaBlocks.CANDELABRA.defaultBlockState());

        var feitio = level.getBlockState(onde);
        if (feitio.getLightEmission() != 15) {
            helper.fail("o candelabro dá luz cheia; deu " + feitio.getLightEmission());
        }
        if (CandelabraBlock.FUMAÇA != 4) helper.fail("três chamas de cada quatro fumegam");
        helper.succeed();
    }

    /** E ele cai quando lhe tiram o chão. */
    @GameTest(maxTicks = 40)
    public void theCandelabraNeedsAFloor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 3, 2));
        level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(onde, OccultaBlocks.CANDELABRA.defaultBlockState());
        if (!level.getBlockState(onde).is(OccultaBlocks.CANDELABRA)) {
            helper.fail("em chão de pedra ele fica");
        }

        level.setBlockAndUpdate(onde.below(), Blocks.AIR.defaultBlockState());
        if (level.getBlockState(onde).is(OccultaBlocks.CANDELABRA)) {
            helper.fail("e sem chão ele cai");
        }
        helper.succeed();
    }

    /**
     * <b>O cheio e o vazio são dois itens e um bloco só.</b>
     *
     * <p>Cada item põe o mesmo bloco com o seu feitio, e o botão do meio devolve o item certo de volta.
     */
    @GameTest
    public void theTwoChalicesArePlacedByTwoItems(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());

        for (boolean cheio : new boolean[] {false, true}) {
            level.setBlockAndUpdate(onde, OccultaBlocks.CHALICE.defaultBlockState()
                    .setValue(ChaliceBlock.CHEIO, cheio));
            ItemStack devolta = level.getBlockState(onde)
                    .getCloneItemStack(level, onde, false);
            var esperado = cheio ? OccultaItems.FILLED_CHALICE : OccultaItems.CHALICE;
            if (!devolta.is(esperado)) {
                helper.fail("o cálice " + (cheio ? "cheio" : "vazio") + " devolve o item dele; veio "
                        + devolta);
            }
        }
        helper.succeed();
    }

    /** E o cálice também cai sem chão. */
    @GameTest(maxTicks = 40)
    public void theChaliceNeedsAFloorToo(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 3, 2));
        level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(onde, OccultaBlocks.CHALICE.defaultBlockState());
        if (!level.getBlockState(onde).is(OccultaBlocks.CHALICE)) helper.fail("em pedra ele fica");

        level.setBlockAndUpdate(onde.below(), Blocks.AIR.defaultBlockState());
        if (level.getBlockState(onde).is(OccultaBlocks.CHALICE)) helper.fail("e sem chão ele cai");
        helper.succeed();
    }
}
