package net.thaumcraft.occulta.client;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.thaumcraft.occulta.StatueOfWorshipBlock;
import net.thaumcraft.occulta.StatueOfWorshipBlockEntity;
import net.thaumcraft.occulta.TaglockItem;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Estátua de Adoração</b> no mundo: o {@code RenderStatueOfWorship} do Witchery.
 *
 * <p>Ela é desenhada <b>duas vezes</b> com a mesma malha: primeiro com a <b>pele do dono</b>, puxada a
 * setenta por cento de luz para parecer pedra, e depois com uma <b>folha de pedra</b> translúcida por cima.
 * O que se vê é a cara de alguém <b>debaixo</b> de pedra, e não uma pedra com uma cara pintada — é uma
 * diferença pequena de desenho e é toda a graça da coisa.
 *
 * <p>E ela é um <b>boneco de criança</b>: o original liga o {@code isChild} do {@code ModelBiped}, que
 * desenha a cabeça a três quartos e o resto a metade, cada um deslocado para baixo. Uma estátua de cabeça
 * grande, que é o que a faz parecer ídolo e não enfeite.
 */
public class StatueOfWorshipRenderer implements BlockEntityRenderer<StatueOfWorshipBlockEntity,
        StatueOfWorshipRenderer.State> {
    /** O cinzento com que a pele é pintada, e o da pedra por cima: os do original. */
    public static final int CINZA = 0xFFB3B3B3;
    public static final int PEDRA_CINZA = 0xFFCCCCCC;

    /** As contas do boneco de criança, que são as do {@code ModelBiped}. */
    public static final float CABEÇA = 0.75f;
    public static final float CORPO = 0.5f;
    public static final float CABEÇA_DESCE = 1.0f;
    public static final float CORPO_DESCE = 1.5f;

    private final ModelPart pedra;
    private final ModelPart pele;

    public static class State extends BlockEntityRenderState {
        public Direction rumo = Direction.NORTH;
        public @Nullable TaglockItem.Taglock dono;
    }

    public StatueOfWorshipRenderer(BlockEntityRendererProvider.Context contexto) {
        this.pedra = contexto.bakeLayer(StatueOfWorshipModel.PEDRA_MALHA);
        this.pele = contexto.bakeLayer(StatueOfWorshipModel.PELE_MALHA);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(StatueOfWorshipBlockEntity estátua, State estado, float parcial,
                                   net.minecraft.world.phys.Vec3 câmara,
                                   @Nullable net.minecraft.client.renderer.feature.ModelFeatureRenderer
                                           .CrumblingOverlay quebrando) {
        BlockEntityRenderer.super.extractRenderState(estátua, estado, parcial, câmara, quebrando);
        estado.rumo = estátua.getBlockState().getValue(StatueOfWorshipBlock.FACING);
        estado.dono = estátua.dono();
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor,
                       CameraRenderState câmara) {
        desenha(pose, coletor, this.pele, this.pedra, estado.lightCoords, estado.rumo, estado.dono);
    }

    /**
     * Desenha a estátua, no mundo ou na mão.
     *
     * <p>O virar de cabeça para baixo é o de sempre — meia volta em Z e um bloco para baixo —, e a seguir
     * vem o <b>giro</b>, que no original sai dos números dois a cinco do bloco e aqui sai do rumo dele.
     */
    public static void desenha(PoseStack pose, SubmitNodeCollector coletor, ModelPart pele,
                               ModelPart pedra, int luz, Direction rumo,
                               @Nullable TaglockItem.Taglock dono) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, -1.0f, 0.0f);
        pose.mulPose(Axis.YP.rotationDegrees(rumo.toYRot() + 180.0f));

        boneco(pose, coletor, pele, RenderTypes.entityTranslucent(aPeleDe(dono)), luz, CINZA);
        boneco(pose, coletor, pedra,
                RenderTypes.entityTranslucent(StatueOfWorshipModel.PEDRA), luz, PEDRA_CINZA);

        pose.popPose();
    }

    /**
     * O boneco de criança do {@code ModelBiped}: a cabeça a três quartos e o resto a metade, cada um
     * descido o seu tanto.
     */
    private static void boneco(PoseStack pose, SubmitNodeCollector coletor, ModelPart raiz,
                               net.minecraft.client.renderer.rendertype.RenderType tipo, int luz,
                               int cor) {
        pose.pushPose();
        pose.scale(CABEÇA, CABEÇA, CABEÇA);
        pose.translate(0.0f, CABEÇA_DESCE, 0.0f);
        coletor.submitModelPart(raiz.getChild("head"), pose, tipo, luz, OverlayTexture.NO_OVERLAY,
                null, cor, null);
        pose.popPose();

        pose.pushPose();
        pose.scale(CORPO, CORPO, CORPO);
        pose.translate(0.0f, CORPO_DESCE, 0.0f);
        for (String qual : new String[]{"body", "right_arm", "left_arm", "right_leg", "left_leg",
                "headwear"}) {
            coletor.submitModelPart(raiz.getChild(qual), pose, tipo, luz, OverlayTexture.NO_OVERLAY,
                    null, cor, null);
        }
        pose.popPose();
    }

    /**
     * A pele do dono, ou a de ninguém.
     *
     * <p>Em 2014 bastava o <b>nome</b> para ir buscar uma pele; hoje é preciso o <b>número</b>, e o
     * servidor de peles não responde a quem joga sozinho sem rede. Não respondendo, fica a pele de sempre —
     * que é o que o original também fazia quando o download falhava.
     */
    public static Identifier aPeleDe(@Nullable TaglockItem.Taglock dono) {
        if (dono == null) return net.minecraft.client.resources.DefaultPlayerSkin.getDefaultTexture();
        var pele = Minecraft.getInstance().getSkinManager()
                .createLookup(new GameProfile(dono.owner(), dono.name()), false).get();
        return pele == null
                ? net.minecraft.client.resources.DefaultPlayerSkin.getDefaultTexture()
                : pele.body().texturePath();
    }
}
