package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.rite.RiteRegistry;
import net.thaumcraft.occulta.rite.Rites;

import java.util.List;

/**
 * A Maldição da Praga, e o chão que o anel procura.
 *
 * <p>Duas coisas aqui são mais do que a Praga. A primeira é que o anel que cresce passou a <b>procurar chão
 * para baixo</b> — faltava desde a fatia dos círculos, e descendo um barranco ele simplesmente não tocava no
 * chão. A segunda é que a <b>maestria da maldição</b>, a do gato, atravessa a maquinaria inteira: é a segunda
 * coisa que o gato destranca neste porte, e a primeira que se vê no chão.
 */
public class OccultaBlightGameTest {
    /** Ela está na lista. */
    @GameTest(maxTicks = 20)
    public void theBlightIsRegistered(GameTestHelper helper) {
        if (RiteRegistry.all().stream().noneMatch(r -> r.key().equals("tc.rite.curseblight"))) {
            helper.fail("falta o rito tc.rite.curseblight");
        }
        helper.succeed();
    }

    /** A grama e a terra secam: viram areia ou terra pelada. */
    @GameTest(maxTicks = 40)
    public void theGroundDriesToSandOrBareDirt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // vinte casas de grama, para o sorteio de um em cinco não ser acaso
        List<BlockPos> casas = new java.util.ArrayList<>();
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 4; z++) {
                BlockPos onde = helper.absolutePos(new BlockPos(x, 1, z));
                level.setBlockAndUpdate(onde, Blocks.GRASS_BLOCK.defaultBlockState());
                casas.add(onde);
            }
        }

        for (BlockPos onde : casas) {
            Rites.Blight.seca(level, onde, level.getBlockState(onde), false);
        }

        int secas = 0;
        for (BlockPos onde : casas) {
            var qualé = level.getBlockState(onde);
            if (qualé.is(Blocks.SAND) || qualé.is(Blocks.DIRT)) secas++;
            else if (!qualé.is(Blocks.GRASS_BLOCK)) {
                helper.fail("ou seca em areia e terra, ou fica grama — ficou " + qualé);
                return;
            }
        }
        if (secas == 0) helper.fail("em vinte casas, alguma devia ter secado");
        if (secas == casas.size()) helper.fail("mas não todas: é um sorteio, e não uma varredura");
        helper.succeed();
    }

    /** E o que não é chão de grama não seca: pedra fica pedra. */
    @GameTest(maxTicks = 20)
    public void stoneDoesNotDry(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 1, 2));
        level.setBlockAndUpdate(onde, Blocks.STONE.defaultBlockState());
        for (int i = 0; i < 40; i++) {
            Rites.Blight.seca(level, onde, level.getBlockState(onde), true);
        }
        if (!level.getBlockState(onde).is(Blocks.STONE)) helper.fail("pedra não seca");
        helper.succeed();
    }

    /** O aldeão vira zumbi, e o zumbi fica onde ele estava. */
    @GameTest(maxTicks = 40)
    public void aVillagerTurnsIntoAZombie(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
        var aldeão = helper.spawn(EntityTypes.VILLAGER, new BlockPos(3, 2, 3));
        var onde = aldeão.position();

        Rites.Blight.zumbifica(level, aldeão);
        if (aldeão.isAlive()) helper.fail("o aldeão sai");

        var zumbis = level.getEntitiesOfClass(net.minecraft.world.entity.monster.zombie.ZombieVillager.class,
                new net.minecraft.world.phys.AABB(onde, onde).inflate(2.0));
        if (zumbis.isEmpty()) {
            helper.fail("e um zumbi-aldeão fica no lugar dele");
            return;
        }
        zumbis.forEach(z -> z.discard());
        helper.succeed();
    }

    /**
     * E o gato morde mais fundo: um em cada <b>quatro</b>, e não um em cada cinco.
     *
     * <p>A prova mede o número, e não o sorteio — porque o sorteio é sorteio. O que ela guarda é a decisão:
     * a maestria da maldição muda <b>quanto</b>, e não <b>o quê</b>.
     */
    @GameTest(maxTicks = 20)
    public void theCatMakesItBiteDeeper(GameTestHelper helper) {
        if (Rites.Blight.CHÃO != 5) helper.fail("sem gato é um em cada cinco, é " + Rites.Blight.CHÃO);
        if (Rites.Blight.CHÃO_COM_GATO != 4) {
            helper.fail("com gato é um em cada quatro, é " + Rites.Blight.CHÃO_COM_GATO);
        }
        if (Rites.Blight.CHÃO_COM_GATO >= Rites.Blight.CHÃO) {
            helper.fail("e com gato tem de morder mais, não menos");
        }
        helper.succeed();
    }
}
