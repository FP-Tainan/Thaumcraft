package net.thaumcraft.maleficium.client;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.thaumcraft.client.fx.LightningBolt;
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

    /** Os efeitos das lâminas de fortaleza, que a onda de choque do foco também usa. */
    private static final BladeEffects LAMINAS = new BladeEffects();

    public static void init() {
        WarpwoodKnotBlock.clientEffects = MaleficiumFx::knotBurst;
        LumosBlock.clientEffects = MaleficiumFx::lumos;
        net.thaumcraft.maleficium.WarpFertilizerItem.clientEffects = MaleficiumFx::twist;
        net.thaumcraft.maleficium.GateKeyItem.clientEffects = MaleficiumFx::gateSparkle;
        net.thaumcraft.maleficium.FortressBladeItem.clientEffects = LAMINAS;
        MaleficiumHud.init();
        net.thaumcraft.item.Focuses.registerClient("shockwave", MaleficiumFx::shockwaveFx);
        net.thaumcraft.item.Focuses.registerClient("vis_shard", MaleficiumFx::visShardFx);
        net.thaumcraft.item.Focuses.registerClient("lumos", MaleficiumFx::lumosFx);
    }

    /** A onda de choque: um raio do peito de quem lançou até cada um que ela pega, e faíscas em volta deles. */
    private static boolean shockwaveFx(Level level, net.minecraft.world.entity.player.Player player,
                                       net.minecraft.world.item.ItemStack wand, net.thaumcraft.item.FocusItem focus) {
        double alcance = 15.0 + net.thaumcraft.item.FocusItem.level(
                net.thaumcraft.item.WandItem.focusStack(wand), net.thaumcraft.item.FocusUpgradeTable.ENLARGE);
        for (var alvo : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                player.getBoundingBox().inflate(alcance),
                e -> e != player && e.isAlive() && !e.isInvulnerable())) {
            LAMINAS.shockwave(level, player, alvo);
        }
        return true;
    }

    /** A lasca de vis: dezoito faíscas de onde ela sai. */
    private static boolean visShardFx(Level level, net.minecraft.world.entity.player.Player player,
                                      net.minecraft.world.item.ItemStack wand, net.thaumcraft.item.FocusItem focus) {
        if (!(net.thaumcraft.item.Focuses.pointedEntity(level, player, 32.0)
                instanceof net.minecraft.world.entity.LivingEntity)) {
            return false;
        }
        RandomSource random = level.getRandom();
        var olhar = player.getLookAngle();
        double x = player.getX() + olhar.x / 2.0;
        double y = player.getEyeY() + olhar.y / 2.0;
        double z = player.getZ() + olhar.z / 2.0;
        for (int a = 0; a < 18; a++) {
            Sparkle.custom(random, x + random.nextFloat(), y + random.nextFloat(), z + random.nextFloat(),
                    1.75f, 0, 3 + random.nextInt(3), 0.1f, 0.0, 0.0, 0.0);
        }
        return true;
    }

    /** O Lumos: nove faíscas no lugar em que a luz acendeu. */
    private static boolean lumosFx(Level level, net.minecraft.world.entity.player.Player player,
                                   net.minecraft.world.item.ItemStack wand, net.thaumcraft.item.FocusItem focus) {
        var mira = net.thaumcraft.item.Focuses.targetBlock(level, player);
        if (!(mira instanceof net.minecraft.world.phys.BlockHitResult hit)
                || hit.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) {
            return false;
        }
        BlockPos pos = hit.getBlockPos();
        if (!level.getBlockState(pos).canBeReplaced()) pos = pos.relative(hit.getDirection());
        RandomSource random = level.getRandom();
        for (int a = 0; a < 9; a++) {
            Sparkle.custom(random, pos.getX() + random.nextFloat(), pos.getY() + random.nextFloat(),
                    pos.getZ() + random.nextFloat(), 1.75f, 6, 3 + random.nextInt(3), 0.1f, 0.0, 0.0, 0.0);
        }
        return true;
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

    /** O {@code sparkle} da chave do portão: faíscas subindo do lugar a que ela se prendeu. */
    private static void gateSparkle(Level level, double x, double y, double z) {
        RandomSource random = level.getRandom();
        Sparkle.custom(random, x + 0.33f * random.nextGaussian(), y + 0.5 + random.nextFloat(),
                z + 0.33f * random.nextGaussian(), 1.75f, 6, 3 + random.nextInt(3), 0.1f, 0.0, 0.0, 0.0);
    }

    /**
     * Os dois efeitos das lâminas de fortaleza inscritas: a cura da Deusa Benevolente, com fogos-fátuos e
     * faíscas em volta de quem se curou, e o raio do Espírito Vingativo em cada um que a onda de choque pega.
     */
    static class BladeEffects implements net.thaumcraft.maleficium.FortressBladeItem.Effects {
        @Override
        public void heal(Level level, net.minecraft.world.entity.player.Player player) {
            RandomSource random = level.getRandom();
            for (int a = 0; a < 18; a++) {
                double x = player.getX() + random.nextGaussian() * 0.25;
                double y = player.getBoundingBox().minY + 1.0 + random.nextGaussian() * 0.5;
                double z = player.getZ() + random.nextGaussian() * 0.25;
                Wisp.fx2(x, y, z, 0.25f + random.nextFloat() * 0.25f, 3, true, 0.02f);
                Sparkle.spawn(random, x, y, z, 1.0f, 5, 0.0f);
            }
        }

        @Override
        public void shockwave(Level level, net.minecraft.world.entity.player.Player player,
                              net.minecraft.world.entity.Entity target) {
            RandomSource random = level.getRandom();
            for (int a = 0; a < 5; a++) {
                Sparkle.spawn(random, target.getX() + (random.nextFloat() - random.nextFloat()) * 0.6f,
                        target.getY() + (random.nextFloat() - random.nextFloat()) * 0.6f,
                        target.getZ() + (random.nextFloat() - random.nextFloat()) * 0.6f,
                        2.0f + random.nextFloat(), 2, 0.05f + random.nextFloat() * 0.05f);
            }
            LightningBolt bolt = new LightningBolt(player.getX(), player.getEyeY(), player.getZ(),
                    target.getX(), target.getBoundingBox().minY + target.getBbHeight() / 2.0f, target.getZ(),
                    random.nextLong(), 4, 0.5f, 8);
            bolt.defaultFractal();
            bolt.setType(2);
            bolt.setWidth(0.125f);
            bolt.finalizeBolt();
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
