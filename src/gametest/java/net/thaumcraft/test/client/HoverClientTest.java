package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.baubles.Baubles;
import net.thaumcraft.item.JarContents;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

/** O arreio taumostático: pairando com os anéis e as faíscas, o mostrador de Potentia e a tela do jarro. */
public class HoverClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode survival");
            server.runCommand("time set midnight");
            server.runCommand("fill ~-3 ~ ~2 ~3 ~4 ~2 minecraft:stone");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                ItemStack jar = new ItemStack(TCBlocks.JAR);
                jar.set(TCComponents.JAR_CONTENTS, JarContents.of(Aspects.ENERGY, 40, null));
                ItemStack harness = new ItemStack(TCItems.HOVER_HARNESS);
                harness.set(TCComponents.HARNESS_JAR, jar);
                player.setItemSlot(EquipmentSlot.CHEST, harness);
                Baubles.container(player).setItem(Baubles.BELT, new ItemStack(TCItems.HOVER_GIRDLE));
                ItemStack spare = new ItemStack(TCItems.HOVER_HARNESS);
                spare.set(TCComponents.HARNESS_JAR, jar.copy());
                player.getInventory().setItem(0, spare);
                player.getInventory().setItem(1, jar.copy());
            });
            context.waitTicks(10);
            context.getInput().pressKey(net.thaumcraft.client.HoverClient.KEY);
            context.getInput().holdKeyFor(options -> options.keyJump, 10);
            context.waitTicks(10);
            context.runOnClient(c -> c.options.setCameraType(CameraType.THIRD_PERSON_BACK));
            context.waitTicks(10);
            context.takeScreenshot("arreio_pairando_costas");
            context.runOnClient(c -> c.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
            context.waitTicks(10);
            context.takeScreenshot("arreio_pairando_frente");
            context.runOnClient(c -> c.options.setCameraType(CameraType.FIRST_PERSON));
            context.getInput().pressKey(net.thaumcraft.client.HoverClient.KEY);
            context.waitTicks(40);
            context.getInput().pressMouse(org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT);
            context.waitTicks(20);
            context.takeScreenshot("arreio_tela_jarro");
        }
    }
}
