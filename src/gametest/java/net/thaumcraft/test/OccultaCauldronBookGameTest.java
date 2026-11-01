package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.thaumcraft.occulta.CauldronBookItem;
import net.thaumcraft.occulta.OccultaItems;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Os <b>Cozimentos das Bruxas</b>: o manual do ofício, e as suas duzentas marcas.
 *
 * <p>Este livro é quase todo <b>texto com marcas</b>, e um texto com marcas erra calado: um atalho que
 * aponte para um capítulo que não existe abre uma folha em branco, e uma coisa com nome errado desenha um
 * buraco. Nenhuma das duas coisas rebenta, e por isso nenhuma das duas se vê sem olhar.
 *
 * <p>Esta prova <b>anda o livro inteiro</b>, nos dois idiomas, e pergunta de cada marca se ela leva a
 * alguma parte: cada {@code [url=]}, cada {@code [next=]} e cada {@code [template=]} têm de nomear um
 * capítulo que exista, e cada {@code [stack=]} tem de nomear uma coisa que exista.
 */
public class OccultaCauldronBookGameTest {
    /** As marcas que levam a outro capítulo, e a que nomeia uma coisa. */
    private static final Pattern ATALHO = Pattern.compile("\\[url=([^\\s\\]]+)");
    private static final Pattern SEGUINTE = Pattern.compile("\\[next=([^\\s\\]|]+)");
    private static final Pattern MOLDE = Pattern.compile("\\[template=([a-z0-9]+)");
    private static final Pattern COISA = Pattern.compile("\\[stack=([a-z0-9_]+:[a-z0-9_]+)");

    /** Lê um arquivo de idioma do próprio jar, que é de onde o jogo o lê. */
    private static Map<String, String> idioma(String qual) {
        Map<String, String> tudo = new LinkedHashMap<>();
        try (InputStream dentro = OccultaCauldronBookGameTest.class
                .getResourceAsStream("/assets/thaumcraft/lang/" + qual + ".json")) {
            if (dentro == null) return tudo;
            String bruto = new String(dentro.readAllBytes(), StandardCharsets.UTF_8);
            var json = com.google.gson.JsonParser.parseString(bruto).getAsJsonObject();
            for (var entrada : json.entrySet()) {
                tudo.put(entrada.getKey(), entrada.getValue().getAsString());
            }
        } catch (IOException erro) {
            throw new IllegalStateException(erro);
        }
        return tudo;
    }

    /** Só o que é do livro, com a raiz tirada. */
    private static Map<String, String> livro(String qual) {
        Map<String, String> só = new LinkedHashMap<>();
        for (var entrada : idioma(qual).entrySet()) {
            if (!entrada.getKey().startsWith(CauldronBookItem.RAIZ + ".")) continue;
            só.put(entrada.getKey().substring(CauldronBookItem.RAIZ.length() + 1), entrada.getValue());
        }
        return só;
    }

    /** <b>O livro existe nos dois idiomas</b>, e com os mesmos capítulos. */
    @GameTest(maxTicks = 20)
    public void theBookIsWrittenInBothTongues(GameTestHelper helper) {
        var pt = livro("pt_br");
        var en = livro("en_us");
        if (pt.size() < 150) {
            helper.fail("o livro tem quase duzentos capítulos: tem " + pt.size());
            return;
        }
        if (pt.size() != en.size()) {
            helper.fail("os dois idiomas têm os mesmos capítulos: pt " + pt.size() + ", en " + en.size());
            return;
        }
        List<String> faltam = en.keySet().stream().filter(qual -> !pt.containsKey(qual)).toList();
        if (!faltam.isEmpty()) {
            helper.fail("falta em português: " + String.join(", ", faltam));
            return;
        }
        if (!pt.containsKey(CauldronBookItem.PRIMEIRA)) {
            helper.fail("e o índice, por onde ele abre, está lá");
        }
        helper.succeed();
    }

