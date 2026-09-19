package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.thaumcraft.block.ArcaneBoreBaseBlock;
import net.thaumcraft.block.ArcaneBoreBlock;
import net.thaumcraft.block.entity.ArcaneBoreBlockEntity;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.FocusUpgradeTable;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/** A broca arcana: cava à frente com foco e picareta, gasta a picareta e manda o que sai para o baú da base. */
public class ArcaneBoreGameTest {
    private static ArcaneBoreBlockEntity build(GameTestHelper helper) {
        for (int x = 0; x < 5; x++) for (int y = 1; y < 5; y++) for (int z = 0; z < 3; z++) {
            helper.setBlock(new BlockPos(x, y, z), Blocks.STONE.defaultBlockState());
        }
        helper.setBlock(new BlockPos(2, 1, 5), TCBlocks.ARCANE_BORE_BASE.defaultBlockState().setValue(ArcaneBoreBaseBlock.FACING, Direction.WEST));
        helper.setBlock(new BlockPos(2, 2, 5), TCBlocks.ARCANE_BORE.defaultBlockState()
                .setValue(ArcaneBoreBlock.FACING, Direction.NORTH).setValue(ArcaneBoreBlock.BASE, Direction.UP));
        helper.setBlock(new BlockPos(1, 1, 5), Blocks.CHEST.defaultBlockState());
        return helper.getBlockEntity(new BlockPos(2, 2, 5), ArcaneBoreBlockEntity.class);
    }

    @GameTest(maxTicks = 400)
    public void itDigsAndFillsTheChest(GameTestHelper helper) {
        ArcaneBoreBlockEntity bore = build(helper);
        bore.setItem(0, new ItemStack(TCItems.FOCI.get("excavation")));
        bore.setItem(1, new ItemStack(Items.IRON_PICKAXE));
        helper.setBlock(new BlockPos(3, 1, 5), Blocks.REDSTONE_BLOCK.defaultBlockState());
        helper.succeedWhen(() -> {
            ChestBlockEntity chest = helper.getBlockEntity(new BlockPos(1, 1, 5), ChestBlockEntity.class);
            int found = 0;
            for (int i = 0; i < chest.getContainerSize(); i++) if (chest.getItem(i).is(Items.COBBLESTONE)) found += chest.getItem(i).getCount();
            if (found < 2) helper.fail("a pedra cavada vai para o baú da base, " + found + " até agora");
            if (bore.pickaxe().getDamageValue() < 2) helper.fail("e cada bloco gasta a picareta");
        });
    }

    @GameTest(maxTicks = 100)
    public void withoutPowerOrPickaxeItRests(GameTestHelper helper) {
        ArcaneBoreBlockEntity bore = build(helper);
        bore.setItem(0, new ItemStack(TCItems.FOCI.get("excavation")));
        helper.runAfterDelay(80, () -> {
            if (!helper.getBlockState(new BlockPos(2, 2, 2)).is(Blocks.STONE)) helper.fail("sem redstone e sem picareta, parada");
            helper.succeed();
        });
    }

    @GameTest
    public void theFocusSetsWidthSpeedAndFortune(GameTestHelper helper) {
        ArcaneBoreBlockEntity bore = build(helper);
        ItemStack focus = new ItemStack(TCItems.FOCI.get("excavation"));
        FocusItem.apply(focus, FocusUpgradeTable.POTENCY, 1);
        FocusItem.apply(focus, FocusUpgradeTable.ENLARGE, 2);
        FocusItem.apply(focus, FocusUpgradeTable.TREASURE, 3);
        bore.setItem(0, focus);
        bore.setItem(1, new ItemStack(Items.IRON_PICKAXE));
        if (bore.speed != 1 || bore.area != 1 || bore.fortune != 1) helper.fail("potência, ampliar e tesouro: " + bore.speed + " " + bore.area + " " + bore.fortune);
        if (!bore.hasFocus || !bore.hasPickaxe) helper.fail("foco e picareta reconhecidos");
        bore.setItem(1, new ItemStack(Items.IRON_SHOVEL));
        if (bore.hasPickaxe) helper.fail("pá não é picareta");
        helper.succeed();
    }

    @GameTest
    public void theBoreFallsWithoutItsBase(GameTestHelper helper) {
        build(helper);
        helper.setBlock(new BlockPos(2, 1, 5), Blocks.AIR.defaultBlockState());
        helper.assertBlockNotPresent(TCBlocks.ARCANE_BORE, new BlockPos(2, 2, 5));
        helper.succeed();
    }
}
