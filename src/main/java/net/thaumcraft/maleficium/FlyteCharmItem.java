package net.thaumcraft.maleficium;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.WandItem;

/**
 * O amuleto de voo: o {@code ItemFlyteCharm} do Tainted Magic 8.1.1.
 *
 * <p>Levado no inventário, ele deixa voar — pagando quinze de <i>aer</i> por vez, tirados de uma varinha que esteja
 * junto. Sem vis, o voo acaba na hora. Agachando no ar enquanto se cai, ele ainda plana, cobrando cinco.
 */
public class FlyteCharmItem extends Item {
    /** O que o voo custa, e o que planar custa. */
    public static final AspectList FLIGHT = new AspectList().add(Aspects.AIR, 15);
    public static final AspectList GLIDE = new AspectList().add(Aspects.AIR, 5);

    public FlyteCharmItem(Properties properties) {
        super(properties);
    }

    /** Quem tem o amuleto no inventário? */
    public static boolean carried(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).getItem() instanceof FlyteCharmItem) return true;
        }
        return false;
    }

    /**
     * O {@code consumeVisFromInventory} do ajudante do mod: tira o vis da primeira varinha do inventário que tiver o
     * bastante. Com {@code doit} falso, só pergunta se dá.
     */
    public static boolean consume(Player player, AspectList cost, boolean doit) {
        if (player.getAbilities().instabuild) return true;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!(stack.getItem() instanceof WandItem)) continue;
            if (WandItem.consumeRaw(stack, cost, doit)) return true;
        }
        return false;
    }
}
