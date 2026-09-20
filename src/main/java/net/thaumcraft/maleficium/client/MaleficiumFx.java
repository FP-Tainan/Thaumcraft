package net.thaumcraft.maleficium.client;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.thaumcraft.client.fx.Sparkle;
import net.thaumcraft.client.fx.Wisp;
import net.thaumcraft.maleficium.LumosBlock;
import net.thaumcraft.maleficium.WarpwoodKnotBlock;

/**
 * Os efeitos do Maleficium do lado de quem vê: os fogos-fátuos do nó que se quebra e as faíscas do Lumos.
 */
public final class MaleficiumFx {
    private MaleficiumFx() {
    }

    public static void init() {
        WarpwoodKnotBlock.clientEffects = MaleficiumFx::knotBurst;
        LumosBlock.clientEffects = MaleficiumFx::lumos;
        net.thaumcraft.maleficium.WarpFertilizerItem.clientEffects = MaleficiumFx::twist;
    }

    /** O {@code addDestroyEffects} do nó: quinze fogos-fátuos saindo dele devagar. */
    private static void knotBurst(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < 15; i++) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - random.nextDouble()) * 0.5;
            double y = pos.getY() + 0.5 + (random.nextDouble() - random.nextDouble()) * 0.5;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - random.nextDouble()) * 0.5;
            Wisp.fx3(x, y, z, x + (x - pos.getX() - 0.5) * 0.5, y, z + (z - pos.getZ() - 0.5) * 0.5,
                    0.25f + random.nextFloat() * 0.25f, 5, false, 0.01f);
        }
    }

    /** O {@code particles} do adubo: fogos-fátuos saindo da muda que se torce. */
    private static void twist(Level level, BlockPos pos, int count) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < count; i++) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - random.nextDouble()) * 0.5;
            double y = pos.getY() + 0.5 + (random.nextDouble() - random.nextDouble()) * 0.5;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - random.nextDouble()) * 0.5;
            Wisp.fx3(x, y, z, x + (x - pos.getX() - 0.5) * 0.5, y, z + (z - pos.getZ() - 0.5) * 0.5,
                    0.25f + random.nextFloat() * 0.25f, 5, false, 0.01f);
        }
    }

    /** As faíscas do Lumos: uma de vez em quando subindo, e nove de uma vez quando ele se quebra. */
    private static void lumos(Level level, BlockPos pos, RandomSource random, boolean breaking) {
        if (breaking) {
            Sparkle.custom(random, pos.getX() + random.nextFloat(), pos.getY() + random.nextFloat(),
                    pos.getZ() + random.nextFloat(), 1.75f, 6, 3 + random.nextInt(3), 0.5f,
                    random.nextGaussian() * 0.1, random.nextGaussian() * 0.1, random.nextGaussian() * 0.1);
            return;
        }
        Sparkle.custom(random, pos.getX() + 0.5 + random.nextGaussian() * 0.1,
                pos.getY() + 0.5 + random.nextGaussian() * 0.1, pos.getZ() + 0.5 + random.nextGaussian() * 0.1,
                1.75f, 6, 3 + random.nextInt(2), -0.5f, 0.0, 0.0, 0.0);
    }
}
