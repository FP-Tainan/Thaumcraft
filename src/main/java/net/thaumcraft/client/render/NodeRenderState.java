package net.thaumcraft.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.thaumcraft.api.nodes.NodeModifier;
import net.thaumcraft.api.nodes.NodeType;

import java.util.ArrayList;
import java.util.List;

/** O que o desenhista precisa saber de um nó de aura para pintá-lo neste quadro. */
public class NodeRenderState extends BlockEntityRenderState {
    /** Um aspecto do nó: a cor dele e quanto o nó tem. */
    public record Wisp(int color, int amount, boolean dark) {
    }

    public final List<Wisp> wisps = new ArrayList<>();
    public NodeType type = NodeType.NORMAL;
    public NodeModifier modifier;
    /** O tempo, que faz as bolhas girarem e piscarem. */
    public float ticks;
    /** Um número fixo por nó, para que dois nós vizinhos não pisquem no mesmo compasso. */
    public int seed;
}
