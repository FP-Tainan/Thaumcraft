package net.thaumcraft.occulta.infusion.beast;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * O poder do <b>ghast</b>: a {@code CreaturePowerGhast} do Witchery.
 *
 * <p>Uma <b>bola de fogo grande</b>, da que o ghast cospe — a que estoura e faz cratera. Uma de cada vez,
 * uma carga de bicho, e dez delas por ghast morto.
 *
 * <p>É o poder mais caro de encher e o mais bruto de usar, e isso é a troca inteira.
 */
public class GhastPower extends CreaturePower {
    public GhastPower(int id) {
        super(id, EntityTypes.GHAST);
    }

    @Override
    public void usa(ServerLevel level, ServerPlayer quem, int segurou, @Nullable HitResult onde) {
        Vec3 rumo = quem.getLookAngle();
        LargeFireball bola = new LargeFireball(level, quem, rumo.normalize(), 1);
        bola.setPos(quem.getX() + rumo.x, quem.getY() + quem.getEyeHeight() + 0.5, quem.getZ() + rumo.z);
        level.addFreshEntity(bola);
        level.levelEvent(null, 1016, BlockPos.containing(quem.position()), 0);
    }
}
