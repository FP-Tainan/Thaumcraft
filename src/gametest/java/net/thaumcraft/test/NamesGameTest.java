package net.thaumcraft.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Nada do mod pode aparecer com o nome cru da chave.
 *
 * <p>Confere item por item, bloco por bloco e criatura por criatura que a chave de nome existe nos dois idiomas —
 * é o que falha quando alguma coisa nova entra e o texto dela fica para trás.
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
        if (!faltando.isEmpty()) {
            helper.fail(faltando.size() + " sem nome: " + String.join(", ", faltando));
        }
        helper.succeed();
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
