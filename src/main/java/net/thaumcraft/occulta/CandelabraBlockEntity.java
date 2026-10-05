package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A alma do Candelabro: a {@code TileEntityCandelabra} do Witchery.
 *
 * <p>Ela não guarda nada e não faz nada — o original lhe escreve um {@code canUpdate} que devolve falso e
 * mais nada. Existe só porque o candelabro se <b>desenha por fora do bloco</b>, e tanto lá como aqui um
 * desenhista próprio precisa de uma alma a que se pendurar.
 */
public class CandelabraBlockEntity extends BlockEntity {
    public CandelabraBlockEntity(BlockPos onde, BlockState feitio) {
        super(OccultaBlocks.CANDELABRA_ENTITY, onde, feitio);
    }
}
