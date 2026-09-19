package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.thaumcraft.block.entity.ThaumatoriumBlockEntity;
import net.thaumcraft.block.entity.ThaumatoriumTopBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * O taumatório: os números 10 (embaixo) e 11 (em cima) do {@code BlockMetalDevice} da 4.2.3.5 — duas construções
 * alquímicas empilhadas sobre um crisol que a varinha transforma. O de baixo guarda o catalisador e a receita; o de
 * cima só repassa. Sem o crisol embaixo ou sem a outra metade, cada um volta a ser construção alquímica. O modelo
 * inteiro sai do de baixo.
 */
public class ThaumatoriumBlock extends BaseEntityBlock {
    public static final MapCodec<ThaumatoriumBlock> CODEC = simpleCodec(ThaumatoriumBlock::new);
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    /** O {@code facing}: a face em que a varinha bateu; por ela sai o que se fabrica. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public ThaumatoriumBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TOP, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TOP, FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(TOP) ? new ThaumatoriumTopBlockEntity(pos, state) : new ThaumatoriumBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return state.getValue(TOP) ? null : createTickerHelper(type, TCBlockEntities.THAUMATORIUM, ThaumatoriumBlockEntity::tick);
    }

    /** O {@code onBlockActivated}: sem estar agachado, abre a tela do de baixo. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockPos bottom = state.getValue(TOP) ? pos.below() : pos;
        if (level.getBlockEntity(bottom) instanceof ThaumatoriumBlockEntity tile) player.openMenu(tile);
        return InteractionResult.SUCCESS;
    }

    /** O {@code onNeighborBlockChange}: faltando o crisol ou a outra metade, volta a ser construção alquímica. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, @Nullable Orientation orientation, boolean moved) {
        if (!level.isClientSide()) {
            if (!state.getValue(TOP)) {
                BlockState above = level.getBlockState(pos.above());
                boolean ok = above.is(this) && above.getValue(TOP) && level.getBlockState(pos.below()).is(TCBlocks.CRUCIBLE);
                if (!ok) {
                    if (level.getBlockEntity(pos) instanceof ThaumatoriumBlockEntity tile) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), tile.getItem(0));
                        tile.setItem(0, ItemStack.EMPTY);
                    }
                    level.setBlock(pos, TCBlocks.ALCHEMICAL_CONSTRUCT.defaultBlockState(), 3);
                    return;
                }
                if (level.getBlockEntity(pos) instanceof ThaumatoriumBlockEntity tile) tile.getUpgrades();
            } else {
                BlockState below = level.getBlockState(pos.below());
                if (!below.is(this) || below.getValue(TOP)) {
                    level.setBlock(pos, TCBlocks.ALCHEMICAL_CONSTRUCT.defaultBlockState(), 3);
                    return;
                }
                if (level.getBlockEntity(pos.below()) instanceof ThaumatoriumBlockEntity tile) tile.getUpgrades();
            }
        }
        super.neighborChanged(state, level, pos, neighbor, orientation, moved);
    }

    /** O {@code damageDropped}: as duas metades deixam a construção alquímica. */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(new ItemStack(TCBlocks.ALCHEMICAL_CONSTRUCT));
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        BlockEntity te = level.getBlockEntity(pos);
        return te != null && te.triggerEvent(id, param);
    }
}
