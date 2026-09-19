package net.thaumcraft.block.entity.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.registry.TCBlockEntities;

/** O {@code TileEldritchNothing}: só existe no nada que dá para fora, para o desenhista pôr o céu de estrelas nele. */
public class EldritchNothingBlockEntity extends BlockEntity {
    public EldritchNothingBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_NOTHING, pos, state);
    }
}
