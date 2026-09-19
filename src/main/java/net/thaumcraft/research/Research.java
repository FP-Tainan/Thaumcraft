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

    /** O desenho próprio dela, quando tem. */
    public Identifier icon() {
        return iconTexture == null ? null : Thaumcraft.id(iconTexture);
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
}
