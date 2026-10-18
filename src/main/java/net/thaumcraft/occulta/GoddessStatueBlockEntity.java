package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A alma da Estátua da Deusa: a {@code TileEntityStatueGoddess} do Witchery.
 *
 * <p>Ela <b>não guarda nada</b>. No original guarda o nome de quem a assentou, e esse nome é lido num
 * lugar só — o soco que tira a coluna inteira de uma vez, porque lá a estátua são dois blocos empilhados.
 * Aqui ela é um bloco com a caixa de dois, e quebrá-la já a tira inteira.
 *
 * <p>O que sobra é o de sempre: a alma existe para o jogo ter onde pendurar o <b>desenho</b>, que é um
 * modelo e não um bloco.
 */
public class GoddessStatueBlockEntity extends BlockEntity {
    public GoddessStatueBlockEntity(BlockPos onde, BlockState feitio) {
        super(OccultaBlocks.GODDESS_STATUE_ENTITY, onde, feitio);
    }
}
