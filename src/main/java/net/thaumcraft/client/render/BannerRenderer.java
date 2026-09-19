package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.entity.BannerBlockEntity;
import net.thaumcraft.registry.TCComponents;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O {@code TileBannerRenderer} com o {@code ModelBanner} da 4.2.3.5: o mastro (fora da parede), a travessa, as duas
 * presilhas na cor da lã e o pano balançando devagar ao vento, com o aspecto pintado no meio. O dos cultistas usa a
 * própria textura. O item é o mesmo estandarte (o {@code ItemBannerRenderer}).
 */
public class BannerRenderer implements BlockEntityRenderer<BannerBlockEntity, BannerRenderer.State> {
    private static final Identifier CULTIST = Thaumcraft.id("textures/models/banner_cultist.png");
    private static final Identifier BLANK = Thaumcraft.id("textures/models/banner_blank.png");
    /** O {@code Utils.colors}: as dezesseis cores de lã do original. */
    public static final int[] COLORS = {15790320, 15435844, 12801229, 6719955, 14602026, 4312372, 14188952, 4408131, 10526880, 2651799,
            8073150, 2437522, 5320730, 3887386, 11743532, 1973019};

    public static class State extends BlockEntityRenderState {
        int facing;
        int color = -1;
        @Nullable
        Aspect aspect;
        boolean wall;
        float wave;
        boolean inWorld = true;
    }

    /** As peças do {@code ModelBanner} (folha de 128 por 64). */
    static final class Parts {
        final ModelPart pole, beam, b1, b2, banner;

