package net.thaumcraft.mortuorum.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.BoxMesh;
import net.thaumcraft.client.render.MeshDrawer;
import net.thaumcraft.mortuorum.SummoningAltarBlock;
import net.thaumcraft.mortuorum.SummoningAltarBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O Altar de Invocação no mundo: o {@code TileEntityAltarRenderer} do Necromancy.
 *
 * <p>São as onze caixas do {@code ModelAltar} — a coluna de pedra com o encosto torto e a mesa comprida que sai
 * dela por dois blocos —, com a tocha acesa em cima e o livro aberto ao lado dela, que é o mesmo livro da mesa de
 * encantamentos do jogo.
 */
public class SummoningAltarRenderer implements BlockEntityRenderer<SummoningAltarBlockEntity, SummoningAltarRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/summoning_altar.png");
    private static final Identifier BOOK = Identifier.withDefaultNamespace("textures/entity/enchantment/enchanting_table_book.png");

    // as onze caixas do ModelAltar, todas espelhadas, numa folha de 128 por 64
    private static final float[] BOTTOM = BoxMesh.mirror(BoxMesh.box(0, 0, 0, 16, 3, 16, 0, 45, 128, 64));
    private static final float[] PILLAR = BoxMesh.mirror(BoxMesh.box(0, 0, 0, 11, 17, 6, 0, 41, 128, 64));
    private static final float[] BASE = BoxMesh.mirror(BoxMesh.box(0, 0, 0, 16, 2, 16, 64, 46, 128, 64));
    private static final float[] BACK1 = BoxMesh.mirror(BoxMesh.box(0, 0, 0, 1, 2, 16, 0, 46, 128, 64));
    private static final float[] BACK2 = BoxMesh.mirror(BoxMesh.box(-8, 0, 0, 8, 2, 1, 0, 54, 128, 64));
    private static final float[] BACK3 = BoxMesh.mirror(BoxMesh.box(-8, 0, 0, 8, 2, 1, 0, 52, 128, 64));
    private static final float[] CONNECTION = BoxMesh.mirror(BoxMesh.box(0, 0, 0, 1, 13, 1, 0, 0, 128, 64));
    private static final float[] TABLE_BOTTOM = BoxMesh.mirror(BoxMesh.box(0, 0, 0, 32, 3, 16, 0, 0, 128, 64));
    private static final float[] TABLE_MIDDLE = BoxMesh.mirror(BoxMesh.box(0, 0, 0, 29, 11, 10, 0, 0, 128, 64));
    private static final float[] TABLE_TOP = BoxMesh.mirror(BoxMesh.box(0, 0, 0, 32, 2, 16, 0, 0, 128, 64));

    /** O grau que o {@code pillarAltar} do original tem de inclinação. */
    private static final float PILLAR_TILT = 0.0174533f;
    /** O quinze graus para trás dos dois encostos. */
    private static final float BACK_TILT = (float) (-Math.PI / 12.0);

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        final ItemStackRenderState torch = new ItemStackRenderState();
    }

    private final BookModel book;
    private final ItemModelResolver items;

    public SummoningAltarRenderer(BlockEntityRendererProvider.Context context) {
        this.book = new BookModel(context.bakeLayer(ModelLayers.BOOK));
        this.items = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    /** A mesa sai do bloco por mais dois, então o altar tem de continuar a ser desenhado fora do quadro. */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public void extractRenderState(SummoningAltarBlockEntity altar, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(altar, state, partial, camera, crumbling);
        state.facing = altar.getBlockState().hasProperty(SummoningAltarBlock.FACING)
                ? altar.getBlockState().getValue(SummoningAltarBlock.FACING) : Direction.NORTH;
        this.items.updateForTopItem(state.torch, new ItemStack(Items.TORCH), ItemDisplayContext.FIXED,
                altar.getLevel(), null, 0);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        // o translate(x, y+2, z+1) com o scale(1,-1,-1) e o translate(0.5,0.5,0.5) do original, já resolvidos
        float angle = switch (state.facing) {
            case NORTH -> 90.0f;
            case WEST -> 180.0f;
            case SOUTH -> 270.0f;
            default -> 0.0f;
        };
        pose.pushPose();
        pose.translate(0.5f, 1.5f, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(angle));
        pose.scale(1.0f, -1.0f, -1.0f);

        int light = state.lightCoords;
        boxes(pose, collector, light);

        // a tocha acesa e, junto dela, o livro aberto
        pose.pushPose();
        pose.translate(0.3f, -0.1f, -0.3f);
        pose.scale(-0.5f, -0.5f, 0.5f);
        state.torch.submit(pose, collector, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);
        pose.scale(0.1f, 0.1f, 0.1f);
        pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
        pose.translate(-4.0f, -8.0f, 8.0f);
        collector.submitModel(this.book, new BookModel.State(1.22f, 0.0f, 0.0f), pose,
                RenderTypes.entitySolid(BOOK), 0xF000F0, OverlayTexture.NO_OVERLAY, 0xFFE5E5E5, null, 0, null);
        pose.popPose();

        pose.popPose();
    }

    /** As onze caixas do {@code ModelAltar}, já no quadro virado do original. */
    static void boxes(PoseStack pose, SubmitNodeCollector collector, int light) {
        part(pose, collector, BOTTOM, -8, 21, -8, 0.0f, 0.0f, light);
        part(pose, collector, PILLAR, -3, 4, -3, PILLAR_TILT, 0.0f, light);
        part(pose, collector, BASE, -8, 2, -8, 0.0f, 0.0f, light);
        part(pose, collector, BACK1, 7, 0, -8, 0.0f, 0.0f, light);
        part(pose, collector, BACK2, 7, 0, 7.1f, 0.0f, BACK_TILT, light);
        part(pose, collector, BACK3, 7, 0, -8.1f, 0.0f, BACK_TILT, light);
        part(pose, collector, CONNECTION, 7, 8, 3, 0.0f, 0.0f, light);
        part(pose, collector, TABLE_BOTTOM, 8, 21, -8, 0.0f, 0.0f, light);
        part(pose, collector, CONNECTION, 7, 8, -4, 0.0f, 0.0f, light);
        part(pose, collector, TABLE_MIDDLE, 8, 10, -5, 0.0f, 0.0f, light);
        part(pose, collector, TABLE_TOP, 8, 8, -8, 0.0f, 0.0f, light);
    }

    /** Uma caixa do modelo: o ponto de giro, o giro em Z e o giro em X, na ordem do {@code ModelRenderer} antigo. */
    private static void part(PoseStack pose, SubmitNodeCollector collector, float[] mesh,
                            float px, float py, float pz, float rotX, float rotZ, int light) {
        pose.pushPose();
        pose.translate(px / 16.0f, py / 16.0f, pz / 16.0f);
        if (rotZ != 0.0f) pose.mulPose(Axis.ZP.rotation(rotZ));
        if (rotX != 0.0f) pose.mulPose(Axis.XP.rotation(rotX));
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE),
                (m, v) -> MeshDrawer.draw(mesh, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
    }
}
