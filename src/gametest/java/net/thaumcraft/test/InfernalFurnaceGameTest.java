package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.block.InfernalFurnaceBlock;
import net.thaumcraft.block.InfernalFurnaceStructure;
import net.thaumcraft.block.entity.InfernalFurnaceBlockEntity;
import net.thaumcraft.block.entity.InfernalFurnaceNozzleBlockEntity;
import net.thaumcraft.crafting.SmeltingBonus;
import net.thaumcraft.registry.TCBlocks;

/** A fornalha infernal da 4.2.3.5: a montagem, a fundição pela boca, o desmonte e o bônus. */
public class InfernalFurnaceGameTest {
    /** O cubo de 1,1,1 a 3,3,3, com a grade no meio da face sul da camada do meio. */
    static void build(GameTestHelper helper, boolean bars) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    boolean corner = x != 1 && z != 1;
                    BlockState state = corner ? Blocks.NETHER_BRICKS.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState();
                    if (x == 1 && z == 1 && y == 1) state = Blocks.LAVA.defaultBlockState();
                    if (x == 1 && z == 1 && y == 2) state = Blocks.AIR.defaultBlockState();
                    helper.setBlock(new BlockPos(1 + x, 1 + y, 1 + z), state);
                }
            }
        }
        if (bars) helper.setBlock(new BlockPos(2, 2, 3), Blocks.IRON_BARS.defaultBlockState());
    }

    static void form(GameTestHelper helper) {
        build(helper, true);
        InfernalFurnaceStructure.replace(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)));
    }

    @GameTest
    public void theCubeMustHaveExactlyOneGrate(GameTestHelper helper) {
        build(helper, false);
        if (InfernalFurnaceStructure.fits(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)))) helper.fail("sem a grade não forma");
        helper.setBlock(new BlockPos(2, 2, 3), Blocks.IRON_BARS.defaultBlockState());
        if (!InfernalFurnaceStructure.fits(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)))) helper.fail("com uma grade forma");
        helper.setBlock(new BlockPos(2, 1, 3), Blocks.IRON_BARS.defaultBlockState());
        if (InfernalFurnaceStructure.fits(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)))) helper.fail("grade fora da camada do meio não vale");
        helper.succeed();
    }

    @GameTest
    public void formingNumbersEveryBlock(GameTestHelper helper) {
        form(helper);
        if (InfernalFurnaceBlock.part(helper.getBlockState(new BlockPos(2, 2, 2))) != 0) helper.fail("a lava vira o centro");
        BlockState mouth = helper.getBlockState(new BlockPos(2, 2, 3));
        if (InfernalFurnaceBlock.part(mouth) != 10 || mouth.getValue(InfernalFurnaceBlock.FACING) != Direction.NORTH) helper.fail("a grade vira a boca, olhando para o centro");
        if (InfernalFurnaceBlock.part(helper.getBlockState(new BlockPos(1, 1, 1))) != 1) helper.fail("o canto noroeste é o 1");
        if (InfernalFurnaceBlock.part(helper.getBlockState(new BlockPos(3, 3, 3))) != 9) helper.fail("o canto sudeste é o 9");
        if (!helper.getBlockState(new BlockPos(2, 3, 2)).isAir()) helper.fail("o alto do centro fica aberto");
        helper.getBlockEntity(new BlockPos(2, 2, 2), InfernalFurnaceBlockEntity.class);
        // os bicos: os do meio das paredes do meio e o de baixo encostam no centro
        InfernalFurnaceNozzleBlockEntity nozzle = helper.getBlockEntity(new BlockPos(1, 2, 2), InfernalFurnaceNozzleBlockEntity.class);
        if (nozzle.getSuctionAmount(Direction.WEST) != 128) helper.fail("sem pressa, o bico puxa Ignis com 128");
        helper.succeed();
    }

    @GameTest(maxTicks = 400)
    public void whatFallsInTheLavaComesOutOfTheMouth(GameTestHelper helper) {
        form(helper);
        Vec3 above = helper.absoluteVec(new Vec3(2.5, 3.5, 2.5));
        ItemEntity ore = new ItemEntity(helper.getLevel(), above.x, above.y, above.z, new ItemStack(Items.RAW_IRON, 2));
        ore.setDeltaMovement(Vec3.ZERO);
        helper.getLevel().addFreshEntity(ore);
        helper.succeedWhen(() -> {
            int ingots = helper.getEntities(EntityTypes.ITEM, new BlockPos(2, 2, 4), 2.5).stream()
                    .filter(e -> e.getItem().is(Items.IRON_INGOT)).mapToInt(e -> e.getItem().getCount()).sum();
            if (ingots < 2) throw helper.assertionException("os dois saem fundidos pela boca, saíram " + ingots);
        });
    }

    @GameTest
    public void whatDoesNotSmeltIsDestroyed(GameTestHelper helper) {
        form(helper);
        InfernalFurnaceBlockEntity furnace = helper.getBlockEntity(new BlockPos(2, 2, 2), InfernalFurnaceBlockEntity.class);
        if (!furnace.addItemsToInventory(new ItemStack(Items.STICK))) helper.fail("a fornalha engole");
        if (furnace.firstStack() != null) helper.fail("o que não funde some");
        furnace.addItemsToInventory(new ItemStack(Items.RAW_IRON));
        if (furnace.firstStack() == null) helper.fail("o que funde fica");
        helper.succeed();
    }

    @GameTest
    public void breakingAPartUndoesTheFurnace(GameTestHelper helper) {
        form(helper);
        helper.getLevel().destroyBlock(helper.absolutePos(new BlockPos(1, 1, 1)), false);
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.OBSIDIAN, new BlockPos(2, 1, 1));
            helper.assertBlockPresent(Blocks.NETHER_BRICKS, new BlockPos(3, 3, 3));
            helper.assertBlockPresent(Blocks.IRON_BARS, new BlockPos(2, 2, 3));
            helper.assertBlockNotPresent(TCBlocks.INFERNAL_FURNACE, new BlockPos(2, 2, 2));
        });
    }

    @GameTest
    public void theSmeltingBonusTable(GameTestHelper helper) {
        if (SmeltingBonus.of(new ItemStack(Items.RAW_IRON)) != Items.IRON_NUGGET) helper.fail("ferro bruto dá pepita de ferro");
        if (SmeltingBonus.of(new ItemStack(Items.GOLD_ORE)) != Items.GOLD_NUGGET) helper.fail("minério de ouro dá pepita de ouro");
        if (SmeltingBonus.size() != 22) helper.fail("vinte e dois pares hoje (as pepitas de carne e os aglomerados de estanho, prata e chumbo entraram), são " + SmeltingBonus.size());
        if (SmeltingBonus.of(new ItemStack(Items.BEEF)) != net.thaumcraft.registry.TCItems.NUGGET_BEEF) helper.fail("carne de vaca dá pepita de vaca");
        helper.succeed();
    }
}
