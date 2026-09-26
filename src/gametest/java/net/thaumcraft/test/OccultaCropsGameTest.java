package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaCrops;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.WitchCropBlock;

import java.util.List;

/**
 * As oito plantas do ofício: quantas idades têm, onde pegam, o que cai delas e o que a farinha de osso lhes faz.
 */
public class OccultaCropsGameTest {
    /** As oito, com as idades que o {@code WitcheryBlocks} lhes dá. */
    private static final List<Block> AS_OITO = List.of(
            OccultaBlocks.BELLADONNA, OccultaBlocks.MANDRAKE, OccultaBlocks.WATER_ARTICHOKE,
            OccultaBlocks.SNOWBELL, OccultaBlocks.WORMWOOD, OccultaBlocks.MINDRAKE,
            OccultaBlocks.WOLFSBANE, OccultaBlocks.GARLIC);

    @GameTest
    public void theEightPlantsHaveTheirAges(GameTestHelper helper) {
        for (Block planta : AS_OITO) {
            if (!(planta instanceof WitchCropBlock crop)) {
                helper.fail(planta + " devia ser planta do ofício");
                return;
            }
            int idades = crop.getMaxAge();
            int esperado = planta == OccultaBlocks.WOLFSBANE ? 7 : planta == OccultaBlocks.GARLIC ? 5 : 4;
            if (idades != esperado) helper.fail(planta + " devia ter " + esperado + " idades; tem " + idades);
            // e a idade tem de ir de zero ao fim, sem buraco
            BlockState feita = crop.getStateForAge(idades);
            if (crop.getAge(feita) != idades) helper.fail(planta + " não guarda a última idade");
        }
        helper.succeed();
    }

    /** Cada semente planta a sua planta, e a da alcachofra é a única que se usa sobre a água. */
    @GameTest
    public void eachSeedPlantsItsOwnCrop(GameTestHelper helper) {
        confereSemente(helper, OccultaItems.BELLADONNA_SEEDS, OccultaBlocks.BELLADONNA);
        confereSemente(helper, OccultaItems.MANDRAKE_SEEDS, OccultaBlocks.MANDRAKE);
        confereSemente(helper, OccultaItems.WATER_ARTICHOKE_SEEDS, OccultaBlocks.WATER_ARTICHOKE);
        confereSemente(helper, OccultaItems.SNOWBELL_SEEDS, OccultaBlocks.SNOWBELL);
        confereSemente(helper, OccultaItems.WORMWOOD_SEEDS, OccultaBlocks.WORMWOOD);
        confereSemente(helper, OccultaItems.MINDRAKE_BULB, OccultaBlocks.MINDRAKE);
        confereSemente(helper, OccultaItems.WOLFSBANE_SEEDS, OccultaBlocks.WOLFSBANE);
        confereSemente(helper, OccultaItems.GARLIC, OccultaBlocks.GARLIC);
        if (!(OccultaItems.WATER_ARTICHOKE_SEEDS instanceof PlaceOnWaterBlockItem)) {
            helper.fail("a semente da alcachofra tem de poder ser usada sobre a água");
        }
        helper.succeed();
    }

    /** Terra arada serve a sete delas; a alcachofra quer água, e pedra não serve a nenhuma. */
    @GameTest
    public void theyOnlyTakeInTheirOwnGround(GameTestHelper helper) {
        BlockPos onde = new BlockPos(1, 3, 1);
        BlockPos chão = onde.below();
        for (Block planta : AS_OITO) {
            boolean água = planta == OccultaBlocks.WATER_ARTICHOKE;

            helper.setBlock(chão, Blocks.FARMLAND);
            boolean naTerra = planta.defaultBlockState().canSurvive(helper.getLevel(), helper.absolutePos(onde));
            if (naTerra == água) helper.fail(planta + (água ? " não devia pegar em terra arada" : " devia pegar em terra arada"));

            helper.setBlock(chão, Blocks.WATER);
            boolean naÁgua = planta.defaultBlockState().canSurvive(helper.getLevel(), helper.absolutePos(onde));
            if (naÁgua != água) helper.fail(planta + (água ? " devia pegar sobre a água" : " não devia pegar sobre a água"));

            helper.setBlock(chão, Blocks.STONE);
            if (planta.defaultBlockState().canSurvive(helper.getLevel(), helper.absolutePos(onde))) {
                helper.fail(planta + " não devia pegar em pedra");
            }
        }
        // e a grama e a terra servem, que é mais largo que o do trigo
        helper.setBlock(chão, Blocks.GRASS_BLOCK);
        if (!OccultaBlocks.BELLADONNA.defaultBlockState().canSurvive(helper.getLevel(), helper.absolutePos(onde))) {
            helper.fail("a beladona devia pegar em grama, como no original");
        }
        helper.succeed();
    }

