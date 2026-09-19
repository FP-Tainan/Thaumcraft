package net.thaumcraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.RandomSource;

/**
 * O vapor do cano que sangra. É o {@code FXVent} da 4.2.3.5, descompilado.
 *
 * <p>Não é fumaça de fogueira: ele nasce pequenino — de cinco a quinze centésimos do tamanho cheio — e
 * cresce quinze por cento a cada tique, trocando de desenho enquanto cresce e apagando na mesma medida,
 * até chegar ao tamanho cheio e sumir. Os cinco desenhos são os tufos da primeira linha da folha
 * {@code misc/particles.png} do mod, do mais denso ao que já se desfez.
 *
 * <p>Ele sai atirado a um oitavo de bloco por tique, com um tremor sorteado no rumo, perde quinze por cento
 * da velocidade a cada tique e sobe de leve. Atravessa bloco, como o original.
 */
public class VentParticle extends SingleQuadParticle {
    /** O tamanho cheio, onde o vapor some (o {@code psm} do original, que o {@code setScale} multiplica). */
    private final float FULL;
    /** O alfa com que o cano pinta o vapor. */
    private static final float ALPHA = 0.4f;

    private final SpriteSet sprites;
    /** O tamanho de agora, que vai crescendo até {@link #FULL}. */
    private float grown;

    protected VentParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz,
                           ColorParticleOption colour, SpriteSet sprites, RandomSource random, float scale) {
        super(level, x, y, z, sprites.first());
        this.FULL = scale;
        this.sprites = sprites;
        this.setSize(0.02f, 0.02f);
        this.grown = (random.nextFloat() * 0.1f + 0.05f) * scale;
        this.hasPhysics = false;
        this.lifetime = 200;
        this.setColor(colour.getRed(), colour.getGreen(), colour.getBlue());

        // o setHeading do original: aponta, treme um pouco e sai a um oitavo de bloco por tique
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0e-6) length = 1.0;
        double shake = 0.0075 * 5.0;
        this.xd = (dx / length + random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * shake) * 0.125;
        this.yd = (dy / length + random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * shake) * 0.125;
        this.zd = (dz / length + random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * shake) * 0.125;
        this.refresh();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.age++;
        if (this.grown > FULL) {
            this.remove();
            return;
        }
        this.yd += 0.0025;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.85;
        this.yd *= 0.85;
        this.zd *= 0.85;
        if (this.grown < FULL) this.grown *= 1.15f;
        if (this.onGround) {
            this.xd *= 0.7;
            this.zd *= 0.7;
        }
        this.refresh();
    }

    /** O desenho, o tamanho e o alfa acompanham o quanto ele já cresceu. */
    private void refresh() {
        int frame = Math.min(4, (int) (this.grown / FULL * 4.0f));
        this.setSprite(this.sprites.get(frame, 4));
        this.quadSize = 0.3f * this.grown;
        this.setAlpha(Math.max(0.0f, ALPHA * (FULL - this.grown) / FULL));
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    /** Quem cria o vapor a partir da partícula registrada. */
    public static class Provider implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet sprites;
        private final float scale;

        public Provider(SpriteSet sprites) {
            this(sprites, 1.0f);
        }

        /** O vapor ampliado ({@code setScale}): o da abertura do caranguejo sai no dobro. */
        public Provider(SpriteSet sprites, float scale) {
            this.sprites = sprites;
            this.scale = scale;
        }

        @Override
        public VentParticle createParticle(ColorParticleOption colour, ClientLevel level, double x, double y, double z,
                                           double dx, double dy, double dz, RandomSource random) {
            return new VentParticle(level, x, y, z, dx, dy, dz, colour, this.sprites, random, this.scale);
        }
    }
}
