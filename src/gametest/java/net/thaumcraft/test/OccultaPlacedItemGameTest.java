package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.thaumcraft.occulta.AltarBlock;
import net.thaumcraft.occulta.AltarBlockEntity;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.PlacedItemBlock;
import net.thaumcraft.occulta.PlacedItemBlockEntity;

/**
 * O <b>Item Posto</b>, e a <b>Arthana deitada</b> no altar.
 *
 * <p>É um bloco que não é nada e que serve para uma coisa só: fazer com que o altar possa <b>contar o que
 * está em cima dele</b>. Um item largado no chão rola, se junta a outro igual e some ao fim de cinco
 * minutos; um item posto fica onde o puseram.
 *
 * <p>A prova que carrega a fatia é a do <b>alcance</b>: com a faca deitada numa das pedras, o altar
 * <b>dobra</b> o alcance dele.
 */
public class OccultaPlacedItemGameTest {
    /** Posto, ele guarda a coisa, e partido a devolve. */
    @GameTest
    public void itKeepsWhatWasPutOnIt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 3, 2));
        level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());

        PlacedItemBlock.põe(level, onde, new ItemStack(OccultaItems.ARTHANA), null);
        if (!level.getBlockState(onde).is(OccultaBlocks.PLACED_ITEM)) {
            helper.fail("a faca deitada vira um bloco");
        }
        if (!(level.getBlockEntity(onde) instanceof PlacedItemBlockEntity alma)) {
            helper.fail("e o bloco guarda o que lhe deitaram");
            return;
        }
        if (!alma.oquê().is(OccultaItems.ARTHANA)) {
            helper.fail("que é a Arthana; veio " + alma.oquê());
        }
        if (!level.getBlockState(onde).getCloneItemStack(level, onde, false).is(OccultaItems.ARTHANA)) {
            helper.fail("e o botão do meio tira a faca, e não o bloco");
        }
        helper.succeed();
    }

    /** Ele cai com o chão. */
    @GameTest(maxTicks = 40)
    public void itFallsWithTheFloor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 3, 3));
        level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());
        PlacedItemBlock.põe(level, onde, new ItemStack(OccultaItems.ARTHANA), null);

        level.setBlockAndUpdate(onde.below(), Blocks.AIR.defaultBlockState());
        if (level.getBlockState(onde).is(OccultaBlocks.PLACED_ITEM)) {
            helper.fail("sem chão por baixo ele cai");
        }
        helper.succeed();
    }

    /** E ele guarda para onde está virado. */
    @GameTest
    public void itRemembersWhichWayItLies(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(4, 3, 4));
        level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());

        level.setBlockAndUpdate(onde, OccultaBlocks.PLACED_ITEM.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.EAST));
        if (level.getBlockState(onde).getValue(HorizontalDirectionalBlock.FACING) != Direction.EAST) {
            helper.fail("ele guarda para onde está virado");
        }
        helper.succeed();
    }

    /**
     * <b>E a faca deitada no altar dobra o alcance dele.</b>
     *
     * <p>Esta é a prova que carrega a fatia. Dezesseis blocos viram trinta e dois.
     */
    @GameTest
    public void theKnifeOnTheAltarDoublesItsReach(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos canto = helper.absolutePos(new BlockPos(1, 2, 1));
        for (int volta = 0; volta < AltarBlock.PIECES; volta++) {
            level.setBlockAndUpdate(canto.offset(volta % 3, 0, volta / 3),
                    OccultaBlocks.WITCH_ALTAR.defaultBlockState());
        }
        if (!(level.getBlockEntity(canto) instanceof AltarBlockEntity pedra)) {
            helper.fail("as seis pedras deviam fazer um altar");
            return;
        }
        AltarBlockEntity manda = pedra.core();
        if (manda == null) {
            helper.fail("e uma delas devia mandar");
            return;
        }

        manda.refresh();
        if (manda.range() != AltarBlockEntity.RANGE) {
            helper.fail("um altar pelado alcança dezesseis; alcança " + manda.range());
        }

        PlacedItemBlock.põe(level, canto.above(), new ItemStack(OccultaItems.ARTHANA), null);
        manda.refresh();
        if (manda.range() != AltarBlockEntity.RANGE * 2) {
            helper.fail("com a faca deitada ele alcança trinta e dois; alcança " + manda.range());
        }

        // e uma segunda faca não soma: conta-se uma de cada
        PlacedItemBlock.põe(level, canto.offset(1, 1, 0), new ItemStack(OccultaItems.ARTHANA), null);
        manda.refresh();
        if (manda.range() != AltarBlockEntity.RANGE * 2) {
            helper.fail("a segunda faca não soma; alcança " + manda.range());
        }
        helper.succeed();
    }
}
