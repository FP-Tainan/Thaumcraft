package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.entity.JarBlockEntity;
import net.thaumcraft.block.entity.TubeBlockEntity;
import net.thaumcraft.registry.TCBlocks;

/** O jarro do vazio e o tubo estreito têm de seguir o {@code TileJarFillableVoid} e o {@code TileTubeRestrict}. */
public class TubeVariantGameTest {
    @GameTest
    public void theVoidJarSwallowsTheOverflow(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, TCBlocks.JAR_VOID.defaultBlockState());
        JarBlockEntity jar = (JarBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        int left = jar.addToContainer(Aspects.FIRE, 70);
        if (left != 0) helper.fail("o jarro do vazio aceita tudo o que é do aspecto dele");
        if (jar.amount() != JarBlockEntity.CAPACITY) helper.fail("mas guarda só até o limite");
        if (jar.getSuctionAmount(Direction.UP) != 32) helper.fail("cheio, ele continua puxando com trinta e dois");
        if (jar.addToContainer(Aspects.WATER, 1) != 1) helper.fail("outro aspecto ele recusa");
        helper.succeed();
    }

    @GameTest
    public void theRestrictedTubeHalvesTheSuction(GameTestHelper helper) {
        BlockPos jarPos = new BlockPos(1, 2, 1);
        BlockPos tubePos = new BlockPos(1, 3, 1);
        helper.setBlock(jarPos, TCBlocks.JAR.defaultBlockState());
        helper.setBlock(tubePos, TCBlocks.TUBE_RESTRICT.defaultBlockState());
        helper.runAfterDelay(10, () -> {
            TubeBlockEntity tube = (TubeBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(tubePos));
            // o jarro puxa com trinta e dois; o estreito passa dezesseis, e não trinta e um
            if (tube.getSuctionAmount(null) != 16) helper.fail("o estreito devia puxar dezesseis, puxa " + tube.getSuctionAmount(null));
            helper.succeed();
        });
    }
}
