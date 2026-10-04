package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.CritterSnareBlock;
import net.thaumcraft.occulta.MutandisItem;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;

/**
 * O <b>Apanha-Bicho</b>: a planta que engole o que é pequeno, e as duas coisas que saem dela.
 *
 * <p>A prova que carrega a fatia é a do <b>tamanho</b>. Ela apanha uma gosma <b>do menor tamanho</b> e só
 * essa: uma gosma grande passa por cima dela sem lhe acontecer nada. É o que separa a planta de uma
 * armadilha — ela não é um perigo, é um <b>passarinheiro</b>.
 */
public class OccultaCritterSnareGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Os números dela são os do original. */
    @GameTest(maxTicks = 20)
    public void itsNumbersAreTheOriginals(GameTestHelper helper) {
        if (CritterSnareBlock.RECLAMA_UMA_EM != 24) {
            helper.fail("o bicho lá dentro reclama uma vez em vinte e quatro");
        }
        if (CritterSnareBlock.Caught.values().length != 5) {
            helper.fail("são quatro bichos e o vazio");
        }
        if (MutandisItem.APANHA_BICHOS != 2 || MutandisItem.APANHA_ERVAS != 3) {
            helper.fail("e as receitas de bicho pedem dois Apanha-Bichos e três Apanha-Ervas");
        }
        helper.succeed();
    }

    /**
     * <b>Ela apanha o morcego</b>, e passa a mostrá-lo.
     *
     * <p>O morcego desaparece do mundo: ele não morre, fica <b>guardado</b> — e sai vivo quando alguém abrir
     * a planta.
     */
    @GameTest(maxTicks = 60)
    public void itSwallowsABat(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = new BlockPos(2, 2, 2);
        BlockPos casa = helper.absolutePos(onde);
        level.setBlockAndUpdate(casa, OccultaBlocks.CRITTER_SNARE.defaultBlockState());

        var morcego = helper.spawn(EntityTypes.BAT, onde);
        helper.runAfterDelay(15, () -> {
            if (morcego.isAlive()) helper.fail("o morcego entra na planta");
            if (level.getBlockState(casa).getValue(CritterSnareBlock.APANHADO)
                    != CritterSnareBlock.Caught.BAT) {
                helper.fail("e a planta passa a mostrá-lo");
            }
            level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
            helper.succeed();
        });
    }

    /**
     * <b>E só a gosma do menor tamanho.</b>
     *
     * <p>Esta é a prova que carrega a fatia. Uma gosma do tamanho dois passa por cima da planta e não lhe
     * acontece nada — e é por isso que o Apanha-Bicho não é uma armadilha: o que ele apanha é o que não
     * machuca ninguém.
     */
    @GameTest(maxTicks = 60)
    public void andOnlyTheSmallestSlime(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = new BlockPos(5, 2, 2);
        BlockPos casa = helper.absolutePos(onde);
        level.setBlockAndUpdate(casa, OccultaBlocks.CRITTER_SNARE.defaultBlockState());

        var gorda = helper.spawn(EntityTypes.SLIME, onde);
        gorda.setSize(2, true);
        helper.runAfterDelay(15, () -> {
            if (!gorda.isAlive()) helper.fail("uma gosma grande não cabe nela");
            if (level.getBlockState(casa).getValue(CritterSnareBlock.APANHADO)
                    != CritterSnareBlock.Caught.EMPTY) {
                helper.fail("e a planta continua vazia");
            }
            gorda.discard();

            var magra = helper.spawn(EntityTypes.SLIME, onde);
            magra.setSize(1, true);
            helper.runAfterDelay(15, () -> {
                if (magra.isAlive()) helper.fail("a do menor tamanho cabe");
                if (level.getBlockState(casa).getValue(CritterSnareBlock.APANHADO)
                        != CritterSnareBlock.Caught.SLIME) {
                    helper.fail("e a planta passa a mostrá-la");
                }
                level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
                helper.succeed();
            });
        });
    }

    /**
     * <b>De onde ela vem</b>: uma teia, quatro mudas de amieiro, água por baixo e um zumbi ao lado.
     *
     * <p>As mudas têm de ser de <b>amieiro</b>, e a escolha é do original: o amieiro é a árvore que ele
     * associa ao que prende e ao que guarda.
     */
    @GameTest(maxTicks = 60)
    public void aWebInAlderSaplingsBecomesSnares(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos aqui = new BlockPos(2, 2, 5);
        BlockPos onde = helper.absolutePos(aqui);
        level.setBlockAndUpdate(onde.below(), Blocks.WATER.defaultBlockState());
        level.setBlockAndUpdate(onde, Blocks.COBWEB.defaultBlockState());

        if (MutandisItem.éTeiaDeApanhaBicho(level, onde)) helper.fail("uma teia sozinha não serve");

        // as mudas querem terra por baixo, e a teia quer água: o chão muda só debaixo dela
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(onde.relative(lado).below(), Blocks.DIRT.defaultBlockState());
            level.setBlockAndUpdate(onde.relative(lado),
                    OccultaBlocks.ALDER_SAPLING.defaultBlockState());
        }
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(onde.relative(lado)).is(OccultaBlocks.ALDER_SAPLING)) {
                helper.fail("as quatro mudas ficam de pé");
            }
        }
        if (MutandisItem.éTeiaDeApanhaBicho(level, onde)) {
            helper.fail("as quatro mudas sem o zumbi não bastam");
        }

        var zumbi = helper.spawn(EntityTypes.ZOMBIE, aqui);
        if (!MutandisItem.éTeiaDeApanhaBicho(level, onde)) {
            helper.fail("e com o zumbi ao lado bastam");
        }

        zumbi.discard();
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(onde.relative(lado), Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());
        helper.succeed();
    }

    /**
     * <b>E a Coruja</b>: a mesma teia com dois Apanha-Bichos de morcego, três Apanha-Ervas com Mutandis
     * Extremis, um com a Pedra Sintonizada carregada — e um lobo.
     *
     * <p>É a receita mais longa do ramo das plantas, e a prova aqui é que ela é <b>exigente</b>: faltando o
     * lobo, faltando um Apanha-Erva ou faltando a pedra, não é nada.
     */
    @GameTest(maxTicks = 80)
    public void theOwlAsksForEverything(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos aqui = new BlockPos(5, 2, 5);
        BlockPos onde = helper.absolutePos(aqui);
        level.setBlockAndUpdate(onde.below(), Blocks.WATER.defaultBlockState());
        level.setBlockAndUpdate(onde, Blocks.COBWEB.defaultBlockState());

        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(onde.relative(lado), OccultaBlocks.CRITTER_SNARE
                    .defaultBlockState().setValue(CritterSnareBlock.APANHADO,
                            CritterSnareBlock.Caught.BAT));
        }
        BlockPos[] quinas = {onde.offset(1, 0, 1), onde.offset(1, 0, -1),
                onde.offset(-1, 0, 1), onde.offset(-1, 0, -1)};
        for (int i = 0; i < quinas.length; i++) {
            level.setBlockAndUpdate(quinas[i], OccultaBlocks.GRASSPER.defaultBlockState());
            if (level.getBlockEntity(quinas[i])
                    instanceof net.thaumcraft.occulta.GrassperBlockEntity planta) {
                planta.põe(new ItemStack(i == 0
                        ? OccultaItems.ATTUNED_STONE_CHARGED : OccultaItems.MUTANDIS_EXTREMIS));
            }
        }

        /*
         * Uma guarda antes das contas: um Apanha-Bicho precisa de chão sólido por baixo e um Apanha-Erva de
         * qualquer coisa que não seja ar. Montar a receita no ar faz os oito caírem sem dizer nada, e a
         * prova falharia a dizer que a receita não serve quando o que não servia era a arena.
         */
        if (MutandisItem.quantosLados(level, onde, CritterSnareBlock.Caught.BAT) != 4) {
            helper.fail("os quatro Apanha-Bichos ficam de pé");
        }
        if (MutandisItem.quantosSeguram(level, onde, OccultaItems.MUTANDIS_EXTREMIS) != 3) {
            helper.fail("e os três Apanha-Ervas de Mutandis Extremis também");
        }

        if (MutandisItem.éTeiaDeCoruja(level, onde)) helper.fail("sem o lobo não é nada");
        var lobo = helper.spawn(EntityTypes.WOLF, aqui);
        if (!MutandisItem.éTeiaDeCoruja(level, onde)) helper.fail("com o lobo ao lado é");

        // e tirar a pedra sintonizada desfaz a receita
        if (level.getBlockEntity(quinas[0])
                instanceof net.thaumcraft.occulta.GrassperBlockEntity planta) {
            planta.tira();
        }
        if (MutandisItem.éTeiaDeCoruja(level, onde)) helper.fail("sem a pedra sintonizada não é");

        lobo.discard();
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(onde.relative(lado), Blocks.AIR.defaultBlockState());
        }
        for (BlockPos quina : quinas) level.setBlockAndUpdate(quina, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());
        helper.succeed();
    }
}
