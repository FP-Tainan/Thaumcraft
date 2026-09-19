package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/**
 * O minério infundido tem de largar o fragmento do aspecto dele, que é de onde vêm os fragmentos no
 * Thaumcraft 4.2.3.5.
 */
public class OreGameTest {
    /** As seis pedras existem, uma por primordial. */
    @GameTest
    public void thereIsOneStonePerPrimal(GameTestHelper helper) {
        if (TCBlocks.INFUSED_STONE.size() != 6) {
            helper.fail("deviam ser seis pedras infundidas, são " + TCBlocks.INFUSED_STONE.size());
        }
        for (String tag : new String[]{"air", "fire", "water", "earth", "order", "entropy"}) {
            if (TCBlocks.INFUSED_STONE.get(tag) == null) helper.fail("faltou a pedra de " + tag);
            if (TCItems.SHARDS.get(tag) == null) helper.fail("faltou o fragmento de " + tag);
        }
        helper.succeed();
    }

    /** Quebrada com picareta, ela larga o fragmento. */
    @GameTest
    public void breakingItGivesTheShard(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, TCBlocks.INFUSED_STONE.get("fire").defaultBlockState());
        var level = helper.getLevel();
        var drops = net.minecraft.world.level.block.Block.getDrops(
                helper.getBlockState(pos), level, helper.absolutePos(pos), null,
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL),
                new ItemStack(Items.IRON_PICKAXE));
        boolean found = drops.stream().anyMatch(drop -> drop.is(TCItems.SHARDS.get("fire")));
        if (!found) helper.fail("a pedra de fogo devia largar um fragmento de fogo, largou " + drops);

        // sem picareta não larga nada
        var barehanded = net.minecraft.world.level.block.Block.getDrops(
                helper.getBlockState(pos), level, helper.absolutePos(pos), null,
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL), ItemStack.EMPTY);
        if (!barehanded.isEmpty()) helper.fail("sem picareta não devia largar nada");
        helper.succeed();
    }

    private static java.util.List<ItemStack> drops(GameTestHelper helper, BlockPos pos, ItemStack tool) {
        return net.minecraft.world.level.block.Block.getDrops(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos), null,
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL), tool);
    }

    /** O cinábrio larga a si mesmo; o âmbar preso em pedra larga âmbar. Os dois fundem no forno. */
    @GameTest
    public void cinnabarAndAmberOresDropAndSmelt(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, TCBlocks.CINNABAR_ORE.defaultBlockState());
        if (drops(helper, pos, new ItemStack(Items.IRON_PICKAXE)).stream().noneMatch(d -> d.is(TCBlocks.CINNABAR_ORE.asItem()))) {
            helper.fail("o cinábrio larga a si mesmo");
        }
        helper.setBlock(pos, TCBlocks.AMBER_ORE.defaultBlockState());
        var amber = drops(helper, pos, new ItemStack(Items.IRON_PICKAXE));
        if (amber.size() != 1 || !amber.get(0).is(net.thaumcraft.registry.TCResources.get("amber"))) helper.fail("o âmbar preso larga um âmbar; largou " + amber);
        var recipes = helper.getLevel().getServer().getRecipeManager();
        var input = new net.minecraft.world.item.crafting.SingleRecipeInput(new ItemStack(TCBlocks.CINNABAR_ORE));
        var smelted = recipes.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMELTING, input, helper.getLevel());
        if (smelted.isEmpty() || !smelted.get().value().assemble(input).is(net.thaumcraft.registry.TCResources.get("quicksilver"))) {
            helper.fail("o cinábrio fundido vira mercúrio");
        }
        helper.succeed();
    }

    /** O aglomerado precisa de apoio, larga seis fragmentos e o misto um de cada. */
    @GameTest
    public void aCrystalClusterNeedsSupportAndGivesSixShards(GameTestHelper helper) {
        BlockPos support = new BlockPos(1, 1, 1), pos = new BlockPos(1, 2, 1);
        helper.setBlock(support, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        helper.setBlock(pos, TCBlocks.CRYSTAL_CLUSTERS.get("fire").defaultBlockState());
        var fire = drops(helper, pos, ItemStack.EMPTY);
        if (fire.stream().mapToInt(ItemStack::getCount).sum() != 6 || !fire.get(0).is(TCItems.SHARDS.get("fire"))) helper.fail("seis fragmentos de fogo; deu " + fire);
        helper.setBlock(pos, TCBlocks.CRYSTAL_CLUSTERS.get("balanced").defaultBlockState());
        if (drops(helper, pos, ItemStack.EMPTY).size() != 6) helper.fail("o misto dá um de cada");
        helper.setBlock(support, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        helper.runAfterDelay(3, () -> {
            helper.assertBlockNotPresent(TCBlocks.CRYSTAL_CLUSTERS.get("balanced"), pos);
            helper.succeed();
        });
    }
}
