package net.thaumcraft.block.entity.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.net.TCNetwork;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.research.Warp;

/**
 * A armadilha da pedra rúnica: o {@code TileEldritchTrap} da 4.2.3.5. A cada meio a dois segundos, o jogador mais perto a
 * até três blocos leva dois de dano mágico, com um raio da pedra até os olhos dele, e metade das vezes um ou dois de
 * distorção passageira.
 */
public class RunedStoneBlockEntity extends BlockEntity {
    private int count = 20;

    public RunedStoneBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.RUNED_STONE, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RunedStoneBlockEntity te) {
        if (!(level instanceof ServerLevel server) || te.count-- > 0) return;
        te.count = 10 + level.getRandom().nextInt(25);
        Player p = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0, false);
        if (p == null) return;
        p.hurtServer(server, level.damageSources().magic(), 2.0f);
        if (level.getRandom().nextBoolean()) Warp.add(p, 1 + level.getRandom().nextInt(2), true);
        TCNetwork.blockZap(server, pos, Vec3.atCenterOf(pos), new Vec3(p.getX(), p.getBoundingBox().minY + p.getEyeHeight(), p.getZ()));
    }
}
