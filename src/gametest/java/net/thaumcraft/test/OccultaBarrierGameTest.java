package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.BarrierBlock;
import net.thaumcraft.occulta.BarrierBlockEntity;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.rite.ActiveRite;
import net.thaumcraft.occulta.rite.RiteRegistry;
import net.thaumcraft.occulta.rite.RiteStep;
import net.thaumcraft.occulta.rite.Rites;

import java.util.List;
import java.util.UUID;

/**
 * As barreiras: a casa que some sozinha, de quem ela é, e os três ritos que a erguem.
 */
public class OccultaBarrierGameTest {
    /** Uma casa de barreira dura trinta batidas e some. */
    @GameTest(maxTicks = 80)
    public void aBarrierBlockFadesOnItsOwn(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        BarrierBlock.put(level, onde, BarrierBlock.TICKS_TO_LIVE, false, null);

        if (!level.getBlockState(onde).is(OccultaBlocks.BARRIER)) {
            helper.fail("a casa de barreira devia ter sido posta");
            return;
        }
        if (!(level.getBlockEntity(onde) instanceof BarrierBlockEntity alma)) {
            helper.fail("e ter alma");
            return;
        }
        if (alma.remaining() != BarrierBlock.TICKS_TO_LIVE) {
            helper.fail("com a conta cheia, e tem " + alma.remaining());
        }

        helper.runAfterDelay(BarrierBlock.TICKS_TO_LIVE + 10, () -> {
            if (level.getBlockState(onde).is(OccultaBlocks.BARRIER)) {
                helper.fail("e sem ninguém a renovar, ela some");
                return;
            }
            helper.succeed();
        });
    }

    /** Renovada, ela fica: é o que o rito faz de vinte em vinte batidas. */
    @GameTest(maxTicks = 80)
    public void renewingKeepsItUp(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        BarrierBlock.put(level, onde, BarrierBlock.TICKS_TO_LIVE, false, null);

        helper.runAfterDelay(20, () -> {
            BarrierBlock.put(level, onde, BarrierBlock.TICKS_TO_LIVE, false, null);
            helper.runAfterDelay(20, () -> {
                if (!level.getBlockState(onde).is(OccultaBlocks.BARRIER)) {
                    helper.fail("renovada, a casa fica de pé");
                    return;
                }
                helper.succeed();
            });
        });
    }

    /** Uma barreira que trava gente deixa passar quem a ergueu, e não os outros. */
    @GameTest
    public void theBarrierKnowsItsOwner(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(4, 2, 4));
        var dono = helper.makeMockServerPlayerInLevel();
        BarrierBlock.put(level, onde, BarrierBlock.TICKS_TO_LIVE, true, dono.getUUID());

        if (!(level.getBlockEntity(onde) instanceof BarrierBlockEntity alma)) {
            helper.fail("a casa devia ter alma");
            return;
        }
        if (!alma.blocksPlayers()) helper.fail("e travar gente");
        if (!alma.lets(dono)) helper.fail("mas deixar passar quem a ergueu");

        var outro = helper.makeMockServerPlayerInLevel();
        if (alma.lets(outro)) helper.fail("e travar quem não a ergueu");