    /**
     * <b>Nenhum atalho leva a lugar nenhum.</b>
     *
     * <p>É o que esta prova existe para apanhar: um capítulo renomeado numa língua e não na outra, ou um
     * rótulo traduzido sem o alvo em claro — e o atalho passa a abrir uma folha em branco.
     */
    @GameTest(maxTicks = 20)
    public void everyLinkLeadsSomewhere(GameTestHelper helper) {
        List<String> erros = new ArrayList<>();
        for (String qual : List.of("pt_br", "en_us")) {
            var capítulos = livro(qual);
            for (var entrada : capítulos.entrySet()) {
                confere(erros, qual, entrada, ATALHO, capítulos.keySet(), "atalho");
                confere(erros, qual, entrada, SEGUINTE, capítulos.keySet(), "seta");
                confere(erros, qual, entrada, MOLDE, capítulos.keySet(), "molde");
            }
        }
        if (!erros.isEmpty()) helper.fail(String.join("; ", erros.subList(0, Math.min(6, erros.size()))));
        else helper.succeed();
    }

    private static void confere(List<String> erros, String idioma, Map.Entry<String, String> capítulo,
                                Pattern qual, java.util.Set<String> existem, String oquê) {
        Matcher acha = qual.matcher(capítulo.getValue());
        while (acha.find()) {
            String alvo = acha.group(1).split("\\|")[0];
            if (existem.contains(alvo)) continue;
            erros.add(idioma + " " + capítulo.getKey() + ": " + oquê + " para " + alvo);
        }
    }

    /**
     * <b>Toda coisa desenhada no livro existe.</b>
     *
     * <p>O livro veio de 2014, quando um item era um número com um metadado atrás. Cada uma dessas
     * referências teve de ser traduzida à mão para um item de hoje, e uma traduzida errado deixa um
     * buraco na página em vez de uma panela.
     */
    @GameTest(maxTicks = 20)
    public void everyThingDrawnInItExists(GameTestHelper helper) {
        List<String> erros = new ArrayList<>();
        int quantas = 0;
        for (String qual : List.of("pt_br", "en_us")) {
            for (var entrada : livro(qual).entrySet()) {
                Matcher acha = COISA.matcher(entrada.getValue());
                while (acha.find()) {
                    quantas++;
                    Identifier qualCoisa = Identifier.tryParse(acha.group(1));
                    if (qualCoisa != null && BuiltInRegistries.ITEM.getValue(qualCoisa) != Items.AIR) {
                        continue;
                    }
                    erros.add(qual + " " + entrada.getKey() + ": " + acha.group(1));
                }
            }
        }
        if (quantas < 200) {
            helper.fail("o livro desenha centenas de coisas: achou " + quantas);
            return;
        }
        if (!erros.isEmpty()) helper.fail(String.join("; ", erros.subList(0, Math.min(6, erros.size()))));
        else helper.succeed();
    }

    /** <b>E o livro faz-se</b>: um livro, tinta preta, uma pena e um torrão de terra. */
    @GameTest(maxTicks = 40)
    public void theBookIsMadeOfDirtAndInk(GameTestHelper helper) {
        ItemStack nada = ItemStack.EMPTY;
        var entrada = CraftingInput.of(3, 2, List.of(
                nada, new ItemStack(Items.DIRT), nada,
                new ItemStack(Items.DYE.black()), new ItemStack(Items.BOOK), new ItemStack(Items.FEATHER)));
        var saiu = helper.getLevel().recipeAccess()
                .getRecipeFor(RecipeType.CRAFTING, entrada, helper.getLevel());
        if (saiu.isEmpty()) {
            helper.fail("a receita do livro devia estar lá");
            return;
        }
        if (!saiu.get().value().assemble(entrada).is(OccultaItems.CAULDRON_BOOK)) {
            helper.fail("e o que sai dela é o livro");
        }
        helper.succeed();
    }
}
