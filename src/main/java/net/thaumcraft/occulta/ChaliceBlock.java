package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Cálice</b>: a {@code BlockChalice} do Witchery.
 *
 * <p>Uma taça de ouro de pé largo, que se põe <b>cheia ou vazia</b> e fica como se a puseram. Não se enche
 * no lugar e não se esvazia: o que a enche é a <b>Sopa de Redstone</b>, e isso é feito na bancada, antes de
 * ela descer ao altar.
 *
 * <h2>Para que ele serve</h2>
 *
 * <p>Posto em cima de um <b>Altar da Bruxa</b>, ele soma ao <b>teto</b> do altar: <b>um</b> se estiver
 * vazio, <b>dois</b> se estiver cheio. É o único enfeite do altar que vale mais por estar cheio do que por
 * estar lá, e é por isso que a Sopa de Redstone — que de resto não serve para nada — tem razão de existir.
 *
 * <h2>O cheio e o vazio</h2>
 *
 * <p>No original são <b>dois itens</b> e <b>um bloco com dois números</b>, com a alma do bloco guardando a
 * mesma coisa que o número e os dois sendo postos de acordo um com o outro a cada mudança. Aqui são os
 * mesmos <b>dois itens</b>, mas o cheio vive <b>só no feitio do bloco</b>, que é onde o jogo de hoje guarda
 * esse tipo de coisa. A alma fica, porque o cálice se desenha por fora do bloco e um desenhista precisa dela
 * — mas já não tem nada dentro.
 */
public class ChaliceBlock extends Block implements EntityBlock {
    public static final MapCodec<ChaliceBlock> CODEC = simpleCodec(ChaliceBlock::new);

    /** Se há alguma coisa dentro dele. */
    public static final BooleanProperty CHEIO = BooleanProperty.create("filled");

    /** O que ele ocupa, nos números do original: uma taça pequena e fora do meio. */
    private static final VoxelShape FORMA = Block.box(4.8, 0.0, 5.92, 10.08, 7.36, 11.12);

    public ChaliceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CHEIO, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> construtor) {
        construtor.add(CHEIO);
    }

    /** Ele se desenha sozinho, pelo {@link net.thaumcraft.occulta.client.ChaliceRenderer}. */
    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter mundo, BlockPos onde,
                                  CollisionContext quem) {
        return FORMA;
    }

    /** Ele pousa em chão firme, e só nele. */
    @Override
    protected boolean canSurvive(BlockState feitio, LevelReader mundo, BlockPos onde) {
        BlockPos chão = onde.below();
        return mundo.getBlockState(chão).isFaceSturdy(mundo, chão, Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState feitio, LevelReader mundo, ScheduledTickAccess relógio,
                                     BlockPos onde, Direction lado, BlockPos vizinho, BlockState doVizinho,
                                     RandomSource sorte) {
        if (lado == Direction.DOWN && !feitio.canSurvive(mundo, onde)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(feitio, mundo, relógio, onde, lado, vizinho, doVizinho, sorte);
    }

    /** O botão do meio tira o cálice como ele está. */
    @Override
    public ItemStack getCloneItemStack(LevelReader mundo, BlockPos onde, BlockState feitio,
                                       boolean incluirDados) {
        return new ItemStack(feitio.getValue(CHEIO) ? OccultaItems.FILLED_CHALICE : OccultaItems.CHALICE);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new ChaliceBlockEntity(onde, feitio);
    }

    /** Cheio, ele fumega vermelho — um pó por batida, num canto da taça. */
    @Override
    public void animateTick(BlockState feitio, Level mundo, BlockPos onde, RandomSource sorte) {
        if (!feitio.getValue(CHEIO)) return;
        mundo.addParticle(DustParticleOptions.REDSTONE,
                onde.getX() + 0.45, onde.getY() + 0.4, onde.getZ() + 0.5, 0.0, 0.0, 0.0);
    }
}
