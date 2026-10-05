package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.enslave.Enslavement;
import net.thaumcraft.occulta.infusion.InfernalInfusion;
import net.thaumcraft.occulta.infusion.Infusions;
import net.thaumcraft.occulta.infusion.beast.CreaturePower;
import net.thaumcraft.occulta.infusion.beast.CreaturePowers;
import net.thaumcraft.occulta.infusion.beast.SkeletonPower;

/**
 * Os <b>poderes de bicho</b>: o ramo da Infusão Infernal em que não se ganham poderes, <b>tiram-se de quem
 * os tem</b>.
 *
 * <p>A prova que carrega a fatia é a do <b>sacrifício</b>: toma-se um bicho para si, mata-se, e o que ele
 * sabia fazer passa a ser seu.
 */
public class OccultaBeastPowerGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** São vinte e cinco, com os números do original. */
    @GameTest
    public void thereAreTwentyFiveOfThem(GameTestHelper helper) {
        if (CreaturePowers.quantos() != 25) {
            helper.fail("são vinte e cinco poderes de bicho; há " + CreaturePowers.quantos());
        }
        for (int id = 1; id <= 25; id++) {
            if (CreaturePowers.daquele(id) == null) helper.fail("falta o de número " + id);
        }

        // e o de capoeira dá uma carga, não dez
        CreaturePower ovelha = CreaturePowers.daquele(18);
        if (ovelha.porBicho() != 1) helper.fail("a ovelha dá uma carga; dá " + ovelha.porBicho());
        CreaturePower aldeão = CreaturePowers.daquele(22);
        if (aldeão.porBicho() != 2) helper.fail("e o aldeão duas; dá " + aldeão.porBicho());
        CreaturePower ghast = CreaturePowers.daquele(6);
        if (ghast.porBicho() != CreaturePower.POR_BICHO) {
            helper.fail("e o ghast dez; dá " + ghast.porBicho());
        }
        helper.succeed();
    }

    /** Cada poder sai do bicho certo. */
    @GameTest
    public void eachPowerComesFromItsBeast(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(2, 2, 2));
        var poder = CreaturePowers.de(zumbi);
        if (poder == null || poder.id != 9) helper.fail("o zumbi dá o poder nove; deu " + poder);
        zumbi.discard();

        var ovelha = helper.spawn(EntityTypes.SHEEP, new BlockPos(4, 2, 4));
        var outro = CreaturePowers.de(ovelha);
        if (outro == null || outro.id != 18) helper.fail("e a ovelha o dezoito; deu " + outro);
        ovelha.discard();
        helper.succeed();
    }

    /**
     * <b>O sacrifício: toma-se o bicho, mata-se, e o poder dele é seu.</b>
     *
     * <p>Esta é a prova que carrega a fatia.
     */
    @GameTest(maxTicks = 60)
    public void theSacrificeTakesThePower(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        piso(helper);
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(
                net.minecraft.world.level.GameType.SURVIVAL);
        quem.setPos(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5);
        quem.removeAttached(CreaturePowers.BICHO);
        Infusions.infunde(quem, Infusions.daquele(4), Infusions.CARGAS);

        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(4, 2, 4));
        var mão = new ItemStack(OccultaItems.WITCH_HAND);
        quem.setShiftKeyDown(true);

        // o primeiro soco toma-o para si
        Infusions.de(quem).soca(level, quem, mão, zumbi);
        if (!Enslavement.escravoDe(zumbi, quem)) helper.fail("o primeiro soco devia tomá-lo para si");
        if (CreaturePowers.dele(quem) != null) helper.fail("e não lhe dá poder nenhum ainda");

        // e o segundo mata-o e toma-lhe o poder
        Infusions.de(quem).soca(level, quem, mão, zumbi);
        var poder = CreaturePowers.dele(quem);
        if (poder == null || poder.id != 9) {
            helper.fail("o segundo soco devia dar-lhe o poder do zumbi; deu " + poder);
        }
        if (CreaturePowers.cargas(quem) != CreaturePower.POR_BICHO) {
            helper.fail("com dez cargas; veio " + CreaturePowers.cargas(quem));
        }
        if (zumbi.isAlive()) helper.fail("e o zumbi devia morrer disso");
        helper.succeed();
    }

    /**
     * <b>Tomar outro poder apaga o que se tinha; tomar o mesmo soma até vinte.</b>
     *
     * <p>Um de cada vez, e é essa a escolha que o ramo pede.
     */
    @GameTest
    public void takingAnotherErasesTheOne(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.removeAttached(CreaturePowers.BICHO);

        CreaturePowers.toma(quem, CreaturePowers.daquele(9));
        if (CreaturePowers.cargas(quem) != 10) helper.fail("o zumbi dá dez");

        // o mesmo outra vez soma, mas não passa de vinte
        CreaturePowers.toma(quem, CreaturePowers.daquele(9));
        if (CreaturePowers.cargas(quem) != CreaturePowers.TETO) {
            helper.fail("dez e dez dão vinte, que é o teto; veio " + CreaturePowers.cargas(quem));
        }
        CreaturePowers.toma(quem, CreaturePowers.daquele(9));
        if (CreaturePowers.cargas(quem) != CreaturePowers.TETO) helper.fail("e não passa dele");

        // e outro apaga o que havia
        CreaturePowers.toma(quem, CreaturePowers.daquele(18));
        if (CreaturePowers.dele(quem).id != 18) helper.fail("outro bicho troca o poder");
        if (CreaturePowers.cargas(quem) != 1) {
            helper.fail("e a ovelha dá uma só; veio " + CreaturePowers.cargas(quem));
        }
        quem.removeAttached(CreaturePowers.BICHO);
        helper.succeed();
    }

    /** Os números dos poderes são os do original. */
    @GameTest
    public void theirNumbersAreTheOriginals(GameTestHelper helper) {
        if (CreaturePowers.TETO != 20) helper.fail("o teto de cargas de bicho é vinte");
        if (InfernalInfusion.CUSTO_SACRIFICAR != 1) helper.fail("e o sacrifício custa uma de infusão");

        // a força do arco do esqueleto é a do jogo
        if (SkeletonPower.força(20) < 1.0f) helper.fail("ao segundo cheio a flecha sai crítica");
        if (SkeletonPower.força(0) != 0.0f) helper.fail("e sem tempo ela não sai");

        var creeper = (net.thaumcraft.occulta.infusion.beast.CreeperPower)
                CreaturePowers.daquele(3);
        if (creeper.custo(0) != 1 || creeper.custo(60) != 2) {
            helper.fail("o creeper custa uma, e duas se se segurar três segundos");
        }
        helper.succeed();
    }
}
