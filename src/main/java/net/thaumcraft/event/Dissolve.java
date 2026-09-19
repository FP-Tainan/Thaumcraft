package net.thaumcraft.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.item.CrystalEssenceItem;
import net.thaumcraft.registry.TCDamageTypes;
import net.thaumcraft.research.ScanManager;

/**
 * O que a morte líquida deixa: o {@code livingDrops} do {@code EventHandlerEntity} da 4.2.3.5. A criatura dissolvida
 * solta, de cada aspecto dela e com meia chance, cristais de essência — de um até metade do tanto que ela tem.
 */
public final class Dissolve {
    private Dissolve() {
    }

    public static void init() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!source.is(TCDamageTypes.DISSOLVE)) return;
            AspectList aspects = ScanManager.aspectsOf(entity);
            if (aspects == null || aspects.size() == 0) return;
            var random = entity.level().getRandom();
            for (Aspect aspect : aspects.getAspects()) {
                if (random.nextBoolean()) continue;
                int size = 1 + random.nextInt(aspects.getAmount(aspect));
                size = Math.max(1, size / 2);
                var stack = CrystalEssenceItem.of(aspect);
                stack.setCount(size);
                entity.level().addFreshEntity(new ItemEntity(entity.level(), entity.getX(), entity.getY() + entity.getEyeHeight(),
                        entity.getZ(), stack));
            }
        });
    }
}
