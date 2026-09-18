package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O tubo de mão única: o {@code TileTubeOneway} da 4.2.3.5. Ele só sente a fome do lado para onde aponta e
 * só tira essência dos outros lados, então a essência anda num sentido só.
 */
public class TubeOnewayBlockEntity extends TubeBlockEntity {
    public TubeOnewayBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.TUBE_ONEWAY, pos, state);
    }

    @Override
    protected void calculateSuction(@Nullable Aspect filter, boolean restrict, boolean directional) {
        super.calculateSuction(filter, restrict, true);
    }

    @Override
    protected void equalizeWithNeighbours(Level level, BlockPos pos, boolean directional) {
        super.equalizeWithNeighbours(level, pos, true);
    }
}
