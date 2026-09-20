package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.ResearchTableBlock;
import net.thaumcraft.block.entity.ResearchTableBlockEntity;
import net.thaumcraft.item.ScribingToolsItem;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.research.ResearchNote;
import net.thaumcraft.research.ResearchNotes;

/**
 * A mesa de pesquisa: o {@code TileResearchTableRenderer} e o {@code ModelResearchTable} da 4.2.3.5.
 *
 * <p>Um tampo de trinta e dois por dezesseis sobre quatro pernas e uma travessa, e em cima: seis folhas de
 * pergaminho empilhadas meio tortas; o tinteiro com a pena, quando há ferramentas de escrita na mesa; e o
 * rolo de pergaminho com a fita na cor da pesquisa, quando há uma nota.
 */
public class ResearchTableRenderer implements BlockEntityRenderer<ResearchTableBlockEntity, ResearchTableRenderer.State> {
    private static final Identifier TABLE = Thaumcraft.id("textures/models/restable.png");
    private static final Identifier SCROLL = Thaumcraft.id("textures/models/restable2.png");
    private static final Identifier PARCHMENT = Thaumcraft.id("textures/misc/parchment.png");
    private static final Identifier QUILL = Thaumcraft.id("textures/block/tablequill.png");
    private static final float UNIT = 1.0f / 16.0f;

