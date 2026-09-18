package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.thaumcraft.registry.TCBlocks;

/** As pedras de pavimento têm de fazer o que o {@code BlockCosmeticSolid} e o {@code TileWardingStone} faziam. */
public class PavingStoneGameTest {
    @GameTest
    public void theTravelStoneSpeedsUpWhoStepsOnIt(GameTestHelper helper) {
        BlockPos stone = new BlockPos(1, 1, 1);
        helper.setBlock(stone, TCBlocks.BUILDING.get("paving_stone_travel"));
        var zombie = helper.spawn(EntityTypes.ZOMBIE, stone.above());
        var state = helper.getBlockState(stone);
        state.getBlock().stepOn(helper.getLevel(), helper.absolutePos(stone), state, zombie);
        if (!zombie.hasEffect(MobEffects.SPEED) || zombie.getEffect(MobEffects.SPEED).getAmplifier() != 1) {
            helper.fail("quem pisa ganha velocidade II");
        }
        if (!zombie.hasEffect(MobEffects.JUMP_BOOST)) helper.fail("e salto");
        helper.succeed();
    }

    @GameTest(maxTicks = 140)
    public void theWardingStoneRaisesAWallOnlyForMobs(GameTestHelper helper) {
        BlockPos stone = new BlockPos(1, 1, 1);
        helper.setBlock(stone, TCBlocks.BUILDING.get("paving_stone_warding"));
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(TCBlocks.WARDING_BARRIER, stone.above());
            helper.assertBlockPresent(TCBlocks.WARDING_BARRIER, stone.above(2));
            var zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(3, 1, 3));
            var player = helper.makeMockServerPlayerInLevel();
            BlockPos wall = helper.absolutePos(stone.above());
            var barrier = helper.getLevel().getBlockState(wall);
            if (barrier.getCollisionShape(helper.getLevel(), wall, CollisionContext.of(zombie)).isEmpty()) {
                throw helper.assertionException("para o bicho a barreira é parede");
            }
            if (!barrier.getCollisionShape(helper.getLevel(), wall, CollisionContext.of(player)).isEmpty()) {
                throw helper.assertionException("para gente a barreira é ar");
            }
            // com redstone na pedra, a barreira se abre
            helper.setBlock(stone.east(), Blocks.REDSTONE_BLOCK);
            if (!barrier.getCollisionShape(helper.getLevel(), wall, CollisionContext.of(zombie)).isEmpty()) {
                throw helper.assertionException("com redstone a barreira devia se abrir");
            }
            zombie.discard();
        });
    }
}
