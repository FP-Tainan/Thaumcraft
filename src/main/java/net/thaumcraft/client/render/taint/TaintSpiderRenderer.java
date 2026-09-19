package net.thaumcraft.client.render.taint;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.taint.TaintSpiderEntity;

/**
 * A aranha da mácula: o {@code RenderTaintSpider} da 4.2.3.5 — o modelo da aranha (igual ao de então) a 40% (e um quarto
 * mais alta), com a pele {@code taint_spider.png} e os olhos acesos {@code taint_spider_eyes.png}; morta, tomba de
 * costas.
 */
public class TaintSpiderRenderer extends MobRenderer<TaintSpiderEntity, LivingEntityRenderState, SpiderModel> {
    private static final Identifier SKIN = Thaumcraft.id("textures/models/taint_spider.png");
    private static final RenderType EYES = RenderTypes.eyes(Thaumcraft.id("textures/models/taint_spider_eyes.png"));

    public TaintSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel(context.bakeLayer(ModelLayers.SPIDER)), 0.5f);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return EYES;
            }
        });
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
    protected void scale(LivingEntityRenderState state, PoseStack pose) {
        pose.scale(0.4f, 0.4f * 1.25f, 0.4f);
    }

    @Override
    protected float getFlipDegrees() {
        return 180.0f;
    }

}
