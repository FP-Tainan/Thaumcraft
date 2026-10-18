package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.MutandisItem;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.TaglockItem;
import net.thaumcraft.occulta.familiar.WingedMonkeyEntity;
import net.thaumcraft.occulta.waystone.Waystones;

/**
 * O <b>Macaco de Asas</b>: o que ele é, o que o doma e para onde ele leva quem lhe pede.
 */
public class OccultaMonkeyGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /**
     * <b>Os números dele</b>, e o murro que não é o que a folha diz.
     *
     * <p>Vinte de vida, três décimos de passo, <b>quatro</b> na folha de murro e <b>cinco</b> de armadura
     * por cima. E depois o murro tira <b>dois</b>, porque o original escreve o número à mão e nunca olha
     * para a folha. A prova mede os dois lados, que é o que torna o engano visível.
     */
    @GameTest(maxTicks = 40)
    public void theMonkeysNumbersAndThePunchThatIgnoresThem(GameTestHelper helper) {
        piso(helper);
        var macaco = helper.spawn(OccultaEntities.WINGED_MONKEY, new BlockPos(2, 2, 2));
        macaco.setNoAi(true);

        if (macaco.getMaxHealth() != WingedMonkeyEntity.VIDA) helper.fail("vinte de vida");
        if (macaco.getAttributeValue(Attributes.MOVEMENT_SPEED) != WingedMonkeyEntity.VELOCIDADE) {
            helper.fail("três décimos de passo");
        }
        if (macaco.getAttributeValue(Attributes.ATTACK_DAMAGE) != WingedMonkeyEntity.MURRO) {
            helper.fail("quatro na folha de murro");
        }
        if (macaco.getAttributeValue(Attributes.ARMOR) != WingedMonkeyEntity.COURAÇA) {
            helper.fail("cinco de armadura");
        }

        // e o murro, que não lê nada disso
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 2));
        porco.setNoAi(true);
        porco.setInvulnerable(false);
        float antes = porco.getHealth();
        macaco.doHurtTarget(helper.getLevel(), porco);
        float tirou = antes - porco.getHealth();
        if (Math.abs(tirou - WingedMonkeyEntity.MURRO_A_SECO) > 0.01f) {
            helper.fail("o murro dele tira dois, tirou " + tirou);
        }
        if (macaco.braço() != WingedMonkeyEntity.BRAÇO) helper.fail("e levanta o braço por dez batidas");

        porco.discard();
        macaco.discard();
        helper.succeed();
    }

    /**
     * <b>Carne crua doma-o</b>, uma vez em três — e domado ele ganha uma pele de três, senta-se e passa a
     * ser de alguém.
     *
     * <p>A prova oferece carne até pegar, porque a sorte é do original e não se finge: o que se mede é que
     * ela <b>pega</b>, que ela <b>gasta a carne</b> e que o que fica do outro lado é um macaco domado com
     * pele entre um e três.
     */
    @GameTest(maxTicks = 60)
    public void rawMeatTamesItAndGivesItASkin(GameTestHelper helper) {
        piso(helper);
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));
        var macaco = helper.spawn(OccultaEntities.WINGED_MONKEY, new BlockPos(2, 2, 3));
        macaco.setNoAi(true);

        if (macaco.pele() != 0) helper.fail("por domar, ele não tem pele de domado");

        for (int tenta = 0; tenta < 60 && !macaco.isTame(); tenta++) {
            quem.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.PORKCHOP, 1));
            macaco.mobInteract(quem, InteractionHand.MAIN_HAND);
            if (!quem.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                helper.fail("cada oferta gasta a carne");
            }
        }
        if (!macaco.isTame()) helper.fail("sessenta ofertas e nenhuma pegou");
        if (!macaco.isOwnedBy(quem)) helper.fail("domado, ele é de quem o domou");
        if (!macaco.isOrderedToSit()) helper.fail("e senta-se");
        if (macaco.pele() < 1 || macaco.pele() > WingedMonkeyEntity.PELES) {
            helper.fail("a pele de domado é uma de três, é " + macaco.pele());
        }

        macaco.discard();
        helper.succeed();
    }

    /**
     * <b>Tinta na mão do dono</b> pinta-lhe as asas; na mão de um estranho não faz nada.
     *
     * <p>E a cor de nascença está sempre entre as dezesseis, que é o sorteio do original.
     */
    @GameTest(maxTicks = 40)
    public void dyePaintsTheWingsOnlyForTheOwner(GameTestHelper helper) {
        piso(helper);
        var dono = helper.makeMockPlayer(GameType.SURVIVAL);
        dono.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));
        var estranho = helper.makeMockPlayer(GameType.SURVIVAL);
        estranho.snapTo(helper.absoluteVec(new Vec3(4.5, 2, 2.5)));

        var macaco = helper.spawn(OccultaEntities.WINGED_MONKEY, new BlockPos(2, 2, 3));
        macaco.setNoAi(true);
        if (macaco.cor() < 0 || macaco.cor() > 15) helper.fail("a cor de nascença é uma das dezesseis");
        macaco.tame(dono);

        estranho.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DYE.pick(net.minecraft.world.item.DyeColor.RED)));
        int antes = macaco.cor();
        macaco.mobInteract(estranho, InteractionHand.MAIN_HAND);
        if (macaco.cor() != antes) helper.fail("tinta de estranho não lhe toca");

        dono.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DYE.pick(net.minecraft.world.item.DyeColor.LIME)));
        macaco.mobInteract(dono, InteractionHand.MAIN_HAND);
        if (macaco.cor() != net.minecraft.world.item.DyeColor.LIME.getId()) {
            helper.fail("a tinta do dono pinta, ficou " + macaco.cor());
        }
        if (!dono.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) helper.fail("e gasta-se");

        macaco.discard();
        helper.succeed();
    }

    /**
     * <b>Uma Pedra de Caminho presa dá-lhe o rumo</b>, põe quem a deu às costas dele — e <b>prende-se de
     * volta onde ele está</b>, que é o que faz haver viagem de volta.
     */
    @GameTest(maxTicks = 40)
    public void aBoundWaystoneGivesItACourseAndPutsYouOnItsBack(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var dono = helper.makeMockPlayer(GameType.SURVIVAL);
        dono.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));

        var macaco = helper.spawn(OccultaEntities.WINGED_MONKEY, new BlockPos(2, 2, 3));
        macaco.setNoAi(true);
        macaco.tame(dono);
        if (macaco.temCasa()) helper.fail("sem pedra, ele não vai a lugar nenhum");

        BlockPos lá = helper.absolutePos(new BlockPos(6, 2, 6));
        ItemStack pedra = new ItemStack(OccultaItems.BOUND_WAYSTONE);
        pedra.set(OccultaComponents.WAYSTONE, new Waystones.Lugar(level.dimension(), lá));
        dono.setItemInHand(InteractionHand.MAIN_HAND, pedra);
        macaco.mobInteract(dono, InteractionHand.MAIN_HAND);

        if (!macaco.temCasa()) helper.fail("com a pedra, ele tem rumo");
        if (Math.abs(macaco.casaX() - (lá.getX() + 0.5)) > 0.01) helper.fail("e o rumo é o da pedra");
        if (Math.abs(macaco.casaZ() - (lá.getZ() + 0.5)) > 0.01) helper.fail("nos dois eixos");
        if (!dono.isPassenger() || dono.getVehicle() != macaco) helper.fail("e quem a deu vai às costas");
        if (macaco.isOrderedToSit()) helper.fail("um macaco que vai a algum lugar não fica sentado");

        var volta = Waystones.lugar(dono.getItemInHand(InteractionHand.MAIN_HAND));
        if (volta == null || !volta.onde().equals(macaco.blockPosition())) {
            helper.fail("a pedra prende-se de volta onde ele está");
        }

        dono.stopRiding();
        macaco.discard();
        helper.succeed();
    }

    /**
     * <b>E um frasco cheio leva-o a essa pessoa</b> — a onde ela está <b>agora</b>, e não a onde estava
     * quando o frasco se encheu. É por isso que o rumo dele é uma pilha guardada e não três números.
     */
    @GameTest(maxTicks = 40)
    public void aFilledTaglockMakesItFollowThatPersonAround(GameTestHelper helper) {
        piso(helper);
        var dono = helper.makeMockPlayer(GameType.SURVIVAL);
        dono.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));

        var macaco = helper.spawn(OccultaEntities.WINGED_MONKEY, new BlockPos(2, 2, 3));
        macaco.setNoAi(true);
        macaco.tame(dono);

        /* O frasco prende-se a um bicho do mundo, que é o que se consegue mexer e voltar a perguntar. */
        var quem = helper.spawn(EntityTypes.COW, new BlockPos(5, 2, 2));
        quem.setNoAi(true);
        ItemStack frasco = new ItemStack(OccultaItems.TAGLOCK);
        TaglockItem.bind(frasco, quem);
        dono.setItemInHand(InteractionHand.MAIN_HAND, frasco);
        macaco.mobInteract(dono, InteractionHand.MAIN_HAND);

        if (!macaco.temCasa()) helper.fail("com o frasco, ele tem rumo");
        if (!dono.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) helper.fail("e o frasco gasta-se");
        if (Math.abs(macaco.casaX() - quem.getX()) > 0.01) helper.fail("e o rumo é onde ele está");

        // e mexendo-se ele, o rumo mexe-se com ele
        quem.snapTo(helper.absoluteVec(new Vec3(6.5, 2, 6.5)));
        if (Math.abs(macaco.casaX() - quem.getX()) > 0.01) helper.fail("o rumo acompanha-o");
        if (Math.abs(macaco.casaZ() - quem.getZ()) > 0.01) helper.fail("nos dois eixos");
        quem.discard();

        macaco.esqueceACasa();
        if (macaco.temCasa()) helper.fail("esquecido o rumo, ele fica onde está");

        macaco.discard();
        helper.succeed();
    }

    /**
     * <b>O de empréstimo tem hora.</b> Ele some quando o prazo acaba <b>ou quando o alvo dele morre</b>, o
     * que vier primeiro — e enquanto for de empréstimo, mão nenhuma lhe pega.
     */
    @GameTest(maxTicks = 60)
    public void theLoanedOneGoesWhenItsTimeIsUp(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));

        var macaco = helper.spawn(OccultaEntities.WINGED_MONKEY, new BlockPos(2, 2, 3));
        macaco.setNoAi(true);
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(4, 2, 3));
        porco.setNoAi(true);
        macaco.setTarget(porco);
        macaco.prazo(2);

        if (!macaco.deEmpréstimo()) helper.fail("com prazo, ele é de empréstimo");
        quem.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.PORKCHOP));
        macaco.mobInteract(quem, InteractionHand.MAIN_HAND);
        if (macaco.isTame()) helper.fail("um macaco de empréstimo não se doma");

        if (macaco.oPrazo(level)) helper.fail("na primeira batida ele ainda está cá");
        if (!macaco.oPrazo(level)) helper.fail("na segunda, acabou-se-lhe o prazo");
        if (!macaco.isRemoved()) helper.fail("e ele some");

        // e o outro lado da mesma condição: morto o alvo, ele some logo
        var outro = helper.spawn(OccultaEntities.WINGED_MONKEY, new BlockPos(2, 2, 5));
        outro.setNoAi(true);
        outro.prazo(1000);
        if (!outro.oPrazo(level)) helper.fail("sem alvo vivo, ele some logo");

        porco.discard();
        helper.succeed();
    }

    /**
     * <b>A mutação que o faz</b>: teia, água por baixo, quatro vagens de cacau maduras, dois Apanha-Ervas
     * com Mutandis Extremis, um com a Pedra Sintonizada carregada, um com um dente-de-leão — <b>e uma
     * coruja e um lobo</b>, que é a única receita do mod que gasta duas vidas.
     */
    @GameTest(maxTicks = 80)
    public void theMonkeyMutationAsksForTwoLives(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos aqui = new BlockPos(4, 2, 4);
        BlockPos onde = helper.absolutePos(aqui);
        level.setBlockAndUpdate(onde.below(), Blocks.WATER.defaultBlockState());
        level.setBlockAndUpdate(onde, Blocks.COBWEB.defaultBlockState());

        /* O cacau precisa de um tronco de selva a que se agarrar, e ele fica do lado de fora. */
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(onde.relative(lado, 2), Blocks.JUNGLE_LOG.defaultBlockState());
            level.setBlockAndUpdate(onde.relative(lado), Blocks.COCOA.defaultBlockState()
                    .setValue(CocoaBlock.FACING, lado).setValue(CocoaBlock.AGE, 2));
        }
        BlockPos[] quinas = {onde.offset(1, 0, 1), onde.offset(1, 0, -1),
                onde.offset(-1, 0, 1), onde.offset(-1, 0, -1)};
        net.minecraft.world.item.Item[] boca = {OccultaItems.MUTANDIS_EXTREMIS,
                OccultaItems.MUTANDIS_EXTREMIS, OccultaItems.ATTUNED_STONE_CHARGED, Items.DANDELION};
        for (int i = 0; i < quinas.length; i++) {
            level.setBlockAndUpdate(quinas[i], OccultaBlocks.GRASSPER.defaultBlockState());
            if (level.getBlockEntity(quinas[i])
                    instanceof net.thaumcraft.occulta.GrassperBlockEntity planta) {
                planta.põe(new ItemStack(boca[i]));
            }
        }

        /* A guarda de sempre: uma peça que caiu calada faria a prova mentir sobre a receita. */
        if (MutandisItem.quantosCacaus(level, onde) != MutandisItem.CACAUS) {
            helper.fail("as quatro vagens ficam de pé");
        }
        if (MutandisItem.quantosSeguram(level, onde, OccultaItems.MUTANDIS_EXTREMIS) != 2) {
            helper.fail("e os dois Apanha-Ervas de Extremis também");
        }

        if (MutandisItem.éTeiaDeMacaco(level, onde)) helper.fail("sem bicho nenhum não é nada");
        var coruja = helper.spawn(OccultaEntities.OWL, aqui);
        if (MutandisItem.éTeiaDeMacaco(level, onde)) helper.fail("só com a coruja também não");
        var lobo = helper.spawn(EntityTypes.WOLF, aqui);
        if (!MutandisItem.éTeiaDeMacaco(level, onde)) helper.fail("com as duas, é");

        // e tirar o dente-de-leão desfaz tudo, que é o que diz que ele conta
        if (level.getBlockEntity(quinas[3])
                instanceof net.thaumcraft.occulta.GrassperBlockEntity planta) {
            planta.tira();
        }
        if (MutandisItem.éTeiaDeMacaco(level, onde)) helper.fail("sem o dente-de-leão não é");

        coruja.discard();
        lobo.discard();
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(onde.relative(lado), Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(onde.relative(lado, 2), Blocks.AIR.defaultBlockState());
        }
        for (BlockPos quina : quinas) level.setBlockAndUpdate(quina, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());
        helper.succeed();
    }
}
