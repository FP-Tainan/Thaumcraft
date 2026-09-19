package net.thaumcraft.client.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.item.Architect;
import net.thaumcraft.item.WandItem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A prévia do arquiteto: o {@code handleArchitectOverlay} do {@code REHWandHandler} da 4.2.3.5. Com a varinha de foco
 * com arquiteto na mão, cada bloco da área que o foco vai atingir ganha a casca do vidro protegido (ligada entre os
 * blocos, somando luz, azulada e piscando, vista através de tudo) e, no bloco da mira, as setas das dimensões que a
 * tecla G muda. O contorno comum do bloco some.
 */
public final class ArchitectOverlay {
    private static final Identifier ARROWS = Thaumcraft.id("textures/misc/architect_arrows.png");

    private static RenderPipeline pipeline(String name, boolean add) {
        var builder = RenderPipeline.builder()
                .withLocation(Thaumcraft.id("pipeline/" + name))
                .withBindGroupLayout(BindGroupLayouts.GLOBALS)
                .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                .withVertexShader(Thaumcraft.id("core/architect"))
                .withFragmentShader(Thaumcraft.id("core/architect"))
                .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withCull(false)
                .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false));
        if (add) builder = builder.withShaderDefine("ADD");
        return RenderPipelines.register(builder.build());
    }

    private static final RenderPipeline FACES = pipeline("architect_faces", true);
    private static final RenderPipeline AXES = pipeline("architect_axes", false);
    private static final RenderType FACES_TYPE = RenderType.create("thaumcraft_architect_faces", RenderSetup.builder(FACES)
            .withTexture("Sampler0", net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas.BLOCK.getTextureLocation())
            .sortOnUpload()
            .createRenderSetup());
    private static final RenderType AXES_TYPE = RenderType.create("thaumcraft_architect_axes", RenderSetup.builder(AXES)
            .withTexture("Sampler0", ARROWS)
            .sortOnUpload()
            .createRenderSetup());

    private static int lastHash;
    private static List<BlockPos> blocks = List.of();
    private static Set<BlockPos> set = Set.of();
    private static boolean drawing;

    private ArchitectOverlay() {
    }

    public static void init() {
        LevelRenderEvents.COLLECT_SUBMITS.register(ArchitectOverlay::draw);
        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, outline) -> !drawing);
    }

    private static void draw(LevelRenderContext context) {
        drawing = false;
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) return;
        ItemStack wand = player.getMainHandItem();
        if (!(wand.getItem() instanceof WandItem) || !Architect.active(wand)) return;
        HitResult hit = minecraft.hitResult;
        if (!(hit instanceof BlockHitResult target) || hit.getType() != HitResult.Type.BLOCK) return;
        BlockPos pos = target.getBlockPos();
        int side = target.getDirection().get3DDataValue();
        int ticks = player.tickCount;
        int hash = (pos.getX() + "" + pos.getY() + "" + pos.getZ() + "" + side + "" + ticks / 5).hashCode();
        if (hash != lastHash) {
            lastHash = hash;
            List<BlockPos> found = Architect.blocks(wand, minecraft.level, pos, side, player);
            blocks = found == null ? List.of() : found;
            set = new HashSet<>(blocks);
        }
        if (blocks.isEmpty()) return;
        drawing = true;
        float partial = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 camera = context.levelState().cameraRenderState.pos;
        PoseStack pose = context.poseStack();
        SubmitNodeCollector collector = context.submitNodeCollector();
        axes(pose, collector, camera, pos, player, Architect.showAxis(wand, side, Architect.Axis.X),
                Architect.showAxis(wand, side, Architect.Axis.Y), Architect.showAxis(wand, side, Architect.Axis.Z));
        TextureAtlasSprite[] sprites = sprites(minecraft);
        for (BlockPos c : blocks) block(pose, collector, camera, c, ticks, sprites);
    }

    private static TextureAtlasSprite[] sprites(Minecraft minecraft) {
        TextureAtlasSprite[] made = new TextureAtlasSprite[47];
        var atlas = minecraft.getAtlasManager();
        for (int i = 0; i < 47; i++) {
            made[i] = atlas.get(new SpriteId(net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas.BLOCK.getTextureLocation(),
                    Thaumcraft.id("block/warded_glass_" + (i + 1))));
        }
        return made;
    }

    /** O {@code shouldSideBeRendered}: a face só aparece se o vizinho daquele lado não é da área. */
    private static boolean open(BlockPos c, Direction face) {
        return !set.contains(c.relative(face));
    }

    private static TextureAtlasSprite icon(TextureAtlasSprite[] sprites, BlockPos c, int side) {
        return sprites[WardedGlassModel.index((x, y, z) -> set.contains(new BlockPos(x, y, z)), c, side)];
    }

    /** O {@code drawOverlayBlock}: a casca de 1,002 de lado em volta do bloco. */
    private static void block(PoseStack pose, SubmitNodeCollector collector, Vec3 camera, BlockPos c, int ticks, TextureAtlasSprite[] sprites) {
        float r = Mth.sin(ticks / 2.0f + c.getX()) * 0.2f + 0.3f;
        float g = Mth.sin(ticks / 3.0f + c.getY()) * 0.2f + 0.3f;
        float b = Mth.sin(ticks / 4.0f + c.getZ()) * 0.2f + 0.8f;
        int colour = 0x33000000 | (int) (r * 255) << 16 | (int) (g * 255) << 8 | (int) (b * 255);
        float lo = -0.001f, hi = 1.001f;
        pose.pushPose();
        pose.translate(c.getX() - camera.x, c.getY() - camera.y, c.getZ() - camera.z);
        collector.submitCustomGeometry(pose, FACES_TYPE, (m, v) -> {
            if (open(c, Direction.DOWN)) {
                TextureAtlasSprite s = icon(sprites, c, 0);
                quad(m, v, colour, s, lo, lo, hi, 0, 1, lo, lo, lo, 0, 0, hi, lo, lo, 1, 0, hi, lo, hi, 1, 1);
            }
            if (open(c, Direction.UP)) {
                TextureAtlasSprite s = icon(sprites, c, 1);
                quad(m, v, colour, s, hi, hi, hi, 1, 1, hi, hi, lo, 1, 0, lo, hi, lo, 0, 0, lo, hi, hi, 0, 1);
            }
            if (open(c, Direction.NORTH)) {
                TextureAtlasSprite s = icon(sprites, c, 2);
                quad(m, v, colour, s, lo, hi, lo, 1, 0, hi, hi, lo, 0, 0, hi, lo, lo, 0, 1, lo, lo, lo, 1, 1);
            }
            if (open(c, Direction.SOUTH)) {
                TextureAtlasSprite s = icon(sprites, c, 3);
                quad(m, v, colour, s, lo, hi, hi, 0, 0, lo, lo, hi, 0, 1, hi, lo, hi, 1, 1, hi, hi, hi, 1, 0);
            }
            if (open(c, Direction.WEST)) {
                TextureAtlasSprite s = icon(sprites, c, 4);
                quad(m, v, colour, s, lo, hi, hi, 1, 0, lo, hi, lo, 0, 0, lo, lo, lo, 0, 1, lo, lo, hi, 1, 1);
            }
            if (open(c, Direction.EAST)) {
                TextureAtlasSprite s = icon(sprites, c, 5);
                quad(m, v, colour, s, hi, lo, hi, 0, 1, hi, lo, lo, 1, 1, hi, hi, lo, 1, 0, hi, hi, hi, 0, 0);
            }
        });
        pose.popPose();
    }

    /** Quatro cantos: x, y, z e o u, v de 0 a 1 dentro do quadro do atlas. */
    private static void quad(PoseStack.Pose m, VertexConsumer v, int colour, TextureAtlasSprite s, float... k) {
        for (int i = 0; i < 4; i++) {
            int o = i * 5;
            v.addVertex(m, k[o], k[o + 1], k[o + 2]).setUv(s.getU(k[o + 3]), s.getV(k[o + 4])).setColor(colour);
        }
    }

    /** O {@code drawArchitectAxis}: as setas no bloco da mira, uma cruz de dois quadrados por eixo. */
    private static void axes(PoseStack pose, SubmitNodeCollector collector, Vec3 camera, BlockPos c, Player player,
                             boolean dx, boolean dy, boolean dz) {
        if (!dx && !dy && !dz) return;
        float t = player.tickCount;
        float r = Mth.sin(t / 4.0f + c.getX()) * 0.2f + 0.3f;
        float g = Mth.sin(t / 3.0f + c.getY()) * 0.2f + 0.3f;
        float b = Mth.sin(t / 2.0f + c.getZ()) * 0.2f + 0.8f;
        int colour = 0xFF000000 | (int) (r * 255) << 16 | (int) (g * 255) << 8 | (int) (b * 255);
        pose.pushPose();
        pose.translate(c.getX() + 0.5 - camera.x, c.getY() + 0.5 - camera.y, c.getZ() + 0.5 - camera.z);
        pose.mulPose(Axis.XP.rotationDegrees(90.0f));
        if (dx) {
            pose.pushPose();
            arrow(pose, collector, colour);
            pose.mulPose(Axis.XP.rotationDegrees(90.0f));
            arrow(pose, collector, colour);
            pose.popPose();
        }
        if (dz) {
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
            arrow(pose, collector, colour);
            pose.mulPose(Axis.XP.rotationDegrees(90.0f));
            arrow(pose, collector, colour);
            pose.popPose();
        }
        if (dy) {
            pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            arrow(pose, collector, colour);
            pose.mulPose(Axis.XP.rotationDegrees(90.0f));
            arrow(pose, collector, colour);
        }
        pose.popPose();
    }

    /** O {@code renderQuadCenteredFromTexture}: um quadrado de lado um, centrado, com a textura inteira. */
    private static void arrow(PoseStack pose, SubmitNodeCollector collector, int colour) {
        collector.submitCustomGeometry(pose, AXES_TYPE, (m, v) -> {
            v.addVertex(m, -0.5f, 0.5f, 0.0f).setUv(0.0f, 1.0f).setColor(colour);
            v.addVertex(m, 0.5f, 0.5f, 0.0f).setUv(1.0f, 1.0f).setColor(colour);
            v.addVertex(m, 0.5f, -0.5f, 0.0f).setUv(1.0f, 0.0f).setColor(colour);
            v.addVertex(m, -0.5f, -0.5f, 0.0f).setUv(0.0f, 0.0f).setColor(colour);
        });
    }
}
