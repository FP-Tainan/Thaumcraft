package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.GolemEntity;
import net.thaumcraft.item.GolemUpgradeItem;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

import java.util.HashMap;
import java.util.Map;

/**
 * O golem desenhado: o {@code RenderGolemBase} da 4.2.3.5. Por cima do corpo (com a pele da matéria): o núcleo preso
 * nas costas e as plaquinhas das melhorias acima dele; os acessórios; as rachaduras de {@code golem_damage.png}, tanto
 * mais fortes quanto mais ferido; e o que ele carrega — a coisa nos braços, o balde de líquido com o líquido dentro,
 * o jarro do alquimista ou a vara de pescar. Ele balança de um lado para o outro enquanto anda, e esverdeia ao se remendar.
 */
public class GolemRenderer extends MobRenderer<GolemEntity, GolemRenderer.State, GolemModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("golem"), "main");
    public static final ModelLayerLocation DAMAGE = new ModelLayerLocation(Thaumcraft.id("golem"), "damage");
    public static final ModelLayerLocation ACCESSORIES = new ModelLayerLocation(Thaumcraft.id("golem"), "accessories");
    private static final Identifier DECORATION = Thaumcraft.id("textures/entity/golem_decoration.png");
    private static final Identifier DAMAGE_TEX = Thaumcraft.id("textures/entity/golem_damage.png");
    private static final Identifier BUCKET = Thaumcraft.id("textures/entity/bucket.png");
    private static final Map<String, Identifier> SKINS = new HashMap<>();

    /** O que o desenho precisa saber do golem neste quadro. */
    public static class State extends LivingEntityRenderState {
        public String material = "wood";
        public int core = -1;
        public float bootup = -1.0f;
        public boolean inactive;
        public int carryLimit = 1;
        public int actionTimer;
        public int leftArm;
        public int rightArm;
        public int healing;
        public boolean carrying;
        public boolean bucket;
        public String decoration = "";
        public boolean advanced;
        public boolean plated;
        public byte[] upgrades = new byte[0];
        public float healthPct = 1.0f;
        public boolean jar;
        public boolean block3d;
        public final ItemStackRenderState coreItem = new ItemStackRenderState();
        public final ItemStackRenderState carried = new ItemStackRenderState();
        public final ItemStackRenderState rod = new ItemStackRenderState();
        public Fluid fluid;
        public int fluidAmount;
        public int fluidLimit = 1000;
    }

    public GolemRenderer(EntityRendererProvider.Context context) {
        super(context, new GolemModel(context.bakeLayer(LAYER)), 0.25f);
        GolemModel damage = new GolemModel(context.bakeLayer(DAMAGE));
        GolemAccessoriesModel accessories = new GolemAccessoriesModel(context.bakeLayer(ACCESSORIES));
        // o passe 0: o núcleo e as melhorias nas costas
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack pose, SubmitNodeCollector collector, int light, State s, float yRot, float xRot) {
                GolemRenderer.submitBack(pose, collector, light, s);
            }
        });
        // o passe 1: os acessórios
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack pose, SubmitNodeCollector collector, int light, State s, float yRot, float xRot) {
                if (s.isInvisible || (s.decoration.isEmpty() && !s.advanced)) return;
                collector.submitModel(accessories, s, pose, RenderTypes.entityTranslucent(DECORATION), light,
                        LivingEntityRenderer.getOverlayCoords(s, 0.0f), -1, null, s.outlineColor, null);
            }
        });
        // o passe 2: as rachaduras, com a transparência do que falta de vida
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack pose, SubmitNodeCollector collector, int light, State s, float yRot, float xRot) {
                if (s.isInvisible || s.healthPct >= 1.0f) return;
                int alpha = (int) (Mth.clamp(1.0f - s.healthPct, 0.0f, 1.0f) * 255.0f);
                collector.submitModel(damage, s, pose, RenderTypes.entityTranslucent(DAMAGE_TEX), light,
                        LivingEntityRenderer.getOverlayCoords(s, 0.0f), alpha << 24 | 0xFFFFFF, null, s.outlineColor, null);
            }
        });
        // e o renderEquippedItems: o que ele leva
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack pose, SubmitNodeCollector collector, int light, State s, float yRot, float xRot) {
                GolemRenderer.this.submitCarried(pose, collector, light, s);
            }
        });
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GolemEntity golem, State s, float partial) {
        super.extractRenderState(golem, s, partial);
        s.material = golem.material();
        s.core = golem.getCore();
        s.bootup = golem.bootup;
        s.inactive = golem.inactive;
        s.carryLimit = golem.getCarryLimit();
        s.actionTimer = golem.getActionTimer();
        s.leftArm = golem.leftArm;
        s.rightArm = golem.rightArm;
        s.healing = golem.healing;
        ItemStack carried = golem.getCarriedForDisplay();
        s.carrying = !carried.isEmpty();
        s.bucket = s.core == 5;
        s.decoration = golem.getGolemDecoration();
        s.advanced = golem.advanced;
        s.plated = s.decoration.contains("P");
        s.upgrades = new byte[golem.upgradeSlots()];
        for (int a = 0; a < s.upgrades.length; a++) s.upgrades[a] = golem.getUpgrade(a);
        s.healthPct = golem.getHealthPercentage();
        s.jar = carried.is(TCBlocks.JAR.asItem());
        this.itemModelResolver.updateForLiving(s.carried, carried, ItemDisplayContext.NONE, golem);
        s.block3d = s.carried.usesBlockLight() && carried.getItem() instanceof net.minecraft.world.item.BlockItem;
        ItemStack core = s.core > -1 ? new ItemStack(TCItems.GOLEM_CORES.get(net.thaumcraft.api.golems.GolemTypes.CORES[s.core])) : ItemStack.EMPTY;
        this.itemModelResolver.updateForLiving(s.coreItem, core, ItemDisplayContext.NONE, golem);
        this.itemModelResolver.updateForLiving(s.rod, s.core == 11 ? new ItemStack(Items.FISHING_ROD) : ItemStack.EMPTY, ItemDisplayContext.NONE, golem);
        s.fluid = golem.displayFluid();
        s.fluidAmount = golem.displayFluidAmount();
        s.fluidLimit = golem.getFluidCarryLimit();
    }

    @Override
    public Identifier getTextureLocation(State s) {
        return SKINS.computeIfAbsent(s.material, name -> Thaumcraft.id("textures/entity/golem_" + name + ".png"));
    }

    /** O verde de quem se remenda: o {@code glColor3f(0.5 + h/10, 0.9 + h/5, 0.5 + h/10)} do original. */
    @Override
    protected int getModelTint(State s) {
        if (s.healing <= 0) return -1;
        float h1 = s.healing / 10.0f, h2 = s.healing / 5.0f;
        int r = (int) (Math.min(1.0f, 0.5f + h1) * 255), g = (int) (Math.min(1.0f, 0.9f + h2) * 255), b = (int) (Math.min(1.0f, 0.5f + h1) * 255);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    /** O {@code renderWithSway}: o balanço de um lado para o outro no passo. */
    @Override
    protected void setupRotations(State s, PoseStack pose, float bodyRot, float scale) {
        super.setupRotations(s, pose, bodyRot, scale);
        if (s.walkAnimationSpeed >= 0.01f) {
            float var5 = 13.0f;
            float var6 = s.walkAnimationPos + 6.0f;
            float var7 = (Math.abs(var6 % var5 - var5 * 0.5f) - var5 * 0.25f) / (var5 * 0.25f);
            pose.mulPose(Axis.ZP.rotationDegrees(6.5f * var7));
        }
    }

    /**
     * O {@code renderItemIn2D} do jogo de então (uma coisa chapada de 0 a 1, com a espessura pedida), feito com o
     * modelo da coisa de hoje: centrado, virado de costas e esticado na espessura.
     */
    static void in2D(PoseStack pose, SubmitNodeCollector collector, ItemStackRenderState item, float thickness, int light) {
        if (item.isEmpty()) return;
        pose.pushPose();
        pose.translate(0.5f, 0.5f, -thickness / 2.0f);
        pose.mulPose(Axis.YP.rotationDegrees(180.0f));
        pose.scale(1.0f, 1.0f, thickness * 16.0f);
        item.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }

    /** O passe 0: o núcleo preso nas costas e as plaquinhas das melhorias (a vazia também aparece). */
    private static void submitBack(PoseStack pose, SubmitNodeCollector collector, int light, State s) {
        if (s.isInvisible) return;
        if (s.core > -1) {
            pose.pushPose();
            pose.mulPose(Axis.XP.rotationDegrees(180.0f));
            pose.translate(0.0875f, -0.96f, 0.15f + (s.plated ? 0.03f : 0.0f));
            pose.scale(0.175f, 0.175f, 0.175f);
            pose.mulPose(Axis.YP.rotationDegrees(180.0f));
            in2D(pose, collector, s.coreItem, 0.2f, light);
            pose.popPose();
        }
        int upgrades = s.upgrades.length;
        float shift = 0.08f;
        for (int a = 0; a < upgrades; a++) {
            pose.pushPose();
            pose.mulPose(Axis.XP.rotationDegrees(180.0f));
            pose.translate(-0.05f - shift * (upgrades - 1) / 2.0f + shift * a, -1.106f, 0.099f);
            pose.scale(0.1f, 0.1f, 0.1f);
            byte u = s.upgrades[a];
            Identifier tex = Thaumcraft.id("textures/item/golem_upgrade_" + (u < 0 ? "empty" : GolemUpgradeItem.NAMES[u]) + ".png");
            collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(tex), (m, c) -> {
                quad(m, c, 0, 0, 1.0f, 1.0f, light);
                quad(m, c, 1, 0, 0.0f, 1.0f, light);
                quad(m, c, 1, 1, 0.0f, 0.0f, light);
                quad(m, c, 0, 1, 1.0f, 0.0f, light);
            });
            pose.popPose();
        }
    }

    private static void quad(PoseStack.Pose m, VertexConsumer c, float x, float y, float u, float v, int light) {
        c.addVertex(m, x, y, 0.0f).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m, 0.0f, 0.0f, 1.0f);
    }

    /** O {@code renderCarriedItems}. */
    private void submitCarried(PoseStack pose, SubmitNodeCollector collector, int light, State s) {
        if (s.isInvisible) return;
        if (s.core == 11) {
            // a vara de pescar presa ao braço direito
            this.model.setupAnim(s);
            pose.pushPose();
            pose.mulPose(Axis.XP.rotationDegrees(5.0f + 90.0f * this.model.rightArm().xRot / (float) Math.PI));
            pose.translate(-0.26875f, 1.6f, -0.53f);
            pose.mulPose(Axis.YN.rotationDegrees(90.0f));
            pose.mulPose(Axis.ZN.rotationDegrees(30.0f));
            pose.scale(0.66f, -0.66f, 0.66f);
            in2D(pose, collector, s.rod, 0.0625f, light);
            pose.popPose();
        }
        if (s.carrying && s.deathTime == 0 && s.core != 5) {
            pose.pushPose();
            pose.scale(0.4f, 0.4f, 0.4f);
            if (!s.block3d || s.jar) {
                if (s.jar) {
                    pose.translate(0.0f, 2.5f, -1.0f);
                    if (s.core == 6) {
                        float sc = 0.5f + Math.min(64, s.carryLimit) / 128.0f;
                        pose.scale(sc, sc, sc);
                    }
                    pose.translate(-0.5f, 0.0f, -0.8f);
                    pose.mulPose(Axis.XP.rotationDegrees(180.0f));
                    pose.mulPose(Axis.YP.rotationDegrees(180.0f));
                    pose.mulPose(Axis.ZN.rotationDegrees(335.0f));
                    pose.mulPose(Axis.YN.rotationDegrees(50.0f));
                    // o jarro do original é desenhado de 0 a 1, como um bloco
                    pose.translate(0.5f, 0.5f, 0.5f);
                    s.carried.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
                } else {
                    pose.translate(-0.5f, 2.5f, -1.25f);
                    pose.mulPose(Axis.XP.rotationDegrees(180.0f));
                    pose.mulPose(Axis.YP.rotationDegrees(180.0f));
                    pose.mulPose(Axis.ZN.rotationDegrees(335.0f));
                    pose.mulPose(Axis.YN.rotationDegrees(50.0f));
                    in2D(pose, collector, s.carried, 0.0625f, light);
                }
            } else {
                pose.translate(0.0f, 2.5f, -1.25f);
                pose.mulPose(Axis.XP.rotationDegrees(180.0f));
                s.carried.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
            }
            pose.popPose();
        } else if (s.core == 5) {
            pose.pushPose();
            pose.scale(0.4f, 0.4f, 0.4f);
            pose.translate(0.0f, 3.0f, -1.1f);
            pose.mulPose(Axis.XP.rotationDegrees(90.0f));
            float[] bucket = ObjModel.part("bucket", "Bucket");
            collector.submitCustomGeometry(pose, RenderTypes.entityCutout(BUCKET),
                    (m, c) -> ObjMesh.draw(bucket, m, c, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
            if (s.fluid != null) {
                float max = Math.max(s.fluidAmount, s.fluidLimit);
                float fill = s.fluidAmount / max;
                pose.translate(0.0f, 0.0f, 0.2f + 0.8f * fill);
                pose.scale(0.8f, 0.8f, 0.8f);
                TextureAtlasSprite sprite = FluidSprites.still(s.fluid);
                int tint = FluidSprites.tint(s.fluid);
                int lum = s.fluid.defaultFluidState().createLegacyBlock().getLightEmission();
                int b = Math.max(light, 15728640 | lum << 4);
                if (sprite != null) {
                    collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (m, c) -> {
                        c.addVertex(m, -0.5f, 0.5f, 0.0f).setColor(tint).setUv(sprite.getU0(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(b).setNormal(m, 0, 0, 1);
                        c.addVertex(m, 0.5f, 0.5f, 0.0f).setColor(tint).setUv(sprite.getU1(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(b).setNormal(m, 0, 0, 1);
                        c.addVertex(m, 0.5f, -0.5f, 0.0f).setColor(tint).setUv(sprite.getU1(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(b).setNormal(m, 0, 0, 1);
                        c.addVertex(m, -0.5f, -0.5f, 0.0f).setColor(tint).setUv(sprite.getU0(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(b).setNormal(m, 0, 0, 1);
                    });
                }
            }
            pose.popPose();
        }
    }
}
