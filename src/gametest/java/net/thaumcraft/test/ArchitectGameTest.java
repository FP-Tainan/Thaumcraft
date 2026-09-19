package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.Architect;
import net.thaumcraft.item.FocusUpgradeTable;
import net.thaumcraft.item.Focuses;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

import java.util.List;

/** O arquiteto: a área na varinha, a tecla G e os blocos que a troca e a proteção pegam. */
public class ArchitectGameTest {
    private static ItemStack wand(String focus, Short... upgrades) {
        ItemStack wand = new ItemStack(TCItems.WAND);
        wand.set(TCComponents.WAND_FOCUS, focus);
        java.util.ArrayList<Short> list = new java.util.ArrayList<>(List.of(upgrades));
        while (list.size() < 5) list.add((short) -1);
        wand.set(TCComponents.FOCUS_UPGRADES, list);
        AspectList vis = new AspectList();
        for (Aspect primal : Aspects.primals()) vis.add(primal, 2500);
        wand.set(TCComponents.WAND_VIS, vis);
        return wand;
    }

    @GameTest
    public void theKeyGrowsAndWrapsTheArea(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = wand("trade", FocusUpgradeTable.FRUGAL.id(), FocusUpgradeTable.FRUGAL.id(), FocusUpgradeTable.ARCHITECT.id());
        if (!Architect.active(wand)) helper.fail("a troca com arquiteto responde");
        if (Architect.areaX(wand) != 3) helper.fail("sem mexer, a área é a máxima (3)");
        Architect.toggleMisc(wand, player);
        if (Architect.areaX(wand) != 0 || Architect.areaZ(wand) != 0) helper.fail("passou do máximo, volta a zero");
        Architect.toggleMisc(wand, player);
        if (Architect.areaX(wand) != 1 || Architect.areaY(wand) != 1) helper.fail("e cresce de um em um");
        if (Architect.active(wand("trade"))) helper.fail("sem a melhoria, nada");
        helper.succeed();
    }

    @GameTest
    public void tradeTakesTheExposedPlane(GameTestHelper helper) {
        for (int x = 0; x < 7; x++) for (int z = 0; z < 7; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.DIRT.defaultBlockState());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = wand("trade", FocusUpgradeTable.FRUGAL.id(), FocusUpgradeTable.FRUGAL.id(), FocusUpgradeTable.ARCHITECT.id());
        wand.set(TCComponents.WAND_AREA, List.of(1, 1, 1, 0));
        var blocks = Architect.tradeBlocks(wand, helper.getLevel(), helper.absolutePos(new BlockPos(3, 1, 3)), 1, player);
        if (blocks.size() != 9) helper.fail("área 1 em volta: três por três, deu " + blocks.size());
        helper.setBlock(new BlockPos(4, 1, 3), Blocks.STONE.defaultBlockState());
        blocks = Architect.tradeBlocks(wand, helper.getLevel(), helper.absolutePos(new BlockPos(3, 1, 3)), 1, player);
        if (blocks.size() != 8) helper.fail("só os iguais");
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void wardingCoversTheWall(GameTestHelper helper) {
        for (int x = 0; x < 5; x++) for (int y = 1; y < 5; y++) helper.setBlock(new BlockPos(x, y, 3), Blocks.STONE.defaultBlockState());
        var player = helper.makeMockServerPlayerInLevel();
        Vec3 feet = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        player.snapTo(feet.x, feet.y, feet.z, 0.0f, 0.0f);
        ItemStack wand = wand("warding", FocusUpgradeTable.FRUGAL.id(), FocusUpgradeTable.ARCHITECT.id());
        player.getInventory().setItem(0, wand);
        player.getInventory().setSelectedSlot(0);
        if (!Focuses.tick(helper.getLevel(), player, wand, Focuses.on(wand))) helper.fail("o foco devia proteger");
        int warded = 0;
        for (int x = 0; x < 5; x++) for (int y = 1; y < 5; y++) {
            if (helper.getBlockState(new BlockPos(x, y, 3)).is(TCBlocks.WARDED)) warded++;
        }
        if (warded != 20) helper.fail("a área 3 em volta pega a parede inteira, pegou " + warded);
        helper.succeed();
    }
}
