package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.nodes.NodeType;
import net.thaumcraft.block.entity.NodeBlockEntity;
import net.thaumcraft.block.entity.WandPedestalBlockEntity;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

/** O pedestal de recarga: a varinha em cima bebe dos nós em volta; com o foco composto, também dos compostos. */
public class WandPedestalGameTest {
    private static NodeBlockEntity node(GameTestHelper helper, AspectList aura) {
        helper.setBlock(new BlockPos(4, 2, 1), TCBlocks.NODE.defaultBlockState());
        NodeBlockEntity node = helper.getBlockEntity(new BlockPos(4, 2, 1), NodeBlockEntity.class);
        node.setup(aura, NodeType.NORMAL, null);
        return node;
    }

    private static WandPedestalBlockEntity pedestal(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), TCBlocks.WAND_PEDESTAL.defaultBlockState());
        WandPedestalBlockEntity ped = helper.getBlockEntity(new BlockPos(1, 1, 1), WandPedestalBlockEntity.class);
        ItemStack wand = new ItemStack(TCItems.WAND);
        wand.set(TCComponents.WAND_ROD, "greatwood");
        wand.set(TCComponents.WAND_CAP, "gold");
        ped.setItem(0, wand);
        return ped;
    }

    @GameTest(maxTicks = 120)
    public void theWandDrinksFromTheNode(GameTestHelper helper) {
        NodeBlockEntity node = node(helper, new AspectList().add(Aspects.AIR, 20));
        WandPedestalBlockEntity ped = pedestal(helper);
        helper.succeedWhen(() -> {
            int got = WandItem.vis(ped.held(), Aspects.AIR);
            if (got < 3 * WandItem.VIS_UNIT) helper.fail("a varinha bebe um ponto de ar a cada cinco tiques, tem " + got);
            if (node.aspects().getAmount(Aspects.AIR) > 17) helper.fail("e o nó perde o que ela bebe");
            if (ped.comparator() < 1) helper.fail("o comparador lê a varinha");
        });
    }

    @GameTest(maxTicks = 100)
    public void compoundsNeedTheFocus(GameTestHelper helper) {
        NodeBlockEntity node = node(helper, new AspectList().add(Aspects.MOTION, 20));
        WandPedestalBlockEntity ped = pedestal(helper);
        helper.runAfterDelay(30, () -> {
            if (WandItem.vis(ped.held()).visSize() != 0) helper.fail("sem o foco composto, Motus não serve");
            helper.setBlock(new BlockPos(1, 2, 1), TCBlocks.RECHARGE_FOCUS.defaultBlockState());
        });
        helper.runAfterDelay(80, () -> {
            if (WandItem.vis(ped.held()).visSize() == 0) helper.fail("com o foco, Motus vira Aer e Ordo na varinha");
            if (node.aspects().getAmount(Aspects.MOTION) >= 20) helper.fail("e sai do nó");
            helper.succeed();
        });
    }
}
