package net.thaumcraft.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * Os {@code drawInfusionParticles} do {@code ClientProxy}: o que sai de um pedestal rumo à matriz enquanto ela consome o
 * que está em cima dele — migalhas do item (1) ou do bloco (2), uma faísca roxa (3) — e a faísca verde que sai de quem
 * paga a infusão de encantamento com experiência (4). Todas miram meio bloco abaixo da matriz.
 */
public final class InfusionFx {
    private InfusionFx() {
    }

    /** Um em três, a faísca roxa; senão, migalhas do bloco (se o item for um bloco) ou do item (o {@code particleCount(2)}, quatro com as partículas no máximo). */
    public static void fromPedestal(ClientLevel level, BlockPos pedestal, BlockPos matrix, ItemStack stack) {
        RandomSource random = level.getRandom();
        if (random.nextInt(3) == 0) {
            spark(pedestal.getX() + random.nextFloat(), pedestal.getY() + random.nextFloat() + 1.0f, pedestal.getZ() + random.nextFloat(),
                    matrix, 0.4f + random.nextFloat() * 0.2f, 0.2f, 0.6f + random.nextFloat() * 0.3f);
        } else if (stack.getItem() instanceof BlockItem block) {
            for (int a = 0; a < 4; a++) {
                Minecraft.getInstance().particleEngine.add(new BoreParticle(level, pedestal.getX() + random.nextFloat(),
                        pedestal.getY() + random.nextFloat() + 1.0f, pedestal.getZ() + random.nextFloat(), matrix.getX() + 0.5,
                        matrix.getY() - 0.5, matrix.getZ() + 0.5, block.getBlock().defaultBlockState(), matrix));
            }
        } else {
            TextureAtlasSprite sprite = Sprites.INSTANCE.of(stack, level, random);
            for (int a = 0; a < 4; a++) {
                ItemBit bit = new ItemBit(level, pedestal.getX() + 0.4f + random.nextFloat() * 0.2f,
                        pedestal.getY() + 1.23f + random.nextFloat() * 0.2f, pedestal.getZ() + 0.4f + random.nextFloat() * 0.2f,
                        matrix.getX() + 0.5, matrix.getY() - 0.5, matrix.getZ() + 0.5, sprite);
                // o drawInfusionParticles1 sacode as migalhas do item com mais força
                bit.push(random.nextGaussian() * 0.03f, random.nextGaussian() * 0.03f, random.nextGaussian() * 0.03f);
                Minecraft.getInstance().particleEngine.add(bit);
            }
        }
    }

    /** O {@code drawInfusionParticles4}: a faísca verde da experiência, de um ponto de quem paga. */
    public static void experience(double x, double y, double z, BlockPos matrix, RandomSource random) {
        spark(x, y, z, matrix, 0.2f, 0.6f + random.nextFloat() * 0.3f, 0.3f);
    }

    private static void spark(double x, double y, double z, BlockPos matrix, float r, float g, float b) {
        RandomSource random = Minecraft.getInstance().level.getRandom();
        ThaumFx.add(new BoreFx.Spark(x, y, z, matrix.getX() + 0.5, matrix.getY() - 0.5, matrix.getZ() + 0.5, random).colour(r, g, b));
    }

    /** O desenho do item que as migalhas usam: o mesmo que o jogo usa para o item quebrando. */
    private static final class Sprites extends BreakingItemParticle.ItemParticleProvider<SimpleParticleType> {
        static final Sprites INSTANCE = new Sprites();

        TextureAtlasSprite of(ItemStack stack, ClientLevel level, RandomSource random) {
            return this.getSprite(ItemStackTemplate.fromNonEmptyStack(stack), level, random);
        }

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z,
                                       double xd, double yd, double zd, RandomSource random) {
            return null;
        }
    }

    /** O {@code FXBoreParticles} com item: um quarto do desenho do item, cinza (0,6), voando para o alvo. */
    static final class ItemBit extends BreakingItemParticle {
        private final double targetX, targetY, targetZ;

        ItemBit(ClientLevel level, double x, double y, double z, double tx, double ty, double tz, TextureAtlasSprite sprite) {
            super(level, x, y, z, sprite);
            this.targetX = tx;
            this.targetY = ty;
            this.targetZ = tz;
            this.rCol = this.gCol = this.bCol = 0.6f;
            this.quadSize = 0.1f * (this.random.nextFloat() * 0.3f + 0.4f);
            double dx = tx - x, dy = ty - y, dz = tz - z;
            int base = Math.max(1, (int) (Math.sqrt(dx * dx + dy * dy + dz * dz) * 3.0));
            this.lifetime = base / 2 + this.random.nextInt(base);
            this.xd = level.getRandom().nextGaussian() * 0.01;
            this.yd = level.getRandom().nextGaussian() * 0.01;
            this.zd = level.getRandom().nextGaussian() * 0.01;
            this.gravity = 0.2f;
            this.hasPhysics = true;
            var viewer = Minecraft.getInstance().getCameraEntity();
            if (viewer != null && viewer.distanceToSqr(x, y, z) > 64.0 * 64.0) this.lifetime = 0;
        }

        void push(double xd, double yd, double zd) {
            this.xd = xd;
            this.yd = yd;
            this.zd = zd;
        }

        @Override
        public void tick() {
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
            boolean arrived = Mth.floor(this.x) == Mth.floor(this.targetX) && Mth.floor(this.y) == Mth.floor(this.targetY)
                    && Mth.floor(this.z) == Mth.floor(this.targetZ);
            if (this.age++ >= this.lifetime || arrived) {
                this.remove();
                return;
            }
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.985;
            this.yd *= 0.985;
            this.zd *= 0.985;
            double dx = this.targetX - this.x, dy = this.targetY - this.y, dz = this.targetZ - this.z;
            double speed = 0.3;
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (d < 4.0) {
                this.quadSize *= 0.9f;
                speed = 0.6;
            }
            this.xd = Mth.clamp(this.xd + dx / d * speed, -0.35, 0.35);
            this.yd = Mth.clamp(this.yd + dy / d * speed, -0.35, 0.35);
            this.zd = Mth.clamp(this.zd + dz / d * speed, -0.35, 0.35);
        }
    }
}