    /** Planta verde larga uma semente e mais nada. */
    @GameTest
    public void anUnripePlantOnlyGivesBackItsSeed(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (Block planta : AS_OITO) {
            WitchCropBlock crop = (WitchCropBlock) planta;
            List<ItemStack> caiu = OccultaCrops.drops(planta, crop.getStateForAge(0), level, level.getRandom(), 0);
            if (caiu.size() != 1 || !caiu.get(0).is(crop.getBaseSeedId().asItem())) {
                helper.fail(planta + " verde devia largar uma semente só; largou " + caiu);
            }
        }
        helper.succeed();
    }

    /**
     * Planta feita larga a colheita e, em média, uma semente e meia — três tentativas de oito em quinze.
     */
    @GameTest
    public void aRipePlantGivesItsHarvestAndSomeSeeds(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        WitchCropBlock beladona = (WitchCropBlock) OccultaBlocks.BELLADONNA;
        BlockState feita = beladona.getStateForAge(beladona.getMaxAge());
        int voltas = 400;
        int sementes = 0;
        for (int volta = 0; volta < voltas; volta++) {
            List<ItemStack> caiu = OccultaCrops.drops(OccultaBlocks.BELLADONNA, feita, level, level.getRandom(), 0);
            long flores = caiu.stream().filter(is -> is.is(OccultaItems.BELLADONNA_FLOWER)).count();
            if (flores != 1) {
                helper.fail("a beladona feita devia largar uma flor; largou " + flores);
                return;
            }
            sementes += caiu.stream().filter(is -> is.is(OccultaItems.BELLADONNA_SEEDS)).count();
        }
        double média = sementes / (double) voltas;
        double conta = OccultaCrops.SEED_TRIES * OccultaCrops.SEED_CHANCE / 15.0;
        if (Math.abs(média - conta) > 0.3) {
            helper.fail("devia dar perto de " + conta + " sementes por colheita; deu " + média);
        }
        // e a fortuna acrescenta tentativas
        int comFortuna = 0;
        for (int volta = 0; volta < voltas; volta++) {
            comFortuna += OccultaCrops.drops(OccultaBlocks.BELLADONNA, feita, level, level.getRandom(), 3).stream()
                    .filter(is -> is.is(OccultaItems.BELLADONNA_SEEDS)).count();
        }
        if (comFortuna <= sementes) helper.fail("a fortuna devia render mais sementes");
        helper.succeed();
    }

    /** A campainha-de-neve dá bola de neve, e a Agulha de Gelo numa colheita de cada cinco. */
    @GameTest
    public void theSnowbellSometimesGivesAnIcyNeedle(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        WitchCropBlock campainha = (WitchCropBlock) OccultaBlocks.SNOWBELL;
        BlockState feita = campainha.getStateForAge(campainha.getMaxAge());
        int voltas = 600;
        int agulhas = 0;
        for (int volta = 0; volta < voltas; volta++) {
            List<ItemStack> caiu = OccultaCrops.drops(OccultaBlocks.SNOWBELL, feita, level, level.getRandom(), 0);
            if (caiu.stream().noneMatch(is -> is.is(Items.SNOWBALL))) {
                helper.fail("a campainha-de-neve devia largar bola de neve; largou " + caiu);
                return;
            }
            agulhas += caiu.stream().filter(is -> is.is(OccultaItems.ICY_NEEDLE)).count();
        }
        double parte = agulhas / (double) voltas;
        if (Math.abs(parte - OccultaCrops.ICY_NEEDLE) > 0.08) {
            helper.fail("a agulha devia sair em " + OccultaCrops.ICY_NEEDLE + " das colheitas; saiu em " + parte);
        }
        helper.succeed();
    }

