package net.thaumcraft.mixin;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * A experiência que um bicho dá ao morrer, para se poder pôr a zero.
 *
 * <p>É o que o Witchery faz por reflexão — o {@code ReflectionHelper.findField(EntityLiving.class,
 * "experienceValue")} do {@code Shapeshift} — ao chamar os cães do uivo de um lobisomem de grau alto. Eles
 * vêm para morrer, e por isso <b>não podem valer nada</b>: sem isto, um lobisomem de grau dez tem uma fábrica
 * de experiência que basta uivar para ligar.
 */
@Mixin(Mob.class)
public interface MobXpAccessor {
    @Accessor("xpReward")
    void thaumcraft$experiência(int quanto);
}
