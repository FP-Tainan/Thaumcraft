package net.thaumcraft.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.nodes.NodeModifier;
import net.thaumcraft.api.nodes.NodeType;
import org.jetbrains.annotations.Nullable;

/**
 * O nó guardado num jarro, como vai no item: o que o {@code ItemJarNode} da 4.2.3.5 escreve na etiqueta — os aspectos,
 * o tipo, o feitio (vazio se não tem) e o número do nó.
 */
public record JarredNode(AspectList aspects, String type, String modifier, String id) {
    public static final Codec<JarredNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AspectList.CODEC.fieldOf("aspects").forGetter(JarredNode::aspects),
            Codec.STRING.optionalFieldOf("type", "normal").forGetter(JarredNode::type),
            Codec.STRING.optionalFieldOf("modifier", "").forGetter(JarredNode::modifier),
            Codec.STRING.optionalFieldOf("id", "").forGetter(JarredNode::id)
    ).apply(instance, JarredNode::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, JarredNode> STREAM_CODEC = StreamCodec.composite(
            AspectList.STREAM_CODEC, JarredNode::aspects,
            ByteBufCodecs.STRING_UTF8, JarredNode::type,
            ByteBufCodecs.STRING_UTF8, JarredNode::modifier,
            ByteBufCodecs.STRING_UTF8, JarredNode::id,
            JarredNode::new);

    public static JarredNode of(AspectList aspects, NodeType type, @Nullable NodeModifier modifier, String id) {
        return new JarredNode(aspects.copy(), type.getSerializedName(), modifier == null ? "" : modifier.getSerializedName(), id);
    }

    public NodeType nodeType() {
        for (NodeType t : NodeType.values()) if (t.getSerializedName().equals(this.type)) return t;
        return NodeType.NORMAL;
    }

    public @Nullable NodeModifier nodeModifier() {
        for (NodeModifier m : NodeModifier.values()) if (m.getSerializedName().equals(this.modifier)) return m;
        return null;
    }
}