    /** A mindrake dá um bulbo sempre, e o segundo uma vez em quatro; e não dá semente de outra coisa. */
    @GameTest
    public void theMinedrakeGivesBulbs(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        WitchCropBlock mindrake = (WitchCropBlock) OccultaBlocks.MINDRAKE;
        BlockState feita = mindrake.getStateForAge(mindrake.getMaxAge());
        int voltas = 600;
        int bulbos = 0;
        for (int volta = 0; volta < voltas; volta++) {
            List<ItemStack> caiu = OccultaCrops.drops(OccultaBlocks.MINDRAKE, feita, level, level.getRandom(), 0);
            if (caiu.isEmpty() || caiu.size() > 2) {
                helper.fail("a mindrake devia largar um bulbo ou dois; largou " + caiu);
                return;
            }
            for (ItemStack is : caiu) {
                if (!is.is(OccultaItems.MINDRAKE_BULB)) {
                    helper.fail("a mindrake só larga bulbo; largou " + is);
                    return;
                }
            }
            bulbos += caiu.size();
        }
        double média = bulbos / (double) voltas;
        double conta = 1.0 + 1.0 / OccultaCrops.MINDRAKE_AGAIN;
        if (Math.abs(média - conta) > 0.15) {
            helper.fail("devia dar perto de " + conta + " bulbos por colheita; deu " + média);
        }
        helper.succeed();
    }

    /**
     * A mandrágora foge de quem a arranca fora de hora: de dia quase sempre, de noite quase nunca.
     *
     * <p>A conta olha a hora que o mundo de prova tem, seja ela qual for, e confere a chance dessa hora.
     */
    @GameTest
    public void theMandrakeEscapesByDay(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        WitchCropBlock mandrágora = (WitchCropBlock) OccultaBlocks.MANDRAKE;
        BlockState feita = mandrágora.getStateForAge(mandrágora.getMaxAge());
        int voltas = 500;
        int fugiu = 0;
        for (int volta = 0; volta < voltas; volta++) {
            if (OccultaCrops.drops(OccultaBlocks.MANDRAKE, feita, level, level.getRandom(), 0).isEmpty()) fugiu++;
        }
        double parte = fugiu / (double) voltas;
        double conta = level.isBrightOutside() ? OccultaCrops.ESCAPE_BY_DAY : OccultaCrops.ESCAPE_BY_NIGHT;
        if (Math.abs(parte - conta) > 0.1) {
            helper.fail("de " + (level.isBrightOutside() ? "dia" : "noite") + " a mandrágora devia fugir em "
                    + conta + " das vezes; fugiu em " + parte);
        }
        // e nenhuma das outras foge
        RandomSource sorte = level.getRandom();
        for (Block planta : AS_OITO) {
            if (planta == OccultaBlocks.MANDRAKE) continue;
            if (OccultaCrops.escapa(planta, level, sorte)) helper.fail(planta + " não foge de quem a colhe");
        }
        helper.succeed();
    }

    /** A farinha de osso adianta duas ou mais idades na beladona, e uma só na mindrake e na acônito. */
    @GameTest
    public void bonemealHelpsSomeMoreThanOthers(GameTestHelper helper) {
        BlockPos onde = new BlockPos(1, 3, 1);
        helper.setBlock(onde.below(), Blocks.FARMLAND);
        for (Block planta : List.of(OccultaBlocks.BELLADONNA, OccultaBlocks.MINDRAKE, OccultaBlocks.WOLFSBANE)) {
            WitchCropBlock crop = (WitchCropBlock) planta;
            boolean devagar = planta != OccultaBlocks.BELLADONNA;
            for (int volta = 0; volta < 20; volta++) {
                BlockPos absoluto = helper.absolutePos(onde);
                helper.getLevel().setBlockAndUpdate(absoluto, crop.getStateForAge(0));
                crop.performBonemeal(helper.getLevel(), helper.getLevel().getRandom(), absoluto,
                        helper.getLevel().getBlockState(absoluto));
                int idade = crop.getAge(helper.getLevel().getBlockState(absoluto));
                if (devagar && idade != 1) {
                    helper.fail(planta + " não aceita adiantar mais de uma idade; foi para " + idade);
                    return;
                }
                if (!devagar && idade < 2) {
                    helper.fail(planta + " devia adiantar duas idades ou mais; foi para " + idade);
                    return;
                }
            }
        }
        helper.succeed();
    }

