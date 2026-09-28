package net.thaumcraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.RandomSource;

/**
 * O pó que um feitiço deixa no ar: as partículas das Formas de área do Ars Magica 2.
 *
 * <p>É um mote pequeno que <b>sobe devagar</b> e apaga. Os números são os do original: vive <b>vinte
 * batidas</b>, sai a <b>quinze centésimos</b> de tamanho, e sobe com a aceleração do
 * {@code ParticleFloatUpward(0,0; 0,07)} — sem velocidade de partida, ganhando sete centésimos por batida.
 *
 * <p>A cor vem da <b>Afinidade</b> do feitiço e viaja na própria partícula, como a do efeito de poção do jogo.
 * É o que faz uma Zona de fogo ser laranja e uma de gelo ser azul sem ninguém ter escolhido.
 *
 * <p>Ele <b>atravessa bloco</b>, como no original: o pó de um feitiço não bate em parede.
 */
public class SpellParticle extends SingleQuadParticle {
    /** Quanto ele vive: as vinte batidas do original. */
    public static final int LIFE = 20;

    /**
     * O tamanho com que sai.
     *
     * <p><b>Desvio declarado.</b> O original diz {@code setParticleScale(0.15F)}, mas esse número é da escala
     * do motor de partículas <i>dele</i> — no do jogo de hoje, o mesmo 0,15 dá um ponto quase invisível. Aqui
     * o número foi afinado <b>pelo que se vê</b> e não pelo que está escrito, que é o único jeito honesto de
     * portar um número de aparência entre dois motores diferentes.
     */
    public static final float SCALE = 0.25f;

    /** E o quanto ele ganha de subida por batida. */
    public static final double LIFT = 0.07;

    protected SpellParticle(ClientLevel level, double x, double y, double z,
                            double dx, double dy, double dz,
                            ColorParticleOption cor, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        this.setSize(0.02f, 0.02f);
        this.hasPhysics = false;
        this.lifetime = LIFE;
        this.quadSize = SCALE;
        this.setColor(cor.getRed(), cor.getGreen(), cor.getBlue());

        // o empurrão de partida, que quem manda a partícula escolhe
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.yd += LIFT / this.lifetime;
        this.move(this.xd, this.yd, this.zd);

        // e apaga no fim, em vez de sumir de repente
        this.setAlpha(1.0f - (float) this.age / this.lifetime);
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    /** Quem cria o pó a partir da partícula registrada. */
    public record Provider(SpriteSet sprites) implements ParticleProvider<ColorParticleOption> {
        @Override
        public SpellParticle createParticle(ColorParticleOption cor, ClientLevel level,
                                            double x, double y, double z,
                                            double dx, double dy, double dz, RandomSource sorte) {
            return new SpellParticle(level, x, y, z, dx, dy, dz, cor, this.sprites);
        }
    }
}
