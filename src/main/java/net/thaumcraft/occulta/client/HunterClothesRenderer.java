package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
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

/**
 * As roupas de caçador no corpo de quem as veste: o {@code ModelHunterClothes} e o {@code getArmorModel} do
 * {@code ItemHunterClothes}.
 *
 * <p><b>Elas não são armadura de folha.</b> O original não pinta uma folha por cima do corpo: desenha
 * <b>quatro caixas a mais</b> — o <b>chapéu de três andares com aba larga</b>, preso à cabeça, e o
 * <b>casaco</b>, preso ao tronco, que desce abaixo da cintura. É o chapéu que faz um caçador de bruxas ser
 * reconhecido de longe, e ele não cabe numa folha.
 *
 * <p>Por isso vão pelo mesmo caminho dos Abafadores: um modelo próprio, com as caixas do original número por
 * número, numa folha de <b>cento e vinte e oito por sessenta e quatro</b> — que é a do original, e é por isso
 * que ela parecia vazia quando posta como folha de armadura comum.
 *
 * <p><b>E são dois modelos, não um.</b> O original faz o do peito com {@code 0,4} de folga e o das pernas com
 * {@code 0,01}: o casaco tem de sobrar do corpo, e as calças têm de ficar coladas à perna. Fica igual.
 *
 * <p>O casaco é <b>comprido e de mangas</b>: a caixa a mais desce dez pontos abaixo do tronco, e é ela que
 * faz a silhueta — um chapéu de aba larga por cima de um casaco que vai até o joelho. É o que se reconhece de
 * longe, e é o que o Witchery desenhou.
 */
public final class HunterClothesRenderer implements ArmorRenderer {
    /** O do peito e da cabeça, com a folga do original. */
    public static final ModelLayerLocation PEITO =
            new ModelLayerLocation(Thaumcraft.id("hunter_clothes"), "chest");

    /** E o das pernas, colado. */
    public static final ModelLayerLocation PERNAS =
            new ModelLayerLocation(Thaumcraft.id("hunter_clothes"), "legs");

    private static final Identifier FOLHA = Thaumcraft.id("textures/models/hunter_clothes.png");
    private static final Identifier FOLHA_PERNAS = Thaumcraft.id("textures/models/hunter_clothes_legs.png");

    /** E as duas folhas de cima, que vão por cima da tinta. */
    private static final Identifier FOLHA_POR_CIMA =
            Thaumcraft.id("textures/models/hunter_clothes_overlay.png");
    private static final Identifier FOLHA_PERNAS_POR_CIMA =
            Thaumcraft.id("textures/models/hunter_clothes_legs_overlay.png");

    /** As folgas do original: o peito sobra, as pernas colam. */
    public static final float FOLGA_DO_PEITO = 0.4f;
    public static final float FOLGA_DAS_PERNAS = 0.01f;

    /** E a do chapéu, que é a mesma nos três andares menos na aba. */
    private static final float CHAPÉU = 0.52f;
    private static final float ABA = CHAPÉU - 0.2f;

    /** A folga do casaco, que é negativa: ele é mais estreito do que o corpo que o leva. */
    private static final float CASACO = -0.3f;

    private final Modelo doPeito;
    private final Modelo dasPernas;

    public HunterClothesRenderer(EntityRendererProvider.Context context) {
        this.doPeito = new Modelo(context.bakeLayer(PEITO));
        this.dasPernas = new Modelo(context.bakeLayer(PERNAS));
    }

    /** O modelo do peito e da cabeça. */
    public static LayerDefinition peito() {
        return constrói(FOLGA_DO_PEITO);
    }

    /** E o das pernas. */
    public static LayerDefinition pernas() {
        return constrói(FOLGA_DAS_PERNAS);
    }

