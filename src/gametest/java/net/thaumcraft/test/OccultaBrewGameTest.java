package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.WitchesCauldronBlockEntity;
import net.thaumcraft.occulta.brew.Brew;
import net.thaumcraft.occulta.brew.BrewCapacity;
import net.thaumcraft.occulta.brew.BrewItem;
import net.thaumcraft.occulta.brew.BrewModifiers;
import net.thaumcraft.occulta.brew.BrewRegistry;

import java.util.List;

/**
 * O motor de cozimentos: o espaço do caldeirão, os temperos, o nome que se monta e o frasco que sai.
 */
public class OccultaBrewGameTest {
    /** Água não recebe efeito nenhum: sem ingrediente de porte, não entra nada. */
    @GameTest
    public void waterTakesNoEffect(GameTestHelper helper) {
        if (Brew.canAdd(List.of(), Items.SPIDER_EYE, false)) {
            helper.fail("num caldeirão de água pura o veneno não tem onde caber");
        }
        // a Raiz de Mandrágora abre o primeiro espaço, mas um só — e o veneno pesa dois
        if (!Brew.canAdd(List.of(), OccultaItems.MANDRAKE_ROOT, false)) {
            helper.fail("a raiz entra em água pura");
        }
        List<Item> comRaiz = List.of(OccultaItems.MANDRAKE_ROOT);
        if (Brew.canAdd(comRaiz, Items.SPIDER_EYE, false)) {
            helper.fail("a raiz abre um só de espaço, e o veneno pesa dois");
        }
        // a Verruga do Nether abre dois, e aí cabe
        List<Item> comVerruga = List.of(OccultaItems.MANDRAKE_ROOT, Items.NETHER_WART);
        if (!Brew.canAdd(comVerruga, Items.SPIDER_EYE, false)) {
            helper.fail("com a verruga há dois de espaço, e o veneno cabe");
        }
        helper.succeed();
    }

    /** Dois ingredientes de porte não somam sempre: o segundo olha o teto do primeiro. */
    @GameTest
    public void roomHasACeiling(GameTestHelper helper) {
        BrewCapacity duasVerrugas = Brew.capacity(List.of(Items.NETHER_WART, Items.NETHER_WART));
        if (duasVerrugas.max() != 2) {
            helper.fail("duas verrugas não valem quatro: a segunda vê que já se passou do teto dela; deu "
                    + duasVerrugas.max());
        }
        BrewCapacity raizEVerruga = Brew.capacity(List.of(OccultaItems.MANDRAKE_ROOT, Items.NETHER_WART));
        if (raizEVerruga.max() != 3) {
            helper.fail("a raiz abre um e a verruga mais dois; deu " + raizEVerruga.max());
        }
        BrewCapacity comDiamante = Brew.capacity(List.of(Items.NETHER_WART, Items.DIAMOND));
        if (comDiamante.max() != 4) {
            helper.fail("o diamante abre dois enquanto o teto dele (oito) não se alcançou; deu "
                    + comDiamante.max());
        }
        helper.succeed();
    }

    /** O mesmo ingrediente não entra duas vezes seguidas sem um efeito de permeio. */
    @GameTest
    public void noIngredientRepeatsItself(GameTestHelper helper) {
        List<Item> dentro = List.of(Items.NETHER_WART, Items.DIAMOND, Items.GLOWSTONE_DUST);
        if (Brew.canAdd(dentro, Items.GLOWSTONE_DUST, false)) {
            helper.fail("dois pós de pedra luminosa em seguida não fazem nada, e o original recusa o segundo");
        }
        // com um efeito de permeio, entra
        List<Item> comEfeito = List.of(Items.NETHER_WART, Items.DIAMOND, Items.GLOWSTONE_DUST, Items.SPIDER_EYE);
        if (!Brew.canAdd(comEfeito, Items.GLOWSTONE_DUST, false)) {
            helper.fail("depois de um efeito, o mesmo tempero entra outra vez");
        }
        helper.succeed();
    }

    /** O poder que o altar tem de dar é a soma do que cada coisa custa. */
    @GameTest
    public void powerIsTheSumOfTheParts(GameTestHelper helper) {
        // verruga 50 + veneno 0
        if (Brew.power(List.of(Items.NETHER_WART, Items.SPIDER_EYE)) != 50) {
            helper.fail("verruga e olho de aranha custam cinquenta; deu "
                    + Brew.power(List.of(Items.NETHER_WART, Items.SPIDER_EYE)));
        }
        // verruga 50 + pó de pedra luminosa 50 + cenoura dourada 200
        int três = Brew.power(List.of(Items.NETHER_WART, Items.GLOWSTONE_DUST, Items.GOLDEN_CARROT));
        if (três != 300) helper.fail("os três custam trezentos; deu " + três);
        if (Brew.power(List.of()) != 0) helper.fail("água pura não custa nada");
        helper.succeed();
    }

