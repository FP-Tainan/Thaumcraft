package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.block.entity.HoleBlockEntity;
import net.thaumcraft.registry.TCBlocks;

/** O buraco portátil tem de seguir o {@code TileHole}: abre um túnel de três por três e devolve tudo no fim. */
public class PortableHoleGameTest {
    @GameTest(maxTicks = 200)
    public void theHoleTunnelsAndCloses(GameTestHelper helper) {
        // um bloco de pedra de três de fundo, e a face de cima clicada
        for (int x = 0; x < 3; x++) for (int y = 1; y < 4; y++) for (int z = 0; z < 3; z++) {
            helper.setBlock(new BlockPos(x, y, z), Blocks.STONE.defaultBlockState());
        }
        BlockPos top = new BlockPos(1, 3, 1);
        HoleBlockEntity.createHole(helper.getLevel(), helper.absolutePos(top), Direction.UP.get3DDataValue(), 3,
                HoleBlockEntity.DURATION);
        helper.runAfterDelay(5, () -> {
            if (!helper.getBlockState(new BlockPos(1, 1, 1)).is(TCBlocks.HOLE)) helper.fail("o túnel desce até o fundo");
            if (!helper.getBlockState(new BlockPos(0, 2, 0)).is(TCBlocks.HOLE)) helper.fail("e abre as casas em volta");
        });
        helper.runAfterDelay(HoleBlockEntity.DURATION + 20, () -> {
            if (!helper.getBlockState(top).is(Blocks.STONE)) helper.fail("no fim a pedra volta");
            if (!helper.getBlockState(new BlockPos(0, 2, 0)).is(Blocks.STONE)) helper.fail("e as casas em volta também");
            helper.succeed();
        });
    }
}
