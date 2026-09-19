package net.thaumcraft.block.eldritch;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.eldritch.EldritchNothingBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O nada: o {@code BlockEldritchNothing} da 4.2.3.5, a casca entre as salas do labirinto e o vazio. Não se quebra, não se
 * pega, não se vê: onde dá para algum lugar aberto (o {@link #EXPOSED}, o número 1 do original), mostra o céu de
 * estrelas; e quem encosta nele (tirando quem está no criativo) leva oito de dano do vazio a cada tique.
 */
public class EldritchNothingBlock extends BaseEntityBlock {
    public static final MapCodec<EldritchNothingBlock> CODEC = simpleCodec(EldritchNothingBlock::new);
    public static final BooleanProperty EXPOSED = BooleanProperty.create("exposed");
    private static final VoxelShape COLLISION = Block.box(2.0, 2.0, 2.0, 14.0, 14.0, 14.0);

    public EldritchNothingBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(EXPOSED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(EXPOSED);
    }

    /** O {@code BlockUtils.isBlockExposed}: algum vizinho que não tapa a vista. */
    public static boolean exposed(BlockGetter level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            if (!level.getBlockState(pos.relative(d)).isSolidRender()) return true;
        }
        return false;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
                                     BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return state.setValue(EXPOSED, exposed(level, pos));
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(EXPOSED) ? new EldritchNothingBlockEntity(pos, state) : null;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return Shapes.block();
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        if (level instanceof ServerLevel server && entity.tickCount > 20 && !(entity instanceof Player p && p.getAbilities().instabuild)) {
            entity.hurtServer(server, level.damageSources().fellOutOfWorld(), 8.0f);
        }
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return ItemStack.EMPTY;
    }
}
