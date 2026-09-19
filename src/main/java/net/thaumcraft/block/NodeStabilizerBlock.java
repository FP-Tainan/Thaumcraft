package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.block.entity.NodeStabilizerBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O estabilizador de nó, comum e avançado: os metadados 9 e 10 do {@code BlockStoneDevice} da 4.2.3.5. Posto
 * embaixo de um nó (e sem sinal de redstone), trava o nó: o comum dobra o tempo de refazer e impede que ele seja
 * drenado por outros nós; o avançado multiplica o tempo por vinte. Os dois seguram o instável e o esmaecido.
 * Desenhado por inteiro pelo {@link net.thaumcraft.client.render.NodeStabilizerRenderer}.
 */
public class NodeStabilizerBlock extends BaseEntityBlock {
    public static final MapCodec<NodeStabilizerBlock> CODEC = simpleCodec(properties -> new NodeStabilizerBlock(false, properties));
    private final boolean advanced;

    public NodeStabilizerBlock(boolean advanced, Properties properties) {
        super(properties);
        this.advanced = advanced;
    }

    /** O {@code lock} que dá ao nó de cima: 1 o comum, 2 o avançado. */
    public int lock() {
        return this.advanced ? 2 : 1;
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NodeStabilizerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? createTickerHelper(type, TCBlockEntities.NODE_STABILIZER, NodeStabilizerBlockEntity::tick) : null;
    }
}
