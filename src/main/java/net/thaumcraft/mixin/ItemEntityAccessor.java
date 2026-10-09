package net.thaumcraft.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * O {@code delayBeforeCanPickup} de então, que os golens de juntar leem (só pegam o que já pode ser pego) —
 * e a <b>idade</b>, que o Cinto de Casca precisa de adiantar: o pau que ele larga tem de sumir em três
 * segundos, e o jeito de o dizer é nascer já velho.
 */
@Mixin(ItemEntity.class)
public interface ItemEntityAccessor {
    @Accessor("pickupDelay")
    int thaumcraft$pickupDelay();

    @Accessor("age")
    void thaumcraft$age(int quanta);
}
