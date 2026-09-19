package net.thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Os efeitos que as ferramentas mágicas pedem ao {@code ClientProxy}, desenhados por quem vê. */
public final class ToolFx {
    public interface Client {
        /** O {@code blockSparkle}. */
        void sparkle(BlockPos pos, int colour, int count);

        /** O {@code crucibleBubble}. */
        void bubble(Level level, double x, double y, double z, float r, float g, float b);

        /** O {@code startScan} da picareta elemental. */
        void oreScan(Level level, BlockPos pos);

        /** O {@code smokeSpiral}. */
        void smokeSpiral(Level level, double x, double y, double z, float radius, int start, int miny, int colour);
    }

    public static Client client;

    private ToolFx() {
    }
}
