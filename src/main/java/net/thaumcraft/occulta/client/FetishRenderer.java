package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.fetish.FetishBlock;
import net.thaumcraft.occulta.fetish.FetishBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Os fetiches no mundo: o {@code RenderFetish} e os dois bonecos dele.
 *
 * <p>Dois dos três têm boneco. O <b>Espantalho</b> é uma estaca com uma travessa, um saco na cabeça e dois
 * braços de palha; o <b>Ídolo de Treant</b> é um toco de madeira com braços tortos, pernas em dois pedaços
 * e três galhos no toucado. A <b>Escada de Bruxa</b> não tem boneco nenhum: ela é uma folha cruzada, como
 * um rebento, e desenha-se como bloco.
 *
 * <h2>O que leva tinta e o que não leva</h2>
 *
 * <p>No Espantalho, as <b>estacas</b> e as <b>caras</b> saem sem tinta, e a <b>cabeça</b>, o <b>corpo</b> e
 * os <b>braços</b> saem pintados — é por isso que um espantalho azul tem a estaca de madeira e o fato
 * azul. No Ídolo, só a <b>cara</b> é pintada.
 *
 * <p>É um desenho de duas passagens, e vale a pena ver por quê: a textura é cinzenta, e a cor é dada na
 * hora. Assim um só arquivo de dezesseis cores não existe, e as dezesseis saem da mesma folha.
 *
 * <h2>E a cópia do outro lado</h2>
 *
 * <p>A peça <b>espectral</b> — a que o mundo dos sonhos põe no mundo de cima — sai a <b>seis décimos</b>,
 * e dentro dela as partes pintadas a <b>sete</b>. Vê-se que está ali e vê-se que não está.
 */
public class FetishRenderer implements BlockEntityRenderer<FetishBlockEntity, FetishRenderer.State> {
    public static final ModelLayerLocation ESPANTALHO =
            new ModelLayerLocation(Thaumcraft.id("scarecrow"), "main");
    public static final ModelLayerLocation IDOLO =
            new ModelLayerLocation(Thaumcraft.id("treant_idol"), "main");

    public static final Identifier FOLHA_ESPANTALHO = Thaumcraft.id("textures/block/scarecrow.png");
    public static final Identifier FOLHA_IDOLO = Thaumcraft.id("textures/block/trent.png");

    /** A transparência da cópia espectral, e a das partes pintadas dela. */
    public static final int ESPECTRAL = 0x99FFFFFF;
    public static final int ESPECTRAL_PINTADO = 0xB3FFFFFF;

    private final ModelPart espantalho;
    private final ModelPart ídolo;

    public static class State extends BlockEntityRenderState {
        public Direction rumo = Direction.NORTH;
        public boolean espectral;
        public int cor = FetishBlockEntity.DEFAULT_COLOR;
        public boolean ídolo;
        public boolean escada;
    }

    public FetishRenderer(BlockEntityRendererProvider.Context contexto) {
        this.espantalho = contexto.bakeLayer(ESPANTALHO);
        this.ídolo = contexto.bakeLayer(IDOLO);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FetishBlockEntity alma, State estado, float parcial,
                                   net.minecraft.world.phys.Vec3 câmara,
                                   @Nullable net.minecraft.client.renderer.feature.ModelFeatureRenderer
                                           .CrumblingOverlay quebrando) {
        BlockEntityRenderer.super.extractRenderState(alma, estado, parcial, câmara, quebrando);
        estado.rumo = alma.getBlockState().getValue(FetishBlock.FACING);
        estado.espectral = alma.spectral();
        estado.cor = alma.color();
        estado.ídolo = alma.getBlockState().is(OccultaBlocks.TREANT_IDOL);
        estado.escada = alma.getBlockState().is(OccultaBlocks.WITCHS_LADDER);
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor,
                       CameraRenderState câmara) {
        if (estado.escada) return;
        desenha(pose, coletor, estado.ídolo ? this.ídolo : this.espantalho, estado.ídolo,
                estado.lightCoords, estado.rumo, estado.cor, estado.espectral);
    }

