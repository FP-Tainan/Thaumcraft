package net.thaumcraft.naturalis.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * O tronco comum aos quatro feitios do Baú Maligno do Magia Naturalis 0.5.0: a boca de cima, que abre com a mesma
 * curva do baú comum, e — no demoníaco — as asas, que batem com o tempo.
 *
 * <p>As peças de cada feitio saem do jar pelo {@code scratchpad/mn-baus.js}; o que fica aqui é o movimento.
 */
public abstract class EvilTrunkModel extends EntityModel<EvilTrunkRenderer.State> {
    private final ModelPart root;

    protected EvilTrunkModel(ModelPart root) {
        super(root);
        this.root = root;
    }

    /** As peças que sobem junto com a boca. */
    protected abstract String[] lid();

    /** Se este feitio tem asas. */
    protected boolean hasWings() {
        return false;
    }

    @Override
    public void setupAnim(EvilTrunkRenderer.State state) {
        super.setupAnim(state);
        // a mesma curva da tampa do baú comum, sobre o quanto a boca já abriu
        float aberto = 1.0f - state.lidRot;
        aberto = 1.0f - aberto * aberto * aberto;
        float giro = -(aberto * (float) Math.PI / 2.0f);
        for (String nome : this.lid()) {
            this.root.getChild(nome).xRot = giro;
        }
        if (!this.hasWings()) return;

        ModelPart direita = this.root.getChild("chest_right_wing");
        ModelPart esquerda = this.root.getChild("chest_left_wing");
        ModelPart pontaDireita = this.root.getChild("chest_outer_right_wing");
        ModelPart pontaEsquerda = this.root.getChild("chest_outer_left_wing");
        direita.yRot = Mth.cos(state.ageInTicks * 0.5f) * (float) Math.PI * 0.25f;
        esquerda.yRot = -direita.yRot;
        float x = 10.0f * Mth.cos(direita.yRot);
        float z = 10.0f * Mth.sin(direita.yRot);
        pontaDireita.setPos(15.0f + x, 11.0f, 9.0f - z);
        pontaEsquerda.setPos(1.0f - x, 11.0f, 9.0f - z);
        pontaDireita.yRot = direita.yRot * 1.9f;
        pontaEsquerda.yRot = -direita.yRot * 1.9f;
    }
}
