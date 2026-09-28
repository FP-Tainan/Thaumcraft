package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.arcana.ArcanaBlocks;
import net.thaumcraft.arcana.ArcanaItems;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.InscriptionTableBlockEntity;
import net.thaumcraft.arcana.Modifiers;
import net.thaumcraft.arcana.Shapes;

/**
 * A Mesa de Inscrição vista, nos três estados que interessam: a frase que fecha, a que não fecha e a vazia.
 *
 * <p>A foto da que não fecha é a que vale mais: é ela que mostra a mesa <b>dizendo por quê</b>, que é o que
 * transforma a gramática do ramo numa coisa que se aprende jogando.
 */
public class ArcanaInscriptionClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("tp @p 0 -59 0 0 0");

            BlockPos onde = new BlockPos(0, -59, 2);

            // 1) a frase vazia
            abre(context, server, onde, mesa -> {
            });
            context.takeScreenshot("aa_mesa_vazia");

            // 2) a que fecha: Projétil, depois Área com Dano de Fogo e dois Danos
            abre(context, server, onde, mesa -> {
                mesa.setItem(0, new ItemStack(ArcanaItems.itemOf(Shapes.PROJECTILE)));
                mesa.setItem(1, new ItemStack(ArcanaItems.itemOf(Modifiers.SPEED)));
                mesa.setItem(2, new ItemStack(ArcanaItems.itemOf(Shapes.AOE)));
                mesa.setItem(3, new ItemStack(ArcanaItems.itemOf(Essences.FIRE_DAMAGE)));
                mesa.setItem(4, new ItemStack(ArcanaItems.itemOf(Modifiers.DAMAGE)));
                mesa.setItem(5, new ItemStack(ArcanaItems.itemOf(Modifiers.DAMAGE)));
            });
            context.takeScreenshot("aa_mesa_boa");

            // 3) e a que não fecha: uma Zona no fim da frase
            abre(context, server, onde, mesa -> {
                mesa.setItem(0, new ItemStack(ArcanaItems.itemOf(Shapes.ZONE)));
                mesa.setItem(1, new ItemStack(ArcanaItems.itemOf(Essences.FIRE_DAMAGE)));
            });
            context.takeScreenshot("aa_mesa_ruim");
        }
    }

    /** Põe a mesa, escreve nela o que se pediu, e abre a tela. */
    private static void abre(ClientGameTestContext context,
                             net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
                             BlockPos onde,
                             java.util.function.Consumer<InscriptionTableBlockEntity> escreve) {
        server.runOnServer(s -> {
            var jogador = s.getPlayerList().getPlayers().getFirst();
            var level = jogador.level();
            level.setBlockAndUpdate(onde, ArcanaBlocks.INSCRIPTION_TABLE.defaultBlockState());
            if (level.getBlockEntity(onde) instanceof InscriptionTableBlockEntity mesa) {
                mesa.clearContent();
                escreve.accept(mesa);
                mesa.reread();
                jogador.openMenu(mesa);
            }
        });
        context.waitTicks(20);
    }
}
