package net.thaumcraft.research;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;

import java.util.List;

/**
 * Uma pesquisa do Thaumonomicon: onde ela fica no mapa, do que é feita e de que outras ela depende.
 *
 * <p>É o {@code ResearchItem} do mod original, com os mesmos campos e as mesmas marcas. A tabela inteira
 * está em {@link Researches}, gerada a partir do {@code ConfigResearch} da 4.2.3.5.
 *
 * @param key          a chave, que também dá nome e texto pelo idioma
 * @param category     a aba em que ela mora
 * @param tags         os aspectos que custa pesquisar
 * @param column       a coluna no mapa (cada casa vale 24 pontos de tela)
 * @param row          a linha no mapa
 * @param complexity   de um a três, o quanto ela é difícil
 * @param iconTexture  o desenho próprio dela, quando tem (caminho dentro dos recursos do mod)
 * @param iconStack    o item que a representa, quando o desenho é um item
 * @param marks        as marcas do original: degrau, redonda, escondida, e assim por diante
 * @param parents      de quais pesquisas ela nasce
 * @param parentsHidden os pais que só aparecem depois de ela mesma aparecer
 * @param siblings     as irmãs, ligadas por linha mas sem exigência
 * @param pages        as páginas: de texto (pelo nome no idioma) e de receita (pelo nome da receita no original)
 * @param warp         quanta distorção ela traz para quem a aprende
 * @param requires     a etiqueta de lingote de que ela precisa para existir (os metais que vinham de outros mods: o
 *                     {@code Config.foundXIngot} do original), ou nada
 */
