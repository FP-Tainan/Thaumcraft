package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.MindSpiderEntity;

/**
 * A aranha da mente: o {@code RenderMindSpider} da 4.2.3.5 — o modelo da aranha a 30%, com a pele da aranha maculada
 * quase transparente (no máximo um décimo, crescendo nos primeiros dez tiques) e os olhos acesos. A que tem dono só
 * aparece para ele.
 */
public class MindSpiderRenderer extends MobRenderer<MindSpiderEntity, LivingEntityRenderState, SpiderModel> {
    private static final Identifier SKIN = Thaumcraft.id("textures/models/taint_spider.png");
    private static final RenderType EYES = RenderTypes.eyes(Thaumcraft.id("textures/models/taint_spider_eyes.png"));

    public MindSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel(context.bakeLayer(ModelLayers.SPIDER)), 0.0f);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return EYES;
            }
        });
    }

    @Override
    public boolean shouldRender(MindSpiderEntity spider, Frustum frustum, double x, double y, double z) {
        String viewer = spider.getViewer();
        var player = Minecraft.getInstance().player;
        if (!viewer.isEmpty() && (player == null || !viewer.equals(player.getName().getString()))) return false;
        return super.shouldRender(spider, frustum, x, y, z);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return SKIN;
    }

    @Override
    protected RenderType getRenderType(LivingEntityRenderState state, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderTypes.entityTranslucent(SKIN);
    }

    @Override
    protected int getModelTint(LivingEntityRenderState state) {
        return ARGB.colorFromFloat(Math.min(0.1f, state.ageInTicks / 100.0f), 1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void scale(LivingEntityRenderState state, PoseStack pose) {
        pose.scale(0.3f, 0.3f, 0.3f);
    }

    @Override
    protected float getFlipDegrees() {
        return 180.0f;
    }
}
