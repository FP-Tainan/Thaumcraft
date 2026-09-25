package net.thaumcraft.mortuorum.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.mortuorum.MinionParts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * O modelo do lacaio: o {@code ModelMinion} do Necromancy.
 *
 * <p>Ele não tem corpo próprio nenhum — o corpo dele é o que lhe emprestaram. Para cada bicho da tabela o cliente
 * coze uma camada com os cinco lugares, e o lacaio usa de cada uma só o lugar que a peça dele mandar. A raiz fica
 * vazia de propósito: quem desenha é o {@link MinionRenderer}, peça por peça, porque cada peça tem a folha do
 * bicho de onde veio.
 */
public class MinionModel extends EntityModel<MinionRenderState> {
    /** Uma camada por bicho, com os cinco lugares dentro. */
    public static final Map<String, ModelLayerLocation> LAYERS = Util.make(new LinkedHashMap<>(), mapa -> {
        for (String mob : MinionModels.all().keySet()) {
            mapa.put(mob, new ModelLayerLocation(Thaumcraft.id("minion_" + mob.toLowerCase(java.util.Locale.ROOT)), "main"));
        }
    });

    /** A raiz cozida de cada bicho. */
    private final Map<String, ModelPart> roots;

    public MinionModel(ModelPart empty, Map<String, ModelPart> roots) {
        super(empty);
        this.roots = roots;
    }

    /** O modelo cozido, das camadas de todos os bichos: serve ao lacaio no mundo e ao corpo deitado no altar. */
    public static MinionModel build(java.util.function.Function<ModelLayerLocation, ModelPart> forno) {
        Map<String, ModelPart> raizes = new LinkedHashMap<>();
        for (var entrada : LAYERS.entrySet()) raizes.put(entrada.getKey(), forno.apply(entrada.getValue()));
        return new MinionModel(forno.apply(EMPTY), raizes);
    }

    /** Os pedaços daquele lugar daquele bicho, prontos para desenhar, ou lista vazia. */
    public List<ModelPart> pieces(String mob, String place) {
        ModelPart raiz = this.roots.get(mob);
        if (raiz == null || !raiz.hasChild(place)) return List.of();
        ModelPart grupo = raiz.getChild(place);
        List<ModelPart> saida = new ArrayList<>();
        int i = 0;
        while (grupo.hasChild(String.valueOf(i))) {
            saida.add(grupo.getChild(String.valueOf(i)));
            i++;
        }
        return saida;
    }

    /** A folha do bicho de onde a peça veio. */
    public static Identifier textureOf(String mob) {
        MinionModels.Mob dados = MinionModels.of(mob);
        return dados == null ? null : Identifier.withDefaultNamespace(dados.texture());
    }

    /** O {@code setRotationAngles} de cada lugar, pelo bicho que o emprestou. */
    @Override
    public void setupAnim(MinionRenderState state) {
        MinionParts parts = state.parts;
        List<String> pecas = parts.all();
        for (int i = 0; i < MinionModels.PLACES.size(); i++) {
            String lugar = MinionModels.PLACES.get(i);
            String mob = MinionParts.mobOf(pecas.get(i));
            MinionModels.Mob dados = MinionModels.of(mob);
            if (dados == null) continue;
            List<ModelPart> partes = this.pieces(mob, lugar);
            if (partes.isEmpty()) continue;
            for (ModelPart parte : partes) parte.resetPose();
            MinionAnimations.apply(dados.family(), lugar, partes, state);
        }
    }

    // ------------------------------------------------------------- a cozedura

    /** A camada de um bicho: um grupo por lugar do corpo, e dentro dele um filho por pedaço. */
    public static LayerDefinition createLayer(MinionModels.Mob mob) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition raiz = mesh.getRoot();
        for (var entrada : mob.limbs().entrySet()) {
            PartDefinition grupo = raiz.addOrReplaceChild(entrada.getKey(), CubeListBuilder.create(), PartPose.ZERO);
            int i = 0;
            for (MinionModels.Piece peca : entrada.getValue()) {
                addPiece(grupo, String.valueOf(i), peca);
                i++;
            }
        }
        return LayerDefinition.create(mesh, mob.textureWidth(), mob.textureHeight());
    }

    private static void addPiece(PartDefinition pai, String nome, MinionModels.Piece peca) {
        CubeListBuilder caixas = CubeListBuilder.create();
        if (peca.mirror()) caixas.mirror();
        for (MinionModels.Cube caixa : peca.cubes()) {
            caixas.texOffs(caixa.u(), caixa.v()).addBox(caixa.x(), caixa.y(), caixa.z(),
                    caixa.w(), caixa.h(), caixa.d(), new CubeDeformation(caixa.inflate()));
        }
        PartDefinition filho = pai.addOrReplaceChild(nome, caixas,
                PartPose.offsetAndRotation(peca.px(), peca.py(), peca.pz(), peca.rx(), peca.ry(), peca.rz()));
        int i = 0;
        for (MinionModels.Piece neto : peca.children()) {
            addPiece(filho, String.valueOf(i), neto);
            i++;
        }
    }

    /** A raiz vazia do modelo, que não desenha nada. */
    public static LayerDefinition createEmptyLayer() {
        return LayerDefinition.create(new MeshDefinition(), 1, 1);
    }

    /** A camada da raiz vazia. */
    public static final ModelLayerLocation EMPTY = new ModelLayerLocation(Thaumcraft.id("minion"), "main");
}
