package net.thaumcraft.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * A lista de vontades de uma criatura, para pôr uma de fora: é o que o {@code EntityJoinWorldEvent} do Magia
 * Naturalis fazia ao ensinar o creeper a fugir do Baú Maligno.
 */
@Mixin(Mob.class)
public interface MobGoalAccessor {
    @Accessor("goalSelector")
    GoalSelector thaumcraft$goalSelector();
}
