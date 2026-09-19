package net.thaumcraft.entity.golem;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.block.entity.EssentiaReservoirBlockEntity;
import net.thaumcraft.block.entity.JarBlockEntity;
import net.thaumcraft.entity.GolemEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * O {@code GolemHelper} da 4.2.3.5: onde o golem procura baús marcados, o que falta na casa dele, que jarro tem lugar
 * para a essência, que líquido a casa aceita — e a "geladeira" dos itens que não têm para onde ir, que o golem ignora
 * por {@link #IGNORE_DELAY} milissegundos ({@code golemIgnoreDelay} do original).
 */
public final class GolemHelper {
    public static final double ADJACENT_RANGE = 4.0;
    /** O {@code golemIgnoreDelay} de fábrica: dez segundos. */
    public static final long IGNORE_DELAY = 10000L;
    /** O {@code golemDelay} de fábrica: o golem pensa a cada cinco tiques. */
    public static final int DELAY = 5;
    private static final List<SortingItemTimeout> ITEM_TIMEOUT = new ArrayList<>();

    private GolemHelper() {
    }

    // ---- baús marcados

    public static List<Container> getMarkedContainers(Level world, GolemEntity golem) {
        List<Container> results = new ArrayList<>();
        for (Marker marker : golem.getMarkers()) {
            if (!marker.in(world)) continue;
            BlockEntity te = world.getBlockEntity(marker.pos());
            if (te instanceof Container container) {
                results.add(container);
                Container dc = InventoryUtils.getDoubleChest(te);
                if (dc != null) results.add(dc);
            }
        }
        return results;
    }

    public static List<Container> getMarkedContainersAdjacentToGolem(Level world, GolemEntity golem) {
        List<Container> results = new ArrayList<>();
        for (Container inventory : getMarkedContainers(world, golem)) {
            BlockPos p = InventoryUtils.posOf(inventory);
            if (golem.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5) < ADJACENT_RANGE) {
                results.add(inventory);
                Container dc = InventoryUtils.getDoubleChest((BlockEntity) inventory);
                if (dc != null) results.add(dc);
            }
        }
        return results;
    }

    /** As marcas em blocos que não guardam nada (chão, ar), perto do golem. */
    public static List<BlockPos> getMarkedBlocksAdjacentToGolem(Level world, GolemEntity golem, byte color) {
        List<BlockPos> results = new ArrayList<>();
        for (Marker marker : golem.getMarkers()) {
            if ((marker.color() == color || color == -1) && !(world.getBlockEntity(marker.pos()) instanceof Container)
                    && golem.distanceToSqr(marker.x() + 0.5, marker.y() + 0.5, marker.z() + 0.5) < ADJACENT_RANGE) {
                results.add(marker.pos());
            }
        }
        return results;
    }

    public static List<Container> getContainersWithRoom(Level world, GolemEntity golem, byte color) {
        return getContainersWithRoom(world, golem, color, golem.getCarried());
    }

    public static List<Container> getContainersWithRoom(Level world, GolemEntity golem, byte color, ItemStack itemToMatch) {
        List<Container> results = new ArrayList<>();
        for (Container inventory : getMarkedContainers(world, golem)) {
            for (int side : getMarkedSides(golem, (BlockEntity) inventory, color)) {
                ItemStack result = InventoryUtils.placeItemStackIntoInventory(itemToMatch, inventory, side, false);
                if (!ItemStack.matches(result, itemToMatch)) {
                    results.add(inventory);
                    break;
                }
                Container dc = InventoryUtils.getDoubleChest((BlockEntity) inventory);
                if (dc != null) {
                    result = InventoryUtils.placeItemStackIntoInventory(itemToMatch, dc, side, false);
                    if (!ItemStack.matches(result, itemToMatch)) results.add(dc);
                }
            }
        }
        return results;
    }

    public static List<Integer> getMarkedSides(GolemEntity golem, BlockEntity tile, byte color) {
        return getMarkedSides(golem, tile.getBlockPos(), tile.getLevel(), color);
    }

    public static List<Integer> getMarkedSides(GolemEntity golem, BlockPos pos, Level level, byte color) {
        List<Integer> out = new ArrayList<>();
        List<Marker> gm = golem.getMarkers();
        if (gm.isEmpty()) return out;
        for (int a = 0; a < 6; a++) {
            if (contained(gm, new Marker(pos, level, a, color))) out.add(a);
        }
        return out;
    }

    public static boolean contained(List<Marker> list, Marker m) {
        for (Marker mark : list) if (m.equalsFuzzy(mark)) return true;
        return false;
    }

    public static List<Container> getContainersWithGoods(Level world, GolemEntity golem, ItemStack goods, byte color) {
        List<Container> results = new ArrayList<>();
        for (Container inventory : getMarkedContainers(world, golem)) {
            for (int side : getMarkedSides(golem, (BlockEntity) inventory, color)) {
                if (!InventoryUtils.extractStack(inventory, goods, side, golem.checkOreDict(), golem.ignoreDamage(), golem.ignoreNBT(), false).isEmpty()) {
                    results.add(inventory);
                    break;
                }
                Container dc = InventoryUtils.getDoubleChest((BlockEntity) inventory);
                if (dc != null && !InventoryUtils.extractStack(dc, goods, side, golem.checkOreDict(), golem.ignoreDamage(), golem.ignoreNBT(), false).isEmpty()) {
                    results.add(dc);
                    break;
                }
            }
        }
        return results;
    }

    /** O {@code getMissingItems}: o que o baú da casa ainda não tem, do que as casas do golem pedem. */
    @Nullable
    public static List<ItemStack> getMissingItems(GolemEntity golem) {
        Direction facing = golem.homeFacing();
        int slotCount = golem.inventory.slotCount;
        if (golem.getToggles()[0]) {
            List<ItemStack> qr = new ArrayList<>();
            for (int q = 0; q < slotCount; q++) {
                ItemStack toCheck = golem.inventory.getItem(q);
                if (!toCheck.isEmpty()) qr.add(toCheck.copy());
            }
            return qr;
        }
        BlockEntity tile = golem.level().getBlockEntity(golem.homeContainer());
        if (tile == null) return null;
        List<ItemStack> qr = new ArrayList<>();
        boolean fuzzy = golem.getUpgradeAmount(5) > 0;
        next:
        for (int q = 0; q < slotCount; q++) {
            ItemStack toCheck = golem.inventory.getItem(q);
            if (toCheck.isEmpty()) continue;
            int foundAmount = 0;
            BlockEntity at = tile;
            boolean repeat = true, didRepeat = false;
            while (repeat) {
                if (didRepeat) repeat = false;
                if (!(at instanceof Container inv)) break;
                for (int slot : InventoryUtils.slotsFor(inv, facing.get3DDataValue())) {
                    ItemStack in = inv.getItem(slot);
                    if (InventoryUtils.areItemStacksEqual(in, toCheck, golem.checkOreDict(), golem.ignoreDamage(), golem.ignoreNBT())) {
                        foundAmount += in.getCount();
                        if (foundAmount >= golem.inventory.getAmountNeededSmart(in, fuzzy)) continue next;
                    }
                }
                BlockEntity dc = InventoryUtils.getDoubleChest(at);
                if (!didRepeat && dc != null) {
                    at = dc;
                    didRepeat = true;
                } else {
                    repeat = false;
                }
            }
            ItemStack ret = toCheck.copy();
            ret.shrink(foundAmount);
            qr.add(ret);
        }
        return qr;
    }

    // ---- essência

    /** O {@code findJarWithRoom}: o jarro (ou reservatório, ou tubo marcado) para onde levar a essência que carrega. */
    @Nullable
    public static BlockPos findJarWithRoom(GolemEntity golem) {
        Level world = golem.level();
        float dmod = golem.getRange();
        dmod *= dmod;
        BlockPos home = golem.home();
        List<BlockEntity> jars = new ArrayList<>();
        List<BlockEntity> others = new ArrayList<>();
        for (Marker marker : golem.getMarkers()) {
            if (!marker.in(world)) continue;
            BlockEntity te = world.getBlockEntity(marker.pos());
            if (te instanceof JarBlockEntity) {
                if (te.getBlockPos().distSqr(home) <= dmod) jars.add(te);
            } else if (te instanceof EssentiaReservoirBlockEntity res) {
                Direction face = res.facing();
                if (res.getSuctionAmount(face) > 0 && (res.getSuctionType(face) == null || res.getSuctionType(face) == golem.essentia)
                        && te.getBlockPos().distSqr(home) <= dmod) {
                    others.add(te);
                }
            } else if (te instanceof EssentiaTransport trans) {
                Direction side = marker.direction();
                if (golem.essentia != null && golem.essentiaAmount > 0 && trans.canInputFrom(side) && trans.getSuctionAmount(side) > 0
                        && (trans.getSuctionType(side) == null || trans.getSuctionType(side) == golem.essentia)
                        && te.getBlockPos().distSqr(home) <= dmod) {
                    others.add(te);
                }
            }
        }
        Map<BlockPos, JarBlockEntity> jarlist = new LinkedHashMap<>();
        if (!jars.isEmpty()) {
            for (BlockEntity jar : jars) {
                jarlist.put(jar.getBlockPos(), (JarBlockEntity) jar);
                getConnectedJars((JarBlockEntity) jar, jarlist);
            }
        } else if (others.isEmpty()) {
            return null;
        }
        List<BlockEntity> found = new ArrayList<>(others);
        var e = golem.essentia;
        boolean carrying = e != null && golem.essentiaAmount > 0;
        // a mesma ordem de preferência do original: rotulado com a mesma essência, rotulado vazio, vazio rotulado,
        // sem rótulo com a mesma, sem rótulo vazio, e por fim os jarros do vazio
        for (JarBlockEntity jar : jarlist.values()) {
            if (jar.aspect() != null && jar.amount() > 0 && jar.amount() < JarBlockEntity.CAPACITY && jar.label() != null
                    && carrying && jar.aspect().equals(e) && jar.doesContainerAccept(e)) found.add(jar);
        }
        if (found.isEmpty()) for (JarBlockEntity jar : jarlist.values()) {
            if ((jar.aspect() == null || jar.amount() == 0) && jar.label() != null && jar.doesContainerAccept(e)) found.add(jar);
        }
        if (found.isEmpty()) for (JarBlockEntity jar : jarlist.values()) {
            if (jar.aspect() != null && jar.amount() >= JarBlockEntity.CAPACITY && jar.isVoid() && jar.label() != null
                    && carrying && jar.aspect().equals(e) && jar.doesContainerAccept(e)) found.add(jar);
        }
        if (found.isEmpty()) for (JarBlockEntity jar : jarlist.values()) {
            if (jar.aspect() != null && jar.amount() > 0 && jar.amount() < JarBlockEntity.CAPACITY && jar.label() == null
                    && carrying && jar.aspect().equals(e) && jar.doesContainerAccept(e)) found.add(jar);
        }
        if (found.isEmpty()) for (JarBlockEntity jar : jarlist.values()) {
            if ((jar.aspect() == null || jar.amount() == 0) && jar.label() == null && !jar.isVoid() && jar.doesContainerAccept(e)) found.add(jar);
        }
        if (found.isEmpty()) for (JarBlockEntity jar : jarlist.values()) {
            if (jar.aspect() != null && jar.isVoid() && jar.label() == null && carrying && jar.aspect().equals(e)
                    && jar.doesContainerAccept(e)) found.add(jar);
        }
        if (found.isEmpty()) for (JarBlockEntity jar : jarlist.values()) {
            if ((jar.aspect() == null || jar.amount() == 0) && jar.label() == null && jar.isVoid() && jar.doesContainerAccept(e)) found.add(jar);
        }
        double dist = Double.MAX_VALUE;
        BlockPos dest = null;
        for (BlockEntity jar : found) {
            double d = jar.getBlockPos().distSqr(home);
            if (jar instanceof JarBlockEntity j && j.isVoid()) d += dmod;
            if (d < dist) {
                dist = d;
                dest = jar.getBlockPos();
            }
        }
        return dest;
    }

    private static void getConnectedJars(JarBlockEntity jar, Map<BlockPos, JarBlockEntity> jarlist) {
        Level world = jar.getLevel();
        if (world == null) return;
        for (Direction fd : Direction.values()) {
            BlockPos p = jar.getBlockPos().relative(fd);
            if (jarlist.containsKey(p)) continue;
            if (world.getBlockEntity(p) instanceof JarBlockEntity te) {
                jarlist.put(p, te);
                getConnectedJars(te, jarlist);
            }
        }
    }

    // ---- líquidos (o núcleo de decantação)

    /** Os líquidos de verdade (as fontes), que eram o {@code FluidRegistry} de então. */
    public static List<Fluid> getReggedLiquids() {
        List<Fluid> out = new ArrayList<>();
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (fluid != Fluids.EMPTY && fluid.isSource(fluid.defaultFluidState())) out.add(fluid);
        }
        return out;
    }

    @Nullable
    public static Storage<FluidVariant> fluidStorage(Level level, BlockPos pos, @Nullable Direction side) {
        return FluidStorage.SIDED.find(level, pos, side);
    }

    /** O líquido que um item cheio carrega (o balde d'água, por exemplo), ou nulo. */
    @Nullable
    public static Fluid fluidInItem(ItemStack stack) {
        if (stack.isEmpty()) return null;
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        if (storage == null) return null;
        for (StorageView<FluidVariant> view : storage) {
            if (!view.isResourceBlank() && view.getAmount() > 0) return view.getResource().getFluid();
        }
        return null;
    }

    /** Se é um recipiente de líquido (cheio ou vazio): o que a casa fantasma do golem de decantação aceita. */
    public static boolean isFluidContainer(ItemStack stack) {
        return !stack.isEmpty() && FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack)) != null;
    }

    /** O {@code getMissingLiquids}: os líquidos que o tanque da casa aceita (e que as casas do golem pedem, se pedirem). */
    public static List<Fluid> getMissingLiquids(GolemEntity golem) {
        List<Fluid> out = new ArrayList<>();
        Direction facing = golem.homeFacing();
        Storage<FluidVariant> handler = fluidStorage(golem.level(), golem.homeContainer(), facing);
        if (handler == null) return out;
        for (Fluid id : getReggedLiquids()) {
            if (golem.fluidAmount > 0 && golem.fluidCarried.getFluid() != id) continue;
            if (!canFill(handler, id)) continue;
            if (golem.inventory.hasSomething()) {
                boolean found = false;
                for (int a = 0; a < golem.inventory.slotCount; a++) {
                    if (fluidInItem(golem.inventory.getItem(a)) == id) {
                        found = true;
                        break;
                    }
                }
                if (!found) continue;
            }
            out.add(id);
        }
        return out;
    }

    private static boolean canFill(Storage<FluidVariant> handler, Fluid fluid) {
        if (!handler.supportsInsertion()) return false;
        try (Transaction t = Transaction.openOuter()) {
            return handler.insert(FluidVariant.of(fluid), FluidConstants.BUCKET / 1000, t) > 0;
        }
    }

    /** Quanto dá para tirar (em mB) daquele tanque, sem tirar. */
    public static long drainable(Storage<FluidVariant> handler, Fluid fluid, long mb) {
        if (!handler.supportsExtraction()) return 0;
        try (Transaction t = Transaction.openOuter()) {
            return handler.extract(FluidVariant.of(fluid), mb * (FluidConstants.BUCKET / 1000), t) / (FluidConstants.BUCKET / 1000);
        }
    }

    /** O {@code findPossibleLiquid}: o tanque marcado mais perto que tenha o líquido, ou a fonte marcada no chão. */
    @Nullable
    public static Vec3 findPossibleLiquid(Fluid ls, GolemEntity golem) {
        float dmod = golem.getRange();
        BlockPos v = null;
        double dd = Double.MAX_VALUE;
        for (BlockPos tile : getMarkedFluidHandlers(ls, golem.level(), golem)) {
            double d = golem.distanceToSqr(tile.getX() + 0.5, tile.getY() + 0.5, tile.getZ() + 0.5);
            if (d <= dmod * dmod && d < dd) {
                dd = d;
                v = tile;
            }
        }
        if (v == null) {
            dd = Double.MAX_VALUE;
            for (BlockPos coord : getMarkedFluidBlocks(ls, golem.level(), golem)) {
                double d = golem.distanceToSqr(coord.getX() + 0.5, coord.getY() + 0.5, coord.getZ() + 0.5);
                if (d <= dmod * dmod && d < dd) {
                    dd = d;
                    v = coord;
                }
            }
        }
        return v != null ? new Vec3(v.getX(), v.getY(), v.getZ()) : null;
    }

    public static List<Marker> getMarkedFluidHandlersAdjacentToGolem(Fluid ls, Level world, GolemEntity golem) {
        List<Marker> results = new ArrayList<>();
        for (Marker marker : golem.getMarkers()) {
            if (!marker.in(world) || world.getBlockEntity(marker.pos()) == null) continue;
            if (golem.distanceToSqr(marker.x() + 0.5, marker.y() + 0.5, marker.z() + 0.5) >= ADJACENT_RANGE) continue;
            Storage<FluidVariant> handler = fluidStorage(world, marker.pos(), marker.direction());
            if (handler != null && drainable(handler, ls, 1) > 0) results.add(marker);
        }
        return results;
    }

    public static List<BlockPos> getMarkedFluidHandlers(Fluid ls, Level world, GolemEntity golem) {
        List<BlockPos> results = new ArrayList<>();
        for (Marker marker : golem.getMarkers()) {
            if (!marker.in(world) || world.getBlockEntity(marker.pos()) == null) continue;
            Storage<FluidVariant> handler = fluidStorage(world, marker.pos(), marker.direction());
            if (handler != null && drainable(handler, ls, 1) > 0) results.add(marker.pos());
        }
        return results;
    }

    /** As fontes daquele líquido marcadas no chão. */
    public static List<BlockPos> getMarkedFluidBlocks(Fluid ls, Level world, GolemEntity golem) {
        List<BlockPos> results = new ArrayList<>();
        for (Marker marker : golem.getMarkers()) {
            if (marker.in(world) && isSourceOf(world, marker.pos(), ls)) results.add(marker.pos());
        }
        return results;
    }

    /** Uma fonte daquele líquido (o metadado 0 de então). */
    public static boolean isSourceOf(Level world, BlockPos pos, Fluid fluid) {
        FluidState state = world.getFluidState(pos);
        return state.isSource() && state.getType().isSame(fluid)
                && world.getBlockState(pos).getBlock() == fluid.defaultFluidState().createLegacyBlock().getBlock();
    }

    // ---- o que o golem precisa, conforme o núcleo

    @Nullable
    public static List<ItemStack> getItemsNeeded(GolemEntity golem, boolean fuzzy) {
        List<ItemStack> needed;
        switch (golem.getCore()) {
            case 1 -> {
                needed = golem.inventory.getItemsNeeded(golem.getUpgradeAmount(5) > 0);
                if (needed.isEmpty()) return null;
                List<ItemStack> out = new ArrayList<>();
                for (ItemStack s : needed) if (!isOnTimeOut(golem, s) && findSomethingEmptyCore(golem, s)) out.add(s);
                return out;
            }
            case 8 -> {
                needed = golem.inventory.getItemsNeeded(golem.getUpgradeAmount(5) > 0);
                if (needed.isEmpty()) return null;
                List<ItemStack> out = new ArrayList<>();
                for (ItemStack s : needed) if (!isOnTimeOut(golem, s) && findSomethingUseCore(golem, s)) out.add(s);
                return out;
            }
            case 10 -> {
                needed = getItemsInHomeContainer(golem);
                if (needed == null) return null;
                List<ItemStack> out = new ArrayList<>();
                for (ItemStack s : needed) if (!isOnTimeOut(golem, s) && findSomethingSortCore(golem, s)) out.add(s);
                return out;
            }
            default -> {
                return null;
            }
        }
    }

    public static boolean findSomethingUseCore(GolemEntity golem, ItemStack itemToMatch) {
        Level level = golem.level();
        for (byte col : golem.getColorsMatching(itemToMatch)) {
            for (Marker marker : golem.getMarkers()) {
                boolean air = level.isEmptyBlock(marker.pos());
                if ((marker.color() == col || col == -1) && (!golem.getToggles()[0] || air) && (golem.getToggles()[0] || !air)) {
                    if (level.isEmptyBlock(marker.pos().relative(marker.direction()))) return true;
                }
            }
        }
        timeOut(golem, itemToMatch);
        return false;
    }

    public static boolean findSomethingEmptyCore(GolemEntity golem, ItemStack itemToMatch) {
        List<Byte> matchingColors = golem.getColorsMatching(itemToMatch);
        BlockPos c = golem.homeContainer();
        float dmod = golem.getRange();
        for (byte color : matchingColors) {
            for (Container te : getContainersWithRoom(golem.level(), golem, color, itemToMatch)) {
                BlockPos p = InventoryUtils.posOf(te);
                double distance = golem.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                if (distance <= dmod * dmod && !p.equals(c)) return true;
            }
        }
        for (byte color : matchingColors) {
            for (Marker marker : golem.getMarkers()) {
                if ((marker.color() == color || color == -1) && !(golem.level().getBlockEntity(marker.pos()) instanceof Container)) return true;
            }
        }
        timeOut(golem, itemToMatch);
        return false;
    }

    public static boolean findSomethingSortCore(GolemEntity golem, ItemStack itemToMatch) {
        BlockPos c = golem.homeContainer();
        float dmod = golem.getRange();
        for (Container te : getContainersWithRoom(golem.level(), golem, (byte) -1, itemToMatch)) {
            BlockPos p = InventoryUtils.posOf(te);
            double distance = golem.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
            if (distance <= dmod * dmod && !p.equals(c)) {
                for (int side : getMarkedSides(golem, (BlockEntity) te, (byte) -1)) {
                    if (InventoryUtils.inventoryContains(te, itemToMatch, side, golem.checkOreDict(), golem.ignoreDamage(), golem.ignoreNBT())) {
                        return true;
                    }
                }
            }
        }
        timeOut(golem, itemToMatch);
        return false;
    }

    private static void timeOut(GolemEntity golem, ItemStack stack) {
        ITEM_TIMEOUT.add(new SortingItemTimeout(golem.getId(), stack.copy(), System.currentTimeMillis() + IGNORE_DELAY));
    }

    public static boolean isOnTimeOut(GolemEntity golem, ItemStack stack) {
        for (int q = 0; q < ITEM_TIMEOUT.size(); q++) {
            SortingItemTimeout t = ITEM_TIMEOUT.get(q);
            if (!t.matches(golem.getId(), stack)) continue;
            if (System.currentTimeMillis() < t.time) return true;
            ITEM_TIMEOUT.remove(q);
            return false;
        }
        return false;
    }

    public static boolean validTargetForItem(GolemEntity golem, ItemStack stack) {
        if (isOnTimeOut(golem, stack)) return false;
        switch (golem.getCore()) {
            case 1:
                return findSomethingEmptyCore(golem, stack);
            case 8:
                return findSomethingUseCore(golem, stack);
            case 10:
                return findSomethingSortCore(golem, stack);
            default:
                List<ItemStack> neededList = getItemsNeeded(golem, golem.getUpgradeAmount(5) > 0);
                if (neededList != null) {
                    for (ItemStack ss : neededList) {
                        if (InventoryUtils.areItemStacksEqual(ss, golem.itemCarried, golem.checkOreDict(), golem.ignoreDamage(), golem.ignoreNBT())) {
                            return true;
                        }
                    }
                }
                timeOut(golem, stack);
                return false;
        }
    }

    /** O {@code getFirstItemUsingTimeout}: a primeira coisa do baú que não esteja na geladeira, até o que cabe na mão. */
    public static ItemStack getFirstItemUsingTimeout(GolemEntity golem, Container inventory, int side, boolean doit) {
        ItemStack stack1 = ItemStack.EMPTY;
        for (int slot : InventoryUtils.slotsFor(inventory, side)) {
            if (stack1.isEmpty() && !inventory.getItem(slot).isEmpty()) {
                if (isOnTimeOut(golem, inventory.getItem(slot))) continue;
                stack1 = inventory.getItem(slot).copyWithCount(golem.getCarrySpace());
            }
            if (!stack1.isEmpty()) stack1 = InventoryUtils.attemptExtraction(inventory, stack1, slot, side, false, false, false, doit);
            if (!stack1.isEmpty()) break;
        }
        if (!stack1.isEmpty()) return stack1.copy();
        if (doit) inventory.setChanged();
        return ItemStack.EMPTY;
    }

    @Nullable
    public static List<ItemStack> getItemsInHomeContainer(GolemEntity golem) {
        if (!(golem.level().getBlockEntity(golem.homeContainer()) instanceof Container inv)) return null;
        List<ItemStack> out = new ArrayList<>();
        for (int slot : InventoryUtils.slotsFor(inv, golem.homeFacing().get3DDataValue())) {
            if (!inv.getItem(slot).isEmpty()) out.add(inv.getItem(slot).copy());
        }
        return out;
    }

    /** Um item na geladeira: o golem, a coisa (comparada pelo item, dano e componentes) e até quando. */
    private record SortingItemTimeout(int golemId, ItemStack stack, long time) {
        boolean matches(int id, ItemStack other) {
            return this.golemId == id && InventoryUtils.isItemEqual(this.stack, other) && InventoryUtils.tagsEqual(this.stack, other);
        }
    }
}
