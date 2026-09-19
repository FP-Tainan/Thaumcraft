package net.thaumcraft.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/**
 * As partículas da mácula: o {@code FXBreaking} da 4.2.3.5 (um pedacinho da bola de slime, tingido, que some encolhendo
 * e esmaecendo) e o {@code FXDrop} (a gota que pende, cai e se esparrama, com os quadros de pingo do jogo).
 */
public final class TaintFx {
    private static final ItemStackRenderState SCRATCH = new ItemStackRenderState();

    private TaintFx() {
    }

    private static TextureAtlasSprite slime(ClientLevel level) {
        Minecraft mc = Minecraft.getInstance();
        mc.getItemModelResolver().updateForTopItem(SCRATCH, new ItemStack(Items.SLIME_BALL), ItemDisplayContext.GROUND, level, null, 0);
        var material = SCRATCH.pickParticleMaterial(level.getRandom());
        return material != null ? material.sprite() : mc.getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS).missingSprite();
    }

    /** O {@code FXBreaking}: gravidade da neve, meio tamanho, cor e alfa dados, e a vida dada (ou a do jogo, se zero). */
    public static class Breaking extends BreakingItemParticle {
        public Breaking(ClientLevel level, double x, double y, double z, float r, float g, float b, float alpha, int maxAge) {
            super(level, x, y, z, slime(level));
            this.setColor(r, g, b);
            this.setAlpha(alpha);
            this.gravity = 1.0f;
            if (maxAge > 0) this.lifetime = maxAge;
        }

        private float fade(float partial) {
            return Mth.clamp(1.0f - (this.age + partial) / this.lifetime, 0.0f, 1.0f);
        }

        @Override
        public float getQuadSize(float partial) {
            return this.quadSize * this.fade(partial);
        }

        @Override
        public SingleQuadParticle.Layer getLayer() {
            return SingleQuadParticle.Layer.TRANSLUCENT_ITEMS;
        }

        @Override
        public void extract(net.minecraft.client.renderer.state.level.QuadParticleRenderState state, net.minecraft.client.Camera camera, float partial) {
            float a = this.alpha;
            this.alpha = a * this.fade(partial);
            super.extract(state, camera, partial);
            this.alpha = a;
        }

        public void alpha(float alpha) {
            this.setAlpha(alpha);
        }

        public Breaking motion(double xd, double yd, double zd) {
            this.xd = xd;
            this.yd = yd;
            this.zd = zd;
            return this;
        }
    }

    /** O {@code taintLandFX}: um pedaço escuro que espirra em volta de quem caiu. */
    public static void land(Entity e) {
        if (!(e.level() instanceof ClientLevel level)) return;
        var random = level.getRandom();
        float f = random.nextFloat() * (float) Math.PI * 2.0f;
        float f1 = random.nextFloat() * 0.5f + 0.5f;
        float f2 = Mth.sin(f) * 2.0f * 0.5f * f1;
        float f3 = Mth.cos(f) * 2.0f * 0.5f * f1;
        var box = e.getBoundingBox();
        Minecraft.getInstance().particleEngine.add(new Breaking(level, e.getX() + f2, (box.minY + box.maxY) / 2.0, e.getZ() + f3,
                0.1f, 0.0f, 0.1f, 0.4f, (int) (66.0f / (random.nextFloat() * 0.9f + 0.1f))));
    }

    /** O {@code slimeJumpFX}: o respingo roxo do slime ao pular, espalhado pelo tamanho {@code i}. */
    public static void slimeJump(Entity e, int i) {
        if (!(e.level() instanceof ClientLevel level)) return;
        var random = level.getRandom();
        float f = random.nextFloat() * (float) Math.PI * 2.0f;
        float f1 = random.nextFloat() * 0.5f + 0.5f;
        float f2 = Mth.sin(f) * i * 0.5f * f1;
        float f3 = Mth.cos(f) * i * 0.5f * f1;
        var box = e.getBoundingBox();
        Minecraft.getInstance().particleEngine.add(new Breaking(level, e.getX() + f2, (box.minY + box.maxY) / 2.0, e.getZ() + f3,
                0.7f, 0.0f, 1.0f, 0.4f, (int) (66.0f / (random.nextFloat() * 0.9f + 0.1f))));
    }

    /** O {@code splooshFX}: um pedaço roxo em volta de quem acabou de virar maculado (ou do esporo que estoura). */
    public static void sploosh(Entity e) {
        if (!(e.level() instanceof ClientLevel level)) return;
        var random = level.getRandom();
        float f = random.nextFloat() * (float) Math.PI * 2.0f;
        float f1 = random.nextFloat() * 0.5f + 0.5f;
        float f2 = Mth.sin(f) * 2.0f * 0.5f * f1;
        float f3 = Mth.cos(f) * 2.0f * 0.5f * f1;
        boolean light = random.nextBoolean();
        Minecraft.getInstance().particleEngine.add(new Breaking(level, e.getX() + f2, e.getY() + random.nextFloat() * e.getBbHeight(),
                e.getZ() + f3, light ? 0.6f : 0.3f, 0.0f, 0.3f, light ? 0.4f : 0.6f, (int) (66.0f / (random.nextFloat() * 0.9f + 0.1f))));
    }

    /** O {@code tentacleAriseFX}: o tentáculo rompendo o chão — pedaços roxos e do bloco embaixo. */
    public static void tentacleArise(Entity e) {
        if (!(e.level() instanceof ClientLevel level)) return;
        var random = level.getRandom();
        BlockPos below = BlockPos.containing(e.getX(), e.getY(), e.getZ()).below();
        BlockState ground = level.getBlockState(below);
        float h = e.getBbHeight();
        for (int j = 0; j < 2.0f * h; j++) {
            float f = random.nextFloat() * (float) Math.PI * h;
            float f1 = random.nextFloat() * 0.5f + 0.5f;
            float f2 = Mth.sin(f) * h * 0.25f * f1;
            float f3 = Mth.cos(f) * h * 0.25f * f1;
            Minecraft.getInstance().particleEngine.add(new Breaking(level, e.getX() + f2, e.getY(), e.getZ() + f3, 0.4f, 0.0f, 0.4f, 0.5f,
                    (int) (66.0f / (random.nextFloat() * 0.9f + 0.1f))));
            if (!ground.isAir()) {
                f = random.nextFloat() * (float) Math.PI * h;
                f1 = random.nextFloat() * 0.5f + 0.5f;
                level.addParticle(new net.minecraft.core.particles.BlockParticleOption(net.minecraft.core.particles.ParticleTypes.BLOCK, ground),
                        e.getX() + Mth.sin(f) * h * 0.25f * f1, e.getY(), e.getZ() + Mth.cos(f) * h * 0.25f * f1, 0.0, 0.0, 0.0);
            }
        }
    }

    /** O {@code taintsplosionFX}: pedaços roxos voando para todo lado de quem estourou. */
    public static void taintsplosion(Entity e) {
        if (!(e.level() instanceof ClientLevel level)) return;
        var random = level.getRandom();
        Breaking fx = new Breaking(level, e.getX(), e.getY() + random.nextFloat() * e.getBbHeight(), e.getZ(), 0, 0, 0, 1, 0);
        if (random.nextBoolean()) {
            fx.setColor(0.6f, 0.0f, 0.3f);
            fx.alpha(0.4f);
        } else {
            fx.setColor(0.3f, 0.0f, 0.3f);
            fx.alpha(0.6f);
        }
        double mx = Math.random() * 2.0 - 1.0, my = Math.random() * 2.0 - 1.0, mz = Math.random() * 2.0 - 1.0;
        float f = (float) (Math.random() + Math.random() + 1.0) * 0.15f;
        double f1 = Math.sqrt(mx * mx + my * my + mz * mz);
        fx.motion(mx / f1 * f * 0.9640000000596046, my / f1 * f * 0.9640000000596046 + 0.1, mz / f1 * f * 0.9640000000596046);
        Minecraft.getInstance().particleEngine.add(fx);
    }

    /** A gota do {@code FXDrop}: pende quarenta tiques, cai e se esparrama no chão; some ao entrar em fluido ou bloco. */
    public static class Drop extends SingleQuadParticle {
        private final TextureAtlasSprite hang, fall, landed;
        private int bobTimer = 40;

        public Drop(ClientLevel level, double x, double y, double z, float r, float g, float b) {
            super(level, x, y, z, sprite("drip_hang"));
            this.hang = this.sprite;
            this.fall = sprite("drip_fall");
            this.landed = sprite("drip_land");
            this.setColor(r, g, b);
            this.setSize(0.01f, 0.01f);
            this.gravity = 0.06f;
            this.xd = this.yd = this.zd = 0.0;
            this.lifetime = (int) (64.0 / (Math.random() * 0.8 + 0.2));
        }

        private static TextureAtlasSprite sprite(String name) {
            return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.PARTICLES).getSprite(Identifier.withDefaultNamespace(name));
        }

        @Override
        protected SingleQuadParticle.Layer getLayer() {
            return SingleQuadParticle.Layer.OPAQUE;
        }

        @Override
        public int getLightCoords(float partial) {
            return 0xF000F0;
        }

        @Override
        public void tick() {
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
            this.yd -= this.gravity;
            if (this.bobTimer-- > 0) {
                this.xd *= 0.02;
                this.yd *= 0.02;
                this.zd *= 0.02;
                this.sprite = this.hang;
            } else {
                this.sprite = this.fall;
            }
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.98;
            this.yd *= 0.98;
            this.zd *= 0.98;
            if (this.lifetime-- <= 0) this.remove();
            if (this.onGround) {
                this.sprite = this.landed;
                this.xd *= 0.7;
                this.zd *= 0.7;
            }
            BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
            BlockState state = this.level.getBlockState(pos);
            if (!state.getFluidState().isEmpty()) {
                if (this.y < pos.getY() + state.getFluidState().getHeight(this.level, pos)) this.remove();
            } else if (state.isCollisionShapeFullBlock(this.level, pos)) {
                this.remove();
            }
        }
    }

    /** O {@code dropletFX}. */
    public static void droplet(ClientLevel level, double x, double y, double z, float r, float g, float b) {
        Minecraft.getInstance().particleEngine.add(new Drop(level, x, y, z, r, g, b));
    }
}
