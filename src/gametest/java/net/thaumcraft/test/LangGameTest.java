package net.thaumcraft.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * O português do mod tem de estar todo em português.
 *
 * <p>O pt_BR que veio com o Thaumcraft 4.2.3.5 era um trabalho pela metade: páginas inteiras do Thaumonomicon
 * ficaram em inglês, e algumas cortavam no meio da frase — a primeira metade traduzida, a segunda não. Aqui se
 * confere, palavra por palavra, que não sobrou nada em inglês.
 */
public class LangGameTest {
    /** Palavras que só existem em inglês: duas delas num texto e aquilo não é português. */
    private static final Pattern INGLES = Pattern.compile(
            "\b(the|they|them|you|your|and|with|that|will|this|from|have|their|which|when|while|there|these|does|"
                    + "been|being|would|could|should|about|into|other|than|then|what|where|who|why)\b",
            Pattern.CASE_INSENSITIVE);

    /** Os nomes próprios e os formatos que o original também deixa como estão. */
    private static final Set<String> DEIXA = Set.of(
            "golemthreat.5.text", "item.thaumcraft.wand.focused", "tc.aspect.amount");

    @GameTest
    public void theBrazilianFileIsFullyTranslated(GameTestHelper helper) {
        JsonObject pt = read("assets/thaumcraft/lang/pt_br.json");
        if (pt == null) helper.fail("não achei o pt_br.json");
        List<String> ingles = new ArrayList<>();
        for (var entrada : pt.entrySet()) {
            if (DEIXA.contains(entrada.getKey())) continue;
            String texto = entrada.getValue().getAsString();
            if (texto.length() < 12) continue;
            var busca = INGLES.matcher(texto);
            int achou = 0;
            while (busca.find()) achou++;
            if (achou >= 2) ingles.add(entrada.getKey());
        }
        if (!ingles.isEmpty()) helper.fail(ingles.size() + " textos ainda em inglês: " + String.join(", ", ingles));
        helper.succeed();
    }

    private static JsonObject read(String caminho) {
        try (var stream = LangGameTest.class.getClassLoader().getResourceAsStream(caminho)) {
            if (stream == null) return null;
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception erro) {
            return null;
        }
    }
}
