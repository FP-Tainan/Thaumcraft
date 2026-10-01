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
     * Uma casa da Mesa já lida: a peça, e o que se pôs <b>ao lado dela</b>.
     *
     * <p>Quase nenhuma peça precisa de alguma coisa ao lado. A <b>Cor</b> precisa: ela não diz <i>qual</i>
     * cor, e quem diz é a tinta. No original essa tinta está entre os itens da receita da Mesa, e aqui está
     * numa casa logo a seguir à peça — que é o mesmo lugar, lido da esquerda para a direita.
     *
     * @param peça a peça de feitiço
     * @param dado o número que a tinta ao lado dela deu, ou nada
     */
    public record Posta(SpellPart peça, @Nullable Integer dado) {
        public Posta(SpellPart peça) {
            this(peça, null);
        }
    }

    /** Só as peças, que é o que a gramática olha. */
    public static List<SpellPart> onlyParts(List<Posta> postas) {
        return postas.stream().map(Posta::peça).toList();
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

    /**
     * A prova de uma frase com o que se pôs ao lado de cada peça.
     *
     * <p>É a mesma de sempre, mais uma regra: <b>a Cor pede uma tinta</b>. Sem ela a peça não sabe o que
     * fazer, e o original resolve isso na receita — lá a tinta é ingrediente. Aqui a Mesa recusa e diz porquê,
     * que é melhor do que escrever um feitiço que sai preto sem ninguém ter pedido.
     */
    public static Result validateWithData(List<Posta> postas) {
        for (Posta posta : postas) {
            if (posta.peça() == Modifiers.COLOUR && posta.dado() == null) {
                return Result.bad(posta.peça(), "tc.spell.validate.needs_dye");
            }
        }
        return validate(onlyParts(postas));
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

    /**
     * Monta o feitiço, levando o que se pôs ao lado de cada peça.
     *
     * <p>O dado vai para a <b>etapa</b> a que a peça pertence, com o nome dela por chave — que é o alcance
     * que o original dá a esses números.
     */
    public static Spell buildWithData(List<Posta> postas) {
        // a divisão em etapas é a mesma, e feita sobre as peças; o que se guarda aqui é de que etapa é cada uma
        var porEtapa = new ArrayList<List<Posta>>();
        for (Posta posta : postas) {
            if (posta.peça() instanceof SpellPart.Shape || porEtapa.isEmpty()) {
                porEtapa.add(new ArrayList<>());
            }
            porEtapa.getLast().add(posta);
        }

        var etapas = new ArrayList<Spell.Stage>();
        for (List<Posta> etapa : porEtapa) {
            SpellPart.Shape forma = null;
            var essências = new ArrayList<SpellPart.Essence>();
            var modificadores = new ArrayList<SpellPart.Modifier>();
            var dados = new java.util.LinkedHashMap<String, Integer>();

            for (Posta posta : etapa) {
                SpellPart peça = posta.peça();
                if (peça instanceof SpellPart.Shape qual && forma == null) forma = qual;
                else if (peça instanceof SpellPart.Essence qual) essências.add(qual);
                else if (peça instanceof SpellPart.Modifier qual) modificadores.add(qual);
                if (posta.dado() != null) dados.put(peça.name(), posta.dado());
            }
            if (forma == null) continue;
            etapas.add(new Spell.Stage(forma, List.copyOf(essências), List.copyOf(modificadores),
                    java.util.Map.copyOf(dados)));
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
