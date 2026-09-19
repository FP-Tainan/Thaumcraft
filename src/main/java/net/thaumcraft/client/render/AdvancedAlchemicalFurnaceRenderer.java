package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.AdvancedAlchemicalFurnaceBlockEntity;

/**
 * A fornalha alquímica avançada: o {@code TileAlchemyFurnaceAdvancedRenderer} da 4.2.3.5. O {@code adv_alch_furnace.obj}
 * inteiro sai do bloco do meio: a base (acesa com calor acima de 100) e os quatro tanques (acesos com essência). Com
 * essência, a gosma de fluxo na boca de cima e nas janelas dos tanques, na altura do quanto está cheia; com calor, o
 * fogo nas quatro grelhas inclinadas, subindo com o calor.
 */
public class AdvancedAlchemicalFurnaceRenderer implements BlockEntityRenderer<AdvancedAlchemicalFurnaceBlockEntity, AdvancedAlchemicalFurnaceRenderer.State> {
    private static final Identifier BASE = Thaumcraft.id("textures/models/alch_furnace.png");
    private static final Identifier BASE_ON = Thaumcraft.id("textures/models/alch_furnace_on.png");
    private static final Identifier TANK = Thaumcraft.id("textures/models/alch_furnace_tank.png");
    private static final Identifier TANK_ON = Thaumcraft.id("textures/models/alch_furnace_tank_on.png");

    public static class State extends BlockEntityRenderState {
        int heat;
        int vis;
        int maxVis;
        int maxPower;
    }

    public AdvancedAlchemicalFurnaceRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AdvancedAlchemicalFurnaceBlockEntity tile, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(tile, state, crumbling);
        state.heat = tile.heat;
        state.vis = tile.vis;
        state.maxVis = tile.maxVis;
        state.maxPower = tile.maxPower;
    }

    /** O modelo passa do bloco do meio (3 × 3 × 2): desenha mesmo com o meio fora da vista. */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    private static TextureAtlasSprite sprite(Identifier id) {
        return Minecraft.getInstance().getAtlasManager().get(new SpriteId(QuadAtlas.BLOCK.getTextureLocation(), id));
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        pose.pushPose();
        pose.translate(0.5f, 0.0f, 0.5f);
        pose.mulPose(Axis.XN.rotationDegrees(90.0f));
        float[] base = ObjModel.part("adv_alch_furnace", "Base");
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(state.heat <= 100 ? BASE : BASE_ON),
                (m, v) -> ObjMesh.draw(base, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        float[] tank = ObjModel.part("adv_alch_furnace", "Tank");
        Identifier tankTexture = state.vis <= 0 ? TANK : TANK_ON;
        for (int a = 0; a < 4; a++) {
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(90 * a));
            collector.submitCustomGeometry(pose, RenderTypes.entityCutout(tankTexture),
                    (m, v) -> ObjMesh.draw(tank, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
            pose.popPose();
        }
        TextureAtlasSprite goo = sprite(Thaumcraft.id("block/flux_goo"));
        TextureAtlasSprite metal = sprite(Thaumcraft.id("block/metalbase"));
        if (state.vis > 0) {
            pose.pushPose();
            pose.translate(0.5f, -0.5f, 1.1f);
            pose.mulPose(Axis.YP.rotationDegrees(180.0f));
            quad(pose, collector, goo, 190, 0.0f);
            pose.popPose();
            float f = 1.0f - (float) state.vis / state.maxVis;
            for (int a = 0; a < 4; a++) {
                pose.pushPose();
                pose.pushPose();
                pose.mulPose(Axis.ZP.rotationDegrees(90 * a));
                pose.mulPose(Axis.XN.rotationDegrees(90.0f));
                pose.translate(0.85f, -1.8f, -1.4f);
                pose.scale(0.3f, 0.6f, 1.0f);
                quad(pose, collector, metal, 150, 0.0f);
                pose.translate(0.0f, 0.0f, -0.01f);
                quad(pose, collector, goo, 190, f);
                pose.popPose();
                pose.pushPose();
                pose.mulPose(Axis.ZN.rotationDegrees(90 * a));
                pose.mulPose(Axis.XP.rotationDegrees(90.0f));
                pose.translate(1.15f, 1.8f, -1.4f);
                pose.scale(-0.3f, -0.6f, -1.0f);
                quad(pose, collector, metal, 150, 0.0f);
                pose.translate(0.0f, 0.0f, 0.01f);
                quad(pose, collector, goo, 190, f);
                pose.popPose();
                pose.popPose();
            }
        }
        if (state.heat > 100) {
            TextureAtlasSprite fire = sprite(Identifier.withDefaultNamespace("block/fire_0"));
            pose.pushPose();
            pose.translate(0.0f, 0.0f, 1.0f);
            for (int a = 0; a < 4; a++) {
                pose.pushPose();
                pose.mulPose(Axis.ZP.rotationDegrees(90 * a));
                pose.mulPose(Axis.XP.rotationDegrees(135.0f));
                pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
                pose.translate(-0.5f, 0.0f, -1.0f);
                quad(pose, collector, fire, 220, 1.0f - Math.min(1.0f, (float) state.heat / state.maxPower));
                pose.translate(0.0f, 0.0f, 0.05f);
                quad(pose, collector, metal, 150, 0.0f);
                pose.popPose();
            }
            pose.popPose();
        }
        pose.popPose();
    }

    /**
     * O {@code renderQuadCenteredFromIcon}: um quadrado de 0 a 1 em x e de {@code width} a 1 em y, com a textura
     * inteira do ícone (esticada, não cortada), no brilho dado. Uma face só: este tipo de desenho já não descarta o verso.
     */
    static void quad(PoseStack pose, SubmitNodeCollector collector, TextureAtlasSprite icon, int brightness, float width) {
        float u0 = icon.getU0(), u1 = icon.getU1(), v0 = icon.getV0(), v1 = icon.getV1();
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(QuadAtlas.BLOCK.getTextureLocation()), (m, c) -> {
            vertex(m, c, 0.0f, 1.0f, u1, v1, brightness);
            vertex(m, c, 1.0f, 1.0f, u0, v1, brightness);
            vertex(m, c, 1.0f, width, u0, v0, brightness);
            vertex(m, c, 0.0f, width, u1, v0, brightness);
        });
    }

    private static void vertex(PoseStack.Pose m, VertexConsumer c, float x, float y, float u, float v, int light) {
        c.addVertex(m, x, y, 0.0f).setColor(0xFFFFFFFF).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m, 0.0f, 0.0f, 1.0f);
    }
}
