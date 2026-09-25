package net.thaumcraft.mortuorum;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * De que o lacaio é feito: a cabeça, o tronco, os dois braços e as pernas, cada um dito pelo nome da peça
 * ({@code cow_head}, {@code skeleton_arm}...) ou vazio, quando aquele lugar ficou sem nada.
 *
 * <p>É o {@code BodyPart[][]} do {@code EntityMinion} do Necromancy, guardado em dado em vez de em objeto.
 */
public record MinionParts(String head, String torso, String armLeft, String armRight, String legs) {
    public static final MinionParts EMPTY = new MinionParts("", "", "", "", "");

    public static final Codec<MinionParts> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("head", "").forGetter(MinionParts::head),
            Codec.STRING.optionalFieldOf("torso", "").forGetter(MinionParts::torso),
            Codec.STRING.optionalFieldOf("arm_left", "").forGetter(MinionParts::armLeft),
            Codec.STRING.optionalFieldOf("arm_right", "").forGetter(MinionParts::armRight),
            Codec.STRING.optionalFieldOf("legs", "").forGetter(MinionParts::legs)
    ).apply(instance, MinionParts::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MinionParts> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, MinionParts::head,
            ByteBufCodecs.STRING_UTF8, MinionParts::torso,
            ByteBufCodecs.STRING_UTF8, MinionParts::armLeft,
            ByteBufCodecs.STRING_UTF8, MinionParts::armRight,
            ByteBufCodecs.STRING_UTF8, MinionParts::legs,
            MinionParts::new);

    /** As cinco, na ordem em que o altar as pede. */
    public java.util.List<String> all() {
        return java.util.List.of(this.head, this.torso, this.armLeft, this.armRight, this.legs);
    }

    public boolean isEmpty() {
        return this.all().stream().allMatch(String::isEmpty);
    }

    /** De que bicho é a peça que está neste lugar, ou vazio. */
    public static String mobOf(String part) {
        var found = MortuorumItems.PARTS.get(part);
        return found == null ? "" : found.mob();
    }
}
