package net.thaumcraft.arcana;

import com.mojang.serialization.Codec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A lista de todas as peças de feitiço: o {@code SkillManager} do Ars Magica 2.
 *
 * <p>O original guarda cada peça por <b>número</b>, somando mil às essências e cinco mil aos modificadores para
 * as separar. Aqui guardam-se por <b>nome</b>, e a separação é o próprio tipo da peça — um feitiço não se
 * estraga porque outro mod entrou no meio e empurrou os números.
 */
public final class SpellParts {
    private static final Map<String, SpellPart.Shape> SHAPES = new LinkedHashMap<>();
    private static final Map<String, SpellPart.Essence> ESSENCES = new LinkedHashMap<>();
    private static final Map<String, SpellPart.Modifier> MODIFIERS = new LinkedHashMap<>();

    private SpellParts() {
    }

    public static <T extends SpellPart.Shape> T shape(T qual) {
        if (SHAPES.putIfAbsent(qual.name(), qual) != null) {
            throw new IllegalStateException("duas Formas com o mesmo nome: " + qual.name());
        }
        return qual;
    }

    public static <T extends SpellPart.Essence> T essence(T qual) {
        if (ESSENCES.putIfAbsent(qual.name(), qual) != null) {
            throw new IllegalStateException("duas Essências com o mesmo nome: " + qual.name());
        }
        return qual;
    }

    public static <T extends SpellPart.Modifier> T modifier(T qual) {
        if (MODIFIERS.putIfAbsent(qual.name(), qual) != null) {
            throw new IllegalStateException("dois Modificadores com o mesmo nome: " + qual.name());
        }
        return qual;
    }

    public static List<SpellPart.Shape> shapes() {
        return List.copyOf(SHAPES.values());
    }

    public static List<SpellPart.Essence> essences() {
        return List.copyOf(ESSENCES.values());
    }

    public static List<SpellPart.Modifier> modifiers() {
        return List.copyOf(MODIFIERS.values());
    }

    /** Todas elas, para quem precisar de contar. */
    public static int count() {
        return SHAPES.size() + ESSENCES.size() + MODIFIERS.size();
    }

    // ------------------------------------------------------------------ escrever e ler

    private static <T extends SpellPart> Codec<T> codec(Map<String, T> lista, String o) {
        return Codec.STRING.comapFlatMap(
                nome -> {
                    T achado = lista.get(nome);
                    return achado == null
                            ? com.mojang.serialization.DataResult.error(() -> "não há " + o + " com o nome " + nome)
                            : com.mojang.serialization.DataResult.success(achado);
                },
                SpellPart::name);
    }

    public static final Codec<SpellPart.Shape> SHAPE_CODEC = Codec.lazyInitialized(
            () -> codec(SHAPES, "Forma"));
    public static final Codec<SpellPart.Essence> ESSENCE_CODEC = Codec.lazyInitialized(
            () -> codec(ESSENCES, "Essência"));
    public static final Codec<SpellPart.Modifier> MODIFIER_CODEC = Codec.lazyInitialized(
            () -> codec(MODIFIERS, "Modificador"));

    /** Uma peça pelo nome, seja ela do que for. */
    public static SpellPart byName(String nome) {
        SpellPart achado = SHAPES.get(nome);
        if (achado != null) return achado;
        achado = ESSENCES.get(nome);
        if (achado != null) return achado;
        return MODIFIERS.get(nome);
    }
}
