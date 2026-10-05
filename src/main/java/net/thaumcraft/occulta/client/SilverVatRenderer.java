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
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.SilverVatBlockEntity;

/**
 * A <b>Tina de Prata</b> no mundo: o {@code ModelSilverVat} e o {@code RenderSilverVat} do Witchery.
 *
 * <p>Ela é uma bacia de cinco caixas — fundo e quatro paredes —, e o que a torna interessante são as outras
 * duas coisas que o desenhista dela faz:
 *
 * <ul>
 *   <li><b>os bicos</b>: de cada lado em que houver uma <b>máquina</b> — uma coisa com alma —, ela mostra
 *       um bico virado para ela. Uma tina entre duas fornalhas tem dois bicos; uma tina no meio do campo
 *       não tem nenhum;</li>
 *   <li><b>as camadas</b>: o pó lá dentro sobe em <b>oito pedacinhos</b>, um por cada oito pós. De fora
 *       vê-se quanto ela já juntou, sem se abrir nada.</li>
 * </ul>
 *
 * <p>É o melhor exemplo do mod de um bloco que <b>conta o seu estado pelo corpo</b>, e não por um número
 * numa tela.
 */
public class SilverVatRenderer implements BlockEntityRenderer<SilverVatBlockEntity,
        SilverVatRenderer.State> {
    public static final ModelLayerLocation TINA = new ModelLayerLocation(Thaumcraft.id("silver_vat"), "main");
    public static final Identifier FOLHA = Thaumcraft.id("textures/block/silver_vat.png");

    /** As cinco peças que estão sempre lá. */
    private static final String[] BACIA = {"base", "side_right", "side_front", "side_back", "side_left"};

    /** E os bicos, dois por lado: o de cima e o de baixo. */
    private static final String[][] BICOS = {
        {"spout_upper_left", "spout_lower_left"},
        {"spout_upper_right", "spout_lower_right"},
        {"spout_upper_front", "spout_lower_front"},
        {"spout_upper_back", "spout_lower_back"},
    };

    /** Os lados a que cada par de bicos olha, na ordem do original. */
    private static final Direction[] LADOS = {
        Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH,
    };

    private final ModelPart raiz;

    public static class State extends BlockEntityRenderState {
        public int camadas;
        public final boolean[] bicos = new boolean[4];
    }

    public SilverVatRenderer(BlockEntityRendererProvider.Context contexto) {
        this.raiz = contexto.bakeLayer(TINA);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SilverVatBlockEntity tina, State estado, float parcial,
                                   net.minecraft.world.phys.Vec3 câmara,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer
                                           .@org.jetbrains.annotations.Nullable CrumblingOverlay quebrando) {
        BlockEntityRenderer.super.extractRenderState(tina, estado, parcial, câmara, quebrando);
        estado.camadas = tina.camadas();
        var mundo = tina.getLevel();
        for (int volta = 0; volta < LADOS.length; volta++) {
            estado.bicos[volta] = mundo != null
                    && mundo.getBlockEntity(tina.getBlockPos().relative(LADOS[volta])) != null;
        }
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor,
                       CameraRenderState câmara) {
        RenderType tipo = RenderTypes.entityCutout(FOLHA);
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, -1.0f, 0.0f);

        for (String qual : BACIA) {
            coletor.submitModelPart(this.raiz.getChild(qual), pose, tipo, estado.lightCoords,
                    OverlayTexture.NO_OVERLAY, null);
        }
        for (int volta = 0; volta < BICOS.length; volta++) {
            if (!estado.bicos[volta]) continue;
            for (String qual : BICOS[volta]) {
                coletor.submitModelPart(this.raiz.getChild(qual), pose, tipo, estado.lightCoords,
                        OverlayTexture.NO_OVERLAY, null);
            }
        }
        for (int volta = 0; volta < estado.camadas; volta++) {
            coletor.submitModelPart(this.raiz.getChild("silver" + (volta + 1)), pose, tipo,
                    estado.lightCoords, OverlayTexture.NO_OVERLAY, null);
        }
        pose.popPose();
    }

    /** As vinte e uma caixas do {@code ModelSilverVat}, número por número. */
    public static LayerDefinition tina() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        caixa(raiz, "base", 0, 19, -6.0f, 23.0f, -6.0f, 12, 1, 12);
        caixa(raiz, "side_right", 38, 10, -7.0f, 16.0f, -6.0f, 1, 8, 12);
        caixa(raiz, "side_left", 38, 10, 6.0f, 16.0f, -6.0f, 1, 8, 12);
        caixa(raiz, "side_front", 34, 0, -7.0f, 16.0f, -7.0f, 14, 8, 1);
        caixa(raiz, "side_back", 34, 0, -7.0f, 16.0f, 6.0f, 14, 8, 1);

        caixa(raiz, "spout_upper_left", 15, 3, 4.0f, 14.0f, -1.5f, 4, 2, 3);
        caixa(raiz, "spout_lower_left", 15, 0, 4.2f, 16.0f, -0.5f, 1, 1, 1);
        caixa(raiz, "spout_upper_right", 15, 3, -8.0f, 14.0f, -1.5f, 4, 2, 3);
        caixa(raiz, "spout_lower_right", 15, 0, -5.2f, 16.0f, -0.5f, 1, 1, 1);
        caixa(raiz, "spout_upper_front", 15, 9, -1.5f, 14.0f, -8.0f, 3, 2, 4);
        caixa(raiz, "spout_lower_front", 15, 0, -0.5f, 16.0f, -5.2f, 1, 1, 1);
        caixa(raiz, "spout_upper_back", 15, 9, -1.5f, 14.0f, 4.0f, 3, 2, 4);
        caixa(raiz, "spout_lower_back", 15, 0, -0.5f, 16.0f, 4.2f, 1, 1, 1);

        caixa(raiz, "silver1", 0, 3, -2.2f, 19.3f, -3.9f, 1, 1, 1);
        caixa(raiz, "silver2", 0, 6, 1.6f, 19.0f, -2.1f, 2, 1, 1);
        caixa(raiz, "silver3", 0, 9, -3.8f, 19.1f, 3.1f, 1, 1, 2);
        caixa(raiz, "silver4", 0, 13, -3.4f, 18.8f, 0.9f, 1, 1, 1);
        caixa(raiz, "silver5", 0, 16, 1.6f, 19.0f, -0.1f, 1, 1, 1);
        caixa(raiz, "silver6", 0, 19, -4.6f, 19.1f, -1.6f, 2, 1, 1);
        caixa(raiz, "silver7", 0, 22, -0.5f, 19.0f, 2.0f, 1, 1, 1);
        caixa(raiz, "silver8", 0, 25, -1.2f, 19.0f, -0.3f, 1, 1, 1);

        return LayerDefinition.create(malha, 64, 32);
    }

    /**
     * Uma caixa do original.
     *
     * <p>O {@code setRotationPoint} dele é o canto de onde a caixa cresce, e a caixa começa sempre em
     * zero — de modo que o canto é o lugar e o tamanho é tudo o que falta.
     */
    private static void caixa(PartDefinition raiz, String nome, int u, int v,
                              float x, float y, float z, int largo, int alto, int fundo) {
        raiz.addOrReplaceChild(nome, CubeListBuilder.create().texOffs(u, v)
                        .addBox(0.0f, 0.0f, 0.0f, largo, alto, fundo),
                PartPose.offset(x, y, z));
    }
}
