package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.block.entity.InfusionMatrixBlockEntity;
import net.thaumcraft.block.entity.JarBlockEntity;
import net.thaumcraft.block.entity.PedestalBlockEntity;
import net.thaumcraft.crafting.InfusionRecipe;
import net.thaumcraft.crafting.InfusionRecipes;
import net.thaumcraft.registry.TCBlocks;

import java.util.concurrent.atomic.AtomicReference;

/** A infusão trabalhando: a essência vindo dos jarros pelo ar, as runas, as migalhas dos pedestais e os avisos do canto. */
public class InfusionRunClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            AtomicReference<BlockPos> base = new AtomicReference<>();
            InfusionRecipe recipe = InfusionRecipes.ALL.getFirst();
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos p = player.blockPosition();
                base.set(p);
                for (int x = -6; x <= 6; x++) for (int z = 2; z <= 12; z++) {
                    level.setBlockAndUpdate(p.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
                    for (int y = 0; y < 5; y++) level.setBlockAndUpdate(p.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
                BlockPos matrix = p.offset(0, 2, 8);
                level.setBlockAndUpdate(matrix, TCBlocks.INFUSION_MATRIX.defaultBlockState());
                level.setBlockAndUpdate(matrix.below(2), TCBlocks.PEDESTAL.defaultBlockState());
                for (int dx = -1; dx <= 1; dx += 2) for (int dz = -1; dz <= 1; dz += 2) {
                    level.setBlockAndUpdate(matrix.offset(dx, -1, dz), TCBlocks.INFUSION_PILLAR_TOP.defaultBlockState());
                    level.setBlockAndUpdate(matrix.offset(dx, -2, dz), TCBlocks.INFUSION_PILLAR.defaultBlockState());
                }
                ((PedestalBlockEntity) level.getBlockEntity(matrix.below(2))).hold(new ItemStack(recipe.central().items().iterator().next()));
                int[][] spots = {{-3, 0}, {3, 0}, {0, 3}, {0, -3}, {-2, 2}, {2, -2}, {2, 2}, {-2, -2}};
                int slot = 0;
                for (var wanted : recipe.components()) {
                    BlockPos at = matrix.offset(spots[slot][0], -2, spots[slot][1]);
                    slot++;
                    level.setBlockAndUpdate(at, TCBlocks.PEDESTAL.defaultBlockState());
                    ((PedestalBlockEntity) level.getBlockEntity(at)).hold(new ItemStack(wanted.items().iterator().next()));
                }
                int jar = 0;
                for (var aspect : recipe.essentia().getAspects()) {
                    BlockPos at = matrix.offset(jar % 2 == 0 ? -5 : 5, -2, -1 + jar / 2 * 2);
                    jar++;
                    level.setBlockAndUpdate(at, TCBlocks.JAR.defaultBlockState());
                    ((JarBlockEntity) level.getBlockEntity(at)).addToContainer(aspect, JarBlockEntity.CAPACITY);
                }
                net.thaumcraft.research.Knowledges.of(player).completeResearch(recipe.research());
            });
            BlockPos p = base.get();
            server.runCommand("tp @p " + (p.getX() + 0.5) + " " + p.getY() + " " + (p.getZ() + 0.5) + " 0 18");
            context.waitTicks(20);
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                BlockPos matrix = p.offset(0, 2, 8);
                var te = (InfusionMatrixBlockEntity) player.level().getBlockEntity(matrix);
                te.poke(player.level(), matrix, player, ItemStack.EMPTY);
                te.poke(player.level(), matrix, player, ItemStack.EMPTY);
                // os avisos do canto: distorção e pontos de pesquisa
                net.thaumcraft.research.Warp.add(player, 2, true);
                net.thaumcraft.net.TCNetwork.aspectPool(player, net.thaumcraft.api.aspects.Aspects.FIRE, 3, 10);
            });
            context.waitTicks(25);
            context.takeScreenshot("infusao_essencia");
            context.waitTicks(recipe.essentia().visSize() * 10);
            context.takeScreenshot("infusao_ingredientes");
            context.waitTicks(recipe.components().size() * 60 + 40);
            context.takeScreenshot("infusao_pronta");
        }
    }
}
