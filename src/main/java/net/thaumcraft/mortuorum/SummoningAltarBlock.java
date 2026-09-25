package net.thaumcraft.mortuorum;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * O Altar de Invocação: o {@code BlockAltar} do Necromancy.
 *
 * <p>O bloco que se põe é só a coluna do altar; a mesa comprida que sai dele ocupa mais dois blocos para o lado
 * para onde ele olha, que são {@link SummoningAltarPartBlock}. Clicado, abre as sete casas; agachado, acorda o
 * lacaio que estiver montado nelas.
 */
public class SummoningAltarBlock extends BaseEntityBlock {
    public static final MapCodec<SummoningAltarBlock> CODEC = simpleCodec(SummoningAltarBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** A coluna: o modelo tem dezesseis por vinte e quatro de altura nesta casa. */
    private static final VoxelShape SHAPE = net.minecraft.world.level.block.Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public SummoningAltarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SummoningAltarBlockEntity(pos, state);
    }

    /** Quem desenha o altar é o {@code SummoningAltarRenderer}, como no original. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        if (!hasRoom(context.getLevel(), context.getClickedPos(), facing)) return null;
        return this.defaultBlockState().setValue(FACING, facing);
    }

    /** A mesa precisa de dois blocos livres para o lado para onde o altar olha. */
    public static boolean hasRoom(LevelReader level, BlockPos pos, Direction facing) {
        for (int passo = 1; passo <= 2; passo++) {
            if (!level.getBlockState(pos.relative(facing, passo)).canBeReplaced()) return false;
        }
        return true;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        Direction facing = state.getValue(FACING);
        BlockState peca = MortuorumBlocks.SUMMONING_ALTAR_PART.defaultBlockState()
                .setValue(SummoningAltarPartBlock.FACING, facing);
        for (int passo = 1; passo <= 2; passo++) {
            level.setBlock(pos.relative(facing, passo), peca, 3);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return use(level, pos, player);
    }

    /** O {@code func_149727_a} do original, que a peça da mesa também chama. */
    static InteractionResult use(Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof SummoningAltarBlockEntity altar)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown() && (altar.canSpawn() || player.getAbilities().instabuild)) {
            altar.spawn((ServerLevel) level, player);
        } else {
            player.openMenu(altar);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moving) {
        if (level.getBlockEntity(pos) instanceof SummoningAltarBlockEntity altar) {
            net.minecraft.world.Containers.dropContents(level, pos, altar);
        }
        Direction facing = state.getValue(FACING);
        for (int passo = 1; passo <= 2; passo++) {
            BlockPos outra = pos.relative(facing, passo);
            if (level.getBlockState(outra).is(MortuorumBlocks.SUMMONING_ALTAR_PART)) {
                level.setBlock(outra, Blocks.AIR.defaultBlockState(), 35);
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moving);
    }
}
