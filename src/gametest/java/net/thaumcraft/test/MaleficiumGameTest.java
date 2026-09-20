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
        if (MaleficiumItems.count() != nomes.length) {
            helper.fail("a aba tem " + MaleficiumItems.count() + " coisas, devia ter " + nomes.length);
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

    private static ItemEntity drop(GameTestHelper helper, net.minecraft.world.item.Item salis) {
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(1, 2, 1)));
        ItemEntity item = new ItemEntity(helper.getLevel(), at.x, at.y, at.z, new ItemStack(salis));
        item.setDeltaMovement(Vec3.ZERO);
        helper.getLevel().addFreshEntity(item);
        return item;
    }
}
