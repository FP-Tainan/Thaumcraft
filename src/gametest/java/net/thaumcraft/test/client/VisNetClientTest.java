package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.nodes.NodeType;
import net.thaumcraft.block.VisRelayBlock;
import net.thaumcraft.block.entity.ArcaneWorkbenchBlockEntity;
import net.thaumcraft.block.entity.EnergizedNodeBlockEntity;
import net.thaumcraft.block.entity.NodeBlockEntity;
import net.thaumcraft.entity.AspectOrbEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/**
 * A rede de vis montada: estabilizador com nó travado, o transdutor com o nó energizado, dois relés e o carregador
 * sobre a bancada, vistos com os óculos; e orbes de aspecto soltos.
 */
public class VisNetClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set midnight");
            server.runCommand("tp @p ~ ~ ~ 180 20");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos base = player.blockPosition().north(5);
                player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TCItems.GOGGLES));
                AspectList aura = new AspectList().add(Aspects.AIR, 25).add(Aspects.FIRE, 16).add(Aspects.ORDER, 9).add(Aspects.WATER, 12);
                // à esquerda, um nó travado pelo estabilizador comum
                BlockPos left = base.west(3);
                level.setBlockAndUpdate(left, TCBlocks.NODE_STABILIZER.defaultBlockState());
                level.setBlockAndUpdate(left.above(), TCBlocks.NODE.defaultBlockState());
                if (level.getBlockEntity(left.above()) instanceof NodeBlockEntity node) node.setup(aura.copy(), NodeType.NORMAL, null);
                // no meio, a fonte: avançado, nó energizado e transdutor
                level.setBlockAndUpdate(base, TCBlocks.NODE_STABILIZER_ADVANCED.defaultBlockState());
                level.setBlockAndUpdate(base.above(2), TCBlocks.NODE_CONVERTER.defaultBlockState());
                level.setBlockAndUpdate(base.above(), TCBlocks.ENERGIZED_NODE.defaultBlockState());
                if (level.getBlockEntity(base.above()) instanceof EnergizedNodeBlockEntity energized) energized.setup(aura.copy(), NodeType.NORMAL, null);
                // à direita, dois relés e o carregador em cima da bancada
                BlockPos right = base.east(3);
                level.setBlockAndUpdate(right, Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(right.above(), TCBlocks.VIS_RELAY.defaultBlockState().setValue(VisRelayBlock.FACING, Direction.UP));
                level.setBlockAndUpdate(right.east(3).above(3), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(right.east(3).above(2), TCBlocks.VIS_RELAY.defaultBlockState().setValue(VisRelayBlock.FACING, Direction.DOWN));
                BlockPos bench = right.east(2).south(2);
                level.setBlockAndUpdate(bench, TCBlocks.ARCANE_WORKBENCH.defaultBlockState());
                level.setBlockAndUpdate(bench.above(), TCBlocks.WORKBENCH_CHARGER.defaultBlockState());
                if (level.getBlockEntity(bench) instanceof ArcaneWorkbenchBlockEntity workbench) {
                    workbench.setItem(ArcaneWorkbenchBlockEntity.WAND_SLOT, new ItemStack(TCItems.WAND));
                }
                var inv = player.getInventory();
                inv.setItem(0, new ItemStack(TCItems.NODE_STABILIZER));
                inv.setItem(1, new ItemStack(TCItems.NODE_STABILIZER_ADVANCED));
                inv.setItem(2, new ItemStack(TCItems.NODE_CONVERTER));
                inv.setItem(3, new ItemStack(TCItems.VIS_RELAY));
                inv.setItem(4, new ItemStack(TCItems.WORKBENCH_CHARGER));
                inv.setItem(5, new ItemStack(TCItems.LOOT_BAG));
                inv.setItem(6, new ItemStack(TCItems.LOOT_BAG_UNCOMMON));
                inv.setItem(7, new ItemStack(TCItems.LOOT_BAG_RARE));
                for (var aspect : Aspects.primals()) {
                    level.addFreshEntity(new AspectOrbEntity(level, base.getX() + 0.5, base.getY() + 3, base.getZ() + 2.5, aspect, 1));
                }
            });
            context.waitTicks(100);
            context.takeScreenshot("rede_vis");
            server.runCommand("tp @p ~ ~ ~ 225 25");
            context.waitTicks(20);
            context.takeScreenshot("rede_vis_reles");
            server.runCommand("gamemode survival");
            context.waitTicks(5);
            context.getInput().pressKey(options -> options.keyInventory);
            context.waitTicks(10);
            context.takeScreenshot("rede_vis_inventario");
        }
    }
}
