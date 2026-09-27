package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.kettle.KettleBlockEntity;
import net.thaumcraft.occulta.kettle.KettleRecipes;

/**
 * O Caldeirão de Pote: a água, o lume, as seis coisas, o que sai e o que estraga.
 */
public class OccultaKettleGameTest {
    /** Sem água, o pote não aceita nada; com água, aceita seis. */
    @GameTest
    public void theKettleNeedsWaterFirst(GameTestHelper helper) {
        KettleBlockEntity pote = put(helper, new BlockPos(2, 2, 2));
        if (pote == null) return;

        if (pote.throwIn(new ItemStack(OccultaItems.MANDRAKE_ROOT))) {
            helper.fail("num pote seco não entra nada");
        }
        if (!pote.fill()) helper.fail("um balde de água enche-o");
        if (pote.fill()) helper.fail("e um segundo não cabe");
        if (!pote.filled()) helper.fail("e ele fica cheio");

        for (int i = 0; i < KettleBlockEntity.INGREDIENTS; i++) {
            if (!pote.throwIn(new ItemStack(OccultaItems.MANDRAKE_ROOT))) {
                helper.fail("cabem seis coisas, e a " + (i + 1) + " não entrou");
            }
        }
        // a sétima não cabe, e o pote estraga
        pote.throwIn(new ItemStack(OccultaItems.MANDRAKE_ROOT));
        if (!pote.ruined()) helper.fail("coisa a mais estraga o pote");
        limpa(helper, new BlockPos(2, 2, 2));
        helper.succeed();
    }

    /** Os frascos de vidro vão para a casa deles, e não para as seis. */
    @GameTest
    public void bottlesGoToTheirOwnSlot(GameTestHelper helper) {
        KettleBlockEntity pote = put(helper, new BlockPos(4, 2, 2));
        if (pote == null) return;
        pote.fill();

        if (!pote.throwIn(new ItemStack(Items.GLASS_BOTTLE, 3))) helper.fail("os frascos entram");
        if (pote.bottles() != 3) helper.fail("e ficam na casa deles, e são " + pote.bottles());
        if (!pote.getItem(0).isEmpty()) helper.fail("e não ocupam as seis casas do que se cozinha");
        limpa(helper, new BlockPos(4, 2, 2));
        helper.succeed();
    }

    /** A tabela casa sem ordem, e não casa a mais nem a menos. */
    @GameTest
    public void theTableMatchesWithoutOrder(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var sopa = KettleRecipes.of(OccultaItems.REDSTONE_SOUP);
        if (sopa == null) {
            helper.fail("a Sopa de Redstone devia estar na tabela");
            return;
        }
        if (sopa.inputs().size() != KettleBlockEntity.INGREDIENTS) {
            helper.fail("uma receita do pote são seis coisas");
        }

        // as mesmas seis, ao contrário, casam
        java.util.List<ItemStack> avesso = new java.util.ArrayList<>();
        for (int i = sopa.inputs().size() - 1; i >= 0; i--) {
            avesso.add(new ItemStack(sopa.inputs().get(i)));
        }
        if (!sopa.matches(avesso, false, level)) helper.fail("a ordem não conta");

        // faltando uma, só casa pela metade
        avesso.removeLast();
        if (sopa.matches(avesso, false, level)) helper.fail("faltando uma, não casa inteira");
        if (!sopa.matches(avesso, true, level)) helper.fail("mas casa pela metade, que é o que dá a cor");

        // trocando uma por outra coisa, não casa de jeito nenhum
        avesso.add(new ItemStack(Items.DIAMOND));
        if (sopa.matches(avesso, false, level) || sopa.matches(avesso, true, level)) {
            helper.fail("com coisa que não é dela, não casa");
        }
        helper.succeed();
    }

    /** Sem lume por baixo, o que estava a cozinhar estraga. */
    @GameTest(maxTicks = 60)
    public void withoutFireTheBrewIsRuined(GameTestHelper helper) {
        BlockPos onde = new BlockPos(2, 2, 4);
        KettleBlockEntity pote = put(helper, onde);
        if (pote == null) return;
        pote.fill();
        pote.throwIn(new ItemStack(OccultaItems.MANDRAKE_ROOT));

        // vinte batidas sem lume, e ele estraga
        helper.runAfterDelay(KettleBlockEntity.EVERY + 2, () -> {
            if (!pote.ruined()) {
                helper.fail("sem lume por baixo, o cozimento estraga");
                return;
            }
            // e recomeça-se do zero
            pote.reset(true);
            if (pote.ruined() || pote.filled()) helper.fail("e recomeça-se com o pote limpo e seco");
            limpa(helper, onde);
            helper.succeed();
        });
    }

    /**
     * Com lume por baixo e as seis certas dentro, o líquido toma a cor da receita — e, <b>sem altar por perto</b>,
     * ele fica por aí: a Sopa de Redstone pede mil de poder, e sem poder o pote não a entrega.
     */
    @GameTest(maxTicks = 60)
    public void theRightSixColourTheWater(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = new BlockPos(4, 3, 4);
        BlockPos absoluto = helper.absolutePos(onde);
        // o lume só fica de pé sobre o netherrack no mundo de prova
        level.setBlockAndUpdate(absoluto.below(2), Blocks.NETHERRACK.defaultBlockState());
        level.setBlockAndUpdate(absoluto.below(), Blocks.FIRE.defaultBlockState());

        KettleBlockEntity pote = put(helper, onde);
        if (pote == null) return;
        pote.fill();
        var sopa = KettleRecipes.of(OccultaItems.REDSTONE_SOUP);
        if (sopa == null) {
            helper.fail("a Sopa de Redstone devia estar na tabela");
            return;
        }
        for (var coisa : sopa.inputs()) pote.throwIn(new ItemStack(coisa));

        helper.runAfterDelay(KettleBlockEntity.EVERY + 2, () -> {
            if (pote.ruined()) {
                helper.fail("com lume e as seis certas, o pote não estraga");
                return;
            }
            if (pote.color() != sopa.color()) {
                helper.fail("o líquido toma a cor da receita, e tomou " + Integer.toHexString(pote.color()));
                return;
            }
            if (pote.powered()) helper.fail("sem altar por perto não há poder nenhum");
            if (pote.ready()) helper.fail("e sem poder a Sopa não sai");
            level.setBlockAndUpdate(absoluto.below(), Blocks.AIR.defaultBlockState());
            limpa(helper, onde);
            helper.succeed();
        });
    }

    private static KettleBlockEntity put(GameTestHelper helper, BlockPos onde) {
        ServerLevel level = helper.getLevel();
        BlockPos absoluto = helper.absolutePos(onde);
        level.setBlockAndUpdate(absoluto, OccultaBlocks.WITCHES_KETTLE.defaultBlockState());
        if (level.getBlockEntity(absoluto) instanceof KettleBlockEntity pote) return pote;
        helper.fail("o pote devia ter alma");
        return null;
    }

    private static void limpa(GameTestHelper helper, BlockPos onde) {
        helper.getLevel().setBlockAndUpdate(helper.absolutePos(onde), Blocks.AIR.defaultBlockState());
    }
}
