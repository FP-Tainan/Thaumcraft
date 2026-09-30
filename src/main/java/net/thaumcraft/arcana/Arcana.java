package net.thaumcraft.arcana;

import net.thaumcraft.Thaumcraft;

/**
 * O Ars Arcana — o Ars Magica 2 1.4.0.009, de Mithion, com o nome que a lore de quem joga lhe dá.
 *
 * <p>É a <b>gramática da magia</b>. Onde a Thaumaturgia pergunta <i>por que a magia funciona</i> e o Ars Occulta
 * pergunta <i>que vínculo faz o mundo responder</i>, o Ars Arcana pergunta outra coisa: <b>como construir
 * exatamente o efeito que se quer</b>.
 *
 * <p>E a resposta dele é que um feitiço não é uma receita: é uma <b>frase</b>, escrita com três classes de
 * palavra — a <b>Forma</b>, que diz como o efeito entra no mundo; a <b>Essência</b>, que diz o que ele faz; e os
 * <b>Modificadores</b>, que mudam os números de uma e de outra. Trocar uma palavra faz outro feitiço.
 *
 * <p>Ele traz também a <b>Mana</b>, que não é Vis e não a substitui: Vis é a energia que existe no mundo, e Mana
 * é o quanto um corpo consegue puxar dela de uma vez. É por isso que um arcanista seca no meio de uma aura
 * cheia — a energia está lá, o cano é que acabou.
 *
 * <p>Como os outros ramos, mora no mesmo jar do Thaumcraft, com figuras e textos no espaço de nome
 * {@code thaumcraft}, aba própria no criativo e chaves de pesquisa com o prefixo {@code AA_}.
 *
 * <p><b>Por onde vai:</b> está feito o núcleo — a gramática, a mana, e o bastante de cada classe de palavra para
 * um feitiço correr de ponta a ponta. As Formas que faltam, a Afinidade, a árvore de perícias e a mesa onde se
 * escrevem feitiços vêm depois, na ordem que o {@code docs/PORTE.md} marca.
 */
public final class Arcana {
    /** A aba do ramo no Thaumonomicon. */
    public static final String CATEGORY = "ARCANA";

    private Arcana() {
    }

    public static void init() {
        ArcanaComponents.init();
        ArcanaBlocks.init();
        ArcanaEntities.init();
        AffinityPools.init();
        Contingency.init();
        SkillData.init();
        SkillTree.init();
        // a ordem importa: as peças se registram ao carregar a classe, e os feitiços leem-nas pelo nome
        Shapes.init();
        Essences.init();
        Modifiers.init();
        ArcanaItems.init();
        ManaClock.init();

        // a aba do ramo no livro: sem ela o ramo existia e ninguém dava por ele
        net.thaumcraft.api.ThaumcraftApi.category(CATEGORY,
                Thaumcraft.id("textures/item/spell.png"),
                Thaumcraft.id("textures/gui/gui_researchback.png"));
        ArcanaTable.research();
        // as receitas do livro pedem itens prontos, e por isso esperam a montagem acabar
        net.thaumcraft.api.ThaumcraftApi.onSetup(ArcanaTable::recipes);
    }
}
