package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.AdvancedAlchemicalFurnaceStructure;
import net.thaumcraft.block.EssentiaReservoirBlock;
import net.thaumcraft.block.entity.AdvancedAlchemicalFurnaceBlockEntity;
import net.thaumcraft.block.entity.EssentiaReservoirBlockEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/** A fornalha alquímica avançada acesa e com essência, dois reservatórios (um cheio pela metade) e a construção avançada. */
public class AdvancedAlchemyClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("tp @p ~ ~1 ~ 180 25");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos centre = player.blockPosition().offset(0, -1, -5);
                AdvancedAlchemicalFurnaceStructure.build(level, centre);
                if (level.getBlockEntity(centre) instanceof AdvancedAlchemicalFurnaceBlockEntity furnace) {
                    furnace.heat = 400;
                    furnace.aspects.add(Aspects.WATER, 120).add(Aspects.FIRE, 80);
                    furnace.vis = furnace.aspects.visSize();
                    furnace.sync();
                }
                BlockPos res = centre.offset(3, 0, 1);
                level.setBlockAndUpdate(res, TCBlocks.ESSENTIA_RESERVOIR.defaultBlockState().setValue(EssentiaReservoirBlock.FACING, Direction.DOWN));
                if (level.getBlockEntity(res) instanceof EssentiaReservoirBlockEntity te) te.addToContainer(Aspects.MAGIC, 128);
                level.setBlockAndUpdate(res.west(5), TCBlocks.ESSENTIA_RESERVOIR.defaultBlockState().setValue(EssentiaReservoirBlock.FACING, Direction.EAST));
                level.setBlockAndUpdate(res.west(5).above(), TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.defaultBlockState());
                var inv = player.getInventory();
                inv.setItem(0, new ItemStack(TCItems.ESSENTIA_RESERVOIR));
                inv.setItem(1, new ItemStack(TCItems.ADVANCED_ALCHEMICAL_CONSTRUCT));
                inv.setSelectedSlot(0);
            });
            context.waitTicks(60);
            context.takeScreenshot("fornalha_avancada_reservatorio");
        }
    }
}
