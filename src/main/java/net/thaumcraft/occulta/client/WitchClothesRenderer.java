package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.clothes.WitchClothesItem;

/**
 * As <b>roupas de bruxa</b> no corpo: o {@code ModelWitchesClothes} do Witchery.
 *
 * <p>Elas são o boneco de gente <b>inchado</b> — como toda armadura de 2014 — com peças penduradas nele:
 *
 * <ul>
 *   <li>no capacete, um <b>chapéu</b> de quatro caixas empilhadas: uma aba larga de catorze, uma gola de
 *       dez, um corpo de seis e uma ponta de dois. É o chapéu de bruxa do desenho animado, e é
 *       exatamente isso que o faz funcionar;</li>
 *   <li>no peito, o corpo inchado por {@value #INCHA_PEITO} com uma <b>saia</b> por baixo dele, que é a
 *       parte do manto que cai sobre as pernas;</li>
 *   <li>e, só no de <b>Necromante</b>, duas <b>ombreiras</b> penduradas nos braços, um bocadinho mais
 *       largas que o resto.</li>
 * </ul>
 *
 * <h2>Duas escalas, e não uma</h2>
 *
 * <p>O original monta <b>dois bonecos</b>: um a {@value #INCHA_PEITO} e outro a {@value #INCHA_CABEÇA}, e
 * escolhe entre eles pela <b>casa</b> da peça — a cabeça e as pernas levam o magro, o peito e os pés o
 * gordo. É de 2014 e não faz sentido nenhum, mas é o que dá o volume que ele dá, e por isso fica.
 *
 * <h2>O chapéu da Baba Yaga</h2>
 *
 * <p>Ele não é o chapéu de bruxa pintado de outra cor: são <b>quatro caixas encaixadas umas nas
 * outras</b>, cada uma menor e mais torta que a anterior. O que sai é um cone <b>amassado</b>, que
 * se dobra sobre si próprio — e é por isso que ele é o único que não se tinge. Um chapéu assim é de
 * alguém.
 *
 * <h2>Uma folha, e não duas</h2>
 *
 * <p><b>Diferença.</b> O original tem duas folhas, como o couro do jogo: uma que leva tinta e uma por
 * cima que não leva. A segunda, a {@code witchclothes_overlay.png}, está <b>vazia</b> — sessenta e quatro
 * por sessenta e quatro de nada. Aqui desenha-se uma vez só, com a folha tingida, porque desenhar o vazio
 * duas vezes não dá nada a ninguém.
 */
public class WitchClothesRenderer implements ArmorRenderer {
    public static final ModelLayerLocation CABEÇA =
            new ModelLayerLocation(Thaumcraft.id("witch_clothes_head"), "main");
    public static final ModelLayerLocation PEITO =
            new ModelLayerLocation(Thaumcraft.id("witch_clothes_chest"), "main");

    public static final Identifier FOLHA = Thaumcraft.id("textures/entity/witch_clothes.png");

    /** As duas escalas do original: o boneco gordo e o magro. */
    public static final float INCHA_PEITO = 0.61f;
    public static final float INCHA_CABEÇA = 0.45f;

    private final Boneco cabeça;
    private final Boneco peito;

    public WitchClothesRenderer(EntityRendererProvider.Context contexto) {
        this.cabeça = new Boneco(contexto.bakeLayer(CABEÇA));
        this.peito = new Boneco(contexto.bakeLayer(PEITO));
    }

    @Override
    public void render(PoseStack pose, SubmitNodeCollector coletor, ItemStack peça,
                       HumanoidRenderState estado, EquipmentSlot casa, int luz,
                       HumanoidModel<HumanoidRenderState> doCorpo) {
        if (!(peça.getItem() instanceof WitchClothesItem roupa)) return;

        Boneco boneco = casa == EquipmentSlot.HEAD ? this.cabeça : this.peito;
        boneco.casa = casa;
        boneco.baba = peça.is(OccultaItems.BABAS_HAT);
        boneco.ombreiras = roupa.necro();

        int cor = cor(peça, roupa);
        coletor.submitModel(boneco, estado, pose, RenderTypes.armorCutoutNoCull(FOLHA), luz,
                OverlayTexture.NO_OVERLAY, cor, null, 0, null);
    }

