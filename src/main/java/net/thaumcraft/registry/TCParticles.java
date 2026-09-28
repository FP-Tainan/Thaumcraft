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

    /** O mesmo vapor no dobro do tamanho: o que sai da abertura dos caranguejos ({@code drawVentParticles} com escala 2). */
    public static final ParticleType<ColorParticleOption> VENT_LARGE = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE, Thaumcraft.id("vent_large"),
            FabricParticleTypes.complex(ColorParticleOption::codec, ColorParticleOption::streamCodec));

    /**
     * O <b>pó de feitiço</b> do Ars Arcana: o mote que a Zona, a Parede, a Onda e a Corrente deixam no ar.
     *
     * <p>No original cada Afinidade tem a sua figura de partícula; aqui é uma só, e a cor vem da Afinidade
     * do feitiço, que viaja na própria partícula. Declarado no {@code docs/PORTE.md}.
     */
    public static final ParticleType<ColorParticleOption> SPELL = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE, Thaumcraft.id("spell"),
            FabricParticleTypes.complex(ColorParticleOption::codec, ColorParticleOption::streamCodec));

    private TCParticles() {
    }

    public static void init() {
    }
}
