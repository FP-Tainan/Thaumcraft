package net.thaumcraft.client.fx;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.Focuses;

import java.util.HashMap;
import java.util.Map;

/**
 * O que os focos desenham do lado de quem vê: a metade {@code isRemote} do {@code onUsingFocusTick} de cada
 * foco da 4.2.3.5.
 *
 * <p>O raio sai da mão a cada tique até o que estiver na mira, e onde ele bate pipocam faíscas azuis. O facho
 * da escavação fica aceso enquanto o botão está apertado, e o bloco na mira vai rachando no mesmo passo em
 * que o servidor o rói.
 */
public final class FocusEffects {
    private static final Map<Integer, Focuses.Dig> DIGS = new HashMap<>();

    private FocusEffects() {
    }

    public static void init() {
        Focuses.clientEffects = FocusEffects::tick;
        net.thaumcraft.entity.AlumentumEntity.clientTick = FocusEffects::alumentumTick;
        net.thaumcraft.entity.PrimalOrbEntity.clientEffects = new net.thaumcraft.entity.PrimalOrbEntity.ClientEffects() {
            @Override
            public void tick(net.thaumcraft.entity.PrimalOrbEntity orb) {
                primalTick(orb);
            }

            @Override
            public void burst(net.thaumcraft.entity.PrimalOrbEntity orb) {
                primalBurst(orb);
            }
        };
    }

    /**
     * O rastro da esfera primordial: seis fogos-fátuos puxados para ela e um que fica para trás.
     *
     * <p>O original passa ao {@code wispFX4} só o desvio, sem somar a posição da esfera, e os seis nascem perto
     * da origem do mundo — longe de quem vê, somem no ato. Fica igual.
     */
    private static void primalTick(net.thaumcraft.entity.PrimalOrbEntity orb) {
        net.minecraft.util.RandomSource r = orb.level().getRandom();
        for (int a = 0; a < 6; a++) {
            Wisp.fx4((r.nextFloat() - r.nextFloat()) * 0.2f, (r.nextFloat() - r.nextFloat()) * 0.2f,
                    (r.nextFloat() - r.nextFloat()) * 0.2f, orb, a, true, 0.0f);
        }
        Wisp.fx2(orb.getX() + (r.nextFloat() - r.nextFloat()) * 0.2f, orb.getY() + (r.nextFloat() - r.nextFloat()) * 0.2f,
                orb.getZ() + (r.nextFloat() - r.nextFloat()) * 0.2f, 0.1f, r.nextInt(6), true, 0.0f);
    }

    /** O rastro do Alumentum: fogos-fátuos negros em volta dele e no meio do caminho, e uma faísca. */
    private static void alumentumTick(net.thaumcraft.entity.AlumentumEntity alumentum) {
        net.minecraft.util.RandomSource r = alumentum.level().getRandom();
        for (int a = 0; a < 3; a++) {
            Wisp.fx2(alumentum.getX() + (r.nextFloat() - r.nextFloat()) * 0.3f,
                    alumentum.getY() + (r.nextFloat() - r.nextFloat()) * 0.3f,
                    alumentum.getZ() + (r.nextFloat() - r.nextFloat()) * 0.3f, 0.3f, 5, true, 0.02f);
            Wisp.fx2((alumentum.getX() + alumentum.xo) / 2.0 + (r.nextFloat() - r.nextFloat()) * 0.3f,
                    (alumentum.getY() + alumentum.yo) / 2.0 + (r.nextFloat() - r.nextFloat()) * 0.3f,
                    (alumentum.getZ() + alumentum.zo) / 2.0 + (r.nextFloat() - r.nextFloat()) * 0.3f, 0.3f, 5, true, 0.02f);
            Sparkle.spawn(r, alumentum.getX() + (r.nextFloat() - r.nextFloat()) * 0.1f,
                    alumentum.getY() + (r.nextFloat() - r.nextFloat()) * 0.1f,
                    alumentum.getZ() + (r.nextFloat() - r.nextFloat()) * 0.1f, 1.5f, 6, 0.0f);
        }
    }

    /** O estouro: trinta e seis fogos-fátuos das seis cores voando para fora. */
    private static void primalBurst(net.thaumcraft.entity.PrimalOrbEntity orb) {
        net.minecraft.util.RandomSource r = orb.level().getRandom();
        for (int a = 0; a < 6; a++) {
            for (int b = 0; b < 6; b++) {
                float fx = (r.nextFloat() - r.nextFloat()) * 0.5f;
                float fy = (r.nextFloat() - r.nextFloat()) * 0.5f;
                float fz = (r.nextFloat() - r.nextFloat()) * 0.5f;
                Wisp.fx3(orb.getX() + fx, orb.getY() + fy, orb.getZ() + fz, orb.getX() + fx * 10.0f,
                        orb.getY() + fy * 10.0f, orb.getZ() + fz * 10.0f, 0.4f, b, true, 0.05f);
            }
        }
    }

