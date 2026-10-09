package net.thaumcraft.mixin;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * O <b>barulho que o bicho faz por si</b>, para se poder pedir fora de hora.
 *
 * <p>É o que o Amuleto da Polinésia precisa: o {@code playGreeting} dele chama o {@code playLivingSound}
 * três vezes seguidas, que é o cumprimento de uma vaca que acaba de virar vendedora. E é também o que cada
 * troca fechada faz — a vaca responde.
 */
@Mixin(Mob.class)
public interface MobAmbientSoundInvoker {
    @Invoker("playAmbientSound")
    void thaumcraft$barulho();
}
