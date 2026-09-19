package net.thaumcraft.entity.golem.ai;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.entity.GolemEntity;
import net.thaumcraft.entity.golem.GolemHelper;
import net.thaumcraft.entity.golem.Marker;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.world.CropUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * As tarefas de mexer no mundo dos golens da 4.2.3.5 ({@code thaumcraft.common.entities.ai.interact}): colher,
 * lenhar e usar a coisa que carrega nos blocos marcados. A pesca fica em {@link FishGoal}.
 */
public final class InteractGoals {
    private InteractGoals() {
    }

    /** O som de bater no bloco e as rachaduras crescendo, como no original. */
    static void chip(GolemEntity golem, Level level, BlockPos pos, BlockState state, int delay, int maxDelay) {
        golem.startActionTimer();
        var sound = state.getSoundType();
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, sound.getBreakSound(), golem.getSoundSource(),
                (sound.getVolume() + 0.7f) / 8.0f, sound.getPitch() * 0.5f);
        level.destroyBlockProgress(golem.getId(), pos, (int) (9.0f * (1.0f - (float) delay / maxDelay)));
    }

    /** O {@code AIHarvestCrops}: colhe as plantas maduras em volta da casa (e replanta, com a ordem). */
    public static class HarvestCrops extends GolemGoal {
        private BlockPos at = BlockPos.ZERO;
        private BlockState block = Blocks.AIR.defaultBlockState();
        private final float distance;
        private int delay = -1;
        private int maxDelay = 1;
        private int mod = 1;
        private int count;
        private final List<BlockPos> checklist = new ArrayList<>();

        public HarvestCrops(GolemEntity golem) {
            super(golem);
            this.distance = Mth.ceil(golem.getRange() / 4.0f);
        }

        @Override
        public boolean canUse() {
            if (this.delay >= 0 || !this.onBeat() || !this.pathDone()) return false;
            BlockPos found = this.findGrownCrop();
            if (found == null) return false;
            this.at = found;
            this.block = this.golem.level().getBlockState(found);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.golem.level().getBlockState(this.at) == this.block && this.count-- > 0 && (this.delay > 0 || !this.pathDone());
        }

        @Override
        public void tick() {
            Level level = this.golem.level();
            double dist = this.golem.distanceToSqr(this.at.getX() + 0.5, this.at.getY() + 0.5, this.at.getZ() + 0.5);
            this.golem.getLookControl().setLookAt(this.at.getX() + 0.5, this.at.getY() + 0.5, this.at.getZ() + 0.5, 30.0f, 30.0f);
            if (dist > 4.0) return;
            if (this.delay < 0) {
                this.delay = (int) Math.max(10.0f, (20.0f - this.golem.getGolemStrength() * 2.0f) * this.block.getDestroySpeed(level, this.at));
                this.maxDelay = this.delay;
                this.mod = this.delay / Math.max(1, Math.round(this.delay / 6.0f));
            }
            if (this.delay > 0) {
                if (--this.delay > 0 && this.delay % this.mod == 0 && this.pathDone()) {
                    chip(this.golem, level, this.at, this.block, this.delay, this.maxDelay);
                }
                if (this.delay == 0) {
                    this.harvest();
                    this.checkAdjacent();
                }
            }
        }

        private void checkAdjacent() {
            BlockPos home = this.golem.home();
            for (int x2 = -2; x2 <= 2; x2++) {
                for (int z2 = -2; z2 <= 2; z2++) {
                    for (int y2 = -1; y2 <= 1; y2++) {
                        BlockPos p = this.at.offset(x2, y2, z2);
                        if (Math.abs(home.getX() - p.getX()) > this.distance || Math.abs(home.getY() - p.getY()) > this.distance
                                || Math.abs(home.getZ() - p.getZ()) > this.distance) continue;
                        if (CropUtils.isGrownCrop(this.golem.level(), p)) {
                            this.at = p;
                            this.block = this.golem.level().getBlockState(p);
                            this.delay = -1;
                            this.start();
                            return;
                        }
                    }
                }
            }
        }

        @Override
        public void stop() {
            this.golem.level().destroyBlockProgress(this.golem.getId(), this.at, -1);
            this.delay = -1;
        }

        @Override
        public void start() {
            this.count = 200;
            this.moveTo(this.at.getX() + 0.5, this.at.getY() + 0.5, this.at.getZ() + 0.5);
        }

        private BlockPos findGrownCrop() {
            BlockPos home = this.golem.home();
            if (this.checklist.isEmpty()) {
                for (int a = (int) -this.distance; a <= this.distance; a++) {
                    for (int b = (int) -this.distance; b <= this.distance; b++) {
                        this.checklist.add(new BlockPos(home.getX() + a, 0, home.getZ() + b));
                    }
                }
                Collections.shuffle(this.checklist, new java.util.Random(this.golem.getRandom().nextLong()));
            }
            BlockPos column = this.checklist.removeFirst();
            for (int y = home.getY() - 3; y <= home.getY() + 3; y++) {
                BlockPos p = new BlockPos(column.getX(), y, column.getZ());
                if (CropUtils.isGrownCrop(this.golem.level(), p)) return p;
            }
            return null;
        }

        private void harvest() {
            if (!(this.golem.level() instanceof ServerLevel level)) return;
            this.count = 200;
            FakePlayer fp = FakePlayer.get(level);
            fp.snapTo(this.golem.getX(), this.golem.getY(), this.golem.getZ());
            level.destroyBlock(this.at, true);
            if (this.golem.getUpgradeAmount(4) > 0) {
                List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, this.golem.getBoundingBox().inflate(6.0),
                        e -> e.distanceTo(this.golem) <= 6.0);
                for (ItemEntity item : drops) {
                    if (item.tickCount < 2) {
                        Vec3 v = new Vec3(item.getX() - this.golem.getX(), item.getY() - this.golem.getY(), item.getZ() - this.golem.getZ()).normalize();
                        item.setDeltaMovement(-v.x / 4.0, 0.075, -v.z / 4.0);
                    }
                    boolean done = false;
                    ItemStack st = item.getItem().copy();
                    if (st.is(Items.COCOA_BEANS)) {
                        if (this.block.getBlock() instanceof CocoaBlock) {
                            BlockPos log = CropUtils.cocoaLog(this.block, this.at);
                            if (level.getBlockState(log).is(net.minecraft.tags.BlockTags.JUNGLE_LOGS)) {
                                st.shrink(1);
                                level.setBlock(this.at, Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.FACING,
                                        this.block.getValue(CocoaBlock.FACING)), 3);
                            }
                        }
                        done = true;
                    } else if (st.is(TCItems.MANA_BEAN)) {
                        if (CropUtils.manaPodFits(level, this.at)) {
                            st.shrink(1);
                            if (!this.useBean(fp, level, st)) level.setBlock(this.at, TCBlocks.MANA_POD.defaultBlockState(), 3);
                        }
                        done = true;
                    } else {
                        int[] xm = {0, 0, 1, 1, -1, 0, -1, -1, 1};
                        int[] zm = {0, 1, 0, 1, 0, -1, -1, 1, -1};
                        for (int c = 0; !st.isEmpty() && c < 9; c++) {
                            if (isSeed(st) && plant(fp, level, st, this.at.offset(xm[c], -1, zm[c]))) st.shrink(1);
                        }
                    }
                    if (st.isEmpty()) item.discard();
                    else item.setItem(st);
                    if (done) break;
                }
            }
            this.golem.startActionTimer();
        }

        private boolean useBean(FakePlayer fp, ServerLevel level, ItemStack st) {
            ItemStack one = st.copyWithCount(1);
            fp.setItemInHand(InteractionHand.MAIN_HAND, one);
            BlockPos above = this.at.above();
            var hit = new BlockHitResult(Vec3.atCenterOf(above), Direction.DOWN, above, false);
            boolean ok = one.useOn(new UseOnContext(fp, InteractionHand.MAIN_HAND, hit)).consumesAction();
            fp.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            return ok;
        }

        /** O {@code IPlantable}/{@code ItemSeedFood} de então: as sementes e os legumes que se plantam. */
        private static boolean isSeed(ItemStack st) {
            if (!(st.getItem() instanceof BlockItem bi)) return false;
            Block b = bi.getBlock();
            return b instanceof CropBlock || b instanceof StemBlock || b instanceof NetherWartBlock || b instanceof PitcherCropBlock;
        }

        private static boolean plant(FakePlayer fp, ServerLevel level, ItemStack st, BlockPos soil) {
            ItemStack one = st.copyWithCount(1);
            fp.setItemInHand(InteractionHand.MAIN_HAND, one);
            var hit = new BlockHitResult(Vec3.atCenterOf(soil).add(0, 0.5, 0), Direction.UP, soil, false);
            boolean ok = one.useOn(new UseOnContext(fp, InteractionHand.MAIN_HAND, hit)).consumesAction();
            fp.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            return ok;
        }
    }

    /** O {@code AIHarvestLogs}: derruba as árvores em volta da casa, tronco por tronco, do mais longe para perto. */
    public static class HarvestLogs extends GolemGoal {
        private BlockPos at = BlockPos.ZERO;
        private BlockState block = Blocks.AIR.defaultBlockState();
        private final float distance;
        private int delay = -1;
        private int maxDelay = 1;
        private int mod = 1;
        private int count;

        public HarvestLogs(GolemEntity golem) {
            super(golem);
            this.distance = Mth.ceil(golem.getRange() / 3.0f);
        }

        @Override
        public boolean canUse() {
            if (this.delay >= 0 || !this.onBeat() || !this.pathDone()) return false;
            BlockPos found = this.findLog();
            if (found == null) return false;
            this.at = found;
            this.block = this.golem.level().getBlockState(found);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.golem.level().getBlockState(this.at) == this.block && this.count-- > 0
                    && (this.delay > 0 || CropUtils.isWoodLog(this.golem.level(), this.at) || !this.pathDone());
        }

        @Override
        public void tick() {
            Level level = this.golem.level();
            double dist = this.golem.distanceToSqr(this.at.getX() + 0.5, this.at.getY() + 0.5, this.at.getZ() + 0.5);
            this.golem.getLookControl().setLookAt(this.at.getX() + 0.5, this.at.getY() + 0.5, this.at.getZ() + 0.5, 30.0f, 30.0f);
            if (dist > 4.0) return;
            if (this.delay < 0) {
                this.delay = (int) Math.max(5.0f, (20.0f - this.golem.getGolemStrength() * 3.0f) * this.block.getDestroySpeed(level, this.at));
                this.maxDelay = this.delay;
                this.mod = this.delay / Math.max(1, Math.round(this.delay / 6.0f));
            }
            if (this.delay > 0) {
                if (--this.delay > 0 && this.delay % this.mod == 0 && this.pathDone()) {
                    chip(this.golem, level, this.at, this.block, this.delay, this.maxDelay);
                }
                if (this.delay == 0) {
                    this.harvest();
                    if (CropUtils.isWoodLog(level, this.at)) {
                        this.delay = -1;
                        this.block = level.getBlockState(this.at);
                        this.start();
                    } else {
                        this.checkAdjacent();
                    }
                }
            }
        }

        private void checkAdjacent() {
            BlockPos home = this.golem.home();
            for (int x2 = -1; x2 <= 1; x2++) {
                for (int z2 = -1; z2 <= 1; z2++) {
                    for (int y2 = -1; y2 <= 1; y2++) {
                        BlockPos p = this.at.offset(x2, y2, z2);
                        if (Math.abs(home.getX() - p.getX()) > this.distance || Math.abs(home.getY() - p.getY()) > this.distance
                                || Math.abs(home.getZ() - p.getZ()) > this.distance) continue;
                        if (CropUtils.isWoodLog(this.golem.level(), p)) {
                            this.at = p;
                            this.block = this.golem.level().getBlockState(p);
                            this.delay = -1;
                            this.start();
                            return;
                        }
                    }
                }
            }
        }

        @Override
        public void stop() {
            this.golem.level().destroyBlockProgress(this.golem.getId(), this.at, -1);
            this.delay = -1;
        }

        @Override
        public void start() {
            this.count = 200;
            this.moveTo(this.at.getX() + 0.5, this.at.getY() + 0.5, this.at.getZ() + 0.5);
        }

        private void harvest() {
            if (!(this.golem.level() instanceof ServerLevel level)) return;
            this.count = 200;
            level.levelEvent(2001, this.at, Block.getId(this.block));
            CropUtils.breakFurthestBlock(level, this.at, this.block.getBlock(), FakePlayer.get(level));
            this.golem.startActionTimer();
        }

        private BlockPos findLog() {
            var rand = this.golem.getRandom();
            BlockPos home = this.golem.home();
            for (int i = 0; i < this.distance * 4.0f; i++) {
                int x = (int) (home.getX() + rand.nextInt((int) (1.0f + this.distance * 2.0f)) - this.distance);
                int y = (int) (home.getY() + rand.nextInt((int) (1.0f + this.distance)) - this.distance / 2.0f);
                int z = (int) (home.getZ() + rand.nextInt((int) (1.0f + this.distance * 2.0f)) - this.distance);
                BlockPos p = new BlockPos(x, y, z);
                if (CropUtils.isWoodLog(this.golem.level(), p)) {
                    BlockPos v = p;
                    double dist = this.golem.distanceToSqr(x + 0.5, y + 0.5, z + 0.5);
                    for (int yy = 1; CropUtils.isWoodLog(this.golem.level(), p.below(yy))
                            && this.golem.distanceToSqr(x + 0.5, y - yy + 0.5, z + 0.5) < dist; yy++) {
                        v = p.below(yy);
                        dist = this.golem.distanceToSqr(x + 0.5, y - yy + 0.5, z + 0.5);
                    }
                    return v;
                }
            }
            return null;
        }
    }

    /** O {@code AIUseItem}: usa (ou bate com) o que carrega nos blocos marcados, como um jogador faria. */
    public static class UseItem extends GolemGoal {
        private BlockPos at = BlockPos.ZERO;
        private BlockState block = Blocks.AIR.defaultBlockState();
        private int count;
        private int color = -1;
        private int nextTick;

        public UseItem(GolemEntity golem) {
            super(golem);
            this.nextTick = golem.tickCount + golem.level().getRandom().nextInt(6);
        }

        private boolean ignoreItem() {
            return !(this.golem.level().getBlockEntity(this.golem.homeContainer()) instanceof net.minecraft.world.Container);
        }

        @Override
        public boolean canUse() {
            int d = Math.max(1, 5 - this.golem.tickCount);
            if ((!this.golem.itemCarried.isEmpty() || this.ignoreItem()) && this.golem.tickCount >= this.nextTick && this.pathDone()) {
                this.nextTick = this.golem.tickCount + d * 3;
                return this.findSomething();
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.golem.level().getBlockState(this.at) == this.block && this.count-- > 0 && !this.pathDone();
        }

        @Override
        public void tick() {
            this.golem.getLookControl().setLookAt(this.at.getX() + 0.5, this.at.getY() + 0.5, this.at.getZ() + 0.5, 30.0f, 30.0f);
            double dist = this.golem.distanceToSqr(this.at.getX() + 0.5, this.at.getY() + 0.5, this.at.getZ() + 0.5);
            if (dist <= 4.0) this.click();
        }

        @Override
        public void stop() {
            this.count = 0;
            this.golem.getNavigation().stop();
        }

        @Override
        public void start() {
            this.count = 200;
            this.moveTo(this.at.getX() + 0.5, this.at.getY() + 0.5, this.at.getZ() + 0.5);
        }

        private void click() {
            if (!(this.golem.level() instanceof ServerLevel level)) return;
            boolean ignoreItem = this.ignoreItem();
            FakePlayer player = FakePlayer.get(level);
            player.snapTo(this.golem.getX(), this.golem.getY(), this.golem.getZ(), this.golem.getYRot(), this.golem.getXRot());
            player.getInventory().clearContent();
            player.setItemInHand(InteractionHand.MAIN_HAND, this.golem.itemCarried);
            player.setShiftKeyDown(this.golem.getToggles()[2]);
            List<Integer> sides = GolemHelper.getMarkedSides(this.golem, this.at, level, (byte) this.color);
            if (sides.isEmpty()) return;
            int side = sides.getFirst();
            BlockPos target = this.at;
            if (level.isEmptyBlock(this.at)) target = this.at.relative(Direction.from3DDataValue(side).getOpposite());
            if (this.golem.itemCarried.isEmpty() && !ignoreItem) {
                this.stop();
                return;
            }
            try {
                Direction face = Direction.from3DDataValue(side);
                if (this.golem.getToggles()[1]) {
                    this.golem.startLeftArmTimer();
                    player.gameMode.handleBlockBreakAction(target, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, face, level.getMaxY(), 0);
                } else {
                    var hit = new BlockHitResult(Vec3.atCenterOf(target), face, target, false);
                    if (player.gameMode.useItemOn(player, level, this.golem.itemCarried, InteractionHand.MAIN_HAND, hit).consumesAction()) {
                        this.golem.startRightArmTimer();
                    }
                }
                this.golem.itemCarried = player.getMainHandItem();
                if (this.golem.itemCarried.getCount() <= 0) this.golem.itemCarried = ItemStack.EMPTY;
                var inv = player.getInventory();
                for (int a = 1; a < inv.getContainerSize(); a++) {
                    ItemStack in = inv.getItem(a);
                    if (in.isEmpty()) continue;
                    if (this.golem.itemCarried.isEmpty()) this.golem.itemCarried = in.copy();
                    else player.drop(in.copy(), false);
                    inv.setItem(a, ItemStack.EMPTY);
                }
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                player.setShiftKeyDown(false);
                this.golem.updateCarried();
                this.stop();
            } catch (Exception e) {
                this.stop();
            }
        }

        private boolean findSomething() {
            Level level = this.golem.level();
            for (byte col : this.golem.getColorsMatching(this.golem.itemCarried)) {
                for (Marker marker : this.golem.getMarkers()) {
                    boolean air = level.isEmptyBlock(marker.pos());
                    if ((marker.color() == col || col == -1) && (!this.golem.getToggles()[0] || air) && (this.golem.getToggles()[0] || !air)) {
                        if (level.isEmptyBlock(marker.pos().relative(marker.direction()))) {
                            this.color = col;
                            this.at = marker.pos();
                            this.block = level.getBlockState(this.at);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    /** Uma criatura dentro do raio de uma posição (o {@code EntityUtils.getEntitiesInRange} de então). */
    static List<Entity> inRange(Level level, Entity center, double range) {
        return level.getEntities(center, center.getBoundingBox().inflate(range), e -> e.distanceTo(center) <= range);
    }
}
