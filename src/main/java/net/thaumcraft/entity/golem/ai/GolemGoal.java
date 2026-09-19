package net.thaumcraft.entity.golem.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.entity.GolemEntity;
import net.thaumcraft.entity.golem.GolemHelper;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * A base das tarefas do golem: o {@code EntityAIBase} com o {@code setMutexBits(3)} (andar e olhar) que todas as
 * tarefas dos golens usam, rodando a cada tique como no original.
 */
public abstract class GolemGoal extends Goal {
    protected final GolemEntity golem;

    protected GolemGoal(GolemEntity golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /** O {@code ticksExisted % golemDelay <= 0}: o golem só pensa em trabalho novo a cada cinco tiques. */
    protected boolean onBeat() {
        return this.golem.tickCount % GolemHelper.DELAY <= 0;
    }

    protected boolean pathDone() {
        return this.golem.getNavigation().isDone();
    }

    protected boolean nearHome(double maxSq) {
        BlockPos home = this.golem.home();
        return !(this.golem.distanceToSqr(home.getX() + 0.5, home.getY() + 0.5, home.getZ() + 0.5) > maxSq);
    }

    /** O {@code tryMoveToXYZ} de então: vai até o ponto exato (o de hoje aceita parar a um bloco dele). */
    protected boolean moveTo(double x, double y, double z) {
        var nav = this.golem.getNavigation();
        return nav.moveTo(nav.createPath(x, y, z, 0), this.golem.golemSpeed());
    }

    /**
     * A tarefa que vai até um lugar, com o destrave do original: se em duzentos tiques ele não saiu do bloco, dá uns
     * passos para um lado qualquer e tenta de novo.
     */
    public abstract static class Goto extends GolemGoal {
        protected double movePosX, movePosY, movePosZ;
        protected int count;
        private int prevX, prevY, prevZ;

        protected Goto(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canContinueToUse() {
            return this.count > 0 && !this.pathDone();
        }

        @Override
        public void tick() {
            this.count--;
            if (this.count == 0 && this.prevX == Mth.floor(this.golem.getX()) && this.prevY == Mth.floor(this.golem.getY())
                    && this.prevZ == Mth.floor(this.golem.getZ())) {
                Vec3 v = DefaultRandomPos.getPos(this.golem, 2, 1);
                if (v != null) {
                    this.count = 20;
                    this.moveTo(v.x, v.y, v.z);
                }
            }
        }

        @Override
        public void stop() {
            this.count = 0;
        }

        @Override
        public void start() {
            this.count = 200;
            this.prevX = Mth.floor(this.golem.getX());
            this.prevY = Mth.floor(this.golem.getY());
            this.prevZ = Mth.floor(this.golem.getZ());
            this.moveTo(this.movePosX, this.movePosY, this.movePosZ);
        }
    }

    /** A tarefa que mexe num baú: a tampa fica aberta cinco tiques e fecha quando a tarefa acaba. */
    public abstract static class WithChest extends GolemGoal {
        protected int countChest;
        protected int count;
        @Nullable
        protected Container inv;

        protected WithChest(GolemEntity golem) {
            super(golem);
        }

        protected void opened(Container container) {
            this.golem.openChest(container);
            this.countChest = 5;
            this.inv = container;
        }

        @Override
        public void stop() {
            if (this.inv != null) this.golem.closeChest(this.inv);
            this.inv = null;
        }

        @Override
        public void tick() {
            this.countChest--;
        }
    }
}
