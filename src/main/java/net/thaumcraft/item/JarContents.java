package net.thaumcraft.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.thaumcraft.api.aspects.Aspect;
import org.jetbrains.annotations.Nullable;

/**
 * O que um jarro leva dentro quando sai do chão. É o {@code ItemJarFilled} do original.
 *
 * <p>Sem isto não há como mudar essência de lugar: quebrar um jarro jogava fora tudo o que ele guardava.
 * No original o jarro quebrado cai com a essência — um aspecto só, até sessenta e quatro — e com o rótulo,
 * e pô-lo de novo devolve tudo como estava. O jarro cheio não empilha, justamente para cada um carregar o
 * seu.
 *
 * @param aspect o que tem dentro, ou vazio
 * @param amount quanto
 * @param label  o rótulo colado, ou vazio
 */
public record JarContents(String aspect, int amount, String label) {
    public static final Codec<JarContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("aspect", "").forGetter(JarContents::aspect),
            Codec.INT.optionalFieldOf("amount", 0).forGetter(JarContents::amount),
            Codec.STRING.optionalFieldOf("label", "").forGetter(JarContents::label)
    ).apply(instance, JarContents::new));

    public static final StreamCodec<ByteBuf, JarContents> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, JarContents::aspect,
            ByteBufCodecs.VAR_INT, JarContents::amount,
            ByteBufCodecs.STRING_UTF8, JarContents::label,
            JarContents::new);

    public static JarContents of(@Nullable Aspect aspect, int amount, @Nullable Aspect label) {
        return new JarContents(aspect == null || amount <= 0 ? "" : aspect.tag(), Math.max(0, amount),
                label == null ? "" : label.tag());
    }

    @Nullable
    public Aspect heldAspect() {
        return this.amount > 0 ? Aspect.of(this.aspect) : null;
    }

    @Nullable
    public Aspect labelAspect() {
        return Aspect.of(this.label);
    }

    /** Vale a pena carregar isto? Jarro vazio e sem rótulo é jarro comum. */
    public boolean worthKeeping() {
        return this.heldAspect() != null || this.labelAspect() != null;
    }
}
