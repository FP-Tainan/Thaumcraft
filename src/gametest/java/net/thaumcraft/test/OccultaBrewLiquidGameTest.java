package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.brew.Brew;
import net.thaumcraft.occulta.brew.BrewActions;
import net.thaumcraft.occulta.brew.BrewDispersal;
import net.thaumcraft.occulta.brew.BrewFluidBlockEntity;
import net.thaumcraft.occulta.brew.BrewImpact;
import net.thaumcraft.occulta.brew.BrewLiquidBlock;

import java.util.List;

/**
 * A <b>poça de cozimento</b>: a losna faz um frasco que escorre, e o que ele deixa no chão corre, mingua e
 * seca.
 */
public class OccultaBrewLiquidGameTest {
    /** O cozimento de mentira que se põe na poça, para ela ter o que carregar. */
    private static final List<Item> DENTRO = List.of(net.minecraft.world.item.Items.SUGAR);

    /** <b>É a losna que manda o cozimento escorrer</b>, e nenhum outro ingrediente de espalhamento. */
    @GameTest(maxTicks = 20)
    public void theWormwoodSprigIsWhatMakesABrewFlow(GameTestHelper helper) {
        BrewDispersal achado = null;
        for (var ação : Brew.actions(List.of(OccultaItems.WORMWOOD_SPRIG))) {
            if (ação instanceof BrewActions.Dispersal espalha) achado = espalha.jeito();
        }
        if (achado == null) helper.fail("a losna não dá jeito de espalhar nenhum");
        if (!(achado instanceof BrewDispersal.Liquid)) {
            helper.fail("a losna faz o cozimento escorrer, e não " + achado.getClass().getSimpleName());
        }

        // e o pedaço de nome que ela põe na frente é o do original
        if (!achado.nameKey().equals("tc.brew.dispersal.liquid")) {
            helper.fail("o nome do escorrimento é o do original");
        }
        helper.succeed();
    }

    /**
     * <b>Num chão plano ela se abre em cruz</b> — uma casa de altura por passo, nos quatro lados ao mesmo
     * tempo, porque nenhum deles leva mais perto de um buraco que os outros. E depois <b>seca</b>, da
     * nascente para fora, até não sobrar nada.
     */
    @GameTest(maxTicks = 240, padding = 16)
    public void thePuddleOpensInACrossAndThenDriesUp(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(7, 3, 7));

        // um chão de pedra largo o bastante para a poça inteira caber nele, e nada em volta que a desvie
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                level.setBlockAndUpdate(meio.offset(dx, -1, dz), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(meio.offset(dx, 0, dz), Blocks.AIR.defaultBlockState());
            }
        }

        level.setBlockAndUpdate(meio, OccultaBlocks.BREW_LIQUID.defaultBlockState());
        if (!(level.getBlockEntity(meio) instanceof BrewFluidBlockEntity poça)) {
            helper.fail("a poça tem de carregar o cozimento");
            return;
        }
        poça.start(DENTRO, new BrewImpact(null));

        // nada esbarra nela, nem na nascente
        if (!level.getBlockState(meio).getCollisionShape(level, meio).isEmpty()) {
            helper.fail("não se esbarra numa poça");
        }

        helper.runAfterDelay(12, () -> {
            for (Direction lado : Direction.Plane.HORIZONTAL) {
                BlockPos vizinho = meio.relative(lado);
                var oquê = level.getBlockState(vizinho);
                if (!oquê.is(OccultaBlocks.BREW_LIQUID)) {
                    helper.fail("a poça se abre para " + lado + " também");
                    return;
                }
                if (oquê.getValue(BrewLiquidBlock.LEVEL) != 1) {
                    helper.fail("e o primeiro passo gasta uma casa de altura, não "
                            + oquê.getValue(BrewLiquidBlock.LEVEL));
                    return;
                }
                if (!(level.getBlockEntity(vizinho) instanceof BrewFluidBlockEntity nova)
                        || !nova.contents().equals(DENTRO)) {
                    helper.fail("e leva o cozimento da nascente consigo");
                    return;
                }
            }

            helper.runAfterDelay(220, () -> {
                int sobrou = 0;
                for (int dx = -7; dx <= 7; dx++) {
                    for (int dz = -7; dz <= 7; dz++) {
                        if (level.getBlockState(meio.offset(dx, 0, dz)).is(OccultaBlocks.BREW_LIQUID)) {
                            sobrou++;
                        }
                    }
                }
                // e a arena fica como estava
                for (int dx = -7; dx <= 7; dx++) {
                    for (int dz = -7; dz <= 7; dz++) {
                        level.setBlockAndUpdate(meio.offset(dx, 0, dz), Blocks.AIR.defaultBlockState());
                        level.setBlockAndUpdate(meio.offset(dx, -1, dz), Blocks.AIR.defaultBlockState());
                    }
                }
                if (sobrou > 0) helper.fail("uma poça seca sozinha, e sobraram " + sobrou + " casas");
                else helper.succeed();
            });
        });
    }
}
