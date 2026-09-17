package net.thaumcraft.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.thaumcraft.Thaumcraft;

/**
 * Todo som que o mod anuncia tem de existir de verdade.
 *
 * <p>Som que falta não estoura o jogo: ele simplesmente não toca, e ninguém percebe até jogar. Por isso
 * esta prova percorre o caminho inteiro — do nome registrado em {@code TCSounds} até o arquivo {@code .ogg}
 * dentro do jar.
 */
public class SoundGameTest {
    @GameTest
    public void everySoundHasAFileBehindIt(GameTestHelper helper) {
        JsonObject index;
        try (InputStream stream = SoundGameTest.class.getResourceAsStream("/assets/thaumcraft/sounds.json")) {
            if (stream == null) {
                helper.fail("falta o sounds.json do mod");
                return;
            }
            index = JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception problem) {
            helper.fail("não deu para ler o sounds.json: " + problem);
            return;
        }

        int ours = 0;
        for (SoundEvent sound : BuiltInRegistries.SOUND_EVENT) {
            Identifier id = BuiltInRegistries.SOUND_EVENT.getKey(sound);
            if (id == null || !id.getNamespace().equals(Thaumcraft.MOD_ID)) continue;
            ours++;

            if (!index.has(id.getPath())) {
                helper.fail("o som " + id.getPath() + " está registrado mas não está no sounds.json");
                continue;
            }
            JsonArray files = index.getAsJsonObject(id.getPath()).getAsJsonArray("sounds");
            if (files == null || files.isEmpty()) {
                helper.fail("o som " + id.getPath() + " não aponta para arquivo nenhum");
                continue;
            }
            for (var entry : files) {
                String name = entry.getAsString().replace("thaumcraft:", "");
                String path = "/assets/thaumcraft/sounds/" + name + ".ogg";
                try (InputStream file = SoundGameTest.class.getResourceAsStream(path)) {
                    if (file == null) helper.fail("falta o arquivo " + path);
                } catch (Exception problem) {
                    helper.fail("não deu para abrir " + path + ": " + problem);
                }
            }
        }

        if (ours < 10) helper.fail("o mod devia ter mais sons que isso: " + ours);

        // e o contrário: nada de sobra no sounds.json sem registro por trás
        for (String name : index.keySet()) {
            if (BuiltInRegistries.SOUND_EVENT.getValue(Thaumcraft.id(name)) == null) {
                helper.fail("o sounds.json cita " + name + ", que o mod não registra");
            }
        }
        helper.succeed();
    }
}
