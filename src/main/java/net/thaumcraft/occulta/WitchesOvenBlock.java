package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * O Forno das Bruxas: o {@code BlockWitchesOven} do Witchery 0.24.1.
 *
 * <p>Cozinha o que um forno comum cozinha, mas só o que vira <b>carvão, comida ou cinza de madeira</b> — e, com
 * um pote de barro dentro, guarda o cheiro do que queimou: um dos sete fumos. Quem manda no que sai é o
 * {@link WitchesOvenBlockEntity}.
 *
 * <p>No original são dois blocos, um aceso e outro apagado, que é como o jogo de 2014 fazia com o forno. Aqui é
 * um só, com a marca {@code lit} — que é como o jogo de hoje faz.
 */
public class WitchesOvenBlock extends BaseEntityBlock {
    public static final MapCodec<WitchesOvenBlock> CODEC = simpleCodec(WitchesOvenBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    /** A luz que ele dá aceso: o {@code setLightLevel(0.875F)} do original, que em luz inteira são catorze. */
    public static final int LIGHT = 14;

    public WitchesOvenBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /** Quem o desenha é o desenhista do bloco, e não um modelo — o {@code getRenderType} do original devolve -1. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WitchesOvenBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, OccultaBlocks.WITCHES_OVEN_ENTITY, WitchesOvenBlockEntity::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof WitchesOvenBlockEntity forno) player.openMenu(forno);
        return InteractionResult.SUCCESS;
    }

    /** Quebrado, ele devolve o que estava dentro. */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moving) {
        if (level.getBlockEntity(pos) instanceof WitchesOvenBlockEntity forno) {
            net.minecraft.world.Containers.dropContents(level, pos, forno);
        }
    }

    /** O fogo e a fumaça saindo da boca, que é o lado para onde ele olha: o {@code randomDisplayTick} do original. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        Direction frente = state.getValue(FACING);
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.2 + random.nextFloat() * 6.0 / 16.0;
        double z = pos.getZ() + 0.5;
        double desvio = random.nextFloat() * 0.6 - 0.3;
        double borda = 0.52;
        double px = x + frente.getStepX() * borda + (frente.getStepX() == 0 ? desvio : 0.0);
        double pz = z + frente.getStepZ() * borda + (frente.getStepZ() == 0 ? desvio : 0.0);
        level.addParticle(ParticleTypes.SMOKE, px, y, pz, 0.0, 0.0, 0.0);
        level.addParticle(ParticleTypes.FLAME, px, y, pz, 0.0, 0.0, 0.0);
    }
}
