package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.block.TaintBlock;
import net.thaumcraft.registry.TCBlocks;

/**
 * A mácula tem de se comportar como no Thaumcraft 4.2.3.5: ela se alastra onde já está forte, míngua
 * onde está sozinha, e recua diante da Flor Etérea.
 */
public class TaintGameTest {
    /** Um bloco de mácula sozinho no meio da terra acaba secando. */
    @GameTest(maxTicks = 400)
    public void loneTaintWithers(GameTestHelper helper) {
        // emparedada em pedra de propósito: assim ela não tem ar em volta onde criar fibra, e a fibra
        // deixaria de ser mácula sozinha — que é justamente o que esta prova quer medir
        BlockPos at = new BlockPos(3, 2, 3);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    helper.setBlock(at.offset(dx, dy, dz), Blocks.STONE);
                }
            }
        }
        helper.setBlock(at, TCBlocks.TAINT_SOIL);

        helper.succeedWhen(() -> {
            // o teste apressa o tique aleatório para não esperar a sorte do jogo
            helper.getLevel().getBlockState(helper.absolutePos(at))
                    .randomTick(helper.getLevel(), helper.absolutePos(at), helper.getLevel().getRandom());
            if (helper.getBlockState(at).is(TCBlocks.TAINT_SOIL)) {
                helper.fail("mácula sozinha devia ter secado");
            }
        });
    }

    /** Com companhia, ela avança sobre a terra em volta. */
    @GameTest(maxTicks = 600)
    public void taintWithCompanySpreads(GameTestHelper helper) {
        // uma mancha de mácula colada, com um bloco de terra no meio dela: ele tem de cair
        BlockPos middle = new BlockPos(3, 2, 3);
        helper.setBlock(middle, Blocks.DIRT);
        helper.setBlock(middle.north(), TCBlocks.TAINT_SOIL);
        helper.setBlock(middle.south(), TCBlocks.TAINT_SOIL);
        helper.setBlock(middle.east(), TCBlocks.TAINT_SOIL);
        // e o resto da mancha, para que as três não fiquem sozinhas e sequem
        helper.setBlock(middle.north().east(), TCBlocks.TAINT_SOIL);
        helper.setBlock(middle.south().east(), TCBlocks.TAINT_SOIL);

        if (TaintBlock.adjacentTaint(helper.getLevel(), helper.absolutePos(middle)) != 3) {
            helper.fail("o bloco do meio devia ter três vizinhos maculados");
        }

        helper.succeedWhen(() -> {
            for (BlockPos from : new BlockPos[]{middle.north(), middle.south(), middle.east(),
                    middle.north().east(), middle.south().east()}) {
                var at = helper.absolutePos(from);
                helper.getLevel().getBlockState(at)
                        .randomTick(helper.getLevel(), at, helper.getLevel().getRandom());
            }
            if (!TaintBlock.isTaint(helper.getBlockState(middle))) {
                helper.fail("o bloco do meio devia ter sido tomado");
            }
        });
    }

    /** A Flor Etérea desfaz a mácula à volta dela. */
    @GameTest(maxTicks = 600)
    public void theBloomCleansesTaint(GameTestHelper helper) {
        BlockPos bloomAt = new BlockPos(3, 2, 3);
        helper.setBlock(bloomAt.below(), Blocks.GRASS_BLOCK);
        helper.setBlock(bloomAt, TCBlocks.ETHEREAL_BLOOM);

        BlockPos dirty = new BlockPos(5, 1, 5);
        helper.setBlock(dirty, TCBlocks.TAINT_SOIL);

        helper.succeedWhen(() -> {
            var at = helper.absolutePos(bloomAt);
            // o sorteio da flor escolhe um bloco qualquer em volta: quatro por tique, para não depender da sorte
            for (int i = 0; i < 4; i++) {
                helper.getLevel().getBlockState(at)
                        .randomTick(helper.getLevel(), at, helper.getLevel().getRandom());
            }
            if (TaintBlock.isTaint(helper.getBlockState(dirty))) {
                helper.fail("a flor devia ter limpado a mácula ali perto");
            }
        });
    }
}
