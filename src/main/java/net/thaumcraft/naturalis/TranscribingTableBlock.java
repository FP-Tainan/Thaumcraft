package net.thaumcraft.naturalis;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A Mesa de Transcrição: o {@code TranscribingTableBlock} do Magia Naturalis 0.5.0. Uma mesa arcana com outra
 * pintura, que copia para o diário o que as mesas de decomposição em volta vão tirando.
 */
public class TranscribingTableBlock extends BaseEntityBlock {
    public static final MapCodec<TranscribingTableBlock> CODEC = simpleCodec(TranscribingTableBlock::new);

    /** O tampo da mesa arcana, do jeito que a 4.2.3.5 o media. */
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 13.0, 16.0);

    public TranscribingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TranscribingTableBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                           BlockEntityType<T> type) {
        return level.isClientSide() ? null
                : createTickerHelper(type, NaturalisBlocks.TRANSCRIBING_TABLE_ENTITY, TranscribingTableBlockEntity::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof TranscribingTableBlockEntity table) player.openMenu(table);
        return InteractionResult.CONSUME;
    }

    /** O fio de encanto que vai da mesa de decomposição até aqui, quando ela tem um primário para dar. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0) return;
        if (!(level.getBlockEntity(pos) instanceof TranscribingTableBlockEntity table)) return;
        if (!(table.getItem(0).getItem() instanceof ResearchLogItem)) return;
        BlockPos alvo = TranscribingTableBlockEntity.pick(level, pos);
        if (alvo == null) return;
        if (!(level.getBlockEntity(alvo) instanceof net.thaumcraft.block.entity.DeconstructionTableBlockEntity mesa)) return;
        if (mesa.aspect() == null) return;
        level.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 2.0,
                pos.getZ() + 0.5, alvo.getX() - pos.getX(), -1.0, alvo.getZ() - pos.getZ());
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        if (level.getBlockEntity(pos) instanceof TranscribingTableBlockEntity table) {
            Containers.dropContents(level, pos, table);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }
}
