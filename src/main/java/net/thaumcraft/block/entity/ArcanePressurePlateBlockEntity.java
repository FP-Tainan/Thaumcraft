package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.registry.TCBlockEntities;

/** O {@code TileArcanePressurePlate}: o dono e as chaves; o que dispara a placa fica no estado do bloco. */
public class ArcanePressurePlateBlockEntity extends OwnedBlockEntity {
    public ArcanePressurePlateBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ARCANE_PRESSURE_PLATE, pos, state);
    }
}
