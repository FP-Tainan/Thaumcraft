package net.thaumcraft.api.nodes;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

/**
 * Que tipo de nó de aura é aquele, com os seis nomes da 4.2.3.5.
 *
 * <p>O tipo manda em como o nó se comporta com quem se aproxima: o comum só está ali, o instável se
 * remexe, o escuro cobra caro, o maculado espalha mácula, o faminto engole o que chega perto e o puro
 * limpa o que está em volta.
 */
public enum NodeType implements StringRepresentable {
    NORMAL("normal"),
    UNSTABLE("unstable"),
    DARK("dark"),
    TAINTED("tainted"),
    HUNGRY("hungry"),
    PURE("pure");

    public static final com.mojang.serialization.Codec<NodeType> CODEC = StringRepresentable.fromEnum(NodeType::values);

    private final String name;

    NodeType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public Component title() {
        return Component.translatable("tc.nodetype." + this.name);
    }
}
