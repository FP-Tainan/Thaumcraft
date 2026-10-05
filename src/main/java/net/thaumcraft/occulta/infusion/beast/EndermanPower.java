package net.thaumcraft.occulta.infusion.beast;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.occulta.infusion.Infusion;
import org.jetbrains.annotations.Nullable;

/**
 * O poder do <b>enderman</b>: a {@code CreaturePowerEnderman} do Witchery.
 *
 * <p>O salto da {@linkplain net.thaumcraft.occulta.infusion.OtherwhereInfusion Infusão do Outro Lugar},
 * emprestado a quem não a tem — e com o alcance do olhar e não o dela.
 *
 * <p>É o poder que mais vale a pena ter no bolso, e o mais difícil de encher: enderman não é bicho que se
 * ande a tomar para si.
 */
public class EndermanPower extends CreaturePower {
    public EndermanPower(int id) {
        super(id, EntityTypes.ENDERMAN);
    }

    @Override
    public void usa(ServerLevel level, ServerPlayer quem, int segurou, @Nullable HitResult onde) {
        if (!(onde instanceof BlockHitResult bateu)) {
            Infusion.falha(level, quem);
            return;
        }
        pó(level, quem);
        var ali = bateu.getLocation();
        double y = ali.y;
        switch (bateu.getDirection()) {
            case DOWN -> y -= 2.0;
            default -> {
            }
        }
        quem.resetFallDistance();
        quem.teleportTo(ali.x, y, ali.z);
        pó(level, quem);
    }

    private static void pó(ServerLevel level, ServerPlayer quem) {
        level.sendParticles(ParticleTypes.PORTAL, quem.getX(), quem.getY() + 1.0, quem.getZ(),
                16, 0.5, 2.0, 0.5, 0.0);
        Infusion.toca(level, quem, SoundEvents.ENDERMAN_TELEPORT);
    }
}
