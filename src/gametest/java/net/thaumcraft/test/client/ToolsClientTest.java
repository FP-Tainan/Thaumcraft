package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.entity.PrimalArrowEntity;
import net.thaumcraft.registry.TCItems;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/** As ferramentas soltas na tela: os ícones, o medidor de sanidade, a varredura da picareta e as flechas primordiais. */
public class ToolsClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            AtomicReference<BlockPos> base = new AtomicReference<>();
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var inv = player.getInventory();
                List<ItemStack> items = List.of(new ItemStack(TCItems.SANITY_CHECKER), new ItemStack(TCItems.ELEMENTAL_PICKAXE),
                        new ItemStack(TCItems.ELEMENTAL_AXE), new ItemStack(TCItems.ELEMENTAL_SHOVEL), new ItemStack(TCItems.ELEMENTAL_HOE),
                        new ItemStack(TCItems.ELEMENTAL_SWORD), new ItemStack(TCItems.PRIMAL_CRUSHER), new ItemStack(TCItems.CRIMSON_SWORD),
                        new ItemStack(TCItems.BONE_BOW));
                for (int i = 0; i < items.size(); i++) inv.setItem(i, items.get(i));
                int slot = 9;
                for (var arrow : TCItems.PRIMAL_ARROWS.values()) inv.setItem(slot++, new ItemStack(arrow, 16));
                for (var item : List.of(TCItems.RESONATOR, TCItems.SINISTER_STONE, TCItems.SANITY_SOAP, TCItems.NUGGET_BEEF,
                        TCItems.NUGGET_CHICKEN, TCItems.NUGGET_PORK, TCItems.NUGGET_FISH, TCItems.TRIPLE_MEAT_TREAT)) {
                    inv.setItem(slot++, new ItemStack(item));
                }
                inv.setSelectedSlot(0);
                net.thaumcraft.research.Warp.add(player, 20, false);
                net.thaumcraft.research.Warp.addSticky(player, 12);
                net.thaumcraft.research.Warp.add(player, 8, true);
                BlockPos p = player.blockPosition();
                base.set(p);
                var level = player.level();
                for (int x = -4; x <= 4; x++) for (int z = 2; z <= 8; z++) for (int y = -4; y <= -1; y++) {
                    level.setBlockAndUpdate(p.offset(x, y, z), Blocks.STONE.defaultBlockState());
                }
                level.setBlockAndUpdate(p.offset(-2, -2, 4), Blocks.IRON_ORE.defaultBlockState());
                level.setBlockAndUpdate(p.offset(1, -3, 5), Blocks.GOLD_ORE.defaultBlockState());
                level.setBlockAndUpdate(p.offset(2, -2, 3), Blocks.DIAMOND_ORE.defaultBlockState());
                level.setBlockAndUpdate(p.offset(0, -3, 6), Blocks.LAVA.defaultBlockState());
                level.setBlockAndUpdate(p.offset(-1, -2, 6), Blocks.WATER.defaultBlockState());
            });
            BlockPos p = base.get();
            server.runCommand("tp @p " + (p.getX() + 0.5) + " " + p.getY() + " " + (p.getZ() + 0.5) + " 0 30");
            context.waitTicks(20);
            context.takeScreenshot("ferramentas_sanidade");
            context.runOnClient(mc -> {
                mc.player.getInventory().setSelectedSlot(1);
                net.thaumcraft.client.fx.OreScan.start(mc.level, p.offset(0, -2, 4));
            });
            context.waitTicks(10);
            context.takeScreenshot("ferramentas_varredura");
            context.setScreen(() -> new net.minecraft.client.gui.screens.inventory.InventoryScreen(net.minecraft.client.Minecraft.getInstance().player));
            context.waitTicks(5);
            context.takeScreenshot("ferramentas_inventario");
            context.setScreen(() -> null);
            server.runCommand("tp @p " + (p.getX() + 0.5) + " " + p.getY() + " " + (p.getZ() + 0.5) + " 0 0");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                for (int i = 0; i < 6; i++) {
                    var arrow = new PrimalArrowEntity(player.level(), p.getX() - 2.5 + i, p.getY() + 1.4, p.getZ() + 3.0,
                            new ItemStack(TCItems.PRIMAL_ARROWS.get(PrimalArrowEntity.TYPES[i])), i);
                    arrow.setNoGravity(true);
                    arrow.setDeltaMovement(0.0, 0.0, 0.001);
                    player.level().addFreshEntity(arrow);
                }
            });
            context.waitTicks(10);
            context.takeScreenshot("ferramentas_flechas");
        }
    }
}
