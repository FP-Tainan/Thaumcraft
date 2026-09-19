package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Todo item e bloco do mod tem de ter nome nos idiomas: nada de chave crua no inventário. */
public class NamesClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (var singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            context.runOnClient(minecraft -> {
                List<String> semNome = new ArrayList<>();
                for (var item : BuiltInRegistries.ITEM) {
                    var id = BuiltInRegistries.ITEM.getKey(item);
                    if (!id.getNamespace().equals("thaumcraft")) continue;
                    // o que vale é o nome que aparece: alguns itens montam o nome na mão (o anel de aprendiz, a varinha)
                    String nome = new ItemStack(item).getHoverName().getString();
                    if (nome.contains("thaumcraft.")) semNome.add(id.getPath() + " -> " + nome);
                }
                for (var block : BuiltInRegistries.BLOCK) {
                    var id = BuiltInRegistries.BLOCK.getKey(block);
                    if (!id.getNamespace().equals("thaumcraft")) continue;
                    String nome = block.getName().getString();
                    if (nome.contains("thaumcraft.")) semNome.add(id.getPath() + " -> " + nome);
                }
                if (!semNome.isEmpty()) {
                    System.out.println("[NOMES] faltando (" + semNome.size() + "): " + String.join(", ", semNome));
                    throw new AssertionError("há " + semNome.size() + " coisas sem nome: " + String.join(", ", semNome));
                }
                System.out.println("[NOMES] tudo nomeado");
            });
        }
    }
}
