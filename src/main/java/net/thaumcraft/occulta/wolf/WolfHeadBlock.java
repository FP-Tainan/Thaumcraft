package net.thaumcraft.occulta.wolf;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Cabeça de Lobo</b> no chão: o {@code BlockWolfHead} do Witchery, no ramo em que ela se põe de pé.
 *
 * <p>É um troféu, e é o <b>único jeito de chegar à Estátua do Lobisomem</b> — a estátua pede três delas. Cai
 * de um lobo morto <b>uma vez em doze</b>, e a Pilhagem melhora isso até quatro em doze: três lobos de sorte,
 * ou uma matilha.
 *
 * <p>O original é o crânio do jogo copiado letra por letra, e por isso aqui ela também é <b>dois blocos</b>:
 * este, que fica no chão e <b>gira em dezesseis passos</b>, e o {@link WolfHeadWallBlock}, que se prega numa
 * parede e olha para fora dela. Quem escolhe entre os dois é o item, como o do crânio.
 *
 * <p>E ela usa a <b>pele do lobo do jogo</b>, sem folha nova: no original também, e é por isso que a cabeça
 * pregada na parede é reconhecível de longe.
 *
 * <p><b>O nome mudou, e não por gosto</b>: o original só lhe chama {@code wolfhead}, e esse nome já está
 * tomado neste jogo — o ramo do Mortuorum tem uma <i>cabeça de lobo</i> que é peça de costura, e duas coisas
 * não podem ter o mesmo nome. Esta é a <b>empalhada</b>, a que se prega na parede, e é o que ela diz no jogo.
 */
public class WolfHeadBlock extends BaseEntityBlock {
    public static final MapCodec<WolfHeadBlock> CODEC = simpleCodec(WolfHeadBlock::new);

    /** Os dezesseis passos de giro do crânio. */
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;

    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0);

    public WolfHeadBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(ROTATION,
                net.minecraft.util.Mth.floor(context.getRotation() * 16.0f / 360.0f + 0.5f) & 15);
    }

    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                  CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new WolfHeadBlockEntity(onde, feitio);
    }

    /** O giro desta cabeça em graus, que é o que o desenhista precisa. */
    public static float giro(BlockState feitio) {
        if (feitio.hasProperty(ROTATION)) return feitio.getValue(ROTATION) * 360.0f / 16.0f;
        if (!feitio.hasProperty(WolfHeadWallBlock.FACING)) return 0.0f;
        return switch (feitio.getValue(WolfHeadWallBlock.FACING)) {
            case SOUTH -> 180.0f;
            case WEST -> 270.0f;
            case EAST -> 90.0f;
            default -> 0.0f;
        };
    }

    /** E para onde ela está pregada, ou {@code null} se está no chão. */
    @Nullable
    public static Direction parede(BlockState feitio) {
        return feitio.hasProperty(WolfHeadWallBlock.FACING)
                ? feitio.getValue(WolfHeadWallBlock.FACING) : null;
    }
}
