package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.Swapper;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

/** A Troca Equivalente tem de seguir o {@code VirtualSwapper}: troca, devolve o que saiu e se espalha. */
public class EqualTradeGameTest {
    @GameTest(maxTicks = 60)
    public void theTradeSpreadsOverTheWall(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        ItemStack wand = new ItemStack(TCItems.WAND);
        wand.set(TCComponents.WAND_FOCUS, "trade");
        wand.set(TCComponents.WAND_PICKED, "minecraft:dirt");
        AspectList vis = new AspectList();
        for (var primal : Aspects.primals()) vis.add(primal, 2500);
        wand.set(TCComponents.WAND_VIS, vis);
        player.getInventory().setItem(0, wand);
        player.getInventory().setSelectedSlot(0);
        player.getInventory().add(new ItemStack(Items.DIRT, 8));

        for (int x = 0; x < 3; x++) helper.setBlock(new BlockPos(x, 2, 1), Blocks.STONE.defaultBlockState());
        BlockPos first = helper.absolutePos(new BlockPos(0, 2, 1));
        Swapper.add(helper.getLevel(), first, Blocks.STONE.defaultBlockState(), Items.DIRT, 3, player, 0);
        helper.runAfterDelay(20, () -> {
            for (int x = 0; x < 3; x++) {
                if (!helper.getBlockState(new BlockPos(x, 2, 1)).is(Blocks.DIRT)) helper.fail("a pedra " + x + " devia virar terra");
            }
            if (!player.getInventory().contains(new ItemStack(Items.COBBLESTONE))) helper.fail("o que saiu vai para o inventário");
            int dirt = 0;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (player.getInventory().getItem(i).is(Items.DIRT)) dirt += player.getInventory().getItem(i).getCount();
            }
            if (dirt != 5) helper.fail("cada troca gasta um bloco de terra; sobraram " + dirt);
            helper.succeed();
        });
    }
}
