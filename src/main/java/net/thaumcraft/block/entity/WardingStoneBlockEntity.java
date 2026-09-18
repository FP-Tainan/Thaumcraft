package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;

/**
 * O {@code TileWardingStone} da 4.2.3.5: a cada cinco tiques, sem redstone, empurra para trás os bichos que
 * estiverem no ar sobre a pedra; a cada cem, repõe a barreira nos dois blocos de cima, se couber.
 */
public class WardingStoneBlockEntity extends BlockEntity {
    private int count;

    public WardingStoneBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.WARDING_STONE, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WardingStoneBlockEntity stone) {
        if (stone.count == 0) stone.count = level.getRandom().nextInt(100);
        if (stone.count % 5 == 0 && !level.hasNeighborSignal(pos)) {
            AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1).inflate(0.1);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (target.onGround() || target instanceof Player) continue;
                float yaw = (target.getYRot() + 180.0f) * Mth.DEG_TO_RAD;
                target.push(-Mth.sin(yaw) * 0.2f, -0.1, Mth.cos(yaw) * 0.2f);
            }
        }
        if (++stone.count % 100 == 0) {
            for (int up = 1; up <= 2; up++) {
                BlockPos at = pos.above(up);
                BlockState there = level.getBlockState(at);
                if (!there.is(TCBlocks.WARDING_BARRIER) && there.canBeReplaced()) {
                    level.setBlock(at, TCBlocks.WARDING_BARRIER.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }
}
