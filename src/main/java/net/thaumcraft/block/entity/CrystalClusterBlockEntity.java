package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.registry.TCBlockEntities;

/** O {@code TileCrystal} da 4.2.3.5: só existe para o desenho. A face vem do estado do bloco. */
public class CrystalClusterBlockEntity extends BlockEntity {
    public CrystalClusterBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.CRYSTAL_CLUSTER, pos, state);
    }
}
