package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.thaumcraft.item.LootBagItem;
import net.thaumcraft.loot.ThaumLoot;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;

/** As sacolas de tesouro e o que o Thaumcraft põe nos baús do mundo, como no {@code Config.initLoot}. */
public class LootGameTest {
    @GameTest
    public void aBagSpillsEightToTwelveThings(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(TCItems.LOOT_BAG, 2));
        var box = new net.minecraft.world.phys.AABB(player.blockPosition()).inflate(3);
        int before = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, box).size();
        TCItems.LOOT_BAG.use(helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        int spilled = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, box).size() - before;
        if (spilled < 8 || spilled > 12) helper.fail("de oito a doze coisas; saíram " + spilled);
        if (player.getMainHandItem().getCount() != 1) helper.fail("e a sacola se gasta");
        helper.succeed();
    }

    @GameTest
    public void theBagsFollowTheirTables(GameTestHelper helper) {
        RandomSource random = RandomSource.create(42);
        var access = helper.getLevel().registryAccess();
        int coins = 0, rolls = 2000, star = 0;
        for (int a = 0; a < rolls; a++) {
            if (LootBagItem.generateLoot(0, random, access).is(TCResources.get("gold_coin"))) coins++;
            if (LootBagItem.generateLoot(2, random, access).is(net.minecraft.world.item.Items.NETHER_STAR)) star++;
        }
        int total = ThaumLoot.COMMON.stream().mapToInt(ThaumLoot.Entry::weight).sum();
        float expected = 2500.0f / total;
        if (Math.abs(coins / (float) rolls - expected) > 0.05f) helper.fail("moedas saem na proporção do peso " + expected + "; saíram " + coins / (float) rolls);
        if (ThaumLoot.RARE.stream().noneMatch(e -> e.stack().get().is(net.minecraft.world.item.Items.NETHER_STAR))) helper.fail("a rara pode dar estrela do Nether");
        if (ThaumLoot.COMMON.stream().anyMatch(e -> e.stack().get().is(net.minecraft.world.item.Items.NETHER_STAR))) helper.fail("a comum não");
        // o livro sai encantado
        boolean book = false;
        for (int a = 0; a < 4000 && !book; a++) {
            ItemStack s = LootBagItem.generateLoot(1, random, access);
            if (s.is(net.minecraft.world.item.Items.BOOK)) helper.fail("livro comum não sai: sai encantado");
            book = s.is(net.minecraft.world.item.Items.ENCHANTED_BOOK);
        }
        if (!book) helper.fail("o livro encantado aparece de vez em quando");
        helper.succeed();
    }

    @GameTest
    public void theStrongholdLibraryHidesKnowledge(GameTestHelper helper) {
        var level = helper.getLevel();
        var table = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.STRONGHOLD_LIBRARY);
        var params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, helper.absoluteVec(net.minecraft.world.phys.Vec3.ZERO))
                .create(LootContextParamSets.CHEST);
        boolean fragment = false, bag = false;
        for (int a = 0; a < 300; a++) {
            for (ItemStack s : table.getRandomItems(params, a)) {
                if (s.is(TCResources.get("knowledge_fragment"))) fragment = true;
                if (s.getItem() instanceof LootBagItem) bag = true;
            }
        }
        if (!fragment || !bag) helper.fail("a biblioteca da fortaleza guarda fragmentos de conhecimento e sacolas");
        helper.succeed();
    }
}
