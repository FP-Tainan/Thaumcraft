package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.block.BellowsBlock;
import net.thaumcraft.registry.TCBlocks;

/** O fole tem de soprar como o {@code TileBellows}: só o que aponta para o forno conta, e redstone o para. */
public class BellowsGameTest {
    @GameTest
    public void onlyBellowsFacingTheFurnaceCount(GameTestHelper helper) {
        BlockPos furnace = new BlockPos(2, 1, 2);
        helper.setBlock(furnace, TCBlocks.ALCHEMICAL_FURNACE);
        helper.setBlock(furnace.east(), TCBlocks.BELLOWS.defaultBlockState().setValue(BellowsBlock.FACING, Direction.WEST));
        helper.setBlock(furnace.west(), TCBlocks.BELLOWS.defaultBlockState().setValue(BellowsBlock.FACING, Direction.WEST));
        int blowing = BellowsBlock.blowingInto(helper.getLevel(), helper.absolutePos(furnace));
        if (blowing != 1) helper.fail("só o fole que aponta para o forno sopra nele; contou " + blowing);
        helper.setBlock(furnace.east().east(), Blocks.REDSTONE_BLOCK);
        if (BellowsBlock.blowingInto(helper.getLevel(), helper.absolutePos(furnace)) != 0) helper.fail("com redstone, o fole para");
        helper.succeed();
    }
}
