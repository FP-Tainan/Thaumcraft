package net.thaumcraft.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A casa do resultado da bancada arcana.
 *
 * <p>Nada entra aqui: só sai. E ao sair, a conta é cobrada — o vis da varinha e o que estava na grade.
 */
public class ArcaneResultSlot extends Slot {
    private final ArcaneWorkbenchMenu menu;
    private final Player player;

    public ArcaneResultSlot(ArcaneWorkbenchMenu menu, Player player, Container result, int index, int x, int y) {
        super(result, index, x, y);
        this.menu = menu;
        this.player = player;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        this.menu.take();
        super.onTake(player, stack);
    }
}
