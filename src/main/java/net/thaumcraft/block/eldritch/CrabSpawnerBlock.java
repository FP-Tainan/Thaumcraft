package net.thaumcraft.block.eldritch;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.block.entity.eldritch.CrabSpawnerBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * A abertura incrustada: o número 9 do {@code BlockEldritch} da 4.2.3.5 — a pedra incrustada com o respiradouro
 * ({@code crabvent.obj}) na face de onde saem os caranguejos. Dureza quinze; quebrada, não deixa nada além de seis a dez
 * de experiência.
 */
public class CrabSpawnerBlock extends BaseEntityBlock {
    public static final MapCodec<CrabSpawnerBlock> CODEC = simpleCodec(CrabSpawnerBlock::new);

    public CrabSpawnerBlock(Properties properties) {
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrabSpawnerBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.CRAB_SPAWNER, CrabSpawnerBlockEntity::tick);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        return level.getBlockEntity(pos) instanceof CrabSpawnerBlockEntity te && te.triggerEvent(id, param);
    }

    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, tool, dropExperience);
        if (dropExperience) this.tryDropExperience(level, pos, tool, UniformInt.of(6, 10));
    }
}
