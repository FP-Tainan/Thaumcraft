package net.thaumcraft.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.thaumcraft.occulta.OccultaSounds;

/**
 * Os <b>sons do ofício</b>: os sessenta e quatro do Witchery, com arquivo e tudo.
 *
 * <p>A prova é de três partes: o <b>número</b> — sessenta e quatro, que é quantos o jar de 2014 tinha —, o
 * <b>registro</b> de cada um deles no jogo, e o <b>arquivo</b> de cada som que o {@code sounds.json}
 * promete.
 *
 * <p>A terceira é a que importa. Um som registrado sem arquivo <b>não dá erro nenhum</b>: ele simplesmente
 * não toca, e ninguém descobre até estar jogando. Esta prova descobre.
 */
public class OccultaSoundsGameTest {
    /** Quantos sons o original tinha. */
    public static final int QUANTOS = 64;

    /** O que marca os sons do ofício entre os do Thaumcraft. */
    public static final String MARCA = "occulta.";

    /** Onde o jogo procura um arquivo de som do mod. */
    private static final String PASTA = "/assets/thaumcraft/sounds/";

    /** Eles são todos, e são os sessenta e quatro. */
    @GameTest(maxTicks = 20)
    public void thereAreSixtyFourOfThem(GameTestHelper helper) {
        long achados = BuiltInRegistries.SOUND_EVENT.keySet().stream()
                .filter(id -> id.getNamespace().equals("thaumcraft") && id.getPath().startsWith(MARCA))
                .count();
        if (achados != QUANTOS) {
            helper.fail("são sessenta e quatro sons do ofício, e achei " + achados);
        }
        helper.succeed();
    }

    /**
     * E os que esta fatia foi buscar estão entre eles.
     *
     * <p>Um de cada canto, para a prova dizer alguma coisa quando falhar: o que se faz com as mãos, o que um
     * lugar faz sozinho, o lobisomem, e um bicho que ainda não veio mas já tem voz guardada.
     */
    @GameTest(maxTicks = 20)
    public void theOnesThisSliceWasForAreThere(GameTestHelper helper) {
        confere(helper, "o estalo da armadilha", OccultaSounds.MANTRAP);
        confere(helper, "o clique de armar", OccultaSounds.CLICK);
        confere(helper, "o coração batendo", OccultaSounds.HEARTBEAT);
        confere(helper, "o uivo", OccultaSounds.WOLFMAN_HOWL);
        confere(helper, "o Senhor dos Lobos", OccultaSounds.WOLFMAN_LORD);
        confere(helper, "o riso do diabrete", OccultaSounds.IMP_LAUGH);
        helper.succeed();
    }

    private static void confere(GameTestHelper helper, String oQueÉ,
                                net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> qual) {
        Identifier id = qual.value().location();
        if (!BuiltInRegistries.SOUND_EVENT.containsKey(id)) {
            helper.fail(oQueÉ + " não está registrado: " + id);
        }
    }

    /**
     * <b>E cada um deles tem arquivo.</b>
     *
     * <p>Lê o {@code sounds.json} como o jogo o lê, junta todos os nomes de arquivo que os sons do ofício
     * prometem, e procura cada um deles onde o jogo procuraria.
     */
    @GameTest(maxTicks = 40)
    public void everyOneOfThemHasItsFile(GameTestHelper helper) {
        JsonObject raiz;
        try (InputStream entrada = OccultaSoundsGameTest.class
                .getResourceAsStream("/assets/thaumcraft/sounds.json")) {
            if (entrada == null) {
                helper.fail("o sounds.json do mod está no jar");
                return;
            }
            raiz = JsonParser.parseReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        } catch (Exception erro) {
            helper.fail("o sounds.json se lê: " + erro);
            return;
        }

        List<String> faltam = new ArrayList<>();
        int quantos = 0;
        int arquivos = 0;
        for (String evento : raiz.keySet()) {
            if (!evento.startsWith(MARCA)) continue;
            quantos++;
            Identifier id = Identifier.fromNamespaceAndPath("thaumcraft", evento);
            if (!BuiltInRegistries.SOUND_EVENT.containsKey(id)) {
                faltam.add("o evento " + evento + ", que está no json e não no jogo");
            }
            for (var som : raiz.getAsJsonObject(evento).getAsJsonArray("sounds")) {
                String nome = som.getAsString();
                arquivos++;
                String onde = PASTA + nome.substring(nome.indexOf(':') + 1) + ".ogg";
                if (OccultaSoundsGameTest.class.getResource(onde) == null) {
                    faltam.add("o arquivo " + onde);
                }
            }
        }

        if (quantos != QUANTOS) helper.fail("o json promete sessenta e quatro, e achei " + quantos);
        if (arquivos < QUANTOS) helper.fail("um arquivo por evento, no mínimo, e achei " + arquivos);
        if (!faltam.isEmpty()) {
            helper.fail("faltam " + faltam.size() + ", a começar por " + faltam.getFirst());
        }
        helper.succeed();
    }
}
