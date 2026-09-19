package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.nodes.NodeType;
import net.thaumcraft.block.VisRelayBlock;
import net.thaumcraft.block.entity.EnergizedNodeBlockEntity;
import net.thaumcraft.block.entity.FocalManipulatorBlockEntity;
import net.thaumcraft.block.entity.VisRelayBlockEntity;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.FocusUpgradeTable;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/** O manipulador focal: cobra a experiência, puxa o vis da rede e põe a melhoria no próximo posto do foco. */
public class FocalManipulatorGameTest {
    @GameTest
    public void theCostDoublesPerRank(GameTestHelper helper) {
        // a potência (Telum) no primeiro posto: 200 de Telum, em primários
        AspectList first = FocalManipulatorBlockEntity.costOf(FocusUpgradeTable.POTENCY, 1);
        AspectList third = FocalManipulatorBlockEntity.costOf(FocusUpgradeTable.POTENCY, 3);
        for (Aspect a : first.getAspects()) {
            if (!a.isPrimal()) helper.fail("só primários");
            if (third.getAmount(a) != first.getAmount(a) * 4) helper.fail("o terceiro posto custa quatro vezes o primeiro");
        }
        helper.succeed();
    }

    @GameTest
    public void itChargesExperienceAndChecksTheSlot(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), TCBlocks.FOCAL_MANIPULATOR.defaultBlockState());
        FocalManipulatorBlockEntity table = helper.getBlockEntity(new BlockPos(1, 1, 1), FocalManipulatorBlockEntity.class);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        table.setItem(0, new ItemStack(TCItems.FOCI.get("fire")));
        if (table.startCraft(FocusUpgradeTable.POTENCY.id(), player)) helper.fail("sem os oito níveis, não começa");
        player.giveExperienceLevels(30);
        if (table.startCraft(FocusUpgradeTable.FIREBALL.id(), player)) helper.fail("a bola de fogo não cabe no primeiro posto");
        if (!table.startCraft(FocusUpgradeTable.POTENCY.id(), player)) helper.fail("a potência cabe");
        if (player.experienceLevel != 22) helper.fail("custa oito níveis, sobrou " + player.experienceLevel);
        if (table.size <= 0) helper.fail("e começa a puxar");
        if (table.startCraft(FocusUpgradeTable.FRUGAL.id(), player)) helper.fail("uma de cada vez");
        helper.succeed();
    }

    @GameTest(maxTicks = 1200)
    public void itDrainsTheNetAndUpgradesTheFocus(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), TCBlocks.NODE_STABILIZER.defaultBlockState());
        helper.setBlock(new BlockPos(1, 3, 1), TCBlocks.NODE_CONVERTER.defaultBlockState());
        helper.setBlock(new BlockPos(1, 2, 1), TCBlocks.ENERGIZED_NODE.defaultBlockState());
        EnergizedNodeBlockEntity energized = helper.getBlockEntity(new BlockPos(1, 2, 1), EnergizedNodeBlockEntity.class);
        AspectList aura = new AspectList();
        for (Aspect primal : Aspects.primals()) aura.add(primal, 50);
        energized.setup(aura, NodeType.NORMAL, null);
        helper.setBlock(new BlockPos(4, 1, 1), Blocks.STONE.defaultBlockState());
        helper.setBlock(new BlockPos(4, 2, 1), TCBlocks.VIS_RELAY.defaultBlockState().setValue(VisRelayBlock.FACING, Direction.UP));
        VisRelayBlockEntity relay = helper.getBlockEntity(new BlockPos(4, 2, 1), VisRelayBlockEntity.class);
        helper.setBlock(new BlockPos(4, 1, 3), TCBlocks.FOCAL_MANIPULATOR.defaultBlockState());
        FocalManipulatorBlockEntity table = helper.getBlockEntity(new BlockPos(4, 1, 3), FocalManipulatorBlockEntity.class);
        table.setItem(0, new ItemStack(TCItems.FOCI.get("fire")));
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.giveExperienceLevels(8);
        boolean[] started = {false};
        helper.onEachTick(() -> {
            if (!started[0] && relay.parent() == energized) {
                started[0] = table.startCraft(FocusUpgradeTable.POTENCY.id(), player);
                if (!started[0]) helper.fail("a potência devia começar");
            }
        });
        helper.succeedWhen(() -> {
            if (!started[0]) helper.fail("o relé ainda não se pendurou");
            if (FocusItem.level(table.getItem(0), FocusUpgradeTable.POTENCY) != 1) helper.fail("a melhoria ainda não entrou");
            if (table.size != 0) helper.fail("e a mesa para");
        });
    }
}