        // e uma que não trava gente deixa passar toda a gente
        BlockPos solta = helper.absolutePos(new BlockPos(5, 2, 5));
        BarrierBlock.put(level, solta, BarrierBlock.TICKS_TO_LIVE, false, dono.getUUID());
        if (level.getBlockEntity(solta) instanceof BarrierBlockEntity livre && !livre.lets(outro)) {
            helper.fail("uma barreira que não trava gente deixa passar quem for");
        }
        helper.succeed();
    }

    /** Os três ritos estão na lista, e o portátil é o único que não come poder. */
    @GameTest
    public void theThreeBarrierRitesAreRegistered(GameTestHelper helper) {
        record Par(String chave, float come) {
        }
        for (Par par : List.of(
                new Par("tc.rite.barrier", 1.2f),
                new Par("tc.rite.barrierlarge", 1.4f),
                new Par("tc.rite.barrierportable", 0.0f))) {
            var entrada = RiteRegistry.all().stream()
                    .filter(r -> r.key().equals(par.chave())).findFirst().orElse(null);
            if (entrada == null) {
                helper.fail("falta o rito " + par.chave());
                return;
            }
            if (!(entrada.rite() instanceof Rites.Barrier barreira)) {
                helper.fail(par.chave() + " devia ser uma barreira");
                return;
            }
            if (barreira.upkeep() != par.come()) {
                helper.fail(par.chave() + " come " + par.come() + " por batida, e come " + barreira.upkeep());
            }
            if (Rites.shown(entrada).stream().noneMatch(c -> c.is(Items.OBSIDIAN))) {
                helper.fail(par.chave() + " pede obsidiana");
            }
        }

        // e o portátil é o que pede a pedra carregada
        var portátil = RiteRegistry.all().stream()
                .filter(r -> r.key().equals("tc.rite.barrierportable")).findFirst().orElseThrow();
        if (Rites.shown(portátil).stream().noneMatch(c -> c.is(OccultaItems.ATTUNED_STONE_CHARGED))) {
            helper.fail("o portátil pede a pedra carregada");
        }
        helper.succeed();
    }

    /** O rito desenha a cúpula: chão, parede e teto de barreira em volta do círculo. */
    @GameTest(maxTicks = 80)
    public void theRiteDrawsTheDome(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(5, 3, 5));

        // o portátil não come poder, e por isso corre sem altar
        var qual = new Rites.Barrier(2, 2, 0.0f, false, 60);
        var rito = new ActiveRite("tc.rite.barrierportable", qual, List.of(), UUID.randomUUID(), 0);
        var passo = qual.steps(0).getFirst();
        if (passo.run(level, meio, 20L, rito) != RiteStep.Result.UPKEEP) {
            helper.fail("o rito devia erguer a cúpula e sustentar-se");
            return;
        }

        int casas = 0;
        for (BlockPos casa : BlockPos.betweenClosed(meio.offset(-3, -2, -3), meio.offset(3, 4, 3))) {
            if (level.getBlockState(casa).is(OccultaBlocks.BARRIER)) casas++;
        }
        if (casas < 20) helper.fail("a cúpula devia ter muitas casas, e tem " + casas);

        // e o meio fica vazio, que é o que faz dela um abrigo
        if (level.getBlockState(meio).is(OccultaBlocks.BARRIER)) {
            helper.fail("o meio da cúpula fica livre");
        }
        helper.succeed();
    }

    /** Sem altar por perto, a barreira que come poder morre em vez de ficar de graça. */
    @GameTest(maxTicks = 60)
    public void aBarrierWithoutPowerDies(GameTestHelper helper) {
        BlockPos meio = helper.absolutePos(new BlockPos(2, 3, 2));
        var qual = new Rites.Barrier(2, 2, 1.2f, false, 0);
        var rito = new ActiveRite("tc.rite.barrier", qual, List.of(), null, 0);
        var passo = qual.steps(0).getFirst();
        if (passo.run(helper.getLevel(), meio, 20L, rito) != RiteStep.Result.ABORTED) {
            helper.fail("sem altar, a barreira morre");
        }
        helper.succeed();
    }

    /** A barreira não se apanha nem se quebra de graça. */
    @GameTest
    public void theBarrierIsNotLoot(GameTestHelper helper) {
        var feitio = OccultaBlocks.BARRIER.defaultBlockState();
        BlockPos onde = helper.absolutePos(new BlockPos(1, 2, 1));
        if (feitio.getDestroySpeed(helper.getLevel(), onde) >= 0.0f) {
            helper.fail("ela não se quebra à mão");
        }
        if (OccultaBlocks.BARRIER.asItem() != Items.AIR) helper.fail("e não tem item");
        helper.succeed();
    }
}
