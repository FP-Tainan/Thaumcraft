package net.thaumcraft.registry;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.thaumcraft.Thaumcraft;

/**
 * As partículas próprias do Thaumcraft.
 *
 * <p>O mod original tem o seu próprio motor de partículas, com uma folha de desenhos dele
 * ({@code misc/particles.png}) e efeitos que não se parecem com nada do Minecraft. Onde a aparência
 * depende deles, o porte registra a partícula de verdade em vez de pegar emprestada uma do jogo.
 */
public final class TCParticles {
    /**
     * O vapor que o cano solta quando sangra: o {@code FXVent} do original, na cor do aspecto.
     *
     * <p>A cor viaja na própria partícula, como a do efeito de poção do jogo.
     */
    public static final ParticleType<ColorParticleOption> VENT = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE, Thaumcraft.id("vent"),
            FabricParticleTypes.complex(ColorParticleOption::codec, ColorParticleOption::streamCodec));

    private TCParticles() {
    }

    public static void init() {
    }
}
