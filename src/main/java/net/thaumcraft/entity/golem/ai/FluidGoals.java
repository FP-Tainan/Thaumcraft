package net.thaumcraft.entity.golem.ai;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.block.entity.AlembicBlockEntity;
import net.thaumcraft.block.entity.EssentiaReservoirBlockEntity;
import net.thaumcraft.block.entity.JarBlockEntity;
import net.thaumcraft.entity.GolemEntity;
import net.thaumcraft.entity.golem.GolemHelper;
import net.thaumcraft.entity.golem.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * As tarefas de líquido e de essência dos golens da 4.2.3.5 ({@code thaumcraft.common.entities.ai.fluid}): o golem de
 * decantação leva líquido das fontes e tanques marcados para o tanque da casa; o alquimista leva a essência dos
 * alambiques e jarros da casa para os jarros marcados.
 */
public final class FluidGoals {
    private static final long MB = FluidConstants.BUCKET / 1000;

    private FluidGoals() {
    }

    private static void swim(GolemEntity golem, float volume) {
        Level level = golem.level();
        level.playSound(null, golem.getX(), golem.getY(), golem.getZ(), SoundEvents.GENERIC_SWIM, golem.getSoundSource(), volume,
                1.0f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3f);
    }

    private static void carry(GolemEntity golem, Fluid fluid, long mb) {
        if (golem.fluidAmount > 0 && !golem.fluidCarried.isBlank()) {
            golem.fluidAmount += (int) mb;
        } else {
            golem.fluidCarried = FluidVariant.of(fluid);
            golem.fluidAmount = (int) mb;
        }
    }

    /** O {@code AILiquidGoto}: vai até a fonte ou o tanque marcado de um líquido que a casa aceita. */
    public static class LiquidGoto extends GolemGoal.Goto {
        public LiquidGoto(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            if (!this.onBeat() || !(this.golem.fluidAmount <= 0 || this.golem.fluidAmount <= this.golem.getFluidCarryLimit() - 1000)) return false;
            for (Fluid fluid : GolemHelper.getMissingLiquids(this.golem)) {
                Vec3 v = GolemHelper.findPossibleLiquid(fluid, this.golem);
                if (v == null) continue;
                // o original guarda o líquido que busca num item: basta aqui saber que há um
                this.golem.itemWatched = new ItemStack(Items.BUCKET);
                this.movePosX = v.x;
                this.movePosY = v.y;
                this.movePosZ = v.z;
                double dd = Math.sqrt(this.golem.distanceToSqr(this.movePosX, this.movePosY, this.movePosZ));
                for (int xx = -1; xx <= 1; xx++) {
                    for (int zz = -1; zz <= 1; zz++) {
                        double dd2 = Math.sqrt(this.golem.distanceToSqr(v.x + xx, this.movePosY, v.z + zz));
                        BlockPos p = BlockPos.containing(v.x + xx, this.movePosY, v.z + zz);
                        if (dd2 < dd && this.golem.level().getBlockState(p).isFaceSturdy(this.golem.level(), p, Direction.UP)) {
                            this.movePosX = v.x + xx;
                            this.movePosZ = v.z + zz;
                            dd = dd2;
                        }
                    }
                }
                return true;
            }
            return false;
        }
    }

    /** O {@code AILiquidEmpty}: despeja no tanque da casa. */
    public static class LiquidEmpty extends GolemGoal {
        public LiquidEmpty(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            if (!this.pathDone() || this.golem.fluidAmount <= 0 || this.golem.fluidCarried.isBlank() || !this.nearHome(5.0)) return false;
            for (Fluid fluid : GolemHelper.getMissingLiquids(this.golem)) {
                if (fluid == this.golem.fluidCarried.getFluid()) return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            Storage<FluidVariant> fh = GolemHelper.fluidStorage(this.golem.level(), this.golem.homeContainer(), this.golem.homeFacing());
            if (fh == null) return;
            long amt;
            try (Transaction t = Transaction.openOuter()) {
                amt = fh.insert(this.golem.fluidCarried, this.golem.fluidAmount * MB, t) / MB;
                t.commit();
            }
            this.golem.fluidAmount -= (int) amt;
            if (this.golem.fluidAmount <= 0) {
                this.golem.fluidAmount = 0;
                this.golem.fluidCarried = FluidVariant.blank();
            }
            if (amt > 200) swim(this.golem, Math.min(0.2f, 0.2f * ((float) amt / this.golem.getFluidCarryLimit())));
            this.golem.updateCarried();
            this.golem.itemWatched = ItemStack.EMPTY;
        }
    }

