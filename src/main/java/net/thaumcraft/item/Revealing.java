package net.thaumcraft.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.registry.TCItems;

/**
 * Quem enxerga o que está por trás do mundo.
 *
 * <p>No Thaumcraft 4.2.3.5 os nós de aura não ficam à vista de qualquer um: é preciso estar com o
 * thaumômetro na mão ou com os Óculos da Revelação no rosto. Sem isso, um nó é só ar.
 */
public final class Revealing {
    /** A etiqueta por onde um mod de fora diz que o elmo dele também revela. */
    public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> REVEALING =
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                    net.thaumcraft.Thaumcraft.id("revealing"));

    private Revealing() {
    }

    /** Este jogador enxerga o que está por trás do mundo agora? */
    public static boolean can(Player player) {
        if (player == null) return false;
        if (player.getMainHandItem().is(TCItems.THAUMOMETER)) return true;
        if (player.getOffhandItem().is(TCItems.THAUMOMETER)) return true;
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        // o elmo de fortaleza com os óculos embutidos também revela (o showNodes do ItemFortressArmor)
        // e o capuz do manto do vazio (o showNodes do ItemVoidRobeArmor)
        return head.is(TCItems.GOGGLES) || head.is(TCItems.VOID_ROBE_HELMET) || head.is(REVEALING)
                || Boolean.TRUE.equals(head.get(net.thaumcraft.registry.TCComponents.FORTRESS_GOGGLES));
    }
}
