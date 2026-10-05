package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.CrystalBallBlockEntity;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.divine.Prediction;
import net.thaumcraft.occulta.divine.Predictions;
import net.thaumcraft.occulta.divine.Prophecy;

/**
 * A <b>Bola de Cristal</b> e as <b>dezessete profecias</b>.
 *
 * <p>A prova que carrega a fatia é a de que a profecia <b>se cumpre à força</b>: passado o prazo, ela deixa
 * de esperar que o mundo a cumpra e passa a fabricá-la. É a ideia inteira do ramo.
 */
public class OccultaCrystalBallGameTest {
    /** São dezessete, com os números e os pesos do original. */
    @GameTest
    public void thereAreSeventeenOfThem(GameTestHelper helper) {
        if (Predictions.quantas() != 17) {
            helper.fail("são dezessete profecias; há " + Predictions.quantas());
        }
        for (int id = 1; id <= 17; id++) {
            if (Predictions.daquele(id) == null) helper.fail("falta a de número " + id);
        }
        Prediction queda = Predictions.daquele(4);
        if (queda.peso != 13) helper.fail("a da queda pesa treze; pesa " + queda.peso);
        if (queda.prazo() != Predictions.PRAZO) helper.fail("e tem o prazo de oito minutos");

        // as que o mod cumpre sempre têm prazo próprio, muito mais curto
        Prediction amor = Predictions.daquele(9);
        if (amor.prazo() != 1210) helper.fail("a do amor tem prazo próprio; tem " + amor.prazo());
        helper.succeed();
    }

    /** Quem não é vidente não lê a sorte de ninguém. */
    @GameTest
    public void onlyASeerCanRead(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.removeAttached(Predictions.VIDENTE);
        quem.removeAttached(Predictions.PROFECIA);

        /*
         * O jogador de mentira das provas nasce no criativo, e no criativo qualquer um lê a sorte. Por isso
         * a prova pergunta à marca, e não ao recado: sem ela ele não é vidente, e é isso que a bola olha.
         */
        if (Predictions.vidente(quem)) helper.fail("ninguém nasce vidente");
        Predictions.ensina(quem);
        if (!Predictions.vidente(quem)) helper.fail("e o rito da bola o ensina");
        helper.succeed();
    }

    /**
     * <b>Uma de cada vez, e a segunda leitura repete a primeira.</b>
     *
     * <p>É o que faz da profecia uma coisa que se vive em vez de se colecionar.
     */
    @GameTest
    public void onlyOneAtATime(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setAttached(Predictions.VIDENTE, Unit.INSTANCE);
        quem.removeAttached(Predictions.PROFECIA);

        Predictions.lê(level, quem, quem, false);
        Prophecy primeira = quem.getAttached(Predictions.PROFECIA);
        if (primeira == null) helper.fail("a bola devia dizer-lhe alguma coisa");

        Predictions.lê(level, quem, quem, false);
        Prophecy segunda = quem.getAttached(Predictions.PROFECIA);
        if (segunda == null || segunda.id() != primeira.id()) {
            helper.fail("a segunda leitura repete a primeira; veio " + segunda);
        }
        quem.removeAttached(Predictions.PROFECIA);
        helper.succeed();
    }

    /**
     * <b>Passado o prazo, ela abre o chão.</b>
     *
     * <p>Esta é a prova que carrega a fatia. Dá-se a um jogador a profecia da queda, finge-se que ela foi
     * dita há muito tempo, e corre-se uma batida: o chão debaixo dele vira <b>cascalho</b> e o que estava por
     * baixo vai embora.
     *
     * <p>A hipótese por batida é de cinco por cento, de modo que a prova corre muitas batidas.
     */
    @GameTest(maxTicks = 200)
    public void pastDueItDigsTheHole(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pé = helper.absolutePos(new BlockPos(3, 3, 3));

        // um chão de terra de três por três, com pedra por baixo
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y >= -6; y--) {
                    level.setBlockAndUpdate(pé.offset(x, y, z), Blocks.STONE.defaultBlockState());
                }
                level.setBlockAndUpdate(pé.offset(x, 0, z), Blocks.DIRT.defaultBlockState());
            }
        }

        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.snapTo(pé.getX() + 0.5, pé.getY() + 1.0, pé.getZ() + 0.5);
        quem.setAttached(Predictions.PROFECIA, new Prophecy(4, level.getGameTime() - 20000L));

        helper.succeedWhen(() -> {
            Predictions.batida(level, quem);
            if (!level.getBlockState(pé).is(Blocks.GRAVEL)) {
                throw helper.assertionException("o chão devia virar cascalho");
            }
            if (!level.getBlockState(pé.below()).isAir()) {
                helper.fail("e o que estava por baixo devia sumir");
            }
            if (quem.hasAttached(Predictions.PROFECIA)) {
                helper.fail("e a profecia devia estar cumprida");
            }
        });
    }

    /** A bola tem uma recarga de cem batidas entre uma leitura e outra. */
    @GameTest
    public void theBallNeedsToRecharge(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(onde, OccultaBlocks.CRYSTAL_BALL.defaultBlockState());

        if (!(level.getBlockEntity(onde) instanceof CrystalBallBlockEntity bola)) {
            helper.fail("a bola devia ter alma");
            return;
        }
        if (!bola.podeSerUsada(level)) helper.fail("uma bola nova pode ser usada");
        bola.usada(level);
        if (bola.podeSerUsada(level)) helper.fail("e logo a seguir não pode");
        if (CrystalBallBlockEntity.RECARGA != 100L) helper.fail("a recarga é de cem batidas");
        helper.succeed();
    }

    /**
     * <b>E o miolo dela pulsa com a hora do mundo.</b>
     *
     * <p>O vaivém é de cento e sessenta batidas, e o tom vai de vinte a cem por cento. Todas as bolas do
     * mundo batem juntas, porque todas leem a mesma hora.
     */
    @GameTest
    public void itsHeartBeatsWithTheWorld(GameTestHelper helper) {
        int claro = CrystalBallBlockEntity.pulso(80L) & 0xFF;
        int escuro = CrystalBallBlockEntity.pulso(0L) & 0xFF;
        if (claro != 255) helper.fail("no meio do vaivém ela está no claro; deu " + claro);
        if (escuro != Math.round(20 * 0.01f * 255)) {
            helper.fail("e nas pontas no escuro; deu " + escuro);
        }
        if (CrystalBallBlockEntity.pulso(160L) != CrystalBallBlockEntity.pulso(0L)) {
            helper.fail("e o vaivém dá a volta em cento e sessenta");
        }
        helper.succeed();
    }
}
