package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Todo item do mod tem de ter modelo de verdade.
 *
 * <p>Quem joga viu isto três vezes: um arquivo de item com um erro dentro não derruba o jogo — o jogo resmunga
 * uma linha no registo e desenha o cubo de xadrez roxo e preto no lugar. Foi assim que o Cristal de Marca ficou
 * sem pele, por causa de um {@code "value": null} que a condição não aceita. Este teste apanha o caso todo de
 * uma vez: se o modelo que o jogo acabou por assar é o modelo de falta, é porque o arquivo não passou.
 */
public class ItemModelClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (var singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            context.runOnClient(minecraft -> {
                var modelos = minecraft.getModelManager();
                // o modelo de falta é o que sai de um nome que ninguém registou; é ele que se quer não ver
                var falta = modelos.getItemModel(Identifier.fromNamespaceAndPath("thaumcraft", "nao_existe_de_proposito"));

                List<String> quebrados = new ArrayList<>();
                for (var item : BuiltInRegistries.ITEM) {
                    var id = BuiltInRegistries.ITEM.getKey(item);
                    if (!id.getNamespace().equals("thaumcraft")) continue;
                    // o que se desenha é o que o componente diz, e não o nome do item
                    Identifier qual = new ItemStack(item).get(DataComponents.ITEM_MODEL);
                    if (qual == null) {
                        quebrados.add(id.getPath() + " (sem componente de modelo)");
                        continue;
                    }
                    if (modelos.getItemModel(qual) == falta) quebrados.add(id.getPath() + " -> " + qual);
                }
                if (!quebrados.isEmpty()) {
                    System.out.println("[MODELOS] no cubo roxo (" + quebrados.size() + "): " + String.join(", ", quebrados));
                    throw new AssertionError("há " + quebrados.size() + " itens sem modelo: " + String.join(", ", quebrados));
                }
                System.out.println("[MODELOS] todos os itens do mod têm modelo");
            });
        }
    }
}
