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
}
