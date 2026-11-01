package net.thaumcraft.occulta.biome;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * O <b>índice de biomas</b> do Livro de Biomas: as {@code BIOME_TYPES} do {@code ItemBook} do Witchery.
 *
 * <p>O livro do original não é uma lista de biomas: é uma lista de <b>seções</b>, uma por tipo do dicionário
 * de biomas da Forge, e dentro de cada seção os biomas que a Forge dizia serem daquele tipo. São catorze
 * seções, nesta ordem — praia, floresta, colina, mesa, montanha, pradaria, areia, neve, pântano, terra
 * devastada, rio, mar, assombrado e mágico —, e é por elas que se navega: o marcador do lado direito da
 * folha salta para o primeiro bioma de cada uma.
 *
 * <h2>O dicionário da Forge não existe mais</h2>
 *
 * <p><b>Desvio declarado.</b> O {@code BiomeDictionary} era uma lista que cada mod ia enchendo à mão, e não
 * há nada igual hoje: o que há são <b>rótulos</b>, e metade das catorze seções tem um rótulo do próprio jogo
 * com o mesmo sentido — {@code is_beach}, {@code is_forest}, {@code is_hill}, {@code is_badlands},
 * {@code is_mountain}, {@code is_river} e {@code is_ocean}. Essas sete usam o rótulo do jogo, que é a
 * tradução certa de 2014 para hoje.
 *
 * <p>Das outras sete, <b>seis</b> já tinham rótulo escrito à mão neste porte: são os climas da
 * {@linkplain net.thaumcraft.occulta.rite.ClimateChange Mudança de Clima}. Reaproveitá-los é mais do que
 * poupar arquivos — faz o <b>livro e o rito falarem a mesma língua</b>: a seção de neve do livro é
 * exatamente o clima de gelo do rito, e quem lê um aprende o outro. A sétima, a <b>assombrada</b>, não tinha
 * par e ganhou rótulo próprio.
 *
 * <h2>E uma seção que o original não tinha</h2>
 *
 * <p>Em 2014 os biomas eram uma lista fechada no código; hoje são <b>dados</b>, e um pacote pode acrescentar
 * quantos quiser. Um livro que só mostrasse os das catorze seções <b>esconderia</b> esses — e esconderia
 * também os do próprio jogo que não caem em nenhuma delas. Por isso há uma décima quinta seção, o
 * <b>resto</b>, com todos os biomas do mundo de cima que as outras não nomearam. Nenhum bioma fica invisível.
 *
 * <p>O que fica de fora dela, de propósito, é o <b>inferno e o fim</b>: o livro do original só falava do
 * mundo de cima, e trocar o bioma de um pedaço de pradaria pelo do vazio do fim não é coisa que o ofício
 * faça.
 */
public final class BiomeCatalogue {
    /** Uma seção do livro: o nome dela e o rótulo que diz quais biomas são dela. */
    public record Seção(String nome, TagKey<Biome> rótulo) {
        /** A chave do nome dela no arquivo de idioma: o {@code witchery.book.biomes.*.name}. */
        public String chave() {
            return "tc.book.biomes." + this.nome + ".name";
        }

        public Component rótuloEscrito() {
            return Component.translatable(this.chave());
        }
    }

    /** O rótulo de um clima do rito, que é o que estas seções reaproveitam. */
    private static TagKey<Biome> doRito(String clima) {
        return TagKey.create(Registries.BIOME, Thaumcraft.id("climate_" + clima));
    }

    /** E o rótulo próprio da única seção que não tinha par. */
    public static final TagKey<Biome> ASSOMBRADO =
            TagKey.create(Registries.BIOME, Thaumcraft.id("book_spooky"));

    /** As catorze, na ordem do original. */
    public static final List<Seção> SEÇÕES = List.of(
            new Seção("beach", BiomeTags.IS_BEACH),
            new Seção("forest", BiomeTags.IS_FOREST),
            new Seção("hills", BiomeTags.IS_HILL),
            new Seção("mesa", BiomeTags.IS_BADLANDS),
            new Seção("mountain", BiomeTags.IS_MOUNTAIN),
            new Seção("plains", doRito("plains")),
            new Seção("sandy", doRito("desert")),
            new Seção("snowy", doRito("frozen")),
            new Seção("swamp", doRito("swamp")),
            new Seção("wasteland", doRito("wasteland")),
            new Seção("river", BiomeTags.IS_RIVER),
            new Seção("ocean", BiomeTags.IS_OCEAN),
            new Seção("spooky", ASSOMBRADO),
            new Seção("magical", doRito("magical")));

    /** E a décima quinta, que não é do original: o que sobrou. */
    public static final Seção RESTO = new Seção("other", BiomeTags.IS_OVERWORLD);

    /** Uma folha do livro: o bioma dela e a seção onde ela está. */
    public record Página(ResourceKey<Biome> qual, Seção seção) {
        public Component nome() {
            return Component.translatable(this.qual().identifier().toLanguageKey("biome"));
        }
    }

    private BiomeCatalogue() {
    }

    /**
     * O livro inteiro, folha a folha.
     *
     * <p>Cada seção traz os seus biomas <b>na ordem do registro</b>, que é a única ordem estável que há: a
     * do original era a ordem em que os mods tinham enchido o dicionário, e essa mudava com a lista de mods.
     *
     * <p>Um bioma de duas seções aparece <b>duas vezes</b>, como no original — e é de propósito: quem procura
     * a praia nevada acha-a na praia e acha-a na neve.
     */
    public static List<Página> páginas(HolderLookup.RegistryLookup<Biome> biomas) {
        List<Página> tudo = new ArrayList<>();
        Set<ResourceKey<Biome>> nomeados = new LinkedHashSet<>();

        for (Seção seção : SEÇÕES) {
            var rol = biomas.get(seção.rótulo()).orElse(null);
            if (rol == null) continue;
            biomas.listElementIds().forEach(qual -> {
                if (!rol.contains(biomas.getOrThrow(qual))) return;
                tudo.add(new Página(qual, seção));
                nomeados.add(qual);
            });
        }

        var doMundoDeCima = biomas.get(RESTO.rótulo()).orElse(null);
        if (doMundoDeCima != null) {
            biomas.listElementIds().forEach(qual -> {
                if (nomeados.contains(qual)) return;
                if (!doMundoDeCima.contains(biomas.getOrThrow(qual))) return;
                tudo.add(new Página(qual, RESTO));
            });
        }
        return tudo;
    }

    /** Onde cada seção começa, para os marcadores da beira da folha. */
    public static List<Integer> marcadores(List<Página> páginas) {
        List<Integer> onde = new ArrayList<>();
        Seção anterior = null;
        for (int i = 0; i < páginas.size(); i++) {
            Seção qual = páginas.get(i).seção();
            if (!qual.equals(anterior)) {
                onde.add(i);
                anterior = qual;
            }
        }
        return onde;
    }

    /** Em que folha está aquele bioma, ou a primeira se ele não estiver no livro. */
    public static int folhaDe(List<Página> páginas, @Nullable ResourceKey<Biome> qual) {
        if (qual == null) return 0;
        for (int i = 0; i < páginas.size(); i++) {
            if (páginas.get(i).qual().equals(qual)) return i;
        }
        return 0;
    }
}
