package net.thaumcraft.occulta.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.ghost.BansheeEntity;
import net.thaumcraft.occulta.ghost.PoltergeistEntity;
import net.thaumcraft.occulta.ghost.SpectreEntity;
import net.thaumcraft.occulta.ghost.SummonedUndeadEntity;

/**
 * Os três <b>fantasmas</b> no mundo: os {@code ModelSpectre}, {@code ModelPoltergeist} e os desenhistas
 * deles do Witchery.
 *
 * <p>Os três são <b>translúcidos</b>, e cada um na sua medida — que é o que diz, antes de qualquer outra
 * coisa, o que cada um é:
 *
 * <ul>
 *   <li>o <b>Espectro</b> a <b>quinze centésimos</b> enquanto está apagado, que é como ele nasce, e a seis
 *       décimos depois: um vulto que mal se vê vindo;</li>
 *   <li>a <b>Banshee</b> a <b>sete décimos</b>: ela não se esconde, ela grita;</li>
 *   <li>o <b>Poltergeist</b> a <b>quatro décimos</b> — e além disso <b>invisível</b> por poção, de modo que
 *       só se vê o que ele faz, nunca ele.</li>
 * </ul>
 *
 * <h2>Duas malhas para três bichos</h2>
 *
 * <p>O Espectro e a Banshee partilham a malha do {@code ModelSpectre}, e a diferença entre eles é uma
 * bandeira: com ela, os braços ficam <b>estendidos para a frente</b> — é o Espectro vindo buscar alguém;
 * sem ela, caídos — é a Banshee, que não precisa de mãos.
 *
 * <p>E a <b>boca</b> é uma caixa sem fundura presa à cabeça, que só aparece quando o bicho está
 * <b>gritando</b>. Na Banshee ela abre e os braços se levantam ao mesmo tempo.
 */
public final class GhostRenderers {
    public static final ModelLayerLocation ESPECTRO =
            new ModelLayerLocation(Thaumcraft.id("spectre"), "main");
    public static final ModelLayerLocation POLTERGEIST =
            new ModelLayerLocation(Thaumcraft.id("poltergeist"), "main");

    public static final Identifier FOLHA_ESPECTRO = Thaumcraft.id("textures/entity/spectre.png");
    public static final Identifier FOLHA_BANSHEE = Thaumcraft.id("textures/entity/banshee.png");
    public static final Identifier FOLHA_POLTERGEIST = Thaumcraft.id("textures/entity/poltergeist.png");

    /** As transparências do original, como cor de trinta e dois bits. */
    public static final int APAGADO = 0x26FFFFFF;
    public static final int ESPECTRO_COR = 0x99FFFFFF;
    public static final int BANSHEE_COR = 0xB3FFFFFF;
    public static final int POLTERGEIST_COR = 0x66FFFFFF;

    /** O braço estendido do Espectro, e o caído da Banshee. */
    private static final float ESTENDIDO = -1.5f;
    private static final float CAÍDO = -0.2f;

    /** E o balanço de sempre, que o original soma a tudo o que tem braço. */
    private static final float BALANÇO = 0.067f;
    private static final float QUANTO = 0.05f;

    private GhostRenderers() {
    }

    /** O que os desenhistas precisam de saber de um fantasma. */
    public static class Estado extends LivingEntityRenderState {
        public boolean gritando;
        public boolean apagado;
        public float braço;
    }

    // ------------------------------------------------------------------ o Espectro e a Banshee

