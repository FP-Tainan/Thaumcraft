package net.thaumcraft.maleficium.client;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.thaumcraft.maleficium.SalisItem;
import net.thaumcraft.client.fx.Sparkle;

/**
 * As faíscas do sal: o {@code spawnParticles} do {@code ItemSalis} da 8.1.1.
 *
 * <p>Uma faísca por tique em volta do item, caindo para cima (gravidade negativa); na hora em que o sal se gasta,
 * duzentas de uma vez, empurradas para fora e demorando dez vezes mais a sumir. O Tempestas usa a faísca de número
 * sete, o Aevum a de seis, que são as cores que o original escolheu.
 */
public final class SalisFx {
    private SalisFx() {
    }

    public static void tick(ItemEntity entity, SalisItem salis, int age) {
        if (age < SalisItem.LIFE) {
            sparkle(entity, salis, false);
            return;
        }
        if (age > SalisItem.LIFE) return;
        for (int i = 0; i < 200; i++) sparkle(entity, salis, true);
    }

    private static void sparkle(ItemEntity entity, SalisItem salis, boolean death) {
        RandomSource random = entity.level().getRandom();
        double theta = random.nextDouble() * Math.PI;
        double phi = random.nextDouble() * Math.PI * 2.0;
        double x = Math.cos(phi) * Math.sin(theta) * 0.25;
        double y = Math.sin(phi) * Math.sin(theta) * 0.25;
        double z = Math.cos(theta) * 0.25;
        int type = salis.kind() == SalisItem.Kind.TEMPESTAS ? 7 : 6;
        int multiplier = death ? 30 + random.nextInt(5) : 3 + random.nextInt(2);
        Sparkle.custom(random,
                entity.getX() + x, entity.getBoundingBox().maxY + y, entity.getZ() + z,
                1.75f, type, multiplier, death ? 0.0f : -0.1f,
                death ? x * 3.0 : 0.0, death ? y * 3.0 : 0.0, death ? z * 3.0 : 0.0);
    }
}
