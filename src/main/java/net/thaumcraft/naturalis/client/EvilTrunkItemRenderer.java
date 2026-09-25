package net.thaumcraft.naturalis.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.naturalis.EvilTrunkEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O Baú Maligno na mão e no inventário: o {@code RenderItemEvilTrunkSpawner} do Magia Naturalis 0.5.0 — o item que
 * o chama é desenhado com o modelo do baú do feitio dele, de boca fechada.
 */
public record EvilTrunkItemRenderer(EvilTrunkEntity.Kind kind, ModelPart root, Identifier skin)
        implements SpecialModelRenderer<Unit> {

    @Override
    public void submit(@Nullable Unit ignored, PoseStack pose, SubmitNodeCollector collector,
                       int light, int overlay, boolean foil, int tint) {
        pose.pushPose();
        // o modelo nasce de cabeça para baixo, como os de criatura; o original o vira e o encaixa na casa
        pose.translate(0.5f, 1.0f, 0.5f);
        pose.scale(1.0f, -1.0f, -1.0f);
        collector.submitModelPart(this.root, pose, RenderTypes.entityCutout(this.skin), light, overlay, null);
        pose.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        extents.accept(new Vector3f(-0.5f, 0.0f, -0.5f));
        extents.accept(new Vector3f(0.5f, 1.0f, 0.5f));
    }

    @Override
    public Unit extractArgument(ItemStack stack) {
        return Unit.INSTANCE;
    }

    /** O que o arquivo do item declara para pedir este desenhista. */
    public record Unbaked(EvilTrunkEntity.Kind kind) implements SpecialModelRenderer.Unbaked<Unit> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                com.mojang.serialization.Codec.STRING.fieldOf("kind")
                        .forGetter(unbaked -> unbaked.kind().name().toLowerCase())
        ).apply(instance, name -> new Unbaked(EvilTrunkEntity.Kind.valueOf(name.toUpperCase()))));

        @Override
        public SpecialModelRenderer<Unit> bake(SpecialModelRenderer.BakingContext context) {
            ModelPart root = context.entityModelSet().bakeLayer(EvilTrunkRenderer.LAYERS.get(this.kind));
            return new EvilTrunkItemRenderer(this.kind, root,
                    Thaumcraft.id("textures/models/" + switch (this.kind) {
                        case CORRUPTED -> "trunk_corrupted";
                        case SINISTER -> "trunk_sinister";
                        case DEMONIC -> "trunk_demonic_wings";
                        case TAINTED -> "trunk_tainted";
                    } + ".png"));
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
