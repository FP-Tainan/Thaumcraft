package net.thaumcraft.mortuorum;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;

/**
 * A Foice e a Foice de Osso: o {@code ItemScythe} do Necromancy.
 *
 * <p>São espadas, e o que têm de seu é o que fazem quando o golpe mata: se quem bateu tiver uma garrafa vazia, a
 * garrafa se gasta e no lugar dela fica uma Alma num Pote, com uma nuvem de caveiras e o corpo atirado para cima.
 */
public class ScytheItem extends Item {
    public ScytheItem(Properties properties) {
        super(properties);
    }

    /** O que o cliente faz com as caveiras; no servidor não há nada para fazer. */
    public interface Skulls {
        void burst(net.minecraft.world.level.Level level, double x, double y, double z);
    }

    public static Skulls clientSkulls = (level, x, y, z) -> {
    };

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (target.getHealth() > 0.0f) return;
        if (!(attacker instanceof Player quemBateu)) return;
        if (!(attacker.level() instanceof ServerLevel)) return;
        if (!take(quemBateu, Items.GLASS_BOTTLE)) return;

        if (!quemBateu.getInventory().add(new ItemStack(MortuorumItems.SOUL_IN_A_JAR))) {
            quemBateu.drop(new ItemStack(MortuorumItems.SOUL_IN_A_JAR), false);
        }
        // o original atira o corpo para cima com dez mil de empuxo; aqui um salto que se possa ver
        target.setDeltaMovement(target.getDeltaMovement().x, 2.0, target.getDeltaMovement().z);
        target.hurtMarked = true;
        target.level().broadcastEntityEvent(target, (byte) 60);
    }

    /** Tira uma coisa da mochila de quem bateu, como o {@code consumeInventoryItem} do original. */
    private static boolean take(Player player, Item item) {
        if (player.getAbilities().instabuild) return true;
        var mochila = player.getInventory();
        for (int casa = 0; casa < mochila.getContainerSize(); casa++) {
            ItemStack pilha = mochila.getItem(casa);
            if (pilha.is(item)) {
                pilha.shrink(1);
                return true;
            }
        }
        return false;
    }
}
