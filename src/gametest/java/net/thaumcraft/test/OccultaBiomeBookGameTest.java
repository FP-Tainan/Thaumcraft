package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.biome.BiomeBookItem;
import net.thaumcraft.occulta.biome.BiomeCatalogue;
import net.thaumcraft.occulta.biome.BiomeNoteItem;

import java.util.ArrayList;
import java.util.List;

/**
 * O <b>Livro de Biomas</b> e a <b>Nota de Bioma</b>: o escolhedor do ofício, e o recado que sai dele.
 *
 * <p>A prova que carrega a fatia é a de que <b>nenhum bioma fica invisível</b>. O livro do original lia o
 * dicionário de biomas da Forge, que já não existe; aqui ele lê catorze rótulos, e um bioma que não esteja
 * em nenhum deles <b>não tem folha</b> — e sem folha não há nota, e sem nota não há como pedi-lo ao
 * caldeirão. É a espécie de buraco que não rebenta nada e que ninguém vê sem contar.
 */
public class OccultaBiomeBookGameTest {
    /** <b>As catorze seções estão lá</b>, na ordem do original, e nenhuma delas é vazia. */
    @GameTest(maxTicks = 20)
    public void theFourteenSectionsAreThere(GameTestHelper helper) {
        List<String> ordem = List.of("beach", "forest", "hills", "mesa", "mountain", "plains", "sandy",
                "snowy", "swamp", "wasteland", "river", "ocean", "spooky", "magical");
        if (BiomeCatalogue.SEÇÕES.size() != ordem.size()) {
            helper.fail("são catorze seções: são " + BiomeCatalogue.SEÇÕES.size());
            return;
        }
        for (int i = 0; i < ordem.size(); i++) {
            if (!BiomeCatalogue.SEÇÕES.get(i).nome().equals(ordem.get(i))) {
                helper.fail("a seção " + i + " do original é " + ordem.get(i)
                        + ": é " + BiomeCatalogue.SEÇÕES.get(i).nome());
                return;
            }
        }

        var biomas = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        for (var seção : BiomeCatalogue.SEÇÕES) {
            var rol = biomas.get(seção.rótulo()).orElse(null);
            if (rol == null || rol.size() == 0) {
                helper.fail("a seção " + seção.nome() + " não tem bioma nenhum, e o marcador dela é morto");
                return;
            }
        }
        helper.succeed();
    }

    /**
     * <b>E nenhum bioma do mundo de cima fica de fora.</b>
     *
     * <p>É a razão de haver uma décima quinta seção que o original não tinha: em 2014 a lista de biomas era
     * fechada, e hoje é dados.
     */
    @GameTest(maxTicks = 20)
    public void nothingOfTheOverworldIsInvisible(GameTestHelper helper) {
        var biomas = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        var páginas = BiomeCatalogue.páginas(biomas);
        List<ResourceKey<Biome>> tem = páginas.stream().map(BiomeCatalogue.Página::qual).toList();

        var doMundoDeCima = biomas.get(BiomeTags.IS_OVERWORLD).orElseThrow();
        List<String> faltam = new ArrayList<>();
        biomas.listElementIds().forEach(qual -> {
            if (!doMundoDeCima.contains(biomas.getOrThrow(qual))) return;
            if (!tem.contains(qual)) faltam.add(qual.identifier().toString());
        });
        if (!faltam.isEmpty()) {
            helper.fail("sem folha no livro: " + String.join(", ", faltam));
            return;
        }
        // e o inferno não entra: o livro do original só falava do mundo de cima
        if (tem.contains(Biomes.NETHER_WASTES) || tem.contains(Biomes.THE_END)) {
            helper.fail("o livro não fala do inferno nem do fim");
            return;
        }
        if (páginas.size() < 50) {
            helper.fail("o livro tem uma folha por bioma: tem " + páginas.size());
            return;
        }
        helper.succeed();
    }

