package net.thaumcraft.occulta.kettle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * O Caldeirão de Pote: a {@code BlockKettle} do Witchery.
 *
 * <p>Um pote de ferro pendurado em correntes, com lume por baixo. É nele que se fazem os <b>cozimentos de pote</b>
 * — os que saem em frasco e se atiram, e não os que se misturam por ordem no Caldeirão da Bruxa.
 *
 * <p>O que se faz com ele: enche-se de água com um balde, acende-se lume debaixo dele, <b>atira-se</b> o que
 * entra lá para dentro (seis coisas), atiram-se frascos de vidro, e ao ficar pronto se chega com um frasco na mão.
 */
public class KettleBlock extends BaseEntityBlock {
    public static final MapCodec<KettleBlock> CODEC = simpleCodec(KettleBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** O pote, que é quase o bloco todo menos uma casa de cada lado. */
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);

    public KettleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KettleBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, net.thaumcraft.occulta.OccultaBlocks.WITCHES_KETTLE_ENTITY,
                (mundo, onde, feitio, alma) -> alma.tick());
    }

    /**
     * O clique: o balde enche e esvazia, e o frasco de vidro tira o que ficou pronto.
     *
     * <p><b>Do original ficam de fora, declarados</b>, os dois acréscimos de quem tem o chapéu de bruxa ou um
     * familiar de cozimento: um e outro dão frasco a mais, e nenhum dos dois está portado.
     */
    @Override
    protected InteractionResult useItemOn(ItemStack naMão, BlockState state, Level level, BlockPos pos,
                                          Player quem, net.minecraft.world.InteractionHand mão,
                                          BlockHitResult hit) {
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        if (!(server.getBlockEntity(pos) instanceof KettleBlockEntity pote)) return InteractionResult.PASS;

        if (naMão.is(Items.WATER_BUCKET) && pote.fill()) {
            if (!quem.hasInfiniteMaterials()) {
                quem.setItemInHand(mão, ItemStack.EMPTY);
                give(quem, new ItemStack(Items.BUCKET));
            }
            server.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        if (naMão.is(Items.BUCKET) && pote.filled() && !pote.ready() && pote.empty()) {
            if (!quem.hasInfiniteMaterials()) {
                naMão.shrink(1);
                give(quem, new ItemStack(Items.WATER_BUCKET));
            }
            server.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        if (naMão.is(Items.GLASS_BOTTLE) && pote.ready()) {
            ItemStack saída = pote.takeWithBottle(quem);
            if (saída.isEmpty()) return InteractionResult.PASS;
            if (!quem.hasInfiniteMaterials()) naMão.shrink(1);
            give(quem, saída);
            server.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static void give(Player quem, ItemStack coisa) {
        if (!quem.getInventory().add(coisa)) quem.drop(coisa, false);
    }

    /** O que se larga em cima do pote cai dentro dele: o {@code onEntityCollidedWithBlock} do original. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity quem,
                                InsideBlockEffectApplier applier, boolean dentroMesmo) {
        if (!(level instanceof ServerLevel server) || !(quem instanceof ItemEntity largado)) return;
        if (!(server.getBlockEntity(pos) instanceof KettleBlockEntity pote)) return;
        ItemStack coisa = largado.getItem();
        if (coisa.isEmpty() || !pote.throwIn(coisa)) return;

        if (coisa.is(Items.GLASS_BOTTLE)) {
            largado.discard();
        } else {
            coisa.shrink(1);
            if (coisa.isEmpty()) largado.discard();
        }
        server.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.4f, 1.0f);
        server.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH, pos.getX() + 0.5,
                pos.getY() + 0.8, pos.getZ() + 0.5, 8, 0.3, 0.1, 0.3, 0.0);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moving) {
        if (level.getBlockEntity(pos) instanceof KettleBlockEntity pote) {
            net.minecraft.world.Containers.dropContents(level, pos, pote);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moving);
    }
}
