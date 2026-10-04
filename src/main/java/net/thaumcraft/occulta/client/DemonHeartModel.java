package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.thaumcraft.Thaumcraft;

/**
 * O <b>Coração de Demônio</b>, caixa por caixa: o {@code ModelDemonHeart} do Witchery.
 *
 * <p>Dez peças numa chapa de trinta e dois: <b>quatro</b> fazem o músculo — um bloco de cinco por oito, um de
 * três por oito mais fundo ao lado, uma lasca de um pixel na ponta e uma tampa de dois — e <b>seis</b> são
 * cano: um cano grosso deitado de través e <b>cinco canos finos</b> de três de comprido, cada um torcido num
 * ângulo diferente, saindo de cima como veias cortadas.
 *
 * <p>Nenhuma delas é espelhada: o original liga o espelho depois de criar as caixas, e depois de criada a
 * caixa o espelho não faz mais nada.
 *
 * <h2>E ele bate</h2>
 *
 * <p>O músculo — e só ele — <b>incha e desincha</b> numa onda de seno, entre 1,11 e 1,20 do tamanho dele,
 * com período de <b>vinte e cinco batidas</b>. Os canos ficam parados.
 *
 * <p>Vinte e cinco batidas é <b>exatamente</b> o intervalo do som de coração que a
 * {@linkplain net.thaumcraft.occulta.demon.DemonHeartBlockEntity alma do bloco} toca. O inchaço e a batida
 * são a mesma batida, e é por isso que o bloco funciona: ouve-se o coração e, olhando, vê-se o coração fazer
 * o barulho. Sem o inchaço, o som vem de um enfeite parado.
 */
public class DemonHeartModel {
    public static final ModelLayerLocation CORAÇÃO =
            new ModelLayerLocation(Thaumcraft.id("demon_heart"), "main");

    /** A onda do inchaço: 0,165 × (7 + ¼·sen(ω·t)), com ω dando a volta em vinte e cinco batidas. */
    public static final double BASE = 0.165;
    public static final double CORPO = 7.0;
    public static final double INCHA = 0.25;
    public static final double VOLTA = 0.25132741228718347;

    private DemonHeartModel() {
    }

    /** Trinta e dois por trinta e dois de textura. */
    public static LayerDefinition coração() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        // o músculo, que é o que bate
        raiz.addOrReplaceChild("shape1", CubeListBuilder.create()
                        .texOffs(14, 20).addBox(0.0f, 0.0f, 0.0f, 5.0f, 8.0f, 4.0f),
                PartPose.offset(-3.0f, 14.0f, 0.0f));
        raiz.addOrReplaceChild("shape2", CubeListBuilder.create()
                        .texOffs(0, 7).addBox(0.0f, 0.0f, 0.0f, 3.0f, 8.0f, 6.0f),
                PartPose.offset(-4.0f, 15.0f, -1.0f));
        raiz.addOrReplaceChild("shape3", CubeListBuilder.create()
                        .texOffs(13, 0).addBox(0.0f, 0.0f, 0.0f, 1.0f, 6.0f, 4.0f),
                PartPose.offset(-5.0f, 16.0f, 0.0f));
        raiz.addOrReplaceChild("shape4", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 2.0f, 1.0f, 2.0f),
                PartPose.offset(-3.0f, 13.0f, 1.0f));

        // o cano grosso, deitado de través
        raiz.addOrReplaceChild("bigtube1", CubeListBuilder.create()
                        .texOffs(3, 3).addBox(0.0f, 0.0f, 0.0f, 3.0f, 2.0f, 2.0f),
                PartPose.offsetAndRotation(-2.0f, 15.0f, -1.0f, 0.0f, 0.3717861f, 0.2230717f));

        // e os cinco canos finos, cada um torcido para o seu lado
        cano(raiz, "tube1", -3.0f, 14.0f, 1.0f, 0.4089647f, 0.6320364f, 0.0f);
        cano(raiz, "tube2", -2.0f, 14.0f, 1.0f, -0.2974289f, -0.2230717f, -0.3346075f);
        cano(raiz, "tube3", 1.0f, 13.0f, -0.8f, -0.0743572f, 0.1487144f, -0.2602503f);
        cano(raiz, "tube4", 0.0f, 15.0f, 0.0f, 0.2602503f, 0.0f, 0.4089647f);
        cano(raiz, "tube5", 0.0f, 14.0f, 1.0f, -0.2602503f, 0.0f, 0.0f);

        return LayerDefinition.create(malha, 32, 32);
    }

    private static void cano(PartDefinition raiz, String nome, float x, float y, float z,
                             float xRot, float yRot, float zRot) {
        raiz.addOrReplaceChild(nome, CubeListBuilder.create()
                        .texOffs(19, 11).addBox(0.0f, -3.0f, 1.0f, 1.0f, 3.0f, 1.0f),
                PartPose.offsetAndRotation(x, y, z, xRot, yRot, zRot));
    }

    /** O tamanho do músculo nesta batida. */
    public static float inchaço(long batidas) {
        return (float) (BASE * (CORPO + INCHA * Math.sin(VOLTA * batidas)));
    }
}
