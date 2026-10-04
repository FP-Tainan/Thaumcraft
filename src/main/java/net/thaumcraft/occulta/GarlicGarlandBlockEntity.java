package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A alma da Guirlanda de Alho, que não guarda nada.
 *
 * <p>Ela existe por uma razão só: o modelo da guirlanda são <b>vinte e nove peças</b> de caixas, e isso não
 * cabe num modelo de bloco do jogo. Ela é o lugar onde o desenhista se pendura, e nada mais — no original é
 * exatamente a mesma coisa, uma {@code TileEntity} vazia que diz {@code canUpdate() == false}.
 */
public class GarlicGarlandBlockEntity extends BlockEntity {
    public GarlicGarlandBlockEntity(BlockPos onde, BlockState state) {
        super(OccultaBlocks.GARLIC_GARLAND_ENTITY, onde, state);
    }
}
