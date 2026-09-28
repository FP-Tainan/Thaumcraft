package net.thaumcraft.arcana;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Se uma frase faz sentido: o {@code SpellValidator} do Ars Magica 2.
 *
 * <p>É a <b>gramática</b> do ramo escrita como regra, e não como costume. Até aqui um feitiço malformado só
 * dava em nada ao ser lançado; a partir daqui há quem diga <b>por quê</b>, e onde.
 *
 * <p>As regras são quatro, e todas do original:
 *
 * <ol>
 *   <li>toda etapa tem <b>uma Forma</b>;
 *   <li>a <b>última</b> etapa tem ao menos uma Essência — as do meio não precisam, porque quem faz alguma
 *       coisa é o fim da frase;
 *   <li>uma Forma <b>principum</b> não pode ser a última: ela cria um lugar e pede quem o use;
 *   <li>e uma Forma <b>terminus</b> só pode ser a última: depois dela não há mais frase.
 * </ol>
 *
 * <p>A ordem em que as peças entram é a que as separa em etapas: <b>cada Forma começa uma etapa nova</b>, e o
 * que vier depois dela — Essências e Modificadores — é dessa etapa. É o {@code splitToStages}, e é por isso que
 * escrever um feitiço é escrever uma lista e não preencher um formulário.
 */
public final class SpellValidator {
    private SpellValidator() {
    }

    /**
     * O que saiu da prova de uma frase.
     *
     * @param ok    se ela passa
     * @param blame a peça em que a coisa se estragou, ou nada
     * @param why   e o que dizer a quem escreveu
     */
    public record Result(boolean ok, @Nullable SpellPart blame, @Nullable Component why) {
        public static final Result GOOD = new Result(true, null, null);

        /** Uma frase vazia não é errada: é só uma frase que ainda não se escreveu. */
        public static final Result EMPTY = new Result(false, null, null);

        public static Result bad(SpellPart quem, String chave, Object... com) {
            return new Result(false, quem, Component.translatable(chave, com));
        }
    }

    /** O que uma etapa pode ser. */
    private enum Stage {
        VALID, NOT_VALID, PRINCIPUM, TERMINUS
    }

    /**
     * Separa uma lista de peças em etapas: o {@code splitToStages}.
     *
     * <p>Cada Forma começa uma etapa nova. Peças soltas antes da primeira Forma ficam numa etapa sem Forma —
     * que é inválida, e é assim que se descobre que alguém escreveu a frase pelo fim.
     */
    public static List<List<SpellPart>> split(List<SpellPart> receita) {
        var etapas = new ArrayList<List<SpellPart>>();
        for (SpellPart peça : receita) {
            if (peça instanceof SpellPart.Shape || etapas.isEmpty()) {
                etapas.add(new ArrayList<>());
            }
            etapas.getLast().add(peça);
        }
        return List.copyOf(etapas.stream().map(List::copyOf).toList());
    }

    /** A prova de uma frase escrita como lista de peças. */
    public static Result validate(List<SpellPart> receita) {
        if (receita.isEmpty()) return Result.EMPTY;
        return validateStages(split(receita));
    }

    /** E a mesma prova, com as etapas já separadas. */
    public static Result validateStages(List<List<SpellPart>> etapas) {
        if (etapas.isEmpty()) return Result.EMPTY;

        for (int i = 0; i < etapas.size(); i++) {
            List<SpellPart> etapa = etapas.get(i);
            if (etapa.isEmpty()) continue;
            boolean última = i == etapas.size() - 1;
            SpellPart primeira = etapa.getFirst();

            switch (check(etapa, última)) {
                case NOT_VALID -> {
                    return Result.bad(primeira, "tc.spell.validate.missing");
                }
                case PRINCIPUM -> {
                    if (última) {
                        return Result.bad(primeira, "tc.spell.validate.principum",
                                Component.translatable("tc.spell.shape." + primeira.name()));
                    }
                }
                case TERMINUS -> {
                    if (!última) {
                        return Result.bad(primeira, "tc.spell.validate.terminus",
                                Component.translatable("tc.spell.shape." + primeira.name()));
                    }
                }
                case VALID -> {
                    // segue
                }
            }
        }
        return Result.GOOD;
    }

    /** O {@code validateStage}: o que esta etapa é. */
    private static Stage check(List<SpellPart> etapa, boolean última) {
        boolean temForma = false;
        boolean temEssência = !última;
        boolean principum = false;
        boolean terminus = false;

        for (SpellPart peça : etapa) {
            if (peça instanceof SpellPart.Shape forma) {
                temForma = true;
                if (forma.principum()) principum = true;
                if (forma.terminus()) terminus = true;
            } else if (peça instanceof SpellPart.Essence) {
                temEssência = true;
            }
        }

        if (principum) return Stage.PRINCIPUM;
        if (!temForma || !temEssência) return Stage.NOT_VALID;
        return terminus ? Stage.TERMINUS : Stage.VALID;
    }

    // ------------------------------------------------------------------ escrever

    /**
     * Monta o feitiço que aquela lista de peças descreve.
     *
     * <p>Só chame depois de a prova passar: uma frase malformada não vira feitiço nenhum.
     */
    public static Spell build(List<SpellPart> receita) {
        var etapas = new ArrayList<Spell.Stage>();
        for (List<SpellPart> etapa : split(receita)) {
            SpellPart.Shape forma = null;
            var essências = new ArrayList<SpellPart.Essence>();
            var modificadores = new ArrayList<SpellPart.Modifier>();

            for (SpellPart peça : etapa) {
                if (peça instanceof SpellPart.Shape qual && forma == null) forma = qual;
                else if (peça instanceof SpellPart.Essence qual) essências.add(qual);
                else if (peça instanceof SpellPart.Modifier qual) modificadores.add(qual);
            }
            if (forma == null) continue;
            etapas.add(new Spell.Stage(forma, List.copyOf(essências), List.copyOf(modificadores)));
        }
        return new Spell(List.copyOf(etapas));
    }

    /** E o caminho de volta: as peças de um feitiço já escrito, o {@code reverseEngineerSpell}. */
    public static List<SpellPart> parts(Spell feitiço) {
        var peças = new ArrayList<SpellPart>();
        for (Spell.Stage etapa : feitiço.stages()) {
            peças.add(etapa.shape());
            peças.addAll(etapa.essences());
            peças.addAll(etapa.modifiers());
        }
        return List.copyOf(peças);
    }
}
