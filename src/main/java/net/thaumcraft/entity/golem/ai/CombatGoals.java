package net.thaumcraft.entity.golem.ai;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.entity.GolemEntity;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * As tarefas de briga dos golens da 4.2.3.5 ({@code thaumcraft.common.entities.ai.combat}): fugir do creeper que vai
 * explodir, bater de perto, atirar dardos, e escolher em quem bater (quem o feriu, o mais perto, ou — para o
 * açougueiro — o mais velho de um rebanho de mais de dois).
 */
public final class CombatGoals {
    private CombatGoals() {
    }

    /** O {@code AIAvoidCreeperSwell}: foge (com um pulo) do creeper que está inchando. */
    public static class AvoidCreeperSwell extends Goal {
        private final GolemEntity golem;
        private float farSpeed, nearSpeed;
        private Entity closest;
        private Path path;
        private Vec3 targetBlock;

        public AvoidCreeperSwell(GolemEntity golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            if (this.farSpeed == 0.0f) {
                this.farSpeed = this.golem.golemSpeed() * 1.125f;
                this.nearSpeed = this.golem.golemSpeed() * 1.25f;
            }
            List<Creeper> list = this.golem.level().getEntitiesOfClass(Creeper.class, this.golem.getBoundingBox().inflate(5.0, 3.0, 5.0));
            if (list.isEmpty() || list.getFirst().getSwellDir() != 1) return false;
            this.closest = list.getFirst();
            if (!this.golem.getSensing().hasLineOfSight(this.closest)) return false;
            Vec3 v = DefaultRandomPos.getPosAway(this.golem, 16, 7, this.closest.position());
            if (v == null) return false;
            if (this.closest.distanceToSqr(v) < this.closest.distanceToSqr(this.golem)) return false;
            this.path = this.golem.getNavigation().createPath(v.x, v.y, v.z, 0);
            this.targetBlock = v;
            return this.path != null && this.path.canReach();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.golem.getNavigation().isDone();
        }

        @Override
        public void start() {
            double dx = this.targetBlock.x + 0.5 - this.golem.getX();
            double dz = this.targetBlock.z + 0.5 - this.golem.getZ();
            double d = Math.sqrt(dx * dx + dz * dz);
            Vec3 m = this.golem.getDeltaMovement();
            this.golem.setDeltaMovement(m.x + (dx / d * 0.8f + m.x * 0.2f), 0.3, m.z + (dz / d * 0.8f + m.z * 0.2f));
            this.golem.getNavigation().moveTo(this.path, this.nearSpeed);
        }

        @Override
        public void stop() {
            this.closest = null;
        }

        @Override
        public void tick() {
            this.golem.getNavigation().setSpeedModifier(this.golem.distanceToSqr(this.closest) < 49.0 ? this.nearSpeed : this.farSpeed);
        }
    }

    /** O {@code AIGolemAttackOnCollide}: persegue e bate, no compasso de ataque do golem. */
    public static class AttackOnCollide extends GolemGoal {
        private LivingEntity target;
        private int attackTick;
        private Path path;
        private int counter;

        public AttackOnCollide(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            LivingEntity t = this.golem.getTarget();
            if (t == null || !t.isAlive()) return false;
            if (!this.golem.isValidTarget(t)) {
                this.golem.setTarget(null);
                return false;
            }
            this.target = t;
            this.path = this.golem.getNavigation().createPath(t, 0);
            return this.path != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() && !this.pathDone();
        }

        @Override
        public void start() {
            this.golem.getNavigation().moveTo(this.path, this.golem.golemSpeed());
            this.counter = 0;
        }

        @Override
        public void stop() {
            this.target = null;
            this.golem.getNavigation().stop();
        }

        @Override
        public void tick() {
            this.golem.getLookControl().setLookAt(this.target, 30.0f, 30.0f);
            if (this.golem.getSensing().hasLineOfSight(this.target) && --this.counter <= 0) {
                this.counter = 4 + this.golem.getRandom().nextInt(7);
                this.golem.getNavigation().moveTo(this.target, this.golem.golemSpeed());
            }
            this.attackTick = Math.max(this.attackTick - 1, 0);
            double attackRange = this.target.getBbWidth() * 2.0f * this.target.getBbWidth() * 2.0f + 1.0;
            if (this.golem.distanceToSqr(this.target.getX(), this.target.getBoundingBox().minY, this.target.getZ()) <= attackRange
                    && this.attackTick <= 0) {
                this.attackTick = this.golem.getAttackSpeed();
                this.golem.startActionTimer();
                if (this.golem.level() instanceof net.minecraft.server.level.ServerLevel server) this.golem.doHurtTarget(server, this.target);
            }
        }
    }

    /** O {@code AIDartAttack}: com o lança-dardos (R), atira de longe no alvo. */
    public static class DartAttack extends GolemGoal {
        private LivingEntity attackTarget;
        private int rangedAttackTime;
        private final int maxRangedAttackTime;

        public DartAttack(GolemEntity golem) {
            super(golem);
            this.maxRangedAttackTime = 30 - golem.getUpgradeAmount(0) * 8;
            this.rangedAttackTime = this.maxRangedAttackTime / 2;
        }

