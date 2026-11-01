package net.thaumcraft.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * O <b>desenho de toda coisa do mod</b>: o guarda que percorre o jar.
 *
 * <p>Um item sem desenho não rebenta nada. Ele aparece na mão como o <b>quadriculado preto e roxo</b> de
 * textura perdida, e nenhuma prova de servidor dá por isso — o item existe, tem nome, faz o que tem de
 * fazer. Só se vê abrindo o jogo e olhando.
 *
 * <p>Este porte tem mais de <b>novecentos</b> itens. Olhar para novecentos ícones à mão não é um plano, e
 * olhar para eles <b>uma vez</b> não chega: basta uma fatia renomear um arquivo para um deles voltar a
 * ficar quadriculado.
 *
 * <p>O que esta prova faz é andar o caminho que o jogo anda — do item para a <b>definição</b>
 * ({@code assets/thaumcraft/items/*.json}), da definição para o <b>modelo</b>, e do modelo para as
 * <b>texturas</b> — e dizer o primeiro degrau que falta.
 */
public class AssetsGameTest {
    /** Até quantas faltas se escrevem no erro antes de ele ficar ilegível. */
    private static final int MOSTRA = 8;

    /** Lê um arquivo do jar, ou nada. */
    private static String lê(String caminho) {
        try (InputStream dentro = AssetsGameTest.class.getResourceAsStream(caminho)) {
            if (dentro == null) return null;
            return new String(dentro.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception erro) {
            return null;
        }
    }

    private static boolean existe(String caminho) {
        try (InputStream dentro = AssetsGameTest.class.getResourceAsStream(caminho)) {
            return dentro != null;
        } catch (Exception erro) {
            return false;
        }
    }

    /** O caminho de um arquivo de assets, a partir de um nome com espaço de nomes. */
    private static String assets(Identifier qual, String meio, String ponta) {
        return "/assets/" + qual.getNamespace() + "/" + meio + "/" + qual.getPath() + ponta;
    }

    /**
     * Todo modelo que a definição nomeia, a qualquer fundura.
     *
     * <p>São <b>duas</b> chaves e não uma: o {@code model} de sempre, e o {@code base} dos itens de
     * desenhista próprio — o Tauminômetro e os aglomerados de cristal desenham-se em código, e o que a
     * definição dá é o modelo de base que o desenhista monta por cima.
     */
    private static void modelosDe(JsonElement onde, Set<String> achados) {
        if (onde.isJsonObject()) {
            JsonObject objeto = onde.getAsJsonObject();
            for (var entrada : objeto.entrySet()) {
                boolean nomeia = entrada.getKey().equals("model") || entrada.getKey().equals("base");
                if (nomeia && entrada.getValue().isJsonPrimitive()) {
                    achados.add(entrada.getValue().getAsString());
                    continue;
                }
                modelosDe(entrada.getValue(), achados);
            }
            return;
        }
        if (onde.isJsonArray()) {
            for (JsonElement cada : onde.getAsJsonArray()) modelosDe(cada, achados);
        }
    }

    /** E toda textura que um modelo nomeia, seguindo os pais dele. */
    private static void texturasDe(String modelo, Set<String> achadas, Set<String> jáVistos) {
        if (!jáVistos.add(modelo)) return;
        Identifier qual = Identifier.parse(modelo);
        // os modelos do próprio jogo não estão neste jar, e não são deste guarda
        if (!qual.getNamespace().equals(Thaumcraft.MOD_ID)) return;
        String bruto = lê(assets(qual, "models", ".json"));
        if (bruto == null) {
            achadas.add("!modelo:" + modelo);
            return;
        }
        JsonObject json = JsonParser.parseString(bruto).getAsJsonObject();
        if (json.has("textures")) {
            for (var entrada : json.getAsJsonObject("textures").entrySet()) {
                if (!entrada.getValue().isJsonPrimitive()) continue;
                String valor = entrada.getValue().getAsString();
                // um valor que começa por cardinal é um atalho para outra chave do mesmo modelo
                if (valor.startsWith("#")) continue;
                achadas.add(valor);
            }
        }
        if (json.has("parent")) texturasDe(json.get("parent").getAsString(), achadas, jáVistos);
    }

    /**
     * <b>Toda coisa do mod tem desenho.</b>
     *
     * <p>Do item à definição, da definição ao modelo, do modelo à textura — e o primeiro degrau que
     * faltar é o que a prova diz.
     */
    @GameTest(maxTicks = 20)
    public void everythingHasAPicture(GameTestHelper helper) {
        List<String> faltam = new ArrayList<>();
        int quantos = 0;

        for (var item : BuiltInRegistries.ITEM) {
            Identifier qual = BuiltInRegistries.ITEM.getKey(item);
            if (!qual.getNamespace().equals(Thaumcraft.MOD_ID)) continue;
            quantos++;

            String bruto = lê(assets(qual, "items", ".json"));
            if (bruto == null) {
                faltam.add(qual.getPath() + ": sem definição");
                continue;
            }

            Set<String> modelos = new LinkedHashSet<>();
            modelosDe(JsonParser.parseString(bruto), modelos);
            if (modelos.isEmpty()) {
                faltam.add(qual.getPath() + ": a definição não nomeia modelo nenhum");
                continue;
            }

            Set<String> texturas = new LinkedHashSet<>();
            Set<String> vistos = new LinkedHashSet<>();
            for (String modelo : modelos) texturasDe(modelo, texturas, vistos);

            for (String textura : texturas) {
                if (textura.startsWith("!modelo:")) {
                    faltam.add(qual.getPath() + ": sem o modelo " + textura.substring(8));
                    continue;
                }
                Identifier onde = Identifier.parse(textura);
                if (!onde.getNamespace().equals(Thaumcraft.MOD_ID)) continue;
                if (existe(assets(onde, "textures", ".png"))) continue;
                faltam.add(qual.getPath() + ": sem a textura " + textura);
            }
        }

        if (quantos < 500) {
            helper.fail("o mod tem centenas de itens: contei " + quantos);
            return;
        }
        if (!faltam.isEmpty()) {
            helper.fail(faltam.size() + " sem desenho: "
                    + String.join("; ", faltam.subList(0, Math.min(MOSTRA, faltam.size()))));
            return;
        }
        helper.succeed();
    }

    /**
     * <b>E todo bloco do mod tem estado de bloco.</b>
     *
     * <p>Um bloco sem {@code blockstate} é o mesmo quadriculado, e com o mesmo silêncio.
     */
    @GameTest(maxTicks = 20)
    public void everyBlockHasABlockstate(GameTestHelper helper) {
        List<String> faltam = new ArrayList<>();
        int quantos = 0;
        for (var bloco : BuiltInRegistries.BLOCK) {
            Identifier qual = BuiltInRegistries.BLOCK.getKey(bloco);
            if (!qual.getNamespace().equals(Thaumcraft.MOD_ID)) continue;
            quantos++;
            if (existe(assets(qual, "blockstates", ".json"))) continue;
            faltam.add(qual.getPath());
        }
        if (quantos < 100) {
            helper.fail("o mod tem centenas de blocos: contei " + quantos);
            return;
        }
        if (!faltam.isEmpty()) {
            helper.fail(faltam.size() + " blocos sem estado: "
                    + String.join(", ", faltam.subList(0, Math.min(MOSTRA, faltam.size()))));
            return;
        }
        helper.succeed();
    }

}
