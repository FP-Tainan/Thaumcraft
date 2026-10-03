package net.thaumcraft.occulta.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ambient.BatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.BatRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * O corpo que um jogador em <b>forma de morcego</b> veste: o {@code TransformBat} do Witchery.
 *
 * <p>E ele não é <b>um</b> morcego. São <b>três</b>.
 *
 * <h2>Os três morcegos, que é a melhor ideia do desenho do mod</h2>
 *
 * <p>O original desenha o morcego de mentira três vezes: um no lugar do jogador, e dois atrás dele, a três
 * quartos de bloco de distância, seis décimos mais baixos e a oito décimos do tamanho — com as asas
 * <b>fora de compasso</b>, duas e sete batidas à frente. O que se vê não é um bicho: é uma <b>nuvenzinha</b>
 * de morcegos que se move junta, e é assim que um vampiro atravessa um vale no Witchery.
 *
 * <p>Nenhuma linha do mod diz isto. Quem vira morcego descobre que virou <b>vários</b>.
 *
 * <h2>E um erro do autor que fica</h2>
 *
 * <p>Os dois morcegos de trás saem de uma volta do vetor do olhar, e o original pede essa volta em
 * <b>graus</b> a um método que a conta em <b>radianos</b>: ele escreve {@code 90} e {@code -180} onde queria
 * noventa e cento e oitenta graus. Noventa radianos, descontadas as voltas inteiras, dão cento e dezesseis
 * graus e meio — e o segundo pedido, de menos cento e oitenta radianos, devolve o vetor ao outro lado.
 *
 * <p>O resultado é <b>simétrico por acaso</b>: os dois ficam a cento e dezesseis graus e meio para cada lado
 * do olhar, isto é, <b>atrás</b> dele, um de cada banda. É o que se vê no jogo, e por isso fica como está —
 * os números do original, e não os que ele queria.
 */
public final class VampireBatModels {
    /** A pele do morcego, que é a do jogo. */
    public static final Identifier PELE =
            Identifier.withDefaultNamespace("textures/entity/bat/bat.png");

    /** Quantos são, e qual deles é o do meio. */
    public static final int QUANTOS = 3;

    /** Os dois de trás: a que distância, quanto mais baixos, e de que tamanho. */
    public static final double ATRÁS = 0.75;
    public static final double ABAIXO = -0.6;
    public static final float TAMANHO = 0.8f;

    /** E quantas batidas à frente vão as asas de cada um, para não baterem juntas. */
    public static final int[] ADIANTADO = {0, 2, 7};

    /**
     * A volta do vetor do olhar, <b>em radianos</b>, tal como o original a pede: noventa, e depois menos
     * cento e oitenta sobre o resultado.
     */
    public static final float VOLTA = 90.0f;
    public static final float VOLTA_DE_VOLTA = -180.0f;

    @Nullable
    private static BatModel modelo;

    /** Um estado por morcego, porque as asas dos três não batem no mesmo compasso. */
    private static final BatRenderState[] ESTADOS = {
            new BatRenderState(), new BatRenderState(), new BatRenderState(),
    };

    private VampireBatModels() {
    }

    /** O modelo do morcego, assado na primeira vez que alguém o pedir. */
    public static BatModel modelo() {
        if (modelo == null) {
            modelo = new BatModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BAT));
        }
        return modelo;
    }

    /**
     * O estado do morcego número tal, enchido com o que o do jogador tem.
     *
     * <p>Os campos que se copiam são os de <b>vivo</b>. Os que só um morcego tem ficam no que importa: ele
     * <b>não está pendurado</b>, porque um jogador em forma de morcego está sempre a voar.
     */
    public static BatRenderState como(LivingEntityRenderState doJogador, int qual) {
        BatRenderState morcego = ESTADOS[qual];

        morcego.ageInTicks = doJogador.ageInTicks;
        morcego.walkAnimationPos = doJogador.walkAnimationPos;
        morcego.walkAnimationSpeed = doJogador.walkAnimationSpeed;
        morcego.bodyRot = doJogador.bodyRot;
        morcego.yRot = doJogador.yRot;
        morcego.xRot = doJogador.xRot;
        morcego.isFullyFrozen = doJogador.isFullyFrozen;
        morcego.isBaby = false;
        morcego.isInWater = doJogador.isInWater;
        morcego.deathTime = doJogador.deathTime;
        morcego.pose = doJogador.pose;
        morcego.scale = doJogador.scale;
        morcego.ageScale = 1.0f;
        morcego.lightCoords = doJogador.lightCoords;
        morcego.x = doJogador.x;
        morcego.y = doJogador.y;
        morcego.z = doJogador.z;

        // um jogador em forma de morcego nunca está pendurado
        morcego.isResting = false;
        morcego.restAnimationState.stop();
        morcego.flyAnimationState.animateWhen(true,
                (int) doJogador.ageInTicks - ADIANTADO[qual]);
        return morcego;
    }

    /**
     * De onde sai cada um dos três: o vetor do olhar com a volta do original por cima.
     *
     * <p>O do meio fica onde o jogador está. Os outros dois saem do vetor virado, e é aí que os noventa
     * radianos entram.
     */
    public static Vec3 onde(LivingEntityRenderState doJogador, int qual) {
        if (qual == 0) return Vec3.ZERO;

        Vec3 olhar = olhar(doJogador);
        Vec3 virado = voltaEmY(olhar, VOLTA);
        if (qual == 2) virado = voltaEmY(virado, VOLTA_DE_VOLTA);
        return new Vec3(virado.x * ATRÁS, ABAIXO, virado.z * ATRÁS);
    }

    /** O vetor do olhar, tirado dos dois ângulos do estado como o jogo o tira do bicho. */
    private static Vec3 olhar(LivingEntityRenderState doJogador) {
        float cima = -doJogador.xRot * ((float) Math.PI / 180.0f);
        float lado = -doJogador.yRot * ((float) Math.PI / 180.0f);
        float cosCima = net.minecraft.util.Mth.cos(cima);
        float senCima = net.minecraft.util.Mth.sin(cima);
        float cosLado = net.minecraft.util.Mth.cos(lado);
        float senLado = net.minecraft.util.Mth.sin(lado);
        return new Vec3(senLado * cosCima, senCima, cosLado * cosCima);
    }

    /**
     * A volta em Y do {@code Vec3.rotateAroundY} de então, com o ângulo <b>em radianos</b> e a mesma conta.
     *
     * <p>É a conta exata do original, com o erro de unidade dele dentro: quem passar noventa recebe cento e
     * dezesseis graus e meio, e é isso que o jogo mostra.
     */
    private static Vec3 voltaEmY(Vec3 quem, float quanto) {
        float cos = net.minecraft.util.Mth.cos(quanto);
        float sen = net.minecraft.util.Mth.sin(quanto);
        return new Vec3(quem.x * cos + quem.z * sen, quem.y, quem.z * cos - quem.x * sen);
    }
}
