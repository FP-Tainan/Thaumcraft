package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.brew.Brew;
import net.thaumcraft.occulta.brew.BrewWorldActions;
import net.thaumcraft.occulta.brew.WitchLilyPadBlock;
import net.thaumcraft.occulta.brew.WitchPlants;
import net.thaumcraft.occulta.brew.WitchVineBlock;

import java.util.List;

/**
 * As <b>quatro plantas do ofício</b>: a teia, o cato, o cipó e o nenúfar de bruxa.
 */
public class OccultaWitchPlantsGameTest {
    /** <b>Cada planta vem de um ingrediente</b>, e são esses três que as fazem. */
    @GameTest(maxTicks = 20)
    public void eachPlantComesFromItsOwnIngredient(GameTestHelper helper) {
        confere(helper, OccultaItems.WITCH_WEB, BrewWorldActions.Webs.class, "a teia");
        confere(helper, Items.VINE, BrewWorldActions.Vines.class, "o cipó");
        confere(helper, Items.CACTUS, BrewWorldActions.Thorns.class, "o cato");
        helper.succeed();
    }

    private static void confere(GameTestHelper helper, net.minecraft.world.item.Item qual,
                                Class<?> esperado, String nome) {
        for (var ação : Brew.actions(List.of(qual))) {
            if (esperado.isInstance(ação)) return;
        }
        helper.fail(nome + " vem do ingrediente dele, e não veio");
    }

    /**
     * <b>A teia cresce em bola.</b> Sem força é uma casa; no primeiro grau é a cruz de sete; no segundo é a
     * bola de dezenove — tudo o que toca o meio por face ou por aresta, e nada pelas quinas.
     */
    @GameTest(maxTicks = 20, padding = 8)
    public void thewebGrowsIntoABall(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(3, 6, 3));

        // no ar puro, a teia nasce uma casa acima do estouro e nada a atrapalha
        limpa(helper, meio, 3);
        WitchPlants.teia(level, meio, null, 0);
        if (conta(helper, meio, 3) != 1) helper.fail("sem força, a teia é uma casa só");

        limpa(helper, meio, 3);
        WitchPlants.teia(level, meio, null, 1);
        if (conta(helper, meio, 3) != 7) helper.fail("no primeiro grau, ela é a cruz de sete");

        limpa(helper, meio, 3);
        WitchPlants.teia(level, meio, null, 2);
        int tem = conta(helper, meio, 3);
        if (tem != 19) helper.fail("no segundo grau, ela é a bola de dezenove, e deu " + tem);

