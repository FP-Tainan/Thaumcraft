package net.thaumcraft.block.entity.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.registry.TCBlockEntities;

/** O {@code TileEldritchCrystal}: só existe para o desenhista pôr o {@code vcrystal.obj}. */
public class StrangeCrystalBlockEntity extends BlockEntity {
    public StrangeCrystalBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.STRANGE_CRYSTALS, pos, state);
    }
}
