package net.thaumcraft.occulta.infusion;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A <b>Base de Cozimento Infundido</b>: o {@code itemInfusionBase} do Witchery.
 *
 * <p>É o frasco que se põe no círculo para que o rito faça dele um {@linkplain InfusedBrews cozimento
 * infundido}. Por si não vale nada, e o original diz isso do jeito mais claro que há: bebida, ela dá
 * <b>Definhamento IV</b> por dez segundos.
 *
 * <p>Não é um castigo nem uma piada — é a maneira de 2014 de escrever «isto é um ingrediente». Quem a bebe
 * aprende de uma vez.
 */
public class InfusionBaseItem extends Item {
    /** O que ela faz a quem a bebe: dez segundos de Definhamento IV. */
    public static final int QUANTO = 200;
    public static final int GRAU = 3;

    public InfusionBaseItem(Properties properties) {
        super(properties);
    }

    public static Properties feitio(Properties properties) {
        return properties.stacksTo(16)
                .food(new net.minecraft.world.food.FoodProperties.Builder()
                        .nutrition(0).saturationModifier(0.0f).alwaysEdible().build())
                .usingConvertsTo(net.minecraft.world.item.Items.GLASS_BOTTLE);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack coisa, Level level, LivingEntity quem) {
        if (!level.isClientSide()) {
            quem.addEffect(new MobEffectInstance(MobEffects.WITHER, QUANTO, GRAU));
        }
        return super.finishUsingItem(coisa, level, quem);
    }
}
