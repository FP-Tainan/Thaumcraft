package net.thaumcraft.entity;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

/**
 * O arremesso do {@code EntityThrowable} do 1.7.10, que é de onde as brasas e as esferas de gelo do mod saem.
 *
 * <p>O projétil nasce no olho de quem lança, dezesseis centésimos para o lado e um décimo abaixo, e parte na
 * direção da mira. O espalhamento é o do jogo antigo: um sorteio de sino vezes 0,0075 vezes o espalhamento
 * pedido, em cada eixo — o jogo novo espalha mais que o dobro disso para o mesmo número.
 */
public final class Throw {
    private Throw() {
    }

    static void from(Projectile projectile, LivingEntity thrower, float velocity, float inaccuracy) {
        // o construtor do EntityThrowable já mira com espalhamento um, e o da brasa mira de novo por cima
        aim(projectile, thrower);
        Vec3 first = projectile.getDeltaMovement();
        projectile.setDeltaMovement(heading(projectile.getRandom(), first.x, first.y, first.z, velocity, inaccuracy));
    }

    /**
     * Só o arremesso do construtor, com a velocidade que o projétil declara ({@code func_70182_d}) e
     * espalhamento um — é assim que a esfera primordial sai, sem a segunda mira das brasas.
     */
    static void once(Projectile projectile, LivingEntity thrower, float velocity) {
        aim(projectile, thrower, velocity);
    }

    /** O mesmo, com o {@code func_70183_g}: o tanto que a mira sobe (negativo) ou desce, em graus. */
    static void once(Projectile projectile, LivingEntity thrower, float velocity, float pitchOffset) {
        aim(projectile, thrower, velocity, pitchOffset);
    }

    private static void aim(Projectile projectile, LivingEntity thrower) {
        aim(projectile, thrower, 1.5f);
    }

    private static void aim(Projectile projectile, LivingEntity thrower, float velocity) {
        aim(projectile, thrower, velocity, 0.0f);
    }

    private static void aim(Projectile projectile, LivingEntity thrower, float velocity, float pitchOffset) {
        float yaw = thrower.getYRot(), pitch = thrower.getXRot();
        double x = thrower.getX() - Mth.cos(yaw / 180.0f * (float) Math.PI) * 0.16f;
        double y = thrower.getEyeY() - 0.1;
        double z = thrower.getZ() - Mth.sin(yaw / 180.0f * (float) Math.PI) * 0.16f;
        projectile.snapTo(x, y, z, yaw, pitch);

        float f = 0.4f;
        double mx = -Mth.sin(yaw / 180.0f * (float) Math.PI) * Mth.cos(pitch / 180.0f * (float) Math.PI) * f;
        double mz = Mth.cos(yaw / 180.0f * (float) Math.PI) * Mth.cos(pitch / 180.0f * (float) Math.PI) * f;
        double my = -Mth.sin((pitch + pitchOffset) / 180.0f * (float) Math.PI) * f;
        projectile.setDeltaMovement(heading(projectile.getRandom(), mx, my, mz, velocity, 1.0f));
    }

    /** O {@code setThrowableHeading} do 1.7.10 num projétil já no ar. */
    public static void shoot(Projectile projectile, double x, double y, double z, float velocity, float inaccuracy) {
        projectile.setDeltaMovement(heading(projectile.getRandom(), x, y, z, velocity, inaccuracy));
    }

    /** O arremesso do construtor do {@code EntityThrowable}, para quem está fora deste pacote. */
    public static void fromThrower(Projectile projectile, LivingEntity thrower, float velocity) {
        aim(projectile, thrower, velocity);
    }

    /** O {@code setThrowableHeading} do 1.7.10. */
    private static Vec3 heading(RandomSource random, double x, double y, double z, float velocity, float inaccuracy) {
        double length = Math.sqrt(x * x + y * y + z * z);
        x /= length;
        y /= length;
        z /= length;
        x += random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.0075 * inaccuracy;
        y += random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.0075 * inaccuracy;
        z += random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.0075 * inaccuracy;
        return new Vec3(x * velocity, y * velocity, z * velocity);
    }
}
