package net.thaumcraft.block.eldritch;

import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.registry.TCBlocks;

/** As peças que eram o {@code BlockEldritch} do 1.7, todas de um bloco só: o portal só se sustenta entre duas delas. */
public final class MazeStones {
    private MazeStones() {
    }

    public static boolean isEldritch(BlockState state) {
        return state.is(TCBlocks.ELDRITCH_ALTAR) || state.is(TCBlocks.ELDRITCH_OBELISK) || state.is(TCBlocks.ELDRITCH_OBELISK_UPPER)
                || state.is(TCBlocks.ELDRITCH_CAPSTONE) || state.is(TCBlocks.GLOWING_CRUSTED_STONE) || state.is(TCBlocks.GLYPHED_STONE)
                || state.is(TCBlocks.ELDRITCH_DECO) || state.is(TCBlocks.ANCIENT_DOORWAY) || state.is(TCBlocks.ANCIENT_LOCK)
                || state.is(TCBlocks.CRUSTED_OPENING) || state.is(TCBlocks.RUNED_STONE);
    }
}
