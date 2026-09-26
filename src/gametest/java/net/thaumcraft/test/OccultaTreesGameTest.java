package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.occulta.LargeWitchTree;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaFumes;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.WitchSaplingBlock;
import net.thaumcraft.occulta.WitchTree;

import java.util.List;

/**
 * As três árvores do ofício: a sorveira, o amieiro e o espinheiro-alvar — como nascem, o que largam e o cheiro
 * que deixam no Forno das Bruxas.
 */
public class OccultaTreesGameTest {
    /** As árvores nascem bem acima da área do teste, para não cair em cima dos vizinhos. */
    private static final int SKY = 120;

    /** A sorveira nasce da muda dela, com tronco e copa. */
    @GameTest(maxTicks = 100)
    public void theRowanGrowsFromItsSapling(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = chão(helper, level);
        if (!WitchTree.generate(level, level.getRandom(), onde, false)) {
            helper.fail("a sorveira devia caber aqui");
            return;
        }
        if (!level.getBlockState(onde).is(OccultaBlocks.ROWAN_LOG)) {
            helper.fail("o tronco da sorveira começa no lugar da muda");
        }
        int toras = 0, folhas = 0;
        for (BlockPos pos : BlockPos.betweenClosed(onde.offset(-4, 0, -4), onde.offset(4, 9, 4))) {
            if (level.getBlockState(pos).is(OccultaBlocks.ROWAN_LOG)) toras++;
            if (level.getBlockState(pos).is(OccultaBlocks.ROWAN_LEAVES)) folhas++;
        }
        if (toras < 5) helper.fail("a sorveira tem de cinco a sete de tronco; teve " + toras);
        if (folhas < 20) helper.fail("e uma copa; teve " + folhas + " folhas");
        helper.succeed();
    }

    /** O amieiro e o espinheiro-alvar são árvores grandes, de galhos. */
    @GameTest(maxTicks = 100)
    public void theBigTreesHaveBranches(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        confereGrande(helper, level, LargeWitchTree.alder(), OccultaBlocks.ALDER_LOG, OccultaBlocks.ALDER_LEAVES);
        helper.succeed();
    }

