package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.entity.EtherealBloomBlockEntity;
import net.thaumcraft.entity.AlumentumEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;
import net.thaumcraft.research.Knowledges;

/** O que o clique direito faz nos recursos do {@code ItemResource}, e a Flor Etérea com o tile dela. */
public class ResourceUseGameTest {
    @GameTest(maxTicks = 20)
    public void alumentumIsThrown(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        Vec3 spot = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        player.snapTo(spot.x, spot.y, spot.z, 0.0f, 0.0f);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TCItems.ALUMENTUM, 2));
        player.getItemInHand(InteractionHand.MAIN_HAND).use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        if (player.getItemInHand(InteractionHand.MAIN_HAND).getCount() != 1) helper.fail("o arremesso gasta um");
        var thrown = helper.getLevel().getEntities(TCEntities.ALUMENTUM, player.getBoundingBox().inflate(3.0), e -> true);
        if (thrown.size() != 1) helper.fail("sai um Alumentum; saíram " + thrown.size());
        helper.succeed();
    }

    @GameTest(maxTicks = 60)
    public void alumentumExplodesWhereItLands(GameTestHelper helper) {
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.DIRT.defaultBlockState());
        }
        AlumentumEntity alumentum = new AlumentumEntity(TCEntities.ALUMENTUM, helper.getLevel());
        Vec3 start = helper.absoluteVec(new Vec3(1.5, 3.5, 1.5));
        alumentum.setPos(start.x, start.y, start.z);
        helper.getLevel().addFreshEntity(alumentum);
        helper.succeedWhen(() -> {
            if (!alumentum.isRemoved()) helper.fail("ainda caindo");
            boolean hole = false;
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) hole |= helper.getBlockState(new BlockPos(x, 1, z)).isAir();
            }
            if (!hole) helper.fail("a explosão devia abrir um buraco na terra");
        });
    }

    @GameTest
    public void theKnowledgeFragmentGivesOneOrTwoOfEachPrimal(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TCResources.get("knowledge_fragment")));
        var before = Knowledges.of(player).pool().copy();
        player.getItemInHand(InteractionHand.MAIN_HAND).use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) helper.fail("o fragmento some ao ser lido");
        var after = Knowledges.of(player).pool();
        for (Aspect primal : Aspects.primals()) {
            int gained = after.getAmount(primal) - before.getAmount(primal);
            if (gained < 1 || gained > 2) helper.fail(primal.tag() + " devia subir um ou dois; subiu " + gained);
        }
        helper.succeed();
    }

    @GameTest(maxTicks = 10)
    public void theEtherealBloomGrowsWithItsTile(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE.defaultBlockState());
        helper.setBlock(new BlockPos(1, 1, 1), TCBlocks.ETHEREAL_BLOOM.defaultBlockState());
        helper.succeedWhen(() -> {
            if (!(helper.getBlockEntity(new BlockPos(1, 1, 1), EtherealBloomBlockEntity.class) instanceof EtherealBloomBlockEntity bloom)
                    || bloom.growthCounter < 5) {
                helper.fail("a flor tem de contar o crescimento");
            }
        });
    }
}
