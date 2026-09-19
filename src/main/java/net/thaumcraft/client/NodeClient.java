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
        net.thaumcraft.client.fx.Burst.spawn(at, 1.0f, level.getRandom());
        level.playLocalSound(at.x, at.y, at.z, net.thaumcraft.registry.TCSounds.CRAFT_FAIL.value(), net.minecraft.sounds.SoundSource.BLOCKS, 0.5f, 1.0f, false);
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

    public static boolean isLocalPlayer(Entity entity) {
        return entity == Minecraft.getInstance().player;
    }
}
