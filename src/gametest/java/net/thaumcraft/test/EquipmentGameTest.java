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

    @GameTest
    public void theFullFortressSetTakesEightyPercentOff(GameTestHelper helper) {
        var zombie = helper.makeMockServerPlayerInLevel();
        zombie.setGameMode(GameType.SURVIVAL);
        var attacker = helper.makeMockPlayer(GameType.SURVIVAL);
        zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TCItems.FORTRESS_HELMET));
        zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TCItems.FORTRESS_CHESTPLATE));
        zombie.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TCItems.FORTRESS_LEGGINGS));
        float taken;
        try {
            var absorb = net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("getDamageAfterArmorAbsorb",
                    net.minecraft.world.damagesource.DamageSource.class, float.class);
            absorb.setAccessible(true);
            taken = (float) absorb.invoke(zombie, helper.getLevel().damageSources().playerAttack(attacker), 10.0f);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        // o golpe de outro jogador (que não muda com a dificuldade): (3 + 7 + 6) / 25 × (0,875 + 3 × 0,125) = 0,8: dos 10, passam 2
        if (Math.abs(taken - 2.0f) > 0.01f) helper.fail("a fortaleza completa deixa passar 2 de 10; passou " + taken);
        helper.succeed();
    }

    @GameTest
    public void theFocusKeySwapsThroughInventoryAndPouch(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = new ItemStack(TCItems.WAND);
        player.getInventory().setItem(0, wand);
        player.getInventory().setSelectedSlot(0);
        player.getInventory().setItem(5, new ItemStack(TCItems.FOCI.get("fire")));
        ItemStack pouch = new ItemStack(TCItems.FOCUS_POUCH);
        var inside = net.minecraft.core.NonNullList.withSize(18, ItemStack.EMPTY);
        inside.set(0, new ItemStack(TCItems.FOCI.get("frost")));
        net.thaumcraft.item.FocusPouchItem.setContents(pouch, inside);
        player.getInventory().setItem(6, pouch);

        // pedir qualquer coisa antes de "AF" pega o primeiro: o fogo, do inventário
        net.thaumcraft.item.FocusSwap.change(wand, player, "");
        if (!"fire".equals(wand.get(net.thaumcraft.registry.TCComponents.WAND_FOCUS))) helper.fail("o primeiro foco é o de fogo");
        if (!player.getInventory().getItem(5).isEmpty()) helper.fail("o foco de fogo sai do inventário");
        // pedir o de gelo tira da bolsa, e o de fogo vai para a casa vazia da bolsa
        net.thaumcraft.item.FocusSwap.change(wand, player, "BF");
        if (!"frost".equals(wand.get(net.thaumcraft.registry.TCComponents.WAND_FOCUS))) helper.fail("agora o de gelo");
        var after = net.thaumcraft.item.FocusPouchItem.contents(player.getInventory().getItem(6));
        if (!after.get(0).is(TCItems.FOCI.get("fire"))) helper.fail("o de fogo volta para a bolsa");
        // agachado: tira o foco
        net.thaumcraft.item.FocusSwap.change(wand, player, net.thaumcraft.item.FocusSwap.REMOVE);
        if (wand.has(net.thaumcraft.registry.TCComponents.WAND_FOCUS)) helper.fail("REMOVE tira o foco");
        helper.succeed();
    }

    @GameTest
    public void wornBaublesCount(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = new ItemStack(TCItems.WAND);
        float fire = WandItem.modifier(wand, player, Aspects.FIRE), water = WandItem.modifier(wand, player, Aspects.WATER);
        var worn = net.thaumcraft.baubles.Baubles.container(player);
        worn.setItem(net.thaumcraft.baubles.Baubles.RING_1, new ItemStack(TCItems.APPRENTICE_RINGS.get("fire")));
        if (Math.abs(fire - WandItem.modifier(wand, player, Aspects.FIRE) - 0.01f) > 0.001f) helper.fail("o anel de aprendiz de fogo dá 1% no fogo");
        if (Math.abs(water - WandItem.modifier(wand, player, Aspects.WATER)) > 0.001f) helper.fail("e nada na água");

        // a bolsa vestida no cinto também vale para a tecla de trocar foco
        ItemStack pouch = new ItemStack(TCItems.FOCUS_POUCH);
        var inside = net.minecraft.core.NonNullList.withSize(18, ItemStack.EMPTY);
        inside.set(3, new ItemStack(TCItems.FOCI.get("shock")));
        net.thaumcraft.item.FocusPouchItem.setContents(pouch, inside);
        worn.setItem(net.thaumcraft.baubles.Baubles.BELT, pouch);
        net.thaumcraft.item.FocusSwap.change(wand, player, "BL");
        if (!"shock".equals(wand.get(net.thaumcraft.registry.TCComponents.WAND_FOCUS))) helper.fail("o foco de raio sai da bolsa do cinto");
        helper.succeed();
    }
}
