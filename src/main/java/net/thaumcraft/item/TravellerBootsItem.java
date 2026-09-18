package net.thaumcraft.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * As botas do viajante: o {@code ItemBootsTraveller} da 4.2.3.5.
 *
 * <p>Andando para a frente, empurram mais cinco centésimos e meio por tique no chão (um quarto disso na água) e dão
 * mais controle no ar; sobem degraus de um bloco inteiro (um modificador do próprio item, já que o jogo de hoje
 * guarda o degrau num atributo) e tiram um quarto de bloco da queda a cada tique.
 */
public class TravellerBootsItem extends Item {
    public TravellerBootsItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (slot == EquipmentSlot.FEET && entity instanceof Player player) tickWorn(player);
    }

    /**
     * O {@code onArmorTick}, dos dois lados: o empurrão vale para quem joga (o cliente move o próprio jogador), a
     * queda para o servidor. O cliente chama isto a cada tique do jogador local.
     */
    public static void tickWorn(Player player) {
        Level level = player.level();
        if (level.isClientSide() && !player.getAbilities().flying && player.zza > 0.0f) {
            if (player.onGround()) {
                float bonus = player.isInWater() ? 0.055f / 4.0f : 0.055f;
                player.moveRelative(bonus, new Vec3(0.0, 0.0, 1.0));
            } else {
                // o jumpMovementFactor de 0,05 no lugar dos 0,02 de sempre: os três centésimos que faltam
                player.moveRelative(0.03f, new Vec3(player.xxa, 0.0, player.zza));
            }
        }
        if (!level.isClientSide() && player.fallDistance > 0.0) player.fallDistance = Math.max(0.0, player.fallDistance - 0.25);
    }
}
