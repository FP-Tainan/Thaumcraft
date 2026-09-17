package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.wands.WandParts;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

/**
 * As contas da varinha têm de ser as do Thaumcraft 4.2.3.5.
 */
public class WandGameTest {
    /** As peças e os números delas vieram do original. */
    @GameTest
    public void partsCameFromTheOriginal(GameTestHelper helper) {
        if (WandParts.CAPS.size() != 4) helper.fail("o original tem quatro pontas");
        if (WandParts.RODS.size() != 9) helper.fail("o original tem nove hastes de varinha");
        if (WandParts.STAFF_RODS.size() != 9) helper.fail("o original tem nove hastes de bastão");

        // conferidas na mão contra o Thaumcraft.java da 4.2.3.5
        if (WandParts.rod("wood").capacity() != 25) helper.fail("a haste de madeira guarda vinte e cinco");
        if (WandParts.rod("silverwood").capacity() != 100) helper.fail("a de silverwood guarda cem");
        if (WandParts.cap("iron").discount() != 1.1f) helper.fail("a ponta de ferro cobra dez por cento a mais");
        if (WandParts.cap("void").discount() != 0.8f) helper.fail("a de vazio cobra vinte por cento a menos");
        if (!WandParts.rod("blaze").glowing()) helper.fail("a haste de blaze acende");
        if (WandParts.rod("obsidian").primal() != Aspects.EARTH) helper.fail("a de obsidiana recolhe terra");
        helper.succeed();
    }

    /** O vis é contado em centésimos, e a varinha não guarda mais do que cabe. */
    @GameTest
    public void visIsCountedInHundredths(GameTestHelper helper) {
        ItemStack wand = new ItemStack(TCItems.WAND);
        wand.set(TCComponents.WAND_ROD, "wood");
        wand.set(TCComponents.WAND_CAP, "iron");
        if (WandItem.maxVis(wand) != 2500) helper.fail("vinte e cinco de haste são dois mil e quinhentos");

        WandItem.addVis(wand, Aspects.AIR, 10);
        if (WandItem.vis(wand, Aspects.AIR) != 1000) helper.fail("dez pontos são mil");
        int left = WandItem.addVis(wand, Aspects.AIR, 100);
        if (left != 85) helper.fail("só cabiam mais quinze, deviam sobrar oitenta e cinco, sobraram " + left);
        if (WandItem.vis(wand, Aspects.AIR) != 2500) helper.fail("devia estar cheia");
        helper.succeed();
    }

    /** A ponta manda no custo: a de ferro cobra mais, a de vazio cobra menos. */
    @GameTest
    public void capsChangeWhatEachUseCosts(GameTestHelper helper) {
        ItemStack iron = new ItemStack(TCItems.WAND);
        iron.set(TCComponents.WAND_ROD, "greatwood");
        iron.set(TCComponents.WAND_CAP, "iron");
        WandItem.addVis(iron, Aspects.FIRE, 10);

        AspectList cost = new AspectList().add(Aspects.FIRE, 9);
        // nove pontos com dez por cento a mais são novecentos e noventa: cabe nos mil
        if (!WandItem.consume(iron, cost, true)) helper.fail("nove pontos deviam caber em dez");
        if (WandItem.vis(iron, Aspects.FIRE) != 10) helper.fail("deviam sobrar dez centésimos");

        ItemStack voidCap = new ItemStack(TCItems.WAND);
        voidCap.set(TCComponents.WAND_ROD, "greatwood");
        voidCap.set(TCComponents.WAND_CAP, "void");
        WandItem.addVis(voidCap, Aspects.FIRE, 8);
        // com a ponta de vazio, nove pontos custam setecentos e vinte
        if (!WandItem.consume(voidCap, cost, true)) helper.fail("com ponta de vazio nove pontos deviam caber em oito");
        helper.succeed();
    }
}
