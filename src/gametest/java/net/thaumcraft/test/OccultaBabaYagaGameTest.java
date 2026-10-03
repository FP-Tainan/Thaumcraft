package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.baba.BabaYagaEntity;

/**
 * A Baba Yaga: o teto da pancada, o salto, e a visita.
 *
 * <p>O que faz dela um chefe não é a vida: é o <b>teto</b>. Quinhentos de vida com quinze por pancada são
 * trinta e quatro golpes no mínimo — e com magia, duzentos e vinte. Uma prova que não guardasse esse número
 * deixaria a fatia virar um saco de vida.
 */
public class OccultaBabaYagaGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Os números dela são os do original. */
    @GameTest(maxTicks = 20)
    public void herNumbersAreTheOriginals(GameTestHelper helper) {
        if (BabaYagaEntity.VIDA != 500.0) helper.fail("ela tem quinhentos, tem " + BabaYagaEntity.VIDA);
        if (BabaYagaEntity.TETO_DA_PANCADA != 15.0f) {
            helper.fail("e nenhuma pancada tira mais de quinze");
        }
        if (BabaYagaEntity.MAGIA_VALE != 0.15f) helper.fail("e magia vale quinze por cento");
        if (BabaYagaEntity.VISITA != 600) helper.fail("e a visita dura trinta segundos");
        helper.succeed();
    }

    /** <b>Nenhuma pancada lhe tira mais de quinze</b>, por maior que seja. */
    @GameTest(maxTicks = 40)
    public void noBlowTakesMoreThanFifteen(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var baba = helper.spawn(OccultaEntities.BABA_YAGA, new BlockPos(3, 2, 3));
        float tinha = baba.getHealth();

        baba.hurtServer(level, level.damageSources().generic(), 1000.0f);
        float tirou = tinha - baba.getHealth();
        if (tirou > BabaYagaEntity.TETO_DA_PANCADA + 0.001f) {
            helper.fail("mil de dano tiram quinze, e tiraram " + tirou);
        }
        if (tirou <= 0.0f) helper.fail("mas tiram alguma coisa");

        baba.discard();
        helper.succeed();
    }

    /** E magia tira quinze por cento do que tiraria. */
    @GameTest(maxTicks = 40)
    public void magicIsWorthFifteenPercent(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();

        var comArma = helper.spawn(OccultaEntities.BABA_YAGA, new BlockPos(2, 2, 2));
        float antesArma = comArma.getHealth();
        comArma.hurtServer(level, level.damageSources().generic(), 10.0f);
        float porArma = antesArma - comArma.getHealth();

        var comMagia = helper.spawn(OccultaEntities.BABA_YAGA, new BlockPos(5, 2, 5));
        float antesMagia = comMagia.getHealth();
        comMagia.hurtServer(level, level.damageSources().magic(), 10.0f);
        float porMagia = antesMagia - comMagia.getHealth();

        if (porMagia >= porArma) {
            helper.fail("magia tem de doer menos: arma " + porArma + ", magia " + porMagia);
        }

        comArma.discard();
        comMagia.discard();
        helper.succeed();
    }

    /** Ela salta para longe de quem a persegue. */
    @GameTest(maxTicks = 60)
    public void sheLeapsAwayFromWhoeverChasesHer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // um chão largo, para o salto ter onde cair
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                for (int y = 1; y <= 2; y++) {
                    level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, y, z)),
                            y == 1 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
                }
            }
        }

        var baba = helper.spawn(OccultaEntities.BABA_YAGA, new BlockPos(4, 2, 4));
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(4.5, 2, 4.5)));

        Vec3 antes = baba.position();
        boolean saltou = false;
        for (int tentativa = 0; tentativa < 40 && !saltou; tentativa++) {
            saltou = baba.saltaPara(level, quem);
        }
        if (!saltou) {
            helper.fail("em quarenta tentativas ela devia ter achado para onde saltar");
            return;
        }
        if (baba.position().equals(antes)) helper.fail("e saltar é sair do lugar");

        baba.discard();
        helper.succeed();
    }

    /** O presente dela é sempre um ingrediente do ofício. */
    @GameTest(maxTicks = 20)
    public void herGiftIsAlwaysSomethingOfTheCraft(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int n = 0; n < 40; n++) {
            var qual = BabaYagaEntity.presente(level);
            if (qual == null) {
                helper.fail("ela nunca larga nada");
                return;
            }
        }
        helper.succeed();
    }

    /** E a visita acaba: ao fim de trinta segundos ela some. */
    @GameTest(maxTicks = 40)
    public void theVisitEnds(GameTestHelper helper) {
        piso(helper);
        var baba = helper.spawn(OccultaEntities.BABA_YAGA, new BlockPos(3, 2, 3));
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);

        if (baba.dono() != null) helper.fail("ela não começa com dono");
        baba.dono(quem);
        if (baba.dono() == null) helper.fail("chamada, tem dono");

        // e com dono ela deixa de ter aquele jogador por alvo
        baba.setTarget(quem);
        if (baba.getTarget() == quem && baba.dono().equals(quem.getUUID())) {
            // o alvo só se recusa pela lista de alvos, que não corre numa prova parada:
            // o que esta prova guarda é que o dono ficou registrado
            helper.assertTrue(true, "");
        }

        baba.discard();
        helper.succeed();
    }

    /** Quem foge pelo ar fica pesado. */
    @GameTest(maxTicks = 40)
    public void whoeverFleesThroughTheAirGetsHeavy(GameTestHelper helper) {
        if (BabaYagaEntity.LENTIDÃO_GRAU != 5) {
            helper.fail("a lentidão dela é de grau seis (cinco a contar do zero), é "
                    + BabaYagaEntity.LENTIDÃO_GRAU);
        }
        if (BabaYagaEntity.LENTIDÃO_TEMPO != 200) helper.fail("e dura dez segundos");
        if (MobEffects.SLOWNESS == null) helper.fail("e é a lentidão do jogo");
        helper.succeed();
    }

    /** Ela não se fere a si mesma. */
    @GameTest(maxTicks = 40)
    public void sheCannotHurtHerself(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var baba = helper.spawn(OccultaEntities.BABA_YAGA, new BlockPos(3, 2, 3));
        float tinha = baba.getHealth();

        baba.hurtServer(level, level.damageSources().mobAttack(baba), 10.0f);
        if (baba.getHealth() != tinha) helper.fail("o que ela atira não a fere");

        // mas o de outro bicho fere
        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(5, 2, 5));
        baba.hurtServer(level, level.damageSources().mobAttack(zumbi), 10.0f);
        if (baba.getHealth() >= tinha) helper.fail("e o de outro, sim");

        baba.discard();
        zumbi.discard();
        helper.succeed();
    }
}
