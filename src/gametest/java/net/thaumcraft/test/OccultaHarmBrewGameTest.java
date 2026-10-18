package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.brew.BrewActions;
import net.thaumcraft.occulta.brew.BrewModifiers;
import net.thaumcraft.occulta.brew.BrewRegistry;
import net.thaumcraft.occulta.leonard.LeonardEntity;
import net.thaumcraft.occulta.torment.Demonic;

/**
 * O <b>Cozimento de Ferir Demônios</b>, que é o único do mod que <b>bate</b>.
 *
 * <p>A prova que carrega a fatia é a dos <b>três casos</b>: ele tira muito do que é do inferno, pouco do
 * resto, e contra o <b>Leonard</b> não bate — abre a porta da fraqueza dele, que salta os tetos do chefe.
 * É o que torna o Leonard matável antes do anoitecer.
 */
public class OccultaHarmBrewGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** <b>O gelo está na panela</b>, com os números do original. */
    @GameTest(maxTicks = 20)
    public void theIceIsInThePot(GameTestHelper helper) {
        var ação = BrewRegistry.of(Items.ICE);
        if (!(ação instanceof BrewActions.Harm ferir)) {
            helper.fail("o bloco de gelo devia ser um cozimento que fere");
            return;
        }
        if (ferir.power() != 500) helper.fail("e custar quinhentos de altar");
        if (!ferir.isEffect()) helper.fail("e contar como efeito");
        if (BrewActions.Harm.NO_INFERNO != 10 || BrewActions.Harm.NO_RESTO != 3) {
            helper.fail("dez no inferno e três no resto");
        }
        if (BrewActions.Harm.TETO != 1 || BrewActions.Harm.TETO_SEM_TETO != 3) {
            helper.fail("e a força dele para em um, ou três sem o teto");
        }
        helper.succeed();
    }

    /**
     * <b>Os três casos</b>: o do inferno leva muito, o resto leva pouco, e o Leonard leva pela porta da
     * fraqueza.
     */
    @GameTest(maxTicks = 40)
    public void theThreeCases(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var ação = (BrewActions.Harm) BrewRegistry.of(Items.ICE);

        // um bicho do inferno: vinte, que é dez deslocado por um
        var blaze = helper.spawn(EntityTypes.BLAZE, new BlockPos(2, 2, 2));
        if (!Demonic.é(blaze)) {
            helper.fail("o blaze é do inferno");
            blaze.discard();
            return;
        }
        blaze.setHealth(blaze.getMaxHealth());
        float tinha = blaze.getHealth();
        ação.applyToEntity(level, blaze, temperos());
        float tirou = tinha - blaze.getHealth();
        blaze.discard();
        if (tirou <= 0.0f) {
            helper.fail("e devia ter apanhado");
            return;
        }

        // um porco: seis, que é três deslocado por um — e menos do que o blaze
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(5, 2, 5));
        porco.setHealth(porco.getMaxHealth());
        float tinhaPorco = porco.getHealth();
        ação.applyToEntity(level, porco, temperos());
        float tirouDoPorco = tinhaPorco - porco.getHealth();
        porco.discard();
        if (tirouDoPorco <= 0.0f) {
            helper.fail("o porco também apanha, menos");
            return;
        }
        if (tirouDoPorco >= tirou) {
            helper.fail("mas menos do que o do inferno: " + tirouDoPorco + " contra " + tirou);
            return;
        }

        /*
         * E o Leonard: pela <b>porta da fraqueza</b>, que só abre abaixo de quatro décimos da vida. O que
         * a prova mede é que ela <b>abre</b> — o mesmo golpe, dado pela porta da frente, não lhe toca,
         * porque o hurtServer dele só deixa passar golpe de gente.
         */
        var ele = helper.spawn(OccultaEntities.LEONARD, new BlockPos(3, 2, 3));
        ele.setHealth(ele.getMaxHealth() * 0.3f);

        float antesDaPorta = ele.getHealth();
        ele.hurtServer(level, level.damageSources().indirectMagic(null, null), 10.0f);
        if (ele.getHealth() != antesDaPorta) {
            helper.fail("pela porta da frente, a magia não lhe toca");
            ele.discard();
            return;
        }

        ele.invulnerableTime = 0;
        float tinhaEle = ele.getHealth();
        ação.applyToEntity(level, ele, temperos());
        float tirouDele = tinhaEle - ele.getHealth();
        ele.discard();
        if (tirouDele != BrewActions.Harm.NO_INFERNO) {
            helper.fail("mas pela porta da fraqueza tira os dez do inferno: tirou " + tirouDele);
            return;
        }
        helper.succeed();
    }

    /** <b>E o Leonard entrou no rótulo do inferno</b>, que era o nono e último que faltava. */
    @GameTest(maxTicks = 20)
    public void theLeonardIsOfHell(GameTestHelper helper) {
        piso(helper);
        var ele = helper.spawn(OccultaEntities.LEONARD, new BlockPos(3, 2, 3));
        boolean é = Demonic.é(ele);
        ele.discard();
        if (!é) helper.fail("o Leonard é do inferno, como os outros oito");
        helper.succeed();
    }

    /** Temperos de um cozimento simples, sem nada que mexa na força. */
    private static BrewModifiers temperos() {
        return new BrewModifiers(1.0, 1.0);
    }
}
