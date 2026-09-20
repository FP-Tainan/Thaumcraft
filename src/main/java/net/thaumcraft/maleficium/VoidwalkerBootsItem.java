package net.thaumcraft.maleficium;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.wands.VisDiscountGear;
import net.thaumcraft.research.WarpEvents;
import org.jetbrains.annotations.Nullable;

/**
 * As botas do caminhante do vazio: o {@code ItemVoidwalkerBoots} do Tainted Magic 8.1.1.
 *
 * <p>Andando para a frente, empurram mais doze centésimos por tique (o dobro com a faixa ligada) e sobem um bloco
 * inteiro; no ar dão mais controle; tiram a queda acima de três blocos; pulam um quarto mais alto; descontam cinco
 * por cento do vis; consertam-se sozinhas um ponto por segundo; e distorcem quem as calça (cinco).
 */
public class VoidwalkerBootsItem extends Item implements VisDiscountGear, WarpEvents.WarpingGear {
    /** O empurrão do original, e o dobro dele com a faixa ligada. */
    public static final float BOOST = 0.12f;

    public VoidwalkerBootsItem(Properties properties) {
        super(properties);
    }

    @Override
    public int visDiscount(ItemStack stack, Player player, @Nullable Aspect aspect) {
        return 5;
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return 5;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        // o conserto sozinho vale mesmo guardada, como no original
        if (entity.tickCount % 20 == 0 && stack.isDamaged()) stack.setDamageValue(stack.getDamageValue() - 1);
        if (slot == EquipmentSlot.FEET && entity instanceof Player player) tickWorn(player);
    }

    /** O {@code onArmorTick}: o empurrão, o controle no ar e a queda amortecida. Vale nos dois lados. */
    public static void tickWorn(Player player) {
        if (player.fallDistance > 3.0) player.fallDistance = 1.0;
        if (player.zza <= 0.0f) return;
        boolean sash = MaleficiumBaubles.speedSash(player);
        if (player.onGround() || player.getAbilities().flying) {
            float bonus = sash ? BOOST * 2.0f : BOOST;
            player.moveRelative(player.getAbilities().flying ? bonus * 0.75f : bonus, new Vec3(0.0, 0.0, 1.0));
        }
    }
}
