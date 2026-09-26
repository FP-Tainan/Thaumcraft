package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * O miolo do Funil de Fumos: o {@code TileEntityFumeFunnel} do Witchery, que não guarda nada e não bate.
 *
 * <p>Ele existe por um motivo só, que é o mesmo do original: o funil muda de feitio conforme os fornos que tem ao
 * lado e embaixo, e quem desenha isso é um desenhista de tile — que precisa de um tile para desenhar.
 */
public class FumeFunnelBlockEntity extends BlockEntity {
    public FumeFunnelBlockEntity(BlockPos pos, BlockState state) {
        super(OccultaBlocks.FUME_FUNNEL_ENTITY, pos, state);
    }
}
