package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * O Caldeirão da Bruxa: o {@code BlockCauldron} do Witchery.
 *
 * <p>Enche-se de água, acende-se fogo embaixo e espera-se ferver. Ferve em cinco segundos, e daí em diante o que
 * se jogar dentro entra na panela — e, quando a coisa certa cai por último, sai dela o que a receita manda
 * (ver o {@link OccultaRituals}).
 *
 * <p>Ele não se fabrica: faz-se <b>untando um caldeirão comum com Pasta de Unção</b>, como no original.
 *
 * <p><b>Desvio declarado, a pedido de quem manda:</b> o feitio não é o do Witchery. Como agora tudo é Thaumcraft,
 * o caldeirão é o <b>crisol do mod</b> com quatro molhos de ervas amarrados por fora — a unção não troca a panela,
 * enfeita-a. O modelo de Techne do original saiu, e fica no histórico.
 */
public class WitchesCauldronBlock extends BaseEntityBlock {
    public static final MapCodec<WitchesCauldronBlock> CODEC = simpleCodec(WitchesCauldronBlock::new);

    /** O tamanho dele é o do crisol: fundo cheio e as quatro paredes, com o meio vazio. */
    private static final VoxelShape SHAPE = net.minecraft.world.phys.shapes.Shapes.or(
            Block.box(0, 0, 0, 16, 5, 16),
            Block.box(0, 0, 0, 2, 13.6, 16), Block.box(0, 0, 0, 16, 13.6, 2),
            Block.box(14, 0, 0, 16, 13.6, 16), Block.box(0, 0, 14, 16, 13.6, 16));

    public WitchesCauldronBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WitchesCauldronBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, OccultaBlocks.WITCHES_CAULDRON_ENTITY, WitchesCauldronBlockEntity::tick);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** O balde de água enche o caldeirão; o balde vazio leva de volta o que estiver dentro. */
    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof WitchesCauldronBlockEntity caldeirão)) {
            return InteractionResult.PASS;
        }
        if (held.is(Items.WATER_BUCKET)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            if (!caldeirão.fill(WitchesCauldronBlockEntity.BUCKET)) return InteractionResult.CONSUME;
            if (!player.hasInfiniteMaterials()) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        if (held.is(Items.BUCKET) && caldeirão.water() >= WitchesCauldronBlockEntity.BUCKET) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            caldeirão.empty();
            if (!player.hasInfiniteMaterials()) player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        // a garrafa de vidro tira o cozimento do caldeirão
        if (held.is(Items.GLASS_BOTTLE)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            ItemStack frasco = caldeirão.bottle((net.minecraft.server.level.ServerLevel) level, pos);
            if (frasco.isEmpty()) return InteractionResult.CONSUME;
            if (!player.hasInfiniteMaterials()) held.shrink(1);
            if (!player.getInventory().add(frasco)) player.drop(frasco, false);
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /** O {@code onEntityCollidedWithBlock}: o que cai dentro fervendo entra na panela. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effects, boolean flag) {
        if (level.isClientSide() || !(entity instanceof ItemEntity caído)) return;
        if (!(level.getBlockEntity(pos) instanceof WitchesCauldronBlockEntity caldeirão)) return;
        if (!caldeirão.isBoiling()) return;
        if (caldeirão.addItem(caído.getItem())) {
            caído.discard();
            ((ServerLevel) level).sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.9,
                    pos.getZ() + 0.5, 8, 0.2, 0.1, 0.2, 0.1);
            level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.4f, 1.2f);
        }
    }

    /** Quebrado, ele devolve o que estava dentro dele. */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moving) {
        if (level.getBlockEntity(pos) instanceof WitchesCauldronBlockEntity caldeirão) caldeirão.spill(level, pos);
    }

    /** As bolhas da fervura, na cor do que está dentro. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof WitchesCauldronBlockEntity caldeirão)) return;
        if (!caldeirão.isBoiling()) return;
        double altura = pos.getY() + 0.2 + caldeirão.filled() * 0.5;
        for (int volta = 0; volta < 2; volta++) {
            level.addParticle(ParticleTypes.BUBBLE_POP,
                    pos.getX() + 0.2 + random.nextDouble() * 0.6, altura,
                    pos.getZ() + 0.2 + random.nextDouble() * 0.6, 0.0, 0.0, 0.0);
        }
        if (random.nextInt(5) == 0) {
            level.playLocalSound(pos.getX() + 0.5, altura, pos.getZ() + 0.5, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT,
                    SoundSource.BLOCKS, 0.4f, 0.8f + random.nextFloat() * 0.2f, false);
        }
    }
}
