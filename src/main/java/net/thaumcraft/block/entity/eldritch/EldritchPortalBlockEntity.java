package net.thaumcraft.block.entity.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.research.ResearchManager;
import net.thaumcraft.world.OuterLands;
import net.thaumcraft.world.outer.OuterTeleporter;

/**
 * O portal eldritch: o {@code TileEldritchPortal} da 4.2.3.5. De quem vê, abre em trinta tiques e geme de doze em doze
 * segundos; do servidor, a cada cinco tiques, quem estiver dentro (e não estiver montado nem carregando ninguém) passa
 * para as Terras de Fora — descobrindo "Entrar nas Terras de Fora" — ou, de lá, volta para o mundo de cima.
 */
public class EldritchPortalBlockEntity extends BlockEntity {
    public int opencount = -1;
    private int count;

    public EldritchPortalBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_PORTAL, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EldritchPortalBlockEntity te) {
        te.count++;
        if (level.isClientSide()) {
            if (te.count % 250 == 0 || te.count == 0) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.EVIL_PORTAL.value(), SoundSource.BLOCKS,
                        1.0f, 1.0f, false);
            }
            if (te.opencount < 30) te.opencount++;
            return;
        }
        if (te.count % 5 != 0 || !(level instanceof ServerLevel server)) return;
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(0.5, 1.0, 0.5))) {
            if (player.isPassenger() || player.isVehicle()) continue;
            if (player.isOnPortalCooldown()) {
                player.setPortalCooldown(100);
            } else if (!OuterLands.is(level)) {
                player.setPortalCooldown(100);
                ServerLevel outer = server.getServer().getLevel(OuterLands.KEY);
                if (outer == null) continue;
                OuterTeleporter.send(player, outer);
                ResearchManager.complete(player, "ENTEROUTER");
            } else {
                player.setPortalCooldown(100);
                OuterTeleporter.send(player, server.getServer().overworld());
            }
        }
    }
}
