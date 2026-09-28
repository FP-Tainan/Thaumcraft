package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.arcana.ArcanaItems;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Modifiers;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellItem;

import java.util.List;

/**
 * Os feitiços vistos: a frase escrita no item, com a Forma, as Essências e os Modificadores em linha.
 */
public class ArcanaSpellClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");

            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                var mochila = jogador.getInventory();

                // um feitiço de cada Forma, e um de duas etapas para se ver a frase inteira
                mochila.setItem(0, SpellItem.write(new ItemStack(ArcanaItems.SPELL),
                        Spell.of(Shapes.SELF, Essences.HEAL)));
                mochila.setItem(1, SpellItem.write(new ItemStack(ArcanaItems.SPELL),
                        Spell.of(Shapes.TOUCH, Essences.FIRE_DAMAGE)));
                mochila.setItem(2, SpellItem.write(new ItemStack(ArcanaItems.SPELL),
                        Spell.of(Shapes.AOE, Essences.FROST_DAMAGE)));
                mochila.setItem(3, SpellItem.write(new ItemStack(ArcanaItems.SPELL),
                        Spell.of(Shapes.TOUCH, Essences.DIG)));
                mochila.setItem(4, SpellItem.write(new ItemStack(ArcanaItems.SPELL),
                        Spell.of(Shapes.TOUCH, Essences.LIGHT)));
                mochila.setItem(5, new ItemStack(ArcanaItems.SPELL));

                // e o de duas etapas, com modificadores, que é o que mostra a gramática inteira
                mochila.setItem(6, SpellItem.write(new ItemStack(ArcanaItems.SPELL), new Spell(List.of(
                        new Spell.Stage(Shapes.TOUCH, List.of(Essences.FIRE_DAMAGE),
                                List.of(Modifiers.DAMAGE, Modifiers.DAMAGE)),
                        new Spell.Stage(Shapes.AOE, List.of(Essences.LIGHT),
                                List.of(Modifiers.DURATION))))));
                jogador.containerMenu.broadcastChanges();
            });
            context.waitTicks(30);
            context.takeScreenshot("aa_feiticos");

            // e a dica do de duas etapas, que é onde a frase se lê
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("aa_mochila");
        }
    }
}
