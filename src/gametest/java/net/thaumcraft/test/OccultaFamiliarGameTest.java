package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.familiar.FamiliarData;
import net.thaumcraft.occulta.familiar.FamiliarKind;
import net.thaumcraft.occulta.familiar.Familiars;

/**
 * O vínculo com um familiar: quem pode ser, o que cada um destranca, e a pancada que ele leva.
 */
public class OccultaFamiliarGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Os três números do original: gato um, sapo dois, coruja três. */
    @GameTest(maxTicks = 20)
    public void theThreeKindsKeepTheirNumbers(GameTestHelper helper) {
        if (FamiliarKind.CAT.numeroDoOriginal() != 1) helper.fail("o gato é o um");
        if (FamiliarKind.TOAD.numeroDoOriginal() != 2) helper.fail("o sapo é o dois");
        if (FamiliarKind.OWL.numeroDoOriginal() != 3) helper.fail("a coruja é a três");
        for (FamiliarKind qual : FamiliarKind.values()) {
            if (qual.quantosNomes() != 12) {
                helper.fail(qual + " tem doze nomes no original, tem " + qual.quantosNomes());
            }
        }
        helper.succeed();
    }

    /**
     * <b>Só bicho domado vira familiar</b>, e só os três feitios que o original aceita.
     *
     * <p>O lobo é a prova pelo avesso: é domável, é do jogo, e <b>não</b> serve — no original a lista é
     * fechada, e aqui também.
     */
    @GameTest(maxTicks = 20)
    public void onlyTamedAnimalsOfTheRightKindCanBind(GameTestHelper helper) {
        piso(helper);
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));

        var sapo = helper.spawn(OccultaEntities.TOAD, new BlockPos(2, 2, 3));
        if (Familiars.podeVirar(sapo)) helper.fail("sapo por domar não vira familiar");
        sapo.tame(quem);
        if (!Familiars.podeVirar(sapo)) helper.fail("domado, vira");
        if (Familiars.deQueFeitio(sapo) != FamiliarKind.TOAD) helper.fail("e é do feitio sapo");

        var lobo = helper.spawn(EntityTypes.WOLF, new BlockPos(4, 2, 3));
        lobo.tame(quem);
        if (Familiars.deQueFeitio(lobo) != null) helper.fail("lobo não é familiar de ninguém");
        if (Familiars.podeVirar(lobo)) helper.fail("nem domado");

        sapo.discard();
        lobo.discard();
        helper.succeed();
    }

    /** Vincular dá nome ao bicho, e <b>um de cada vez</b>. */
    @GameTest(maxTicks = 20)
    public void bindingNamesTheAnimalAndOnlyOneAtATime(GameTestHelper helper) {
        piso(helper);
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));
        if (Familiars.temAlgum(quem)) helper.fail("ninguém começa com familiar");

        var sapo = helper.spawn(OccultaEntities.TOAD, new BlockPos(2, 2, 3));
        sapo.tame(quem);
        if (!Familiars.vincula(quem, sapo)) helper.fail("o primeiro vínculo pega");
        if (!Familiars.temAlgum(quem)) helper.fail("e passa a ter familiar");
        if (!sapo.hasCustomName()) helper.fail("e o bicho ganha nome");
        if (FamiliarData.of(quem).kind().orElse(null) != FamiliarKind.TOAD) {
            helper.fail("e o familiar é um sapo");
        }

        var coruja = helper.spawn(OccultaEntities.OWL, new BlockPos(4, 2, 3));
        coruja.tame(quem);
        if (Familiars.vincula(quem, coruja)) helper.fail("o segundo não pega: é um de cada vez");
        if (FamiliarData.of(quem).kind().orElse(null) != FamiliarKind.TOAD) {
            helper.fail("e o primeiro continua sendo o familiar");
        }

        sapo.discard();
        coruja.discard();
        helper.succeed();
    }

    /**
     * Cada bicho destranca a sua maestria, e só a sua.
     *
     * <p>É isto que faz escolher um familiar ser uma escolha: o gato dá o escuro mais longo, o sapo o frasco
     * a mais, e a coruja a da vassoura — que ainda não tem vassoura.
     */
    @GameTest(maxTicks = 20)
    public void eachKindUnlocksItsOwnMasteryAndNoOther(GameTestHelper helper) {
        piso(helper);
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));

        if (Familiars.temMaestriaDeMaldicao(quem)) helper.fail("sem familiar não há maestria nenhuma");

        var sapo = helper.spawn(OccultaEntities.TOAD, new BlockPos(2, 2, 3));
        sapo.tame(quem);
        Familiars.vincula(quem, sapo);
        if (!Familiars.temMaestriaDeCozimento(quem)) helper.fail("o sapo dá a do cozimento");
        if (Familiars.temMaestriaDeMaldicao(quem)) helper.fail("e não a da maldição");
        if (Familiars.temMaestriaDeVassoura(quem)) helper.fail("nem a da vassoura");

        Familiars.desfaz(quem);
        var coruja = helper.spawn(OccultaEntities.OWL, new BlockPos(4, 2, 3));
        coruja.tame(quem);
        Familiars.vincula(quem, coruja);
        if (!Familiars.temMaestriaDeVassoura(quem)) helper.fail("a coruja dá a da vassoura");
        if (Familiars.temMaestriaDeCozimento(quem)) helper.fail("e não a do cozimento");

        sapo.discard();
        coruja.discard();
        helper.succeed();
    }

    /** E quem cai perde o fio: o bicho fica no mundo, solto. */
    @GameTest(maxTicks = 20)
    public void dyingBreaksTheBondButNotTheAnimal(GameTestHelper helper) {
        piso(helper);
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));

        var sapo = helper.spawn(OccultaEntities.TOAD, new BlockPos(2, 2, 3));
        sapo.tame(quem);
        Familiars.vincula(quem, sapo);
        if (!Familiars.temAlgum(quem)) helper.fail("vinculado");

        Familiars.doneMorreu(quem);
        if (Familiars.temAlgum(quem)) helper.fail("quem cai perde o fio");
        if (!sapo.isAlive()) helper.fail("mas o bicho fica");

        sapo.discard();
        helper.succeed();
    }
}