    /** O {@code AILiquidGather}: bebe do tanque marcado ou da fonte marcada ao lado (e com entropia, o lago inteiro). */
    public static class LiquidGather extends GolemGoal {
        private int count;
        private final Map<BlockPos, List<BlockPos>> queue = new HashMap<>();

        public LiquidGather(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            List<Fluid> fluids = GolemHelper.getMissingLiquids(this.golem);
            if (this.golem.itemWatched.isEmpty() || fluids.isEmpty() || !this.pathDone()) return false;
            Level level = this.golem.level();
            int camt = this.golem.fluidAmount;
            int max = this.golem.getFluidCarryLimit();
            for (Fluid fluid : fluids) {
                for (Marker marker : GolemHelper.getMarkedFluidHandlersAdjacentToGolem(fluid, level, this.golem)) {
                    Storage<FluidVariant> te = GolemHelper.fluidStorage(level, marker.pos(), marker.direction());
                    if (te != null && GolemHelper.drainable(te, fluid, max - camt) > 0) return true;
                }
                for (BlockPos loc : GolemHelper.getMarkedBlocksAdjacentToGolem(level, this.golem, (byte) -1)) {
                    // o bloco daquele líquido: vale se for fonte (o metadado 0 de então)
                    if (level.getFluidState(loc).getType().isSame(fluid)) return GolemHelper.isSourceOf(level, loc, fluid);
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.count < 20 && !this.golem.itemWatched.isEmpty();
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }

        @Override
        public void start() {
            this.count = 0;
        }

        @Override
        public void stop() {
            this.count = 0;
            this.golem.itemWatched = ItemStack.EMPTY;
        }

        @Override
        public void tick() {
            if (++this.count < 10) return;
            Level level = this.golem.level();
            int camt = this.golem.fluidAmount;
            int max = this.golem.getFluidCarryLimit();
            for (Fluid fluid : GolemHelper.getMissingLiquids(this.golem)) {
                for (Marker marker : GolemHelper.getMarkedFluidHandlersAdjacentToGolem(fluid, level, this.golem)) {
                    Storage<FluidVariant> te = GolemHelper.fluidStorage(level, marker.pos(), marker.direction());
                    if (te == null) continue;
                    long got;
                    try (Transaction t = Transaction.openOuter()) {
                        got = te.extract(FluidVariant.of(fluid), (long) (max - camt) * MB, t) / MB;
                        t.commit();
                    }
                    if (got > 0) {
                        carry(this.golem, fluid, got);
                        if (got > 200) swim(this.golem, 0.2f * ((float) got / max));
                        this.golem.updateCarried();
                        if (this.golem.fluidAmount >= this.golem.getFluidCarryLimit()) this.golem.itemWatched = ItemStack.EMPTY;
                        this.count = 0;
                    }
                }
                for (BlockPos loc : GolemHelper.getMarkedBlocksAdjacentToGolem(level, this.golem, (byte) -1)) {
                    BlockPos at = loc;
                    if (this.golem.getUpgradeAmount(5) > 0) {
                        List<BlockPos> t = this.queue.get(loc);
                        if (t == null || t.isEmpty()) {
                            this.rebuildQueue(loc, fluid);
                            t = this.queue.get(loc);
                        }
                        if (t != null && !t.isEmpty()) {
                            do {
                                at = t.removeFirst();
                            } while (!t.isEmpty() && !GolemHelper.isSourceOf(level, at, fluid));
                        }
                    }
                    if (GolemHelper.isSourceOf(level, at, fluid) && 1000 <= max - camt) {
                        carry(this.golem, fluid, 1000);
                        level.removeBlock(at, false);
                        swim(this.golem, 0.2f);
                        this.golem.updateCarried();
                        if (this.golem.fluidAmount > this.golem.getFluidCarryLimit() - 1000) this.golem.itemWatched = ItemStack.EMPTY;
                        this.count = 0;
                    }
                }
            }
        }

        /** Com a entropia, as fontes ligadas àquela, da mais longe para a mais perto (a bomba do original). */
        private void rebuildQueue(BlockPos origin, Fluid fluid) {
            float pumpDist = this.golem.getRange() * this.golem.getRange();
            Set<BlockPos> cache = new HashSet<>();
            List<BlockPos> sources = new ArrayList<>();
            List<BlockPos> open = new ArrayList<>();
            open.add(origin);
            cache.add(origin);
            Level level = this.golem.level();
            while (!open.isEmpty() && cache.size() < 4096) {
                BlockPos p = open.removeLast();
                for (int a = -1; a <= 1; a++) {
                    for (int b = -1; b <= 1; b++) {
                        for (int c = -1; c <= 1; c++) {
                            if (a == 0 && b == 0 && c == 0) continue;
                            BlockPos cc = p.offset(a, b, c);
                            if (cc.distSqr(origin) > pumpDist || !cache.add(cc)) continue;
                            if (!level.getFluidState(cc).getType().isSame(fluid)) continue;
                            if (GolemHelper.isSourceOf(level, cc, fluid)) sources.add(cc);
                            open.add(cc);
                        }
                    }
                }
            }
            sources.sort((x, y) -> Double.compare(y.distSqr(origin), x.distSqr(origin)));
            this.queue.put(origin, sources);
        }
    }

    /** O {@code AIEssentiaGoto}: vai até o jarro (ou tubo) marcado que tem lugar para a essência. */
    public static class EssentiaGoto extends GolemGoal.Goto {
        public EssentiaGoto(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            if (!this.onBeat() || this.golem.essentia == null || this.golem.essentiaAmount == 0) return false;
            BlockPos jar = GolemHelper.findJarWithRoom(this.golem);
            if (jar == null) return false;
            this.movePosX = jar.getX();
            this.movePosY = jar.getY();
            this.movePosZ = jar.getZ();
            return true;
        }
    }

    /** O {@code AIEssentiaEmpty}: despeja a essência no jarro marcado ao lado. */
    public static class EssentiaEmpty extends GolemGoal {
        private BlockPos jar = BlockPos.ZERO;

        public EssentiaEmpty(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            if (!this.pathDone() || this.golem.essentia == null || this.golem.essentiaAmount == 0) return false;
            BlockPos found = GolemHelper.findJarWithRoom(this.golem);
            if (found == null || this.golem.distanceToSqr(found.getX() + 0.5, found.getY() + 0.5, found.getZ() + 0.5) > 4.0) return false;
            this.jar = found;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            Level level = this.golem.level();
            BlockEntity tile = level.getBlockEntity(this.jar);
            if (tile instanceof JarBlockEntity jarTile) {
                this.golem.essentiaAmount = jarTile.addToContainer(this.golem.essentia, this.golem.essentiaAmount);
                if (this.golem.essentiaAmount == 0) this.golem.essentia = null;
                swim(this.golem, 0.2f);
                this.golem.updateCarried();
            } else if (tile instanceof EssentiaReservoirBlockEntity res) {
                Direction face = res.facing();
                if (res.getSuctionAmount(face) > 0 && (res.getSuctionType(face) == null || res.getSuctionType(face) == this.golem.essentia)) {
                    int added = res.addEssentia(this.golem.essentia, this.golem.essentiaAmount, face);
                    if (added > 0) {
                        this.golem.essentiaAmount -= added;
                        if (this.golem.essentiaAmount == 0) this.golem.essentia = null;
                        swim(this.golem, 0.2f);
                        this.golem.updateCarried();
                    }
                }
            } else if (tile instanceof EssentiaTransport trans) {
                for (int s : GolemHelper.getMarkedSides(this.golem, tile, (byte) -1)) {
                    Direction side = Direction.from3DDataValue(s);
                    if (trans.canInputFrom(side) && trans.getSuctionAmount(side) > 0
                            && (trans.getSuctionType(side) == null || trans.getSuctionType(side) == this.golem.essentia)) {
                        int added = trans.addEssentia(this.golem.essentia, this.golem.essentiaAmount, side);
                        if (added > 0) {
                            this.golem.essentiaAmount -= added;
                            if (this.golem.essentiaAmount == 0) this.golem.essentia = null;
                            swim(this.golem, 0.2f);
                            this.golem.updateCarried();
                            break;
                        }
                    }
                }
            }
        }
    }

    /** O {@code AIEssentiaGather}: bebe do alambique mais cheio da pilha da casa, ou do jarro da casa. */
    public static class EssentiaGather extends GolemGoal {
        private long delay;
        private int startAt;

        public EssentiaGather(GolemEntity golem) {
            super(golem);
        }

        private boolean accepts(EssentiaTransport etrans, Direction facing) {
            Aspect a = this.golem.essentia;
            return this.golem.essentiaAmount == 0 || (a == null || a.equals(etrans.getEssentiaType(facing)) || a.equals(etrans.getEssentiaType(null)))
                    && this.golem.essentiaAmount < this.golem.getCarryLimit();
        }

        @Override
        public boolean canUse() {
            if (!this.pathDone() || this.delay > System.currentTimeMillis()) return false;
            BlockPos c = this.golem.homeContainer();
            Direction facing = this.golem.homeFacing();
            if (this.golem.distanceToSqr(c.getX() + 0.5, c.getY() + 0.5, c.getZ() + 0.5) > 6.0) return false;
            this.startAt = 0;
            Level level = this.golem.level();
            BlockEntity te = level.getBlockEntity(c);
            if (te == null) return false;
            if (te instanceof EssentiaTransport etrans) {
                if ((te instanceof JarBlockEntity || te instanceof EssentiaReservoirBlockEntity || etrans.canOutputTo(facing))
                        && etrans.getEssentiaAmount(facing) > 0 && this.accepts(etrans, facing)) {
                    this.delay = System.currentTimeMillis() + 1000L;
                    this.startAt = 0;
                    return true;
                }
                return false;
            }
            this.startAt = -1;
            int prevTot = -1;
            for (int a = 5; a >= 0; a--) {
                if (level.getBlockEntity(c.above(a)) instanceof AlembicBlockEntity ta) {
                    Aspect g = this.golem.essentia;
                    if ((this.golem.essentiaAmount == 0 || (g == null || g.equals(ta.aspect())) && this.golem.essentiaAmount < this.golem.getCarryLimit())
                            && ta.amount() > prevTot) {
                        this.delay = System.currentTimeMillis() + 1000L;
                        this.startAt = a;
                        prevTot = ta.amount();
                    }
                }
            }
            return this.startAt >= 0;
        }

        @Override
        public void start() {
            BlockPos c = this.golem.homeContainer();
            Direction facing = this.golem.homeFacing();
            BlockEntity te = this.golem.level().getBlockEntity(c.above(this.startAt));
            if (!(te instanceof EssentiaTransport ta)) return;
            if (te instanceof AlembicBlockEntity || te instanceof JarBlockEntity) facing = Direction.UP;
            if (te instanceof EssentiaReservoirBlockEntity res) facing = res.facing();
            if (ta.getEssentiaAmount(facing) == 0) return;
            if (!ta.canOutputTo(facing) || ta.getEssentiaAmount(facing) <= 0 || !this.accepts(ta, facing)) return;
            Aspect a = ta.getEssentiaType(facing);
            if (a == null) a = ta.getEssentiaType(null);
            int qq = ta.getEssentiaAmount(facing);
            if (te instanceof EssentiaReservoirBlockEntity res) qq = res.containerContains(a);
            int am = Math.min(qq, this.golem.getCarryLimit() - this.golem.essentiaAmount);
            this.golem.essentia = a;
            int taken = a == null ? 0 : ta.takeEssentia(a, am, facing);
            if (taken > 0) {
                this.golem.essentiaAmount += taken;
                swim(this.golem, 0.05f);
                this.golem.updateCarried();
            } else {
                this.golem.essentia = null;
            }
            this.delay = System.currentTimeMillis() + 100L;
        }
    }
}
