package net.thaumcraft.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** O {@code delayBeforeCanPickup} de então, que os golens de juntar leem (só pegam o que já pode ser pego). */
@Mixin(ItemEntity.class)
public interface ItemEntityAccessor {
    @Accessor("pickupDelay")
    int thaumcraft$pickupDelay();
}
