package net.thaumcraft.occulta.curse;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * O frasco do Cozimento do Grotesco, que se <b>bebe</b> — e não se atira.
 *
 * <p>Os outros cozimentos do pote voam e arrebentam onde batem; este é dos poucos que se engolem. Bebido, dá
 * o minuto do {@link Grotesque}: tudo o que chegar a quatro blocos é empurrado para trás.
 */
public class GrotesqueBrewItem extends Item {
    public GrotesqueBrewItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack frasco, Level level, LivingEntity quem) {
        ItemStack sobra = super.finishUsingItem(frasco, level, quem);
        if (!level.isClientSide() && quem instanceof Player gente) Grotesque.bebeu(gente);
        return sobra;
    }
}
