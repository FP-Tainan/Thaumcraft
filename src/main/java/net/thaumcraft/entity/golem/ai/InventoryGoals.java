package net.thaumcraft.entity.golem.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.entity.GolemEntity;
import net.thaumcraft.entity.golem.GolemHelper;
import net.thaumcraft.entity.golem.InventoryUtils;
import net.thaumcraft.entity.golem.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * As tarefas de inventário dos golens da 4.2.3.5 ({@code thaumcraft.common.entities.ai.inventory}): levar para casa,
 * buscar o que falta, esvaziar a casa nos baús marcados, juntar do chão e separar.
 */
public final class InventoryGoals {
    private InventoryGoals() {
    }

    /** O baú da casa, e a outra metade dele se for duplo, na ordem em que o original tenta. */
    static List<Container> homeContainers(GolemEntity golem) {
        List<Container> out = new ArrayList<>();
        BlockEntity tile = golem.level().getBlockEntity(golem.homeContainer());
        if (tile instanceof Container c) out.add(c);
        BlockEntity dc = InventoryUtils.getDoubleChest(tile);
        if (dc instanceof Container c2) out.add(c2);
        return out;
    }

    /** Joga o que carrega na direção de um bloco (o {@code EntityItem} com o empurrão do original). */
    static void throwCarried(GolemEntity golem, BlockPos at) {
        ItemEntity item = new ItemEntity(golem.level(), golem.getX(), golem.getY() + golem.getBbHeight() / 2.0f, golem.getZ(), golem.itemCarried.copy());
        double distance = Math.sqrt(golem.distanceToSqr(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5));
        item.setDeltaMovement((at.getX() + 0.5 - golem.getX()) * (distance / 3.0),
                0.1 + (at.getY() + 0.5 - (golem.getY() + golem.getBbHeight() / 2.0f)) * (distance / 3.0),
                (at.getZ() + 0.5 - golem.getZ()) * (distance / 3.0));
        item.setPickUpDelay(10);
        golem.level().addFreshEntity(item);
        golem.itemCarried = ItemStack.EMPTY;
        golem.startActionTimer();
    }

    /** O {@code AIHomeReplace}: devolve à casa o que não tem para onde ir. */
    public static class HomeReplace extends GolemGoal.WithChest {
        public HomeReplace(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            if (this.golem.getCarried().isEmpty() || !this.onBeat() || !this.pathDone() || !this.nearHome(5.0)) return false;
            if (GolemHelper.isOnTimeOut(this.golem, this.golem.getCarried())) return true;
            switch (this.golem.getCore()) {
                case 1:
                    return !GolemHelper.findSomethingEmptyCore(this.golem, this.golem.getCarried());
                case 8:
                    return !GolemHelper.findSomethingUseCore(this.golem, this.golem.getCarried());
                case 10:
                    return !GolemHelper.findSomethingSortCore(this.golem, this.golem.getCarried());
                default:
                    List<ItemStack> neededList = GolemHelper.getItemsNeeded(this.golem, this.golem.getUpgradeAmount(5) > 0);
                    if (neededList == null || neededList.isEmpty()) return false;
                    for (ItemStack stack : neededList) {
                        if (InventoryUtils.areItemStacksEqual(stack, this.golem.itemCarried, this.golem.checkOreDict(),
                                this.golem.ignoreDamage(), this.golem.ignoreNBT())) return false;
                    }
                    return true;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() || this.countChest > 0;
        }

        @Override
        public void start() {
            putHome(this);
        }
    }

    /** Põe no baú da casa o que carrega (o miolo do {@code startExecuting} do HomeReplace e do HomePlace). */
    static void putHome(GolemGoal.WithChest goal) {
        GolemEntity golem = goal.golem;
        int side = golem.homeFacing().get3DDataValue();
        for (Container tile : homeContainers(golem)) {
            ItemStack result = InventoryUtils.placeItemStackIntoInventory(golem.getCarried(), tile, side, true);
            if (!ItemStack.matches(result, golem.itemCarried)) {
                golem.setCarried(result);
                goal.opened(tile);
                break;
            }
        }
    }