    /**
     * A cor desta peça: a que lhe deram, ou a de fábrica.
     *
     * <p>E a de fábrica vale <b>sempre</b> nas que não se tingem, que é a conta do {@code getColor} do
     * original: as Chinelas de Rubi devolvem o vermelho delas mesmo que alguém lhes ponha tinta, e o
     * Chapéu da Baba fica no couro cru do jogo.
     */
    private static int cor(ItemStack peça, WitchClothesItem roupa) {
        var tinta = roupa.dyeable()
                ? peça.get(net.minecraft.core.component.DataComponents.DYED_COLOR) : null;
        int rgb = tinta == null ? roupa.corDeFábrica() : tinta.rgb();
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    // ------------------------------------------------------------------ o boneco

    /** O boneco de uma peça: mostra o que é dela, e troca os dois chapéus. */
    public static class Boneco extends HumanoidModel<HumanoidRenderState> {
        EquipmentSlot casa = EquipmentSlot.CHEST;
        boolean baba;
        boolean ombreiras;

        public Boneco(net.minecraft.client.model.geom.ModelPart raiz) {
            super(raiz);
        }

        private void mostra(net.minecraft.client.model.geom.ModelPart onde, String qual, boolean sim) {
            if (onde.hasChild(qual)) onde.getChild(qual).visible = sim;
        }

        /**
         * O que cada casa mostra, pela conta do original — que é a de três casas e não de quatro:
         * a <b>cabeça</b> mostra a cabeça e o chapéu; o <b>peito</b> mostra o corpo e os braços; e os
         * <b>pés</b> mostram as <b>pernas</b>, no mesmo boneco inchado do peito.
         */
        @Override
        public void setupAnim(HumanoidRenderState estado) {
            super.setupAnim(estado);
            boolean naCabeça = this.casa == EquipmentSlot.HEAD;
            boolean nosPés = this.casa == EquipmentSlot.FEET;
            this.head.visible = naCabeça;
            this.hat.visible = false;
            this.body.visible = !naCabeça && !nosPés;
            this.rightArm.visible = this.leftArm.visible = !naCabeça && !nosPés;
            this.rightLeg.visible = this.leftLeg.visible = nosPés;

            this.mostra(this.head, "hat", naCabeça && !this.baba);
            this.mostra(this.head, "baba", naCabeça && this.baba);
            this.mostra(this.rightArm, "shoulder", !naCabeça && !nosPés && this.ombreiras);
            this.mostra(this.leftArm, "shoulder", !naCabeça && !nosPés && this.ombreiras);
        }
    }

    // ------------------------------------------------------------------ as duas camadas

    /** A do <b>capacete</b>: o boneco magro, com os dois chapéus na cabeça. */
    public static LayerDefinition cabeça() {
        MeshDefinition malha = HumanoidModel.createMesh(new CubeDeformation(INCHA_CABEÇA), 0.0f);
        PartDefinition cabeça = malha.getRoot().getChild("head");

        cabeça.addOrReplaceChild("hat", CubeListBuilder.create()
                .texOffs(0, 49).addBox(-7.0f, -7.0f, -7.0f, 14.0f, 1.0f, 14.0f)
                .texOffs(0, 36).addBox(-5.0f, -9.0f, -5.0f, 10.0f, 2.0f, 10.0f)
                .texOffs(31, 34).addBox(-3.0f, -14.0f, -3.0f, 6.0f, 5.0f, 6.0f)
                .texOffs(50, 34).addBox(-1.0f, -17.0f, -1.0f, 2.0f, 3.0f, 2.0f), PartPose.ZERO);

        PartDefinition baba = cabeça.addOrReplaceChild("baba", CubeListBuilder.create()
                        .texOffs(72, 48).addBox(0.0f, 0.0f, 0.0f, 14.0f, 2.0f, 14.0f,
                                new CubeDeformation(0.52f)),
                PartPose.offset(-7.0f, -8.0f, -7.0f));
        PartDefinition um = baba.addOrReplaceChild("baba1", CubeListBuilder.create()
                        .texOffs(83, 29).addBox(0.0f, 0.0f, 0.0f, 7.0f, 4.0f, 7.0f,
                                new CubeDeformation(0.4f)),
                PartPose.offsetAndRotation(3.75f, -4.0f, 4.0f, -0.05235988f, 0.0f, 0.02617994f));
        PartDefinition dois = um.addOrReplaceChild("baba2", CubeListBuilder.create()
                        .texOffs(83, 40).addBox(0.0f, 0.0f, 0.0f, 4.0f, 4.0f, 4.0f),
                PartPose.offsetAndRotation(1.75f, -4.0f, 2.0f, -0.10471976f, 0.0f, 0.05235988f));
        dois.addOrReplaceChild("baba3", CubeListBuilder.create()
                        .texOffs(81, 48).addBox(0.0f, 0.0f, 0.0f, 1.0f, 2.0f, 1.0f,
                                new CubeDeformation(0.25f)),
                PartPose.offsetAndRotation(1.75f, -2.0f, 2.0f, (float) (-Math.PI / 15), 0.0f,
                        0.10471976f));

        return LayerDefinition.create(malha, 128, 64);
    }

    /** E a do <b>peito</b>: o boneco gordo, com a saia no corpo e as ombreiras nos braços. */
    public static LayerDefinition peito() {
        MeshDefinition malha = HumanoidModel.createMesh(new CubeDeformation(INCHA_PEITO), 0.0f);
        PartDefinition raiz = malha.getRoot();

        raiz.getChild("body").addOrReplaceChild("skirt", CubeListBuilder.create()
                        .texOffs(43, 46).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 6.0f, 4.0f,
                                new CubeDeformation(INCHA_PEITO)),
                PartPose.offset(0.0f, 12.0f, 0.0f));

        /*
         * As ombreiras do Necromante vão nos <b>braços</b>, e com a escala do manto <b>mais um décimo</b>:
         * ficam um bocadinho mais largas que ele, que é o que faz uma ombreira parecer uma ombreira.
         */
        raiz.getChild("right_arm").addOrReplaceChild("shoulder", CubeListBuilder.create()
                        .texOffs(61, 32).addBox(0.0f, 0.0f, 0.0f, 5.0f, 1.0f, 6.0f,
                                new CubeDeformation(INCHA_PEITO + 0.1f)),
                PartPose.offset(-4.0f, -2.0f, -3.0f));
        raiz.getChild("left_arm").addOrReplaceChild("shoulder", CubeListBuilder.create()
                        .texOffs(61, 39).addBox(0.0f, 0.0f, 0.0f, 5.0f, 1.0f, 6.0f,
                                new CubeDeformation(INCHA_PEITO + 0.1f)),
                PartPose.offset(0.0f, -2.0f, -3.0f));

        return LayerDefinition.create(malha, 128, 64);
    }
}
