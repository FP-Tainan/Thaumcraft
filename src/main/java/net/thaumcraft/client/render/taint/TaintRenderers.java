package net.thaumcraft.client.render.taint;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.taint.TaintChickenEntity;
import net.thaumcraft.entity.taint.TaintCreeperEntity;
import net.thaumcraft.entity.taint.TaintSheepEntity;
import net.thaumcraft.entity.taint.TaintSporeEntity;
import net.thaumcraft.entity.taint.TaintacleEntity;
import net.thaumcraft.entity.taint.ThaumicSlimeEntity;

/**
 * Os desenhistas da fauna da mácula: o {@code RenderTaintChicken}, {@code Cow}, {@code Pig}, {@code Sheep},
 * {@code Creeper}, {@code Villager}, {@code RenderThaumicSlime}, {@code RenderTaintSpore},
 * {@code RenderTaintSporeSwarmer} e {@code RenderTaintacle} da 4.2.3.5.
 */
public final class TaintRenderers {
    private TaintRenderers() {
    }

    public static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(Thaumcraft.id(name), "main");
    }

    public static final ModelLayerLocation CHICKEN = layer("taint_chicken"), COW = layer("taint_cow"), PIG = layer("taint_pig"),
            SHEEP = layer("taint_sheep"), SHEEP_WOOL = layer("taint_sheep_wool"), CREEPER = layer("taint_creeper"),
            VILLAGER = layer("taint_villager"), SLIME_INNER = layer("thaumic_slime"), SLIME_OUTER = layer("thaumic_slime_outer"),
            SPORE = layer("taint_spore"), SWARMER_INNER = layer("taint_swarmer"), SWARMER_OUTER = layer("taint_swarmer_outer"),
            TAINTACLE = layer("taintacle"), TAINTACLE_SMALL = layer("taintacle_small");

    static Identifier texture(String name) {
        return Thaumcraft.id("textures/models/" + name + ".png");
    }

    /** O desenhista comum dos bichos: modelo, pele e sombra. */
    public static class Beast<T extends Mob> extends MobRenderer<T, TaintModels.State, net.minecraft.client.model.EntityModel<TaintModels.State>> {
        private final Identifier skin;

        public Beast(EntityRendererProvider.Context context, net.minecraft.client.model.EntityModel<TaintModels.State> model, float shadow, String skin) {
            super(context, model, shadow);
            this.skin = texture(skin);
        }

        @Override
        public TaintModels.State createRenderState() {
            return new TaintModels.State();
        }

        @Override
        public Identifier getTextureLocation(TaintModels.State state) {
            return this.skin;
        }

        @Override
        public void extractRenderState(T entity, TaintModels.State state, float partial) {
            super.extractRenderState(entity, state, partial);
            if (entity instanceof TaintChickenEntity chicken) {
                float flap = Mth.lerp(partial, chicken.oFlap, chicken.flap);
                float speed = Mth.lerp(partial, chicken.oFlapSpeed, chicken.flapSpeed);
                state.wing = (Mth.sin(flap) + 1.0f) * speed;
            }
            if (entity instanceof TaintSheepEntity sheep) {
                state.headEatPosition = sheep.headEatPositionScale(partial);
                state.headEatAngle = sheep.headEatAngleScale(partial);
                state.sheared = sheep.isSheared();
            }
            if (entity instanceof TaintCreeperEntity creeper) state.swelling = creeper.getSwelling(partial);
        }
    }

    public static Beast<TaintChickenEntity> chicken(EntityRendererProvider.Context context) {
        return new Beast<>(context, new TaintModels.Chicken(context.bakeLayer(CHICKEN)), 0.3f, "chicken");
    }

    public static Beast<net.thaumcraft.entity.taint.TaintCowEntity> cow(EntityRendererProvider.Context context) {
        return new Beast<>(context, new TaintModels.Quadruped(context.bakeLayer(COW), false), 0.7f, "cow");
    }

    public static Beast<net.thaumcraft.entity.taint.TaintPigEntity> pig(EntityRendererProvider.Context context) {
        return new Beast<>(context, new TaintModels.Quadruped(context.bakeLayer(PIG), false), 0.7f, "pig");
    }

    /** A ovelha: o corpo sem lã e, por cima, a lã ({@code sheep_fur.png}) enquanto não tosquiada. */
    public static class Sheep extends Beast<TaintSheepEntity> {
        public Sheep(EntityRendererProvider.Context context) {
            super(context, new TaintModels.Quadruped(context.bakeLayer(SHEEP), true), 0.7f, "sheep");
            TaintModels.Quadruped wool = new TaintModels.Quadruped(context.bakeLayer(SHEEP_WOOL), true);
            Identifier fur = texture("sheep_fur");
            this.addLayer(new RenderLayer<>(this) {
                @Override
                public void submit(PoseStack pose, SubmitNodeCollector collector, int light, TaintModels.State state, float yRot, float xRot) {
                    if (state.sheared || state.isInvisible) return;
                    collector.submitModel(wool, state, pose, RenderTypes.entityCutout(fur), light, LivingEntityRenderer.getOverlayCoords(state, 0.0f),
                            -1, null, state.outlineColor, null);
                }
            });
        }
    }

    /** O creeper: incha e pisca branco para estourar (o {@code updateCreeperScale}/{@code ColorMultiplier}). */
    public static class Creeper extends Beast<TaintCreeperEntity> {
        public Creeper(EntityRendererProvider.Context context) {
            super(context, new TaintModels.Creeper(context.bakeLayer(CREEPER)), 0.5f, "creeper");
        }

        @Override
        protected void scale(TaintModels.State state, PoseStack pose) {
            float f = state.swelling;
            float wobble = 1.0f + Mth.sin(f * 100.0f) * f * 0.01f;
            f = Mth.clamp(f, 0.0f, 1.0f);
            f *= f;
            f *= f;
            float xz = (1.0f + f * 0.4f) * wobble;
            float y = (1.0f + f * 0.1f) / wobble;
            pose.scale(xz, y, xz);
        }

        @Override
        protected float getWhiteOverlayProgress(TaintModels.State state) {
            float f = state.swelling;
            return (int) (f * 10.0f) % 2 == 0 ? 0.0f : Mth.clamp(f * 0.2f, 0.0f, 1.0f);
        }
    }

    /** O aldeão, a 15/16 do tamanho. */
    public static class Villager extends Beast<net.thaumcraft.entity.taint.TaintVillagerEntity> {
        public Villager(EntityRendererProvider.Context context) {
            super(context, new TaintModels.Villager(context.bakeLayer(VILLAGER)), 0.5f, "villager");
        }

        @Override
        protected void scale(TaintModels.State state, PoseStack pose) {
            pose.scale(0.9375f, 0.9375f, 0.9375f);
        }
    }

    // ------------------------------------------------------------------------------------------------ o slime

    /** O slime taumático: o miolo por dentro e a casca translúcida por fora, esmagando no pulo. */
    public static class Slime extends MobRenderer<ThaumicSlimeEntity, TaintOddModels.State, TaintOddModels.Plain> {
        private static final Identifier SKIN = texture("tslime");
        private final TaintOddModels.Plain outer;

        public Slime(EntityRendererProvider.Context context) {
            super(context, new TaintOddModels.Plain(context.bakeLayer(SLIME_INNER)), 0.25f);
            this.outer = new TaintOddModels.Plain(context.bakeLayer(SLIME_OUTER));
            this.addLayer(new RenderLayer<>(this) {
                @Override
                public void submit(PoseStack pose, SubmitNodeCollector collector, int light, TaintOddModels.State state, float yRot, float xRot) {
                    if (state.isInvisible) return;
                    collector.submitModel(Slime.this.outer, state, pose, RenderTypes.entityTranslucent(SKIN), light,
                            LivingEntityRenderer.getOverlayCoords(state, 0.0f), -1, null, state.outlineColor, null);
                }
            });
        }

        @Override
        public TaintOddModels.State createRenderState() {
            return new TaintOddModels.State();
        }

        @Override
        public Identifier getTextureLocation(TaintOddModels.State state) {
            return SKIN;
        }

        @Override
        public void extractRenderState(ThaumicSlimeEntity slime, TaintOddModels.State state, float partial) {
            super.extractRenderState(slime, state, partial);
            state.size = slime.getSize();
            state.squish = Mth.lerp(partial, slime.oSquish, slime.squish);
        }

        /** O {@code scaleSlime}. */
        @Override
        protected void scale(TaintOddModels.State state, PoseStack pose) {
            float f1 = (float) Math.sqrt(state.size);
            float f2 = state.squish / (f1 * 0.25f + 1.0f);
            float f3 = 1.0f / (f2 + 1.0f);
            pose.scale(f3 * f1 + 0.1f, 1.0f / f3 * f1 + 0.1f, f3 * f1 + 0.1f);
        }

        @Override
        protected float getShadowRadius(TaintOddModels.State state) {
            return 0.25f;
        }
    }

    // ------------------------------------------------------------------------------------------------ os esporos

    /** O esporo: translúcido, aceso, crescendo até o tamanho dele e pulsando; morto não tomba. */
    public static class Spore extends MobRenderer<TaintSporeEntity, TaintOddModels.State, TaintOddModels.Spore> {
        private static final Identifier SKIN = texture("taint_spore");

        public Spore(EntityRendererProvider.Context context) {
            super(context, new TaintOddModels.Spore(context.bakeLayer(SPORE)), 0.25f);
        }

        @Override
        public TaintOddModels.State createRenderState() {
            return new TaintOddModels.State();
        }

        @Override
        public Identifier getTextureLocation(TaintOddModels.State state) {
            return SKIN;
        }

        @Override
        public void extractRenderState(TaintSporeEntity spore, TaintOddModels.State state, float partial) {
            super.extractRenderState(spore, state, partial);
            state.sporeSize = spore.renderSize(partial);
            state.pulse = spore.pulse();
            state.hurt = spore.hurtTime > 0;
            state.lightCoords = 0xF000F0;
        }

        @Override
        protected void scale(TaintOddModels.State state, PoseStack pose) {
            float f3 = -0.12f;
            pose.scale(f3 * state.sporeSize - state.pulse, f3 * state.sporeSize + state.pulse, f3 * state.sporeSize - state.pulse);
        }

        @Override
        protected RenderType getRenderType(TaintOddModels.State state, boolean visible, boolean translucent, boolean glowing) {
            return RenderTypes.entityTranslucent(SKIN);
        }

        @Override
        protected float getFlipDegrees() {
            return 0.0f;
        }
    }

    /**
     * O enxameador: o cubo de dentro, aceso e pulsando do tamanho do esporo, e o de fora com a luz do lugar — os dois
     * translúcidos. Morto não tomba.
     */
    public static class Swarmer extends LivingEntityRenderer<net.thaumcraft.entity.taint.TaintSporeSwarmerEntity, TaintOddModels.State, TaintOddModels.Plain> {
        private static final Identifier SKIN = texture("taint_spore");
        private final TaintOddModels.Spore inner;

        public Swarmer(EntityRendererProvider.Context context) {
            super(context, new TaintOddModels.Plain(context.bakeLayer(SWARMER_OUTER)), 0.25f);
            this.inner = new TaintOddModels.Spore(context.bakeLayer(SWARMER_INNER));
            this.addLayer(new RenderLayer<>(this) {
                @Override
                public void submit(PoseStack pose, SubmitNodeCollector collector, int light, TaintOddModels.State state, float yRot, float xRot) {
                    pose.pushPose();
                    float f3 = -0.07f;
                    pose.translate(0.0f, 1.6f, 0.0f);
                    pose.scale(f3 * state.sporeSize - state.pulse, f3 * state.sporeSize + state.pulse, f3 * state.sporeSize - state.pulse);
                    pose.translate(0.0f, -(f3 * state.sporeSize + state.pulse) / 2.0f, 0.0f);
                    collector.submitModel(Swarmer.this.inner, state, pose, RenderTypes.entityTranslucent(SKIN), 0xF000F0,
                            LivingEntityRenderer.getOverlayCoords(state, 0.0f), -1, null, state.outlineColor, null);
                    pose.popPose();
                }
            });
        }

        @Override
        public TaintOddModels.State createRenderState() {
            return new TaintOddModels.State();
        }

        @Override
        public Identifier getTextureLocation(TaintOddModels.State state) {
            return SKIN;
        }

        @Override
        public void extractRenderState(net.thaumcraft.entity.taint.TaintSporeSwarmerEntity spore, TaintOddModels.State state, float partial) {
            super.extractRenderState(spore, state, partial);
            state.sporeSize = spore.displaySize;
            state.pulse = spore.pulse();
            state.hurt = spore.hurtTime > 0;
        }

        @Override
        protected RenderType getRenderType(TaintOddModels.State state, boolean visible, boolean translucent, boolean glowing) {
            return RenderTypes.entityTranslucent(SKIN);
        }

        @Override
        protected float getFlipDegrees() {
            return 0.0f;
        }

        @Override
        protected boolean shouldShowName(net.thaumcraft.entity.taint.TaintSporeSwarmerEntity entity, double distance) {
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------ o tentáculo

    /**
     * O tentáculo: brota do chão nos primeiros tiques (altura vezes dez), do tamanho da altura dele, translúcido; os
     * gomos com a luz do lugar e a bolinha e a cabeça acesas.
     */
    public static class Taintacle extends MobRenderer<TaintacleEntity, TaintOddModels.State, TaintOddModels.Taintacle> {
        private static final Identifier SKIN = texture("taintacle");
        private final TaintOddModels.Taintacle tips;

        public Taintacle(EntityRendererProvider.Context context, ModelLayerLocation layer, int length, float shadow) {
            super(context, new TaintOddModels.Taintacle(context.bakeLayer(layer), length, false), shadow);
            this.tips = new TaintOddModels.Taintacle(context.bakeLayer(layer), length, true);
            this.addLayer(new RenderLayer<>(this) {
                @Override
                public void submit(PoseStack pose, SubmitNodeCollector collector, int light, TaintOddModels.State state, float yRot, float xRot) {
                    collector.submitModel(Taintacle.this.tips, state, pose, RenderTypes.entityTranslucent(SKIN), 0xF000F0,
                            LivingEntityRenderer.getOverlayCoords(state, 0.0f), -1, null, state.outlineColor, null);
                }
            });
        }

        @Override
        public TaintOddModels.State createRenderState() {
            return new TaintOddModels.State();
        }

        @Override
        public Identifier getTextureLocation(TaintOddModels.State state) {
            return SKIN;
        }

        @Override
        public void extractRenderState(TaintacleEntity entity, TaintOddModels.State state, float partial) {
            super.extractRenderState(entity, state, partial);
            state.agitated = entity.agitated();
            state.flail = entity.flailIntensity;
            state.hurtTicks = entity.hurtTime;
            state.attackTicks = entity.attackTime;
            state.height = entity.getBbHeight();
            state.tickCount = entity.tickCount;
        }

        /** O {@code render} do {@code ModelTaintacle}: a subida do chão e o tamanho pela altura. */
        @Override
        protected void scale(TaintOddModels.State state, PoseStack pose) {
            float h = state.height;
            float hc = h * 10.0f;
            float rise = state.tickCount < hc ? (hc - state.tickCount) / hc * h : 0.0f;
            // no original isto vem depois do recuo de 1,5 do corpo, que aqui vem depois: desfaz-se e refaz-se em volta
            pose.translate(0.0f, -1.501f + (h == 3.0f ? 0.6f : 1.2f) + rise, 0.0f);
            pose.scale(h / 3.0f, h / 3.0f, h / 3.0f);
            pose.translate(0.0f, 1.501f, 0.0f);
        }

        @Override
        protected RenderType getRenderType(TaintOddModels.State state, boolean visible, boolean translucent, boolean glowing) {
            return RenderTypes.entityTranslucent(SKIN);
        }
    }

    /** Para quem não tem o que desenhar (o enxame, que é só partícula). */
    public static class Nothing extends net.minecraft.client.renderer.entity.EntityRenderer<net.thaumcraft.entity.taint.TaintSwarmEntity, net.minecraft.client.renderer.entity.state.EntityRenderState> {
        public Nothing(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public net.minecraft.client.renderer.entity.state.EntityRenderState createRenderState() {
            return new net.minecraft.client.renderer.entity.state.EntityRenderState();
        }

        @Override
        public void submit(net.minecraft.client.renderer.entity.state.EntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        }
    }

}
