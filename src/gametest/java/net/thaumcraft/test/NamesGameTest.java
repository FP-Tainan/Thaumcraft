package net.thaumcraft.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.thaumcraft.research.Page;
import net.thaumcraft.research.Research;
import net.thaumcraft.research.ResearchCategories;
import net.thaumcraft.research.Researches;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Nada do mod pode aparecer com o nome cru da chave.
 *
 * <p>Confere item por item, bloco por bloco e criatura por criatura, e também o que o Thaumonomicon mostra: o nome
 * de cada aba, o de cada pesquisa e o texto de cada página escrita. É o que falha quando alguma coisa nova entra e
 * o texto dela fica para trás.
 */
public class NamesGameTest {
    /** Os anéis de aprendiz montam o nome na hora, com o aspecto: a chave deles é outra. */
    private static final java.util.Set<String> MONTAM_O_NOME = java.util.Set.of(
            "item.thaumcraft.apprentice_ring_air", "item.thaumcraft.apprentice_ring_earth",
            "item.thaumcraft.apprentice_ring_fire", "item.thaumcraft.apprentice_ring_water",
            "item.thaumcraft.apprentice_ring_order", "item.thaumcraft.apprentice_ring_entropy");

    @GameTest
    public void everythingHasAFriendlyName(GameTestHelper helper) {
        JsonObject en = read("assets/thaumcraft/lang/en_us.json");
        JsonObject pt = read("assets/thaumcraft/lang/pt_br.json");
        if (en == null || pt == null) helper.fail("não achei os arquivos de idioma");

        List<String> faltando = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            var id = BuiltInRegistries.ITEM.getKey(item);
            if (!id.getNamespace().equals("thaumcraft")) continue;
            String chave = new ItemStack(item).getItem().getDescriptionId();
            confere(en, pt, chave, faltando);
        }
        for (Block block : BuiltInRegistries.BLOCK) {
            var id = BuiltInRegistries.BLOCK.getKey(block);
            if (!id.getNamespace().equals("thaumcraft")) continue;
            confere(en, pt, block.getDescriptionId(), faltando);
        }
        for (var type : BuiltInRegistries.ENTITY_TYPE) {
            var id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!id.getNamespace().equals("thaumcraft")) continue;
            confere(en, pt, type.getDescriptionId(), faltando);
        }
        for (var effect : BuiltInRegistries.MOB_EFFECT) {
            var id = BuiltInRegistries.MOB_EFFECT.getKey(effect);
            if (id == null || !id.getNamespace().equals("thaumcraft")) continue;
            confere(en, pt, effect.getDescriptionId(), faltando);
        }
        // os encantamentos e as terras vêm dos dados, então moram no registro do mundo
        var mundo = helper.getLevel().registryAccess();
        mundo.lookupOrThrow(Registries.ENCHANTMENT).listElements().forEach(holder -> {
            var id = holder.key().identifier();
            if (!id.getNamespace().equals("thaumcraft")) return;
            confere(en, pt, "enchantment." + id.getNamespace() + "." + id.getPath(), faltando);
        });
        mundo.lookupOrThrow(Registries.BIOME).listElements().forEach(holder -> {
            var id = holder.key().identifier();
            if (!id.getNamespace().equals("thaumcraft")) return;
            confere(en, pt, "biome." + id.getNamespace() + "." + id.getPath(), faltando);
        });
        if (!faltando.isEmpty()) {
            helper.fail(faltando.size() + " sem nome: " + String.join(", ", faltando));
        }
        helper.succeed();
    }

    /**
     * O livro: cada aba, cada pesquisa que aparece no mapa e cada página escrita.
     *
     * <p>As virtuais ficam de fora porque não se desenham — no original elas também não têm nome; são só degraus
     * entre outras, como o {@code CAP_iron} e o {@code ROD_wood} da varinha.
     */
    @GameTest
    public void theBookHasNoRawKeys(GameTestHelper helper) {
        JsonObject en = read("assets/thaumcraft/lang/en_us.json");
        JsonObject pt = read("assets/thaumcraft/lang/pt_br.json");
        if (en == null || pt == null) helper.fail("não achei os arquivos de idioma");

        List<String> faltando = new ArrayList<>();
        for (String categoria : ResearchCategories.ALL.keySet()) {
            if (doTeste(categoria)) continue;
            confere(en, pt, "tc.research_category." + categoria, faltando);
        }
        for (Research research : Researches.ALL.values()) {
            if (research.is(Research.Mark.VIRTUAL) || doTeste(research.key())) continue;
            confere(en, pt, "tc.research_name." + research.key(), faltando);
            for (Page page : research.pages()) {
                if (page instanceof Page.Text texto) confere(en, pt, texto.key(), faltando);
                if (page instanceof Page.Concealed escondida) confere(en, pt, escondida.key(), faltando);
            }
        }
        if (!faltando.isEmpty()) {
            helper.fail(faltando.size() + " sem texto no livro: " + String.join(", ", faltando));
        }
        helper.succeed();
    }

    /** O mod de mentira que os testes registram para provar a porta de fora não tem idioma, e nem precisa. */
    private static boolean doTeste(String chave) {
        return chave.startsWith(net.thaumcraft.test.addon.TestAddon.CATEGORY);
    }

    private static void confere(JsonObject en, JsonObject pt, String chave, List<String> faltando) {
        if (MONTAM_O_NOME.contains(chave)) return;
        if (!en.has(chave)) faltando.add(chave + " (en)");
        if (!pt.has(chave)) faltando.add(chave + " (pt)");
    }

    private static JsonObject read(String caminho) {
        try (var stream = NamesGameTest.class.getClassLoader().getResourceAsStream(caminho)) {
            if (stream == null) return null;
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception erro) {
            return null;
        }
    }
}