    /**
     * As caixas do {@code ModelHunterClothes}, número por número.
     *
     * <p>O chapéu é uma <b>escada</b>: a aba está presa à cabeça, o meio está preso à aba, e o topo ao meio —
     * e por isso basta virar a cabeça para o chapéu inteiro virar com ela.
     */
    private static LayerDefinition constrói(float folga) {
        MeshDefinition malha = HumanoidModel.createMesh(new CubeDeformation(folga), 0.0f);
        PartDefinition raiz = malha.getRoot();

        PartDefinition cabeça = raiz.getChild("head");
        PartDefinition aba = cabeça.addOrReplaceChild("hat_brim",
                CubeListBuilder.create().texOffs(0, 50)
                        .addBox(-6.5f, 0.0f, -6.5f, 13.0f, 1.0f, 13.0f, new CubeDeformation(ABA)),
                PartPose.offset(0.0f, -6.0f, 0.0f));
        PartDefinition meio = aba.addOrReplaceChild("hat_mid",
                CubeListBuilder.create().texOffs(40, 52)
                        .addBox(-4.0f, 0.0f, -4.0f, 8.0f, 2.0f, 8.0f, new CubeDeformation(CHAPÉU)),
                PartPose.offset(0.0f, -2.0f, 0.0f));
        meio.addOrReplaceChild("hat_top",
                CubeListBuilder.create().texOffs(12, 41)
                        .addBox(-3.5f, 0.0f, -3.5f, 7.0f, 2.0f, 7.0f, new CubeDeformation(CHAPÉU)),
                PartPose.offset(0.0f, -2.0f, 0.0f));

        raiz.getChild("body").addOrReplaceChild("coat",
                CubeListBuilder.create().texOffs(41, 33)
                        .addBox(-5.5f, 0.0f, -3.0f, 11.0f, 10.0f, 6.0f, new CubeDeformation(CASACO)),
                PartPose.offset(0.0f, 12.0f, 0.0f));

        return LayerDefinition.create(malha, 128, 64);
    }

    /**
     * O que se vê de cada casa: o {@code getArmorModel}, que acende e apaga as partes uma a uma.
     *
     * <p>É o mesmo desenho três vezes, com partes diferentes acesas — e é por isso que o chapéu e o casaco
     * nunca aparecem juntos por engano.
     *
     * <p>E são <b>duas passagens</b>, como no couro do jogo e como no original: a primeira leva a <b>tinta</b>
     * de quem a pintou; a segunda é a folha de cima, sem cor nenhuma, e é ela que põe os botões, as fivelas e
     * a aba do chapéu por cima da cor.
     */
    @Override
    public void render(PoseStack pose, SubmitNodeCollector coletor, ItemStack peça, HumanoidRenderState estado,
                       EquipmentSlot casa, int luz, HumanoidModel<HumanoidRenderState> contexto) {
        Modelo modelo = switch (casa) {
            case HEAD, CHEST -> this.doPeito;
            case LEGS, FEET -> this.dasPernas;
            default -> null;
        };
        if (modelo == null) return;

        modelo.acende(casa);
        boolean emCima = casa == EquipmentSlot.HEAD || casa == EquipmentSlot.CHEST;
        Identifier folha = emCima ? FOLHA : FOLHA_PERNAS;
        Identifier porCima = emCima ? FOLHA_POR_CIMA : FOLHA_PERNAS_POR_CIMA;

        coletor.submitModel(modelo, estado, pose, RenderTypes.armorCutoutNoCull(folha), luz,
                OverlayTexture.NO_OVERLAY, tinta(peça), null, 0, null);
        coletor.submitModel(modelo, estado, pose, RenderTypes.armorCutoutNoCull(porCima), luz,
                OverlayTexture.NO_OVERLAY, -1, null, 0, null);
    }

    /** A tinta desta peça: a de quem a pintou, ou a de fábrica. */
    private static int tinta(ItemStack peça) {
        var pintada = peça.get(net.minecraft.core.component.DataComponents.DYED_COLOR);
        if (pintada != null) return 0xFF000000 | pintada.rgb();
        if (peça.getItem() instanceof net.thaumcraft.occulta.hunter.HunterClothesItem roupa) {
            return 0xFF000000 | roupa.corDeFábrica();
        }
        return -1;
    }
    /** O bípede das roupas, que acende só o que a casa pede. */
    static class Modelo extends HumanoidModel<HumanoidRenderState> {
        private EquipmentSlot casa = EquipmentSlot.CHEST;

        Modelo(ModelPart raiz) {
            super(raiz);
        }

        void acende(EquipmentSlot casa) {
            this.casa = casa;
        }

        @Override
        public void setupAnim(HumanoidRenderState estado) {
            super.setupAnim(estado);
            boolean cabeça = this.casa == EquipmentSlot.HEAD;
            boolean peito = this.casa == EquipmentSlot.CHEST;
            boolean pernas = this.casa == EquipmentSlot.LEGS || this.casa == EquipmentSlot.FEET;

            this.head.visible = cabeça;
            this.hat.visible = cabeça;
            this.body.visible = peito;
            this.rightArm.visible = peito;
            this.leftArm.visible = peito;
            this.rightLeg.visible = pernas;
            this.leftLeg.visible = pernas;
        }
    }
}
