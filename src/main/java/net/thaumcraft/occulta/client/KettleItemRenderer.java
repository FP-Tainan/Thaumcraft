package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O Caldeirão de Pote na mão e no inventário: o mesmo modelo do bloco, virado para o norte, <b>vazio</b> e sem
 * frasco nenhum — que é o que ele é antes de alguém o assentar.
 */
public record KettleItemRenderer() implements SpecialModelRenderer<Unit> {
    @Override
    public void submit(@Nullable Unit ignored, PoseStack pose, SubmitNodeCollector collector,
                       int light, int overlay, boolean foil, int tint) {
        KettleRenderer.State vazio = new KettleRenderer.State();
        vazio.facing = Direction.NORTH;
        vazio.lightCoords = light;
        vazio.inHand = true;
        KettleRenderer.draw(pose, collector, vazio);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        extents.accept(new Vector3f(0.0f, 0.0f, 0.0f));
        extents.accept(new Vector3f(1.0f, 1.0f, 1.0f));
    }

    @Override
    public Unit extractArgument(ItemStack stack) {
        return Unit.INSTANCE;
    }

    /** O que o arquivo do item declara para pedir este desenhista. */
    public record Unbaked() implements SpecialModelRenderer.Unbaked<Unit> {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public SpecialModelRenderer<Unit> bake(net.minecraft.client.renderer.special.SpecialModelRenderer.BakingContext context) {
            return new KettleItemRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
