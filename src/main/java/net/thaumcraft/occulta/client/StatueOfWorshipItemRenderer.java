package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.TaglockItem;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * A <b>Estátua de Adoração</b> na mão e no inventário, com a mesma malha do bloco.
 *
 * <p>O original faz o mesmo: ele prende o desenhista da alma dela <b>também</b> ao item, de modo que uma
 * estátua na mão é a mesma estátua que está no chão. Sem isto, o item seria um quadrado chapado com a
 * folha de pedra desenhada para um boneco — que é o engano que a {@code Textura de chapa de modelo} do
 * porte já ensinou a evitar.
 *
 * <p>E ela leva a <b>cara do dono</b> na mão também: uma estátua presa a alguém mostra-o de quem é antes
 * mesmo de se pôr no chão.
 */
public class StatueOfWorshipItemRenderer implements SpecialModelRenderer<TaglockItem.Taglock> {
    /** O quanto o modelo desce para caber centrado na caixa do item. */
    public static final float DESCE = -0.1f;

    private final ModelPart pedra;
    private final ModelPart pele;

    public StatueOfWorshipItemRenderer(ModelPart pele, ModelPart pedra) {
        this.pele = pele;
        this.pedra = pedra;
    }

    @Override
    public void submit(@Nullable TaglockItem.Taglock dono, PoseStack pose,
                       SubmitNodeCollector coletor, int luz, int overlay, boolean brilho, int cor) {
        pose.pushPose();
        pose.translate(0.0f, DESCE, 0.0f);
        StatueOfWorshipRenderer.desenha(pose, coletor, this.pele, this.pedra, luz,
                Direction.NORTH, dono);
        pose.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> cantos) {
        cantos.accept(new Vector3f(0.2f, DESCE, 0.2f));
        cantos.accept(new Vector3f(0.8f, DESCE + 0.95f, 0.8f));
    }

    @Override
    public @Nullable TaglockItem.Taglock extractArgument(ItemStack oquê) {
        return oquê.get(OccultaComponents.TAGLOCK);
    }

    /** O que o arquivo do item declara para pedir este desenhista. */
    public record Unbaked() implements SpecialModelRenderer.Unbaked<TaglockItem.Taglock> {
        public static final MapCodec<Unbaked> CODEC =
                RecordCodecBuilder.mapCodec(campo -> campo.point(new Unbaked()));

        @Override
        public SpecialModelRenderer<TaglockItem.Taglock> bake(SpecialModelRenderer.BakingContext onde) {
            return new StatueOfWorshipItemRenderer(
                    onde.entityModelSet().bakeLayer(StatueOfWorshipModel.PELE_MALHA),
                    onde.entityModelSet().bakeLayer(StatueOfWorshipModel.PEDRA_MALHA));
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
