package net.thaumcraft.arcana;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.registry.TCParticles;

/**
 * O pó que os feitiços deixam no ar: as partículas das Formas do Ars Magica 2, do lado de quem as manda.
 *
 * <p>Todas elas fazem a mesma coisa — pedir ao mundo que ponha motes da <b>cor da Afinidade</b> num lugar —,
 * e o que muda de uma Forma para outra é <b>onde</b>: a Zona põe em roda, a Parede põe em linha, a Corrente põe
 * entre um elo e o seguinte.
 *
 * <p>Os ritmos são os do original: a Zona põe <b>quatro</b> motes de duas em duas batidas, girando dez graus de
 * cada vez; a Parede põe um a cada <b>meio bloco</b> da linha dela.
 */
public final class SpellFx {
    /** Quantos motes a Zona põe de cada vez, em roda: os quatro do original. */
    public static final int ZONE_MOTES = 4;

    /** Quanto ela gira a cada volta: os dez graus do {@code rotationSpeed}. */
    public static final float ZONE_SPIN = 10.0f;

    /** De quantas em quantas batidas ela põe. */
    public static final int ZONE_EVERY = 2;

    /** E de quanto em quanto se põe um mote ao longo de uma linha: o meio bloco do original. */
    public static final double LINE_STEP = 0.5;

    private SpellFx() {
    }

    /** A cor com que um feitiço pinta o seu pó. */
    public static int color(Spell feitiço) {
        Affinity qual = feitiço.mainAffinity();
        return qual == Affinity.NONE ? Affinity.NONE.color : qual.color;
    }

    /** Um mote parado naquele ponto. */
    public static void mote(ServerLevel level, Spell feitiço, Vec3 onde) {
        mote(level, color(feitiço), onde, Vec3.ZERO, 0.0);
    }

    /**
     * Um mote naquele ponto, com empurrão e tremor.
     *
     * @param tremor o quanto ele pode nascer fora do ponto, em cada eixo
     */
    public static void mote(ServerLevel level, int cor, Vec3 onde, Vec3 empurrão, double tremor) {
        level.sendParticles(ColorParticleOption.create(TCParticles.SPELL, cor),
                onde.x, onde.y, onde.z, 0, empurrão.x, empurrão.y, empurrão.z, 1.0);
        if (tremor > 0.0) {
            level.sendParticles(ColorParticleOption.create(TCParticles.SPELL, cor),
                    onde.x, onde.y, onde.z, 1, tremor, tremor, tremor, 0.0);
        }
    }

    /**
     * O anel da <b>Zona</b>: quatro motes em roda, girando.
     *
     * <p>O ângulo sai da idade, e é o que os faz andar em volta em vez de piscarem sempre nos mesmos quatro
     * pontos.
     */
    public static void zone(ServerLevel level, Spell feitiço, Vec3 meio, double raio, int idade) {
        if (idade % ZONE_EVERY != 0) return;
        int cor = color(feitiço);
        float giro = idade * ZONE_SPIN % 360.0f;

        for (int i = 0; i < ZONE_MOTES; i++) {
            double ângulo = Math.toRadians((giro + 90.0f * i) % 360.0f);
            Vec3 onde = meio.add(-Math.cos(ângulo) * raio, 0.0, -Math.sin(ângulo) * raio);
            mote(level, cor, onde, Vec3.ZERO, 0.0);
        }
    }

    /**
     * A linha da <b>Parede</b> e da <b>Onda</b>: um mote a cada meio bloco, com um bloco de tremor.
     *
     * <p>O tremor é o {@code addRandomOffset(1, 1, 1)} do original, e é o que faz a parede parecer uma cortina
     * em vez de um fio.
     */
    public static void line(ServerLevel level, Spell feitiço, Vec3 de, Vec3 até) {
        int cor = color(feitiço);
        double comprimento = de.distanceTo(até);
        int passos = Math.max(1, (int) Math.ceil(comprimento / LINE_STEP));

        for (int i = 0; i <= passos; i++) {
            Vec3 onde = de.add(até.subtract(de).scale((double) i / passos));
            mote(level, cor, onde, Vec3.ZERO, 1.0);
        }
    }

    /**
     * O facho da <b>Corrente</b>, de um elo ao seguinte.
     *
     * <p><b>Desvio declarado:</b> o original desenha um facho de verdade — uma tira contínua entre os dois
     * pontos, e um <i>raio</i> se a Afinidade for a do Relâmpago. Aqui é uma fila de motes bem juntos, que dá
     * a mesma leitura e não precisa de um desenhista próprio.
     */
    public static void chain(ServerLevel level, Spell feitiço, Vec3 de, Vec3 até) {
        int cor = color(feitiço);
        double comprimento = de.distanceTo(até);
        int passos = Math.max(2, (int) Math.ceil(comprimento * 4.0));

        for (int i = 0; i <= passos; i++) {
            Vec3 onde = de.add(até.subtract(de).scale((double) i / passos));
            mote(level, cor, onde, Vec3.ZERO, 0.0);
        }
    }

    /** E o rastro do <b>Projétil</b>, que é um mote por batida onde ele está. */
    public static void trail(ServerLevel level, Affinity qual, Vec3 onde) {
        mote(level, qual == Affinity.NONE ? Affinity.NONE.color : qual.color, onde, Vec3.ZERO, 0.05);
    }
}
