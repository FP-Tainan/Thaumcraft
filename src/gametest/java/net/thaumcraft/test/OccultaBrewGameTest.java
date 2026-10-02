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
        if (!semPoder.bottle(level, ondeSem, null).isEmpty()) helper.fail("caldeirão frio não dá frasco nenhum");

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
            if (!comPoder.bottle(level, ondeCom, null).isEmpty()) {
                helper.fail("sem altar por perto, o cozimento que pede poder não sai");
            }

            // e o que não pede nada sai
            semPoder.addItem(new ItemStack(OccultaItems.MANDRAKE_ROOT));
            if (semPoder.brewPower() != 0) helper.fail("a raiz de mandrágora não custa poder nenhum");
            ItemStack frasco = semPoder.bottle(level, ondeSem, null);
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

    /**
     * Toda receita que o livro ensina é receita que o caldeirão aceita — ingrediente por ingrediente, na ordem.
     *
     * <p>É a prova que guarda as quatro ramificações de receitas: se alguma delas pedir um efeito que não cabe
     * no espaço aberto, ou repetir um ingrediente onde não se pode, ela cai aqui e não no jogo de quem lê.
     */
    @GameTest
    public void everyBrewInTheBookIsBrewable(GameTestHelper helper) {
        int quantas = 0;
        for (var pesquisa : net.thaumcraft.research.Researches.of(net.thaumcraft.occulta.Occulta.CATEGORY)) {
            for (var página : pesquisa.pages()) {
                if (!(página instanceof net.thaumcraft.research.Page.Brew cozimento)) continue;
                quantas++;
                List<Item> dentro = new java.util.ArrayList<>();
                for (var cai : cozimento.ingredients()) {
                    Item item = cai.get().getItem();
                    if (!Brew.canAdd(dentro, item, false)) {
                        helper.fail("o livro ensina uma receita que o caldeirão recusa: " + dentro + " + " + item);
                        return;
                    }
                    dentro = Brew.add(dentro, item);
                }
                // e o que o livro diz que custa é o que custa mesmo
                if (cozimento.power() != Brew.power(dentro)) {
                    helper.fail("o poder escrito na página não bate com o do motor: " + dentro);
                }
                // e a receita tem de fazer alguma coisa: uma receita sem efeito nenhum não se ensina
                if (Brew.capacity(dentro).effects() == 0) {
                    helper.fail("esta receita não faz efeito nenhum: " + dentro);
                }
            }
        }
        if (quantas < 16) helper.fail("as quatro ramificações têm dezesseis receitas; achei " + quantas);
        helper.succeed();
    }

    /** A pólvora faz o cozimento de atirar, e o nome dele muda. */
    @GameTest
    public void gunpowderMakesItThrowable(GameTestHelper helper) {
        List<Item> bebido = List.of(Items.NETHER_WART, Items.SPIDER_EYE);
        List<Item> atirado = List.of(Items.NETHER_WART, Items.SPIDER_EYE, Items.GUNPOWDER);
        if (Brew.splash(bebido)) helper.fail("sem pólvora, o cozimento se bebe");
        if (!Brew.splash(atirado)) helper.fail("com pólvora, o cozimento se atira");
        String nome = Brew.name(atirado).getString();
        if (!nome.contains("Splash") && !nome.contains("Arremess")) {
            helper.fail("e o nome dele diz que se atira; veio " + nome);
        }
        // o globo de alcachofra faz o mesmo
        if (!Brew.splash(List.of(Items.NETHER_WART, Items.SPIDER_EYE, OccultaItems.WATER_ARTICHOKE_GLOBE))) {
            helper.fail("o globo de alcachofra também faz o cozimento se atirar");
        }
        helper.succeed();
    }

    /** Dois jeitos de espalhar não convivem: o novo desfaz o velho. */
    @GameTest
    public void oneDispersalUndoesTheOther(GameTestHelper helper) {
        List<Item> dentro = Brew.add(List.of(Items.NETHER_WART, Items.SPIDER_EYE), Items.GUNPOWDER);
        if (!dentro.contains(Items.GUNPOWDER)) helper.fail("a pólvora entrou");
        List<Item> depois = Brew.add(dentro, OccultaItems.WATER_ARTICHOKE_GLOBE);
        if (depois.contains(Items.GUNPOWDER)) {
            helper.fail("o globo desfaz a pólvora que estava lá; ficou " + depois);
        }
        if (!depois.contains(OccultaItems.WATER_ARTICHOKE_GLOBE)) helper.fail("e fica ele no lugar");
        helper.succeed();
    }

    /** A cinza de madeira e o cacau alargam o estouro; a flor e o lápis esticam o que fica no chão. */
    @GameTest
    public void ashAndCocoaWidenTheSplash(GameTestHelper helper) {
        var nu = Brew.impact(List.of(Items.GUNPOWDER), null);
        if (nu.extent != 0) helper.fail("sem tempero, o estouro é o de sempre");
        var comCinza = Brew.impact(List.of(OccultaItems.WOOD_ASH, Items.GUNPOWDER), null);
        if (comCinza.extent != 1) helper.fail("a cinza de madeira alarga um; deu " + comCinza.extent);
        var comAmbos = Brew.impact(List.of(OccultaItems.WOOD_ASH, Items.COCOA_BEANS, Items.GUNPOWDER), null);
        if (comAmbos.extent != 2) helper.fail("cinza e cacau alargam dois; deu " + comAmbos.extent);
        var duasCinzas = Brew.impact(List.of(OccultaItems.WOOD_ASH, OccultaItems.WOOD_ASH, Items.GUNPOWDER), null);
        if (duasCinzas.extent != 1) helper.fail("duas cinzas não valem duas: a segunda vê o teto dela");
        var comLápis = Brew.impact(List.of(Items.LAPIS_LAZULI, Items.GUNPOWDER), null);
        if (comLápis.lifetime != 1) helper.fail("o lápis-lazúli estica um; deu " + comLápis.lifetime);
        helper.succeed();
    }

    /** O frasco atirado arrebenta e o que estiver perto apanha o cozimento. */
    @GameTest
    public void theThrownBottleCatchesWhoIsNear(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var perto = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));
        var longe = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 6));

        List<Item> dentro = List.of(Items.NETHER_WART, Items.SPIDER_EYE, Items.GUNPOWDER);
        var onde = new net.minecraft.world.phys.BlockHitResult(
                perto.position(), net.minecraft.core.Direction.UP,
                helper.absolutePos(new BlockPos(2, 1, 2)), false);
        if (!Brew.impact(level, dentro, onde, null)) helper.fail("o frasco com pólvora espalha");

        if (!perto.hasEffect(MobEffects.POISON)) helper.fail("quem estava no estouro apanha o veneno");
        if (longe.hasEffect(MobEffects.POISON)) {
            helper.fail("e quem estava a quatro blocos, não: o estouro alcança três");
        }

        // e sem jeito de espalhar não há estouro nenhum
        if (Brew.impact(level, List.of(Items.NETHER_WART, Items.SPIDER_EYE), onde, null)) {
            helper.fail("sem pólvora não há frasco atirado");
        }
        perto.discard();
        longe.discard();
        helper.succeed();
    }

    /** E o frasco de atirar voa da mão em vez de se beber. */
    @GameTest
    public void theBottleFliesFromTheHand(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var quem = helper.makeMockServerPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack frasco = BrewItem.of(OccultaItems.BREW,
                List.of(Items.NETHER_WART, Items.SPIDER_EYE, Items.GUNPOWDER));
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, frasco);
        frasco.getItem().use(level, quem, net.minecraft.world.InteractionHand.MAIN_HAND);

        var voando = level.getEntitiesOfClass(net.thaumcraft.occulta.brew.BrewProjectile.class,
                new net.minecraft.world.phys.AABB(quem.blockPosition()).inflate(8.0));
        if (voando.isEmpty()) helper.fail("o frasco de atirar sai da mão a voar");
        else if (!BrewItem.contents(voando.getFirst().getItem()).contains(Items.SPIDER_EYE)) {
            helper.fail("e leva o cozimento com ele");
        }
        voando.forEach(net.minecraft.world.entity.Entity::discard);
        helper.succeed();
    }

    /** A lã de morcego faz o cozimento virar nuvem em vez de estouro. */
    @GameTest
    public void batWoolMakesACloud(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 3, 2));
        List<Item> dentro = List.of(Items.NETHER_WART, Items.SPIDER_EYE, OccultaItems.BAT_WOOL);
        if (!Brew.splash(dentro)) helper.fail("a nuvem também se atira");

        var bateu = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(onde.below()),
                net.minecraft.core.Direction.UP, onde.below(), false);
        if (!Brew.impact(level, dentro, bateu, null)) helper.fail("o frasco de névoa espalha");
        if (!level.getBlockState(onde).is(OccultaBlocks.BREW_GAS)) {
            helper.fail("e onde ele bateu fica uma nuvem; ficou " + level.getBlockState(onde));
            return;
        }
        if (!(level.getBlockEntity(onde) instanceof net.thaumcraft.occulta.brew.BrewFluidBlockEntity nuvem)) {
            helper.fail("a nuvem leva o cozimento dentro dela");
            return;
        }
        if (!nuvem.contents().equals(dentro)) helper.fail("e leva tudo o que estava na panela");
        if (nuvem.color() != Brew.color(dentro)) helper.fail("e a cor do que se cozeu");

        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** A nuvem cresce sozinha, e quem passa dentro dela apanha o cozimento. */
    @GameTest(maxTicks = 200)
    public void theCloudSpreadsAndTouches(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 3, 2));
        // com a flor de beladona ela dura mais, e assim a prova não fica no sorteio de quando ela morre
        List<Item> dentro = List.of(Items.NETHER_WART, Items.SPIDER_EYE, OccultaItems.BELLADONNA_FLOWER,
                OccultaItems.BAT_WOOL);
        level.setBlockAndUpdate(onde, OccultaBlocks.BREW_GAS.defaultBlockState());
        if (level.getBlockEntity(onde) instanceof net.thaumcraft.occulta.brew.BrewFluidBlockEntity nuvem) {
            nuvem.start(dentro, Brew.impact(dentro, null));
        }

        var bicho = helper.spawn(EntityTypes.PIG, new BlockPos(2, 3, 2));
        bicho.setNoAi(true);
        // guarda-se o maior que já se viu: a nuvem cresce e some, e o que importa é que ela cresceu
        int[] maior = new int[1];
        helper.succeedWhen(() -> {
            int quantas = 0;
            for (var perto : BlockPos.betweenClosed(onde.offset(-4, -4, -4), onde.offset(4, 4, 4))) {
                if (level.getBlockState(perto).is(OccultaBlocks.BREW_GAS)) quantas++;
            }
            maior[0] = Math.max(maior[0], quantas);
            if (maior[0] < 2) throw helper.assertionException("a nuvem ainda não cresceu; há " + quantas);
            if (!bicho.hasEffect(MobEffects.POISON)) {
                throw helper.assertionException("quem está dentro dela ainda não apanhou o cozimento");
            }
            bicho.discard();
        });
    }

    /** E a nuvem some sozinha, que é o que a faz não durar para sempre. */
    @GameTest(maxTicks = 400)
    public void theCloudFadesAway(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 3, 2));
        List<Item> dentro = List.of(Items.NETHER_WART, Items.SPIDER_EYE, OccultaItems.BAT_WOOL);
        level.setBlockAndUpdate(onde, OccultaBlocks.BREW_GAS.defaultBlockState());
        if (level.getBlockEntity(onde) instanceof net.thaumcraft.occulta.brew.BrewFluidBlockEntity nuvem) {
            nuvem.start(dentro, Brew.impact(dentro, null));
        }
        helper.succeedWhen(() -> {
            for (var perto : BlockPos.betweenClosed(onde.offset(-6, -6, -6), onde.offset(6, 6, 6))) {
                if (level.getBlockState(perto).is(OccultaBlocks.BREW_GAS)) {
                    throw helper.assertionException("ainda há nuvem no ar");
                }
            }
        });
    }

    /** A derrubada leva os troncos que estiverem por perto. */
    @GameTest
    public void fellingTakesTheLogs(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(onde.above(), Blocks.OAK_LOG.defaultBlockState());
        level.setBlockAndUpdate(onde.above(2), Blocks.OAK_LOG.defaultBlockState());
        level.setBlockAndUpdate(onde.north(), Blocks.OAK_LOG.defaultBlockState());

        List<Item> dentro = List.of(Items.NETHER_WART, Items.STRING, Items.GUNPOWDER);
        Brew.applyToBlock(level, dentro, onde, net.minecraft.core.Direction.UP, 3, new BrewModifiers());

        if (!level.getBlockState(onde.above()).isAir()) helper.fail("o tronco em cima devia ter caído");
        if (!level.getBlockState(onde.north()).isAir()) helper.fail("e o do lado também");
        level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(onde).inflate(6.0))
                .forEach(net.minecraft.world.entity.Entity::discard);
        helper.succeed();
    }

    /** A pulverização desfaz a pedra um degrau de cada vez. */
    @GameTest
    public void pulverisationGrindsTheStone(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(onde, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(onde.north(), Blocks.COBBLESTONE.defaultBlockState());
        level.setBlockAndUpdate(onde.south(), Blocks.GRAVEL.defaultBlockState());

        List<Item> dentro = List.of(Items.NETHER_WART, Items.FLINT, Items.GUNPOWDER);
        Brew.applyToBlock(level, dentro, onde, net.minecraft.core.Direction.UP, 2, new BrewModifiers());

        if (!level.getBlockState(onde).is(Blocks.COBBLESTONE)) helper.fail("pedra vira pedregulho");
        if (!level.getBlockState(onde.north()).is(Blocks.GRAVEL)) helper.fail("pedregulho vira cascalho");
        if (!level.getBlockState(onde.south()).is(Blocks.SAND)) helper.fail("e cascalho vira areia");

        for (var lugar : BlockPos.betweenClosed(onde.offset(-3, -3, -3), onde.offset(3, 3, 3))) {
            if (!level.getBlockState(lugar).isAir()) level.setBlockAndUpdate(lugar, Blocks.AIR.defaultBlockState());
        }
        helper.succeed();
    }

    /** A praga seca o mato e apodrece o chão. */
    @GameTest
    public void blightWithersTheGround(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(onde, Blocks.GRASS_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(onde.above(), Blocks.SHORT_GRASS.defaultBlockState());
        level.setBlockAndUpdate(onde.north(), Blocks.GRASS_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(onde.north().above(), Blocks.DANDELION.defaultBlockState());

        List<Item> dentro = List.of(Items.NETHER_WART, Items.DIAMOND, Items.POISONOUS_POTATO, Items.GUNPOWDER);
        Brew.applyToBlock(level, dentro, onde.above(), net.minecraft.core.Direction.UP, 3, new BrewModifiers());

        if (!level.getBlockState(onde.above()).isAir()) helper.fail("o mato some");
        if (!level.getBlockState(onde.north().above()).is(Blocks.DEAD_BUSH)) {
            helper.fail("e a flor vira arbusto seco; ficou " + level.getBlockState(onde.north().above()));
        }
        for (var lugar : BlockPos.betweenClosed(onde.offset(-4, -1, -4), onde.offset(4, 2, 4))) {
            level.setBlockAndUpdate(lugar, Blocks.AIR.defaultBlockState());
        }
        helper.succeed();
    }

    /** E estes efeitos só acontecem no frasco atirado: bebidos, não fazem nada ao lugar. */
    @GameTest
    public void theseOnlyHappenWhenThrown(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(onde.above(), Blocks.OAK_LOG.defaultBlockState());
        var bicho = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 3));
        Brew.apply(level, bicho, List.of(Items.NETHER_WART, Items.STRING), new BrewModifiers());
        if (level.getBlockState(onde.above()).isAir()) {
            helper.fail("bebida, a derrubada não derruba nada");
        }
        level.setBlockAndUpdate(onde.above(), Blocks.AIR.defaultBlockState());
        bicho.discard();
        helper.succeed();
    }

    /** As sete poções do ofício entram no caldeirão e pegam em quem bebe. */
    @GameTest
    public void theCraftsOwnPotionsWork(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var bicho = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));
        Brew.apply(level, bicho, List.of(Items.NETHER_WART, Items.FEATHER), new BrewModifiers());
        if (!bicho.hasEffect(net.thaumcraft.occulta.OccultaEffects.FEATHER_FALL)) {
            helper.fail("a pena dá queda de pena");
        }
        Brew.apply(level, bicho, List.of(Items.NETHER_WART, Items.SUGAR_CANE), new BrewModifiers());
        if (!bicho.hasEffect(net.thaumcraft.occulta.OccultaEffects.FLOATING)) {
            helper.fail("a cana-de-açúcar faz flutuar");
        }
        // e o bacalhau, que é leve, cabe num caldeirão com só um de espaço
        if (!Brew.canAdd(List.of(OccultaItems.MANDRAKE_ROOT), Items.COD, false)) {
            helper.fail("o nado pesa um só, e cabe na raiz de mandrágora sozinha");
        }
        bicho.discard();
        helper.succeed();
    }

    /** A alergia ao escuro dói no escuro, e a máscara de gás guarda de névoa ruim. */
    @GameTest(maxTicks = 120)
    public void theAllergyBitesAndTheMaskGuards(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // primeiro a caixa de pedra, e só depois o bicho lá dentro: posta com ele dentro, ela o empurra para fora
        // uma sala de pedra de três por três: um buraco de um bloco é estreito demais e o bicho escorrega para
        // fora dele. As quinas também vão fechadas, senão a luz entra de canto.
        BlockPos dentro = helper.absolutePos(new BlockPos(3, 2, 3));
        for (var lugar : BlockPos.betweenClosed(dentro.offset(-2, -1, -2), dentro.offset(2, 3, 2))) {
            boolean sala = Math.abs(lugar.getX() - dentro.getX()) <= 1
                    && Math.abs(lugar.getZ() - dentro.getZ()) <= 1
                    && lugar.getY() >= dentro.getY() && lugar.getY() <= dentro.getY() + 1;
            if (sala) {
                level.setBlockAndUpdate(lugar, Blocks.AIR.defaultBlockState());
                continue;
            }
            level.setBlockAndUpdate(lugar, Blocks.STONE.defaultBlockState());
        }

        var bicho = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        bicho.setNoAi(true);
        bicho.snapTo(dentro.getX() + 0.5, dentro.getY(), dentro.getZ() + 0.5, 0.0f, 0.0f);
        float antes = bicho.getHealth();
        // do segundo grau, que é o que a caixa de pedra da arena permite: ela deixa entrar luz 3, e a conta do
        // original é "menos de dois, mais dois por grau"
        bicho.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.thaumcraft.occulta.OccultaEffects.DARKNESS_ALLERGY, 200, 1));

        // a máscara guarda do que é ruim numa névoa
        var protegido = helper.spawn(EntityTypes.PIG, new BlockPos(6, 2, 1));
        protegido.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.thaumcraft.occulta.OccultaEffects.GAS_MASK, 400, 0));
        BrewModifiers comMáscara = new BrewModifiers();
        comMáscara.protectedFromBadEffects = true;
        Brew.apply(level, protegido, List.of(Items.NETHER_WART, Items.SPIDER_EYE), comMáscara);
        if (protegido.hasEffect(MobEffects.POISON)) helper.fail("com máscara, o veneno da névoa não pega");

        // e sem ela pega
        var semMáscara = helper.spawn(EntityTypes.PIG, new BlockPos(6, 2, 3));
        Brew.apply(level, semMáscara, List.of(Items.NETHER_WART, Items.SPIDER_EYE), new BrewModifiers());
        if (!semMáscara.hasEffect(MobEffects.POISON)) helper.fail("sem máscara, pega");

        helper.succeedWhen(() -> {
            if (bicho.getHealth() >= antes) {
                throw helper.assertionException("no escuro, a alergia ainda não doeu; a luz aqui é "
                        + level.getMaxLocalRawBrightness(bicho.blockPosition()));
            }
            bicho.discard();
            protegido.discard();
            semMáscara.discard();
            for (var lugar : BlockPos.betweenClosed(dentro.offset(-2, -1, -2), dentro.offset(2, 3, 2))) {
                if (level.getBlockState(lugar).is(Blocks.STONE)) {
                    level.setBlockAndUpdate(lugar, Blocks.AIR.defaultBlockState());
                }
            }
        });
    }

    /** As armas envenenadas envenenam quem se acerta, e não quem as bebe. */
    @GameTest
    public void poisonWeaponsPoisonTheOther(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var quemBate = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(2, 2, 2));
        var quemLeva = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 3));
        Brew.apply(level, quemBate, List.of(Items.NETHER_WART, Items.RED_MUSHROOM), new BrewModifiers());
        if (!quemBate.hasEffect(net.thaumcraft.occulta.OccultaEffects.POISON_WEAPONS)) {
            helper.fail("o cogumelo vermelho põe veneno nas armas de quem bebe");
        }
        if (quemBate.hasEffect(MobEffects.POISON)) helper.fail("mas não envenena quem bebeu");

        quemBate.doHurtTarget(level, quemLeva);
        if (!quemLeva.hasEffect(MobEffects.POISON)) helper.fail("e quem apanha o golpe é que se envenena");
        quemBate.discard();
        quemLeva.discard();
        helper.succeed();
    }

    /** A volatilidade estoura quem apanha. */
    @GameTest
    public void volatilityBlowsUpWhoIsHurt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var bicho = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));
        bicho.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.thaumcraft.occulta.OccultaEffects.VOLATILITY, 400, 3));
        var perto = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 2));
        float antes = perto.getHealth();

        // com o grau alto, quase toda pancada estoura; vinte tentativas bastam
        for (int volta = 0; volta < 20 && perto.getHealth() >= antes; volta++) {
            bicho.hurtServer(level, level.damageSources().magic(), 1.0f);
            bicho.invulnerableTime = 0;
            bicho.setHealth(20.0f);
        }
        if (perto.getHealth() >= antes) helper.fail("quem estava ao lado devia ter apanhado do estouro");
        bicho.discard();
        perto.discard();
        helper.succeed();
    }

    /** E os espinhos ferem quem se encosta. */
    @GameTest(maxTicks = 100)
    public void spikesHurtWhoTouches(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var comEspinho = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));
        comEspinho.setNoAi(true);
        comEspinho.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.thaumcraft.occulta.OccultaEffects.SPIKED, 200, 0));
        var encostado = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));
        encostado.setNoAi(true);
        float antes = encostado.getHealth();
        helper.succeedWhen(() -> {
            if (encostado.getHealth() >= antes) {
                throw helper.assertionException("quem está encostado ainda não se espetou");
            }
            comEspinho.discard();
            encostado.discard();
        });
    }

    /** O osso levanta um morto onde o frasco bate. */
    @GameTest
    public void theBoneRaisesTheDead(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(onde, Blocks.STONE.defaultBlockState());

        List<Item> dentro = List.of(Items.NETHER_WART, Items.DIAMOND, Items.BONE, Items.GUNPOWDER);
        Brew.applyToBlock(level, dentro, onde, net.minecraft.core.Direction.UP, 3, new BrewModifiers());

        var mortos = level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
                new net.minecraft.world.phys.AABB(onde).inflate(4.0),
                bicho -> bicho.getType() == EntityTypes.ZOMBIE || bicho.getType() == EntityTypes.SKELETON
                        || bicho.getType() == EntityTypes.ZOMBIFIED_PIGLIN);
        if (mortos.isEmpty()) helper.fail("devia ter-se levantado um morto");
        mortos.forEach(net.minecraft.world.entity.Entity::discard);
        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }
}
