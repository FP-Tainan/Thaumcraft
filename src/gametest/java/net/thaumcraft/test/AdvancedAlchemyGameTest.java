package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.block.AdvancedAlchemicalFurnaceBlock;
import net.thaumcraft.block.AdvancedAlchemicalFurnaceStructure;
import net.thaumcraft.block.EssentiaReservoirBlock;
import net.thaumcraft.block.entity.AdvancedAlchemicalFurnaceBlockEntity;
import net.thaumcraft.block.entity.AdvancedAlchemicalFurnaceNozzleBlockEntity;
import net.thaumcraft.block.entity.EssentiaReservoirBlockEntity;
import net.thaumcraft.crafting.InfusionRecipes;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/** A fornalha alquímica avançada e o reservatório de essência da 4.2.3.5. */
public class AdvancedAlchemyGameTest {
    static final BlockPos CENTRE = new BlockPos(2, 1, 2);

    /** O molde: a fornalha no meio, as construções avançadas em volta; em cima, alambiques nos cantos e construções. */
    static void mould(GameTestHelper helper) {
        for (int a = -1; a <= 1; a++) {
            for (int c = -1; c <= 1; c++) {
                BlockPos low = CENTRE.offset(a, 0, c);
                helper.setBlock(low, a == 0 && c == 0 ? TCBlocks.ALCHEMICAL_FURNACE : TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT);
                if (a == 0 && c == 0) continue;
                helper.setBlock(low.above(), a != 0 && c != 0 ? TCBlocks.ALEMBIC : TCBlocks.ALCHEMICAL_CONSTRUCT);
            }
        }
    }

    static AdvancedAlchemicalFurnaceBlockEntity form(GameTestHelper helper) {
        mould(helper);
        AdvancedAlchemicalFurnaceStructure.build(helper.getLevel(), helper.absolutePos(CENTRE));
        return helper.getBlockEntity(CENTRE, AdvancedAlchemicalFurnaceBlockEntity.class);
    }

    @GameTest
    public void theMouldMustBeComplete(GameTestHelper helper) {
        mould(helper);
        if (!AdvancedAlchemicalFurnaceStructure.fits(helper.getLevel(), helper.absolutePos(CENTRE))) helper.fail("o molde completo serve");
        helper.setBlock(CENTRE.offset(1, 1, 1), TCBlocks.ALCHEMICAL_CONSTRUCT);
        if (AdvancedAlchemicalFurnaceStructure.fits(helper.getLevel(), helper.absolutePos(CENTRE))) helper.fail("canto de cima sem alambique não serve");
        helper.succeed();
    }

    @GameTest
    public void formingNumbersTheParts(GameTestHelper helper) {
        form(helper);
        int[][] expect = {{1, 0, 0, 1}, {1, 0, 1, 4}, {0, 1, 1, 3}, {-1, 1, -1, 2}};
        for (int[] e : expect) {
            var state = helper.getBlockState(CENTRE.offset(e[0], e[1], e[2]));
            if (!state.is(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE) || state.getValue(AdvancedAlchemicalFurnaceBlock.PART) != e[3]) {
                helper.fail("a parte em " + e[0] + "," + e[1] + "," + e[2] + " é a " + e[3]);
            }
        }
        helper.getBlockEntity(CENTRE.east(), AdvancedAlchemicalFurnaceNozzleBlockEntity.class);
        helper.succeed();
    }

