package net.thaumcraft.arcana.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

/**
 * O <b>Elo Partido</b>: o {@code BlockBrokenPowerLink} da 1.4.0.009.
 *
 * <p>Quando a rede confere um caminho e encontra um buraco onde devia estar um nó, ela <b>marca o
 * lugar</b>. Este bloco é essa marca: não se vê, não se toca, não tem forma, e quem puser outra coisa por
 * cima dele apaga-o sem dar por isso.
 *
 * <p>Serve a uma coisa só, e é uma coisa boa: ter onde olhar quando a máquina deixou de receber e
 * ninguém sabe porquê. A rede do Ars Magica 2 salta de nó em nó por caminhos que ninguém vê, e tirar um
 * conduto do meio de um corredor parte tudo o que passava por lá sem fazer barulho nenhum.
 *
 * <p><b>Quem o vê</b> são os <b>Óculos Magitécnicos</b>, e só eles — no original é o {@code getSelectedBoundingBox}
 * que pergunta pelo elmo de quem está a olhar. Os óculos ainda não entraram neste porte: até entrarem, o
 * elo é invisível para toda a gente, que é o que o original mostra a quem não os tem.
 */
public class BrokenPowerLinkBlock extends Block {
    public BrokenPowerLinkBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter nível, BlockPos onde, CollisionContext quem) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState feitio, BlockGetter nível, BlockPos onde, CollisionContext quem) {
        return Shapes.empty();
    }

    /** Pôr qualquer coisa por cima apaga a marca: é assim que se conserta uma rede. */
    @Override
    protected boolean canBeReplaced(BlockState feitio, net.minecraft.world.item.context.BlockPlaceContext contexto) {
        return true;
    }
}
