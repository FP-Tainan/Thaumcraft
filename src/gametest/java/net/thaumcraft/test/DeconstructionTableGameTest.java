package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.block.entity.DeconstructionTableBlockEntity;
import net.thaumcraft.registry.TCBlocks;

/** A mesa de desconstrução tem de seguir o {@code TileDeconstructionTable}. */
public class DeconstructionTableGameTest {
    @GameTest(maxTicks = 100)
    public void theTableBreaksThingsIntoResearch(GameTestHelper helper) {
        BlockPos at = new BlockPos(1, 1, 1);
        helper.setBlock(at, TCBlocks.DECONSTRUCTION_TABLE);
        var table = helper.getBlockEntity(at, DeconstructionTableBlockEntity.class);
        // um bloco de diamante é tanto aspecto que o primário sempre sobra
        table.setItem(0, new ItemStack(Items.DIAMOND_BLOCK, 3));
        helper.runAfterDelay(45, () -> {
            if (table.getItem(0).getCount() != 2) helper.fail("em dois segundos a mesa desfaz um; sobraram " + table.getItem(0).getCount());
            if (table.aspect() == null || !table.aspect().isPrimal()) helper.fail("devia sobrar um primário");
            var player = helper.makeMockServerPlayerInLevel();
            var aspect = table.aspect();
            int before = net.thaumcraft.research.Knowledges.of(player).points(aspect);
            table.collect(player);
            if (net.thaumcraft.research.Knowledges.of(player).points(aspect) != before + 1) helper.fail("recolher dá um ponto");
            if (table.aspect() != null) helper.fail("depois de recolher, a mesa fica livre");
            helper.succeed();
        });
    }
}
