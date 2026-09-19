package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.entity.PechEntity;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;

/** Os três pechs (coletor de espada, mago de varinha, caçador de arco) e a tela de troca. */
public class PechClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("tp @p ~ ~ ~ 180 10");
            final int[] ids = new int[1];
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos base = player.blockPosition().north(4);
                ItemStack wand = new ItemStack(TCItems.WAND);
                wand.set(TCComponents.WAND_FOCUS, "pech");
                ItemStack[] held = {new ItemStack(Items.IRON_SWORD), wand, new ItemStack(Items.BOW)};
                for (int i = 0; i < 3; i++) {
                    PechEntity pech = TCEntities.PECH.create(level, EntitySpawnReason.COMMAND);
                    pech.snapTo(base.getX() + 0.5 + (i - 1) * 2, base.getY(), base.getZ() + 0.5, 0.0f, 0.0f);
                    pech.setYHeadRot(0.0f);
                    pech.yBodyRot = 0.0f;
                    pech.setNoAi(true);
                    pech.setPechType(i);
                    pech.setItemSlot(EquipmentSlot.MAINHAND, held[i]);
                    level.addFreshEntity(pech);
                    if (i == 0) {
                        pech.setTamed(true);
                        ids[0] = pech.getId();
                    }
                }
                player.getInventory().setItem(0, new ItemStack(Items.EMERALD, 5));
                player.getInventory().setItem(1, new ItemStack(TCItems.FOCI.get("pech")));
                player.getInventory().setItem(2, new ItemStack(TCItems.PECH_SPAWN_EGG));
            });
            context.waitTicks(40);
            context.takeScreenshot("pechs");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                if (player.level().getEntity(ids[0]) instanceof PechEntity pech) pech.interact(player, InteractionHand.MAIN_HAND, pech.position());
            });
            context.waitTicks(10);
            context.takeScreenshot("pech_troca");
        }
    }
}
