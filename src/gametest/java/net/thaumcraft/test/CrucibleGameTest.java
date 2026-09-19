package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.entity.CrucibleBlockEntity;
import net.thaumcraft.crafting.CrucibleRecipe;
import net.thaumcraft.crafting.CrucibleRecipes;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/**
 * O crisol tem de ferver e fazer contas como o do Thaumcraft 4.2.3.5.
 */
public class CrucibleGameTest {
    /** Ele só ferve com fogo por baixo, água dentro e passando de cento e cinquenta graus. */
    @GameTest
    public void itOnlyBoilsWithFireAndWater(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, TCBlocks.CRUCIBLE.defaultBlockState());
        CrucibleBlockEntity crucible = helper.getBlockEntity(pos, CrucibleBlockEntity.class);
        if (crucible.boiling()) helper.fail("sem água e sem fogo não devia ferver");

        crucible.setWater(true);
        if (crucible.boiling()) helper.fail("com água mas sem fogo ainda não ferve");
        if (CrucibleBlockEntity.BOILING != 150) helper.fail("o ponto de fervura do original é cento e cinquenta");
        if (CrucibleBlockEntity.MAX_HEAT != 200) helper.fail("ele chega a duzentos, como no original");
        helper.succeed();
    }

    /** As receitas de crisol vieram do original e cobram o que têm de cobrar. */
    /**
     * Estanho, prata e chumbo pelas etiquetas c: o {@code TransTin} multiplica qualquer pepita de estanho (a do
     * Thaumcraft está na etiqueta) em três, e o aglomerado dá a pepita na fornalha infernal.
     */
    @GameTest
    public void otherMetalsGoByTags(GameTestHelper helper) {
        var tinNugget = net.thaumcraft.registry.TCResources.get("tin_nugget");
        AspectList water = new AspectList().add(Aspects.METAL, 2).add(Aspects.CRYSTAL, 1);
        CrucibleRecipe trans = CrucibleRecipes.find(water, new ItemStack(tinNugget));
        if (trans == null || !trans.research().equals("TRANSTIN")) helper.fail("a pepita de estanho devia fechar o TransTin");
        if (!trans.result().is(tinNugget) || trans.result().getCount() != 3) helper.fail("o TransTin dá três pepitas de estanho");
        for (String key : new String[]{"PURETIN", "PURESILVER", "PURELEAD", "TRANSSILVER", "TRANSLEAD"}) {
            if (CrucibleRecipes.ALL.stream().noneMatch(r -> r.research().equals(key))) helper.fail("faltou a receita " + key);
        }
        var bonus = net.thaumcraft.crafting.SmeltingBonus.of(new ItemStack(net.thaumcraft.registry.TCResources.get("native_lead_cluster")));
        if (bonus != net.thaumcraft.registry.TCResources.get("lead_nugget")) helper.fail("o aglomerado de chumbo dá pepita de chumbo");
        helper.succeed();
    }

    @GameTest
    public void recipesCameFromTheOriginal(GameTestHelper helper) {
        if (CrucibleRecipes.ALL.size() < 15) {
            helper.fail("só " + CrucibleRecipes.ALL.size() + " receitas de crisol; deviam ser mais");
        }
        // o fragmento equilibrado sai de dois de cada um dos outros cinco primários
        AspectList rich = new AspectList();
        for (var aspect : Aspects.primals()) rich.add(aspect, 2);
        CrucibleRecipe balanced = CrucibleRecipes.find(rich, new ItemStack(TCItems.SHARDS.get("air")));
        if (balanced == null) helper.fail("faltou a receita do fragmento equilibrado");
        if (!balanced.result().is(TCItems.SHARD_BALANCED)) helper.fail("ela devia dar um fragmento equilibrado");

        // e o que sobra na água é o que não foi cobrado
        AspectList left = balanced.removeFrom(rich);
        if (left.getAmount(Aspects.AIR) != 2) helper.fail("o ar entrou como catalisador, não como custo");
        if (left.getAmount(Aspects.FIRE) != 0) helper.fail("o fogo devia ter sido todo cobrado");

        // sem o bastante na água, a receita não fecha
        if (CrucibleRecipes.find(new AspectList().add(Aspects.FIRE, 1),
                new ItemStack(TCItems.SHARDS.get("air"))) != null) {
            helper.fail("com água quase vazia nada devia fechar");
        }
        helper.succeed();
    }

    /** Quem não pesquisou não fabrica: a regra do original vale no crisol e na bancada. */
    @GameTest
    public void recipesNeedTheirResearch(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        // uma receita de crisol que pede pesquisa de verdade
        CrucibleRecipe gated = null;
        for (CrucibleRecipe recipe : CrucibleRecipes.ALL) {
            if (net.thaumcraft.research.Researches.get(recipe.research()) != null) {
                gated = recipe;
                break;
            }
        }
        if (gated == null) helper.fail("nenhuma receita de crisol pede pesquisa que exista");

        if (net.thaumcraft.research.ResearchManager.knows(player, gated.research())) {
            helper.fail("quem não pesquisou não devia poder usar " + gated.research());
        }
        var knowledge = net.thaumcraft.research.Knowledges.of(player);
        knowledge.completeResearch(gated.research());
        net.thaumcraft.research.Knowledges.save(player, knowledge);
        if (!net.thaumcraft.research.ResearchManager.knows(player, gated.research())) {
            helper.fail("depois de pesquisar devia poder");
        }
        // receita sem pesquisa marcada passa livre, como as que o mod deixa abertas
        if (!net.thaumcraft.research.ResearchManager.knows(player, "")) {
            helper.fail("receita sem pesquisa marcada devia passar livre");
        }
        helper.succeed();
    }

    /** O frasco do original não se enche no crisol: enche-se no jarro (oito) e se despeja nele. */
    @GameTest
    public void aPhialFillsFromAJar(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, TCBlocks.JAR.defaultBlockState());
        var jar = helper.getBlockEntity(pos, net.thaumcraft.block.entity.JarBlockEntity.class);
        jar.addToContainer(Aspects.FIRE, 12);
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack phial = new ItemStack(TCItems.PHIAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, phial);
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(pos)),
                net.minecraft.core.Direction.UP, helper.absolutePos(pos), false);
        TCItems.PHIAL.useOn(new net.minecraft.world.item.context.UseOnContext(helper.getLevel(), player,
                net.minecraft.world.InteractionHand.MAIN_HAND, phial, hit));
        if (jar.amount() != 4) helper.fail("o frasco devia ter levado oito do jarro, sobrou " + jar.amount());
        boolean found = false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack in = player.getInventory().getItem(slot);
            if (in.is(TCItems.PHIAL) && net.thaumcraft.item.PhialItem.aspectOf(in) == Aspects.FIRE) found = true;
        }
        if (!found) helper.fail("devia ter aparecido um frasco de fogo no inventário");
        helper.succeed();
    }

    private static CrucibleBlockEntity boiling(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos.below(), Blocks.LAVA.defaultBlockState());
        helper.setBlock(pos, TCBlocks.CRUCIBLE.defaultBlockState());
        CrucibleBlockEntity crucible = helper.getBlockEntity(pos, CrucibleBlockEntity.class);
        crucible.setWater(true);
        for (int i = 0; i <= CrucibleBlockEntity.MAX_HEAT; i++) {
            CrucibleBlockEntity.tick(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), crucible);
        }
        if (!crucible.boiling()) helper.fail("com lava embaixo e água dentro ele devia ferver");
        return crucible;
    }

    /**
     * O que se joga dentro, fervendo, se desfaz nos aspectos que tem. A conta do original anda o índice e encolhe a pilha
     * ao mesmo tempo, então de uma vez só vai uma parte (de três pedras, duas); o resto vai no toque seguinte.
     */
    @GameTest
    public void whatFallsInDissolves(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        CrucibleBlockEntity crucible = boiling(helper, pos);
        var where = helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 2.5, 1.5));
        ItemEntity thrown = new ItemEntity(helper.getLevel(), where.x, where.y, where.z, new ItemStack(Items.STONE, 3));
        helper.getLevel().addFreshEntity(thrown);
        crucible.attemptSmelt(thrown);
        if (crucible.aspects().getAmount(Aspects.EARTH) != 4) {
            helper.fail("de três pedras, duas deviam virar quatro de terra, deu " + crucible.aspects().getAmount(Aspects.EARTH));
        }
        if (thrown.getItem().getCount() != 1) helper.fail("devia sobrar uma pedra");
        crucible.attemptSmelt(thrown);
        if (thrown.isAlive() || crucible.aspects().getAmount(Aspects.EARTH) != 6) helper.fail("no segundo toque a última pedra devia ir");
        helper.succeed();
    }

    /** Receita sem quem jogou (um funil) não fecha; com o jogador que pesquisou, sai flutuando e bebe 50 mB. */
    @GameTest
    public void recipesNeedAThrowerAndDrinkWater(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        CrucibleBlockEntity crucible = boiling(helper, pos);
        for (var aspect : Aspects.primals()) crucible.aspects().add(aspect, 2);
        var where = helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 2.5, 1.5));
        ItemEntity loose = new ItemEntity(helper.getLevel(), where.x, where.y, where.z, new ItemStack(TCItems.SHARDS.get("air")));
        helper.getLevel().addFreshEntity(loose);
        crucible.attemptSmelt(loose);
        if (!helper.getLevel().getEntities(net.thaumcraft.registry.TCEntities.SPECIAL_ITEM, e -> true).isEmpty()) {
            helper.fail("sem quem jogou não devia sair nada");
        }
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var knowledge = net.thaumcraft.research.Knowledges.of(player);
        knowledge.completeResearch("CRUCIBLE");
        net.thaumcraft.research.Knowledges.save(player, knowledge);
        crucible.aspects().remove(Aspects.AIR, crucible.aspects().getAmount(Aspects.AIR));
        for (var aspect : Aspects.primals()) {
            int have = crucible.aspects().getAmount(aspect);
            if (have < 2) crucible.aspects().add(aspect, 2 - have);
        }
        ItemEntity thrown = new ItemEntity(helper.getLevel(), where.x, where.y, where.z, new ItemStack(TCItems.SHARDS.get("air")));
        thrown.setThrower(player);
        helper.getLevel().addFreshEntity(thrown);
        int before = crucible.water();
        crucible.attemptSmelt(thrown);
        if (helper.getLevel().getEntities(net.thaumcraft.registry.TCEntities.SPECIAL_ITEM, e -> true).isEmpty()) {
            helper.fail("o fragmento equilibrado devia sair flutuando");
        }
        if (before - crucible.water() != 50) helper.fail("a receita devia beber 50 mB, bebeu " + (before - crucible.water()));
        helper.succeed();
    }

    /** Com mais de cem de essência, o crisol transborda fluxo; e os compostos se desfazem, bebendo 2 mB. */
    @GameTest(maxTicks = 200)
    public void overflowSpillsAndCompoundsBreakDown(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        CrucibleBlockEntity crucible = boiling(helper, pos);
        crucible.aspects().add(Aspects.FIRE, 120);
        for (int i = 0; i < 100; i++) {
            CrucibleBlockEntity.tick(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), crucible);
        }
        if (crucible.tagAmount() > 110) helper.fail("o transbordo devia ter levado essência: " + crucible.tagAmount());

        BlockPos other = new BlockPos(5, 2, 5);
        CrucibleBlockEntity second = boiling(helper, other);
        second.aspects().add(Aspects.CRYSTAL, 1);
        int water = second.water();
        for (int i = 0; i < 400; i++) {
            CrucibleBlockEntity.tick(helper.getLevel(), helper.absolutePos(other), helper.getBlockState(other), second);
        }
        if (second.aspects().getAmount(Aspects.CRYSTAL) != 0) helper.fail("o cristal devia ter se desfeito");
        if (second.water() >= water) helper.fail("a decomposição devia beber água");
        helper.succeed();
    }

    /** Quebrado, o crisol despeja a essência como fluxo em volta. */
    @GameTest
    public void breakingSpillsFlux(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) helper.setBlock(pos.offset(dx, -1, dz), Blocks.STONE);
        helper.setBlock(pos, TCBlocks.CRUCIBLE.defaultBlockState());
        CrucibleBlockEntity crucible = helper.getBlockEntity(pos, CrucibleBlockEntity.class);
        crucible.setWater(true);
        crucible.aspects().add(Aspects.FIRE, 60);
        helper.getLevel().destroyBlock(helper.absolutePos(pos), false);
        int flux = 0;
        for (int dx = -1; dx <= 1; dx++) for (int dy = 0; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) {
            var state = helper.getBlockState(pos.offset(dx, dy, dz));
            if (state.is(TCBlocks.FLUX_GOO) || state.is(TCBlocks.FLUX_GAS)) flux++;
        }
        if (flux == 0) helper.fail("quebrado com essência, devia ter saído fluxo");
        helper.succeed();
    }
}
