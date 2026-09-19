package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.block.entity.ArcaneSpaBlockEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEffects;
import net.thaumcraft.registry.TCItems;

/** O spa arcano, os sais de banho e os dois fluidos: o purificante e a morte líquida. */
public class SpaGameTest {
    @GameTest(maxTicks = 120)
    public void theSpaMakesPurifyingFluid(GameTestHelper helper) {
        helper.setBlock(new BlockPos(2, 1, 2), TCBlocks.ARCANE_SPA.defaultBlockState());
        ArcaneSpaBlockEntity spa = helper.getBlockEntity(new BlockPos(2, 1, 2), ArcaneSpaBlockEntity.class);
        try (Transaction t = Transaction.openOuter()) {
            spa.tank.insert(FluidVariant.of(Fluids.WATER), 2 * FluidConstants.BUCKET, t);
            t.commit();
        }
        spa.setItem(0, new ItemStack(TCItems.BATH_SALTS, 2));
        helper.succeedWhen(() -> {
            if (!helper.getBlockState(new BlockPos(2, 2, 2)).is(TCBlocks.PURIFYING_FLUID)) helper.fail("a água com o sal sai purificante por cima");
            if (spa.getItem(0).getCount() != 1 || spa.fluidMb() != 1000) helper.fail("gastando um sal e um balde");
        });
    }

    @GameTest(maxTicks = 120)
    public void withoutMixTheFluidGoesOutAsIs(GameTestHelper helper) {
        helper.setBlock(new BlockPos(2, 1, 2), TCBlocks.ARCANE_SPA.defaultBlockState());
        ArcaneSpaBlockEntity spa = helper.getBlockEntity(new BlockPos(2, 1, 2), ArcaneSpaBlockEntity.class);
        try (Transaction t = Transaction.openOuter()) {
            spa.tank.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET, t);
            t.commit();
        }
        spa.toggleMix();
        helper.succeedWhen(() -> {
            if (!helper.getBlockState(new BlockPos(2, 2, 2)).is(Blocks.WATER)) helper.fail("sem misturar, a água sai água");
        });
    }

    @GameTest
    public void thePurifyingFluidWardsAgainstWarp(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.STONE.defaultBlockState());
        helper.setBlock(new BlockPos(1, 2, 1), TCBlocks.PURIFYING_FLUID.defaultBlockState());
        var player = helper.makeMockServerPlayerInLevel();
        BlockPos at = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().getFluidState(at).entityInside(helper.getLevel(), at, player, InsideBlockEffectApplier.NOOP);
        if (!player.hasEffect(TCEffects.WARP_WARD)) helper.fail("a fonte dá a proteção contra a dobra");
        helper.assertBlockNotPresent(TCBlocks.PURIFYING_FLUID, new BlockPos(1, 2, 1));
        helper.succeed();
    }

    @GameTest(maxTicks = 200)
    public void liquidDeathDissolves(GameTestHelper helper) {
        for (int x = 0; x < 3; x++) for (int z = 0; z < 3; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE.defaultBlockState());
        helper.setBlock(new BlockPos(1, 2, 1), TCBlocks.LIQUID_DEATH.defaultBlockState());
        var pig = helper.spawn(EntityTypes.PIG, new BlockPos(1, 2, 1));
        pig.setNoAi(true);
        helper.succeedWhen(() -> {
            if (pig.isAlive()) helper.fail("o porco se dissolve");
        });
    }

    @GameTest(maxTicks = 260)
    public void bathSaltsTurnWaterPure(GameTestHelper helper) {
        for (int x = 0; x < 3; x++) for (int z = 0; z < 3; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE.defaultBlockState());
        for (int x = 0; x < 3; x++) for (int z = 0; z < 3; z++) if (x != 1 || z != 1) helper.setBlock(new BlockPos(x, 2, z), Blocks.STONE.defaultBlockState());
        helper.setBlock(new BlockPos(1, 2, 1), Blocks.WATER.defaultBlockState());
        Vec3 at = helper.absoluteVec(new Vec3(1.5, 2.2, 1.5));
        ItemEntity salts = new ItemEntity(helper.getLevel(), at.x, at.y, at.z, new ItemStack(TCItems.BATH_SALTS));
        salts.setDeltaMovement(Vec3.ZERO);
        helper.getLevel().addFreshEntity(salts);
        helper.succeedWhen(() -> {
            if (salts.isAlive()) helper.fail("os sais duram dez segundos");
            if (!helper.getBlockState(new BlockPos(1, 2, 1)).is(TCBlocks.PURIFYING_FLUID)) helper.fail("e a água vira purificante");
        });
    }
}
