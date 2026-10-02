package net.thaumcraft.occulta.spirit;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * O Cozimento do Sono: o {@code itemBrewOfSleeping} do Witchery.
 *
 * <p>Bebe-se, e se dorme. É a porta boa para o {@linkplain SpiritWorld Mundo dos Espíritos}: o original lhe passa
 * uma chance de pesadelo de <b>0,998</b>, que é quase um — mas um quarto bem montado, com um Apanhador de Sonhos
 * de teia de pesadelos por perto, derruba essa conta e deixa passar o sonho bom. A Maçã do Sono passa um
 * <b>um redondo</b>, e por isso nunca se livra do pesadelo.
 *
 * <p>Só funciona no mundo de cá, e não em quem já anda em espírito.
 */
public class BrewOfSleepingItem extends Item {
    /** A chance de pesadelo que ele passa: o 0,998 do original. */
    public static final double NIGHTMARE = 0.998;

    public BrewOfSleepingItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack coisa, Level level, LivingEntity quem) {
        ItemStack sobra = super.finishUsingItem(coisa, level, quem);
        if (level.isClientSide() || !(quem instanceof ServerPlayer gente)) return sobra;
        if (SpiritWorld.is(level) || SpiritWalk.walking(gente)) return sobra;

        if (SpiritWorld.fallAsleep(gente, NIGHTMARE)) {
            level.playSound(null, gente.getX(), gente.getY(), gente.getZ(), SoundEvents.PLAYER_BURP,
                    SoundSource.PLAYERS, 0.5f, level.getRandom().nextFloat() * 0.1f + 0.9f);
        }
        return sobra;
    }
}
