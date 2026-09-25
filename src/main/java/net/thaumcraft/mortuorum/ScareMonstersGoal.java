package net.thaumcraft.mortuorum;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * O afugentar do ursinho: o {@code EntityAIScareEntities} do Necromancy.
 *
 * <p>De guarda, o ursinho anda até o monstro mais perto; chegando a menos de sete blocos, é o monstro que passa a
 * fugir dele — o ursinho não bate, faz correr.
 */
public class ScareMonstersGoal extends Goal {
    private final TeddyEntity teddy;
    private final float seeking;
    private final float scaring;
    private @Nullable PathfinderMob alvo;

    public ScareMonstersGoal(TeddyEntity teddy, float seeking, float scaring) {
        this.teddy = teddy;
        this.seeking = seeking;
        this.scaring = scaring;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.teddy.state() != TeddyEntity.State.DEFENDING) return false;
        this.alvo = this.closest();
        return this.alvo != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.alvo != null && this.alvo.isAlive() && this.teddy.state() == TeddyEntity.State.DEFENDING;
    }

    @Override
    public void stop() {
        this.alvo = null;
        this.teddy.getNavigation().stop();
    }

    @Override
    public void tick() {
        PathfinderMob monstro = this.alvo;
        if (monstro == null) return;
        this.teddy.getNavigation().moveTo(monstro, 1.0);
        if (this.teddy.distanceTo(monstro) >= this.scaring) return;
        Vec3 longe = DefaultRandomPos.getPosAway(monstro, 16, 7, this.teddy.position());
        if (longe == null) return;
        if (monstro.distanceToSqr(longe) < monstro.distanceToSqr(this.teddy)) return;
        monstro.getNavigation().moveTo(longe.x, longe.y, longe.z, 1.4);
    }

    /** O monstro mais perto, dentro do alcance de procura. */
    private @Nullable PathfinderMob closest() {
        AABB volta = this.teddy.getBoundingBox().inflate(this.seeking);
        PathfinderMob achado = null;
        double perto = this.seeking + 1.0;
        for (PathfinderMob bicho : this.teddy.level().getEntitiesOfClass(PathfinderMob.class, volta,
                candidato -> candidato instanceof Enemy && candidato.isAlive())) {
            double distancia = this.teddy.distanceTo(bicho);
            if (distancia < perto) {
                perto = distancia;
                achado = bicho;
            }
        }
        return achado;
    }
}
