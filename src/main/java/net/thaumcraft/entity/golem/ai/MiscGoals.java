package net.thaumcraft.entity.golem.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.entity.GolemEntity;

/**
 * O resto das tarefas dos golens da 4.2.3.5 ({@code thaumcraft.common.entities.ai.misc}): voltar para casa quando não
 * há o que fazer, e abrir (e fechar depois) as portas de madeira e as porteiras no caminho.
 */
public final class MiscGoals {
    private MiscGoals() {
    }

    /** O {@code AIReturnHome}: volta para a casa, esperando cada vez mais entre as tentativas se não achar caminho. */
    public static class ReturnHome extends GolemGoal {
        private double movePosX, movePosY, movePosZ;
        private int pathingDelay;
        private int pathingDelayInc = 5;
        private int count;
        private int prevX, prevY, prevZ;

        public ReturnHome(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            BlockPos home = this.golem.home();
            if (this.pathingDelay > 0) this.pathingDelay--;
            if (this.pathingDelay <= 0 && !(this.golem.distanceToSqr(home.getX() + 0.5, home.getY() + 0.5, home.getZ() + 0.5) < 3.0)) {
                this.movePosX = home.getX() + 0.5;
                this.movePosY = home.getY() + 0.5;
                this.movePosZ = home.getZ() + 0.5;
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            BlockPos home = this.golem.home();
            return this.pathingDelay <= 0 && this.count > 0 && !this.pathDone()
                    && this.golem.distanceToSqr(home.getX() + 0.5, home.getY() + 0.5, home.getZ() + 0.5) >= 3.0;
        }

        private void tried(boolean path) {
            if (!path) {
                this.pathingDelay = this.pathingDelayInc;
                if (this.pathingDelayInc < 50) this.pathingDelayInc += 5;
            } else {
                this.pathingDelayInc = 5;
            }
        }

        @Override
        public void tick() {
            this.count--;
            if (this.count == 0 && this.prevX == Mth.floor(this.golem.getX()) && this.prevY == Mth.floor(this.golem.getY())
                    && this.prevZ == Mth.floor(this.golem.getZ())) {
                Vec3 v = DefaultRandomPos.getPos(this.golem, 2, 1);
                if (v != null) {
                    this.count = 20;
                    this.tried(this.moveTo(v.x + 0.5, v.y + 0.5, v.z + 0.5));
                }
            }
        }

        @Override
        public void start() {
            this.count = 20;
            this.prevX = Mth.floor(this.golem.getX());
            this.prevY = Mth.floor(this.golem.getY());
            this.prevZ = Mth.floor(this.golem.getZ());
            this.tried(this.moveTo(this.movePosX, this.movePosY, this.movePosZ));
        }
    }

    /** O {@code AIOpenDoor} (e o {@code AIDoorInteract}): abre a porta de madeira ou a porteira no caminho, e fecha depois. */
    public static class OpenDoor extends net.minecraft.world.entity.ai.goal.Goal {
        private final GolemEntity golem;
        private final boolean closeDoor;
        private BlockPos doorPos = BlockPos.ZERO;
        private boolean hasStopped;
        private float dirX, dirZ;
        private int count;
        private int closeTimer;

        public OpenDoor(GolemEntity golem, boolean closeDoor) {
            this.golem = golem;
            this.closeDoor = closeDoor;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        private boolean usable(BlockPos pos) {
            BlockState s = this.golem.level().getBlockState(pos);
            return (s.getBlock() instanceof DoorBlock door && door.type().canOpenByHand()) || s.getBlock() instanceof FenceGateBlock;
        }

        @Override
        public boolean canUse() {
            if (!this.golem.horizontalCollision) return false;
            var nav = this.golem.getNavigation();
            Path path = nav.getPath();
            if (path == null || path.isDone()) return false;
            for (int i = 0; i < Math.min(path.getNextNodeIndex() + 2, path.getNodeCount()); i++) {
                Node n = path.getNode(i);
                this.doorPos = new BlockPos(n.x, n.y, n.z);
                if (this.golem.distanceToSqr(n.x, this.golem.getY(), n.z) <= 2.25 && this.usable(this.doorPos)) {
                    this.count = 200;
                    return true;
                }
            }
            this.doorPos = this.golem.blockPosition();
            if (this.usable(this.doorPos)) {
                this.count = 200;
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.closeDoor && this.closeTimer > 0 && this.count > 0 && !this.hasStopped;
        }

        @Override
        public void start() {
            this.count = 100;
            this.hasStopped = false;
            this.dirX = (float) (this.doorPos.getX() + 0.5f - this.golem.getX());
            this.dirZ = (float) (this.doorPos.getZ() + 0.5f - this.golem.getZ());
            this.closeTimer = 20;
            this.setOpen(true);
        }

        @Override
        public void stop() {
            if (this.closeDoor) this.setOpen(false);
        }

        @Override
        public void tick() {
            this.closeTimer--;
            this.count--;
            float fx = (float) (this.doorPos.getX() + 0.5f - this.golem.getX());
            float fz = (float) (this.doorPos.getZ() + 0.5f - this.golem.getZ());
            if (this.dirX * fx + this.dirZ * fz < 0.0f) this.hasStopped = true;
        }

        private void setOpen(boolean open) {
            Level level = this.golem.level();
            BlockState s = level.getBlockState(this.doorPos);
            if (s.getBlock() instanceof DoorBlock door) {
                door.setOpen(this.golem, level, s, this.doorPos, open);
            } else if (s.getBlock() instanceof FenceGateBlock && s.getValue(BlockStateProperties.OPEN) != open) {
                BlockState next = s.setValue(BlockStateProperties.OPEN, open);
                if (open) {
                    // abre para o lado de lá, como o original (vira a porteira se ela estiver virada para o golem)
                    var facing = this.golem.getDirection();
                    if (s.getValue(FenceGateBlock.FACING) == facing.getOpposite()) next = next.setValue(FenceGateBlock.FACING, facing);
                }
                level.setBlock(this.doorPos, next, 3);
                level.playSound(null, this.doorPos, open ? net.minecraft.sounds.SoundEvents.FENCE_GATE_OPEN : net.minecraft.sounds.SoundEvents.FENCE_GATE_CLOSE,
                        net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, level.getRandom().nextFloat() * 0.1f + 0.9f);
                level.gameEvent(this.golem, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, this.doorPos);
            }
        }
    }
}
