package net.thaumcraft.naturalis.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.naturalis.EvilTrunkEntity;

import java.util.Map;

/**
 * O Baú Maligno no mundo: o {@code EvilTrunkRenderer} do Magia Naturalis 0.5.0 — um modelo e uma pele para cada
 * feitio, e a boca que abre conforme ele pula ou é aberto.
 */
public class EvilTrunkRenderer extends MobRenderer<EvilTrunkEntity, EvilTrunkRenderer.State, EvilTrunkModel> {
    /** Uma camada de modelo por feitio, que o cliente coze na abertura. */
    public static final Map<EvilTrunkEntity.Kind, ModelLayerLocation> LAYERS = Util.makeEnumMap(EvilTrunkEntity.Kind.class,
            kind -> new ModelLayerLocation(Thaumcraft.id("evil_trunk_" + kind.name().toLowerCase()), "main"));
    private static final Map<EvilTrunkEntity.Kind, Identifier> SKINS = Map.of(
            EvilTrunkEntity.Kind.CORRUPTED, Thaumcraft.id("textures/models/trunk_corrupted.png"),
            EvilTrunkEntity.Kind.SINISTER, Thaumcraft.id("textures/models/trunk_sinister.png"),
            EvilTrunkEntity.Kind.DEMONIC, Thaumcraft.id("textures/models/trunk_demonic_wings.png"),
            EvilTrunkEntity.Kind.TAINTED, Thaumcraft.id("textures/models/trunk_tainted.png"));

    public static class State extends LivingEntityRenderState {
        EvilTrunkEntity.Kind kind = EvilTrunkEntity.Kind.CORRUPTED;
        public float lidRot;
    }

    private final Map<EvilTrunkEntity.Kind, EvilTrunkModel> models;

    public EvilTrunkRenderer(EntityRendererProvider.Context context) {
        super(context, new CorruptedTrunkModel(context.bakeLayer(LAYERS.get(EvilTrunkEntity.Kind.CORRUPTED))), 0.5f);
        this.models = Util.makeEnumMap(EvilTrunkEntity.Kind.class, kind -> switch (kind) {
            case CORRUPTED -> new CorruptedTrunkModel(context.bakeLayer(LAYERS.get(kind)));
            case SINISTER -> new SinisterTrunkModel(context.bakeLayer(LAYERS.get(kind)));
            case DEMONIC -> new DemonicTrunkModel(context.bakeLayer(LAYERS.get(kind)));
            case TAINTED -> new TaintedTrunkModel(context.bakeLayer(LAYERS.get(kind)));
        });
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EvilTrunkEntity trunk, State state, float partial) {
        super.extractRenderState(trunk, state, partial);
        state.kind = trunk.kind();
        state.lidRot = trunk.lidRot;
        // o modelo do feitio deste baú é o que vai desenhar
        this.model = this.models.get(state.kind);
    }

    /**
     * O {@code GL11.glTranslatef(-0.5F, 0.5F, -0.5F)} que o modelo do original faz antes de desenhar: as peças do
     * baú são medidas a partir do canto do bloco, e sem isto ele fica meio bloco no ar e meio bloco de lado.
     */
    @Override
    protected void scale(State state, com.mojang.blaze3d.vertex.PoseStack pose) {
        pose.translate(-0.5f, 0.5f, -0.5f);
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return SKINS.get(state.kind);
    }
}