    @GameTest
    public void whatFallsInIsBrokenIntoEssentiaAndTheNozzleGivesIt(GameTestHelper helper) {
        AdvancedAlchemicalFurnaceBlockEntity furnace = form(helper);
        furnace.heat = 500;
        furnace.power1 = 500;
        furnace.power2 = 500;
        int size = ObjectAspects.of(new ItemStack(Items.OAK_LOG)).visSize();
        if (size <= 0) helper.fail("a tora tem aspectos");
        Vec3 above = helper.absoluteVec(new Vec3(2.5, 2.2, 2.5));
        ItemEntity log = new ItemEntity(helper.getLevel(), above.x, above.y, above.z, new ItemStack(Items.OAK_LOG));
        log.setDeltaMovement(Vec3.ZERO);
        helper.getLevel().addFreshEntity(log);
        helper.succeedWhen(() -> {
            if (!log.isRemoved()) throw helper.assertionException("a tora é desfeita");
            if (furnace.vis != size) throw helper.assertionException("a fornalha guarda o que a tora tinha");
            if (furnace.heat != 500 - size * 2 && furnace.heat > 500 - size * 2) throw helper.assertionException("custa o dobro em calor");
            AdvancedAlchemicalFurnaceNozzleBlockEntity nozzle = helper.getBlockEntity(CENTRE.east(), AdvancedAlchemicalFurnaceNozzleBlockEntity.class);
            var first = nozzle.getEssentiaType(Direction.EAST);
            if (first == null || nozzle.takeEssentia(first, 1, Direction.EAST) != 1) throw helper.assertionException("o bico solta para fora");
            if (nozzle.takeEssentia(first, 1, Direction.WEST) != 0) throw helper.assertionException("só pelo lado de fora");
        });
    }

    @GameTest
    public void takingAPartUndoesTheFurnace(GameTestHelper helper) {
        form(helper);
        helper.getLevel().destroyBlock(helper.absolutePos(CENTRE.offset(1, 1, 1)), false);
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(TCBlocks.ALCHEMICAL_FURNACE, CENTRE);
            helper.assertBlockPresent(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT, CENTRE.east());
            helper.assertBlockPresent(TCBlocks.ALCHEMICAL_CONSTRUCT, CENTRE.offset(0, 1, 1));
            helper.assertBlockPresent(TCBlocks.ALEMBIC, CENTRE.offset(-1, 1, -1));
        });
    }

    @GameTest
    public void theReservoirHoldsAnyMixUpTo256(GameTestHelper helper) {
        BlockPos at = new BlockPos(1, 1, 1);
        helper.setBlock(at, TCBlocks.ESSENTIA_RESERVOIR.defaultBlockState().setValue(EssentiaReservoirBlock.FACING, Direction.DOWN));
        EssentiaReservoirBlockEntity te = helper.getBlockEntity(at, EssentiaReservoirBlockEntity.class);
        if (te.addEssentia(Aspects.FIRE, 200, Direction.DOWN) != 200) helper.fail("entra pelo bocal");
        if (te.addEssentia(Aspects.WATER, 100, Direction.DOWN) != 56) helper.fail("até 256, misturado");
        if (te.addEssentia(Aspects.WATER, 10, Direction.UP) != 0) helper.fail("só pelo bocal");
        if (te.getSuctionAmount(Direction.DOWN) != 0) helper.fail("cheio, para de puxar");
        int signal = helper.getBlockState(at).getAnalogOutputSignal(helper.getLevel(), helper.absolutePos(at), Direction.NORTH);
        if (signal != 15) helper.fail("cheio dá 15 no comparador, deu " + signal);
        if (te.takeEssentia(Aspects.FIRE, 5, Direction.DOWN) != 5) helper.fail("sai pelo bocal");
        helper.succeed();
    }

    @GameTest
    public void theWandTurnsTheNozzle(GameTestHelper helper) {
        BlockPos at = new BlockPos(1, 1, 1);
        helper.setBlock(at, TCBlocks.ESSENTIA_RESERVOIR.defaultBlockState());
        var player = helper.makeMockServerPlayerInLevel();
        EssentiaReservoirBlockEntity te = helper.getBlockEntity(at, EssentiaReservoirBlockEntity.class);
        te.onWand(helper.getLevel(), ItemStack.EMPTY, player, helper.absolutePos(at), Direction.NORTH);
        if (helper.getBlockState(at).getValue(EssentiaReservoirBlock.FACING) != Direction.SOUTH) helper.fail("a varinha vira o bocal para longe da face batida");
        helper.succeed();
    }

    @GameTest
    public void theReservoirInfusionExists(GameTestHelper helper) {
        if (InfusionRecipes.ALL.stream().noneMatch(r -> r.result().is(TCItems.ESSENTIA_RESERVOIR))) helper.fail("falta a infusão do reservatório");
        helper.succeed();
    }
}
