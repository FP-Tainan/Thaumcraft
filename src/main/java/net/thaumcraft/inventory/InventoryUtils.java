package net.thaumcraft.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;

/**
 * O {@code InventoryUtils.placeItemStackIntoInventory} da 4.2.3.5: põe uma pilha num inventário pelo lado dado,
 * primeiro juntando com o que já há, depois nas casas vazias — ou só confere quanto caberia, sem mexer em nada.
 */
public final class InventoryUtils {
    private InventoryUtils() {
    }

    /** @return o que sobrou (vazio se coube tudo) */
    public static ItemStack insert(Container inv, ItemStack stack, Direction face, boolean doit) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack left = stack.copy();
        int[] slots = slots(inv, face);
        // junta com o que já há
        for (int slot : slots) {
            if (left.isEmpty()) break;
            ItemStack here = inv.getItem(slot);
            if (here.isEmpty() || !ItemStack.isSameItemSameComponents(here, left) || !can(inv, slot, left, face)) continue;
            int room = Math.min(inv.getMaxStackSize(here), here.getMaxStackSize()) - here.getCount();
            int move = Math.min(room, left.getCount());
            if (move <= 0) continue;
            if (doit) {
                here.grow(move);
                inv.setChanged();
            }
            left.shrink(move);
        }
        // e nas vazias
        for (int slot : slots) {
            if (left.isEmpty()) break;
            if (!inv.getItem(slot).isEmpty() || !can(inv, slot, left, face)) continue;
            int move = Math.min(inv.getMaxStackSize(left), left.getCount());
            if (doit) {
                inv.setItem(slot, left.copyWithCount(move));
                inv.setChanged();
            }
            left.shrink(move);
        }
        return left;
    }

    private static boolean can(Container inv, int slot, ItemStack stack, Direction face) {
        if (!inv.canPlaceItem(slot, stack)) return false;
        return !(inv instanceof WorldlyContainer sided) || sided.canPlaceItemThroughFace(slot, stack, face);
    }

    private static int[] slots(Container inv, Direction face) {
        if (inv instanceof WorldlyContainer sided) return sided.getSlotsForFace(face);
        int[] all = new int[inv.getContainerSize()];
        for (int i = 0; i < all.length; i++) all[i] = i;
        return all;
    }
}