        Parts() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("b1", CubeListBuilder.create().texOffs(0, 29).mirror().addBox(-5.0f, -7.5f, -1.5f, 2, 3, 3), PartPose.ZERO);
            root.addOrReplaceChild("b2", CubeListBuilder.create().texOffs(0, 29).mirror().addBox(3.0f, -7.5f, -1.5f, 2, 3, 3), PartPose.ZERO);
            root.addOrReplaceChild("beam", CubeListBuilder.create().texOffs(30, 0).mirror().addBox(-7.0f, -7.0f, -1.0f, 14, 2, 2), PartPose.ZERO);
            root.addOrReplaceChild("banner", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-7.0f, 0.0f, -0.5f, 14, 28, 1),
                    PartPose.offset(0.0f, -5.0f, 0.0f));
            root.addOrReplaceChild("pole", CubeListBuilder.create().texOffs(62, 0).mirror().addBox(0.0f, 0.0f, -1.0f, 2, 31, 2),
                    PartPose.offset(-1.0f, -7.0f, -2.0f));
            ModelPart baked = LayerDefinition.create(mesh, 128, 64).bakeRoot();
            this.pole = baked.getChild("pole");
            this.beam = baked.getChild("beam");
            this.b1 = baked.getChild("b1");
            this.b2 = baked.getChild("b2");
            this.banner = baked.getChild("banner");
        }
    }

    private final Parts parts = new Parts();

    public BannerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BannerBlockEntity banner, State state, float partial, Vec3 camera,
                                   @Nullable ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(banner, state, partial, camera, crumbling);
        state.facing = banner.getFacing();
        state.color = banner.getColor();
        state.aspect = banner.getAspect();
        state.wall = banner.getWall();
        long k = banner.getLevel() == null ? 0L : banner.getLevel().getGameTime();
        var p = banner.getBlockPos();
        state.wave = p.getX() * 7 + p.getY() * 9 + p.getZ() * 13 + (float) k + partial;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.5f, 1.5f, 0.5f);
        draw(this.parts, state, pose, collector, state.lightCoords);
        pose.popPose();
    }

    /** O {@code renderTileEntityAt}, a partir do meio do bloco, um e meio acima do chão. */
    static void draw(Parts parts, State state, PoseStack pose, SubmitNodeCollector collector, int light) {
        pose.mulPose(Axis.XP.rotationDegrees(180.0f));
        if (state.inWorld) {
            pose.mulPose(Axis.YP.rotationDegrees(180.0f));
            pose.mulPose(Axis.YP.rotationDegrees(state.facing * 360 / 16.0f));
        }
        var type = RenderTypes.entityCutoutCull(state.aspect == null && state.color == -1 ? CULTIST : BLANK);
        if (!state.wall) {
            collector.submitModelPart(parts.pole, pose, type, light, OverlayTexture.NO_OVERLAY, null, -1, null);
        } else {
            pose.translate(0.0, 0.0, -0.4125);
        }
        collector.submitModelPart(parts.beam, pose, type, light, OverlayTexture.NO_OVERLAY, null, -1, null);
        int tint = state.color >= 0 ? 0xFF000000 | COLORS[state.color] : -1;
        collector.submitModelPart(parts.b1, pose, type, light, OverlayTexture.NO_OVERLAY, null, tint, null);
        collector.submitModelPart(parts.b2, pose, type, light, OverlayTexture.NO_OVERLAY, null, tint, null);
        float rx = (0.005f + 0.005f * Mth.cos(state.wave * (float) Math.PI * 0.02f)) * (float) Math.PI;
        parts.banner.xRot = rx;
        collector.submitModelPart(parts.banner, pose, type, light, OverlayTexture.NO_OVERLAY, null, tint, null);
        if (state.aspect != null) {
            // o aspecto, pintado no pano e acompanhando o balanço dele (o drawTag, a 0,75 de opacidade)
            pose.pushPose();
            pose.translate(0.0f, 0.0f, 0.05001f);
            pose.scale(0.0375f, 0.0375f, 0.0375f);
            pose.mulPose(Axis.YP.rotationDegrees(180.0f));
            pose.mulPose(Axis.XP.rotationDegrees(-rx * (180.0f / (float) Math.PI) * 2.0f));
            int colour = 0xBF000000 | state.aspect.color() & 0xFFFFFF;
            collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(state.aspect.image()), (m, c) -> {
                c.addVertex(m, -8.0f, 20.0f, 0.0f).setColor(colour).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m, 0, 0, 1);
                c.addVertex(m, 8.0f, 20.0f, 0.0f).setColor(colour).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m, 0, 0, 1);
                c.addVertex(m, 8.0f, 4.0f, 0.0f).setColor(colour).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m, 0, 0, 1);
                c.addVertex(m, -8.0f, 4.0f, 0.0f).setColor(colour).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m, 0, 0, 1);
            });
            pose.popPose();
        }
    }

    /** O {@code ItemBannerRenderer}: o mesmo estandarte, de pé, parado. */
    public static class Item implements SpecialModelRenderer<State> {
        private final Parts parts = new Parts();

        @Override
        public void submit(@Nullable State state, PoseStack pose, SubmitNodeCollector collector, int light, int overlay, boolean foil, int tint) {
            if (state == null) return;
            pose.pushPose();
            pose.translate(0.5f, 1.0f, 0.5f);
            draw(this.parts, state, pose, collector, light);
            pose.popPose();
        }

        @Override
        public void getExtents(Consumer<Vector3fc> extents) {
            extents.accept(new Vector3f(0.0f, -0.5f, 0.0f));
            extents.accept(new Vector3f(1.0f, 1.5f, 1.0f));
        }

        @Override
        public State extractArgument(ItemStack stack) {
            State state = new State();
            state.inWorld = false;
            Integer color = stack.get(TCComponents.BANNER_COLOR);
            state.color = color == null ? -1 : color;
            String as = stack.get(TCComponents.BANNER_ASPECT);
            state.aspect = as == null || as.isEmpty() ? null : Aspect.of(as);
            return state;
        }
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<State> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.point(new Unbaked()));

        @Override
        public SpecialModelRenderer<State> bake(SpecialModelRenderer.BakingContext context) {
            return new Item();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
