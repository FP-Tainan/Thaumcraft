package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.goblin.GoblinDigGoal;
import net.thaumcraft.occulta.goblin.GoblinDropOffGoal;
import net.thaumcraft.occulta.goblin.GoblinEntity;
import net.thaumcraft.occulta.goblin.GoblinTrades;

/**
 * O goblin: a conta da coragem, e o que ele faz na corda.
 *
 * <p>A prova que carrega a fatia é a da <b>coragem</b>. Um goblin sozinho foge de tudo; três caçam aldeão. É
 * a mesma conta vista dos dois lados, e é tudo o que decide o que um goblin é.
 */
public class OccultaGoblinGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 10; x++) {
            for (int z = 0; z < 10; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 2, z)),
                        Blocks.AIR.defaultBlockState());
            }
        }
    }

    /** Os números dele são os do original. */
    @GameTest(maxTicks = 20)
    public void hisNumbersAreTheOriginals(GameTestHelper helper) {
        if (GoblinEntity.CORAGEM != 3) helper.fail("são precisos três para eles terem coragem");
        if (GoblinEntity.PERTO != 8.0) helper.fail("e contam-se a oito blocos");
        if (GoblinEntity.OFÍCIOS != 4) helper.fail("e há quatro ofícios");
        if (GoblinDigGoal.DEMORA != 60) helper.fail("cada bloco leva três segundos");
        if (GoblinDigGoal.DESISTE_AOS != 15) helper.fail("e às quinze olhadas falhadas ele olha para baixo");
        if (GoblinDropOffGoal.BAÚ_GRANDE != 27) helper.fail("e só um baú grande lhe serve");
        helper.succeed();
    }

    /** <b>Sozinho ele foge; em três, tem coragem.</b> */
    @GameTest(maxTicks = 40)
    public void aloneHeFleesInThreesHeDoesNot(GameTestHelper helper) {
        piso(helper);
        var um = helper.spawn(OccultaEntities.GOBLIN, new BlockPos(3, 2, 3));
        if (um.temCoragem()) helper.fail("um goblin sozinho não tem coragem nenhuma");

        var dois = helper.spawn(OccultaEntities.GOBLIN, new BlockPos(4, 2, 3));
        if (um.temCoragem()) helper.fail("nem dois");

        var três = helper.spawn(OccultaEntities.GOBLIN, new BlockPos(5, 2, 3));
        if (!um.temCoragem()) helper.fail("três, sim");
        if (!três.temCoragem()) helper.fail("e a conta é a mesma para todos eles");

        // e longe não conta
        var longe = helper.spawn(OccultaEntities.GOBLIN, new BlockPos(9, 2, 9));
        dois.discard();
        três.discard();
        if (um.temCoragem()) helper.fail("e um que esteja longe demais não conta");

        um.discard();
        longe.discard();
        helper.succeed();
    }

    /** <b>Na corda, uma picareta que se lhe dê fica com ele.</b> */
    @GameTest(maxTicks = 40)
    public void onALeadHeTakesThePickaxe(GameTestHelper helper) {
        piso(helper);
        var goblin = helper.spawn(OccultaEntities.GOBLIN, new BlockPos(3, 2, 3));
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);

        ItemStack picareta = new ItemStack(Items.IRON_PICKAXE);
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, picareta);

        // solto, ele não a quer
        goblin.mobInteract(quem, net.minecraft.world.InteractionHand.MAIN_HAND);
        if (!goblin.getMainHandItem().isEmpty()) helper.fail("solto, ele não pega em nada");

        goblin.setLeashedTo(quem, true);
        goblin.mobInteract(quem, net.minecraft.world.InteractionHand.MAIN_HAND);
        if (!goblin.getMainHandItem().is(Items.IRON_PICKAXE)) {
            helper.fail("na corda, a picareta fica com ele");
        }

        // e clicar outra vez devolve-a
        goblin.mobInteract(quem, net.minecraft.world.InteractionHand.MAIN_HAND);
        if (!goblin.getMainHandItem().isEmpty()) helper.fail("e clicar outra vez tira-lha");

        goblin.dropLeash();
        goblin.discard();
        helper.succeed();
    }

    /** <b>E ele cava pedra, mas não qualquer coisa.</b> */
    @GameTest(maxTicks = 40)
    public void heDigsStoneButNotEverything(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos pedra = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(pedra, Blocks.STONE.defaultBlockState());
        if (!GoblinDigGoal.cavável(level, pedra)) helper.fail("pedra, sim");

        BlockPos terra = helper.absolutePos(new BlockPos(4, 2, 3));
        level.setBlockAndUpdate(terra, Blocks.DIRT.defaultBlockState());
        if (!GoblinDigGoal.cavável(level, terra)) helper.fail("terra também");

        BlockPos areia = helper.absolutePos(new BlockPos(5, 2, 3));
        level.setBlockAndUpdate(areia, Blocks.SAND.defaultBlockState());
        if (!GoblinDigGoal.cavável(level, areia)) helper.fail("e areia");

        BlockPos tronco = helper.absolutePos(new BlockPos(6, 2, 3));
        level.setBlockAndUpdate(tronco, Blocks.OAK_LOG.defaultBlockState());
        if (GoblinDigGoal.cavável(level, tronco)) helper.fail("mas madeira não");

        BlockPos rocha = helper.absolutePos(new BlockPos(7, 2, 3));
        level.setBlockAndUpdate(rocha, Blocks.BEDROCK.defaultBlockState());
        if (GoblinDigGoal.cavável(level, rocha)) helper.fail("e a rocha-mãe muito menos");

        BlockPos ar = helper.absolutePos(new BlockPos(8, 2, 3));
        if (GoblinDigGoal.cavável(level, ar)) helper.fail("nem o ar");

        // e cavar tira mesmo o bloco
        var goblin = helper.spawn(OccultaEntities.GOBLIN, new BlockPos(1, 2, 1));
        if (!GoblinDigGoal.cava(level, pedra, goblin)) helper.fail("e cavar tira o bloco");
        if (!level.getBlockState(pedra).isAir()) helper.fail("e o lugar fica vazio");

        goblin.discard();
        helper.succeed();
    }

    /** <b>Ele apanha o que está no chão</b> — mas só na corda e de mãos vazias. */
    @GameTest(maxTicks = 60)
    public void heOnlyPicksUpOnALeadAndEmptyHanded(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var goblin = helper.spawn(OccultaEntities.GOBLIN, new BlockPos(3, 2, 3));
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);

        BlockPos onde = helper.absolutePos(new BlockPos(4, 2, 3));
        var largado = new ItemEntity(level, onde.getX() + 0.5, onde.getY() + 0.1, onde.getZ() + 0.5,
                new ItemStack(Items.COBBLESTONE, 8));
        largado.setNoGravity(true);
        level.addFreshEntity(largado);

        var apanhar = new net.thaumcraft.occulta.goblin.GoblinPickUpGoal(goblin,
                net.thaumcraft.occulta.goblin.GoblinPickUpGoal.ALCANCE);

        // solto, ele não apanha
        if (apanhar.canUse()) helper.fail("solto, ele não apanha nada");

        goblin.setLeashedTo(quem, true);
        if (!apanhar.canUse()) helper.fail("na corda, sim");

        // com a mão cheia, também não
        goblin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
        if (apanhar.canUse()) helper.fail("mas de mãos cheias não");

        goblin.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        apanhar.start();
        for (int volta = 0; volta < 20 && goblin.getMainHandItem().isEmpty(); volta++) {
            goblin.getNavigation().stop();
            apanhar.tick();
        }
        if (!goblin.getMainHandItem().is(Items.COBBLESTONE)) {
            helper.fail("e chegando lá, o que estava no chão vai para a mão");
        }

        goblin.dropLeash();
        goblin.discard();
        largado.discard();
        helper.succeed();
    }

    /** <b>E larga no baú o que não é ferramenta.</b> */
    @GameTest(maxTicks = 60)
    public void heDropsOffWhatIsNotATool(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos baú = helper.absolutePos(new BlockPos(5, 2, 5));
        level.setBlockAndUpdate(baú, Blocks.CHEST.defaultBlockState());
        if (!(level.getBlockEntity(baú) instanceof Container dentro)) {
            helper.fail("devia haver baú");
            return;
        }

        var goblin = helper.spawn(OccultaEntities.GOBLIN, new BlockPos(5, 2, 4));
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        goblin.setLeashedTo(quem, true);

        var largar = new GoblinDropOffGoal(goblin, GoblinDropOffGoal.ALCANCE);

        // com a picareta na mão ele não larga nada: a picareta é dele
        goblin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
        if (largar.canUse()) helper.fail("a picareta ele não larga");

        goblin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.COBBLESTONE, 12));
        // o original só se lembra uma vez em sessenta; aqui se insiste até ele se lembrar
        boolean lembrou = false;
        for (int volta = 0; volta < 400 && !lembrou; volta++) lembrou = largar.canUse();
        if (!lembrou) {
            helper.fail("em quatrocentas voltas ele devia ter-se lembrado do baú");
            goblin.dropLeash();
            goblin.discard();
            return;
        }

        for (int volta = 0; volta < 20 && !goblin.getMainHandItem().isEmpty(); volta++) largar.tick();
        if (!goblin.getMainHandItem().isEmpty()) helper.fail("e chegando lá, a mão esvazia");

        boolean achou = false;
        for (int casa = 0; casa < dentro.getContainerSize(); casa++) {
            if (dentro.getItem(casa).is(Items.COBBLESTONE)) achou = true;
        }
        if (!achou) helper.fail("e o que ele trazia fica no baú");

        goblin.dropLeash();
        goblin.discard();
        helper.succeed();
    }

    /** <b>E ele trepa paredes</b>, que é o que faz uma cerca não o segurar. */
    /**
     * <b>A escada do koboldite, que é o que faz do goblin um mercador.</b>
     *
     * <p>Esta é a prova que carrega a fatia do metal. O koboldite não se mina, não se cozinha e não se
     * invoca: sai de um goblin, e sai em <b>três degraus que ele abre um de cada vez</b>. Um goblin
     * acabado de encontrar mostra o primeiro e mais nada.
     */
    @GameTest
    public void theKobolditeLadderOpensOneStepAtATime(GameTestHelper helper) {
        var sorte = net.minecraft.util.RandomSource.create(1234L);
        var tem = new net.minecraft.world.item.trading.MerchantOffers();

        // o primeiro degrau: nove de pó e cinco pepitas de ouro dão uma pepita de koboldite
        GoblinTrades.monta(sorte, 1, tem, 1);
        if (tem.size() != 1) helper.fail("um goblin novo mostra uma troca; mostrou " + tem.size());
        var primeiro = tem.get(0);
        if (!primeiro.getCostA().is(OccultaItems.KOBOLDITE_DUST)
                || primeiro.getCostA().getCount() != GoblinTrades.PÓ_DO_PRIMEIRO) {
            helper.fail("o primeiro pede nove de pó; pede " + primeiro.getCostA());
        }
        if (!primeiro.getCostB().is(Items.GOLD_NUGGET)
                || primeiro.getCostB().getCount() != GoblinTrades.OURO_DO_PRIMEIRO) {
            helper.fail("e cinco pepitas de ouro; pede " + primeiro.getCostB());
        }
        if (!primeiro.getResult().is(OccultaItems.KOBOLDITE_NUGGET)
                || primeiro.getResult().getCount() != 1) {
            helper.fail("e dá uma pepita de koboldite; dá " + primeiro.getResult());
        }

        // o segundo: dezesseis de pó e um lingote de ouro dão duas
        GoblinTrades.monta(sorte, 1, tem, 1);
        var segundo = tem.get(1);
        if (!segundo.getCostA().is(OccultaItems.KOBOLDITE_DUST)
                || segundo.getCostA().getCount() != GoblinTrades.PÓ_DO_SEGUNDO
                || segundo.getResult().getCount() != GoblinTrades.DÁ_O_SEGUNDO) {
            helper.fail("o segundo pede dezesseis de pó e dá duas pepitas; é " + segundo.getCostA()
                    + " por " + segundo.getResult());
        }

        // e o terceiro: nove pepitas e uma esmeralda dão o lingote
        GoblinTrades.monta(sorte, 1, tem, 1);
        var terceiro = tem.get(2);
        if (!terceiro.getCostA().is(OccultaItems.KOBOLDITE_NUGGET)
                || terceiro.getCostA().getCount() != GoblinTrades.PEPITAS_DO_TERCEIRO
                || !terceiro.getCostB().is(Items.EMERALD)
                || !terceiro.getResult().is(OccultaItems.KOBOLDITE_INGOT)) {
            helper.fail("o terceiro dá o lingote; dá " + terceiro.getResult());
        }

        // e a escada acaba: o quarto pedido não traz nada de novo
        GoblinTrades.monta(sorte, 1, tem, 1);
        if (tem.size() != 4 || tem.get(3).getResult().is(OccultaItems.KOBOLDITE_INGOT)) {
            helper.fail("depois do lingote não há mais degraus; veio " + tem.get(3).getResult());
        }
        helper.succeed();
    }

    /**
     * <b>E o pó, que é o primeiro degrau de todos, não se compra: cai no lugar da esmeralda.</b>
     *
     * <p>Uma vez em três, vender comida ou minério a um goblin paga em <b>pó de koboldite</b>. É a única
     * porta de entrada do metal no jogo, e é por isso que ele é o fim do mod e não o meio.
     */
    @GameTest
    public void theDustFallsInsteadOfTheEmerald(GameTestHelper helper) {
        var sorte = net.minecraft.util.RandomSource.create(99L);
        int comPó = 0;
        int comEsmeralda = 0;
        for (int volta = 0; volta < 200; volta++) {
            var tem = new net.minecraft.world.item.trading.MerchantOffers();
            GoblinTrades.monta(sorte, 0, tem, 1);
            for (var troca : tem) {
                if (troca.getResult().is(OccultaItems.KOBOLDITE_DUST)) comPó++;
                else if (troca.getResult().is(Items.EMERALD)) comEsmeralda++;
            }
        }
        if (comPó == 0) helper.fail("em duzentos goblins, algum devia pagar em pó");
        if (comEsmeralda == 0) helper.fail("e algum em esmeralda");
        if (comPó > comEsmeralda) {
            helper.fail("mas o pó é uma em três, e por isso o raro; veio " + comPó + " contra "
                    + comEsmeralda);
        }
        helper.succeed();
    }

    /**
     * <b>Um goblin no mato não vende nada.</b>
     *
     * <p>Ele só regateia onde mora, e é por isso que encontrar uma aldeia com goblins é o começo da linha
     * do koboldite. A arena da prova não é aldeia nenhuma.
     */
    @GameTest(maxTicks = 40)
    public void aGoblinInTheWildDoesNotTrade(GameTestHelper helper) {
        piso(helper);
        var goblin = helper.spawn(OccultaEntities.GOBLIN, new BlockPos(3, 2, 3));
        if (goblin.emAldeia()) helper.fail("a arena não é aldeia nenhuma");
        if (goblin.regateando()) helper.fail("e ninguém está regateando com ele");
        if (goblin.getVillagerXp() != 0) helper.fail("e com ele não se sobe de nível");
        if (goblin.showProgressBar()) helper.fail("nem há barra de progresso");
        goblin.discard();
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void heClimbsWalls(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var goblin = helper.spawn(OccultaEntities.GOBLIN, new BlockPos(3, 2, 3));
        if (goblin.onClimbable()) helper.fail("no meio do chão ele não trepa nada");

        // encostado a uma parede, sim
        goblin.horizontalCollision = true;
        goblin.olhaAParede();
        if (!goblin.onClimbable()) helper.fail("encostado a uma parede, ele trepa");

        goblin.horizontalCollision = false;
        goblin.olhaAParede();
        if (goblin.onClimbable()) helper.fail("e saindo dela, para");

        goblin.discard();
        helper.succeed();
    }
}
