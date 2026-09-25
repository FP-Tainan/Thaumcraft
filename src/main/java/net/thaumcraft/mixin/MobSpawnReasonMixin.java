package net.thaumcraft.mixin;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.thaumcraft.mortuorum.MortuorumEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Por que aquela criatura nasceu.
 *
 * <p>O Ars Mortuorum troca um em cada trinta zumbis e esqueletos <b>que o mundo faz nascer</b> por um Rastejador
 * da Noite ou um Isaac — e só esses: o que vem de ovo, de comando ou de um teste tem de ficar como é. O jogo de
 * hoje não guarda a razão do nascimento em lado nenhum que se possa ler depois, então ela fica marcada aqui.
 */
@Mixin(Mob.class)
public abstract class MobSpawnReasonMixin {
    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    private void thaumcraft$marcaRazao(ServerLevelAccessor level, DifficultyInstance difficulty,
                                       EntitySpawnReason reason, SpawnGroupData data,
                                       CallbackInfoReturnable<SpawnGroupData> info) {
        MortuorumEvents.mark((Mob) (Object) this, reason);
    }
}
