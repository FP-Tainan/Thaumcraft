package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.registry.TCBlockEntities;

/**
 * O miolo do Nitor.
 *
 * <p>Ele não guarda nada: existe só para o desenhista ter onde se pendurar. O Nitor é um brilho virado
 * para quem olha, e isso um modelo de bloco comum não sabe fazer — precisa de alguém desenhando quadro a
 * quadro.
 */
public class NitorBlockEntity extends BlockEntity {
    public NitorBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.NITOR, pos, state);
    }
}
