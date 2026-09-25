package net.thaumcraft.mortuorum.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.mortuorum.IsaacEntity;
import net.thaumcraft.mortuorum.NightCrawlerEntity;
import net.thaumcraft.mortuorum.TeddyEntity;

/**
 * Os desenhistas das criaturas do Ars Mortuorum: o {@code RenderNightCrawler}, o {@code RenderTeddy} e os três
 * {@code RenderIsaac} do Necromancy.
 */
public final class CreatureRenderers {
    public static final ModelLayerLocation NIGHT_CRAWLER = layer("night_crawler");
    public static final ModelLayerLocation TEDDY = layer("teddy");
    public static final ModelLayerLocation ISAAC = layer("isaac");
    public static final ModelLayerLocation ISAAC_SEVERED = layer("isaac_severed");
    public static final ModelLayerLocation ISAAC_HEAD = layer("isaac_head");

    private static final Identifier NIGHT_CRAWLER_SKIN = Thaumcraft.id("textures/entity/nightcrawler.png");
    private static final Identifier TEDDY_SKIN = Thaumcraft.id("textures/entity/teddy.png");
    private static final Identifier ISAAC_SKIN = Thaumcraft.id("textures/entity/isaac.png");
    private static final Identifier ISAAC_BLOOD_SKIN = Thaumcraft.id("textures/entity/isaacblood.png");

    private CreatureRenderers() {
    }

    private static ModelLayerLocation layer(String nome) {
        return new ModelLayerLocation(Thaumcraft.id(nome), "main");
    }

    // ------------------------------------------------------------- os modelos

    /** O {@code ModelNightCrawler}, que o original desenha aumentado e um pouco abaixado. */
    public static class NightCrawlerModel extends EntityModel<LivingEntityRenderState> {
        public NightCrawlerModel(ModelPart root) {
            super(root);
        }
    }

    public static class TeddyModel extends EntityModel<LivingEntityRenderState> {
        public TeddyModel(ModelPart root) {
            super(root);
        }
    }

    /** O {@code ModelIsaacHead}: a cabeça sozinha, no pescoço cortado. */
    public static class IsaacHeadModel extends EntityModel<LivingEntityRenderState> {
        public IsaacHeadModel(ModelPart root) {
            super(root);
        }
    }

    /**
     * O {@code ModelIsaacNormal}: o bípede do jogo com a cabeça grande do original, e o
     * {@code ModelIsaacSevered}, que troca a cabeça por um toco de pescoço.
     */
    public static LayerDefinition isaacLayer(boolean severed) {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        if (severed) {
            mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create().mirror()
                            .texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 2, 1, 2),
                    PartPose.offset(-1.0f, 1.0f, -1.0f));
        } else {
            mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create().mirror()
                            .texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 10, 9, 8),
                    PartPose.offset(-1.0f, 1.0f, 0.0f));
        }
        // o original não desenha o chapéu do bípede
        mesh.getRoot().addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    // ------------------------------------------------------------- os desenhistas

    public static class NightCrawler extends MobRenderer<NightCrawlerEntity, LivingEntityRenderState, NightCrawlerModel> {
        public NightCrawler(EntityRendererProvider.Context context) {
            super(context, new NightCrawlerModel(context.bakeLayer(NIGHT_CRAWLER)), 0.5f);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        /** O {@code glScalef(1.4)} com o {@code glTranslatef(0, -0.4, 0)} que o modelo faz antes de desenhar. */
        @Override
        protected void scale(LivingEntityRenderState state, PoseStack pose) {
            pose.scale(1.4f, 1.4f, 1.4f);
            pose.translate(0.0f, 0.4f, 0.0f);
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return NIGHT_CRAWLER_SKIN;
        }
    }

    public static class Teddy extends MobRenderer<TeddyEntity, LivingEntityRenderState, TeddyModel> {
        public Teddy(EntityRendererProvider.Context context) {
            super(context, new TeddyModel(context.bakeLayer(TEDDY)), 0.3f);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return TEDDY_SKIN;
        }
    }

    /** O Isaac inteiro, o de sangue e o corpo sem cabeça: os três são o bípede do original. */
    public static class Isaac extends HumanoidMobRenderer<IsaacEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
        private final IsaacEntity.Kind kind;

        public Isaac(EntityRendererProvider.Context context, IsaacEntity.Kind kind) {
            super(context, new HumanoidModel<>(context.bakeLayer(
                    kind == IsaacEntity.Kind.BODY ? ISAAC_SEVERED : ISAAC)), 0.5f);
            this.kind = kind;
        }

        @Override
        public HumanoidRenderState createRenderState() {
            return new HumanoidRenderState();
        }

        @Override
        public Identifier getTextureLocation(HumanoidRenderState state) {
            return this.kind == IsaacEntity.Kind.NORMAL ? ISAAC_SKIN : ISAAC_BLOOD_SKIN;
        }
    }

    /** E a cabeça sozinha, que é outro modelo. */
    public static class IsaacHead extends MobRenderer<IsaacEntity, LivingEntityRenderState, IsaacHeadModel> {
        public IsaacHead(EntityRendererProvider.Context context) {
            super(context, new IsaacHeadModel(context.bakeLayer(ISAAC_HEAD)), 0.5f);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return ISAAC_BLOOD_SKIN;
        }
    }

    /** O que o {@code Mth} tem de arredondar para o desenho do ursinho; guardado para não se perder o import. */
    static float wrap(float valor) {
        return Mth.wrapDegrees(valor);
    }
}
