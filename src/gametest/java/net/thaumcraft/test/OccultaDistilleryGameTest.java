package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.occulta.DistilleryBlockEntity;
import net.thaumcraft.occulta.DistilleryRecipes;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.brew.Brew;
import net.thaumcraft.occulta.brew.BrewCapacity;

import java.util.List;

/**
 * A Destilaria: o que ela separa, o que gasta e o que precisa para andar.
 */
public class OccultaDistilleryGameTest {
    /** A receita bate com as duas coisas em qualquer ordem, e só com potes bastantes. */
    @GameTest
    public void theRecipeTakesEitherOrder(GameTestHelper helper) {
        ItemStack sopro = new ItemStack(OccultaItems.BREATH_OF_THE_GODDESS);
        ItemStack lápis = new ItemStack(Items.LAPIS_LAZULI);
        ItemStack potes = new ItemStack(OccultaItems.CLAY_JAR, 3);

        if (DistilleryRecipes.find(sopro, lápis, potes) == null) {
            helper.fail("o sopro com lápis-lazúli destila-se");
        }
        if (DistilleryRecipes.find(lápis, sopro, potes) == null) {
            helper.fail("e a ordem das duas não importa");
        }
        if (DistilleryRecipes.find(sopro, lápis, new ItemStack(OccultaItems.CLAY_JAR, 2)) != null) {
            helper.fail("mas com dois potes só, não: essa receita pede três");
        }
        if (DistilleryRecipes.find(sopro, new ItemStack(Items.STONE), potes) != null) {
            helper.fail("e com pedra não há receita nenhuma");
        }
        helper.succeed();
    }

    /** Destilada, a coisa vira quatro — e os potes se gastam. */
    @GameTest
    public void itSplitsOneIntoFour(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(onde, OccultaBlocks.DISTILLERY.defaultBlockState());
        if (!(level.getBlockEntity(onde) instanceof DistilleryBlockEntity destilaria)) {
            helper.fail("a destilaria devia ter miolo");
            return;
        }

        destilaria.setItem(DistilleryBlockEntity.INPUT_A, new ItemStack(OccultaItems.BREATH_OF_THE_GODDESS));
        destilaria.setItem(DistilleryBlockEntity.INPUT_B, new ItemStack(Items.LAPIS_LAZULI));
        destilaria.setItem(DistilleryBlockEntity.JARS, new ItemStack(OccultaItems.CLAY_JAR, 5));
        if (!destilaria.canDistil()) helper.fail("com o que ela pede dentro, ela pode destilar");

        destilaria.distil();
        if (!destilaria.getItem(DistilleryBlockEntity.OUTPUT_FIRST).is(OccultaItems.TEAR_OF_THE_GODDESS)) {
            helper.fail("do sopro com lápis sai a Lágrima da Deusa; saiu "
                    + destilaria.getItem(DistilleryBlockEntity.OUTPUT_FIRST));
        }
        if (!destilaria.getItem(DistilleryBlockEntity.OUTPUT_FIRST + 1).is(OccultaItems.WHIFF_OF_MAGIC)) {
            helper.fail("e o Sopro de Magia ao lado dela");
        }
        if (destilaria.getItem(DistilleryBlockEntity.JARS).getCount() != 2) {
            helper.fail("e os três potes da receita gastam-se; ficaram "
                    + destilaria.getItem(DistilleryBlockEntity.JARS).getCount());
        }
        if (!destilaria.getItem(DistilleryBlockEntity.INPUT_A).isEmpty()) {
            helper.fail("o que entrou também se gasta");
        }

        level.setBlockAndUpdate(onde, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** Sem altar por perto, ela não anda. */
    @GameTest(maxTicks = 100)
    public void withoutAnAltarItStands(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(onde, OccultaBlocks.DISTILLERY.defaultBlockState());
        if (!(level.getBlockEntity(onde) instanceof DistilleryBlockEntity destilaria)) {
            helper.fail("a destilaria devia ter miolo");
            return;
        }
        destilaria.setItem(DistilleryBlockEntity.INPUT_A, new ItemStack(Items.ENDER_PEARL));
        destilaria.setItem(DistilleryBlockEntity.JARS, new ItemStack(OccultaItems.CLAY_JAR, 6));

        helper.runAfterDelay(60, () -> {
            if (destilaria.cookTime != 0) {
                helper.fail("sem altar ela não devia ter andado nada; andou " + destilaria.cookTime);
            }
            if (destilaria.powerLevel != 0) helper.fail("e sabe que não há poder nenhum");
            level.setBlockAndUpdate(onde, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            helper.succeed();
        });
    }

    /** E as duas que a Destilaria faz abrem espaço no caldeirão, como no original. */
    @GameTest
    public void whatItMakesOpensTheCauldron(GameTestHelper helper) {
        BrewCapacity comLágrima = Brew.capacity(List.of(OccultaItems.TEAR_OF_THE_GODDESS));
        if (comLágrima.max() != 2) {
            helper.fail("a Lágrima da Deusa abre dois; deu " + comLágrima.max());
        }
        BrewCapacity comVerrugaELágrima = Brew.capacity(List.of(Items.NETHER_WART,
                OccultaItems.TEAR_OF_THE_GODDESS));
        if (comVerrugaELágrima.max() != 4) {
            helper.fail("a verruga abre dois e a lágrima mais dois, até o teto dela; deu "
                    + comVerrugaELágrima.max());
        }
        BrewCapacity comVapor = Brew.capacity(List.of(Items.NETHER_WART, OccultaItems.TEAR_OF_THE_GODDESS,
                OccultaItems.DIAMOND_VAPOUR));
        if (comVapor.max() != 6) {
            helper.fail("e o Vapor de Diamante leva até seis; deu " + comVapor.max());
        }
        helper.succeed();
    }
}
