package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.registry.TCBlocks;

/** As peças pequenas do original: velas de sebo e o que vier com elas. */
public class SmallThingsGameTest {
    @GameTest
    public void theCandleStandsOnlyOnFirmGround(GameTestHelper helper) {
        BlockPos floor = new BlockPos(1, 1, 1);
        helper.setBlock(floor, Blocks.STONE.defaultBlockState());
        var candle = TCBlocks.TALLOW_CANDLES.get("red");
        helper.setBlock(floor.above(), candle.defaultBlockState());
        if (candle.defaultBlockState().getLightEmission() != 14) helper.fail("a vela ilumina 14 (o 0,95 do original)");
        if (!candle.defaultBlockState().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
                net.thaumcraft.Thaumcraft.id("infusion_stabilizers")))) helper.fail("e estabiliza a infusão");
        helper.setBlock(floor, Blocks.AIR.defaultBlockState());
        helper.succeedWhen(() -> helper.assertBlockNotPresent(candle, floor.above()));
    }
}
