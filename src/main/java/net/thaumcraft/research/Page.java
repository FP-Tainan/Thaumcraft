package net.thaumcraft.research;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

/**
 * Uma página de pesquisa do Thaumonomicon: o {@code ResearchPage} da 4.2.3.5. As de texto levam o nome da página no idioma;
 * as de receita, o nome que a receita tem no {@code ConfigRecipes} do original (as de lista mostram uma de cada vez, em
 * rodízio); a da fornalha, o que entra nela.
 */
public sealed interface Page {
    /** O texto de uma página ({@code TEXT}). */
    record Text(String key) implements Page {
    }

    /** O texto que só aparece para quem já sabe {@code research} ({@code TEXT_CONCEALED}). */
    record Concealed(String research, String key) implements Page {
    }

    /** Uma página de receita, e de que tipo. */
    record Recipe(Kind kind, List<String> names) implements Page {
    }

    /** A fornalha ({@code SMELTING}): o que entra e o que a fornalha faz dele. */
    record Smelting(Supplier<ItemStack> input, Supplier<ItemStack> output) implements Page {
    }

    /** Os aspectos conhecidos, quatro por página ({@code ASPECTS}): só a pesquisa "Aspectos" as tem, montadas na hora. */
    record Aspects(net.thaumcraft.api.aspects.AspectList aspects) implements Page {
    }

    /** Os tipos de página de receita. */
    enum Kind {
        /** A bancada comum ({@code NORMAL_CRAFTING}). */
        CRAFTING,
        /** A bancada arcana ({@code ARCANE_CRAFTING}). */
        ARCANE,
        /** O crisol ({@code CRUCIBLE_CRAFTING}). */
        CRUCIBLE,
        /** A infusão ({@code INFUSION_CRAFTING}). */
        INFUSION,
        /** O encantamento por infusão ({@code INFUSION_ENCHANTMENT}). */
        ENCHANTMENT,
        /** A montagem de uma estrutura ({@code COMPOUND_CRAFTING}). */
        COMPOUND,
        /** O aumento rúnico, a infusão que endurece o escudo (as {@code InfusionRunicAugmentRecipe}). */
        RUNIC
    }

    // ------------------------------------------------------------------ atalhos para quem escreve um mod de fora

    /** Uma página de texto, pelo nome dela no idioma. */
    static Page text(String key) {
        return new Text(key);
    }

    /** Um texto que só aparece para quem já sabe aquela pesquisa. */
    static Page concealed(String research, String key) {
        return new Concealed(research, key);
    }

    /** Uma página com receitas da bancada comum, pelos nomes que elas têm no livro. */
    static Page crafting(String... names) {
        return new Recipe(Kind.CRAFTING, List.of(names));
    }

    /** Uma página com receitas da bancada arcana. */
    static Page arcane(String... names) {
        return new Recipe(Kind.ARCANE, List.of(names));
    }

    /** Uma página com receitas de crisol. */
    static Page crucible(String... names) {
        return new Recipe(Kind.CRUCIBLE, List.of(names));
    }

    /** Uma página com receitas de infusão. */
    static Page infusion(String... names) {
        return new Recipe(Kind.INFUSION, List.of(names));
    }

    /** Uma página com encantamentos por infusão. */
    static Page enchantment(String... names) {
        return new Recipe(Kind.ENCHANTMENT, List.of(names));
    }

    /** Uma página com a montagem de uma estrutura. */
    static Page compound(String... names) {
        return new Recipe(Kind.COMPOUND, List.of(names));
    }

    /** Uma página de fornalha: o que entra e o que sai. */
    static Page smelting(Supplier<ItemStack> input, Supplier<ItemStack> output) {
        return new Smelting(input, output);
    }
}
