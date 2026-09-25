package net.thaumcraft.mortuorum;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A mesa do altar: o {@code BlockAltarBlock} do Necromancy — os dois blocos que a mesa comprida ocupa ao lado da
 * coluna. Não se põe nem se pega: nasce e morre com o altar, e tudo o que se faz nela vale para ele.
 */
public class SummoningAltarPartBlock extends Block {
    public static final MapCodec<SummoningAltarPartBlock> CODEC = simpleCodec(SummoningAltarPartBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** A tábua da mesa fica na metade de cima do bloco. */
    private static final VoxelShape SHAPE = Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);

    public SummoningAltarPartBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** Quem a desenha é o altar inteiro, no renderizador da coluna. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Anda para trás até achar a coluna: são no máximo dois blocos. */
    public static BlockPos master(BlockGetter level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        for (int passo = 1; passo <= 2; passo++) {
            BlockPos atras = pos.relative(facing.getOpposite(), passo);
            if (level.getBlockState(atras).is(MortuorumBlocks.SUMMONING_ALTAR)) return atras;
        }
        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockPos coluna = master(level, pos, state);
        if (coluna == null) return InteractionResult.PASS;
        return SummoningAltarBlock.use(level, coluna, player);
    }

    /** Quebrar a mesa quebra o altar todo, como no original. */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moving) {
        BlockPos coluna = master(level, pos, state);
        if (coluna != null && level.getBlockState(coluna).is(MortuorumBlocks.SUMMONING_ALTAR)) {
            level.destroyBlock(coluna, true);
        }
        Direction facing = state.getValue(FACING);
        for (int passo = 1; passo <= 2; passo++) {
            for (Direction lado : new Direction[]{facing, facing.getOpposite()}) {
                BlockPos outra = pos.relative(lado, passo);
                if (level.getBlockState(outra).is(MortuorumBlocks.SUMMONING_ALTAR_PART)) {
                    level.setBlock(outra, Blocks.AIR.defaultBlockState(), 35);
                }
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moving);
    }
}
