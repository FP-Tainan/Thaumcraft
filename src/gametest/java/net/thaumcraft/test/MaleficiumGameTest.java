package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.crafting.CrucibleRecipe;
import net.thaumcraft.crafting.CrucibleRecipes;
import net.thaumcraft.maleficium.Maleficium;
import net.thaumcraft.maleficium.MaleficiumItems;
import net.thaumcraft.maleficium.SalisItem;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.research.ResearchCategories;
import net.thaumcraft.research.Researches;

/**
 * O Maleficium — o Tainted Magic 8.1.1 de Yulife — dentro do Thaumcraft: a matéria-prima, o que o crisol faz com
 * ela, os sais que se gastam no chão e a aba do ramo no livro.
 */
public class MaleficiumGameTest {
    @GameTest
    public void everyMaterialIsRegistered(GameTestHelper helper) {
        String[] nomes = {"shadowmetal_ingot", "shadow_cloth", "crimson_cloth", "warped_shard", "tainted_shard",
                "creation_shard", "thaumic_plating", "crimson_plating", "shadowmetal_nugget", "primordial_nodule",
                "primordial_mote", "creation_fragment", "salis_tempestas", "salis_aevum"};
        for (String nome : nomes) {
            if (BuiltInRegistries.ITEM.getOptional(Thaumcraft.id(nome)).isEmpty()) helper.fail("falta o item " + nome);
        }
        if (MaleficiumItems.count() < nomes.length) {
            helper.fail("a aba tem " + MaleficiumItems.count() + " coisas, devia ter ao menos " + nomes.length);
        }
        helper.succeed();
    }

    @GameTest
    public void theCrucibleMakesShadowMetal(GameTestHelper helper) {
        AspectList inside = new AspectList().add(Aspects.DARKNESS, 3).add(Aspects.METAL, 7).add(Aspects.MAGIC, 2);
        CrucibleRecipe recipe = CrucibleRecipes.find(inside, new ItemStack(Items.IRON_INGOT));
        if (recipe == null) helper.fail("o ferro no crisol devia dar metal das sombras");
        else if (!recipe.result().is(MaleficiumItems.SHADOWMETAL_INGOT)) helper.fail("saiu " + recipe.result());
        helper.succeed();
    }

    @GameTest
    public void theCrucibleUnbalancesShards(GameTestHelper helper) {
        var balanced = new ItemStack(TCItems.SHARD_BALANCED);
        CrucibleRecipe warped = CrucibleRecipes.find(new AspectList().add(Aspects.ELDRITCH, 4), balanced);
        if (warped == null || !warped.result().is(MaleficiumItems.WARPED_SHARD)) helper.fail("faltou o fragmento distorcido");
        CrucibleRecipe tainted = CrucibleRecipes.find(new AspectList().add(Aspects.TAINT, 4), balanced);
        if (tainted == null || !tainted.result().is(MaleficiumItems.TAINTED_SHARD)) helper.fail("faltou o fragmento maculado");
        helper.succeed();
    }

    /** O Tainted Magic não anota aspecto nenhum, nem no original: quem os deduz das receitas é o Thaumcraft. */
    @GameTest
    public void theShadowMetalHasAspects(GameTestHelper helper) {
        AspectList aspects = ObjectAspects.of(new ItemStack(MaleficiumItems.SHADOWMETAL_INGOT));
        if (aspects.isEmpty()) helper.fail("o metal das sombras devia ter aspecto deduzido da receita de crisol");
        if (aspects.getAmount(Aspects.METAL) <= 0) helper.fail("faltou metal nos aspectos: " + aspects);
        helper.succeed();
    }

    @GameTest
    public void theBookHasTheMaleficiumTab(GameTestHelper helper) {
        if (ResearchCategories.get(Maleficium.CATEGORY) == null) helper.fail("a aba do Maleficium não entrou no livro");
        for (String key : new String[]{"MALEFICIUM", "SHADOWMETAL", "UNBALANCEDSHARDS"}) {
            var research = Researches.get(key);
            if (research == null) helper.fail("falta a pesquisa " + key);
            else if (!research.category().equals(Maleficium.CATEGORY)) helper.fail(key + " foi para a aba errada");
        }
        helper.succeed();
    }

