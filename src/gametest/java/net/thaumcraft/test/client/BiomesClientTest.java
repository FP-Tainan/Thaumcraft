package net.thaumcraft.test.client;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.ManaPodBlock;
import net.thaumcraft.item.ManaBeanItem;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;
import net.thaumcraft.world.TCBiomes;

/** A Floresta Mágica e a Terra Maculada num mundo de verdade; e as velas, a vagem de mana e o cogumelo-vis. */
public class BiomesClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder()
                .adjustSettings(settings -> {
                    // o mundo normal (o de teste é plano), com uma semente fixa
                    settings.setWorldType(settings.getNormalPresetList().getFirst());
                    settings.setSeed("thaumcraft");
                }).create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            visit(context, singleplayer, TCBiomes.MAGICAL_FOREST, "bioma_floresta_magica", true);
            visit(context, singleplayer, TCBiomes.TAINTED_LAND, "bioma_terra_maculada", false);
        }
    }

    private static void visit(ClientGameTestContext context, TestSingleplayerContext singleplayer, ResourceKey<Biome> biome, String name,
                              boolean props) {
        singleplayer.getServer().runOnServer(s -> {
            var player = s.getPlayerList().getPlayers().getFirst();
            var level = player.level();
            Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(h -> h.is(biome), player.blockPosition(), 6400, 32, 64);
            if (found == null) throw new AssertionError("não achei " + biome);
            BlockPos at = found.getFirst();
            level.getChunk(at);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, at.getX(), at.getZ());
            player.teleportTo(level, at.getX() + 0.5, y + 12, at.getZ() + 0.5, java.util.Set.of(), 45.0f, 35.0f, false);
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            if (props) {
                BlockPos base = new BlockPos(at.getX() + 3, y, at.getZ() + 3);
                int i = 0;
                for (var candle : TCBlocks.TALLOW_CANDLES.values()) {
                    level.setBlockAndUpdate(base.offset(i % 4, 0, i / 4).below(), Blocks.STONE.defaultBlockState());
                    level.setBlockAndUpdate(base.offset(i % 4, 0, i / 4), candle.defaultBlockState());
                    i++;
                }
                BlockPos log = base.offset(-2, 3, 0);
                level.setBlockAndUpdate(log, Blocks.OAK_LOG.defaultBlockState());
                level.setBlockAndUpdate(log.below(), TCBlocks.MANA_POD.defaultBlockState().setValue(ManaPodBlock.AGE, 7));
                if (level.getBlockEntity(log.below()) instanceof net.thaumcraft.block.entity.ManaPodBlockEntity pod) {
                    pod.aspect = Aspects.FIRE;
                    pod.setChanged();
                }
                level.setBlockAndUpdate(base.offset(-2, -1, 2), Blocks.GRASS_BLOCK.defaultBlockState());
                level.setBlockAndUpdate(base.offset(-2, 0, 2), TCBlocks.VISHROOM.defaultBlockState());
                var inv = player.getInventory();
                inv.setItem(0, ManaBeanItem.of(Aspects.WATER));
                inv.setItem(1, new ItemStack(TCBlocks.VISHROOM));
                inv.setItem(2, new ItemStack(TCResources.get("native_iron_cluster")));
                inv.setItem(3, new ItemStack(TCResources.get("native_gold_cluster")));
                inv.setItem(4, new ItemStack(TCResources.get("native_copper_cluster")));
                inv.setItem(5, new ItemStack(TCResources.get("native_cinnabar_cluster")));
                inv.setItem(6, new ItemStack(TCResources.get("quicksilver_drop")));
                inv.setItem(7, new ItemStack(TCBlocks.TALLOW_CANDLES.get("red")));
                inv.setItem(8, new ItemStack(TCBlocks.TALLOW_CANDLES.get("white")));
                inv.setSelectedSlot(0);
            }
        });
        context.waitTicks(20);
        singleplayer.getConnection().waitForChunksRender();
        context.waitTicks(60);
        context.takeScreenshot(name);
        if (props) {
            singleplayer.getServer().runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                // de perto: as velas e a vagem
                BlockPos p = player.blockPosition();
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p.getX(), p.getZ());
                player.teleportTo(level, p.getX() + 0.5, y + 1.8, p.getZ() - 1.5, java.util.Set.of(), 0.0f, 25.0f, false);
            });
            context.waitTicks(40);
            context.takeScreenshot(name + "_perto");
        }
    }
}
