package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.block.entity.ArcaneWorkbenchBlockEntity;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/** A bancada arcana aberta: a grade montada tem de mostrar o resultado e o custo na tela de quem joga. */
public class ArcaneBenchClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("thaumcraft tudo @p");
            server.runCommand("time set noon");
            context.waitTicks(10);
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos pos = player.blockPosition().offset(0, -1, 2);
                level.setBlockAndUpdate(pos, TCBlocks.ARCANE_WORKBENCH.defaultBlockState());
                if (level.getBlockEntity(pos) instanceof ArcaneWorkbenchBlockEntity bench) player.openMenu(bench);
            });
            context.waitTicks(10);
            // a tela já aberta e vazia: agora entram as peças, como quem arrasta os itens
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                BlockPos pos = player.blockPosition().offset(0, -1, 2);
                if (player.level().getBlockEntity(pos) instanceof ArcaneWorkbenchBlockEntity bench) {
                    bench.setItem(2, new ItemStack(TCItems.WAND_CAPS.get("gold")));
                    bench.setItem(6, new ItemStack(TCItems.WAND_CAPS.get("gold")));
                    bench.setItem(4, new ItemStack(TCItems.WAND_RODS.get("greatwood")));
                    ItemStack wand = WandItem.bookStack("gold", "greatwood", false);
                    var vis = new net.thaumcraft.api.aspects.AspectList();
                    for (var primal : net.thaumcraft.api.aspects.Aspects.primals()) vis.add(primal, WandItem.maxVis(wand));
                    WandItem.setVis(wand, vis);
                    bench.setItem(ArcaneWorkbenchBlockEntity.WAND_SLOT, wand);
                }
            });
            context.waitTicks(20);
            context.takeScreenshot("bancada_arcana");
            // com a tela já aberta, o resultado tem de ter aparecido dos dois lados
            server.runOnServer(s -> {
                var menu = s.getPlayerList().getPlayers().getFirst().containerMenu;
                if (menu.getSlot(0).getItem().isEmpty()) throw new AssertionError("o servidor não pôs o resultado na bancada");
            });
            context.runOnClient(minecraft -> {
                if (minecraft.player.containerMenu.getSlot(0).getItem().isEmpty()) {
                    throw new AssertionError("a bancada aberta não mostrou o resultado depois de pôr as peças");
                }
            });
        }
    }
}
