package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.block.BellowsBlock;
import net.thaumcraft.registry.TCBlockEntities;

import java.lang.reflect.Field;

/**
 * O {@code TileBellows} da 4.2.3.5: do lado de quem vê, o saco enche devagar e esvazia depressa, com um sopro
 * baixinho a cada volta; do lado do servidor, num forno comum, empurra o cozimento um tique a cada dois.
 */
public class BellowsBlockEntity extends BlockEntity {
    /** Quão cheio está o saco: de 0,35 a 1. */
    public float inflation = 1.0f;
    private boolean direction;
    private boolean firstRun = true;
    private int delay;

    /** O {@code cookingTimer} do forno comum, que o jogo guarda só para ele. */
    private static final Field COOKING_TIMER;

    static {
        Field found = null;
        try {
            found = AbstractFurnaceBlockEntity.class.getDeclaredField("cookingTimer");
            found.setAccessible(true);
        } catch (ReflectiveOperationException ignored) {
            // sem o campo, o fole só não ajuda o forno comum
        }
        COOKING_TIMER = found;
    }

    public BellowsBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.BELLOWS, pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BellowsBlockEntity bellows) {
        if (level.hasNeighborSignal(pos)) return;
        if (bellows.firstRun) bellows.inflation = 0.35f + level.getRandom().nextFloat() * 0.55f;
        bellows.firstRun = false;
        if (bellows.inflation > 0.35f && !bellows.direction) bellows.inflation -= 0.075f;
        if (bellows.inflation <= 0.35f && !bellows.direction) bellows.direction = true;
        if (bellows.inflation < 1.0f && bellows.direction) bellows.inflation += 0.025f;
        if (bellows.inflation >= 1.0f && bellows.direction) {
            bellows.direction = false;
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.GHAST_SHOOT,
                    SoundSource.BLOCKS, 0.01f, 0.5f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2f, false);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BellowsBlockEntity bellows) {
        if (COOKING_TIMER == null || level.hasNeighborSignal(pos)) return;
        if (++bellows.delay < 2) return;
        bellows.delay = 0;
        BlockPos target = pos.relative(state.getValue(BellowsBlock.FACING));
        if (!(level.getBlockEntity(target) instanceof FurnaceBlockEntity furnace)) return;
        try {
            int cooking = COOKING_TIMER.getInt(furnace);
            if (cooking > 0 && cooking < 199) COOKING_TIMER.setInt(furnace, cooking + 1);
        } catch (IllegalAccessException ignored) {
            // o campo foi aberto no começo; se não abriu, o fole só não ajuda o forno comum
        }
    }
}
