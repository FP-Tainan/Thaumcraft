package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.AltarPower;
import net.thaumcraft.occulta.BloodRoseBlock;
import net.thaumcraft.occulta.BloodRoseBlockEntity;
import net.thaumcraft.occulta.GarlicGarlandBlock;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.TaglockItem;
import net.thaumcraft.occulta.vampire.BolineItem;
import net.thaumcraft.occulta.vampire.Vampire;

/**
 * A <b>Rosa de Sangue</b> e a <b>Guirlanda de Alho</b>: as duas pontas do ramo do vampiro.
 *
 * <p>A prova que carrega a fatia é a da <b>rosa</b>, porque ela é a única coisa do ofício inteiro que prende
 * alguém <b>sem lhe chegar perto</b>. Tudo o resto — a boneca, a maldição, o espelho — precisa de um fio de
 * quem se quer, e um fio pede um encontro. A rosa planta-se no caminho e espera.
 */
public class OccultaRoseAndGarlicGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.DIRT.defaultBlockState());
            }
        }
    }

    // ------------------------------------------------------------------ a rosa

    /**
     * <b>A rosa lembra-se de quem pisou nela</b>, e o Frasco de Vínculo tira-o de lá.
     *
     * <p>É o vínculo à distância: ninguém tocou em ninguém, e a pessoa está presa.
     */
    @GameTest(maxTicks = 40)
    public void theRoseRemembersWhoSteppedOnIt(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(onde, OccultaBlocks.BLOOD_ROSE.defaultBlockState());

        if (level.getBlockState(onde).getValue(BloodRoseBlock.CHEIA)) {
            helper.fail("uma rosa nova está vazia");
        }

        Player quem = helper.makeMockServerPlayerInLevel();
        if (!BloodRoseBlock.pisou(level, onde, quem)) helper.fail("pisar nela deixa-a com alguém");
        if (!level.getBlockState(onde).getValue(BloodRoseBlock.CHEIA)) {
            helper.fail("e ela fecha, que é o que a denuncia");
        }
        if (BloodRoseBlock.pisou(level, onde, quem)) {
            helper.fail("e quem já está lá dentro não a muda outra vez");
        }

        // e o frasco encostado nela sai cheio daquela pessoa
        TaglockItem.Taglock tirou = BloodRoseBlock.tira(level, onde);
        if (tirou == null) helper.fail("o frasco tira dela quem ela apanhou");
        if (tirou != null && !tirou.owner().equals(quem.getUUID())) {
            helper.fail("e é quem pisou, e não outro qualquer");
        }
        if (level.getBlockState(onde).getValue(BloodRoseBlock.CHEIA)) {
            helper.fail("e tirada, a rosa abre outra vez");
        }
        if (BloodRoseBlock.tira(level, onde) != null) helper.fail("e vazia não dá mais nada");

        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /**
     * <b>Só a Boline a colhe</b> — e colhe-a com quem ela guarda dentro.
     *
     * <p>É o que faz da rosa uma coisa que se <b>leva</b>: planta-se no caminho de quem se quer, colhe-se, e
     * leva-se para casa a pessoa apanhada.
     */
    @GameTest(maxTicks = 40)
    public void onlyTheBolinePicksItAndItKeepsWhoItHolds(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(onde, OccultaBlocks.BLOOD_ROSE.defaultBlockState());

        Player quem = helper.makeMockServerPlayerInLevel();
        BloodRoseBlock.pisou(level, onde, quem);

        if (!BolineItem.colheARosa(level, onde, quem)) helper.fail("a Boline colhe-a");
        if (!level.getBlockState(onde).isAir()) helper.fail("e tira-a do chão");

        var roda = new net.minecraft.world.phys.AABB(onde).inflate(2.0);
        var caiu = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, roda);
        if (caiu.isEmpty()) helper.fail("e deixa-a no chão");

        ItemStack rosa = caiu.getFirst().getItem();
        if (!rosa.is(OccultaItems.BLOOD_ROSE)) helper.fail("o que cai é uma Rosa de Sangue");
        var dentro = rosa.get(OccultaComponents.TAGLOCK);
        if (dentro == null) helper.fail("e ela sai com quem tinha dentro");
        if (dentro != null && !dentro.owner().equals(quem.getUUID())) {
            helper.fail("e é a mesma pessoa");
        }
        caiu.forEach(net.minecraft.world.entity.Entity::discard);

        // e plantada outra vez, ela volta cheia
        level.setBlockAndUpdate(onde, OccultaBlocks.BLOOD_ROSE.defaultBlockState());
        if (level.getBlockEntity(onde) instanceof BloodRoseBlockEntity alma && dentro != null) {
            alma.põe(dentro);
            if (alma.vê() == null) helper.fail("replantada, ela volta com quem trazia");
        }

        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** E ela conta para o Altar: dois de poder, até dez. */
    @GameTest(maxTicks = 20)
    public void theRoseFeedsTheAltar(GameTestHelper helper) {
        boolean achou = false;
        for (AltarPower.Source fonte : AltarPower.sources()) {
            if (!fonte.what().test(OccultaBlocks.BLOOD_ROSE.defaultBlockState())) continue;
            if (fonte.factor() == 2 && fonte.limit() == 10) achou = true;
        }
        if (!achou) helper.fail("a Rosa de Sangue vale dois no Altar, e conta até dez");
        helper.succeed();
    }

    // ------------------------------------------------------------------ e a guirlanda

    /**
     * <b>Um vampiro não passa pela Guirlanda de Alho</b>, e quem a quiser arrancar queima.
     *
     * <p>É a primeira coisa deste mod que torna o jogador <b>indesejado na própria aldeia</b> — e ela custa
     * cinco alhos e dois fios.
     */
    @GameTest(maxTicks = 40)
    public void theGarlandPushesVampiresAndBurnsThem(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos parede = helper.absolutePos(new BlockPos(3, 2, 3));
        BlockPos onde = parede.south();

        level.setBlockAndUpdate(parede, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(onde, OccultaBlocks.GARLIC_GARLAND.defaultBlockState()
                .setValue(GarlicGarlandBlock.FACING, Direction.NORTH));
        if (!level.getBlockState(onde).is(OccultaBlocks.GARLIC_GARLAND)) {
            helper.fail("ela pendura-se na parede");
        }

        // e cai quando a parede cai
        level.setBlockAndUpdate(parede, Blocks.AIR.defaultBlockState());
        if (level.getBlockState(onde).is(OccultaBlocks.GARLIC_GARLAND)) {
            helper.fail("sem parede, a guirlanda cai");
        }

        if (GarlicGarlandBlock.ARDE != 1) helper.fail("quem a arranca arde um segundo");
        helper.succeed();
    }

    /** E ela não estorva ninguém que não seja vampiro: não tem caixa de bater em nada. */
    @GameTest(maxTicks = 20)
    public void theGarlandDoesNotBlockTheLiving(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos parede = helper.absolutePos(new BlockPos(3, 2, 3));
        BlockPos onde = parede.south();
        level.setBlockAndUpdate(parede, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(onde, OccultaBlocks.GARLIC_GARLAND.defaultBlockState()
                .setValue(GarlicGarlandBlock.FACING, Direction.NORTH));

        var caixa = level.getBlockState(onde).getCollisionShape(level, onde);
        if (!caixa.isEmpty()) helper.fail("ela não trava ninguém: empurra, e só a vampiro");

        Player mortal = helper.makeMockServerPlayerInLevel();
        if (Vampire.é(mortal)) helper.fail("este não é vampiro");

        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(parede, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }
}
