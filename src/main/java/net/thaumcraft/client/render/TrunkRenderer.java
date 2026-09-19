package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.TravelingTrunkEntity;
import net.thaumcraft.item.GolemUpgradeItem;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O baú itinerante desenhado: o {@code RenderTravelingTrunk} com o {@code ModelTrunk} da 4.2.3.5 — o baú do jogo de
 * então (tampa, fechadura e caixa, na folha {@code trunk.png}, ou {@code trunkangry.png} quando zangado), um nada mais
 * baixo e estreito (maior com a terra), a tampa batendo nos pulos e aberta com a tela, e a plaquinha da melhoria na
 * fechadura. O item é o mesmo baú, fechado.
 */
public class TrunkRenderer extends MobRenderer<TravelingTrunkEntity, TrunkRenderer.State, TrunkRenderer.Model> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("traveling_trunk"), "main");
    private static final Identifier TEXTURE = Thaumcraft.id("textures/entity/trunk.png");
    private static final Identifier ANGRY = Thaumcraft.id("textures/entity/trunkangry.png");

    public static class State extends LivingEntityRenderState {
        public float lidrot;
        public int upgrade = -1;
        public boolean angry;
    }

    /** O {@code ModelTrunk}: tampa, fechadura e caixa. */
    public static class Model extends EntityModel<State> {
        final ModelPart lid;
        final ModelPart knob;

        public Model(ModelPart root) {
            super(root);
            this.lid = root.getChild("lid");
            this.knob = root.getChild("knob");
        }

        public static LayerDefinition createLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, -5.0f, -14.0f, 14, 5, 14),
                    PartPose.offset(1.0f, 7.0f, 15.0f));
            root.addOrReplaceChild("knob", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -2.0f, -15.0f, 2, 4, 1),
                    PartPose.offset(8.0f, 7.0f, 15.0f));
            root.addOrReplaceChild("below", CubeListBuilder.create().texOffs(0, 19).addBox(0.0f, 0.0f, 0.0f, 14, 10, 14),
                    PartPose.offset(1.0f, 6.0f, 1.0f));
            return LayerDefinition.create(mesh, 64, 64);
        }

        /** A tampa: {@code 1 − (1 − lidrot)³}, em quartos de volta. */
        static float lidAngle(float lidrot) {
            float f1 = 1.0f - lidrot;
            f1 = 1.0f - f1 * f1 * f1;
            return -(f1 * 3.141593f / 2.0f);
        }

        @Override
        public void setupAnim(State s) {
            super.setupAnim(s);
            this.lid.xRot = lidAngle(s.lidrot);
            this.knob.xRot = this.lid.xRot;
        }
    }

    public TrunkRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(LAYER)), 0.5f);
        // a plaquinha da melhoria, presa na fechadura e girando com a tampa
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack pose, SubmitNodeCollector collector, int light, State s, float yRot, float xRot) {
                if (s.isInvisible) return;
                pose.pushPose();
                pose.translate(8.0f / 16.0f, 7.0f / 16.0f, 15.0f / 16.0f);
                pose.mulPose(Axis.XP.rotation(Model.lidAngle(s.lidrot)));
                pose.translate(-0.075f, -0.115f, -0.94301f);
                pose.scale(0.15f, 0.15f, 0.15f);
                Identifier tex = Thaumcraft.id("textures/item/golem_upgrade_" + (s.upgrade < 0 ? "empty" : GolemUpgradeItem.NAMES[s.upgrade]) + ".png");
                collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(tex), (m, c) -> {
                    c.addVertex(m, 0, 0, 0).setColor(-1).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m, 0, 0, 1);
                    c.addVertex(m, 1, 0, 0).setColor(-1).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m, 0, 0, 1);
                    c.addVertex(m, 1, 1, 0).setColor(-1).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m, 0, 0, 1);
                    c.addVertex(m, 0, 1, 0).setColor(-1).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m, 0, 0, 1);
                });
                pose.popPose();
            }
        });
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TravelingTrunkEntity trunk, State s, float partial) {
        super.extractRenderState(trunk, s, partial);
        s.lidrot = trunk.lidrot;
        s.upgrade = trunk.getUpgrade();
        s.angry = trunk.getAnger() > 0;
    }

    @Override
    public Identifier getTextureLocation(State s) {
        return s.angry ? ANGRY : TEXTURE;
    }

    /**
     * O {@code adjustTrunk}: o esmagamento do pulo nunca muda do lado de quem vê (o original só o calcula no servidor),
     * então o baú fica a 0,952 × 0,933 × 0,952 (a terra o aumenta) e desce meio bloco para o canto do modelo.
     */
    @Override
    protected void scale(State s, PoseStack pose) {
        float f2 = 1.0f / 1.4f;
        float f3 = s.upgrade == 1 ? 2.0f / 1.33f : 2.0f / 1.5f;
        pose.scale(f2 * f3, 0.5f / f2 * f3, f2 * f3);
        pose.translate(-0.5f, 0.5f, -0.5f);
    }

    /** O item do baú: o {@code ItemTrunkSpawnerRenderer}, o mesmo baú fechado. */
    public static class Item implements SpecialModelRenderer<net.minecraft.util.Unit> {
        private final Model model;

        Item(Model model) {
            this.model = model;
        }

        @Override
        public void submit(@Nullable net.minecraft.util.Unit ignored, PoseStack pose, SubmitNodeCollector collector,
                           int light, int overlay, boolean foil, int tint) {
            pose.pushPose();
            pose.translate(0.5f, 0.5f, 0.5f);
            pose.scale(1.0f, -1.0f, -1.0f);
            pose.translate(-0.5f, -0.5f, -0.5f);
            State s = new State();
            collector.submitModel(this.model, s, pose, RenderTypes.entityCutout(TEXTURE), light, overlay, -1, null, 0, null);
            pose.popPose();
        }

        @Override
        public void getExtents(Consumer<Vector3fc> extents) {
            extents.accept(new Vector3f(0.0f, 0.0f, 0.0f));
            extents.accept(new Vector3f(1.0f, 1.0f, 1.0f));
        }

        @Override
        public net.minecraft.util.Unit extractArgument(ItemStack stack) {
            return net.minecraft.util.Unit.INSTANCE;
        }
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<net.minecraft.util.Unit> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.point(new Unbaked()));

        @Override
        public SpecialModelRenderer<net.minecraft.util.Unit> bake(SpecialModelRenderer.BakingContext context) {
            return new Item(new Model(context.entityModelSet().bakeLayer(LAYER)));
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
