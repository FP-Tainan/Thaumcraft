package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.ArcanePressurePlateBlockEntity;
import net.thaumcraft.item.KeyItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A placa de pressão arcana: os tipos 2 e 3 do {@code BlockWoodenDevice} da 4.2.3.5. Uma placa com dono que dispara com
 * tudo (0), com tudo menos o dono e quem tem chave (1), ou só com eles (2) — o dono troca com a mão. Ligada, dá sinal
 * forte por cima e por baixo, e abre a porta arcana ao lado; explosão e chefão não a derrubam.
 */
public class ArcanePressurePlateBlock extends BaseEntityBlock {
    public static final MapCodec<ArcanePressurePlateBlock> CODEC = simpleCodec(ArcanePressurePlateBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    /** O que dispara a placa (e a cara dela: applate1, 2 e 3). */
    public static final net.minecraft.world.level.block.state.properties.IntegerProperty SETTING =
            net.minecraft.world.level.block.state.properties.IntegerProperty.create("setting", 0, 2);
    private static final VoxelShape UP = Block.box(1.0, 0.0, 1.0, 15.0, 1.0, 15.0);
    private static final VoxelShape DOWN = Block.box(1.0, 0.0, 1.0, 15.0, 0.5, 15.0);

    public ArcanePressurePlateBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false).setValue(SETTING, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, SETTING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcanePressurePlateBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(POWERED) ? DOWN : UP;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player && level.getBlockEntity(pos) instanceof ArcanePressurePlateBlockEntity plate) {
            plate.owner = player.getName().getString();
            plate.setChanged();
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        if (stack.getItem() instanceof KeyItem key) return key.useOnWarded(stack, level, pos, player);
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    /** O dono (ou quem tem a chave de ouro) troca o que dispara a placa. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof ArcanePressurePlateBlockEntity plate && plate.mayManage(player)) {
            int setting = (state.getValue(SETTING) + 1) % 3;
            level.setBlock(pos, state.setValue(SETTING, setting), 3);
            player.sendSystemMessage(Component.translatable("tc.plate.setting." + setting));
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.1f, 0.9f);
            plate.setChanged();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        if (level instanceof ServerLevel server && !state.getValue(POWERED)) this.check(server, pos, state);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) this.check(level, pos, state);
    }

    /** O {@code setStateIfMobInteractsWithPlate}. */
    private void check(ServerLevel level, BlockPos pos, BlockState state) {
        if (!(level.getBlockEntity(pos) instanceof ArcanePressurePlateBlockEntity plate)) return;
        double f = 0.125;
        AABB box = new AABB(pos.getX() + f, pos.getY(), pos.getZ() + f, pos.getX() + 1 - f, pos.getY() + 0.25, pos.getZ() + 1 - f);
        int setting = state.getValue(SETTING);
        List<? extends Entity> near = setting == 2 ? level.getEntitiesOfClass(Player.class, box) : level.getEntities((Entity) null, box);
        boolean hit = false;
        for (Entity entity : near) {
            if (entity.isIgnoringBlockTriggers()) continue;
            if (setting == 1 && entity instanceof Player player && plate.mayUse(player)) continue;
            if (setting == 2 && !(entity instanceof Player player && plate.mayUse(player))) continue;
            hit = true;
            break;
        }
        boolean was = state.getValue(POWERED);
        if (hit != was) {
            level.setBlock(pos, state.setValue(POWERED, hit), 2);
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.below(), this);
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS,
                    0.2f, hit ? 0.6f : 0.5f);
        }
        if (hit) level.scheduleTick(pos, this, 20);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) && direction == Direction.UP ? 15 : 0;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        if (state.getValue(POWERED)) {
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.below(), this);
        }
    }

    @Override
    protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.world.level.Explosion explosion,
                                  java.util.function.BiConsumer<ItemStack, BlockPos> drops) {
        // o onBlockExploded: explosão não a derruba
    }
}
