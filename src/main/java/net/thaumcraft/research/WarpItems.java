package net.thaumcraft.research;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

import java.util.HashMap;
import java.util.Map;

/**
 * A distorção que gruda em quem fabrica cada coisa: o {@code addWarpToItem} do {@code ConfigResearch} da 4.2.3.5.
 * <strong>Gerada</strong> pelo {@code scratchpad/dobra-itens.js}; não editar à mão.
 */
public final class WarpItems {
    private static final Map<Item, Integer> WARP = new HashMap<>();

    private WarpItems() {
    }

    /** O {@code ThaumcraftApi.getWarp(ItemStack)}. */
    public static int of(ItemStack stack) {
        if (WARP.isEmpty()) fill();
        return stack.isEmpty() ? 0 : WARP.getOrDefault(stack.getItem(), 0);
    }

    /** A distorção que um mod de fora põe numa coisa dele: o {@code ThaumcraftApi.addWarpToItem}. */
    public static void register(net.minecraft.world.item.Item item, int amount) {
        if (WARP.isEmpty()) fill();
        WARP.put(item, amount);
    }

    private static void fill() {
        WARP.put(TCItems.FOCI.get("hellbat"), 1);
        WARP.put(TCItems.SINISTER_STONE, 1);
        WARP.put(TCBlocks.BRAIN_JAR.asItem(), 1);
        WARP.put(TCItems.BUCKET_DEATH, 1);
        WARP.put(TCItems.BOTTLE_TAINT, 1);
        WARP.put(TCItems.GOLEM_PLACERS.get("flesh"), 1);
        WARP.put(TCItems.FOCI.get("primal"), 1);
        WARP.put(TCItems.STAFF_RODS.get("primal"), 1);
    }
}