        @Override
        public boolean canUse() {
            LivingEntity t = this.golem.getTarget();
            if (t == null || !t.isAlive()) return false;
            if (!this.golem.isValidTarget(t)) {
                this.golem.setTarget(null);
                return false;
            }
            if (this.golem.distanceToSqr(t.getX(), t.getBoundingBox().minY, t.getZ()) < 9.0) return false;
            this.attackTarget = t;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() && !this.pathDone();
        }

        @Override
        public void stop() {
            this.attackTarget = null;
            this.rangedAttackTime = this.maxRangedAttackTime / 2;
        }

        @Override
        public void tick() {
            double d = this.golem.distanceToSqr(this.attackTarget.getX(), this.attackTarget.getBoundingBox().minY, this.attackTarget.getZ());
            boolean sees = this.golem.getSensing().hasLineOfSight(this.attackTarget);
            this.golem.getNavigation().moveTo(this.attackTarget, this.golem.golemSpeed());
            if (!sees) return;
            this.golem.getLookControl().setLookAt(this.attackTarget, 30.0f, 30.0f);
            this.rangedAttackTime = Math.max(this.rangedAttackTime - 1, 0);
            if (this.rangedAttackTime <= 0) {
                float r = this.golem.getRange() * 0.8f;
                if (d <= r * r) {
                    this.golem.attackEntityWithRangedAttack(this.attackTarget);
                    this.rangedAttackTime = this.maxRangedAttackTime;
                }
            }
        }
    }

    /** O {@code EntityAITarget} do jogo de então, na medida em que os golens o usam. */
    abstract static class TargetGoal extends Goal {
        protected final GolemEntity golem;
        private final boolean checkSight;
        private final float distance;
        private int unseen;

        TargetGoal(GolemEntity golem, float distance, boolean checkSight) {
            this.golem = golem;
            this.distance = distance;
            this.checkSight = checkSight;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity t = this.golem.getTarget();
            if (t == null || !t.isAlive()) return false;
            double range = this.distance > 0 ? this.distance : 32.0;
            if (this.golem.distanceToSqr(t) > range * range) return false;
            if (this.checkSight) {
                if (this.golem.getSensing().hasLineOfSight(t)) this.unseen = 0;
                else if (++this.unseen > 60) return false;
            }
            return true;
        }

        @Override
        public void start() {
            this.unseen = 0;
        }

        @Override
        public void stop() {
            this.golem.setTarget(null);
        }
    }

    /** O {@code AIHurtByTarget}: revida em quem o feriu (menos o dono, e dentro do raio). */
    public static class HurtByTarget extends TargetGoal {
        public HurtByTarget(GolemEntity golem) {
            super(golem, 16.0f, false);
        }

        private boolean suitable(LivingEntity e) {
            if (e == null || e == this.golem || !e.isAlive() || !this.golem.canAttack(e)) return false;
            if (e instanceof Player p && (p.getAbilities().invulnerable || p.getName().getString().equals(this.golem.getOwnerName()))) return false;
            return this.golem.isWithinHome(e.blockPosition());
        }

        @Override
        public boolean canUse() {
            return this.suitable(this.golem.getLastHurtByMob());
        }

        @Override
        public boolean canContinueToUse() {
            return this.golem.getLastHurtByMob() != null;
        }

        @Override
        public void start() {
            this.golem.setTarget(this.golem.getLastHurtByMob());
            super.start();
        }

        @Override
        public void stop() {
            if (this.golem.getTarget() instanceof Player p && p.getAbilities().invulnerable) {
                this.golem.setTarget(null);
                super.stop();
            }
        }
    }

    /** O {@code AINearestAttackableTarget}: o alvo válido mais perto, no raio do golem. */
    public static class NearestAttackableTarget extends TargetGoal {
        private LivingEntity target;

        public NearestAttackableTarget(GolemEntity golem) {
            super(golem, 0.0f, true);
        }

        @Override
        public boolean canUse() {
            float d = this.golem.getRange();
            List<LivingEntity> list = this.golem.level().getEntitiesOfClass(LivingEntity.class, this.golem.getBoundingBox().inflate(d, 4.0, d));
            list.sort(Comparator.comparingDouble(this.golem::distanceToSqr));
            for (LivingEntity e : list) {
                if (e != this.golem && this.golem.isValidTarget(e)) {
                    this.target = e;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            this.golem.setTarget(this.target);
            super.start();
        }
    }

    /** O {@code AINearestButcherTarget}: o bicho mais velho, se houver mais de dois da mesma espécie por perto. */
    public static class NearestButcherTarget extends TargetGoal {
        private LivingEntity target;

        public NearestButcherTarget(GolemEntity golem) {
            super(golem, 0.0f, true);
        }

        @Override
        public boolean canUse() {
            float d = this.golem.getRange();
            List<LivingEntity> list = this.golem.level().getEntitiesOfClass(LivingEntity.class, this.golem.getBoundingBox().inflate(d, 4.0, d));
            list.sort((a, b) -> Integer.compare(b.tickCount, a.tickCount));
            for (LivingEntity e : list) {
                if (e == this.golem || !this.golem.isValidTarget(e)) continue;
                this.target = e;
                int count = 0;
                for (LivingEntity same : this.golem.level().getEntitiesOfClass(e.getClass(), this.golem.getBoundingBox().inflate(d, 4.0, d))) {
                    if (this.golem.isValidTarget(same)) count++;
                }
                if (count > 2) return true;
            }
            return false;
        }

        @Override
        public void start() {
            this.golem.setTarget(this.target);
            super.start();
        }
    }
}
