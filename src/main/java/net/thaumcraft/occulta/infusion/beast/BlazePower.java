package net.thaumcraft.occulta.infusion.beast;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * O poder do <b>blaze</b>: a {@code CreaturePowerBlaze} do Witchery.
 *
 * <p><b>Três</b> bolas de fogo pequenas de uma vez, abertas em leque — é a rajada do blaze, à letra, com o
 * mesmo sorteio que lhe dá a abertura.
 *
 * <p>Contra a do ghast: esta não faz cratera, mas são três e vão longe. É a diferença entre derrubar uma
 * parede e limpar um corredor.
 */
public class BlazePower extends CreaturePower {
    /** Quantas de cada vez. */
    public static final int QUANTAS = 3;

    /** E quanto elas se abrem. */
    public static final double ABRE = 0.2;

    public BlazePower(int id) {
        super(id, EntityTypes.BLAZE);
    }

    @Override
    public void usa(ServerLevel level, ServerPlayer quem, int segurou, @Nullable HitResult onde) {
        level.levelEvent(null, 1018, BlockPos.containing(quem.position()), 0);
        Vec3 rumo = quem.getLookAngle();
        for (int volta = 0; volta < QUANTAS; volta++) {
            Vec3 torta = rumo.add(level.getRandom().nextDouble() * ABRE, 0.0,
                    level.getRandom().nextDouble() * ABRE).normalize();
            SmallFireball bola = new SmallFireball(level, quem, torta);
            bola.setPos(quem.getX() + rumo.x, quem.getY() + quem.getBbHeight() / 2.0f + 0.5,
                    quem.getZ() + rumo.z);
            level.addFreshEntity(bola);
        }
    }
}
