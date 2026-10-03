package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.wolf.HornedHuntsmanEntity;
import net.thaumcraft.occulta.wolf.Werewolf;
import net.thaumcraft.occulta.wolf.WerewolfLadder;
import net.thaumcraft.occulta.wolf.WerewolfPowers;
import net.thaumcraft.occulta.wolf.WerewolfQuest;

/**
 * A <b>escada dos dez graus</b>: o Altar do Lobo e o que ele pede em cada degrau.
 *
 * <p>A prova que carrega a fatia é a do <b>caminho inteiro</b>: subir do grau um ao dez fazendo, em cada
 * degrau, o que aquele degrau pede. Se um degrau estiver quebrado, a escada não é uma escada — e é por isso
 * que essa prova é uma só e vai do princípio ao fim.
 */
public class OccultaWerewolfLadderGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Quantas coisas caíram à frente da estátua. */
    private static java.util.List<ItemEntity> largou(GameTestHelper helper, BlockPos onde) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(helper.absolutePos(onde)).inflate(3.0));
    }

    private static void limpaOChão(GameTestHelper helper, BlockPos onde) {
        for (ItemEntity coisa : largou(helper, onde)) coisa.discard();
    }

    /** O que cada degrau pede: os números do original, um a um. */
    @GameTest(maxTicks = 20)
    public void eachRungAsksWhatTheOriginalAsks(GameTestHelper helper) {
        if (WerewolfLadder.OURO != 3) helper.fail("o primeiro degrau pede três barras de ouro");
        if (WerewolfLadder.CARNEIRO != 30) helper.fail("o segundo, trinta carnes de carneiro");
        if (WerewolfLadder.LÍNGUAS != 10) helper.fail("o terceiro, dez línguas de cachorro");
        if (WerewolfLadder.precisaDe(5) != 10) helper.fail("o quinto, dez monstros mortos no ar");
        if (WerewolfLadder.precisaDe(6) != 16) helper.fail("o sexto, dezesseis lugares uivados");
        if (WerewolfLadder.precisaDe(7) != 6) helper.fail("o sétimo, seis lobos amansados");
        if (WerewolfLadder.precisaDe(8) != 30) helper.fail("o oitavo, trinta porcos-zumbis");
        if (WerewolfLadder.precisaDe(9) != 1) helper.fail("e o nono, uma pessoa");
        if (WerewolfLadder.precisaDe(4) != 0) helper.fail("o quarto não conta nada: ele mata o Caçador");
        if (WerewolfLadder.precisaDe(10) != 0) helper.fail("e no décimo não há mais o que pedir");
        helper.succeed();
    }

    /** O Altar, a Cabeça, o Chifre, a Lança e o Sangue estão todos no jogo. */
    @GameTest(maxTicks = 20)
    public void theAltarAndWhatComesWithItAreThere(GameTestHelper helper) {
        if (!BuiltInRegistries.BLOCK.containsKey(net.thaumcraft.Thaumcraft.id("werewolf_statue"))) {
            helper.fail("falta o Altar do Lobo");
        }
        if (!BuiltInRegistries.BLOCK.containsKey(net.thaumcraft.Thaumcraft.id("mounted_wolf_head"))) {
            helper.fail("falta a Cabeça de Lobo");
        }
        if (!BuiltInRegistries.BLOCK.containsKey(net.thaumcraft.Thaumcraft.id("mounted_wolf_head_wall"))) {
            helper.fail("e a Cabeça pregada na parede");
        }
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(net.thaumcraft.Thaumcraft.id("horned_huntsman"))) {
            helper.fail("falta o Caçador Cornudo");
        }
        for (var qual : java.util.List.of(OccultaItems.HORN_OF_THE_HUNT, OccultaItems.HUNTSMANS_SPEAR,
                OccultaItems.INFERNAL_BLOOD, OccultaItems.WOLF_HEAD, OccultaItems.WEREWOLF_STATUE)) {
            if (!BuiltInRegistries.ITEM.containsKey(BuiltInRegistries.ITEM.getKey(qual))) {
                helper.fail("falta um item da escada");
            }
        }
        helper.succeed();
    }

    /**
     * <b>Quem tem grau zero não é digno</b>: apanha Fadiga de Mineração e nada mais.
     */
    @GameTest(maxTicks = 40)
    public void theUnworthyGetNothingButFatigue(GameTestHelper helper) {
        piso(helper);
        BlockPos onde = new BlockPos(2, 2, 2);
        Player quem = helper.makeMockServerPlayerInLevel();

        WerewolfLadder.fala(helper.getLevel(), quem, ItemStack.EMPTY, helper.absolutePos(onde),
                Direction.NORTH);

        if (!quem.hasEffect(MobEffects.MINING_FATIGUE)) helper.fail("a estátua devia tê-lo castigado");
        if (Werewolf.grauDe(quem) != 0) helper.fail("e não devia dar grau a quem não foi mordido");
        if (!largou(helper, onde).isEmpty()) helper.fail("nem largar coisa nenhuma");
        limpaOChão(helper, onde);
        helper.succeed();
    }

    /**
     * <b>O ouro compra sempre, do segundo grau em diante</b> — e é por isso que um lobisomem de grau dois que
     * chegue com ouro na mão nunca passa do grau dois.
     *
     * <p>É uma falha do original, e está aqui de propósito: a estátua olha o ouro <b>antes</b> de olhar o
     * degrau.
     */
    @GameTest(maxTicks = 40)
    public void goldAlwaysBuysACharmAndNeverTheRung(GameTestHelper helper) {
        piso(helper);
        BlockPos onde = new BlockPos(2, 2, 2);
        Player quem = helper.makeMockServerPlayerInLevel();
        Werewolf.grau(quem, 2);

        ItemStack ouro = new ItemStack(Items.GOLD_INGOT, 5);
        WerewolfLadder.fala(helper.getLevel(), quem, ouro, helper.absolutePos(onde), Direction.NORTH);

        if (ouro.getCount() != 2) helper.fail("ela devia ter comido três barras, e sobraram " + ouro.getCount());
        if (Werewolf.grauDe(quem) != 2) helper.fail("o ouro compra o amuleto e não o degrau");
        boolean amuleto = largou(helper, onde).stream()
                .anyMatch(c -> c.getItem().is(OccultaItems.MOON_CHARM));
        if (!amuleto) helper.fail("e o amuleto devia estar no chão");
        limpaOChão(helper, onde);
        helper.succeed();
    }

    /** Com a mão errada ela só diz o que quer, e não leva nada. */
    @GameTest(maxTicks = 40)
    public void theWrongHandCostsNothing(GameTestHelper helper) {
        piso(helper);
        BlockPos onde = new BlockPos(2, 2, 2);
        Player quem = helper.makeMockServerPlayerInLevel();
        Werewolf.grau(quem, 2);

        ItemStack pedra = new ItemStack(Items.COBBLESTONE, 64);
        WerewolfLadder.fala(helper.getLevel(), quem, pedra, helper.absolutePos(onde), Direction.NORTH);
        if (pedra.getCount() != 64) helper.fail("ela não come o que não pediu");
        if (Werewolf.grauDe(quem) != 2) helper.fail("e não dá degrau de graça");

        // e a mão a meio também não: ela diz quanto falta
        ItemStack pouco = new ItemStack(Items.MUTTON, 10);
        WerewolfLadder.fala(helper.getLevel(), quem, pouco, helper.absolutePos(onde), Direction.NORTH);
        if (pouco.getCount() != 10) helper.fail("dez carnes não são trinta");
        if (Werewolf.grauDe(quem) != 2) helper.fail("e dez carnes não sobem degrau");
        limpaOChão(helper, onde);
        helper.succeed();
    }

    /** Mudar de grau <b>apaga o degrau</b>: cada um começa do zero. */
    @GameTest(maxTicks = 40)
    public void climbingWipesTheRung(GameTestHelper helper) {
        Player quem = helper.makeMockServerPlayerInLevel();
        Werewolf.grau(quem, 6);
        WerewolfQuest.estado(quem, WerewolfQuest.Estado.COMEÇADO);
        WerewolfQuest.conta(quem);
        WerewolfQuest.conta(quem);
        WerewolfQuest.guardaLugar(quem, 7, 7);

        if (WerewolfQuest.contaDe(quem) != 2) helper.fail("dois feitos contados");
        Werewolf.grau(quem, 7);
        if (WerewolfQuest.contaDe(quem) != 0) helper.fail("o degrau novo começa do zero");
        if (WerewolfQuest.estadoDe(quem) != WerewolfQuest.Estado.NENHUM) {
            helper.fail("e o pedido novo ainda não foi feito");
        }
        if (!WerewolfQuest.guardaLugar(quem, 7, 7)) {
            helper.fail("e os lugares onde ele uivou esquecem-se");
        }
        helper.succeed();
    }

    /** <b>Um uivo num lugar onde ele já uivou não conta</b>, e é isso que obriga a andar. */
    @GameTest(maxTicks = 40)
    public void aPlaceOnlyCountsOnce(GameTestHelper helper) {
        Player quem = helper.makeMockServerPlayerInLevel();
        Werewolf.grau(quem, 6);
        WerewolfQuest.estado(quem, WerewolfQuest.Estado.COMEÇADO);

        if (!WerewolfQuest.guardaLugar(quem, 3, 4)) helper.fail("o primeiro uivo conta");
        if (WerewolfQuest.guardaLugar(quem, 3, 4)) helper.fail("o segundo no mesmo lugar não");
        if (!WerewolfQuest.guardaLugar(quem, 3, 5)) helper.fail("mas um pedaço ao lado sim");
        if (!WerewolfQuest.guardaLugar(quem, -3, 4)) helper.fail("e um pedaço negativo também");
        if (WerewolfQuest.guardaLugar(quem, -3, 4)) helper.fail("e esse também só conta uma vez");
        helper.succeed();
    }

    /**
     * <b>O Caçador Cornudo é uma coisa dura</b>: quatrocentos de vida e nenhuma pancada maior do que quinze.
     */
    @GameTest(maxTicks = 60)
    public void theHuntsmanIsAHardThing(GameTestHelper helper) {
        piso(helper);
        var caçador = helper.spawn(net.thaumcraft.occulta.OccultaEntities.HORNED_HUNTSMAN,
                new BlockPos(3, 2, 3));

        if (caçador.getMaxHealth() != HornedHuntsmanEntity.VIDA) helper.fail("quatrocentos de vida");
        if (!caçador.fireImmune()) helper.fail("e ele não arde");

        float antes = caçador.getHealth();
        caçador.hurtServer(helper.getLevel(),
                helper.getLevel().damageSources().magic(), 1000.0f);
        float tirou = antes - caçador.getHealth();
        if (tirou > HornedHuntsmanEntity.TETO_DA_PANCADA + 0.01f) {
            helper.fail("nenhuma pancada lhe tira mais de quinze, e esta tirou " + tirou);
        }

        caçador.discard();
        helper.succeed();
    }

    /**
     * <b>A espera da entrada</b>: ele acende com um quarto da vida e sara até aos quatrocentos.
     *
     * <p>É o tempo que o jogador tem de correr, e é a única coisa para que ele serve.
     */
    @GameTest(maxTicks = 60)
    public void theWaitBeforeHeComes(GameTestHelper helper) {
        piso(helper);
        var caçador = helper.spawn(net.thaumcraft.occulta.OccultaEntities.HORNED_HUNTSMAN,
                new BlockPos(3, 2, 3));
        caçador.acendeAEspera();

        if (caçador.esperando() != HornedHuntsmanEntity.ESPERA) helper.fail("cento e cinquenta de espera");
        float quarto = (float) HornedHuntsmanEntity.VIDA / 4.0f;
        if (Math.abs(caçador.getHealth() - quarto) > 0.01f) {
            helper.fail("ele acende com um quarto da vida, e está com " + caçador.getHealth());
        }
        if (HornedHuntsmanEntity.ESPERA / HornedHuntsmanEntity.SARA_ESPERANDO_DE
                * HornedHuntsmanEntity.SARA_ESPERANDO < HornedHuntsmanEntity.VIDA - quarto) {
            helper.fail("e a espera devia dar-lhe tempo de chegar aos quatrocentos");
        }

        caçador.discard();
        helper.succeed();
    }

    /**
     * <b>Matar o Caçador cumpre o pedido</b>, e é o quarto degrau.
     */
    @GameTest(maxTicks = 60)
    public void killingTheHuntsmanFinishesTheFourthRung(GameTestHelper helper) {
        piso(helper);
        BlockPos onde = new BlockPos(2, 2, 2);
        Player quem = helper.makeMockServerPlayerInLevel();
        Werewolf.grau(quem, 4);

        // a estátua dá-lhe o chifre e põe o pedido a correr
        WerewolfLadder.fala(helper.getLevel(), quem, ItemStack.EMPTY, helper.absolutePos(onde),
                Direction.NORTH);
        if (WerewolfQuest.estadoDe(quem) != WerewolfQuest.Estado.COMEÇADO) {
            helper.fail("o pedido devia estar em curso");
        }
        boolean chifre = largou(helper, onde).stream()
                .anyMatch(c -> c.getItem().is(OccultaItems.HORN_OF_THE_HUNT));
        if (!chifre) helper.fail("e o Chifre da Caça devia estar no chão");
        limpaOChão(helper, onde);

        // falar outra vez não dá outro chifre
        WerewolfLadder.fala(helper.getLevel(), quem, ItemStack.EMPTY, helper.absolutePos(onde),
                Direction.NORTH);
        if (!largou(helper, onde).isEmpty()) helper.fail("um chifre só, e é de propósito");
        if (Werewolf.grauDe(quem) != 4) helper.fail("e sem o Caçador morto não há degrau");

        // e com ele morto, o degrau vem
        WerewolfLadder.caçadorMorto(quem);
        if (WerewolfQuest.estadoDe(quem) != WerewolfQuest.Estado.PRONTO) {
            helper.fail("o Caçador morto cumpre o pedido");
        }
        WerewolfLadder.fala(helper.getLevel(), quem, ItemStack.EMPTY, helper.absolutePos(onde),
                Direction.NORTH);
        if (Werewolf.grauDe(quem) != 5) helper.fail("e então ela dá o quinto grau");
        limpaOChão(helper, onde);
        helper.succeed();
    }

    /**
     * <b>A escada inteira, do grau um ao dez.</b>
     *
     * <p>É a prova que carrega a fatia. Em cada degrau se faz o que aquele degrau pede — as três mãos cheias,
     * o Caçador morto, e os cinco feitos contados — e no fim o grau é <b>dez</b>. Qualquer degrau quebrado
     * para aqui.
     */
    @GameTest(maxTicks = 100)
    public void theWholeLadderHasNoBrokenRung(GameTestHelper helper) {
        piso(helper);
        BlockPos onde = new BlockPos(2, 2, 2);
        BlockPos fora = helper.absolutePos(onde);
        ServerLevel level = helper.getLevel();
        Player quem = helper.makeMockServerPlayerInLevel();
        Werewolf.grau(quem, 1);

        // o primeiro: três barras de ouro, que também lhe dão o Amuleto da Lua
        sobe(helper, quem, fora, new ItemStack(Items.GOLD_INGOT, WerewolfLadder.OURO), 2);
        boolean amuleto = largou(helper, onde).stream()
                .anyMatch(c -> c.getItem().is(OccultaItems.MOON_CHARM));
        if (!amuleto) helper.fail("o primeiro degrau devia dar o Amuleto da Lua");
        limpaOChão(helper, onde);

        // o segundo e o terceiro: as duas mãos cheias
        sobe(helper, quem, fora, new ItemStack(Items.MUTTON, WerewolfLadder.CARNEIRO), 3);
        sobe(helper, quem, fora, new ItemStack(OccultaItems.DOG_TONGUE, WerewolfLadder.LÍNGUAS), 4);

        // o quarto: o chifre, e o Caçador morto
        WerewolfLadder.fala(level, quem, ItemStack.EMPTY, fora, Direction.NORTH);
        WerewolfLadder.caçadorMorto(quem);
        WerewolfLadder.fala(level, quem, ItemStack.EMPTY, fora, Direction.NORTH);
        if (Werewolf.grauDe(quem) != 5) helper.fail("o quarto degrau não passou");
        limpaOChão(helper, onde);

        // e os cinco de feito: cada um conta o que pede e sobe
        for (int grau = 5; grau <= 9; grau++) {
            WerewolfLadder.fala(level, quem, ItemStack.EMPTY, fora, Direction.NORTH);
            if (WerewolfQuest.estadoDe(quem) != WerewolfQuest.Estado.COMEÇADO) {
                helper.fail("o pedido do grau " + grau + " devia ter começado");
            }
            for (int n = 0; n < WerewolfLadder.precisaDe(grau); n++) {
                WerewolfLadder.conta(quem, grau);
            }
            WerewolfLadder.fala(level, quem, ItemStack.EMPTY, fora, Direction.NORTH);
            if (Werewolf.grauDe(quem) != grau + 1) {
                helper.fail("o degrau " + grau + " não passou: ficou no " + Werewolf.grauDe(quem));
            }
        }

        if (Werewolf.grauDe(quem) != Werewolf.TETO) helper.fail("e no fim ele é do décimo grau");
        // e no décimo ela não pede mais nada
        WerewolfLadder.fala(level, quem, ItemStack.EMPTY, fora, Direction.NORTH);
        if (Werewolf.grauDe(quem) != Werewolf.TETO) helper.fail("e dez é o fim da escada");
        limpaOChão(helper, onde);
        helper.succeed();
    }

    /** Um degrau que se paga com a mão cheia: ela come o que pediu e dá o grau. */
    private static void sobe(GameTestHelper helper, Player quem, BlockPos fora, ItemStack mão, int vai) {
        int tinha = mão.getCount();
        WerewolfLadder.fala(helper.getLevel(), quem, mão, fora, Direction.NORTH);
        if (!mão.isEmpty() && mão.getCount() == tinha) helper.fail("ela devia ter comido a mão cheia");
        if (Werewolf.grauDe(quem) != vai) {
            helper.fail("devia ter subido ao grau " + vai + " e está no " + Werewolf.grauDe(quem));
        }
    }

    /**
     * <b>Os feitos só contam no degrau deles.</b>
     *
     * <p>Matar trinta porcos-zumbis no grau cinco não vale nada, e é o que faz de cada degrau um jeito
     * diferente de jogar.
     */
    @GameTest(maxTicks = 40)
    public void aDeedOnlyCountsOnItsOwnRung(GameTestHelper helper) {
        Player quem = helper.makeMockServerPlayerInLevel();
        Werewolf.grau(quem, 5);
        WerewolfQuest.estado(quem, WerewolfQuest.Estado.COMEÇADO);

        WerewolfLadder.conta(quem, 8);
        if (WerewolfQuest.contaDe(quem) != 0) helper.fail("o feito do oitavo não conta no quinto");
        WerewolfLadder.conta(quem, 5);
        if (WerewolfQuest.contaDe(quem) != 1) helper.fail("e o do quinto conta");

        // e nem contam antes de a estátua mandar
        Werewolf.grau(quem, 6);
        WerewolfLadder.conta(quem, 6);
        if (WerewolfQuest.contaDe(quem) != 0) helper.fail("sem o pedido a correr, nada conta");
        helper.succeed();
    }

    /**
     * <b>O uivo de um lobo de grau alto chama cães</b>, e eles vêm a morrer.
     */
    @GameTest(maxTicks = 60)
    public void theHowlOfAnAlphaCallsDogs(GameTestHelper helper) {
        piso(helper);
        Player quem = helper.makeMockServerPlayerInLevel();
        BlockPos pé = helper.absolutePos(new BlockPos(3, 2, 3));
        quem.snapTo(pé.getX() + 0.5, pé.getY(), pé.getZ() + 0.5, 0.0f, 0.0f);
        Werewolf.grau(quem, 10);
        Werewolf.forma(quem, Werewolf.Forma.LOBO);

        int antes = helper.getLevel().getEntitiesOfClass(
                net.minecraft.world.entity.animal.wolf.Wolf.class,
                quem.getBoundingBox().inflate(12.0)).size();
        WerewolfPowers.uiva(helper.getLevel(), quem);
        var cães = helper.getLevel().getEntitiesOfClass(
                net.minecraft.world.entity.animal.wolf.Wolf.class,
                quem.getBoundingBox().inflate(12.0));

        if (cães.size() <= antes) helper.fail("o uivo de um lobo de grau dez devia chamar cães");
        for (var cão : cães) {
            if (!cão.hasEffect(net.thaumcraft.occulta.OccultaEffects.MORTAL_COIL)) {
                helper.fail("e eles vêm com a Morte Certa: eles vêm para morrer");
            }
            if (!net.thaumcraft.occulta.NoDrops.marcado(cão)) helper.fail("e deles não cai nada");
            cão.discard();
        }

        // e o uivo seguinte, dentro do minuto, sai gago
        Werewolf.grau(quem, 0);
        helper.succeed();
    }

    /** E <b>o uivo de um lobisomem prende</b> quem não é lobisomem nem vampiro. */
    @GameTest(maxTicks = 60)
    public void theHowlOfAWolfmanHoldsWhatItFinds(GameTestHelper helper) {
        piso(helper);
        Player quem = helper.makeMockServerPlayerInLevel();
        BlockPos pé = helper.absolutePos(new BlockPos(3, 2, 3));
        quem.snapTo(pé.getX() + 0.5, pé.getY(), pé.getZ() + 0.5, 0.0f, 0.0f);
        Werewolf.grau(quem, 10);
        Werewolf.forma(quem, Werewolf.Forma.LOBISOMEM);

        var vaca = helper.spawn(EntityTypes.COW, new BlockPos(4, 2, 4));
        WerewolfPowers.uiva(helper.getLevel(), quem);

        if (!vaca.hasEffect(net.thaumcraft.occulta.OccultaEffects.PARALYSIS)) {
            helper.fail("o uivo de um lobisomem devia tê-la prendido");
        }
        var presa = vaca.getEffect(net.thaumcraft.occulta.OccultaEffects.PARALYSIS);
        if (presa == null || presa.getAmplifier() != WerewolfPowers.PARALISIA_GRAU) {
            helper.fail("e a paralisia dele é de grau três");
        }

        vaca.discard();
        Werewolf.grau(quem, 0);
        helper.succeed();
    }

    /**
     * <b>Um bicho com arma na mão bate menos do que um bicho sem nada.</b>
     *
     * <p>É o {@code updateChargeDamage} do original, e é o que faz da forma de lobo uma coisa que se joga com
     * as mãos vazias.
     */
    @GameTest(maxTicks = 40)
    public void aBeastWithAWeaponHitsForTwo(GameTestHelper helper) {
        Player quem = helper.makeMockServerPlayerInLevel();
        Werewolf.grau(quem, 10);
        Werewolf.forma(quem, Werewolf.Forma.LOBO);

        quem.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                new ItemStack(Items.DIAMOND_SWORD));
        float comEspada = WerewolfPowers.pancada(quem, 9.0f);
        if (comEspada != WerewolfPowers.ARMA_NA_MÃO_VALE) {
            helper.fail("com arma na mão a pancada dele vale dois, e valeu " + comEspada);
        }

        quem.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        float paradoSemNada = WerewolfPowers.pancada(quem, 1.0f);
        if (paradoSemNada != 1.0f) helper.fail("parado, ele bate o que bateria");

        quem.setSprinting(true);
        float correndo = WerewolfPowers.pancada(quem, 1.0f);
        if (correndo <= 1.0f) helper.fail("e correndo ele soma o dano do grau");

        // e de gente nada disto vale
        quem.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                new ItemStack(Items.DIAMOND_SWORD));
        Werewolf.grau(quem, 0);
        if (WerewolfPowers.pancada(quem, 9.0f) != 9.0f) helper.fail("de gente a espada vale o que vale");
        helper.succeed();
    }

    /** O Altar assenta, olha para quem o assentou, e a Cabeça de Lobo também se assenta. */
    @GameTest(maxTicks = 40)
    public void theAltarAndTheHeadSitWhereTheyArePut(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos altar = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(altar, OccultaBlocks.WEREWOLF_STATUE.defaultBlockState());
        if (level.getBlockEntity(altar) == null) helper.fail("o Altar precisa da alma dele");

        BlockPos cabeça = helper.absolutePos(new BlockPos(4, 2, 2));
        level.setBlockAndUpdate(cabeça, OccultaBlocks.WOLF_HEAD.defaultBlockState());
        if (level.getBlockEntity(cabeça) == null) helper.fail("e a Cabeça também");

        BlockPos parede = helper.absolutePos(new BlockPos(5, 2, 2));
        level.setBlockAndUpdate(parede, OccultaBlocks.WOLF_HEAD_WALL.defaultBlockState()
                .setValue(net.thaumcraft.occulta.wolf.WolfHeadWallBlock.FACING, Direction.SOUTH));
        if (level.getBlockEntity(parede) == null) helper.fail("e a da parede também");
        if (net.thaumcraft.occulta.wolf.WolfHeadBlock.parede(level.getBlockState(parede))
                != Direction.SOUTH) {
            helper.fail("a da parede sabe de que parede sai");
        }
        if (net.thaumcraft.occulta.wolf.WolfHeadBlock.parede(level.getBlockState(cabeça)) != null) {
            helper.fail("e a do chão não sai de parede nenhuma");
        }

        level.setBlockAndUpdate(altar, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(cabeça, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(parede, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }
}