    /** <b>Os marcadores</b>: um por seção que tenha folha, cada um na primeira folha dela. */
    @GameTest(maxTicks = 20)
    public void everySectionGetsOneBookmark(GameTestHelper helper) {
        var biomas = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        var páginas = BiomeCatalogue.páginas(biomas);
        var marcadores = BiomeCatalogue.marcadores(páginas);

        if (marcadores.size() != BiomeCatalogue.SEÇÕES.size() + 1) {
            helper.fail("um marcador por seção, mais o do resto: são " + marcadores.size());
            return;
        }
        for (int i = 0; i < marcadores.size(); i++) {
            int onde = marcadores.get(i);
            if (onde > 0 && páginas.get(onde - 1).seção().equals(páginas.get(onde).seção())) {
                helper.fail("o marcador " + i + " não está na primeira folha da seção");
                return;
            }
        }
        if (marcadores.get(0) != 0) helper.fail("o primeiro marcador é a primeira folha");
        else helper.succeed();
    }

    /**
     * <b>A nota chama-se pelo bioma</b>, e uma nota em branco tem nome próprio.
     *
     * <p>No original isto é um {@code String.format} com o nome do bioma dentro, e com o bioma nulo ele
     * caía num nome truncado. Aqui a nota em branco tem chave só dela.
     */
    @GameTest(maxTicks = 20)
    public void theNoteIsNamedAfterTheBiome(GameTestHelper helper) {
        ItemStack branca = new ItemStack(OccultaItems.BIOME_NOTE);
        ItemStack doDeserto = BiomeNoteItem.de(OccultaItems.BIOME_NOTE, Biomes.DESERT);

        if (BiomeNoteItem.bioma(branca) != null) {
            helper.fail("uma nota nova não tem bioma escrito");
            return;
        }
        if (!Biomes.DESERT.equals(BiomeNoteItem.bioma(doDeserto))) {
            helper.fail("e uma nota do deserto tem o deserto escrito");
            return;
        }
        // o nome do bioma, qualquer que seja o idioma carregado, tem de estar dentro do nome da nota
        String oDoBioma = net.minecraft.network.chat.Component
                .translatable(Biomes.DESERT.identifier().toLanguageKey("biome")).getString();
        String nome = doDeserto.getHoverName().getString();
        if (nome.equals(branca.getHoverName().getString()) || !nome.contains(oDoBioma)) {
            helper.fail("o nome dela traz o do bioma «" + oDoBioma + "»: é «" + nome + "»");
            return;
        }
        helper.succeed();
    }

    /**
     * <b>E a nota sai do livro com um papel</b>, com o bioma da folha aberta — e o livro volta.
     *
     * <p>É o {@code RecipeShapelessBiomeCopy} e o {@code getContainerItem} do original, que é o único jeito
     * de fazer uma nota: o bioma que se quer tem de se achar no livro primeiro.
     */
    @GameTest(maxTicks = 40)
    public void thePaperCopiesThePage(GameTestHelper helper) {
        ItemStack livro = new ItemStack(OccultaItems.BIOME_BOOK);
        var entrada = CraftingInput.of(2, 1, List.of(livro, new ItemStack(Items.PAPER)));

        // um livro em branco não copia nada
        if (helper.getLevel().recipeAccess()
                .getRecipeFor(RecipeType.CRAFTING, entrada, helper.getLevel()).isPresent()) {
            helper.fail("um livro que não está aberto em nenhum bioma não dá nota nenhuma");
            return;
        }

        BiomeBookItem.abre(livro, Biomes.JUNGLE);
        var abertoNaSelva = CraftingInput.of(2, 1, List.of(livro, new ItemStack(Items.PAPER)));
        var receita = helper.getLevel().recipeAccess()
                .getRecipeFor(RecipeType.CRAFTING, abertoNaSelva, helper.getLevel()).orElse(null);
        if (receita == null) {
            helper.fail("a receita da nota devia estar lá");
            return;
        }
        ItemStack saiu = receita.value().assemble(abertoNaSelva);
        if (!saiu.is(OccultaItems.BIOME_NOTE) || !Biomes.JUNGLE.equals(BiomeNoteItem.bioma(saiu))) {
            helper.fail("o que sai é uma nota da selva: é " + saiu);
            return;
        }

        var sobra = receita.value().getRemainingItems(abertoNaSelva);
        if (!sobra.get(0).is(OccultaItems.BIOME_BOOK)
                || !Biomes.JUNGLE.equals(BiomeBookItem.bioma(sobra.get(0)))) {
            helper.fail("e o livro volta, com a folha onde estava");
            return;
        }
        helper.succeed();
    }
}