    /** O {@code AIHomePlace}: guarda na casa o que trouxe. */
    public static class HomePlace extends GolemGoal.WithChest {
        public HomePlace(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            if (this.golem.getCarried().isEmpty() || !this.onBeat() || !this.pathDone() || !this.nearHome(5.0)) return false;
            int side = this.golem.homeFacing().get3DDataValue();
            for (Container tile : homeContainers(this.golem)) {
                ItemStack result = InventoryUtils.placeItemStackIntoInventory(this.golem.getCarried(), tile, side, false);
                if (!ItemStack.matches(result, this.golem.itemCarried)) return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() || this.countChest > 0;
        }

        @Override
        public void start() {
            putHome(this);
        }
    }

    /** O {@code AIHomeDrop}: sem baú na casa, joga o que trouxe no lugar dele. */
    public static class HomeDrop extends GolemGoal.WithChest {
        public HomeDrop(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            if (this.golem.getCarried().isEmpty() || !this.pathDone() || !this.nearHome(5.0)) return false;
            return !(this.golem.level().getBlockEntity(this.golem.homeContainer()) instanceof Container);
        }

        @Override
        public boolean canContinueToUse() {
            return this.count > 0 && (this.canUse() || this.countChest > 0);
        }

        @Override
        public void tick() {
            super.tick();
            this.count--;
        }

        @Override
        public void start() {
            this.count = 200;
            throwCarried(this.golem, this.golem.homeContainer());
            this.golem.updateCarried();
        }
    }

