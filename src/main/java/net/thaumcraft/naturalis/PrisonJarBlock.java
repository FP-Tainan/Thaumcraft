package net.thaumcraft.naturalis;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * O Bicho num Jarro: o {@code PrisonJarBlock} do Magia Naturalis 0.5.0. Um jarro de vidro que guarda uma criatura
 * viva inteira — ela sai de lá do mesmo jeito que entrou, e vem junto quando o jarro é quebrado.
 */
public class PrisonJarBlock extends BaseEntityBlock {
    public static final MapCodec<PrisonJarBlock> CODEC = simpleCodec(PrisonJarBlock::new);
    /** O feitio do jarro do Thaumcraft: doze de largura por catorze de altura. */
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0);

    public PrisonJarBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PrisonJarBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** As faíscas douradas do lado de quem vê, que o cliente pendura aqui ao abrir. */
    public interface ClientEffects {
        void sparkle(Level level, BlockPos pos);
    }

    public static ClientEffects clientEffects = (level, pos) -> {
    };

    /** O {@code randomDisplayTick} do original: uma vez em quatro, faíscas douradas em volta do vidro. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (random.nextInt(4) == 0) clientEffects.sparkle(level, pos);
    }

    /** O que estava guardado no item vai para o jarro posto. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level.getBlockEntity(pos) instanceof PrisonJarBlockEntity jar)) return;
        var guardado = stack.get(net.thaumcraft.registry.TCComponents.JARRED_MOB);
        if (guardado != null) jar.setStored(guardado);
    }

    /** E o que estava no jarro volta para o item quando ele se quebra. */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.player.Player player) {
        if (level.getBlockEntity(pos) instanceof PrisonJarBlockEntity jar && jar.hasStored() && !level.isClientSide()) {
            ItemStack drop = new ItemStack(this);
            drop.set(net.thaumcraft.registry.TCComponents.JARRED_MOB, jar.stored());
            net.minecraft.world.level.block.Block.popResource(level, pos, drop);
            jar.setStored(null);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }
}
