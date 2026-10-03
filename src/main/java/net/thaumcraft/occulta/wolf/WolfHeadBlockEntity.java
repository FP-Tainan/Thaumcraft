package net.thaumcraft.occulta.wolf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.occulta.OccultaBlocks;

/**
 * A alma da Cabeça de Lobo: a {@code TileEntityWolfHead} do Witchery.
 *
 * <p>No original ela guardava o <b>giro</b> e o <b>tipo</b> da cabeça, porque na 1.7.10 um bloco só tinha
 * quatro bits de estado e o crânio precisava de mais. Hoje os dois cabem no próprio estado do bloco, e por
 * isso a alma <b>não guarda nada</b>: ela existe só para o desenho ter onde se pendurar.
 *
 * <p>O <b>tipo</b> não está aqui por outra razão: o original tem dois — o lobo e o <b>cão-do-inferno</b> —, e
 * o cão-do-inferno é um bicho que este porte ainda não tem. Quando ele vier, a cabeça dele vem com ele.
 */
public class WolfHeadBlockEntity extends BlockEntity {
    public WolfHeadBlockEntity(BlockPos onde, BlockState feitio) {
        super(OccultaBlocks.WOLF_HEAD_ENTITY, onde, feitio);
    }
}
