package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.CrystalClusterBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O aglomerado de cristal: o {@code BlockCrystal} da 4.2.3.5 (0 a 5 um primordial cada, 6 o misto). Cresce da face em
 * que foi posto; sem apoio, cai. Brilha de leve (luz sete), solta faíscas na cor dele e estabiliza a infusão.
 * Quebrado, dá seis fragmentos (o misto, um de cada). Desenhado pelo
 * {@link net.thaumcraft.client.render.CrystalClusterRenderer}.
 */
public class CrystalClusterBlock extends BaseEntityBlock {
    public static final MapCodec<CrystalClusterBlock> CODEC = simpleCodec(properties -> new CrystalClusterBlock(6, properties));
    /** A face em que foi posto: o {@code orientation} do {@code TileCrystal}. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    /** O {@code BlockCustomOreItem.colors}: 0 branco, depois ar, fogo, água, terra, ordem, entropia. */
    public static final int[] COLOURS = {16777215, 16777086, 16727041, 37119, 40960, 15650047, 5592439};
    /** As faíscas, do lado de quem joga. */
    public static Effects clientEffects = (level, pos, colour, random) -> {
    };

    private final int kind;

    public interface Effects {
        void spark(Level level, BlockPos pos, int colour, RandomSource random);
    }

    public CrystalClusterBlock(int kind, Properties properties) {
        super(properties);
        this.kind = kind;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    /** 0 a 5 os primordiais, 6 o misto. */
    public int kind() {
        return this.kind;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState().setValue(FACING, context.getClickedFace());
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    /** O apoio é a face sólida do bloco de trás. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos support = pos.relative(facing.getOpposite());
        return level.getBlockState(support).isFaceSturdy(level, support, facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
        if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) ticks.scheduleTick(pos, this, 1);
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) level.destroyBlock(pos, true);
    }

    /** O {@code randomDisplayTick}: uma vez em dezessete, uma faísca na cor do cristal (o misto sorteia uma). */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(17) != 0) return;
        int colour = this.kind == 6 ? COLOURS[random.nextInt(6) + 1] : COLOURS[this.kind + 1];
        clientEffects.spark(level, pos, colour, random);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrystalClusterBlockEntity(pos, state);
    }
}
