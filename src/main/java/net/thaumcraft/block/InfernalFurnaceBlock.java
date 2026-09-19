package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.InfernalFurnaceBlockEntity;
import net.thaumcraft.block.entity.InfernalFurnaceNozzleBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A fornalha infernal: o {@code BlockArcaneFurnace} da 4.2.3.5, o cubo 3 × 3 × 3 de obsidiana e tijolo do Nether que a
 * varinha forma. Cada bloco guarda a posição dele na camada (o {@link #PART}: 1 a 9, linha a linha do noroeste), o
 * centro de lava é o 0 e a grade de ferro, a boca, o 10. O centro é aberto por cima: o que se joga ali dentro cai na
 * lava e é fundido; o que sai, sai pela boca.
 */
public class InfernalFurnaceBlock extends BaseEntityBlock {
    public static final MapCodec<InfernalFurnaceBlock> CODEC = simpleCodec(InfernalFurnaceBlock::new);
    /** O número do original: 0 a lava do centro, 1 a 9 a posição na camada, 10 a boca. */
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 10);
    /** Na boca: para que lado fica o centro. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape CENTER = Block.box(0, 0, 0, 16, 4, 16);

    public InfernalFurnaceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PART, 1).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, FACING);
    }

    public static int part(BlockState state) {
        return state.getBlock() instanceof InfernalFurnaceBlock ? state.getValue(PART) : -1;
    }

    /** O {@code createTileEntity}: a fornalha no centro, os bicos nas posições 2, 4, 5, 6 e 8. */
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        int part = state.getValue(PART);
        if (part == 0) return new InfernalFurnaceBlockEntity(pos, state);
        if (part == 2 || part == 4 || part == 5 || part == 6 || part == 8) return new InfernalFurnaceNozzleBlockEntity(pos, state);
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        BlockEntityTicker<T> furnace = createTickerHelper(type, TCBlockEntities.INFERNAL_FURNACE, InfernalFurnaceBlockEntity::serverTick);
        return furnace != null ? furnace : createTickerHelper(type, TCBlockEntities.INFERNAL_FURNACE_NOZZLE, InfernalFurnaceNozzleBlockEntity::serverTick);
    }

    // ------------------------------------------------------------------ as formas

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(PART) == 0 ? CENTER : Shapes.block();
    }

    /** O {@code addCollisionBoxesToList}: o centro tem um quarto de altura; a boca, a metade do lado do centro. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int part = state.getValue(PART);
        if (part == 0) return CENTER;
        if (part != 10) return Shapes.block();
        return switch (state.getValue(FACING)) {
            case WEST -> Block.box(0, 0, 0, 8, 16, 16);
            case EAST -> Block.box(8, 0, 0, 16, 16, 16);
            case NORTH -> Block.box(0, 0, 0, 16, 16, 8);
            default -> Block.box(0, 0, 8, 16, 16, 16);
        };
    }

    /** O {@code onEntityCollidedWithBlock} vale para a casa inteira, não só para o piso da lava. */
    @Override
    protected VoxelShape getEntityInsideCollisionShape(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return Shapes.block();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return false;
    }

    // ------------------------------------------------------------------ o centro

    /**
     * O {@code onEntityCollidedWithBlock} do centro: puxa de leve para o meio; o item que pousa na lava entra na
     * fornalha, e quem é vivo e não aguenta fogo se queima.
     */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean past) {
        if (state.getValue(PART) != 0) return;
        Vec3 motion = entity.getDeltaMovement();
        double mx = motion.x, mz = motion.z;
        if (entity.getX() < pos.getX() + 0.3) mx += 1.0E-4f;
        if (entity.getX() > pos.getX() + 0.7) mx -= 1.0E-4f;
        if (entity.getZ() < pos.getZ() + 0.3) mz += 1.0E-4f;
        if (entity.getZ() > pos.getZ() + 0.7) mz -= 1.0E-4f;
        if (entity instanceof ItemEntity item) {
            entity.setDeltaMovement(mx, 0.025f, mz);
            if (!level.isClientSide() && item.onGround() && !item.isRemoved()
                    && level.getBlockEntity(pos) instanceof InfernalFurnaceBlockEntity furnace
                    && furnace.addItemsToInventory(item.getItem().copy())) {
                item.discard();
            }
        } else {
            entity.setDeltaMovement(mx, motion.y, mz);
            if (entity instanceof LivingEntity living && !living.fireImmune() && level instanceof ServerLevel server) {
                living.hurtServer(server, level.damageSources().lava(), 3.0f);
                living.igniteForSeconds(10.0f);
            }
        }
    }

    /** O {@code randomDisplayTick}: fumaça grossa saindo pelo alto aberto do centro. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(PART) != 0) return;
        BlockState above = level.getBlockState(pos.above());
        if (!above.isAir() || above.canOcclude()) return;
        for (int a = 0; a < 3; a++) {
            level.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + (double) random.nextFloat(), pos.getY() + 1.0 + random.nextFloat() * 0.5,
                    pos.getZ() + (double) random.nextFloat(), 0.0, 0.0, 0.0);
        }
    }

    // ------------------------------------------------------------------ desmontar

    /** O {@code restoreBlocks}: tudo o que é fornalha em volta do centro volta a ser o bloco que era. */
    private static void restoreBlocks(Level level, BlockPos centre) {
        for (BlockPos at : BlockPos.betweenClosed(centre.offset(-1, -1, -1), centre.offset(1, 1, 1))) {
            BlockState state = level.getBlockState(at);
            if (state.getBlock() instanceof InfernalFurnaceBlock) {
                level.setBlock(at, original(state.getValue(PART)).defaultBlockState(), 3);
            }
        }
    }

    /** O {@code idDropped}: o bloco que cada parte era. */
    public static Block original(int part) {
        if (part == 0) return Blocks.AIR;
        if (part == 10) return Blocks.IRON_BARS;
        return part % 2 != 0 && part != 5 ? Blocks.NETHER_BRICKS : Blocks.OBSIDIAN;
    }

    /**
     * O {@code onNeighborBlockChange} do centro: faltando um bloco da fornalha em volta, ela se desfaz. O laço do
     * original para no meio da fileira em que está o próprio centro (e a do meio de cima), e por isso não olha os
     * blocos ao sul deles; aqui ele é o mesmo.
     */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, @Nullable Orientation orientation, boolean moved) {
        if (!level.isClientSide() && state.getValue(PART) == 0) {
            for (int yy = -1; yy <= 1; yy++) {
                for (int xx = -1; xx <= 1; xx++) {
                    for (int zz = -1; zz <= 1 && (yy != 1 && yy != 0 || zz != 0 || xx != 0); zz++) {
                        if (!(level.getBlockState(pos.offset(xx, yy, zz)).getBlock() instanceof InfernalFurnaceBlock)) {
                            restoreBlocks(level, pos);
                            level.removeBlock(pos, false);
                            return;
                        }
                    }
                }
            }
        }
        super.neighborChanged(state, level, pos, neighbor, orientation, moved);
    }

    /** O {@code breakBlock}: quebrar o centro desfaz a fornalha; quebrar qualquer parte avisa o centro. */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        if (state.getValue(PART) == 0) restoreBlocks(level, pos);
        for (BlockPos at : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            level.neighborChanged(at.immutable(), this, null);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    /** O {@code onBlockDestroyedByPlayer}: do centro quebrado sai um blaze, forte e regenerando. */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (state.getValue(PART) == 0 && level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof InfernalFurnaceBlockEntity) {
            var blaze = EntityTypes.BLAZE.create(server, EntitySpawnReason.TRIGGERED);
            if (blaze != null) {
                blaze.snapTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0.0f, 0.0f);
                blaze.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 6000, 2));
                blaze.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 12000, 0));
                server.addFreshEntity(blaze);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /** O {@code getDrops}: o bloco que a parte era (a lava não deixa nada). */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        Block was = original(state.getValue(PART));
        return was == Blocks.AIR ? List.of() : List.of(new ItemStack(was));
    }

    /** O evento 1, de quando a varinha forma a fornalha: faíscas laranja em cada bloco. */
    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id == 1) {
            if (level.isClientSide()) clientEffects.sparkle(level, pos);
            return true;
        }
        BlockEntity te = level.getBlockEntity(pos);
        return te != null && te.triggerEvent(id, param);
    }

    /** As faíscas, do lado de quem vê. */
    public interface ClientEffects {
        void sparkle(Level level, BlockPos pos);
    }

    public static ClientEffects clientEffects = (level, pos) -> {
    };
}
