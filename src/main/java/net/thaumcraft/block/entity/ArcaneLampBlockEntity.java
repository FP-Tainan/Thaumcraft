package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;

/**
 * A lâmpada arcana: o {@code TileArcaneLamp} da 4.2.3.5.
 *
 * <p>A cada tique sorteia um lugar até quinze blocos em volta (nunca mais de quatro acima do chão); se for ar e
 * estiver com menos de nove de luz, põe ali uma luz invisível. Quebrada, apaga todas as luzes num cubo de
 * trinta e um de lado.
 */
public class ArcaneLampBlockEntity extends BlockEntity {
    private static final int REACH = 15;

    public ArcaneLampBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ARCANE_LAMP, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ArcaneLampBlockEntity lamp) {
        var random = level.getRandom();
        int x = pos.getX() + random.nextInt(16) - random.nextInt(16);
        int y = pos.getY() + random.nextInt(16) - random.nextInt(16);
        int z = pos.getZ() + random.nextInt(16) - random.nextInt(16);
        if (!level.isLoaded(new BlockPos(x, pos.getY(), z))) return;
        int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 4;
        if (y > top) y = top;
        if (y < level.getMinY() + 5) y = level.getMinY() + 5;
        BlockPos at = new BlockPos(x, y, z);
        if (level.isEmptyBlock(at) && level.getMaxLocalRawBrightness(at) < 9) {
            level.setBlock(at, TCBlocks.LAMP_LIGHT.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    /** O {@code removeLights}: some com as luzes da volta quando a lâmpada é quebrada. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level == null || this.level.isClientSide()) return;
        for (BlockPos at : BlockPos.betweenClosed(pos.offset(-REACH, -REACH, -REACH), pos.offset(REACH, REACH, REACH))) {
            if (this.level.getBlockState(at).is(TCBlocks.LAMP_LIGHT)) this.level.removeBlock(at, false);
        }
    }
}
