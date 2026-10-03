package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * A <b>Lança do Caçador</b> na mão: o {@code ModelHuntsmanSpear} e o {@code RenderHuntsmanSpear} do Witchery.
 *
 * <p>Três peças, e nenhuma delas é um bloco: um <b>cabo de trinta e dois de comprido e um de grosso</b>, e
 * <b>duas folhas chatas cruzadas</b> na ponta — uma em x, outra em z — que de qualquer ângulo parecem uma
 * lâmina só. É o mesmo truque da galhada do Caçador, e é a razão de a lança não parecer uma tábua.
 *
 * <p>No inventário ela é <b>plana</b>, como no original: o desenhista dele só respondia por {@code EQUIPPED} e
 * {@code EQUIPPED_FIRST_PERSON}, e o ícone do inventário era a figura chata.
 *
 * <p><b>Uma diferença, declarada</b>: o original punha a lança na mão com três giros e três deslocamentos
 * seus — cem graus em x, cinquenta e um negativos em y, oitenta e um negativos em z —, porque o jogo de então
 * não lhe dava nenhum. O jogo de hoje <b>tem</b> um jeito próprio de segurar uma haste comprida, que é o do
 * tridente, e é esse que se usa aqui. Os seis números do original eram feitos à mão para chegar ao mesmo
 * lugar.
 */
public record HuntsmansSpearRenderer(ModelPart raiz) implements SpecialModelRenderer<Unit> {
    public static final ModelLayerLocation SPEAR =
            new ModelLayerLocation(Thaumcraft.id("huntsmans_spear"), "main");

    private static final Identifier FOLHA = Thaumcraft.id("textures/entity/huntsmans_spear.png");

    /** Sessenta e quatro por sessenta e quatro de textura, como no original. */
    public static LayerDefinition spear() {
        MeshDefinition malha = new MeshDefinition();
        var raiz = malha.getRoot();

        raiz.addOrReplaceChild("shaft", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(0, 0).addBox(-0.5f, -18.0f, -0.5f, 1.0f, 32.0f, 1.0f),
                PartPose.ZERO);
        raiz.addOrReplaceChild("head_front", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(6, 3).addBox(-1.5f, -6.0f, 0.0f, 3.0f, 6.0f, 0.0f),
                PartPose.offset(0.0f, -17.0f, 0.0f));
        raiz.addOrReplaceChild("head_side", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(6, 0).addBox(0.0f, -6.0f, -1.5f, 0.0f, 6.0f, 3.0f),
                PartPose.offset(0.0f, -17.0f, 0.0f));

        return LayerDefinition.create(malha, 64, 64);
    }

    @Override
    public void submit(@Nullable Unit nada, PoseStack pose, SubmitNodeCollector coletor,
                       int luz, int porCima, boolean brilho, int cor) {
        pose.pushPose();
        // o de cabeça para baixo do Techne, e a haste a apontar para a frente da mão
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        var tipo = RenderTypes.entityCutout(FOLHA);
        for (String nome : new String[]{"shaft", "head_front", "head_side"}) {
            coletor.submitModelPart(this.raiz.getChild(nome), pose, tipo, luz,
                    OverlayTexture.NO_OVERLAY, null);
        }
        pose.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> cantos) {
        cantos.accept(new Vector3f(0.0f, 0.0f, 0.0f));
        cantos.accept(new Vector3f(1.0f, 2.0f, 1.0f));
    }

    @Override
    public Unit extractArgument(ItemStack qual) {
        return Unit.INSTANCE;
    }

    /** O que o arquivo do item declara para pedir este desenhista. */
    public record Unbaked() implements SpecialModelRenderer.Unbaked<Unit> {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public SpecialModelRenderer<Unit> bake(SpecialModelRenderer.BakingContext contexto) {
            return new HuntsmansSpearRenderer(contexto.entityModelSet().bakeLayer(SPEAR));
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
