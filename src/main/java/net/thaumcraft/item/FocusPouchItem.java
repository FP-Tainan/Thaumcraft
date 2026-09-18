package net.thaumcraft.item;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.thaumcraft.inventory.FocusPouchMenu;

/**
 * A bolsa de focos: o {@code ItemFocusPouch} da 4.2.3.5. Guarda dezoito focos; aberta com o clique direito, e a
 * tecla de trocar foco também procura foco dentro dela.
 */
public class FocusPouchItem extends Item {
    public static final int SIZE = 18;

    public FocusPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && !level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider((id, inventory, who) -> new FocusPouchMenu(id, inventory, player.getMainHandItem()),
                    Component.translatable("item.thaumcraft.focus_pouch")));
        }
        return InteractionResult.SUCCESS;
    }

    /** O {@code getInventory}: as dezoito casas da bolsa. */
    public static NonNullList<ItemStack> contents(ItemStack pouch) {
        NonNullList<ItemStack> list = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        pouch.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(list);
        return list;
    }

    /** O {@code setInventory}. */
    public static void setContents(ItemStack pouch, NonNullList<ItemStack> list) {
        pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(list));
    }
}
