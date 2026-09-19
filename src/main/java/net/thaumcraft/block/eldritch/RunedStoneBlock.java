package net.thaumcraft.block.eldritch;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.block.entity.eldritch.RunedStoneBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * A pedra rúnica: o número 10 do {@code BlockEldritch} da 4.2.3.5, a armadilha dos corredores do labirinto. Cada face
 * leva uma das quatro runas ({@code es_5} a {@code es_8}); runas azuis sobem do ar em volta; e quem passar a três blocos
 * leva um choque (o {@code TileEldritchTrap}). Dureza quinze; quebrada, de um a quatro de experiência.
 */
public class RunedStoneBlock extends BaseEntityBlock {
    public static final MapCodec<RunedStoneBlock> CODEC = simpleCodec(RunedStoneBlock::new);

    /** As runas que sobem do ar em volta, do lado de quem vê. */
    public static ClientEffects clientEffects = (level, x, y, z, random) -> {
    };

    public interface ClientEffects {
        void runes(Level level, double x, double y, double z, RandomSource random);
    }

    public RunedStoneBlock(Properties properties) {
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
        return new RunedStoneBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, TCBlockEntities.RUNED_STONE, RunedStoneBlockEntity::tick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
        int x = pos.getX() + r.nextInt(2) - r.nextInt(2);
        int y = pos.getY() + r.nextInt(2) - r.nextInt(2);
        int z = pos.getZ() + r.nextInt(2) - r.nextInt(2);
        if (level.isEmptyBlock(new BlockPos(x, y, z))) clientEffects.runes(level, x + r.nextFloat(), y + r.nextFloat(), z + r.nextFloat(), r);
    }

    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, tool, dropExperience);
        if (dropExperience) this.tryDropExperience(level, pos, tool, UniformInt.of(1, 4));
    }
}
