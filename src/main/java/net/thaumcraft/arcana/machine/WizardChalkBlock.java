package net.thaumcraft.arcana.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.arcana.ArcanaItems;

/**
 * O <b>Giz de Mago</b>: o {@code BlockWizardsChalk} da 1.4.0.009.
 *
 * <p>Não é um bloco, é um <b>risco no chão</b> — dois centésimos de altura, atravessa-se, apaga-se com um
 * toque e não deixa nada. O que ele faz é <b>desenhar figuras</b>: o anel à volta do Obelisco, que lhe
 * aumenta o rendimento em um quarto, e as quatro formas dos Rituais de Obelisco.
 *
 * <p>São <b>dezesseis riscos diferentes</b>, sorteados na hora de desenhar. É isso que faz um chão de giz
 * parecer escrito à mão e não carimbado.
 */
public class WizardChalkBlock extends Block {
    /** Qual dos dezesseis riscos é este. */
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 15);

    /** Quantos riscos diferentes há. */
    public static final int VARIANTS = 16;

    private static final VoxelShape FORMA = Block.box(0.0, 0.0, 0.0, 16.0, 0.32, 16.0);

    public WizardChalkBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter nível, BlockPos onde, CollisionContext quem) {
        return FORMA;
    }

    /** Sem chão não há risco. */
    @Override
    protected boolean canSurvive(BlockState feitio, LevelReader nível, BlockPos onde) {
        return nível.getBlockState(onde.below()).isSolidRender();
    }

    /** Pegar nele dá o giz, que é o que o original faz no {@code getPickBlock}. */
    @Override
    protected ItemStack getCloneItemStack(LevelReader nível, BlockPos onde, BlockState feitio, boolean incluirDados) {
        return new ItemStack(ArcanaItems.WIZARD_CHALK);
    }
}
