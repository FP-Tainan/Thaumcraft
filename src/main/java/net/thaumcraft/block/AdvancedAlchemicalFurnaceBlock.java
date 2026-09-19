package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.AdvancedAlchemicalFurnaceBlockEntity;
import net.thaumcraft.block.entity.AdvancedAlchemicalFurnaceNozzleBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A fornalha alquímica avançada: o {@code BlockAlchemyFurnace} da 4.2.3.5, o 3 × 3 × 2 que a varinha forma de uma
 * fornalha alquímica cercada de construções alquímicas avançadas, com alambiques e construções alquímicas em cima.
 * O {@link #PART} é o número do original: 0 a fornalha do meio, 1 os bicos (os lados de baixo), 4 os cantos de baixo,
 * 3 os lados de cima e 2 os cantos de cima. Nada disso se desenha sozinho: o modelo inteiro sai do meio.
 */
public class AdvancedAlchemicalFurnaceBlock extends BaseEntityBlock {
    public static final MapCodec<AdvancedAlchemicalFurnaceBlock> CODEC = simpleCodec(AdvancedAlchemicalFurnaceBlock::new);
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 4);
    /** A luz do meio, que acompanha o calor (o {@code getLightValue} do original). */
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 12);
    private static final VoxelShape LOW = Block.box(0, 0, 0, 16, 11.2, 16);

    public AdvancedAlchemicalFurnaceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PART, 0).setValue(LIGHT, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, LIGHT);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        int part = state.getValue(PART);
        if (part == 0) return new AdvancedAlchemicalFurnaceBlockEntity(pos, state);
        if (part == 1) return new AdvancedAlchemicalFurnaceNozzleBlockEntity(pos, state);
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        BlockEntityTicker<T> furnace = createTickerHelper(type, TCBlockEntities.ADVANCED_ALCHEMICAL_FURNACE, AdvancedAlchemicalFurnaceBlockEntity::tick);
        if (furnace != null) return furnace;
        return level.isClientSide() ? null
                : createTickerHelper(type, TCBlockEntities.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE, AdvancedAlchemicalFurnaceNozzleBlockEntity::serverTick);
    }

    /** O {@code addCollisionBoxesToList}: o meio tem 0,7 de altura para o que não é bicho (os itens caem nele). */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(PART) == 0 && !(context instanceof EntityCollisionContext entity && entity.getEntity() instanceof LivingEntity)) {
            return LOW;
        }
        return Shapes.block();
    }

    @Override
    protected VoxelShape getEntityInsideCollisionShape(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return Shapes.block();
    }

    /** O {@code onEntityCollidedWithBlock}: o item que cai no meio é desfeito, um por vez, com um borbulho. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean past) {
        if (level.isClientSide() || state.getValue(PART) != 0 || !(entity instanceof ItemEntity item) || item.isRemoved()) return;
        if (level.getBlockEntity(pos) instanceof AdvancedAlchemicalFurnaceBlockEntity tile && tile.process(item.getItem())) {
            ItemStack s = item.getItem().copy();
            s.shrink(1);
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), TCSounds.BUBBLE.value(), SoundSource.BLOCKS,
                    0.2f, 1.0f + level.getRandom().nextFloat() * 0.4f);
            if (s.isEmpty()) item.discard();
            else item.setItem(s);
        }
    }

    /** O {@code idDropped}/{@code damageDropped}: cada parte volta a ser a peça que era. */
    public static Block original(int part) {
        return switch (part) {
            case 0 -> TCBlocks.ALCHEMICAL_FURNACE;
            case 1, 4 -> TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT;
            case 3 -> TCBlocks.ALCHEMICAL_CONSTRUCT;
            default -> TCBlocks.ALEMBIC;
        };
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(new ItemStack(original(state.getValue(PART))));
    }

    /**
     * O {@code breakBlock}: tirando uma parte, o meio é avisado e desfaz tudo no tique seguinte; tirando o meio, as
     * partes voltam a ser as peças na hora.
     */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        if (state.getValue(PART) != 0) {
            for (BlockPos at : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                if (level.getBlockEntity(at) instanceof AdvancedAlchemicalFurnaceBlockEntity tile) {
                    tile.destroy = true;
                    break;
                }
            }
        } else {
            restoreAround(level, pos);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    /** As partes em volta do meio (e acima dele) voltam a ser as peças. */
    public static void restoreAround(Level level, BlockPos centre) {
        for (int a = -1; a <= 1; a++) {
            for (int b = 0; b <= 1; b++) {
                for (int c = -1; c <= 1; c++) {
                    if (a == 0 && b == 0 && c == 0) continue;
                    BlockPos at = centre.offset(a, b, c);
                    BlockState there = level.getBlockState(at);
                    if (there.getBlock() instanceof AdvancedAlchemicalFurnaceBlock) {
                        level.setBlock(at, original(there.getValue(PART)).defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    /**
     * O {@code getComparatorInputOverride} dos bicos. A conta do original, {@code floor(r * 14) + vis > 0 ? 1 : 0},
     * pela precedência dá só 0 ou 1 — sinal 1 quando há alguma essência — e aqui também.
     */
    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, net.minecraft.core.Direction direction) {
        if (level.getBlockEntity(pos) instanceof AdvancedAlchemicalFurnaceNozzleBlockEntity nozzle && nozzle.furnace() != null) {
            AdvancedAlchemicalFurnaceBlockEntity furnace = nozzle.furnace();
            float r = (float) furnace.vis / furnace.maxVis;
            return net.minecraft.util.Mth.floor(r * 14.0f) + furnace.vis > 0 ? 1 : 0;
        }
        return 0;
    }

    /** O {@code randomDisplayTick}: bolhas roxas no meio e nos tanques, e às vezes o estalo da lava. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (state.getValue(PART) != 0 || !(level.getBlockEntity(pos) instanceof AdvancedAlchemicalFurnaceBlockEntity tile) || tile.vis <= 0) return;
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        clientEffects.bubble(level, x + (double) rand.nextFloat(), y + 1.0, z + (double) rand.nextFloat(), 0.06f + rand.nextFloat() * 0.06f);
        if (rand.nextInt(50) == 0) {
            level.playLocalSound(x + (double) rand.nextFloat(), y + 1.0, z + (double) rand.nextFloat(), SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                    0.1f + rand.nextFloat() * 0.1f, 0.9f + rand.nextFloat() * 0.15f, false);
        }
        int q = rand.nextInt(2), w = rand.nextInt(2);
        clientEffects.bubble(level, x - 0.6 + rand.nextFloat() * 0.2 + q * 2, y + 2.0, z - 0.6 + rand.nextFloat() * 0.2 + w * 2,
                0.06f + rand.nextFloat() * 0.06f);
    }

    public interface ClientEffects {
        void bubble(Level level, double x, double y, double z, float size);
    }

    public static ClientEffects clientEffects = (level, x, y, z, size) -> {
    };
}
