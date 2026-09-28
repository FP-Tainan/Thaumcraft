package net.thaumcraft.arcana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Um feitiço escrito: a frase inteira, em etapas.
 *
 * <p>No Ars Magica 2 isto mora em pedaços soltos do NBT de um item — {@code NumStages}, {@code ShapeOrdinal_0},
 * {@code SpellComponentIDs_0} e por aí. Aqui mora num <b>componente</b>, que é onde o jogo de hoje guarda o que
 * um item é, e o que se guarda são <b>nomes</b> e não números de registo.
 *
 * <p>Cada <b>etapa</b> é uma Forma com as suas Essências e os seus Modificadores. Lançar um feitiço é correr a
 * primeira etapa; a Forma dela, ao acabar, chama {@link #pop()} e lança <b>o que sobrou</b>. É assim que um
 * projétil que bate acorda o toque que vem depois dele.
 */
public record Spell(List<Stage> stages) {
    public static final Spell EMPTY = new Spell(List.of());

    /**
     * Uma etapa da frase.
     *
     * @param shape      a Forma, que diz como o efeito entra no mundo
     * @param essences   o que ele faz
     * @param modifiers  e o que muda os números dele
     */
    public record Stage(SpellPart.Shape shape, List<SpellPart.Essence> essences,
                        List<SpellPart.Modifier> modifiers) {
        public static final Codec<Stage> CODEC = RecordCodecBuilder.create(i -> i.group(
                SpellParts.SHAPE_CODEC.fieldOf("shape").forGetter(Stage::shape),
                SpellParts.ESSENCE_CODEC.listOf().optionalFieldOf("essences", List.of())
                        .forGetter(Stage::essences),
                SpellParts.MODIFIER_CODEC.listOf().optionalFieldOf("modifiers", List.of())
                        .forGetter(Stage::modifiers))
                .apply(i, Stage::new));
    }

    public static final Codec<Spell> CODEC = Stage.CODEC.listOf()
            .xmap(Spell::new, Spell::stages);

    public static final net.minecraft.network.codec.StreamCodec<
            net.minecraft.network.RegistryFriendlyByteBuf, Spell> STREAM_CODEC =
            net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public boolean isEmpty() {
        return this.stages.isEmpty();
    }

    /** A etapa da frente, que é a que se lança. */
    public @Nullable Stage first() {
        return this.stages.isEmpty() ? null : this.stages.getFirst();
    }

    /** O que sobra depois de a etapa da frente correr: o {@code popStackStage} do original. */
    public Spell pop() {
        if (this.stages.size() <= 1) return EMPTY;
        return new Spell(List.copyOf(this.stages.subList(1, this.stages.size())));
    }

    /** Uma frase de uma etapa só, para quem quiser montar uma à mão. */
    public static Spell of(SpellPart.Shape forma, SpellPart.Essence... essências) {
        return new Spell(List.of(new Stage(forma, List.of(essências), List.of())));
    }

    // ------------------------------------------------------------------ os números

    /**
     * Um número desta etapa, <b>multiplicado</b> por cada modificador que mexe nele: o
     * {@code getModifiedDouble_Mul} do original.
     *
     * <p>O mesmo modificador posto duas vezes multiplica duas vezes, que é o que faz valer a pena repeti-lo.
     */
    public double mul(SpellModifierKind qual, double base) {
        Stage etapa = this.first();
        if (etapa == null) return base;
        double valor = base;
        for (SpellPart.Modifier mod : etapa.modifiers()) {
            if (mod.modifies().contains(qual)) valor *= mod.value(qual);
        }
        return valor;
    }

    /** E o mesmo, <b>somando</b>: o {@code getModifiedDouble_Add}. */
    public double add(SpellModifierKind qual, double base) {
        Stage etapa = this.first();
        if (etapa == null) return base;
        double valor = base;
        for (SpellPart.Modifier mod : etapa.modifiers()) {
            if (mod.modifies().contains(qual)) valor += mod.value(qual);
        }
        return valor;
    }

    /** Se esta etapa traz algum modificador daquele feitio. */
    public boolean has(SpellModifierKind qual) {
        Stage etapa = this.first();
        if (etapa == null) return false;
        return etapa.modifiers().stream().anyMatch(m -> m.modifies().contains(qual));
    }

    /**
     * O que a etapa da frente custa de mana.
     *
     * <p>A conta do original: se soma o que cada essência custa, se multiplica pelo que a Forma pede, e se
     * multiplica outra vez por cada modificador — contado pelo <b>número de vezes</b> que ele aparece, que é
     * o que torna um feitiço muito modificado caro de verdade.
     */
    public float manaCost(@Nullable LivingEntity quem, @Nullable Entity alvo) {
        Stage etapa = this.first();
        if (etapa == null) return 0.0f;

        float custo = 0.0f;
        for (SpellPart.Essence essência : etapa.essences()) custo += essência.manaCost();
        custo *= etapa.shape().manaMultiplier();

        var contados = new java.util.LinkedHashMap<SpellPart.Modifier, Integer>();
        for (SpellPart.Modifier mod : etapa.modifiers()) contados.merge(mod, 1, Integer::sum);
        for (var par : contados.entrySet()) custo *= par.getKey().manaMultiplier(par.getValue());
        return custo;
    }

    /** E o desgaste que ela deixa. */
    public float burnout() {
        Stage etapa = this.first();
        if (etapa == null) return 0.0f;
        float total = 0.0f;
        for (SpellPart.Essence essência : etapa.essences()) total += essência.burnout();
        return total;
    }
}
