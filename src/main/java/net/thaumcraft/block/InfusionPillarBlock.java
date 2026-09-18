package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.InfusionPillarBlockEntity;
import net.thaumcraft.registry.TCBlocks;

/**
 * O pilar do altar de infusão: os tipos três (a base, com o {@code TileInfusionPillar}) e quatro (o topo) do
 * {@code BlockStoneDevice} da 4.2.3.5.
 *
 * <p>Nasce quando a varinha acorda o altar: os tijolos de pedra arcana dos cantos viram a base e a pedra arcana
 * em cima vira o topo, e quem desenha os dois é o modelo do pilar. As metades dependem uma da outra — tirando
 * uma, a outra cai também —, e cada uma devolve o que era: a base, os tijolos; o topo, a pedra arcana.
 */
public class InfusionPillarBlock extends BaseEntityBlock {
    public static final MapCodec<InfusionPillarBlock> CODEC = simpleCodec(properties -> new InfusionPillarBlock(false, properties));
    public static final MapCodec<InfusionPillarBlock> TOP_CODEC = simpleCodec(properties -> new InfusionPillarBlock(true, properties));
    /** A caixa de mira da base: meio bloco, como o {@code setBlockBoundsBasedOnState} do original. */
    private static final VoxelShape BASE_OUTLINE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);

    private final boolean top;

    public InfusionPillarBlock(boolean top, Properties properties) {
        super(properties);
        this.top = top;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return this.top ? TOP_CODEC : CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // o modelo do pilar, dois blocos de altura, é desenhado pela base
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.top ? Shapes.block() : BASE_OUTLINE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        // a base precisa do topo em cima, e o topo da base embaixo; sem a outra metade, esta cai
        if (!this.top && direction == Direction.UP && !neighbor.is(TCBlocks.INFUSION_PILLAR_TOP)) {
            ticks.scheduleTick(pos, this, 1);
        }
        if (this.top && direction == Direction.DOWN && !neighbor.is(TCBlocks.INFUSION_PILLAR)) {
            ticks.scheduleTick(pos, this, 1);
        }
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos other = this.top ? pos.below() : pos.above();
        Block partner = this.top ? TCBlocks.INFUSION_PILLAR : TCBlocks.INFUSION_PILLAR_TOP;
        if (!level.getBlockState(other).is(partner)) level.destroyBlock(pos, true);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return this.top ? null : new InfusionPillarBlockEntity(pos, state);
    }
}
