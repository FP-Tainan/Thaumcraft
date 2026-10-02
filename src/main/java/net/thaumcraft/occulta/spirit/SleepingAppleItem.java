package net.thaumcraft.occulta.spirit;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A Maçã do Sono: o {@code itemSleepingApple} do Witchery.
 *
 * <p>Come-se e se dorme. O corpo fica onde estava, em carne, e o espírito se levanta para o
 * {@linkplain SpiritWorld Mundo dos Espíritos}.
 *
 * <p>Ela abre a porta <b>sempre para o lado feio</b>: o original lhe passa a chance de pesadelo <b>um</b>, e a
 * conta dos arredores só conta para quem já tiver um Apanhador de Sonhos com a teia dos pesadelos. Quem a come
 * sem quarto montado cai em pesadelo, e é assim que se aprende a montar o quarto.
 */
public class SleepingAppleItem extends Item {
    /** A chance de pesadelo que ela passa: o um do original. */
    public static final double NIGHTMARE = 1.0;

    public SleepingAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack coisa, Level level, LivingEntity quem) {
        ItemStack sobra = super.finishUsingItem(coisa, level, quem);
        if (level.isClientSide() || !(quem instanceof ServerPlayer gente)) return sobra;
        if (SpiritWorld.is(level) || SpiritWalk.walking(gente)) return sobra;

        if (SpiritWorld.fallAsleep(gente, NIGHTMARE)) {
            level.playSound(null, gente.getX(), gente.getY(), gente.getZ(), SoundEvents.PLAYER_BURP,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.0f);
        }
        return sobra;
    }
}
