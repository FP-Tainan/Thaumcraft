package net.thaumcraft.maleficium;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * As bagas de beladona: o {@code ItemNightshadeBerries} do Tainted Magic.
 *
 * <p>Comem-se depressa e matam na hora — o original dá trinta e dois mil setecentos e sessenta e sete de dano a
 * quem as engole. Não alimentam nada.
 */
public class NightshadeBerriesItem extends Item {
    public NightshadeBerriesItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        ItemStack rest = super.finishUsingItem(stack, level, user);
        if (!level.isClientSide()) {
            user.hurt(net.thaumcraft.registry.TCDamageTypes.nightshade(level), 32767.0f);
        }
        return rest;
    }
}
