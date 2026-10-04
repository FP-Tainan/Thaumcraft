package net.thaumcraft.test;

import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.brew.BrewDispersal;
import net.thaumcraft.occulta.brew.BrewImpact;
import net.thaumcraft.occulta.curse.CursedBlocks;

/**
 * O <b>cozimento que fica preso na maçaneta</b>.
 *
 * <p>A prova que carrega a fatia é a de que o botão amaldiçoado <b>é um botão</b>: ele tem o desenho do
 * botão, cai como um botão e se aperta como um botão. A armadilha não se vê, e a única maneira de saber é
 * ter visto o frasco bater nele.
 *
 * <p>E a segunda é a da <b>conta</b>: dois frascos da mesma receita no mesmo botão não se trocam, se somam —
 * e a peça fica armada para duas pessoas seguidas.
 */
public class OccultaCursedBlocksGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Um cozimento qualquer, que o caldeirão conhece. */
    private static List<Item> cozimento() {
        return List.of(Items.SPIDER_EYE);
    }

    /** Põe um botão de pedra no chão, de pé. */
    private static BlockPos botão(GameTestHelper helper, BlockPos onde) {
        ServerLevel level = helper.getLevel();
        BlockPos casa = helper.absolutePos(onde);
        level.setBlockAndUpdate(casa, Blocks.STONE_BUTTON.defaultBlockState()
                .setValue(ButtonBlock.FACE, AttachFace.FLOOR)
                .setValue(ButtonBlock.FACING, Direction.NORTH));
        return casa;
    }

    /**
     * <b>O frasco de gatilho fica.</b>
     *
     * <p>Acertando um botão, ele troca a peça por uma gêmea amaldiçoada — que é, em tudo o que se vê, o mesmo
     * botão: mesmo tipo de peça, mesma face, mesmo rumo.
     */
    @GameTest(maxTicks = 40)
    public void theTriggeredBrewSticksToTheButton(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos casa = botão(helper, new BlockPos(3, 2, 3));

        var jeito = new BrewDispersal.Triggered();
        jeito.onImpact(level, cozimento(),
                new BlockHitResult(Vec3.atCenterOf(casa), Direction.UP, casa, false),
                new BrewImpact(null));

        var virou = level.getBlockState(casa);
        if (!virou.is(OccultaBlocks.CURSED_STONE_BUTTON)) {
            helper.fail("o frasco de gatilho troca o botão pela gêmea dele");
            return;
        }
        if (virou.getValue(ButtonBlock.FACE) != AttachFace.FLOOR
                || virou.getValue(ButtonBlock.FACING) != Direction.NORTH) {
            helper.fail("e a gêmea fica com a mesma face e o mesmo rumo");
        }
        if (!CursedBlocks.armada(level, casa)) helper.fail("e com a maldição à espera dentro");

        level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /**
     * E <b>acerta só o que serve</b>: numa pedra qualquer, o frasco se perde.
     *
     * <p>É o único jeito de espalhar do mod que pode ser desperdiçado, e é de propósito: ele vale por acertar
     * o lugar certo.
     */
    @GameTest(maxTicks = 20)
    public void onAnythingElseItIsWasted(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos pedra = helper.absolutePos(new BlockPos(5, 1, 5));

        new BrewDispersal.Triggered().onImpact(level, cozimento(),
                new BlockHitResult(Vec3.atCenterOf(pedra), Direction.UP, pedra, false),
                new BrewImpact(null));

        if (!level.getBlockState(pedra).is(Blocks.STONE)) helper.fail("a pedra continua pedra");
        if (CursedBlocks.armada(level, pedra)) helper.fail("e não guarda maldição nenhuma");
        helper.succeed();
    }

    /**
     * <b>Dois frascos da mesma receita somam.</b>
     *
     * <p>A peça fica armada para duas pessoas seguidas. Um de receita diferente, não: esse <b>troca</b> o que
     * lá estava, e a conta volta a um.
     */
    @GameTest(maxTicks = 40)
    public void twoOfTheSameAddUpAndADifferentOneReplaces(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos casa = botão(helper, new BlockPos(2, 2, 6));
        var jeito = new BrewDispersal.Triggered();
        var bateu = new BlockHitResult(Vec3.atCenterOf(casa), Direction.UP, casa, false);

        jeito.onImpact(level, cozimento(), bateu, new BrewImpact(null));
        jeito.onImpact(level, cozimento(), bateu, new BrewImpact(null));
        var alma = CursedBlocks.oQueTem(level, casa);
        if (alma == null || alma.cargas() != 2) {
            helper.fail("dois da mesma receita somam, e deu " + (alma == null ? "nada" : alma.cargas()));
            level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
            return;
        }

        jeito.onImpact(level, List.of(Items.POISONOUS_POTATO), bateu, new BrewImpact(null));
        alma = CursedBlocks.oQueTem(level, casa);
        if (alma == null || alma.cargas() != 1) {
            helper.fail("e um de receita diferente troca, voltando a um");
        }
        if (alma != null && !alma.dentro().equals(List.of(Items.POISONOUS_POTATO))) {
            helper.fail("com a receita nova dentro");
        }

        level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /**
     * <b>Gasta a última carga, o botão volta a ser um botão.</b>
     *
     * <p>É a diferença entre uma armadilha e uma praga: a armadilha acaba, e quem entrar depois não encontra
     * nada.
     */
    @GameTest(maxTicks = 40)
    public void whenItIsSpentThePieceGoesBackToNormal(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos casa = botão(helper, new BlockPos(6, 2, 3));
        var bateu = new BlockHitResult(Vec3.atCenterOf(casa), Direction.UP, casa, false);
        var jeito = new BrewDispersal.Triggered();
        jeito.onImpact(level, cozimento(), bateu, new BrewImpact(null));
        jeito.onImpact(level, cozimento(), bateu, new BrewImpact(null));

        Player quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(6.5, 2.0, 2.5)));

        // a primeira gasta uma carga e a peça continua armada
        level.getBlockState(casa).useWithoutItem(level, quem, bateu);
        if (!level.getBlockState(casa).is(OccultaBlocks.CURSED_STONE_BUTTON)) {
            helper.fail("com duas cargas, a primeira não a desarma");
        }

        // a segunda gasta a última, e a peça volta a ser o que era
        level.getBlockState(casa).useWithoutItem(level, quem, bateu);
        if (!level.getBlockState(casa).is(Blocks.STONE_BUTTON)) {
            helper.fail("gasta a última, o botão volta a ser um botão");
        }
        if (CursedBlocks.armada(level, casa)) helper.fail("e não guarda mais nada");

        level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** E a lista das peças que a maldição apanha é a do original. */
    @GameTest(maxTicks = 20)
    public void theListOfPiecesIsTheOriginalOne(GameTestHelper helper) {
        record Par(net.minecraft.world.level.block.Block de,
                   net.minecraft.world.level.block.Block para) {
        }
        List<Par> pares = List.of(
                new Par(Blocks.STONE_BUTTON, OccultaBlocks.CURSED_STONE_BUTTON),
                new Par(Blocks.OAK_BUTTON, OccultaBlocks.CURSED_WOODEN_BUTTON),
                new Par(Blocks.LEVER, OccultaBlocks.CURSED_LEVER),
                new Par(Blocks.OAK_DOOR, OccultaBlocks.CURSED_WOODEN_DOOR),
                new Par(Blocks.OAK_PRESSURE_PLATE, OccultaBlocks.CURSED_WOODEN_PRESSURE_PLATE),
                new Par(Blocks.STONE_PRESSURE_PLATE, OccultaBlocks.CURSED_STONE_PRESSURE_PLATE),
                new Par(OccultaBlocks.SNOW_PRESSURE_PLATE, OccultaBlocks.CURSED_SNOW_PRESSURE_PLATE));
        for (Par par : pares) {
            if (CursedBlocks.gêmea(par.de().defaultBlockState()) != par.para()) {
                helper.fail("a gêmea de " + par.de() + " é outra");
            }
        }
        // e nada mais
        if (CursedBlocks.gêmea(Blocks.IRON_DOOR.defaultBlockState()) != null) {
            helper.fail("a porta de ferro não tem gêmea: ela não se abre com a mão");
        }
        if (CursedBlocks.gêmea(Blocks.STONE.defaultBlockState()) != null) {
            helper.fail("nem a pedra");
        }
        helper.succeed();
    }
}
