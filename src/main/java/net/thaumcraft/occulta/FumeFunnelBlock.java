package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * O Funil de Fumos: o {@code BlockFumeFunnel} do Witchery.
 *
 * <p>Posto ao lado do Forno das Bruxas, ou em cima dele, e virado para o mesmo lado que ele, o funil <b>apressa</b>
 * a cozedura e <b>melhora a sorte</b> de o cheiro ficar guardado. O que ele faz de verdade está no
 * {@link WitchesOvenBlockEntity}; aqui ele só se deixa assentar e repassa o clique para o forno a que serve.
 *
 * <p>O com filtro é o mesmo bloco com {@code filtered} ligado, como no original — lá eram duas classes iguais com
 * um sinalizador.
 */
public class FumeFunnelBlock extends net.minecraft.world.level.block.BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final boolean filtered;

    public FumeFunnelBlock(Properties properties, boolean filtered) {
        super(properties);
        this.filtered = filtered;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public boolean filtered() {
        return this.filtered;
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FumeFunnelBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends net.minecraft.world.level.block.BaseEntityBlock> codec() {
        throw new UnsupportedOperationException("o funil não vai em estrutura nem em comando");
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /** Quem o desenha é o desenhista do bloco: o feitio dele muda com os fornos que tem ao lado. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /** O clique no funil abre o forno a que ele serve, como no original. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockPos forno = oven(level, pos, state);
        if (forno != null && level.getBlockEntity(forno) instanceof WitchesOvenBlockEntity miolo) {
            player.openMenu(miolo);
        }
        return InteractionResult.SUCCESS;
    }

    /** O forno que este funil serve: um dos dois lados, ou o de baixo. */
    public static BlockPos oven(Level level, BlockPos pos, BlockState state) {
        Direction mão = state.getValue(FACING).getClockWise();
        for (BlockPos onde : new BlockPos[]{pos.relative(mão), pos.relative(mão.getOpposite()), pos.below()}) {
            if (level.getBlockState(onde).getBlock() instanceof WitchesOvenBlock) return onde;
        }
        return null;
    }

    /** A fumaça saindo do cano, quando ele está em cima do forno. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockState(pos.below()).getBlock() instanceof WitchesOvenBlock)) return;
        level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.45, pos.getY() + 0.4, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
    }

}
