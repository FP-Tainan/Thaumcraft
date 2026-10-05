package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A alma do Cálice: a {@code TileEntityChalice} do Witchery, menos o que ela guardava.
 *
 * <p>No original ela guarda se o cálice está cheio, escreve isso em disco, o manda pela rede e põe o número
 * do bloco de acordo com ele a cada mudança — três lugares para a mesma coisa. Aqui o cheio é um
 * {@linkplain ChaliceBlock#CHEIO feitio do bloco}, que o jogo já guarda e já manda sozinho, e esta alma
 * ficou só com o que o original lhe pedia de resto: <b>existir</b>, para que o desenhista tenha a que se
 * pendurar.
 */
public class ChaliceBlockEntity extends BlockEntity {
    public ChaliceBlockEntity(BlockPos onde, BlockState feitio) {
        super(OccultaBlocks.CHALICE_ENTITY, onde, feitio);
    }
}
