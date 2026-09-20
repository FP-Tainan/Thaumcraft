package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.baubles.BaubleType;
import net.thaumcraft.baubles.Baubles;
import net.thaumcraft.inventory.BaubleSlot;
import net.thaumcraft.registry.TCItems;

import java.util.ArrayList;
import java.util.List;

/**
 * As quatro casas de bijuteria moram no inventário do próprio jogo — amuleto, dois anéis e cinto —, e agachar com a
 * peça na mão veste ({@code InventoryMenuBaublesMixin}).
 */
public class BaublesInventoryGameTest {
    @GameTest
    public void theInventoryHasTheFourBaubleSlots(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        List<BaubleType> tipos = new ArrayList<>();
        for (var slot : player.inventoryMenu.slots) if (slot instanceof BaubleSlot bauble) tipos.add(bauble.type());
        if (!tipos.equals(List.of(BaubleType.AMULET, BaubleType.RING, BaubleType.RING, BaubleType.BELT))) {
            helper.fail("o inventário tem de ter amuleto, dois anéis e cinto; tem " + tipos);
        }
        helper.succeed();
    }

    @GameTest
    public void crouchClickWearsTheBauble(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        var menu = player.inventoryMenu;
        player.getInventory().setItem(9, new ItemStack(TCItems.MUNDANE_AMULET));
        int onde = -1;
        for (int i = 0; i < menu.slots.size(); i++) {
            var slot = menu.slots.get(i);
            if (slot.container == player.getInventory() && slot.getContainerSlot() == 9) onde = i;
        }
        if (onde < 0) helper.fail("não achei a casa do inventário onde o amuleto foi parar");
        menu.quickMoveStack(player, onde);
        ItemStack vestido = Baubles.container(player).getItem(Baubles.AMULET);
        if (!vestido.is(TCItems.MUNDANE_AMULET)) helper.fail("agachar com o amuleto tinha de vesti-lo; a casa tem " + vestido);
        if (!player.getInventory().getItem(9).isEmpty()) helper.fail("o amuleto tinha de sair do inventário");
        helper.succeed();
    }
}
