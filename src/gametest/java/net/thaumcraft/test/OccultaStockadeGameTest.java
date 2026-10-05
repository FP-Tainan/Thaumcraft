package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.StockadeBlock;

/**
 * A <b>Paliçada</b>: a cerca que fere quem encosta.
 *
 * <p>A prova que carrega a fatia é a de que a <b>de gelo não se liga à de madeira</b>. No original as nove
 * madeiras são um bloco só — e por isso um carvalho e uma sorveira dão as mãos — enquanto a de gelo é outro
 * bloco e fica de pé sozinha ao lado delas. É um detalhe que se perde ao portar cada madeira como um bloco
 * seu, e é por isso que aqui elas continuam sendo um bloco com nove chaves.
 */
public class OccultaStockadeGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Os números dela são os do original. */
    @GameTest(maxTicks = 20)
    public void itsNumbersAreTheOriginals(GameTestHelper helper) {
        if (StockadeBlock.ESPETA != 3.0f) helper.fail("ela tira três, de dano de cato");
        if (StockadeBlock.Wood.values().length != 9) helper.fail("e são nove madeiras");
        helper.succeed();
    }

    /**
     * <b>As nove madeiras dão as mãos; o gelo não.</b>
     *
     * <p>Esta é a prova que carrega a fatia. Um carvalho ao lado de uma sorveira liga-se a ela, porque no
     * original as duas são o mesmo bloco com números diferentes. Uma paliçada de gelo ao lado de uma de
     * madeira fica de pé sozinha.
     */
    @GameTest(maxTicks = 40)
    public void theNineWoodsJoinHandsAndTheIceDoesNot(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos carvalho = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos sorveira = carvalho.east();
        BlockPos gelo = sorveira.east();

        level.setBlockAndUpdate(carvalho, OccultaBlocks.STOCKADE.defaultBlockState()
                .setValue(StockadeBlock.MADEIRA, StockadeBlock.Wood.OAK));
        level.setBlockAndUpdate(sorveira, OccultaBlocks.STOCKADE.defaultBlockState()
                .setValue(StockadeBlock.MADEIRA, StockadeBlock.Wood.ROWAN));
        level.setBlockAndUpdate(gelo, OccultaBlocks.ICE_STOCKADE.defaultBlockState());

        if (!level.getBlockState(carvalho).getValue(StockadeBlock.EAST)) {
            helper.fail("o carvalho dá a mão à sorveira");
        }
        if (!level.getBlockState(sorveira).getValue(StockadeBlock.WEST)) {
            helper.fail("e ela a ele");
        }
        if (level.getBlockState(sorveira).getValue(StockadeBlock.EAST)) {
            helper.fail("mas nenhuma das duas dá a mão ao gelo");
        }
        if (level.getBlockState(gelo).getValue(StockadeBlock.WEST)) {
            helper.fail("nem o gelo a elas");
        }

        for (BlockPos ali : new BlockPos[]{carvalho, sorveira, gelo}) {
            level.setBlockAndUpdate(ali, Blocks.AIR.defaultBlockState());
        }
        helper.succeed();
    }

    /**
     * <b>Ela fere quem encosta</b>, e a armadura não ajuda.
     *
     * <p>Três de dano de cato, que é dano que passa por armadura. Encostar numa paliçada custa sempre o
     * mesmo.
     */
    @GameTest(maxTicks = 60)
    public void itHurtsWhoeverTouchesIt(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos aqui = new BlockPos(5, 2, 5);
        level.setBlockAndUpdate(helper.absolutePos(aqui), OccultaBlocks.STOCKADE.defaultBlockState());

        var ovelha = helper.spawn(EntityTypes.SHEEP, aqui);
        float tinha = ovelha.getHealth();
        helper.runAfterDelay(20, () -> {
            if (ovelha.getHealth() >= tinha) helper.fail("a paliçada espeta quem encosta");
            ovelha.discard();
            level.setBlockAndUpdate(helper.absolutePos(aqui), Blocks.AIR.defaultBlockState());
            helper.succeed();
        });
    }

    /**
     * <b>E empilhada ela vira parede.</b>
     *
     * <p>Uma paliçada com outra por cima deixa de apontar: a estaca vai a direito até ao teto, e as duas
     * juntas são um muro sem frestas. É a chave {@code up} do feitio, e é o que o original faz com o
     * {@code oneAbove}.
     */
    @GameTest(maxTicks = 40)
    public void stackedTheyBecomeAWall(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos baixo = helper.absolutePos(new BlockPos(2, 2, 5));
        level.setBlockAndUpdate(baixo, OccultaBlocks.STOCKADE.defaultBlockState());
        if (level.getBlockState(baixo).getValue(StockadeBlock.UP)) helper.fail("sozinha, ela aponta");

        level.setBlockAndUpdate(baixo.above(), OccultaBlocks.STOCKADE.defaultBlockState());
        if (!level.getBlockState(baixo).getValue(StockadeBlock.UP)) {
            helper.fail("com outra por cima, a de baixo vai a direito");
        }
        if (level.getBlockState(baixo.above()).getValue(StockadeBlock.UP)) {
            helper.fail("e a de cima aponta, que é a ponta do muro");
        }

        level.setBlockAndUpdate(baixo.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(baixo, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }
}