    /** O {@code AIFillTake}: tira do baú marcado ao lado o que a casa pede. */
    public static class FillTake extends GolemGoal.WithChest {
        public FillTake(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            if (!this.golem.getCarried().isEmpty() || this.golem.itemWatched.isEmpty() || !this.pathDone() || !this.golem.hasSomething()) return false;
            BlockPos c = this.golem.homeContainer();
            for (Container te : GolemHelper.getMarkedContainersAdjacentToGolem(this.golem.level(), this.golem)) {
                BlockEntity tile = (BlockEntity) te;
                if (tile.getBlockPos().equals(c)) continue;
                for (byte color : this.golem.getColorsMatching(this.golem.itemWatched)) {
                    for (int side : GolemHelper.getMarkedSides(this.golem, tile, color)) {
                        ItemStack target = this.golem.itemWatched.copy();
                        target.setCount(this.golem.getToggles()[0] ? this.golem.getCarrySpace() : Math.min(target.getCount(), this.golem.getCarrySpace()));
                        ItemStack result = InventoryUtils.extractStack(te, target, side, this.golem.checkOreDict(), this.golem.ignoreDamage(),
                                this.golem.ignoreNBT(), true);
                        Container dc = InventoryUtils.getDoubleChest(tile);
                        if (result.isEmpty() && dc != null) {
                            result = InventoryUtils.extractStack(dc, target, side, this.golem.checkOreDict(), this.golem.ignoreDamage(),
                                    this.golem.ignoreNBT(), true);
                        }
                        if (!result.isEmpty()) {
                            this.golem.setCarried(result);
                            this.opened(te);
                            this.count = 200;
                            this.golem.itemWatched = ItemStack.EMPTY;
                            this.golem.updateCarried();
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.count > 0 && (!this.pathDone() || this.countChest > 0);
        }

        @Override
        public void tick() {
            super.tick();
            this.count--;
        }
    }

    /** O {@code AIFillGoto}: vai ao baú marcado que tem o que falta na casa. */
    public static class FillGoto extends GolemGoal.Goto {
        public FillGoto(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            if (!this.golem.getCarried().isEmpty() || !this.onBeat() || !this.golem.hasSomething()) return false;
            List<ItemStack> mi = GolemHelper.getMissingItems(this.golem);
            if (mi == null || mi.isEmpty()) return false;
            List<ItemStack> missingItems = new ArrayList<>();
            if (this.golem.getUpgradeAmount(5) > 0) {
                for (ItemStack stack : mi) {
                    var tags = InventoryUtils.commonTags(stack.getItem());
                    if (!tags.isEmpty()) {
                        for (var holder : net.minecraft.core.registries.BuiltInRegistries.ITEM.getTagOrEmpty(tags.getFirst())) {
                            missingItems.add(new ItemStack(holder.value()));
                        }
                    } else {
                        missingItems.add(stack.copy());
                    }
                }
            } else {
                for (ItemStack stack : mi) missingItems.add(stack.copy());
            }
            List<Container> results = new ArrayList<>();
            for (ItemStack stack : missingItems) {
                this.golem.itemWatched = stack.copy();
                for (byte color : this.golem.getColorsMatching(this.golem.itemWatched)) {
                    results = GolemHelper.getContainersWithGoods(this.golem.level(), this.golem, this.golem.itemWatched, color);
                }
                if (!results.isEmpty()) break;
            }
            if (results.isEmpty()) return false;
            BlockPos c = this.golem.homeContainer();
            BlockPos dest = null;
            double range = Double.MAX_VALUE;
            float dmod = this.golem.getRange();
            for (Container i : results) {
                BlockPos p = InventoryUtils.posOf(i);
                double distance = this.golem.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                if (distance < range && distance <= dmod * dmod && !p.equals(c)) {
                    range = distance;
                    dest = p;
                }
            }
            if (dest == null) return false;
            this.movePosX = dest.getX();
            this.movePosY = dest.getY();
            this.movePosZ = dest.getZ();
            return true;
        }
    }

    /** O {@code AIEmptyPlace}: guarda no baú marcado ao lado o que tirou da casa. */
    public static class EmptyPlace extends GolemGoal.WithChest {
        private BlockPos at = BlockPos.ZERO;

        public EmptyPlace(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            if (this.golem.itemCarried.isEmpty() || !this.pathDone()) return false;
            BlockPos c = this.golem.homeContainer();
            for (Container te : GolemHelper.getMarkedContainersAdjacentToGolem(this.golem.level(), this.golem)) {
                BlockEntity tile = (BlockEntity) te;
                if (tile.getBlockPos().equals(c)) continue;
                for (byte color : this.golem.getColorsMatching(this.golem.itemCarried)) {
                    for (int side : GolemHelper.getMarkedSides(this.golem, tile, color)) {
                        ItemStack is = InventoryUtils.placeItemStackIntoInventory(this.golem.itemCarried, te, side, false);
                        if (!ItemStack.matches(is, this.golem.itemCarried)) {
                            this.at = tile.getBlockPos();
                            return true;
                        }
                    }
                    Container dc = InventoryUtils.getDoubleChest(tile);
                    if (dc != null) {
                        for (int side : GolemHelper.getMarkedSides(this.golem, tile, color)) {
                            ItemStack is = InventoryUtils.placeItemStackIntoInventory(this.golem.itemCarried, dc, side, false);
                            // o original tem aqui a condição trocada (só vale se NÃO couber nada na outra metade)
                            if (ItemStack.matches(is, this.golem.itemCarried)) {
                                this.at = tile.getBlockPos();
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.count > 0 && (this.canUse() || this.countChest > 0);
        }

        @Override
        public void tick() {
            super.tick();
            this.count--;
        }

        @Override
        public void start() {
            this.count = 200;
            placeInto(this, this.at, true);
            this.golem.updateCarried();
        }
    }

    /** O miolo do {@code startExecuting} do EmptyPlace e do SortingPlace: põe pelas faces marcadas de cada cor. */
    static void placeInto(GolemGoal.WithChest goal, BlockPos at, boolean unused) {
        GolemEntity golem = goal.golem;
        BlockEntity tile = golem.level().getBlockEntity(at);
        if (tile == null || at.equals(golem.homeContainer()) || !(tile instanceof Container te)) return;
        for (byte color : golem.getColorsMatching(golem.itemCarried)) {
            for (int side : GolemHelper.getMarkedSides(golem, tile, color)) {
                golem.itemCarried = InventoryUtils.placeItemStackIntoInventory(golem.itemCarried, te, side, true);
                goal.countChest = 5;
                goal.inv = te;
                if (golem.itemCarried.isEmpty()) break;
            }
            Container dc = InventoryUtils.getDoubleChest(tile);
            if (dc != null && !golem.itemCarried.isEmpty()) {
                for (int side : GolemHelper.getMarkedSides(golem, tile, color)) {
                    ItemStack is = InventoryUtils.placeItemStackIntoInventory(golem.itemCarried, dc, side, false);
                    if (!ItemStack.matches(is, golem.itemCarried)) {
                        golem.itemCarried = InventoryUtils.placeItemStackIntoInventory(golem.itemCarried, dc, side, true);
                        goal.countChest = 5;
                        goal.inv = dc;
                        if (golem.itemCarried.isEmpty()) break;
                    }
                }
            }
            if (goal.countChest == 5) {
                golem.openChest(te);
                break;
            }
        }
    }

    /** O {@code AIEmptyDrop}: joga o que carrega no bloco marcado que não é baú. */
    public static class EmptyDrop extends GolemGoal {
        private int count;

        public EmptyDrop(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            if (this.golem.itemCarried.isEmpty() || !this.pathDone()) return false;
            BlockPos home = this.golem.home();
            for (byte color : this.golem.getColorsMatching(this.golem.itemCarried)) {
                for (BlockPos cc : GolemHelper.getMarkedBlocksAdjacentToGolem(this.golem.level(), this.golem, color)) {
                    if (!cc.equals(home)) return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.count > 0 && this.canUse();
        }

        @Override
        public void tick() {
            this.count--;
        }

        @Override
        public void start() {
            this.count = 200;
            BlockPos home = this.golem.home();
            outer:
            for (byte color : this.golem.getColorsMatching(this.golem.itemCarried)) {
                for (BlockPos cc : GolemHelper.getMarkedBlocksAdjacentToGolem(this.golem.level(), this.golem, color)) {
                    if (!cc.equals(home)) {
                        throwCarried(this.golem, cc);
                        break outer;
                    }
                }
            }
            this.golem.updateCarried();
        }
    }

    /** O {@code AIEmptyGoto}: vai ao baú marcado (ou ao bloco marcado) onde cabe o que carrega. */
    public static class EmptyGoto extends GolemGoal.Goto {
        public EmptyGoto(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            if (this.golem.itemCarried.isEmpty() || !this.onBeat()) return false;
            List<Byte> matchingColors = this.golem.getColorsMatching(this.golem.itemCarried);
            BlockPos c = this.golem.homeContainer();
            float dmod = this.golem.getRange();
            for (byte color : matchingColors) {
                List<Container> results = GolemHelper.getContainersWithRoom(this.golem.level(), this.golem, color);
                if (results.isEmpty()) continue;
                BlockPos dest = null;
                double range = Double.MAX_VALUE;
                for (Container te : results) {
                    BlockPos p = InventoryUtils.posOf(te);
                    double distance = this.golem.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                    if (distance < range && distance <= dmod * dmod && !p.equals(c)) {
                        range = distance;
                        dest = p;
                    }
                }
                if (dest != null) {
                    this.movePosX = dest.getX();
                    this.movePosY = dest.getY();
                    this.movePosZ = dest.getZ();
                    return true;
                }
            }
            for (byte color : matchingColors) {
                for (Marker marker : this.golem.getMarkers()) {
                    if ((marker.color() == color || color == -1) && !(this.golem.level().getBlockEntity(marker.pos()) instanceof Container)) {
                        this.movePosX = marker.x();
                        this.movePosY = marker.y();
                        this.movePosZ = marker.z();
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /** O {@code AIHomeTake}: tira da casa o que tem para onde levar. */
    public static class HomeTake extends GolemGoal.WithChest {
        public HomeTake(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            if (!this.golem.getCarried().isEmpty() || !this.onBeat() || !this.pathDone() || !this.nearHome(5.0)) return false;
            int side = this.golem.homeFacing().get3DDataValue();
            for (Container tile : homeContainers(this.golem)) {
                List<ItemStack> neededList = GolemHelper.getItemsNeeded(this.golem, this.golem.getUpgradeAmount(5) > 0);
                if (neededList == null) {
                    ItemStack is;
                    do {
                        is = GolemHelper.getFirstItemUsingTimeout(this.golem, tile, side, false);
                        if (!is.isEmpty() && GolemHelper.validTargetForItem(this.golem, is)) {
                            ItemStack result = GolemHelper.getFirstItemUsingTimeout(this.golem, tile, side, true);
                            this.golem.setCarried(result);
                            this.opened(tile);
                            return true;
                        }
                    } while (!is.isEmpty());
                    return false;
                }
                for (ItemStack stack : neededList) {
                    if (!GolemHelper.validTargetForItem(this.golem, stack)) continue;
                    ItemStack needed = stack.copyWithCount(Math.max(1, this.golem.getCarrySpace()));
                    ItemStack result = InventoryUtils.extractStack(tile, needed, side, this.golem.checkOreDict(), this.golem.ignoreDamage(),
                            this.golem.ignoreNBT(), true);
                    if (!result.isEmpty()) {
                        this.golem.setCarried(result);
                        this.opened(tile);
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.countChest > 0;
        }
    }

    /** O {@code AIHomeTakeSorting}: tira da casa o que algum baú marcado já tem, para separar. */
    public static class HomeTakeSorting extends GolemGoal.WithChest {
        public HomeTakeSorting(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            if (!this.golem.getCarried().isEmpty() || !this.onBeat() || !this.pathDone() || !this.nearHome(5.0)) return false;
            int side = this.golem.homeFacing().get3DDataValue();
            for (Container tile : homeContainers(this.golem)) {
                List<ItemStack> neededList = GolemHelper.getItemsNeeded(this.golem, this.golem.getUpgradeAmount(5) > 0);
                if (neededList == null) continue;
                for (ItemStack stack : neededList) {
                    ItemStack needed = stack.copyWithCount(Math.max(1, this.golem.getCarrySpace()));
                    if (!InventoryUtils.extractStack(tile, needed, side, this.golem.checkOreDict(), this.golem.ignoreDamage(),
                            this.golem.ignoreNBT(), false).isEmpty()) return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.count > 0 && (this.canUse() || this.countChest > 0);
        }

        @Override
        public void tick() {
            super.tick();
            this.count--;
        }

        @Override
        public void start() {
            this.count = 200;
            int side = this.golem.homeFacing().get3DDataValue();
            for (Container tile : homeContainers(this.golem)) {
                List<ItemStack> neededList = GolemHelper.getItemsNeeded(this.golem, this.golem.getUpgradeAmount(5) > 0);
                if (neededList != null) {
                    for (ItemStack stack : neededList) {
                        ItemStack needed = stack.copyWithCount(Math.max(1, this.golem.getCarrySpace()));
                        ItemStack result = InventoryUtils.extractStack(tile, needed, side, this.golem.checkOreDict(), this.golem.ignoreDamage(),
                                this.golem.ignoreNBT(), true);
                        if (!result.isEmpty()) {
                            this.golem.setCarried(result);
                            this.opened(tile);
                            break;
                        }
                    }
                }
                if (!this.golem.getCarried().isEmpty()) break;
            }
        }
    }

    /** O {@code AIItemPickup}: junta do chão, em volta da casa, o que as casas dele pedem (ou tudo). */
    public static class ItemPickup extends GolemGoal {
        private Entity targetEntity;
        private int count;

        public ItemPickup(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            return this.onBeat() && this.findItem();
        }

        private boolean findItem() {
            double range = Double.MAX_VALUE;
            float dmod = this.golem.getRange();
            BlockPos h = this.golem.home();
            AABB box = new AABB(h.getX(), h.getY(), h.getZ(), h.getX() + 1, h.getY() + 1, h.getZ() + 1).inflate(dmod);
            List<Entity> targets = this.golem.level().getEntities(this.golem, box);
            if (targets.isEmpty()) return false;
            this.targetEntity = null;
            for (Entity e : targets) {
                if (!(e instanceof ItemEntity item) || ((net.thaumcraft.mixin.ItemEntityAccessor) item).thaumcraft$pickupDelay() >= 5) continue;
                ItemStack stack = item.getItem();
                boolean wanted = this.golem.inventory.allEmpty()
                        || this.golem.inventory.getAmountNeededSmart(stack, this.golem.getUpgradeAmount(5) > 0) > 0;
                boolean fits = this.golem.getCarried().isEmpty()
                        || InventoryUtils.areItemStacksEqualStrict(this.golem.getCarried(), stack) && stack.getCount() <= this.golem.getCarrySpace();
                if (!wanted || !fits) continue;
                double distance = e.distanceToSqr(h.getX() + 0.5, h.getY() + 0.5, h.getZ() + 0.5);
                double distance2 = e.distanceToSqr(this.golem);
                if (distance2 < range && distance <= dmod * dmod) {
                    range = distance2;
                    this.targetEntity = e;
                }
            }
            return this.targetEntity != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.count-- > 0 && !this.pathDone() && this.targetEntity.isAlive();
        }

        @Override
        public void stop() {
            this.count = 0;
            this.targetEntity = null;
            this.golem.getNavigation().stop();
        }

        @Override
        public void tick() {
            this.golem.getLookControl().setLookAt(this.targetEntity, 30.0f, 30.0f);
            if (this.golem.distanceToSqr(this.targetEntity) <= 2.0) this.pickUp();
        }

        private void pickUp() {
            int amount = 0;
            if (this.targetEntity instanceof ItemEntity item) {
                ItemStack stack = item.getItem().copy();
                amount = Math.min(item.getItem().getCount(), this.golem.getCarrySpace());
                if (item.getItem().getCount() >= this.golem.getCarrySpace()) amount = this.golem.getCarrySpace();
                stack.setCount(amount);
                ItemStack left = item.getItem().copy();
                left.shrink(amount);
                if (left.isEmpty()) item.discard();
                else item.setItem(left);
                if (this.golem.getCarried().isEmpty()) this.golem.setCarried(stack);
                else {
                    this.golem.getCarried().grow(amount);
                    this.golem.updateCarried();
                }
            }
            if (amount != 0) {
                var random = this.targetEntity.level().getRandom();
                this.targetEntity.level().playSound(null, this.targetEntity.getX(), this.targetEntity.getY(), this.targetEntity.getZ(),
                        SoundEvents.ITEM_PICKUP, this.targetEntity.getSoundSource(), 0.2f,
                        ((random.nextFloat() - random.nextFloat()) * 0.7f + 1.0f) * 2.0f);
            }
        }

        @Override
        public void start() {
            this.count = 200;
            this.golem.getNavigation().moveTo(this.targetEntity, this.golem.golemSpeed());
        }
    }

    /** O {@code AISortingGoto}: vai ao baú marcado que já tem aquela coisa e tem lugar. */
    public static class SortingGoto extends GolemGoal.Goto {
        public SortingGoto(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            if (this.golem.itemCarried.isEmpty() || !this.onBeat()) return false;
            List<Container> results = GolemHelper.getContainersWithRoom(this.golem.level(), this.golem, (byte) -1);
            if (results.isEmpty()) return false;
            BlockPos c = this.golem.homeContainer();
            BlockPos dest = null;
            double range = Double.MAX_VALUE;
            float dmod = this.golem.getRange();
            for (Container te : results) {
                BlockPos p = InventoryUtils.posOf(te);
                double distance = this.golem.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                if (distance < range && distance <= dmod * dmod && !p.equals(c)) {
                    for (int side : GolemHelper.getMarkedSides(this.golem, (BlockEntity) te, (byte) -1)) {
                        if (InventoryUtils.inventoryContains(te, this.golem.itemCarried, side, this.golem.checkOreDict(),
                                this.golem.ignoreDamage(), this.golem.ignoreNBT())) {
                            dest = p;
                            range = distance;
                            break;
                        }
                    }
                }
            }
            if (dest == null) return false;
            this.movePosX = dest.getX();
            this.movePosY = dest.getY();
            this.movePosZ = dest.getZ();
            return true;
        }
    }

    /** O {@code AISortingPlace}: guarda no baú marcado ao lado que já tem aquela coisa. */
    public static class SortingPlace extends GolemGoal.WithChest {
        private BlockPos at = BlockPos.ZERO;

        public SortingPlace(GolemEntity golem) {
            super(golem);
        }

        @Override
        public boolean canUse() {
            if (this.golem.itemCarried.isEmpty() || !this.pathDone()) return false;
            BlockPos c = this.golem.homeContainer();
            for (Container te : GolemHelper.getMarkedContainersAdjacentToGolem(this.golem.level(), this.golem)) {
                BlockEntity tile = (BlockEntity) te;
                if (tile.getBlockPos().equals(c)) continue;
                for (int side : GolemHelper.getMarkedSides(this.golem, tile, (byte) -1)) {
                    ItemStack is = InventoryUtils.placeItemStackIntoInventory(this.golem.itemCarried, te, side, false);
                    if (!ItemStack.matches(is, this.golem.itemCarried) && InventoryUtils.inventoryContains(te, this.golem.itemCarried, side,
                            this.golem.checkOreDict(), this.golem.ignoreDamage(), this.golem.ignoreNBT())) {
                        this.at = tile.getBlockPos();
                        return true;
                    }
                }
                Container dc = InventoryUtils.getDoubleChest(tile);
                if (dc != null) {
                    for (int side : GolemHelper.getMarkedSides(this.golem, tile, (byte) -1)) {
                        ItemStack is = InventoryUtils.placeItemStackIntoInventory(this.golem.itemCarried, dc, side, false);
                        if (!ItemStack.matches(is, this.golem.itemCarried) && InventoryUtils.inventoryContains(te, this.golem.itemCarried, side,
                                this.golem.checkOreDict(), this.golem.ignoreDamage(), this.golem.ignoreNBT())) {
                            this.at = tile.getBlockPos();
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.count > 0 && (this.canUse() || this.countChest > 0);
        }

        @Override
        public void tick() {
            super.tick();
            this.count--;
        }

        @Override
        public void start() {
            this.count = 200;
            placeInto(this, this.at, true);
            this.golem.updateCarried();
        }
    }
}