public record Research(String key, String category, AspectList tags, int column, int row, int complexity,
                       String iconTexture, java.util.function.@org.jetbrains.annotations.Nullable Supplier<net.minecraft.world.item.ItemStack> iconStack,
                       List<Mark> marks,
                       List<String> parents, List<String> parentsHidden, List<String> siblings,
                       List<Page> pages, int warp, @org.jetbrains.annotations.Nullable String requires) {

    /** As marcas do original, que mandam em como a pesquisa aparece e em quando ela aparece. */
    public enum Mark {
        /** Não se pesquisa: só se lê. */
        STUB,
        /** Moldura redonda em vez de hexagonal. */
        ROUND,
        /** Já vem sabida desde o começo. */
        AUTO,
        /** Ganha a moldura enfeitada por cima. */
        SPECIAL,
        /** Caminho de lado, não do tronco. */
        SECONDARY,
        /** Só aparece quando se tem o que ela pede. */
        CONCEALED,
        /** Só aparece depois de topar com o que a desperta. */
        HIDDEN,
        /** Não fica no mapa: existe só como degrau entre outras. */
        VIRTUAL,
        /** Some do mapa depois de sabida. */
        LOST
    }

    public boolean is(Mark mark) {
        return marks.contains(mark);
    }

    /** O nome que sai no idioma. */
    public Component name() {
        return Component.translatable("tc.research_name." + key);
    }

    /** Existe neste mundo? As dos metais de outros mods só com a etiqueta do lingote tendo algum item. */
    public boolean present() {
        if (this.requires == null) return true;
        var tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.parse(this.requires));
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getTagOrEmpty(tag).iterator().hasNext();
    }

    /**
     * O desenho próprio dela, quando tem. Sem dois-pontos o caminho é do próprio Thaumcraft, que é como a tabela do
     * mod o escreve; um mod de fora diz o seu ({@code meumod:textures/misc/x.png}).
     */
    public Identifier icon() {
        if (iconTexture == null) return null;
        return iconTexture.indexOf(':') < 0 ? Thaumcraft.id(iconTexture) : Identifier.parse(iconTexture);
    }

    /** O aspecto de que ela mais precisa — no original é o que dá a cara dela quando falta desenho. */
    public Aspect primaryTag() {
        Aspect chosen = null;
        int most = 0;
        for (Aspect aspect : tags.getAspects()) {
            int amount = tags.getAmount(aspect);
            if (amount <= most) continue;
            chosen = aspect;
            most = amount;
        }
        return chosen;
    }

    /**
     * O jeito de montar uma pesquisa de fora do mod, no mesmo espírito do {@code ResearchItem} do original: começa
     * com a chave e a aba, vai recebendo o que tem, e no fim {@link Builder#register()} a põe no livro.
     *
     * <pre>{@code
     * Research.of("WARPWOOD", "MALEFICIUM")
     *         .aspects(new AspectList().add(Aspects.MAGIC, 3))
     *         .at(2, -1).complexity(2)
     *         .icon(() -> new ItemStack(MeuMod.TABUA))
     *         .parents("TAINTMANIP")
     *         .pages(Page.text("meumod.research_page.WARPWOOD.1"), Page.arcane("Warpwood"))
     *         .register();
     * }</pre>
     */
    public static Builder of(String key, String category) {
        return new Builder(key, category);
    }

    /** O construtor de pesquisas de fora; veja {@link Research#of}. */
    public static final class Builder {
        private final String key;
        private final String category;
        private AspectList tags = new AspectList();
        private int column;
        private int row;
        private int complexity = 1;
        private String iconTexture;
        private java.util.function.Supplier<net.minecraft.world.item.ItemStack> iconStack;
        private final List<Mark> marks = new java.util.ArrayList<>();
        private List<String> parents = List.of();
        private List<String> parentsHidden = List.of();
        private List<String> siblings = List.of();
        private List<Page> pages = List.of();
        private int warp;
        private String requires;

        private Builder(String key, String category) {
            this.key = key;
            this.category = category;
        }

        /** Os aspectos que custa pesquisar. */
        public Builder aspects(AspectList tags) {
            this.tags = tags;
            return this;
        }

        /** Onde ela fica no mapa: cada casa vale vinte e quatro pontos de tela. */
        public Builder at(int column, int row) {
            this.column = column;
            this.row = row;
            return this;
        }

        /** De um a três, o quanto ela é difícil. */
        public Builder complexity(int complexity) {
            this.complexity = complexity;
            return this;
        }

        /** O desenho próprio dela (caminho dentro dos recursos do mod, como {@code textures/misc/x.png}). */
        public Builder icon(String texture) {
            this.iconTexture = texture;
            return this;
        }

        /** O item que a representa. */
        public Builder icon(java.util.function.Supplier<net.minecraft.world.item.ItemStack> stack) {
            this.iconStack = stack;
            return this;
        }

        /** De quais pesquisas ela nasce. */
        public Builder parents(String... parents) {
            this.parents = List.of(parents);
            return this;
        }

        /** Os pais que só aparecem depois de ela mesma aparecer. */
        public Builder hiddenParents(String... parents) {
            this.parentsHidden = List.of(parents);
            return this;
        }

        /** As irmãs, ligadas por linha mas sem exigência. */
        public Builder siblings(String... siblings) {
            this.siblings = List.of(siblings);
            return this;
        }

        /** As páginas, na ordem em que o livro as vira. */
        public Builder pages(Page... pages) {
            this.pages = List.of(pages);
            return this;
        }

        /** Quanta distorção ela traz para quem a aprende. */
        public Builder warp(int warp) {
            this.warp = warp;
            return this;
        }

        /** A etiqueta de item sem a qual ela nem existe (o {@code Config.foundXIngot} do original). */
        public Builder requires(String tag) {
            this.requires = tag;
            return this;
        }

        /** As marcas do original, uma a uma. */
        public Builder mark(Mark... marks) {
            this.marks.addAll(List.of(marks));
            return this;
        }

        /** Não se pesquisa: só se lê. */
        public Builder stub() {
            return this.mark(Mark.STUB);
        }

        /** Moldura redonda em vez de hexagonal. */
        public Builder round() {
            return this.mark(Mark.ROUND);
        }

        /** Já vem sabida desde o começo. */
        public Builder auto() {
            return this.mark(Mark.AUTO);
        }

        /** Ganha a moldura enfeitada por cima. */
        public Builder special() {
            return this.mark(Mark.SPECIAL);
        }

        /** Caminho de lado, não do tronco. */
        public Builder secondary() {
            return this.mark(Mark.SECONDARY);
        }

        /** Só aparece quando se tem o que ela pede. */
        public Builder concealed() {
            return this.mark(Mark.CONCEALED);
        }

        /** Só aparece depois de topar com o que a desperta. */
        public Builder hidden() {
            return this.mark(Mark.HIDDEN);
        }

        /** Não fica no mapa: existe só como degrau entre outras. */
        public Builder virtual() {
            return this.mark(Mark.VIRTUAL);
        }

        /** Some do mapa depois de sabida. */
        public Builder lost() {
            return this.mark(Mark.LOST);
        }

        /** Monta a pesquisa sem pô-la no livro. */
        public Research build() {
            return new Research(this.key, this.category, this.tags, this.column, this.row, this.complexity,
                    this.iconTexture, this.iconStack, List.copyOf(this.marks),
                    this.parents, this.parentsHidden, this.siblings, this.pages, this.warp, this.requires);
        }

        /** Monta a pesquisa e a põe no livro. */
        public Research register() {
            Research research = this.build();
            Researches.register(research);
            return research;
        }
    }
}
