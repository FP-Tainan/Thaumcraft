package net.thaumcraft.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.baubles.BaubleItem;
import net.thaumcraft.api.baubles.BaubleType;

/**
 * Uma casa de bijuteria: o {@code SlotBauble} do Baubles 1.0.1.10. Só entra peça do tipo da casa que quem veste aceita,
 * e só sai o que aceita sair (o anel amaldiçoado do original não sai).
 */
public class BaubleSlot extends Slot {
    private final BaubleType type;
    private final Player owner;

    public BaubleSlot(Container container, BaubleType type, int index, int x, int y, Player owner) {
        super(container, index, x, y);
        this.type = type;
        this.owner = owner;
    }

    public BaubleType type() {
        return this.type;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof BaubleItem bauble && bauble.baubleType(stack) == this.type
                && bauble.canEquip(stack, this.owner);
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemStack stack = this.getItem();
        return !stack.isEmpty() && stack.getItem() instanceof BaubleItem bauble && bauble.canUnequip(stack, player);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
