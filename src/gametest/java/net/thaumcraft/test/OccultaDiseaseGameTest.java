package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.DiseaseBlock;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaEffects;

/**
 * A <b>poça de doença</b>: o rasto de quem está doente, que cai, seca e contagia.
 */
public class OccultaDiseaseGameTest {
    /** <b>Ela cai até achar chão</b>, e não se abre para os lados. */
    @GameTest(maxTicks = 80, padding = 8)
    public void theDiseaseFallsUntilItFindsGround(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos chão = helper.absolutePos(new BlockPos(3, 2, 3));
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                level.setBlockAndUpdate(chão.offset(dx, 0, dz), Blocks.STONE.defaultBlockState());
                for (int dy = 1; dy <= 5; dy++) {
                    level.setBlockAndUpdate(chão.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                }
            }
        }
        level.setBlockAndUpdate(chão.above(4), OccultaBlocks.DISEASE.defaultBlockState());

        helper.runAfterDelay(70, () -> {
            boolean emBaixo = level.getBlockState(chão.above()).is(OccultaBlocks.DISEASE);
            boolean secou = true;
            int aoLado = 0;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    for (int dy = 1; dy <= 5; dy++) {
                        if (!level.getBlockState(chão.offset(dx, dy, dz)).is(OccultaBlocks.DISEASE)) continue;
                        secou = false;
                        if (dx != 0 || dz != 0) aoLado++;
                    }
                }
            }
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    for (int dy = 0; dy <= 5; dy++) {
                        level.setBlockAndUpdate(chão.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                    }
                }
            }
            if (aoLado > 0) helper.fail("ela não se abre para os lados, e abriu-se para " + aoLado);
            // ou ela chegou ao chão, ou secou pelo caminho — as duas são dela, e nenhuma é ficar no ar
            else if (!emBaixo && !secou) helper.fail("ela cai até achar chão");
            else helper.succeed();
        });
    }

    /**
     * <b>Quem pisa nela adoece</b> — e o que não tem o que adoecer passa por ela sem sentir.
     *
     * <p>Os dois ficam num <b>curral</b> de três por três com teto, e não por capricho: um bicho com a
     * cabeça desligada <b>não anda</b>, e o jogo só pergunta em que bloco alguém está quando esse alguém se
     * mexe. Com a cabeça ligada eles andam — e com o curral, andam só por cima da poça.
     */
    @GameTest(maxTicks = 140, padding = 10)
    public void whoeverStepsInItFallsIll(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos perto = new BlockPos(3, 2, 3);
        BlockPos meio = helper.absolutePos(perto);

        // o curral: chão de cinco por cinco, paredes na beira, teto por cima
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                boolean beira = dx == -2 || dx == 2 || dz == -2 || dz == 2;
                level.setBlockAndUpdate(meio.offset(dx, 0, dz), Blocks.STONE.defaultBlockState());
                for (int dy = 1; dy <= 2; dy++) {
                    level.setBlockAndUpdate(meio.offset(dx, dy, dz),
                            beira ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
                }
                level.setBlockAndUpdate(meio.offset(dx, 3, dz), Blocks.STONE.defaultBlockState());
            }
        }

        var porco = helper.spawn(EntityTypes.PIG, perto.above());
        var esqueleto = helper.spawn(EntityTypes.SKELETON, perto.above());
        if (!DiseaseBlock.imune(esqueleto)) helper.fail("um esqueleto não tem o que adoecer");
        if (DiseaseBlock.imune(porco)) helper.fail("e um porco tem");

        // a poça é reposta de três em três batidas, como estaria debaixo de quem anda doente
        for (int i = 1; i < 120; i += 3) {
            helper.runAfterDelay(i, () -> enche(helper, meio));
        }

        helper.runAfterDelay(125, () -> {
            boolean pegou = porco.hasEffect(OccultaEffects.DISEASED);
            boolean oEsqueleto = esqueleto.hasEffect(OccultaEffects.DISEASED);
            porco.discard();
            esqueleto.discard();
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    for (int dy = 0; dy <= 3; dy++) {
                        level.setBlockAndUpdate(meio.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                    }
                }
            }
            if (!pegou) helper.fail("quem pisa na poça adoece");
            else if (oEsqueleto) helper.fail("e um esqueleto não");
            else helper.succeed();
        });
    }

    /** Enche o chão do curral de poça. */
    private static void enche(GameTestHelper helper, BlockPos meio) {
        var level = helper.getLevel();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos onde = meio.offset(dx, 1, dz);
                if (level.getBlockState(onde).isAir()) {
                    level.setBlockAndUpdate(onde, OccultaBlocks.DISEASE.defaultBlockState());
                }
            }
        }
    }
}
