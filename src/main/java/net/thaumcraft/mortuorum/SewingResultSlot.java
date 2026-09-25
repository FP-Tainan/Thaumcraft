package net.thaumcraft.mortuorum;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A casa de onde sai a costura: o {@code SlotSewing} do Necromancy.
 *
 * <p>Tirar a peça gasta uma de cada coisa da grade, uma agulha e uma linha — e nada entra aqui.
 */
public class SewingResultSlot extends Slot {
    private final Player player;
    private final Container grid;
    private final Container machine;

    public SewingResultSlot(Player player, Container grid, Container machine, Container result, int index, int x, int y) {
        super(result, index, x, y);
        this.player = player;
        this.grid = grid;
        this.machine = machine;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public void onTake(Player player, ItemStack taken) {
        for (int slot = 0; slot < this.grid.getContainerSize(); slot++) {
            if (!this.grid.getItem(slot).isEmpty()) this.grid.removeItem(slot, 1);
        }
        this.machine.removeItem(SewingMachineBlockEntity.NEEDLE, 1);
        this.machine.removeItem(SewingMachineBlockEntity.THREAD, 1);
        this.grid.setChanged();
        this.machine.setChanged();
        super.onTake(player, taken);
    }
}
