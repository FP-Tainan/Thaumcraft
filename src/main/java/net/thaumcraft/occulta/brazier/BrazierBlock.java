package net.thaumcraft.occulta.brazier;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.occulta.OccultaItems;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * O Braseiro: a {@code BlockBrazier} do Witchery.
 *
 * <p>Um cesto de ferro sobre três pés. Põem-se nele <b>três coisas</b>, acende-se, e o que ele faz é o que
 * acontece <b>em volta</b> enquanto o fogo dura — não sai dele nada senão cinza.
 *
 * <p>Acende-se com <b>isqueiro</b> ou com um sinal de <b>redstone</b>; apaga-se com um <b>balde de água</b>, que
 * volta vazio, ou com um <b>frasco de vidro</b>. Quebrado aceso, larga cinza e mais nada — o que estava dentro
 * ardeu.
 */
public class BrazierBlock extends BaseEntityBlock {
    public static final MapCodec<BrazierBlock> CODEC = simpleCodec(BrazierBlock::new);

    private static final VoxelShape SHAPE = Block.box(3.2, 0.0, 3.2, 12.8, 15.2, 12.8);

    public BrazierBlock(Properties properties) {
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
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrazierBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, net.thaumcraft.occulta.OccultaBlocks.BRAZIER_ENTITY,
                (mundo, onde, feitio, alma) -> alma.tick());
    }

    /** O clique: a água apaga, o isqueiro acende, e o resto entra para queimar. */
    @Override
    protected InteractionResult useItemOn(ItemStack naMão, BlockState state, Level level, BlockPos pos,
                                          Player quem, InteractionHand mão, BlockHitResult hit) {
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        if (!(server.getBlockEntity(pos) instanceof BrazierBlockEntity braseiro)) return InteractionResult.PASS;
        if (naMão.isEmpty()) return InteractionResult.PASS;

        if (naMão.is(Items.WATER_BUCKET) || naMão.is(Items.GLASS_BOTTLE)) {
            if (braseiro.empty() && !braseiro.burning()) return InteractionResult.PASS;
            braseiro.douse();
            if (!quem.hasInfiniteMaterials() && naMão.is(Items.WATER_BUCKET)) {
                quem.setItemInHand(mão, new ItemStack(Items.BUCKET));
            }
            server.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        if (naMão.is(Items.FLINT_AND_STEEL)) {
            braseiro.light();
            server.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        if (braseiro.burning() || !braseiro.add(naMão)) return InteractionResult.PASS;
        if (!quem.hasInfiniteMaterials()) naMão.shrink(1);
        return InteractionResult.SUCCESS;
    }

    /** Um sinal de redstone acende-o, como no original. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block vizinho,
                                   @Nullable Orientation orientation, boolean moved) {
        if (!(level instanceof ServerLevel server)) return;
        if (!(server.getBlockEntity(pos) instanceof BrazierBlockEntity braseiro)) return;
        boolean ligado = server.hasNeighborSignal(pos);
        if (ligado && !braseiro.lastRedstone()) braseiro.light();
        braseiro.setLastRedstone(ligado);
    }

    /** A chama que sobe dele quando arde. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof BrazierBlockEntity braseiro) || !braseiro.burning()) return;
        level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.4 + random.nextInt(3) * 0.1,
                pos.getY() + 1.1 + random.nextInt(2) * 0.1, pos.getZ() + 0.4 + random.nextInt(3) * 0.1,
                0.0, 0.0, 0.0);
    }

    /** Quebrado aceso, larga cinza; apagado, larga o que tinha dentro. */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moving) {
        if (level.getBlockEntity(pos) instanceof BrazierBlockEntity braseiro) {
            if (braseiro.burning()) {
                Block.popResource(level, pos, new ItemStack(OccultaItems.WOOD_ASH));
            } else {
                net.minecraft.world.Containers.dropContents(level, pos, braseiro);
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moving);
    }
}
