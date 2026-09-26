package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
 * O Funil de Fumos na mão e no inventário.
 *
 * <p>Sem forno nenhum em volta, o modelo do original mostra só o corpo largo — e o filtro, se for o com filtro.
 * É o que se vê aqui, que é como ele aparece na mão de quem o carrega.
 */
public record FumeFunnelItemRenderer(boolean filtered) implements SpecialModelRenderer<Unit> {
    @Override
    public void submit(@Nullable Unit ignored, PoseStack pose, SubmitNodeCollector collector,
                       int light, int overlay, boolean foil, int tint) {
        FumeFunnelRenderer.funnel(pose, collector, Direction.NORTH, this.filtered, false, false, false, light);
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
    public record Unbaked(boolean filtered) implements SpecialModelRenderer.Unbaked<Unit> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("filtered", false).forGetter(Unbaked::filtered)
        ).apply(instance, Unbaked::new));

        @Override
        public SpecialModelRenderer<Unit> bake(SpecialModelRenderer.BakingContext context) {
            return new FumeFunnelItemRenderer(this.filtered);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