    /**
     * Desenha um fetiche, no mundo ou na mão.
     *
     * <p>O virar de cabeça para baixo é o de sempre — meia volta em Z e um bloco para baixo — e a seguir
     * vem o giro, que no original sai dos números dois a cinco do bloco e aqui sai do rumo dele.
     */
    public static void desenha(PoseStack pose, SubmitNodeCollector coletor, ModelPart raiz,
                               boolean ídolo, int luz, Direction rumo, int cor, boolean espectral) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, -1.0f, 0.0f);
        pose.mulPose(Axis.YP.rotationDegrees(rumo.toYRot() + 180.0f));

        var tipo = RenderTypes.entityTranslucent(ídolo ? FOLHA_IDOLO : FOLHA_ESPANTALHO);
        int limpo = espectral ? ESPECTRAL : 0xFFFFFFFF;
        int pintado = (espectral ? ESPECTRAL_PINTADO : 0xFF000000)
                | (FleeceColours.of(cor) & 0xFFFFFF);

        for (String qual : ídolo ? SEM_TINTA_IDOLO : SEM_TINTA_ESPANTALHO) {
            coletor.submitModelPart(raiz.getChild(qual), pose, tipo, luz, OverlayTexture.NO_OVERLAY,
                    null, limpo, null);
        }
        for (String qual : ídolo ? COM_TINTA_IDOLO : COM_TINTA_ESPANTALHO) {
            coletor.submitModelPart(raiz.getChild(qual), pose, tipo, luz, OverlayTexture.NO_OVERLAY,
                    null, pintado, null);
        }
        pose.popPose();
    }

    /** As peças do Espantalho que saem sem tinta, e as que saem pintadas. */
    public static final String[] SEM_TINTA_ESPANTALHO = {
            "pole_vertical", "pole_horizontal", "head_inner", "arm_left_inner", "arm_right_inner",
    };
    public static final String[] COM_TINTA_ESPANTALHO = {"head", "body", "arm_left", "arm_right"};

    /** E as do Ídolo: só a cara é pintada. */
    public static final String[] SEM_TINTA_IDOLO = {
            "body", "arm_left", "arm_right", "leg_left_upper", "leg_left_lower",
            "leg_right_upper", "leg_right_lower", "headdress1", "headdress2", "headdress3",
    };
    public static final String[] COM_TINTA_IDOLO = {"face"};

    // ------------------------------------------------------------------ as duas malhas

    /** O <b>Espantalho</b>, caixa por caixa. */
    public static LayerDefinition espantalho() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        raiz.addOrReplaceChild("pole_vertical", CubeListBuilder.create()
                        .texOffs(0, 2).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 15.0f, 2.0f),
                PartPose.offset(0.0f, 9.0f, 0.0f));
        raiz.addOrReplaceChild("pole_horizontal", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-8.0f, 0.0f, -0.5f, 16.0f, 1.0f, 1.0f),
                PartPose.offset(0.0f, 13.0f, 0.0f));
        raiz.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(12, 21).addBox(-2.0f, -4.0f, -2.0f, 4.0f, 5.0f, 4.0f),
                PartPose.offset(0.0f, 12.0f, 0.0f));
        raiz.addOrReplaceChild("head_inner", CubeListBuilder.create()
                        .texOffs(29, 25).addBox(-2.0f, -4.0f, -1.9f, 4.0f, 5.0f, 0.0f),
                PartPose.offset(0.0f, 12.0f, 0.0f));
        raiz.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(8, 2).addBox(-3.0f, 0.0f, -1.5f, 6.0f, 9.0f, 3.0f),
                PartPose.offset(0.0f, 12.5f, 0.0f));
        raiz.addOrReplaceChild("arm_left", CubeListBuilder.create()
                        .texOffs(0, 23).addBox(0.0f, -0.5f, -1.5f, 3.0f, 4.0f, 3.0f),
                PartPose.offset(3.0f, 13.0f, 0.0f));
        raiz.addOrReplaceChild("arm_left_inner", CubeListBuilder.create()
                        .texOffs(29, 25).addBox(2.9f, -0.5f, -1.5f, 0.0f, 4.0f, 3.0f),
                PartPose.offset(3.0f, 13.0f, 0.0f));
        raiz.addOrReplaceChild("arm_right", CubeListBuilder.create()
                        .texOffs(0, 23).addBox(-3.0f, -0.5f, -1.5f, 3.0f, 4.0f, 3.0f),
                PartPose.offset(-3.0f, 13.0f, 0.0f));
        raiz.addOrReplaceChild("arm_right_inner", CubeListBuilder.create()
                        .texOffs(29, 25).addBox(-2.9f, -0.5f, -1.5f, 0.0f, 4.0f, 3.0f),
                PartPose.offset(-3.0f, 13.0f, 0.0f));

        return LayerDefinition.create(malha, 64, 64);
    }

    /** E o <b>Ídolo de Treant</b>, com os três galhos virados para trás. */
    public static LayerDefinition ídolo() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        raiz.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 14).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 9.0f, 6.0f),
                PartPose.offset(0.0f, 12.0f, 0.0f));
        raiz.addOrReplaceChild("face", CubeListBuilder.create()
                        .texOffs(18, 1).addBox(-3.0f, 1.0f, -2.9f, 6.0f, 7.0f, 0.0f),
                PartPose.offset(0.0f, 12.0f, 0.0f));
        raiz.addOrReplaceChild("arm_left", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 5.0f, 2.0f),
                PartPose.offsetAndRotation(2.0f, 13.0f, 0.0f, -0.1858931f, 0.0f, -0.7435722f));
        raiz.addOrReplaceChild("arm_right", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 5.0f, 2.0f),
                PartPose.offsetAndRotation(-2.0f, 13.0f, 0.0f, -0.1858931f, 0.0f, 0.8551081f));
        raiz.addOrReplaceChild("leg_left_upper", CubeListBuilder.create()
                        .texOffs(9, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 5.0f, 2.0f),
                PartPose.offsetAndRotation(2.0f, 18.0f, 0.0f, -0.1487144f, 0.0f, -0.2602503f));
        raiz.addOrReplaceChild("leg_left_lower", CubeListBuilder.create()
                        .texOffs(11, 8).addBox(-0.5f, 0.0f, -0.5f, 1.0f, 3.0f, 1.0f),
                PartPose.offsetAndRotation(3.0f, 21.0f, -0.5f, 0.0743572f, 0.0f, -0.1115358f));
        raiz.addOrReplaceChild("leg_right_upper", CubeListBuilder.create()
                        .texOffs(9, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 5.0f, 2.0f),
                PartPose.offsetAndRotation(-2.0f, 18.0f, 0.0f, 0.1858931f, 0.0f, 0.3346075f));
        raiz.addOrReplaceChild("leg_right_lower", CubeListBuilder.create()
                        .texOffs(11, 8).addBox(-0.5f, 0.0f, -0.5f, 1.0f, 3.0f, 1.0f),
                PartPose.offsetAndRotation(-3.0f, 21.0f, 0.5f, 0.1858931f, 0.0f, 0.2230717f));
        raiz.addOrReplaceChild("headdress1", CubeListBuilder.create()
                        .texOffs(0, 30).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 5.0f, 2.0f),
                PartPose.offsetAndRotation(0.0f, 13.0f, 1.0f, 0.1115358f, 0.0f, -2.862753f));
        raiz.addOrReplaceChild("headdress2", CubeListBuilder.create()
                        .texOffs(0, 30).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 5.0f, 2.0f),
                PartPose.offsetAndRotation(-1.0f, 13.0f, 0.0f, 0.3717861f, 0.0f, 2.639681f));
        raiz.addOrReplaceChild("headdress3", CubeListBuilder.create()
                        .texOffs(0, 30).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 5.0f, 2.0f),
                PartPose.offsetAndRotation(-1.0f, 13.0f, 0.0f, -0.4461433f, 0.0f, 2.862753f));

        return LayerDefinition.create(malha, 64, 64);
    }
}
