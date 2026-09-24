package net.thaumcraft.naturalis;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thaumcraft.api.baubles.BaubleItem;
import net.thaumcraft.api.baubles.BaubleType;

/**
 * A Bolsa de Focos do Fim: o {@code FocusEnderPouchItem} do Magia Naturalis 0.5.0.
 *
 * <p>É uma bolsa de focos que não guarda nada por si: o que ela mostra é o baú do fim de quem a carrega. Vai na
 * casa do cinto, como a bolsa de focos do Thaumcraft, e a troca de foco procura dentro dela.
 */
public class EnderPouchItem extends Item implements BaubleItem {
    public EnderPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (player instanceof ServerPlayer sent) {
            var ender = sent.getEnderChestInventory();
            sent.openMenu(new SimpleMenuProvider((id, inventory, who) -> ChestMenu.threeRows(id, inventory, ender),
                    Component.translatable("item.thaumcraft.focus_ender_pouch")));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BaubleType baubleType(ItemStack stack) {
        return BaubleType.BELT;
    }

    /** O que há dentro dela para a troca de foco: o baú do fim de quem a carrega. */
    public static java.util.List<ItemStack> contents(Player player) {
        var ender = player.getEnderChestInventory();
        java.util.List<ItemStack> list = new java.util.ArrayList<>();
        for (int slot = 0; slot < ender.getContainerSize(); slot++) list.add(ender.getItem(slot));
        return list;
    }
}