    /** Sem chão que preste, nenhuma delas pega. */
    @GameTest
    public void theyNeedSoil(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = chão(helper, level);
        level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());
        if (WitchTree.generate(level, level.getRandom(), onde, false)) {
            helper.fail("a sorveira não nasce em pedra");
        }
        if (LargeWitchTree.generate(level, level.getRandom(), onde, false, LargeWitchTree.hawthorn())) {
            helper.fail("o espinheiro-alvar também não");
        }
        helper.succeed();
    }

    /** A muda não cresce na primeira batida do acaso: a primeira marca, a segunda faz a árvore. */
    @GameTest
    public void theSaplingIsMarkedBeforeItGrows(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = chão(helper, level);
        var muda = (WitchSaplingBlock) OccultaBlocks.ROWAN_SAPLING;
        level.setBlockAndUpdate(onde, muda.defaultBlockState());

        muda.advance(level, onde, level.getBlockState(onde), level.getRandom());
        if (!level.getBlockState(onde).is(OccultaBlocks.ROWAN_SAPLING)) {
            helper.fail("a primeira batida só marca a muda");
            return;
        }
        if (level.getBlockState(onde).getValue(WitchSaplingBlock.STAGE) != 1) {
            helper.fail("e a marca devia ficar nela");
        }
        muda.advance(level, onde, level.getBlockState(onde), level.getRandom());
        if (!level.getBlockState(onde).is(OccultaBlocks.ROWAN_LOG)) {
            helper.fail("a segunda batida faz a árvore; ficou " + level.getBlockState(onde));
        }
        helper.succeed();
    }

    /** A folhagem larga muda de vez em quando, e a da sorveira larga bagas. */
    @GameTest
    public void theLeavesGiveSaplingsAndBerries(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        int voltas = 3000;
        int mudas = 0, bagas = 0;
        for (int volta = 0; volta < voltas; volta++) {
            for (ItemStack caiu : Block.getDrops(OccultaBlocks.ROWAN_LEAVES.defaultBlockState(), level, onde, null,
                    null, ItemStack.EMPTY)) {
                if (caiu.is(OccultaItems.WOOD.get("rowan_sapling"))) mudas++;
                if (caiu.is(OccultaItems.ROWAN_BERRIES)) bagas++;
            }
        }
        double parteMudas = mudas / (double) voltas;
        if (Math.abs(parteMudas - 1.0 / 20.0) > 0.02) {
            helper.fail("a muda sai uma vez em vinte; saiu em " + parteMudas);
        }
        if (bagas == 0) helper.fail("a folhagem da sorveira larga bagas de vez em quando");
        if (bagas > voltas / 50) helper.fail("mas raramente; saíram " + bagas + " em " + voltas);

        // e a do amieiro não larga baga nenhuma
        for (int volta = 0; volta < 600; volta++) {
            for (ItemStack caiu : Block.getDrops(OccultaBlocks.ALDER_LEAVES.defaultBlockState(), level, onde, null,
                    null, ItemStack.EMPTY)) {
                if (caiu.is(OccultaItems.ROWAN_BERRIES)) {
                    helper.fail("só a sorveira dá bagas");
                    return;
                }
            }
        }

        // com tesoura sai a própria folhagem
        List<ItemStack> tesoura = Block.getDrops(OccultaBlocks.ROWAN_LEAVES.defaultBlockState(), level, onde, null,
                null, new ItemStack(Items.SHEARS));
        if (tesoura.size() != 1 || !tesoura.get(0).is(OccultaItems.WOOD.get("rowan_leaves"))) {
            helper.fail("com tesoura sai a folhagem; saiu " + tesoura);
        }
        helper.succeed();
    }

    /** As três mudas do ofício deixam os três cheiros que faltavam ao forno. */
    @GameTest
    public void theWitchSaplingsCompleteTheFumes(GameTestHelper helper) {
        confereCheiro(helper, "rowan_sapling", OccultaItems.WHIFF_OF_MAGIC);
        confereCheiro(helper, "alder_sapling", OccultaItems.REEK_OF_MISFORTUNE);
        confereCheiro(helper, "hawthorn_sapling", OccultaItems.ODOUR_OF_PURITY);
        helper.succeed();
    }

    /** As toras contam como tora e a folhagem como folhagem, que é o que faz o machado e o apodrecer valerem. */
    @GameTest
    public void theWoodIsWoodToTheGame(GameTestHelper helper) {
        for (String árvore : List.of("rowan", "alder", "hawthorn")) {
            BlockState tora = blocoDe(árvore + "_log");
            if (!tora.is(BlockTags.LOGS)) helper.fail("a tora de " + árvore + " devia contar como tora");
            if (!tora.is(BlockTags.MINEABLE_WITH_AXE)) helper.fail("e o machado devia cortá-la");
            BlockState folhagem = blocoDe(árvore + "_leaves");
            if (!folhagem.is(BlockTags.LEAVES)) helper.fail("a folhagem de " + árvore + " devia contar como folhagem");
            if (!(folhagem.getBlock() instanceof LeavesBlock)) helper.fail("e ser folha de verdade");
            BlockState tábuas = blocoDe(árvore + "_planks");
            if (!tábuas.is(BlockTags.PLANKS)) helper.fail("as tábuas de " + árvore + " deviam contar como tábuas");
        }
        helper.succeed();
    }

    /** E o thaumômetro lê nelas o que o {@code ModHookThaumcraft4} diz. */
    @GameTest
    public void theTreesHaveTheirAspects(GameTestHelper helper) {
        var tora = ObjectAspects.of(OccultaItems.WOOD.get("rowan_log"));
        if (tora.getAmount(Aspects.TREE) != 2 || tora.getAmount(Aspects.MAGIC) != 1) {
            helper.fail("a tora é arbor 2 e praecantatio 1; veio " + tora);
        }
        var muda = ObjectAspects.of(OccultaItems.WOOD.get("alder_sapling"));
        if (muda.getAmount(Aspects.ENTROPY) != 1) helper.fail("a muda de amieiro é perditio 1; veio " + muda);
        var bagas = ObjectAspects.of(OccultaItems.ROWAN_BERRIES);
        if (bagas.getAmount(Aspects.HUNGER) < 1) helper.fail("as bagas são fames; vieram " + bagas);
        helper.succeed();
    }

    /** Um chão de terra bem acima da área do teste, com céu de sobra para a árvore. */
    private static BlockPos chão(GameTestHelper helper, ServerLevel level) {
        BlockPos onde = helper.absolutePos(new BlockPos(2, 1, 2)).above(SKY);
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlock(onde.offset(x, -1, z), Blocks.DIRT.defaultBlockState(), 3);
            }
        }
        return onde;
    }

    private static BlockState blocoDe(String nome) {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .getValue(net.thaumcraft.Thaumcraft.id(nome)).defaultBlockState();
    }

    private static void confereCheiro(GameTestHelper helper, String muda, net.minecraft.world.item.Item cheiro) {
        ItemStack saiu = OccultaFumes.of(new ItemStack(OccultaItems.WOOD.get(muda)));
        if (!saiu.is(cheiro)) helper.fail(muda + " devia deixar " + cheiro + "; deixou " + saiu);
    }

    private static void confereGrande(GameTestHelper helper, ServerLevel level, LargeWitchTree.Kind jeito,
                                      Block tora, Block folha) {
        BlockPos onde = chão(helper, level);
        if (!LargeWitchTree.generate(level, level.getRandom(), onde, false, jeito)) {
            helper.fail("a árvore grande devia caber aqui");
            return;
        }
        int toras = 0, folhas = 0, largura = 0;
        for (BlockPos pos : BlockPos.betweenClosed(onde.offset(-8, 0, -8), onde.offset(8, 14, 8))) {
            BlockState qual = level.getBlockState(pos);
            if (qual.is(tora)) toras++;
            if (qual.is(folha)) {
                folhas++;
                int longe = Math.max(Math.abs(pos.getX() - onde.getX()), Math.abs(pos.getZ() - onde.getZ()));
                largura = Math.max(largura, longe);
            }
        }
        if (toras < 5) helper.fail("a árvore grande devia ter tronco; teve " + toras);
        if (folhas < 30) helper.fail("e copa; teve " + folhas + " folhas");
        if (largura < 3) helper.fail("e uma copa larga, que é o que a faz grande; deu " + largura);
    }
}
