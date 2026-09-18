package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.crafting.ArcaneRecipes;
import net.thaumcraft.entity.PrimalOrbEntity;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.Focuses;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;

import java.util.ArrayList;
import java.util.List;

/** O foco Primordial tem de seguir o {@code ItemFocusPrimal} e a {@code EntityPrimalOrb}. */
public class PrimalFocusGameTest {
    @GameTest
    public void theCostIsDrawnFromFiftyToTwoHundredFifty(GameTestHelper helper) {
        for (long t = 0; t < 200_000L; t += 200L) {
            AspectList cost = Focuses.primalCost(t + 1);
            if (cost.size() != 6) helper.fail("o custo leva os seis primários");
            for (Aspect aspect : cost.getAspects()) {
                int amount = cost.getAmount(aspect);
                if (amount < 50 || amount > 250 || amount % 50 != 0) helper.fail("custo fora da tabela: " + amount);
            }
        }
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void theWandShootsAnOrbAndPays(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        Vec3 spot = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        player.snapTo(spot.x, spot.y, spot.z, 0.0f, 0.0f);
        ItemStack wand = new ItemStack(TCItems.WAND);
        wand.set(TCComponents.WAND_FOCUS, "primal");
        AspectList vis = new AspectList();
        for (Aspect primal : Aspects.primals()) vis.add(primal, 2500);
        wand.set(TCComponents.WAND_VIS, vis);
        player.getInventory().setItem(0, wand);
        player.getInventory().setSelectedSlot(0);

        FocusItem focus = Focuses.on(wand);
        if (focus == null) throw helper.assertionException("o foco primordial devia estar registrado");
        if (!Focuses.tick(helper.getLevel(), player, wand, focus)) helper.fail("com vis de sobra, o foco atira");
        // meio segundo de espera entre um tiro e outro
        if (Focuses.tick(helper.getLevel(), player, wand, focus)) helper.fail("o segundo tiro tem de esperar");
        AspectList left = wand.get(TCComponents.WAND_VIS);
        for (Aspect primal : Aspects.primals()) {
            int spent = 2500 - left.getAmount(primal);
            // a ponteira de ferro cobra dez por cento a mais
            if (spent < 55 || spent > 275) helper.fail("gasto de " + primal.tag() + " fora da tabela: " + spent);
        }
        var orbs = helper.getLevel().getEntities(TCEntities.PRIMAL_ORB, player.getBoundingBox().inflate(3.0), e -> true);
        if (orbs.size() != 1) helper.fail("sai uma esfera; saíram " + orbs.size());
        if (orbs.get(0).distanceTo(player) > 2.0) helper.fail("a esfera nasce no olho de quem atira: " + orbs.get(0).position() + " / " + player.position());
        helper.succeed();
    }

    @GameTest(maxTicks = 60)
    public void theOrbExplodesOnTheWall(GameTestHelper helper) {
        for (int y = 1; y < 4; y++) {
            for (int x = 0; x < 3; x++) helper.setBlock(new BlockPos(x, y, 2), Blocks.DIRT.defaultBlockState());
        }
        PrimalOrbEntity orb = new PrimalOrbEntity(TCEntities.PRIMAL_ORB, helper.getLevel());
        Vec3 start = helper.absoluteVec(new Vec3(1.5, 2.5, 0.5));
        orb.setPos(start.x, start.y, start.z);
        orb.setDeltaMovement(0.0, 0.0, 0.5);
        helper.getLevel().addFreshEntity(orb);
        helper.succeedWhen(() -> {
            if (!orb.isRemoved()) throw helper.assertionException("a esfera ainda não bateu");
            helper.assertBlockNotPresent(Blocks.DIRT, new BlockPos(1, 2, 2));
        });
    }

    @GameTest
    public void theRecipeHasTheCharmInTheMiddle(GameTestHelper helper) {
        ItemStack d = new ItemStack(Items.DIAMOND), q = new ItemStack(Items.QUARTZ);
        List<ItemStack> grid = new ArrayList<>(List.of(d, q, d, q, new ItemStack(TCResources.get("primal_charm")), q, d, q, d));
        var recipe = ArcaneRecipes.find(grid);
        if (recipe == null || !recipe.result().is(TCItems.FOCI.get("primal"))) helper.fail("a grade CQC/Q#Q/CQC dá o foco");
        grid.set(4, ItemStack.EMPTY);
        if (ArcaneRecipes.find(grid) != null) helper.fail("sem o amuleto no meio não sai nada");
        helper.succeed();
    }
}
