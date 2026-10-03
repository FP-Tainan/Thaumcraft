package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.BloodCrucibleBlockEntity;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.vampire.VampireEntity;

/**
 * O Vampiro: o gole, o crisol, e a espada que não o mata.
 *
 * <p>A prova que carrega a fatia é a última. Um vampiro que morresse de espada seria um aldeão hostil; o que
 * faz dele o que é são as formas de morrer que ele <b>não</b> tem.
 */
public class OccultaVampireGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Os números dele são os do original. */
    @GameTest(maxTicks = 20)
    public void hisNumbersAreTheOriginals(GameTestHelper helper) {
        if (VampireEntity.JANTAR != 20.0f) helper.fail("o jantar são vinte, é " + VampireEntity.JANTAR);
        if (VampireEntity.POR_GOLE != 4.0f) helper.fail("cada gole vale quatro");
        if (VampireEntity.CHANCE_DO_GOLE != 10) helper.fail("uma mordida em cada dez é um gole");
        if (VampireEntity.ACHA_O_CRISOL != 6) helper.fail("e o crisol está a seis blocos do caixão");
        helper.succeed();
    }

    /** O gole cura e conta para o jantar. */
    @GameTest(maxTicks = 40)
    public void aDrinkHealsAndCounts(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var vampiro = helper.spawn(OccultaEntities.VAMPIRE, new BlockPos(3, 2, 3));
        var aldeão = helper.spawn(EntityTypes.VILLAGER, new BlockPos(4, 2, 3));

        vampiro.setHealth(10.0f);
        float tinha = vampiro.getHealth();
        float bebido = vampiro.bebido();

        vampiro.bebe(level, aldeão);
        if (vampiro.getHealth() <= tinha) helper.fail("o gole cura");
        if (vampiro.bebido() != bebido + VampireEntity.POR_GOLE) {
            helper.fail("e conta quatro para o jantar, contou " + vampiro.bebido());
        }

        vampiro.discard();
        aldeão.discard();
        helper.succeed();
    }

    /** Cheio, ele enche o Crisol de Sangue que estiver perto do caixão. */
    @GameTest(maxTicks = 40)
    public void whenFullHeFeedsTheCrucible(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(4, 2, 4));
        level.setBlockAndUpdate(onde, OccultaBlocks.BLOOD_CRUCIBLE.defaultBlockState());
        if (!(level.getBlockEntity(onde) instanceof BloodCrucibleBlockEntity crisol)) {
            helper.fail("devia haver crisol");
            return;
        }
        float antes = crisol.filled();

        var vampiro = helper.spawn(OccultaEntities.VAMPIRE, new BlockPos(2, 2, 2));
        vampiro.caixão(helper.absolutePos(new BlockPos(2, 2, 2)));
        if (!vampiro.enchaOCrisol(level)) helper.fail("ele acha o crisol a seis blocos do caixão");
        if (crisol.filled() <= antes) helper.fail("e põe sangue nele");

        vampiro.discard();
        helper.succeed();
    }

    /**
     * <b>Uma espada não o mata.</b>
     *
     * <p>Ele leva o dano, cai a zero — e fica. Só o fogo, o sol, a parede, o vazio e a mão de outro
     * vampiro, de um lobisomem ou de um chefe o levam.
     */
    @GameTest(maxTicks = 40)
    public void aSwordDoesNotKillHim(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var vampiro = helper.spawn(OccultaEntities.VAMPIRE, new BlockPos(3, 2, 3));

        vampiro.hurtServer(level, level.damageSources().generic(), 1000.0f);
        if (!vampiro.isAlive()) {
            helper.fail("uma espada não o mata: ele leva o dano e fica");
            return;
        }
        if (vampiro.getHealth() <= 0.0f) helper.fail("e fica com alguma vida");

        vampiro.discard();
        helper.succeed();
    }

    /** Mas o fogo mata. */
    @GameTest(maxTicks = 40)
    public void fireDoesKillHim(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var vampiro = helper.spawn(OccultaEntities.VAMPIRE, new BlockPos(3, 2, 3));

        if (!VampireEntity.podeMorrer(vampiro, level.damageSources().onFire())) {
            helper.fail("o fogo mata um vampiro");
        }
        if (!VampireEntity.podeMorrer(vampiro, level.damageSources().inWall())) {
            helper.fail("e sufocar também");
        }
        if (!VampireEntity.podeMorrer(vampiro, level.damageSources().fellOutOfWorld())) {
            helper.fail("e o vazio");
        }
        if (VampireEntity.podeMorrer(vampiro, level.damageSources().magic())) {
            helper.fail("mas magia não");
        }

        vampiro.hurtServer(level, level.damageSources().onFire(), 1000.0f);
        if (vampiro.isAlive()) helper.fail("e com fogo ele cai mesmo");
        helper.succeed();
    }

    /** E outro vampiro mata: é a única mão viva que lhe chega. */
    @GameTest(maxTicks = 40)
    public void anotherVampireCanKillHim(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var um = helper.spawn(OccultaEntities.VAMPIRE, new BlockPos(2, 2, 2));
        var outro = helper.spawn(OccultaEntities.VAMPIRE, new BlockPos(5, 2, 5));

        if (!VampireEntity.podeMorrer(um, level.damageSources().mobAttack(outro))) {
            helper.fail("outro vampiro mata-o");
        }
        var aldeão = helper.spawn(EntityTypes.VILLAGER, new BlockPos(4, 2, 4));
        if (VampireEntity.podeMorrer(um, level.damageSources().mobAttack(aldeão))) {
            helper.fail("mas um aldeão não");
        }

        um.discard();
        outro.discard();
        aldeão.discard();
        helper.succeed();
    }
}
