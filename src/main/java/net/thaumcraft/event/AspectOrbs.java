package net.thaumcraft.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.entity.LivingEntity;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.block.entity.DeconstructionTableBlockEntity;
import net.thaumcraft.entity.AspectOrbEntity;
import net.thaumcraft.research.ScanManager;

/**
 * O pedaço do {@code livingTick(LivingDeathEvent)} do {@code EventHandlerEntity}: o que morre depois de apanhar de
 * alguém solta orbes dos primordiais de que é feito — metade das vezes cada um, de 1 até o tanto que tem.
 */
public final class AspectOrbs {
    private AspectOrbs() {
    }

    public static void init() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> drop(entity));
    }

    public static void drop(LivingEntity entity) {
        if (entity.level().isClientSide() || entity.getLastHurtByPlayerMemoryTime() <= 0) return;
        AspectList compound = ScanManager.aspectsOf(entity);
        if (compound == null || compound.size() == 0) return;
        AspectList aspects = DeconstructionTableBlockEntity.reduceToPrimals(compound);
        for (Aspect aspect : aspects.getAspects()) {
            if (!entity.getRandom().nextBoolean()) continue;
            int amount = aspects.getAmount(aspect);
            if (amount <= 0) continue;
            entity.level().addFreshEntity(new AspectOrbEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(),
                    aspect, 1 + entity.getRandom().nextInt(amount)));
        }
    }
}
