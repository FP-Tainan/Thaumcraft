package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.gametest.framework.GameTestHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * As figuras de quadros do mosaico têm de trazer o {@code .mcmeta} delas.
 *
 * <p>Uma figura animada é uma tira alta: os quadros vêm um debaixo do outro, e é o {@code .mcmeta} que diz ao
 * jogo que aquilo são quadros e não um desenho comprido. Sem ele o jogo espreme a tira toda dentro do quadrado
 * do item, e sai a pele borrada que quem joga viu no Fragmento de Ira, no Carvão Maculado e nos outros quatro.
 *
 * <p>Só se cobram as pastas que vão para o mosaico — {@code item} e {@code block}. As figuras de
 * {@code models}, {@code entity} e {@code misc} são desenhadas à mão por quem as usa, que já sabe onde estão
 * os quadros; os originais dessas também não trazem {@code .mcmeta}.
 */
public class TextureFramesGameTest {
    @GameTest
    public void everyFrameSheetHasItsMcmeta(GameTestHelper helper) {
        var mod = FabricLoader.getInstance().getModContainer("thaumcraft").orElse(null);
        if (mod == null) helper.fail("não se achou o mod para lhe ler as figuras");

        List<String> sem = new ArrayList<>();
        int vistas = 0;
        for (String pasta : new String[]{"item", "block"}) {
            Path raiz = mod.findPath("assets/thaumcraft/textures/" + pasta).orElse(null);
            if (raiz == null) continue;
            try (Stream<Path> figuras = Files.walk(raiz)) {
                for (Path figura : figuras.toList()) {
                    String nome = figura.getFileName().toString();
                    if (!nome.endsWith(".png")) continue;
                    vistas++;
                    int[] tamanho = png(figura);
                    if (tamanho == null) continue;
                    int largura = tamanho[0];
                    int altura = tamanho[1];
                    // uma tira de quadros: mais alta que larga, e a altura cabe a largura um número certo de vezes
                    if (largura == 0 || altura <= largura || altura % largura != 0) continue;
                    if (Files.exists(figura.resolveSibling(nome + ".mcmeta"))) continue;
                    sem.add(pasta + "/" + nome + " (" + (altura / largura) + " quadros)");
                }
            } catch (IOException erro) {
                helper.fail("não se conseguiu ler as figuras de " + pasta + ": " + erro);
            }
        }

        if (vistas == 0) helper.fail("não se leu figura nenhuma: o caminho das figuras mudou?");
        if (!sem.isEmpty()) helper.fail("há " + sem.size() + " tiras de quadros sem .mcmeta: " + String.join(", ", sem));
        helper.succeed();
    }

    /** A largura e a altura de um PNG, que moram nos oito bytes do IHDR, logo depois da assinatura. */
    private static int[] png(Path figura) {
        try {
            byte[] bytes = new byte[24];
            try (var entrada = Files.newInputStream(figura)) {
                if (entrada.readNBytes(bytes, 0, 24) < 24) return null;
            }
            return new int[]{inteiro(bytes, 16), inteiro(bytes, 20)};
        } catch (IOException erro) {
            return null;
        }
    }

    private static int inteiro(byte[] bytes, int onde) {
        return ((bytes[onde] & 0xFF) << 24) | ((bytes[onde + 1] & 0xFF) << 16)
                | ((bytes[onde + 2] & 0xFF) << 8) | (bytes[onde + 3] & 0xFF);
    }
}