    /** A cor sai da conta do original, e a lã manda nela à força. */
    @GameTest
    public void colorComesFromWhatIsInside(GameTestHelper helper) {
        int veneno = Brew.color(List.of(Items.NETHER_WART, Items.SPIDER_EYE));
        int velocidade = Brew.color(List.of(Items.NETHER_WART, Items.SUGAR));
        if (veneno == velocidade) helper.fail("duas receitas diferentes não têm a mesma cor");
        if (Brew.color(List.of(Items.NETHER_WART, Items.SPIDER_EYE)) != veneno) {
            helper.fail("e a mesma receita tem sempre a mesma cor");
        }
        int comLã = Brew.color(List.of(Items.NETHER_WART, Items.SPIDER_EYE, Items.WOOL.red()));
        if (comLã != (net.minecraft.world.item.DyeColor.RED.getTextureDiffuseColor() & 0xFFFFFF)) {
            helper.fail("a lã vermelha manda na cor à força; deu " + Integer.toHexString(comLã));
        }
        helper.succeed();
    }

    /** O tempero vale para o efeito seguinte, e só para ele. */
    @GameTest
    public void aTemperingLastsOneEffect(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var bicho = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));

        // pedra luminosa antes do veneno: veneno II; a cenoura depois vem sem força
        List<Item> dentro = List.of(Items.NETHER_WART, Items.DIAMOND, Items.GLOWSTONE_DUST, Items.SPIDER_EYE,
                Items.GOLDEN_CARROT);
        Brew.apply(level, bicho, dentro, new BrewModifiers());

        var veneno = bicho.getEffect(MobEffects.POISON);
        if (veneno == null || veneno.getAmplifier() != 1) {
            helper.fail("a pedra luminosa dá um grau ao veneno; veio " + veneno);
        }
        var visão = bicho.getEffect(MobEffects.NIGHT_VISION);
        if (visão == null) helper.fail("a cenoura dourada dá visão noturna");
        else if (visão.getAmplifier() != 0) {
            helper.fail("e ela vem sem força, porque o tempero já se gastou no veneno");
        }
        bicho.discard();
        helper.succeed();
    }

    /** O olho de aranha fermentado inverte o efeito seguinte. */
    @GameTest
    public void aFermentedEyeTurnsTheEffectAround(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var bicho = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));
        Brew.apply(level, bicho, List.of(Items.NETHER_WART, Items.FERMENTED_SPIDER_EYE, Items.SUGAR),
                new BrewModifiers());
        if (bicho.hasEffect(MobEffects.SPEED)) helper.fail("invertido, o açúcar não dá velocidade");
        if (!bicho.hasEffect(MobEffects.SLOWNESS)) helper.fail("invertido, o açúcar dá lentidão");
        bicho.discard();
        helper.succeed();
    }

    /** O nome do frasco monta-se do que está dentro. */
    @GameTest
    public void theNameIsBuiltFromTheBrew(GameTestHelper helper) {
        String água = Brew.name(List.of()).getString();
        if (!água.contains("Water") && !água.contains("Água")) {
            helper.fail("um caldeirão sem nada é Cozimento de Água; veio " + água);
        }
        String veneno = Brew.name(List.of(Items.NETHER_WART, Items.SPIDER_EYE)).getString();
        if (!veneno.contains("Poison") && !veneno.contains("Veneno")) {
            helper.fail("com olho de aranha é Cozimento de Veneno; veio " + veneno);
        }
        String dois = Brew.name(List.of(Items.NETHER_WART, Items.DIAMOND, Items.SPIDER_EYE, Items.SUGAR))
                .getString();
        if (!dois.contains("&") && !dois.contains(" e ")) {
            helper.fail("dois efeitos juntam-se com um \"e\"; veio " + dois);
        }
        helper.succeed();
    }

    /** E o caldeirão fervendo entrega o frasco, com o que estava dentro. */
    @GameTest(maxTicks = 200)
    public void theCauldronFillsABottle(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // dois caldeirões a ferver ao mesmo tempo: um com cozimento que pede poder, outro com um que não pede
        WitchesCauldronBlockEntity semPoder = cauldron(helper, level, new BlockPos(1, 2, 1));
        WitchesCauldronBlockEntity comPoder = cauldron(helper, level, new BlockPos(3, 2, 1));
        BlockPos ondeSem = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos ondeCom = helper.absolutePos(new BlockPos(3, 2, 1));

        // frio não se engarrafa
        if (!semPoder.bottle(level, ondeSem).isEmpty()) helper.fail("caldeirão frio não dá frasco nenhum");

        // espera-se ferver de uma vez: dentro de um succeedWhen, um helper.fail seria engolido como "ainda não"
        helper.runAfterDelay(WitchesCauldronBlockEntity.TICKS_TO_BOIL + 20, () -> {
            if (!semPoder.isBoiling() || !comPoder.isBoiling()) {
                helper.fail("os dois caldeirões deviam ter fervido");
                return;
            }

            // o que pede poder não sai sem altar por perto
            comPoder.addItem(new ItemStack(Items.NETHER_WART));
            comPoder.addItem(new ItemStack(Items.SPIDER_EYE));
            if (comPoder.brewPower() != 50) helper.fail("este cozimento pede cinquenta de poder");
            if (!comPoder.bottle(level, ondeCom).isEmpty()) {
                helper.fail("sem altar por perto, o cozimento que pede poder não sai");
            }

            // e o que não pede nada sai
            semPoder.addItem(new ItemStack(OccultaItems.MANDRAKE_ROOT));
            if (semPoder.brewPower() != 0) helper.fail("a raiz de mandrágora não custa poder nenhum");
            ItemStack frasco = semPoder.bottle(level, ondeSem);
            if (frasco.isEmpty()) {
                helper.fail("sem poder pedido, o frasco sai");
                return;
            }
            if (!frasco.is(OccultaItems.BREW)) helper.fail("e o que sai é um frasco de cozimento");
            if (!BrewItem.contents(frasco).equals(List.of(OccultaItems.MANDRAKE_ROOT))) {
                helper.fail("o frasco leva o que estava dentro; leva " + BrewItem.contents(frasco));
            }
            if (semPoder.water() != 0) helper.fail("e o caldeirão fica vazio");
            helper.succeed();
        });
    }

    /** Um caldeirão cheio, com fogo eterno embaixo. */
    private static WitchesCauldronBlockEntity cauldron(GameTestHelper helper, ServerLevel level, BlockPos onde) {
        BlockPos absoluto = helper.absolutePos(onde);
        level.setBlockAndUpdate(absoluto, OccultaBlocks.WITCHES_CAULDRON.defaultBlockState());
        level.setBlockAndUpdate(absoluto.below(2), Blocks.NETHERRACK.defaultBlockState());
        level.setBlockAndUpdate(absoluto.below(), Blocks.FIRE.defaultBlockState());
        if (!(level.getBlockEntity(absoluto) instanceof WitchesCauldronBlockEntity caldeirão)) {
            helper.fail("o caldeirão devia ter miolo");
            throw new IllegalStateException();
        }
        caldeirão.fill(WitchesCauldronBlockEntity.FULL);
        return caldeirão;
    }

    /** A tabela conhece o que tem de conhecer, e não conhece o que não é de cozimento. */
    @GameTest
    public void theTableKnowsItsIngredients(GameTestHelper helper) {
        for (Item item : List.of(Items.SPIDER_EYE, Items.SUGAR, Items.NETHER_WART, Items.DIAMOND,
                Items.GLOWSTONE_DUST, Items.REDSTONE, Items.FERMENTED_SPIDER_EYE, OccultaItems.MANDRAKE_ROOT,
                OccultaItems.MINDRAKE_BULB, OccultaItems.ROWAN_BERRIES)) {
            if (!BrewRegistry.knows(item)) helper.fail(item + " devia estar na tabela dos cozimentos");
        }
        if (BrewRegistry.knows(Items.COBBLESTONE)) helper.fail("pedregulho não se coze");
        // e as dezesseis lãs pintam o caldo
        int lãs = 0;
        for (var cor : net.minecraft.world.item.DyeColor.values()) {
            if (BrewRegistry.knows(Items.WOOL.pick(cor))) lãs++;
        }
        if (lãs != 16) helper.fail("as dezesseis lãs pintam o caldo; achei " + lãs);
        helper.succeed();
    }
}
