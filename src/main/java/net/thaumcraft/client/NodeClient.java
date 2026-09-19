package net.thaumcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.client.fx.BoreParticle;

/** O que o nó faz só do lado de quem joga. */
public final class NodeClient {
    private NodeClient() {
    }

    /** O {@code hungryNodeFX}: uma migalha do bloco voando para o nó faminto. */
    public static void hungryFx(Level level, BlockPos from, BlockState state, BlockPos node) {
        if (level instanceof ClientLevel client) BoreParticle.hungry(client, from, state, node);
    }

    /** O {@code beamPower}: o fio de um relé até o pai dele. */
    public static void beam(BlockPos relay, net.minecraft.world.phys.Vec3 from, net.minecraft.world.phys.Vec3 to, float r, float g, float b, boolean pulse) {
        net.thaumcraft.client.fx.BeamPower.cont(relay, from, to, r, g, b, pulse);
    }

    /** O {@code burst} com o som de falha: o nó que o transdutor mexeu. */
    public static void burst(Level level, net.minecraft.world.phys.Vec3 at) {
        burst(level, at, true);
    }

    /** O {@code burst}, com ou sem o som. */
    public static void burst(Level level, net.minecraft.world.phys.Vec3 at, boolean sound) {
        net.thaumcraft.client.fx.Burst.spawn(at, 1.0f, level.getRandom());
        if (sound) level.playLocalSound(at.x, at.y, at.z, net.thaumcraft.registry.TCSounds.CRAFT_FAIL.value(), net.minecraft.sounds.SoundSource.BLOCKS, 0.5f, 1.0f, false);
    }

    /** O {@code nodeBolt}: o raio de tipo 0 do transdutor, dez tiques, quatro de força. */
    public static void nodeBolt(net.minecraft.world.phys.Vec3 from, net.minecraft.world.phys.Vec3 to) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        net.thaumcraft.client.fx.LightningBolt bolt = new net.thaumcraft.client.fx.LightningBolt(from.x, from.y, from.z, to.x, to.y, to.z,
                level.getRandom().nextLong(), 10, 4.0f, 5);
        bolt.defaultFractal();
        bolt.setType(0);
        bolt.finalizeBolt();
    }

    /** O {@code wispFX}: a chama colorida do fogo-fátuo, caindo de leve. */
    public static void wisp(double x, double y, double z, float size, int colour) {
        net.thaumcraft.client.fx.Wisp.colored(x, y, z, size, (colour >> 16 & 255) / 255.0f, (colour >> 8 & 255) / 255.0f, (colour & 255) / 255.0f);
    }

    /** O rastro de faíscas do morcego-bomba: os quadros 151 a 159 da folha, brancos. */
    public static void batBomb(double x, double y, double z, net.minecraft.util.RandomSource random) {
        net.thaumcraft.client.fx.ThaumFx.add(new net.thaumcraft.client.fx.GenericFx(x, y, z, 0.0, 0.0, 0.0, 1.0f, 1.0f, 1.0f, 0.8f, false,
                151, 9, 1, 7 + random.nextInt(5), 0, 1.0f + random.nextFloat() * 0.5f));
    }

    /** O rastro da rajada do pech: fogos-fátuos dos tipos 3 e 2 e uma faísca, três vezes por tique. */
    public static void pechBlastTrail(net.minecraft.world.entity.Entity blast) {
        net.minecraft.util.RandomSource r = blast.level().getRandom();
        for (int a = 0; a < 3; a++) {
            net.thaumcraft.client.fx.Wisp.fx2(blast.getX() + (r.nextFloat() - r.nextFloat()) * 0.2f, blast.getY() + (r.nextFloat() - r.nextFloat()) * 0.2f,
                    blast.getZ() + (r.nextFloat() - r.nextFloat()) * 0.2f, 0.3f, 3, true, 0.02f);
            net.thaumcraft.client.fx.Wisp.fx2((blast.getX() + blast.xo) / 2.0 + (r.nextFloat() - r.nextFloat()) * 0.2f,
                    (blast.getY() + blast.yo) / 2.0 + (r.nextFloat() - r.nextFloat()) * 0.2f,
                    (blast.getZ() + blast.zo) / 2.0 + (r.nextFloat() - r.nextFloat()) * 0.2f, 0.3f, 2, true, 0.02f);
            net.thaumcraft.client.fx.Sparkle.spawn(r, blast.getX() + (r.nextFloat() - r.nextFloat()) * 0.1f, blast.getY() + (r.nextFloat() - r.nextFloat()) * 0.1f,
                    blast.getZ() + (r.nextFloat() - r.nextFloat()) * 0.1f, 1.5f, 5, 0.0f);
        }
    }

    /** O estouro da rajada: nove vezes três fogos-fátuos (tipos 3, 2 e 0) saindo para fora. */
    public static void pechBlastBurst(net.minecraft.world.entity.Entity blast) {
        net.minecraft.util.RandomSource r = blast.level().getRandom();
        int[] types = {3, 2, 0};
        for (int a = 0; a < 9; a++) {
            for (int type : types) {
                float fx = (r.nextFloat() - r.nextFloat()) * 0.3f, fy = (r.nextFloat() - r.nextFloat()) * 0.3f, fz = (r.nextFloat() - r.nextFloat()) * 0.3f;
                net.thaumcraft.client.fx.Wisp.fx3(blast.getX() + fx, blast.getY() + fy, blast.getZ() + fz, blast.getX() + fx * 8.0f,
                        blast.getY() + fy * 8.0f, blast.getZ() + fz * 8.0f, 0.3f, type, true, 0.02f);
            }
        }
    }

    public static boolean isLocalPlayer(Entity entity) {
        return entity == Minecraft.getInstance().player;
    }
}