        limpa(helper, meio, 3);
        helper.succeed();
    }

    /**
     * <b>O cato sobe três casas</b>, e mais uma por grau. E ele <b>não fica no ar</b>: tirando-lhe o pé, a
     * coluna inteira vem abaixo.
     */
    @GameTest(maxTicks = 60, padding = 8)
    public void theCactusGrowsAColumnAndFallsWithoutAFoot(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos chão = helper.absolutePos(new BlockPos(3, 2, 8));
        limpa(helper, chão.above(3), 3);
        level.setBlockAndUpdate(chão, Blocks.STONE.defaultBlockState());

        WitchPlants.cato(level, chão, Direction.UP, 0);
        for (int i = 1; i <= 3; i++) {
            if (!level.getBlockState(chão.above(i)).is(OccultaBlocks.WITCH_CACTUS)) {
                helper.fail("o cato sobe três casas, e faltou a " + i);
                return;
            }
        }
        if (level.getBlockState(chão.above(4)).is(OccultaBlocks.WITCH_CACTUS)) {
            helper.fail("e não sobe mais que isso sem grau");
        }

        // tirando o pé, o resto não se segura
        level.removeBlock(chão.above(1), false);
        helper.runAfterDelay(20, () -> {
            for (int i = 1; i <= 3; i++) {
                if (level.getBlockState(chão.above(i)).is(OccultaBlocks.WITCH_CACTUS)) {
                    helper.fail("sem pé, a coluna inteira vem abaixo, e a casa " + i + " ficou");
                    return;
                }
            }
            level.setBlockAndUpdate(chão, Blocks.AIR.defaultBlockState());
            helper.succeed();
        });
    }

    /**
     * <b>O cipó corre a parede</b> de cima a baixo, pela face em que o frasco bateu — e <b>só</b> em parede:
     * no chão e no teto ele não pega.
     */
    @GameTest(maxTicks = 20, padding = 8)
    public void theVineRunsTheWallAndOnlyTheWall(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pé = helper.absolutePos(new BlockPos(8, 2, 3));
        limpa(helper, pé.above(3), 4);
        for (int i = 0; i < 5; i++) {
            level.setBlockAndUpdate(pé.above(i), Blocks.STONE.defaultBlockState());
        }

        // no teto e no chão ele não faz nada
        WitchPlants.cipó(level, pé.above(2), Direction.UP, 0);
        WitchPlants.cipó(level, pé.above(2), Direction.DOWN, 0);
        if (conta(helper, pé.above(2), 4) != 0) helper.fail("o cipó só pega em parede");

        WitchPlants.cipó(level, pé.above(2), Direction.SOUTH, 0);
        for (int i = 0; i < 5; i++) {
            var oquê = level.getBlockState(pé.above(i).south());
            if (!oquê.is(OccultaBlocks.WITCH_VINE)) {
                helper.fail("o cipó corre a parede inteira, e faltou a casa " + i);
                return;
            }
            if (oquê.getValue(WitchVineBlock.FACING) != Direction.NORTH) {
                helper.fail("e ele se agarra à parede, que está ao norte dele");
                return;
            }
        }
        if (level.getBlockState(pé.above(5).south()).is(OccultaBlocks.WITCH_VINE)) {
            helper.fail("acabada a parede, acabado o cipó");
        }

        limpa(helper, pé.above(3), 4);
        for (int i = 0; i < 5; i++) level.setBlockAndUpdate(pé.above(i), Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** <b>O nenúfar de bruxa se alastra</b> pela água, e para quando acaba o que tinha para gastar. */
    @GameTest(maxTicks = 180, padding = 10)
    public void theLilyPadSpreadsItselfAcrossTheWater(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(8, 3, 8));
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                level.setBlockAndUpdate(meio.offset(dx, -1, dz), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(meio.offset(dx, 0, dz), Blocks.WATER.defaultBlockState());
                level.setBlockAndUpdate(meio.offset(dx, 1, dz), Blocks.AIR.defaultBlockState());
            }
        }
        level.setBlockAndUpdate(meio.above(), OccultaBlocks.WITCH_LILY_PAD.defaultBlockState()
                .setValue(WitchLilyPadBlock.GROWTH, 3));

        helper.runAfterDelay(160, () -> {
            int quantos = 0;
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    if (level.getBlockState(meio.offset(dx, 1, dz)).is(OccultaBlocks.WITCH_LILY_PAD)) {
                        quantos++;
                    }
                }
            }
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    level.setBlockAndUpdate(meio.offset(dx, 1, dz), Blocks.AIR.defaultBlockState());
                    level.setBlockAndUpdate(meio.offset(dx, 0, dz), Blocks.AIR.defaultBlockState());
                    level.setBlockAndUpdate(meio.offset(dx, -1, dz), Blocks.AIR.defaultBlockState());
                }
            }
            if (quantos < 2) helper.fail("um nenúfar com três para gastar faz mais que ele mesmo");
            else helper.succeed();
        });
    }

    // ------------------------------------------------------------------ o pano de fundo

    /** Limpa uma caixa à volta do meio, para a prova não ver o que não é dela. */
    private static void limpa(GameTestHelper helper, BlockPos meio, int raio) {
        var level = helper.getLevel();
        for (int dx = -raio; dx <= raio; dx++) {
            for (int dy = -raio; dy <= raio; dy++) {
                for (int dz = -raio; dz <= raio; dz++) {
                    level.setBlockAndUpdate(meio.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    /** E conta as teias que ficaram nela. */
    private static int conta(GameTestHelper helper, BlockPos meio, int raio) {
        var level = helper.getLevel();
        int quantas = 0;
        for (int dx = -raio; dx <= raio; dx++) {
            for (int dy = -raio; dy <= raio; dy++) {
                for (int dz = -raio; dz <= raio; dz++) {
                    var oquê = level.getBlockState(meio.offset(dx, dy, dz));
                    if (oquê.is(OccultaBlocks.WITCH_WEB) || oquê.is(OccultaBlocks.WITCH_VINE)) quantas++;
                }
            }
        }
        return quantas;
    }
}
