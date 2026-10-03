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
import net.thaumcraft.occulta.goblin.GoblinDigGoal;
import net.thaumcraft.occulta.goblin.GoblinDropOffGoal;
import net.thaumcraft.occulta.goblin.GoblinEntity;

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
