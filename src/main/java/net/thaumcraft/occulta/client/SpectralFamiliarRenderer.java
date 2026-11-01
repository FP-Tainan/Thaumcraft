package net.thaumcraft.occulta.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.thaumcraft.occulta.familiar.SpectralFamiliarEntity;

/**
 * O desenhista do <b>Familiar Espectral</b>: o {@code RenderFamiliar} do Witchery.
 *
 * <p>É o <b>porco do jogo</b> — o modelo e a pele, sem lhe tocar —, desenhado <b>translúcido</b> e
 * tingido: <b>azul</b> parado, <b>roxo</b> quando está à procura de alguma coisa. É assim que se sabe que
 * o pedido pegou, e é a única coisa no mod inteiro que diz isso.
 *
 * <p>Não há modelo novo nem pele nova, e é de propósito. O bicho tem de parecer <b>um porco que não está
 * bem ali</b>, e a maneira de o conseguir é não desenhar porco nenhum.
 */
@Environment(EnvType.CLIENT)
public class SpectralFamiliarRenderer extends MobRenderer<SpectralFamiliarEntity,
        SpectralFamiliarRenderer.Estado, PigModel> {
    /**
     * A pele do porco do jogo, que é a dele.
     *
     * <p>Em 2014 havia <b>uma</b> pele de porco, e chamava-se {@code pig.png}. Hoje há três — a de clima
     * temperado, a de frio e a de calor — e nenhuma se chama assim. A deste é a <b>temperada</b>, que é a
     * cor-de-rosa de sempre; pedir a velha dava o quadriculado preto e roxo de textura perdida, que
     * tingido de azul ainda parecia de propósito.
     */
    private static final Identifier PELE =
            Identifier.withDefaultNamespace("textures/entity/pig/pig_temperate.png");

    /** As duas cores do original, com a mesma transparência: {@code 0.51}. */
    public static final int PARADO = 0x828080FF;
    public static final int PROCURANDO = 0x82B34DFF;

    /** E o tamanho a que ele encolhe depois de domado. */
    public static final float DOMADO = 0.8f;

    /** O que o desenhista precisa de saber dele: se está à procura, e se é de alguém. */
    public static class Estado extends LivingEntityRenderState {
        public boolean procurando;
        public boolean domado;
    }

    public SpectralFamiliarRenderer(EntityRendererProvider.Context contexto) {
        super(contexto, new PigModel(contexto.bakeLayer(ModelLayers.PIG)), 0.0f);
    }

    @Override
    public Estado createRenderState() {
        return new Estado();
    }

    @Override
    public void extractRenderState(SpectralFamiliarEntity porco, Estado estado, float parcial) {
        super.extractRenderState(porco, estado, parcial);
        estado.procurando = porco.procura() >= 0;
        estado.domado = porco.isTame();
    }

    @Override
    public Identifier getTextureLocation(Estado estado) {
        return PELE;
    }

    /** Ele é sempre translúcido, e não só quando o jogo acha que devia ser. */
    @Override
    protected RenderType getRenderType(Estado estado, boolean corpo, boolean translúcido,
                                       boolean brilha) {
        return RenderTypes.entityTranslucent(PELE);
    }

    @Override
    protected int getModelTint(Estado estado) {
        return estado.procurando ? PROCURANDO : PARADO;
    }

    @Override
    protected void scale(Estado estado, com.mojang.blaze3d.vertex.PoseStack pose) {
        super.scale(estado, pose);
        if (estado.domado) pose.scale(DOMADO, DOMADO, DOMADO);
    }
}
