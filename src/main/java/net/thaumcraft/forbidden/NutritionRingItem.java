package net.thaumcraft.forbidden;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.baubles.BaubleItem;
import net.thaumcraft.api.baubles.BaubleType;
import net.thaumcraft.baubles.Baubles;

/**
 * O Anel da Nutrição: o {@code ItemRingNutrition} do Forbidden Magic 0.575.
 *
 * <p>Quem o veste tira mais do que come: cada garfada rende dois de fome e dois de saturação a mais.
 */
public class NutritionRingItem extends Item implements BaubleItem {
    public NutritionRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public BaubleType baubleType(ItemStack stack) {
        return BaubleType.RING;
    }

    /** O que o anel soma a cada refeição, se quem come estiver com ele no dedo. */
    public static void onEat(Player player) {
        if (!wearing(player)) return;
        player.getFoodData().eat(2, 2.0f);
    }

    private static boolean wearing(Player player) {
        return Baubles.get(player, Baubles.RING_1).getItem() instanceof NutritionRingItem
                || Baubles.get(player, Baubles.RING_2).getItem() instanceof NutritionRingItem;
    }
}