    /** A losna feita sobe outra em cima dela; as outras não sobem. */
    @GameTest
    public void wormwoodStacksItself(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = new BlockPos(1, 2, 1);
        helper.setBlock(onde.below(), Blocks.FARMLAND);
        WitchCropBlock losna = (WitchCropBlock) OccultaBlocks.WORMWOOD;
        BlockPos absoluto = helper.absolutePos(onde);
        level.setBlockAndUpdate(absoluto, losna.getStateForAge(losna.getMaxAge()));
        for (int volta = 0; volta < 20; volta++) {
            level.getBlockState(absoluto).randomTick(level, absoluto, level.getRandom());
        }
        if (!level.getBlockState(absoluto.above()).is(OccultaBlocks.WORMWOOD)) {
            helper.fail("a losna feita devia subir outra em cima dela");
            return;
        }
        // e a de cima não sobe mais nenhuma, porque já tem losna embaixo
        level.setBlockAndUpdate(absoluto.above(), losna.getStateForAge(losna.getMaxAge()));
        for (int volta = 0; volta < 20; volta++) {
            level.getBlockState(absoluto.above()).randomTick(level, absoluto.above(), level.getRandom());
        }
        if (level.getBlockState(absoluto.above(2)).is(OccultaBlocks.WORMWOOD)) {
            helper.fail("a losna não devia subir uma terceira");
        }
        helper.succeed();
    }

    /** Com luz, a planta cresce sozinha; a mindrake demora mais que as outras. */
    @GameTest
    public void theyGrowWithLight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = new BlockPos(1, 3, 1);
        helper.setBlock(onde.below(), Blocks.FARMLAND);
        BlockPos absoluto = helper.absolutePos(onde);
        WitchCropBlock beladona = (WitchCropBlock) OccultaBlocks.BELLADONNA;
        level.setBlockAndUpdate(absoluto, beladona.getStateForAge(0));
        for (int volta = 0; volta < 400; volta++) {
            level.getBlockState(absoluto).randomTick(level, absoluto, level.getRandom());
        }
        if (beladona.getAge(level.getBlockState(absoluto)) == 0) {
            helper.fail("a beladona devia ter crescido em quatrocentas batidas do acaso");
        }
        helper.succeed();
    }

    /** E o thaumômetro tem o que ler nelas — que é do porte, porque o original não anotava aspecto em nada. */
    @GameTest
    public void theHarvestsHaveTheirAspects(GameTestHelper helper) {
        var raiz = ObjectAspects.of(OccultaItems.MANDRAKE_ROOT);
        if (raiz.getAmount(Aspects.MAGIC) < 2 || raiz.getAmount(Aspects.SOUL) < 2) {
            helper.fail("a raiz de mandrágora é praecantatio e spiritus; veio " + raiz);
        }
        var agulha = ObjectAspects.of(OccultaItems.ICY_NEEDLE);
        if (agulha.getAmount(Aspects.COLD) < 3) helper.fail("a agulha de gelo é gelum; veio " + agulha);
        var semente = ObjectAspects.of(OccultaItems.BELLADONNA_SEEDS);
        if (semente.getAmount(Aspects.PLANT) < 1 || semente.getAmount(Aspects.POISON) < 1) {
            helper.fail("a semente de beladona é herba e venenum; veio " + semente);
        }
        helper.succeed();
    }

    private static void confereSemente(GameTestHelper helper, net.minecraft.world.item.Item semente, Block planta) {
        if (!(semente instanceof BlockItem posta) || posta.getBlock() != planta) {
            helper.fail(semente + " devia plantar " + planta);
        }
    }
}
