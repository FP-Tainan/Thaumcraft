package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/**
 * O minério infundido tem de largar o fragmento do aspecto dele, que é de onde vêm os fragmentos no
 * Thaumcraft 4.2.3.5.
 */
public class OreGameTest {
    /** As seis pedras existem, uma por primordial. */
    @GameTest
    public void thereIsOneStonePerPrimal(GameTestHelper helper) {
        if (TCBlocks.INFUSED_STONE.size() != 6) {
            helper.fail("deviam ser seis pedras infundidas, são " + TCBlocks.INFUSED_STONE.size());
        }
        for (String tag : new String[]{"air", "fire", "water", "earth", "order", "entropy"}) {
            if (TCBlocks.INFUSED_STONE.get(tag) == null) helper.fail("faltou a pedra de " + tag);
            if (TCItems.SHARDS.get(tag) == null) helper.fail("faltou o fragmento de " + tag);
        }
        helper.succeed();
    }

    /** Quebrada com picareta, ela larga o fragmento. */
    @GameTest
    public void breakingItGivesTheShard(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, TCBlocks.INFUSED_STONE.get("fire").defaultBlockState());
        var level = helper.getLevel();
        var drops = net.minecraft.world.level.block.Block.getDrops(
                helper.getBlockState(pos), level, helper.absolutePos(pos), null,
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL),
                new ItemStack(Items.IRON_PICKAXE));
        boolean found = drops.stream().anyMatch(drop -> drop.is(TCItems.SHARDS.get("fire")));
        if (!found) helper.fail("a pedra de fogo devia largar um fragmento de fogo, largou " + drops);

        // sem picareta não larga nada
        var barehanded = net.minecraft.world.level.block.Block.getDrops(
                helper.getBlockState(pos), level, helper.absolutePos(pos), null,
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL), ItemStack.EMPTY);
        if (!barehanded.isEmpty()) helper.fail("sem picareta não devia largar nada");
        helper.succeed();
    }
}
