package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCItems;

/** Os descontos de vis do que se veste têm de somar como o {@code getTotalVisDiscount} do original. */
public class EquipmentGameTest {
    @GameTest
    public void gogglesAndRobesTakeTenPercentOffTheWand(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = new ItemStack(TCItems.WAND);
        float bare = WandItem.modifier(wand, player, Aspects.FIRE);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TCItems.GOGGLES));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TCItems.ROBE_CHESTPLATE));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TCItems.ROBE_LEGGINGS));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(TCItems.ROBE_BOOTS));
        float dressed = WandItem.modifier(wand, player, Aspects.FIRE);
        if (Math.abs(bare - dressed - 0.10f) > 0.001f) helper.fail("óculos 5 + manto 2 + calça 2 + botas 1 = 10%; deu " + (bare - dressed));
        helper.succeed();
    }
}
