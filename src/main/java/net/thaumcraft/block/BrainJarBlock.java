package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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
import net.thaumcraft.block.entity.BrainJarBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * O jarro de cérebro: o número 1 do {@code BlockJar} da 4.2.3.5 — o vidro do jarro com salmoura e um cérebro que come
 * experiência. Cheio, solta faíscas de feitiço pela tampa. Conta como estante para a mesa de encantar.
 */
public class BrainJarBlock extends BaseEntityBlock {
    public static final MapCodec<BrainJarBlock> CODEC = simpleCodec(BrainJarBlock::new);
    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0);

    public BrainJarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrainJarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.BRAIN_JAR, BrainJarBlockEntity::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof BrainJarBlockEntity jar) {
            jar.spill(level, pos);
            if (level.isClientSide()) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.JAR.value(), SoundSource.BLOCKS, 0.2f, 1.0f, false);
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** O {@code randomDisplayTick}: cheio, faíscas de feitiço verde-azuladas saindo pela boca. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (level.getBlockEntity(pos) instanceof BrainJarBlockEntity jar && jar.xp >= BrainJarBlockEntity.XP_MAX) {
            float g = 0.4f + level.getRandom().nextFloat() * 0.1f, b = 0.3f + level.getRandom().nextFloat() * 0.2f;
            level.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.0f, g, b), pos.getX() + 0.3 + rand.nextFloat() * 0.4f,
                    pos.getY() + 0.9, pos.getZ() + 0.3 + rand.nextFloat() * 0.4f, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof BrainJarBlockEntity jar ? jar.comparator() : 0;
    }
}
