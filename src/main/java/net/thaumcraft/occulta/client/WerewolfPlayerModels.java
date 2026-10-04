package net.thaumcraft.occulta.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.animal.wolf.AdultWolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;

/**
 * Os dois corpos que um jogador lobisomem veste: o lobo e o lobisomem.
 *
 * <p>No original isto é um <b>bicho de mentira</b> — um {@code EntityWolf} guardado à parte, com a posição e
 * o passo copiados do jogador a cada quadro, desenhado no lugar dele. Era o jeito de 2014, e era o único.
 *
 * <p>Hoje o jogo separa o <b>que se desenha</b> do <b>que existe</b>: o desenhista recebe um <i>estado</i>, e
 * o estado é só um punhado de números. Por isso aqui não há bicho nenhum — há os dois modelos, assados uma
 * vez, e o estado do próprio jogador entregue a eles.
 *
 * <p>Para o <b>lobisomem</b> o estado serve tal como vem, porque o modelo dele já pede um estado de vivo
 * qualquer. Para o <b>lobo</b> é preciso um estado de lobo, e ele se enche com o que o do jogador tem — o
 * passo, o rumo, a idade e o resto — e com os números que só um lobo tem postos em repouso: rabo a meia
 * altura, sem raiva, sem molha e sem coleira.
 */
public final class WerewolfPlayerModels {
    /** A pele do lobisomem, que é a mesma do bicho. */
    public static final Identifier PELE_LOBISOMEM = Thaumcraft.id("textures/entity/wolfman.png");

    /** E a do lobo, que é a do jogo. */
    public static final Identifier PELE_LOBO =
            Identifier.withDefaultNamespace("textures/entity/wolf/wolf.png");

    /**
     * O tamanho a que o lobo se desenha.
     *
     * <p>É <b>um</b>, que é o natural do modelo: o original desenha um {@code EntityWolf} de mentira, e um
     * lobo é do tamanho de um lobo. Quem encolhe é a <b>caixa</b> do jogador, e isso está no
     * {@link net.thaumcraft.mixin.PlayerWerewolfSizeMixin}.
     */
    public static final float TAMANHO_DO_LOBO = 1.0f;

    /** O rabo de um lobo em paz, em radianos: o mesmo do bicho sossegado. */
    private static final float RABO_PARADO = 0.62831855f;

    @Nullable
    private static WerewolfModel lobisomem;

    @Nullable
    private static AdultWolfModel lobo;

    /** O estado de lobo, reaproveitado: ele se enche de novo a cada quadro. */
    private static final WolfRenderState DO_LOBO = new WolfRenderState();

    private WerewolfPlayerModels() {
    }

    /** O modelo do lobisomem, assado na primeira vez que alguém o pedir. */
    public static WerewolfModel lobisomem() {
        if (lobisomem == null) {
            lobisomem = new WerewolfModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(WolfmanModel.WOLFMAN));
        }
        return lobisomem;
    }

    /** E o do lobo. */
    public static AdultWolfModel lobo() {
        if (lobo == null) {
            lobo = new AdultWolfModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.WOLF));
        }
        return lobo;
    }

    /**
     * O estado de lobo enchido com o que o do jogador tem.
     *
     * <p>Os campos que se copiam são os de <b>vivo</b> — o que um lobo e uma pessoa têm em comum. Os que só
     * um lobo tem ficam em repouso, porque um jogador não os tem: ele não se senta, não sacode a água e não
     * traz coleira.
     */
    public static WolfRenderState comoLobo(LivingEntityRenderState doJogador) {
        WolfRenderState lobo = DO_LOBO;

        lobo.ageInTicks = doJogador.ageInTicks;
        lobo.walkAnimationPos = doJogador.walkAnimationPos;
        lobo.walkAnimationSpeed = doJogador.walkAnimationSpeed;
        lobo.bodyRot = doJogador.bodyRot;
        lobo.yRot = doJogador.yRot;
        lobo.xRot = doJogador.xRot;
        lobo.isFullyFrozen = doJogador.isFullyFrozen;
        lobo.isBaby = doJogador.isBaby;
        lobo.isInWater = doJogador.isInWater;
        lobo.deathTime = doJogador.deathTime;
        lobo.pose = doJogador.pose;
        lobo.scale = doJogador.scale;
        lobo.ageScale = doJogador.ageScale;
        lobo.lightCoords = doJogador.lightCoords;
        lobo.x = doJogador.x;
        lobo.y = doJogador.y;
        lobo.z = doJogador.z;

        // o que só um lobo tem, em repouso
        lobo.isAngry = false;
        lobo.isSitting = false;
        lobo.tailAngle = RABO_PARADO;
        lobo.headRollAngle = 0.0f;
        lobo.shakeAnim = 0.0f;
        lobo.wetShade = 1.0f;
        lobo.collarColor = net.minecraft.world.item.DyeColor.RED;
        lobo.bodyArmorItem = net.minecraft.world.item.ItemStack.EMPTY;
        lobo.texture = PELE_LOBO;
        return lobo;
    }
}