    // as caixas do ModelResearchTable, cada uma já no lugar do seu ponto de giro; todas espelhadas
    private static final float[] BODY = BoxMesh.join(
            BoxMesh.mirror(BoxMesh.box(-8, 0, -8, 32, 4, 16, 0, 0, 128, 64)),
            BoxMesh.mirror(BoxMesh.box(-6, 4, -6, 4, 12, 4, 0, 24, 128, 64)),
            BoxMesh.mirror(BoxMesh.box(-6, 4, 2, 4, 12, 4, 0, 24, 128, 64)),
            BoxMesh.mirror(BoxMesh.box(18, 4, -6, 4, 12, 4, 0, 24, 128, 64)),
            BoxMesh.mirror(BoxMesh.box(18, 4, 2, 4, 12, 4, 0, 24, 128, 64)),
            BoxMesh.mirror(BoxMesh.box(-4, 10, -2, 24, 4, 4, 24, 24, 128, 64)));
    private static final float[] INKWELL = BoxMesh.mirror(BoxMesh.box(-6, -2, 3, 3, 2, 3, 0, 44, 128, 64));
    private static final float[] TUBE = BoxMesh.mirror(BoxMesh.box(-21, -0.5f, -8, 8, 2, 2, 0, 0, 128, 64));
    private static final float[] RIBBON = BoxMesh.mirror(BoxMesh.box(-15.1f, -0.275f, -6.75f, 1, 2, 2, 0, 4, 128, 64));
    /** O giro de dez radianos que o rolo tem no modelo original. */
    private static final float SCROLL_TURN = (float) Math.toDegrees(10.0);

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.EAST;
        boolean ink;
        boolean note;
        int noteColor = ResearchNotes.DEFAULT_COLOR;
    }

    public ResearchTableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ResearchTableBlockEntity table, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(table, state, crumbling);
        state.facing = table.getBlockState().getValue(ResearchTableBlock.FACING);
        state.ink = table.getItem(ResearchTableBlockEntity.INK).getItem() instanceof ScribingToolsItem;
        state.note = table.getItem(ResearchTableBlockEntity.NOTE).is(TCItems.RESEARCH_NOTES);
        ResearchNote note = ResearchNotes.get(table.getItem(ResearchTableBlockEntity.NOTE));
        state.noteColor = note == null ? ResearchNotes.DEFAULT_COLOR : note.color();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        int overlay = OverlayTexture.NO_OVERLAY;
        pose.pushPose();
        pose.translate(0.5f, 1.0f, 0.5f);
        pose.mulPose(Axis.XP.rotationDegrees(180.0f));
        // o original gira conforme para onde a outra metade está: a mesa cresce para o lado dela
        switch (state.facing) {
            case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(270.0f));
            case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(180.0f));
            default -> {
            }
        }
        box(pose, collector, TABLE, BODY, light, overlay, 0xFFFFFFFF, false);

        if (state.ink) {
            box(pose, collector, TABLE, INKWELL, light, overlay, 0xFFFFFFFF, true);
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(-90.0f));
            pose.mulPose(Axis.XP.rotationDegrees(180.0f));
            pose.translate(-0.17f, 0.1f, -0.15f);
            pose.mulPose(Axis.YP.rotationDegrees(15.0f));
            pose.scale(0.5f, 0.5f, 0.5f);
            collector.submitCustomGeometry(pose, RenderTypes.entityCutout(QUILL), (matrix, consumer) ->
                    ExtrudedSprite.draw(matrix, consumer, 16, 0.025f, light, overlay, 0xFFFFFFFF));
            pose.popPose();
        }

        // as seis folhas de pergaminho, cada uma um pouco mais torta
        for (int a = 0; a < 6; a++) {
            pose.pushPose();
            pose.translate(0.1f, -0.01f - a * 0.015f, 0.35f);
            pose.mulPose(Axis.XN.rotationDegrees(90.0f));
            pose.mulPose(Axis.ZP.rotationDegrees(15 + a % 3 * 2));
            pose.scale(0.5f, 0.6f, 0.6f);
            collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(PARCHMENT), (matrix, consumer) -> {
                sheet(matrix, consumer, light, overlay);
            });
            pose.popPose();
        }

        if (state.note) {
            pose.pushPose();
            pose.pushPose();
            pose.scale(UNIT, UNIT, UNIT);
            pose.translate(-2.0f, -2.0f, 2.0f);
            pose.mulPose(Axis.YP.rotationDegrees(SCROLL_TURN));
            pose.scale(16.0f, 16.0f, 16.0f);
            box(pose, collector, SCROLL, TUBE, light, overlay, 0xFFFFFFFF, false);
            pose.popPose();
            // a fita, na cor do aspecto principal da pesquisa, um quinto maior
            pose.scale(1.2f, 1.2f, 1.2f);
            pose.scale(UNIT, UNIT, UNIT);
            pose.translate(-2.0f, -2.0f, 2.0f);
            pose.mulPose(Axis.YP.rotationDegrees(SCROLL_TURN));
            pose.scale(16.0f, 16.0f, 16.0f);
            box(pose, collector, SCROLL, RIBBON, light, overlay, 0xFF000000 | state.noteColor, false);
            pose.popPose();
        }
        pose.popPose();
    }

    private static void sheet(PoseStack.Pose matrix, com.mojang.blaze3d.vertex.VertexConsumer consumer, int light,
                              int overlay) {
        float[][] corners = {{0, 1, 0, 1}, {1, 1, 1, 1}, {1, 0, 1, 0}, {0, 0, 0, 0}};
        for (int side = 0; side < 2; side++) {
            for (int i = 0; i < 4; i++) {
                float[] c = corners[side == 0 ? i : 3 - i];
                consumer.addVertex(matrix, c[0], c[1], 0.0f).setColor(0xFFFFFFFF).setUv(c[2], c[3])
                        .setOverlay(overlay).setLight(light).setNormal(matrix, 0.0f, 0.0f, side == 0 ? -1.0f : 1.0f);
            }
        }
    }

    private static void box(PoseStack pose, SubmitNodeCollector collector, Identifier texture, float[] mesh, int light,
                            int overlay, int colour, boolean translucent) {
        pose.pushPose();
        pose.scale(UNIT, UNIT, UNIT);
        collector.submitCustomGeometry(pose,
                translucent ? RenderTypes.entityTranslucent(texture) : RenderTypes.entityCutout(texture),
                (matrix, consumer) -> MeshDrawer.draw(mesh, matrix, consumer, light, overlay, colour));
        pose.popPose();
    }

    /** A mesa passa do bloco em que mora: não pode sumir quando só a outra metade está na tela. */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    /**
     * A mesa na mão e no inventário — e, por tabela, no Thaumonomicon: o mesmo corpo, as mesmas folhas e a mesma pena
     * do bloco. Como a mesa tem dois blocos de comprimento, ela encolhe e recua meio bloco para caber inteira na casa,
     * que é o que o original faz no {@code ItemBlockSpecialRenderer}.
     */
    public record Item() implements net.minecraft.client.renderer.special.SpecialModelRenderer<net.minecraft.util.Unit> {
        @Override
        public void submit(@org.jetbrains.annotations.Nullable net.minecraft.util.Unit ignored, PoseStack pose,
                           SubmitNodeCollector collector, int light, int overlay, boolean foil, int tint) {
            pose.pushPose();
            pose.translate(0.5f, 1.0f, 0.5f);
            pose.mulPose(Axis.XP.rotationDegrees(180.0f));
            pose.scale(0.6f, 0.6f, 0.6f);
            pose.translate(-0.5f, 0.0f, 0.0f);
            box(pose, collector, TABLE, BODY, light, overlay, 0xFFFFFFFF, false);
            box(pose, collector, TABLE, INKWELL, light, overlay, 0xFFFFFFFF, true);
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(-90.0f));
            pose.mulPose(Axis.XP.rotationDegrees(180.0f));
            pose.translate(-0.17f, 0.1f, -0.15f);
            pose.mulPose(Axis.YP.rotationDegrees(15.0f));
            pose.scale(0.5f, 0.5f, 0.5f);
            collector.submitCustomGeometry(pose, RenderTypes.entityCutout(QUILL), (matrix, consumer) ->
                    ExtrudedSprite.draw(matrix, consumer, 16, 0.025f, light, overlay, 0xFFFFFFFF));
            pose.popPose();
            for (int a = 0; a < 6; a++) {
                pose.pushPose();
                pose.translate(0.1f, -0.01f - a * 0.015f, 0.35f);
                pose.mulPose(Axis.XN.rotationDegrees(90.0f));
                pose.mulPose(Axis.ZP.rotationDegrees(15 + a % 3 * 2));
                pose.scale(0.5f, 0.6f, 0.6f);
                collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(PARCHMENT),
                        (matrix, consumer) -> sheet(matrix, consumer, light, overlay));
                pose.popPose();
            }
            pose.popPose();
        }

        @Override
        public void getExtents(java.util.function.Consumer<org.joml.Vector3fc> extents) {
            extents.accept(new org.joml.Vector3f(0.0f, 0.0f, 0.0f));
            extents.accept(new org.joml.Vector3f(1.0f, 1.0f, 1.0f));
        }

        @Override
        public net.minecraft.util.Unit extractArgument(net.minecraft.world.item.ItemStack stack) {
            return net.minecraft.util.Unit.INSTANCE;
        }
    }

    /** O que o arquivo do item declara: nada além de ser a mesa de pesquisa. */
    public record Unbaked() implements net.minecraft.client.renderer.special.SpecialModelRenderer.Unbaked<net.minecraft.util.Unit> {
        public static final com.mojang.serialization.MapCodec<Unbaked> CODEC =
                com.mojang.serialization.MapCodec.unit(new Unbaked());

        @Override
        public net.minecraft.client.renderer.special.SpecialModelRenderer<net.minecraft.util.Unit> bake(
                net.minecraft.client.renderer.special.SpecialModelRenderer.BakingContext context) {
            return new Item();
        }

        @Override
        public com.mojang.serialization.MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
