package net.thaumcraft.entity.eldritch;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.block.eldritch.EldritchStoneBlock;

import java.util.EnumSet;

/** As inteligências dos cultistas, descompiladas da 4.2.3.5. */
public final class CultistGoals {
    private CultistGoals() {
    }

    /**
     * O {@code AIAttackOnCollide} do Thaumcraft (a do jogo de então): corre atrás do alvo recalculando o caminho de tempos em
     * tempos e bate a cada dez tiques quando chega à distância do corpo — o dobro da largura ao quadrado, mais a do alvo.
     */
    public static class AttackOnCollide extends Goal {
        private final PathfinderMob attacker;
        private final double speed;
        private final boolean longMemory;
        private Path path;
        private int attackTick;
        private int delay;
        private double tx, ty, tz;
        private int failedPathFindingPenalty;

        public AttackOnCollide(PathfinderMob attacker, double speed, boolean longMemory) {
            this.attacker = attacker;
            this.speed = speed;
            this.longMemory = longMemory;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.attacker.getTarget();
            if (target == null || !target.isAlive()) return false;
            if (--this.delay <= 0) {
                this.path = this.attacker.getNavigation().createPath(target, 0);
                this.delay = 4 + this.attacker.getRandom().nextInt(7);
                return this.path != null;
            }
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.attacker.getTarget();
            if (target == null || !target.isAlive()) return false;
            return !this.longMemory ? !this.attacker.getNavigation().isDone() : this.attacker.isWithinHome(target.blockPosition());
        }

        @Override
        public void start() {
            this.attacker.getNavigation().moveTo(this.path, this.speed);
            this.delay = 0;
        }

        @Override
        public void stop() {
            this.attacker.getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.attacker.getTarget();
            if (target == null) return;
            this.attacker.getLookControl().setLookAt(target, 30.0f, 30.0f);
            double d0 = this.attacker.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());
            double d1 = this.attacker.getBbWidth() * 2.0f * this.attacker.getBbWidth() * 2.0f + target.getBbWidth();
            this.delay--;
            if (this.attackTick > 0) this.attackTick--;
            if ((this.longMemory || this.attacker.getSensing().hasLineOfSight(target)) && this.delay <= 0
                    && (this.tx == 0.0 && this.ty == 0.0 && this.tz == 0.0 || target.distanceToSqr(this.tx, this.ty, this.tz) >= 1.0
                    || this.attacker.getRandom().nextFloat() < 0.05f)) {
                this.tx = target.getX();
                this.ty = target.getBoundingBox().minY;
                this.tz = target.getZ();
                this.delay = this.failedPathFindingPenalty + 4 + this.attacker.getRandom().nextInt(7);
                Path current = this.attacker.getNavigation().getPath();
                if (current != null && current.getEndNode() != null
                        && target.distanceToSqr(current.getEndNode().x, current.getEndNode().y, current.getEndNode().z) < 1.0) {
                    this.failedPathFindingPenalty = 0;
                } else {
                    this.failedPathFindingPenalty += 10;
                }
                if (d0 > 1024.0) this.delay += 10;
                else if (d0 > 256.0) this.delay += 5;
                if (!this.attacker.getNavigation().moveTo(target, this.speed)) this.delay += 15;
            }
            if (d0 <= d1 && this.attackTick <= 0) {
                this.attackTick = 10;
                if (!this.attacker.getMainHandItem().isEmpty()) this.attacker.swing(InteractionHand.MAIN_HAND);
                if (this.attacker.level() instanceof net.minecraft.server.level.ServerLevel server) this.attacker.doHurtTarget(server, target);
            }
        }
    }

    /** O {@code AILongRangeAttack}: o ataque de longe do jogo, que não começa com o alvo mais perto do que o mínimo. */
    public static class LongRangeAttack extends RangedAttackGoal {
        private final PathfinderMob wielder;
        private final double minDistance;

        public <T extends PathfinderMob & RangedAttackMob> LongRangeAttack(T mob, double min, double speed, int minInterval, int maxInterval, float range) {
            super(mob, speed, minInterval, maxInterval, range);
            this.wielder = mob;
            this.minDistance = min;
        }

        @Override
        public boolean canUse() {
            boolean ex = super.canUse();
            if (ex) {
                LivingEntity target = this.wielder.getTarget();
                if (target == null) return false;
                if (!target.isAlive()) {
                    this.wielder.setTarget(null);
                    return false;
                }
                double ra = this.wielder.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());
                if (ra < this.minDistance * this.minDistance) return false;
            }
            return ex;
        }
    }

    /** O {@code AIAltarFocus}: o clérigo do ritual larga o ritual se o altar sumir ou ele se afastar mais de quatro blocos. */
    public static class AltarFocus extends Goal {
        private final CultistClericEntity cleric;

        public AltarFocus(CultistClericEntity cleric) {
            this.cleric = cleric;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.cleric.isRitualist() && this.cleric.hasHome();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void tick() {
            if (!this.cleric.hasHome() || this.cleric.tickCount % 40 != 0) return;
            var home = this.cleric.getHomePosition();
            var pos = this.cleric.blockPosition();
            float dist = (float) Math.sqrt(home.distSqr(pos));
            if (dist > 16.0f || !(this.cleric.level().getBlockState(home).getBlock() instanceof EldritchStoneBlock)) {
                this.cleric.setRitualist(false);
            }
        }
    }

    /**
     * O {@code AICultistHurtByTarget}: quem apanha passa a mirar quem bateu e chama os outros cultistas por perto — o
     * clérigo do ritual só larga o ritual uma vez em três.
     */
    public static class HurtByTarget extends TargetGoal {
        private int lastHurtTimestamp;

        public HurtByTarget(PathfinderMob mob) {
            super(mob, false);
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            int i = this.mob.getLastHurtByMobTimestamp();
            LivingEntity attacker = this.mob.getLastHurtByMob();
            return i != this.lastHurtTimestamp && attacker != null && this.canAttack(attacker, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            LivingEntity attacker = this.mob.getLastHurtByMob();
            this.mob.setTarget(attacker);
            this.lastHurtTimestamp = this.mob.getLastHurtByMobTimestamp();
            double d0 = this.getFollowDistance();
            AABB box = new AABB(this.mob.getX(), this.mob.getY(), this.mob.getZ(), this.mob.getX() + 1.0, this.mob.getY() + 1.0,
                    this.mob.getZ() + 1.0).inflate(d0, 10.0, d0);
            for (CultistEntity other : this.mob.level().getEntitiesOfClass(CultistEntity.class, box)) {
                if (other == this.mob || other.getTarget() != null || other.isAlliedTo(attacker)) continue;
                if (!(other instanceof CultistClericEntity cleric) || !cleric.isRitualist()) {
                    other.setTarget(attacker);
                } else if (this.mob.getRandom().nextInt(3) == 0) {
                    cleric.setRitualist(false);
                    other.setTarget(attacker);
                }
            }
            super.start();
        }
    }

    /** O {@code updateRotation} do clérigo, que o vira devagar para o altar. */
    static float updateRotation(float from, float to, float max) {
        float f = Mth.wrapDegrees(to - from);
        if (f > max) f = max;
        if (f < -max) f = -max;
        return from + f;
    }
}
