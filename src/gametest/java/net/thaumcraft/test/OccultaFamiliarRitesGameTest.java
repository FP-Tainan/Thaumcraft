package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.familiar.FamiliarData;
import net.thaumcraft.occulta.familiar.FamiliarKind;
import net.thaumcraft.occulta.familiar.Familiars;
import net.thaumcraft.occulta.rite.RiteStep;
import net.thaumcraft.occulta.rite.Rites;

/**
 * Os <b>dois ritos de familiar</b>: o que prende um bicho domado ao dono e o que chama o bicho de volta.
 */
public class OccultaFamiliarRitesGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /**
     * <b>O rito prende o bicho ao dono</b>, e os dois têm de estar dentro do cubo.
     *
     * <p>Sem bicho nenhum que sirva, ele <b>devolve</b> — que é como um círculo preparado em vão não
     * custa nada a quem o preparou.
     */
    @GameTest(maxTicks = 40)
    public void theRiteBindsTheTamedBeastToItsOwner(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(3, 2, 3));

        var rito = new Rites.BindFamiliar(Rites.FAMILIAR_A);
        var passo = rito.steps(0).getFirst();

        if (passo.run(level, meio, 20L, null) != RiteStep.Result.ABORTED_REFUND) {
            helper.fail("sem bicho nenhum, ele devolve");
        }

        var quem = helper.makeMockServerPlayerInLevel();
        quem.snapTo(new Vec3(meio.getX() + 0.5, meio.getY(), meio.getZ() + 0.5));
        var sapo = helper.spawn(net.thaumcraft.occulta.OccultaEntities.TOAD, new BlockPos(4, 2, 3));
        sapo.setNoAi(true);

        if (passo.run(level, meio, 20L, null) != RiteStep.Result.ABORTED_REFUND) {
            helper.fail("um sapo por domar não serve");
        }

        sapo.tame(quem);
        if (passo.run(level, meio, 20L, null) != RiteStep.Result.COMPLETED) {
            helper.fail("domado, ele prende");
            return;
        }
        if (!Familiars.temAlgum(quem)) helper.fail("e quem o domou fica com familiar");
        if (Familiars.qual(quem).orElse(null) != FamiliarKind.TOAD) helper.fail("do feitio sapo");

        // e segunda vez não prende outro, porque já há um
        if (passo.run(level, meio, 20L, null) != RiteStep.Result.ABORTED_REFUND) {
            helper.fail("quem já tem familiar não leva outro");
        }

        Familiars.desfaz(quem);
        sapo.discard();
        helper.succeed();
    }

    /**
     * <b>E o de chamar traz o bicho para junto de quem o chamou</b> — e, não o havendo no mundo, faz
     * nascer um no círculo com o nome que tinha.
     */
    @GameTest(maxTicks = 40)
    public void theCallingRiteBringsTheBeastBackOrMakesANewOne(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(3, 2, 5));

        var rito = new Rites.CallFamiliar(Rites.FAMILIAR_A);
        var passo = rito.steps(0).getFirst();

        var quem = helper.makeMockServerPlayerInLevel();
        quem.snapTo(new Vec3(meio.getX() + 0.5, meio.getY(), meio.getZ() + 0.5));
        Familiars.desfaz(quem);

        if (passo.run(level, meio, 20L, null) != RiteStep.Result.ABORTED_REFUND) {
            helper.fail("sem ninguém com familiar, ele devolve");
        }

        // com o bicho no mundo, ele vem ter com quem o chamou
        var coruja = helper.spawn(net.thaumcraft.occulta.OccultaEntities.OWL, new BlockPos(7, 2, 7));
        coruja.setNoAi(true);
        coruja.tame(quem);
        if (!Familiars.vincula(quem, coruja)) {
            helper.fail("a coruja domada vira familiar");
            return;
        }
        double longe = coruja.distanceToSqr(quem);
        if (passo.run(level, meio, 20L, null) != RiteStep.Result.COMPLETED) {
            helper.fail("havendo familiar, o rito acaba");
            return;
        }
        if (coruja.distanceToSqr(quem) >= longe) helper.fail("e a coruja vem ter com ela");

        /*
         * E o outro lado: dizendo-se que o bicho já não está no mundo, o rito faz nascer outro — com o
         * mesmo nome, e do mesmo feitio.
         */
        String nome = FamiliarData.of(quem).nome();
        coruja.discard();
        Familiars.foiEmbora(quem);
        if (passo.run(level, meio, 20L, null) != RiteStep.Result.COMPLETED) {
            helper.fail("sem o bicho no mundo, ele faz nascer outro");
            return;
        }
        var nova = Familiars.acha(level, quem);
        if (nova == null) helper.fail("e o novo fica no mundo");
        else {
            if (!nova.hasCustomName() || !nova.getName().getString().equals(nome)) {
                helper.fail("com o nome que tinha");
            }
            nova.discard();
        }

        Familiars.desfaz(quem);
        helper.succeed();
    }
}