    /** A malha do {@code ModelSpectre}, caixa por caixa. */
    public static LayerDefinition espectro() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        PartDefinition cabeça = raiz.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.ZERO);
        cabeça.addOrReplaceChild("mouth", CubeListBuilder.create()
                        .texOffs(56, 0).addBox(0.0f, 0.0f, 0.0f, 4.0f, 5.0f, 0.0f),
                PartPose.offset(-2.0f, -4.0f, -4.02f));

        raiz.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 0).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 10.0f, 4.0f), PartPose.ZERO);
        raiz.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offsetAndRotation(-5.0f, 2.0f, 0.0f, -1.396263f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offsetAndRotation(5.0f, 2.0f, 0.0f, -1.396263f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("robe_upper", CubeListBuilder.create()
                        .texOffs(38, 9).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 6.0f, 5.0f),
                PartPose.offset(0.0f, 10.0f, 0.0f));
        raiz.addOrReplaceChild("robe_lower", CubeListBuilder.create()
                        .texOffs(32, 20).addBox(-5.0f, 0.0f, -2.0f, 10.0f, 6.0f, 6.0f),
                PartPose.offset(0.0f, 16.0f, 0.0f));

        return LayerDefinition.create(malha, 64, 32);
    }

    /** O boneco de manto dos dois, com os braços à frente ou caídos. */
    public static class ModeloEspectro extends EntityModel<Estado> {
        private final ModelPart cabeça;
        private final ModelPart boca;
        private final ModelPart braçoDireito;
        private final ModelPart braçoEsquerdo;
        private final boolean estende;

        public ModeloEspectro(ModelPart raiz, boolean estende) {
            super(raiz);
            this.cabeça = raiz.getChild("head");
            this.boca = this.cabeça.getChild("mouth");
            this.braçoDireito = raiz.getChild("right_arm");
            this.braçoEsquerdo = raiz.getChild("left_arm");
            this.estende = estende;
        }

        @Override
        public void setupAnim(Estado estado) {
            super.setupAnim(estado);
            this.boca.visible = estado.gritando;
            this.cabeça.yRot = estado.yRot * ((float) Math.PI / 180.0f);
            this.cabeça.xRot = estado.xRot * ((float) Math.PI / 180.0f);

            if (this.estende) {
                this.braçoDireito.xRot = ESTENDIDO;
                this.braçoEsquerdo.xRot = ESTENDIDO;
                this.braçoDireito.zRot = 0.0f;
                this.braçoEsquerdo.zRot = 0.0f;
            } else {
                this.braçoDireito.xRot = CAÍDO;
                this.braçoEsquerdo.xRot = CAÍDO;
                this.braçoDireito.zRot = estado.gritando ? 1.0f : 0.0f;
                this.braçoEsquerdo.zRot = estado.gritando ? -1.0f : 0.0f;
            }
            this.braçoDireito.yRot = 0.0f;
            this.braçoEsquerdo.yRot = 0.0f;

            float balanço = Mth.sin(estado.ageInTicks * BALANÇO) * QUANTO;
            this.braçoDireito.xRot += balanço;
            this.braçoEsquerdo.xRot -= balanço;
        }
    }

    // ------------------------------------------------------------------ o Poltergeist

    /** A malha do {@code ModelPoltergeist}: quatro braços compridos e pernas de palito. */
    public static LayerDefinition poltergeist() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        raiz.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0f, -8.0f, -3.0f, 8.0f, 8.0f, 6.0f), PartPose.ZERO);
        raiz.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4.0f, 0.0f, -1.0f, 8.0f, 11.0f, 2.0f), PartPose.ZERO);

        for (String qual : new String[]{"right_arm", "right_arm2"}) {
            raiz.addOrReplaceChild(qual, CubeListBuilder.create()
                            .texOffs(40, 0).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 18.0f, 2.0f),
                    PartPose.offset(-5.0f, 2.0f, 0.0f));
        }
        for (String qual : new String[]{"left_arm", "left_arm2"}) {
            raiz.addOrReplaceChild(qual, CubeListBuilder.create()
                            .texOffs(40, 0).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 18.0f, 2.0f),
                    PartPose.offset(5.0f, 2.0f, 0.0f));
        }

        /*
         * As pernas: o original as monta em (∓2, 11, 0) e depois <b>as repõe em (∓2, 12, 0.1)</b> a
         * cada quadro, no mesmo lugar onde anima tudo o resto. Como repor a posição todo quadro é
         * exatamente o que o jogo de hoje já faz antes de animar, a posição boa é a segunda, e é essa
         * que fica aqui.
         */
        raiz.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 13.0f, 2.0f),
                PartPose.offset(-2.0f, 12.0f, 0.1f));
        raiz.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 13.0f, 2.0f),
                PartPose.offset(2.0f, 12.0f, 0.1f));

        return LayerDefinition.create(malha, 64, 32);
    }

    /**
     * O boneco dele, com os <b>quatro braços</b>.
     *
     * <p>Os de dentro andam ao passo e os de fora a metade dele, de modo que os quatro nunca estão na
     * mesma posição. É isso que o faz parecer que tem mais braços do que tem.
     */
    public static class ModeloPoltergeist extends EntityModel<Estado> {
        private final ModelPart cabeça;
        private final ModelPart braçoDireito;
        private final ModelPart braçoDireito2;
        private final ModelPart braçoEsquerdo;
        private final ModelPart braçoEsquerdo2;
        private final ModelPart pernaDireita;
        private final ModelPart pernaEsquerda;

        public ModeloPoltergeist(ModelPart raiz) {
            super(raiz);
            this.cabeça = raiz.getChild("head");
            this.braçoDireito = raiz.getChild("right_arm");
            this.braçoDireito2 = raiz.getChild("right_arm2");
            this.braçoEsquerdo = raiz.getChild("left_arm");
            this.braçoEsquerdo2 = raiz.getChild("left_arm2");
            this.pernaDireita = raiz.getChild("right_leg");
            this.pernaEsquerda = raiz.getChild("left_leg");
        }

        @Override
        public void setupAnim(Estado estado) {
            super.setupAnim(estado);
            this.cabeça.yRot = estado.yRot * ((float) Math.PI / 180.0f);
            this.cabeça.xRot = estado.xRot * ((float) Math.PI / 180.0f);

            float passo = estado.walkAnimationPos;
            float quanto = estado.walkAnimationSpeed;
            this.braçoDireito.xRot = Mth.cos(passo * 0.6662f + (float) Math.PI) * 2.0f * quanto * 0.5f;
            this.braçoDireito2.xRot = Mth.cos(passo * 0.6662f + (float) Math.PI) * 2.0f * quanto * 0.25f;
            this.braçoEsquerdo.xRot = Mth.cos(passo * 0.6662f) * 2.0f * quanto * 0.5f;
            this.braçoEsquerdo2.xRot = Mth.cos(passo * 0.6662f) * 2.0f * quanto * 0.25f;
            this.pernaDireita.xRot = Mth.cos(passo * 0.6662f) * 1.4f * quanto;
            this.pernaEsquerda.xRot = Mth.cos(passo * 0.6662f + (float) Math.PI) * 1.4f * quanto;

            float abre = Mth.cos(estado.ageInTicks * 0.09f) * QUANTO + QUANTO;
            float balanço = Mth.sin(estado.ageInTicks * BALANÇO) * QUANTO;
            this.braçoDireito.zRot = abre;
            this.braçoDireito2.zRot = -abre;
            this.braçoEsquerdo.zRot = -abre;
            this.braçoEsquerdo2.zRot = abre;
            this.braçoDireito.xRot += balanço;
            this.braçoDireito2.xRot -= balanço;
            this.braçoEsquerdo.xRot -= balanço;
            this.braçoEsquerdo2.xRot += balanço;

            /*
             * E o estrago: os quatro braços sobem de uma vez.
             *
             * <p><b>Diferença.</b> O original escreve {@code vaiEVem(i - par4, 15)}, e o {@code par4} ali
             * é o <b>giro da cabeça em graus</b> — não a fração de tique, que era o que a conta do golem
             * de ferro, de onde ele a copiou, tinha nesse lugar. Com isso, no original, o braço levantado
             * tremia conforme o bicho virava a cabeça. Aqui desconta-se a fração de tique, que é o que a
             * conta quer: o braço sobe liso.
             */
            if (estado.braço <= 0.0f) return;
            float quando = vaiEVem(estado.braço, PoltergeistEntity.BRAÇO);
            float sobe = -1.5f + 0.8f * quando;
            this.braçoDireito.xRot = sobe;
            this.braçoDireito2.xRot = sobe;
            this.braçoEsquerdo.xRot = sobe;
            this.braçoEsquerdo2.xRot = sobe;
            this.braçoDireito.zRot = -(-1.5f + 1.5f * quando);
            this.braçoEsquerdo.zRot = -1.5f + 1.5f * quando;
        }

        /** A onda triangular do original, que leva o braço de zero a um e de volta. */
        private static float vaiEVem(float quanto, float volta) {
            return (Math.abs(quanto % volta - volta * 0.5f) - volta * 0.25f) / (volta * 0.25f);
        }
    }

    // ------------------------------------------------------------------ os três desenhistas

    /** O que os três partilham: a folha, a transparência e o estado. */
    public abstract static class Fantasma<T extends SummonedUndeadEntity>
            extends MobRenderer<T, Estado, EntityModel<Estado>> {
        private final Identifier folha;

        protected Fantasma(EntityRendererProvider.Context contexto, EntityModel<Estado> modelo,
                           Identifier folha) {
            super(contexto, modelo, 0.0f);
            this.folha = folha;
        }

        @Override
        public Estado createRenderState() {
            return new Estado();
        }

        @Override
        public void extractRenderState(T bicho, Estado estado, float parcial) {
            super.extractRenderState(bicho, estado, parcial);
            estado.gritando = bicho.gritando();
            estado.apagado = bicho.apagado();
            estado.braço = bicho instanceof PoltergeistEntity ele ? ele.braço() - parcial : 0.0f;
        }

        @Override
        public Identifier getTextureLocation(Estado estado) {
            return this.folha;
        }

        /** Eles são sempre translúcidos, e não só quando o jogo acha que deviam ser. */
        @Override
        protected @org.jetbrains.annotations.Nullable RenderType getRenderType(Estado estado,
                                                                               boolean corpo,
                                                                               boolean translúcido,
                                                                               boolean brilha) {
            return RenderTypes.entityTranslucent(this.folha);
        }
    }

    /** O Espectro, que some enquanto está apagado. */
    public static class Espectro extends Fantasma<SpectreEntity> {
        public Espectro(EntityRendererProvider.Context contexto) {
            super(contexto, new ModeloEspectro(contexto.bakeLayer(ESPECTRO), true), FOLHA_ESPECTRO);
        }

        @Override
        protected int getModelTint(Estado estado) {
            return estado.apagado ? APAGADO : ESPECTRO_COR;
        }
    }

    /** A Banshee, que não se esconde. */
    public static class Banshee extends Fantasma<BansheeEntity> {
        public Banshee(EntityRendererProvider.Context contexto) {
            super(contexto, new ModeloEspectro(contexto.bakeLayer(ESPECTRO), false), FOLHA_BANSHEE);
        }

        @Override
        protected int getModelTint(Estado estado) {
            return BANSHEE_COR;
        }
    }

    /** E o Poltergeist, que quase não está lá. */
    public static class Poltergeist extends Fantasma<PoltergeistEntity> {
        public Poltergeist(EntityRendererProvider.Context contexto) {
            super(contexto, new ModeloPoltergeist(contexto.bakeLayer(POLTERGEIST)), FOLHA_POLTERGEIST);
        }

        @Override
        protected int getModelTint(Estado estado) {
            return POLTERGEIST_COR;
        }
    }
}