    private static void tick(Level level, Player player, ItemStack wand, FocusItem focus) {
        switch (focus.type()) {
            case "shock" -> shock(level, player);
            case "excavation" -> excavation(level, player, wand, focus);
            default -> {
            }
        }
    }

    /** O {@code doLightningBolt} do lado de quem vê. */
    private static void shock(Level level, Player player) {
        Entity pointed = Focuses.pointedEntity(level, player, 20.0);
        HitResult mop = Focuses.targetBlock(level, player);
        Vec3 look = player.getViewVector(2.0f);
        Vec3 base = hand(player);
        double px = player.getX() + look.x * 10.0, py = base.y + 0.06 + look.y * 10.0, pz = player.getZ() + look.z * 10.0;
        var random = level.getRandom();
        if (mop.getType() != HitResult.Type.MISS) {
            Vec3 at = mop.getLocation();
            px = at.x;
            py = at.y;
            pz = at.z;
            for (int a = 0; a < 5; a++) {
                Sparkle.spawn(random, px + (random.nextFloat() - random.nextFloat()) * 0.3f,
                        py + (random.nextFloat() - random.nextFloat()) * 0.3f,
                        pz + (random.nextFloat() - random.nextFloat()) * 0.3f,
                        2.0f + random.nextFloat(), 2, 0.05f + random.nextFloat() * 0.05f);
            }
        }
        if (pointed != null) {
            px = pointed.getX();
            py = pointed.getBoundingBox().minY + pointed.getBbHeight() / 2.0f;
            pz = pointed.getZ();
            for (int a = 0; a < 5; a++) {
                Sparkle.spawn(random, px + (random.nextFloat() - random.nextFloat()) * 0.6f,
                        py + (random.nextFloat() - random.nextFloat()) * 0.6f,
                        pz + (random.nextFloat() - random.nextFloat()) * 0.6f,
                        2.0f + random.nextFloat(), 2, 0.05f + random.nextFloat() * 0.05f);
            }
        }
        // o shootLightning: da mão até lá, seis tiques de vida, oito segmentos por tique
        LightningBolt bolt = new LightningBolt(base.x, base.y, base.z, px, py, pz, random.nextLong(), 6, 0.5f, 8);
        bolt.defaultFractal();
        bolt.setType(2);
        bolt.setWidth(0.125f);
        bolt.finalizeBolt();
    }

    /** O ponto de onde o raio sai: um tanto para o lado e à frente do olho, o {@code shootLightning}. */
    public static Vec3 hand(Player player) {
        boolean self = player == net.minecraft.client.Minecraft.getInstance().player;
        double px = player.getX(), pz = player.getZ();
        double py = self ? player.getEyeY() : player.getBoundingBox().minY + player.getBbHeight() / 2.0f + 0.25;
        px += -Mth.cos(player.getYRot() / 180.0f * (float) Math.PI) * 0.06f;
        py += -0.06f;
        pz += -Mth.sin(player.getYRot() / 180.0f * (float) Math.PI) * 0.06f;
        Vec3 look = player.getViewVector(1.0f);
        return new Vec3(px + look.x * 0.3, py + look.y * 0.3, pz + look.z * 0.3);
    }

    /** O facho e a rachadura da escavação. */
    private static void excavation(Level level, Player player, ItemStack wand, FocusItem focus) {
        HitResult mop = Focuses.targetBlock(level, player);
        Vec3 look = player.getLookAngle();
        double eyeY = player.getEyeY();
        double tx = player.getX() + look.x * 10.0, ty = eyeY + look.y * 10.0, tz = player.getZ() + look.z * 10.0;
        int impact = 0;
        if (mop.getType() != HitResult.Type.MISS) {
            tx = mop.getLocation().x;
            ty = mop.getLocation().y;
            tz = mop.getLocation().z;
            impact = 5;
        }
        WandBeam.cont(player, tx, ty, tz, 2, 65382, false, impact > 0 ? 2.0f : 0.0f, impact);

        Focuses.Dig dig = DIGS.computeIfAbsent(player.getId(), id -> new Focuses.Dig());
        BlockPos was = dig.pos;
        Focuses.Dig.Step step = dig.advance(level, mop instanceof BlockHitResult block ? block : null, true,
                net.thaumcraft.item.WandItem.focusPotency(wand));
        if (step.progress() >= 0 && step.pos() != null) {
            level.destroyBlockProgress(player.getId(), step.pos(), step.progress());
        } else if (was != null && !was.equals(dig.pos)) {
            level.destroyBlockProgress(player.getId(), was, -1);
        }
    }
}
