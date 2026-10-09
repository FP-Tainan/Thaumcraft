package net.thaumcraft.occulta.client;

import net.minecraft.client.model.monster.silverfish.SilverfishModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.louse.LouseEntity;

/**
 * O <b>Piolho Parasita</b> no mundo: o {@code ModelLouse} e o {@code RenderParasyticLouse} do Witchery.
 *
 * <p>O {@code ModelLouse} do original é o <b>boneco da lacrainha do jogo copiado caixa por caixa</b>, sem
 * uma linha mudada — as mesmas sete peças de corpo, as mesmas três de casca, as mesmas contas de
 * balanço. Aqui ele não se copia: usa-se o do jogo, com a <b>folha do piolho</b> por cima.
 *
 * <p>É a mesma decisão do Ent e do lobisomem, pelo avesso: lá a malha do mod é que vale, aqui é a do
 * jogo — porque o original a copiou de lá.
 */
public class LouseRenderer extends MobRenderer<LouseEntity, LivingEntityRenderState, SilverfishModel> {
    public static final Identifier FOLHA = Thaumcraft.id("textures/entity/louse.png");

    /** A sombra dele, que é a da lacrainha. */
    public static final float SOMBRA = 0.3f;

    public LouseRenderer(EntityRendererProvider.Context contexto) {
        super(contexto, new SilverfishModel(contexto.bakeLayer(ModelLayers.SILVERFISH)), SOMBRA);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState estado) {
        return FOLHA;
    }
}