    @GameTest(maxTicks = 160)
    public void theTempestasTurnsTheWeather(GameTestHelper helper) {
        var level = helper.getLevel();
        // a chuva do jogo entra e sai devagar, então o que se confere é a chave do tempo, não a força da chuva
        boolean raining = level.getWeatherData().isRaining();
        ItemEntity item = drop(helper, MaleficiumItems.SALIS_TEMPESTAS);
        helper.runAfterDelay(SalisItem.LIFE + 20, () -> {
            if (!item.isRemoved()) helper.fail("o sal devia ter se gastado");
            if (level.getWeatherData().isRaining() == raining) helper.fail("o tempo devia ter virado");
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 160)
    public void theAevumTurnsTheDay(GameTestHelper helper) {
        var level = helper.getLevel();
        boolean bright = level.isBrightOutside();
        ItemEntity item = drop(helper, MaleficiumItems.SALIS_AEVUM);
        helper.runAfterDelay(SalisItem.LIFE + 20, () -> {
            if (!item.isRemoved()) helper.fail("o sal devia ter se gastado");
            long hora = level.getOverworldClockTime() % 24000L;
            boolean noite = hora >= 13000L && hora < 23000L;
            if (bright != noite) helper.fail("o dia devia ter virado; são " + hora + " e estava claro: " + bright);
            helper.succeed();
        });
    }

    @GameTest
    public void everyBlockIsRegistered(GameTestHelper helper) {
        for (String nome : new String[]{"warpwood_log", "warpwood_knot", "warpwood_planks", "warpwood_leaves",
                "warpwood_sapling", "nightshade_bush", "lumos"}) {
            if (BuiltInRegistries.BLOCK.getOptional(Thaumcraft.id(nome)).isEmpty()) helper.fail("falta o bloco " + nome);
        }
        helper.succeed();
    }

    /** A árvore distorcida nasce da muda, com tronco, copa e nós — no céu, para não cair nos testes vizinhos. */
    @GameTest
    public void theSaplingGrowsTheWarpwoodTree(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(1, 1, 1)).above(120);
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlock(base.offset(x, -1, z), net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState(), 3);
            }
        }
        var sapling = (net.thaumcraft.maleficium.WarpwoodSaplingBlock) net.thaumcraft.maleficium.MaleficiumBlocks.WARPWOOD_SAPLING;
        boolean grew = false;
        for (int tries = 0; tries < 20 && !grew; tries++) {
            grew = sapling.grow(level, base, sapling.defaultBlockState(), level.getRandom());
        }
        if (!grew) helper.fail("a muda devia virar árvore com o céu livre em cima");
        if (!level.getBlockState(base).is(net.thaumcraft.maleficium.MaleficiumBlocks.WARPWOOD_LOG)) {
            helper.fail("a base do tronco devia ser tora distorcida, é " + level.getBlockState(base));
        }
        int folhas = 0, toras = 0, nos = 0;
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-8, -2, -8), base.offset(8, 20, 8))) {
            var state = level.getBlockState(pos);
            if (state.is(net.thaumcraft.maleficium.MaleficiumBlocks.WARPWOOD_LEAVES)) folhas++;
            if (state.is(net.thaumcraft.maleficium.MaleficiumBlocks.WARPWOOD_LOG)) toras++;
            if (state.is(net.thaumcraft.maleficium.MaleficiumBlocks.WARPWOOD_KNOT)) nos++;
        }
        if (folhas < 50) helper.fail("a copa saiu rala: " + folhas + " folhas");
        if (toras < 20) helper.fail("tronco pequeno demais: " + toras + " toras");
        if (nos > 4) helper.fail("nós demais no tronco: " + nos);
        helper.succeed();
    }

    /** O adubo torce a muda de madeira-prata em muda distorcida. */
    @GameTest
    public void theFertilizerTwistsASilverwoodSapling(GameTestHelper helper) {
        BlockPos ground = new BlockPos(1, 1, 1);
        helper.setBlock(ground, net.minecraft.world.level.block.Blocks.DIRT);
        BlockPos at = ground.above();
        helper.setBlock(at, net.thaumcraft.registry.TCBlocks.SILVERWOOD_SAPLING);
        var player = helper.makeMockServerPlayerInLevel();
        ItemStack stack = new ItemStack(MaleficiumItems.WARP_FERTILIZER, 2);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
        var hit = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(at)),
                net.minecraft.core.Direction.UP, helper.absolutePos(at), false);
        stack.useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, hit));
        if (!helper.getBlockState(at).is(net.thaumcraft.maleficium.MaleficiumBlocks.WARPWOOD_SAPLING)) {
            helper.fail("a muda devia ter se torcido; ficou " + helper.getBlockState(at));
        }
        helper.succeed();
    }

    /** O Lumos ilumina como uma tocha e não atrapalha quem passa. */
    @GameTest
    public void theLumosLightsTheWay(GameTestHelper helper) {
        BlockPos at = new BlockPos(2, 2, 2);
        helper.setBlock(at, net.thaumcraft.maleficium.MaleficiumBlocks.LUMOS);
        var absolute = helper.absolutePos(at);
        helper.getLevel().getLightEngine().checkBlock(absolute);
        helper.runAfterDelay(2, () -> {
            int light = helper.getLevel().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, absolute);
            if (light < 15) helper.fail("o Lumos devia iluminar como uma tocha; ilumina " + light);
            if (!helper.getBlockState(at).getCollisionShape(helper.getLevel(), absolute).isEmpty()) {
                helper.fail("o Lumos não pode atrapalhar quem passa");
            }
            helper.succeed();
        });
    }

    private static ItemEntity drop(GameTestHelper helper, net.minecraft.world.item.Item salis) {
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(1, 2, 1)));
        ItemEntity item = new ItemEntity(helper.getLevel(), at.x, at.y, at.z, new ItemStack(salis));
        item.setDeltaMovement(Vec3.ZERO);
        helper.getLevel().addFreshEntity(item);
        return item;
    }
}
