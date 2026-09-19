package net.thaumcraft.block.entity.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.registry.TCBlockEntities;

/** O capstone do anel eldritch: o {@code TileEldritchCap} da 4.2.3.5, que só existe para ser desenhado. */
public class EldritchCapBlockEntity extends BlockEntity {
    public EldritchCapBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_CAP, pos, state);
    }
}
