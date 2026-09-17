package net.thaumcraft.api.nodes;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

/**
 * O feitio de um nó de aura: o quanto ele se refaz depois de esvaziado.
 *
 * <p>São os três do original. O brilhante se refaz depressa, o pálido devagar, e o esmaecido não se refaz
 * mais — o que se tirar dele está tirado para sempre.
 */
public enum NodeModifier implements StringRepresentable {
    BRIGHT("bright"),
    PALE("pale"),
    FADING("fading");

    public static final com.mojang.serialization.Codec<NodeModifier> CODEC =
            StringRepresentable.fromEnum(NodeModifier::values);

    private final String name;

    NodeModifier(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public Component title() {
        return Component.translatable("tc.nodemod." + this.name);
    }
}
