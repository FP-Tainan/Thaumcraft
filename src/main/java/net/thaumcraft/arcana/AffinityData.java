package net.thaumcraft.arcana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * O quanto cada Afinidade pegou numa pessoa: o {@code AffinityData} do Ars Magica 2.
 *
 * <p>Guarda dois números e uma tranca. As <b>profundidades</b>, de 0 a 100 em cada uma das dez; o <b>retorno
 * decrescente</b>, que desce a cada feitiço lançado e sobe sozinho com o tempo; e a <b>tranca</b>, que fecha
 * quando alguma chega aos 100 e nunca mais abre.
 *
 * <p>O retorno decrescente é o que impede alguém de ficar com uma Afinidade em uma tarde: ele começa em
 * <b>1,2</b>, perde <b>0,3</b> por feitiço lançado e volta a subir <b>0,005</b> por batida. Despejar feitiços
 * seguidos zera o ganho; a Afinidade vem de lançar ao longo de muitos dias, que é exatamente o que ela devia
 * significar.
 *
 * @param depths  quanto cada uma pegou, de 0 a {@link Affinity#MAX_DEPTH}
 * @param falloff o retorno decrescente, de 0 a {@link #MAX_FALLOFF}
 * @param locked  se já trancou
 */
public record AffinityData(Map<Affinity, Float> depths, float falloff, boolean locked) {
    /** Onde o retorno decrescente descansa, e de onde ele nunca passa: o 1,2 do original. */
    public static final float MAX_FALLOFF = 1.2f;

    /** O que ele perde a cada feitiço lançado. */
    public static final float FALLOFF_PER_CAST = 0.3f;

    /** E o que perde quando o feitiço é canalizado, que é um terço disso. */
    public static final float FALLOFF_PER_CHANNELED = 0.1f;

    /** O que ele recupera por batida. */
    public static final float FALLOFF_REGAIN = 0.005f;

    public static final AffinityData NONE = new AffinityData(Map.of(), MAX_FALLOFF, false);

    public static final Codec<AffinityData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(
                            Codec.STRING.xmap(
                                    nome -> Affinity.valueOf(nome.toUpperCase(java.util.Locale.ROOT)),
                                    a -> a.name().toLowerCase(java.util.Locale.ROOT)),
                            Codec.FLOAT)
                    .optionalFieldOf("depths", Map.of()).forGetter(AffinityData::depths),
            Codec.FLOAT.optionalFieldOf("falloff", MAX_FALLOFF).forGetter(AffinityData::falloff),
            Codec.BOOL.optionalFieldOf("locked", false).forGetter(AffinityData::locked))
            .apply(i, AffinityData::new));

    public static final AttachmentType<AffinityData> DATA = AttachmentRegistry.<AffinityData>builder()
            .initializer(() -> NONE)
            .persistent(CODEC)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("affinity"));

    public static AffinityData of(Player quem) {
        return quem.getAttachedOrCreate(DATA);
    }

    public static void set(Player quem, AffinityData agora) {
        quem.setAttached(DATA, agora);
    }

    // ------------------------------------------------------------------ ler

    /** Quanto aquela Afinidade pegou, de 0 a 100. */
    public float raw(Affinity qual) {
        if (qual == Affinity.NONE) return 0.0f;
        return this.depths.getOrDefault(qual, 0.0f);
    }

    /**
     * A mesma coisa de 0 a 1: o {@code getAffinityDepth} do original.
     *
     * <p>É este número que todos os efeitos leem — "a partir de meio", "a partir de três quartos" —, e é por
     * isso que ele vale a pena existir separado do de cima.
     */
    public float depth(Affinity qual) {
        return this.raw(qual) / Affinity.MAX_DEPTH;
    }

    /** A que mais pegou, ou a Afinidade nenhuma se ainda não pegou nada. */
    public Affinity highest() {
        Affinity maior = Affinity.NONE;
        float quanto = 0.0f;
        for (var par : this.depths.entrySet()) {
            if (par.getValue() > quanto) {
                maior = par.getKey();
                quanto = par.getValue();
            }
        }
        return maior;
    }

    // ------------------------------------------------------------------ mexer

    /**
     * Cresce numa Afinidade e <b>encolhe nas outras</b>: o {@code incrementAffinity}.
     *
     * <p>Quem já trancou não muda mais. E quem chega aos 100 tranca aqui.
     */
    public AffinityData increment(Affinity qual, float quanto) {
        if (qual == Affinity.NONE || this.locked || quanto <= 0.0f) return this;

        var novas = new EnumMap<Affinity, Float>(Affinity.class);
        novas.putAll(this.depths);

        soma(novas, qual, quanto);
        for (Affinity vizinha : qual.adjacent()) soma(novas, vizinha, -quanto * Affinity.ADJACENT_FACTOR);
        for (Affinity menor : qual.minor()) soma(novas, menor, -quanto * Affinity.MINOR_FACTOR);
        for (Affinity maior : qual.major()) soma(novas, maior, -quanto * Affinity.MAJOR_FACTOR);
        soma(novas, qual.opposite(), -quanto);

        boolean trancou = novas.getOrDefault(qual, 0.0f) >= Affinity.MAX_DEPTH;
        return new AffinityData(Map.copyOf(novas), this.falloff, trancou);
    }

    private static void soma(Map<Affinity, Float> onde, Affinity qual, float quanto) {
        if (qual == Affinity.NONE) return;
        float agora = Math.clamp(onde.getOrDefault(qual, 0.0f) + quanto, 0.0f, Affinity.MAX_DEPTH);
        onde.put(qual, agora);
    }

    /** Põe uma Afinidade num valor, sem mexer nas outras: o {@code setAffinityAndDepth}. */
    public AffinityData with(Affinity qual, float quanto) {
        if (qual == Affinity.NONE) return this;
        var novas = new EnumMap<Affinity, Float>(Affinity.class);
        novas.putAll(this.depths);
        novas.put(qual, Math.clamp(quanto, 0.0f, Affinity.MAX_DEPTH));
        return new AffinityData(Map.copyOf(novas), this.falloff, this.locked);
    }

    /** O retorno decrescente depois de um feitiço: o {@code addDiminishingReturns}. */
    public AffinityData spent(boolean canalizado) {
        float agora = Math.max(0.0f,
                this.falloff - (canalizado ? FALLOFF_PER_CHANNELED : FALLOFF_PER_CAST));
        return new AffinityData(this.depths, agora, this.locked);
    }

    /** E o que ele recupera numa batida: o {@code tickDiminishingReturns}. */
    public AffinityData recovered() {
        if (this.falloff >= MAX_FALLOFF) return this;
        return new AffinityData(this.depths, Math.min(MAX_FALLOFF, this.falloff + FALLOFF_REGAIN),
                this.locked);
    }

    /** Tudo o que pegou, da que mais pegou para a que menos, para o livro e para as provas. */
    public List<Map.Entry<Affinity, Float>> sorted() {
        var lista = new java.util.ArrayList<>(new LinkedHashMap<>(this.depths).entrySet());
        lista.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));
        return List.copyOf(lista);
    }
}
