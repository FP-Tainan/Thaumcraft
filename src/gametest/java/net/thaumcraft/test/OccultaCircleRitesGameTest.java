package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.rite.ActiveRite;
import net.thaumcraft.occulta.rite.RiteRegistry;
import net.thaumcraft.occulta.rite.RiteStep;
import net.thaumcraft.occulta.rite.Rites;

import java.util.List;

/**
 * Os quatro que mexem no próprio círculo: empurrar, puxar, trazer minério e repintar o giz.
 */
public class OccultaCircleRitesGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Os quatro estão na lista. */
    @GameTest(maxTicks = 20)
    public void theFourAreRegistered(GameTestHelper helper) {
        for (String chave : List.of("tc.rite.protection", "tc.rite.imprisonment",
                "tc.rite.teleportironore", "tc.rite.glyphictransform")) {
            if (RiteRegistry.all().stream().noneMatch(r -> r.key().equals(chave))) {
                helper.fail("falta o rito " + chave);
                return;
            }
        }
        helper.succeed();
    }

    /** O de Proteção empurra o bicho para fora — e <b>não mexe em gente</b>. */
    @GameTest(maxTicks = 40)
    public void protectionPushesBeastsAndNotPeople(GameTestHelper helper) {
        piso(helper);
        BlockPos meio = helper.absolutePos(new BlockPos(3, 2, 3));

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(4, 2, 3));
        porco.setDeltaMovement(Vec3.ZERO);
        Rites.PushCircle.empurra(porco, meio.getX(), meio.getY(), meio.getZ());
        if (porco.getDeltaMovement().horizontalDistanceSqr() <= 0.0) {
            helper.fail("o bicho é empurrado");
        }
        // e para FORA: o porco está a leste do meio, e tem de ir mais para leste
        if (porco.getDeltaMovement().x <= 0.0) {
            helper.fail("e para longe do meio, e foi " + porco.getDeltaMovement().x);
        }

        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(4.5, 2, 3.5)));
        quem.setDeltaMovement(Vec3.ZERO);
        Rites.PushCircle.empurra(quem, meio.getX(), meio.getY(), meio.getZ());
        if (!quem.getDeltaMovement().equals(Vec3.ZERO)) {
            helper.fail("gente não se mexe: um anel que empurrasse o dono seria armadilha para ele");
        }

        porco.discard();
        helper.succeed();
    }

    /** O de Aprisionamento só puxa quem está <b>na borda</b>: quem está no meio fica quieto. */
    @GameTest(maxTicks = 40)
    public void imprisonmentOnlyPullsFromTheEdge(GameTestHelper helper) {
        piso(helper);
        BlockPos meio = helper.absolutePos(new BlockPos(3, 2, 3));

        var doMeio = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        doMeio.setDeltaMovement(Vec3.ZERO);
        Rites.PushCircle.puxa(doMeio, meio.getX(), meio.getY(), meio.getZ(), 4);
        if (!doMeio.getDeltaMovement().equals(Vec3.ZERO)) {
            helper.fail("quem está no meio fica quieto, senão o anel cospe os bichos e eles saltam");
        }

        var daBorda = helper.spawn(EntityTypes.PIG, new BlockPos(6, 2, 3));
        daBorda.setDeltaMovement(Vec3.ZERO);
        Rites.PushCircle.puxa(daBorda, meio.getX(), meio.getY(), meio.getZ(), 4);
        if (daBorda.getDeltaMovement().horizontalDistanceSqr() <= 0.0) {
            helper.fail("quem está na borda é puxado");
        }
        // e para DENTRO: o porco está a leste, e tem de vir para oeste
        if (daBorda.getDeltaMovement().x >= 0.0) {
            helper.fail("e para o meio, e foi " + daBorda.getDeltaMovement().x);
        }
        if (daBorda.getDeltaMovement().y != 0.0) helper.fail("sem subir: o puxão zera o de cima");

        doMeio.discard();
        daBorda.discard();
        helper.succeed();
    }

    /** O dos minérios arranca o que está por baixo e põe em cima — e o ouro só com coven cheio. */
    @GameTest(maxTicks = 60)
    public void oresComeUpAndGoldOnlyWithAFullCoven(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(3, 3, 3));
        level.setBlockAndUpdate(meio.below(), Blocks.IRON_ORE.defaultBlockState());
        level.setBlockAndUpdate(meio.below().east(), Blocks.GOLD_ORE.defaultBlockState());

        var rito = new Rites.TransposeOres(2, 30, List.of(Blocks.IRON_ORE, Blocks.GOLD_ORE));
        var sozinha = new ActiveRite("tc.rite.teleportironore", rito, List.of(), null, 0);
        rito.steps(0).getFirst().run(level, meio, 10L, sozinha);

        if (level.getBlockState(meio.below()).is(Blocks.IRON_ORE)) helper.fail("o ferro sobe");
        if (!level.getBlockState(meio.below().east()).is(Blocks.GOLD_ORE)) {
            helper.fail("e o ouro fica, porque o coven está vazio");
        }

        var cheio = new ActiveRite("tc.rite.teleportironore", rito, List.of(), null, 6);
        rito.steps(0).getFirst().run(level, meio, 10L, cheio);
        if (level.getBlockState(meio.below().east()).is(Blocks.GOLD_ORE)) {
            helper.fail("com seis bruxas o ouro sobe também");
        }
        helper.succeed();
    }

    /** E o giz largado repinta o anel: um giz muda o de dentro. */
    @GameTest(maxTicks = 60)
    public void chalkRepaintsTheRing(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(4, 2, 4));

        // uma casa do anel de dentro, pelo desenho do original: três para o norte do meio
        BlockPos doAnel = meio.north(3);
        level.setBlockAndUpdate(doAnel, OccultaBlocks.RITUAL_GLYPH.defaultBlockState());

        var largado = new ItemEntity(level, meio.getX() + 0.5, meio.getY() + 0.5, meio.getZ() + 0.5,
                new ItemStack(OccultaItems.INFERNAL_CHALK, 1));
        level.addFreshEntity(largado);

        var rito = new Rites.GlyphicTransformation();
        var corrido = new ActiveRite("tc.rite.glyphictransform", rito, List.of(), null, 0);
        RiteStep.Result saiu = rito.steps(0).getFirst().run(level, meio, 30L, corrido);
        if (saiu != RiteStep.Result.COMPLETED) {
            helper.fail("com giz no chão ele repinta, e deu " + saiu);
            return;
        }
        if (!level.getBlockState(doAnel).is(OccultaBlocks.INFERNAL_GLYPH)) {
            helper.fail("o glifo do anel de dentro vira do giz que se largou, e é "
                    + level.getBlockState(doAnel));
        }
        helper.succeed();
    }

    /** Sem giz nenhum, ele desiste e devolve. */
    @GameTest(maxTicks = 40)
    public void withoutChalkItRefunds(GameTestHelper helper) {
        piso(helper);
        var rito = new Rites.GlyphicTransformation();
        var corrido = new ActiveRite("tc.rite.glyphictransform", rito, List.of(), null, 0);
        RiteStep.Result saiu = rito.steps(0).getFirst()
                .run(helper.getLevel(), helper.absolutePos(new BlockPos(4, 2, 4)), 30L, corrido);
        if (saiu != RiteStep.Result.ABORTED_REFUND) {
            helper.fail("sem giz ele desiste e devolve, e deu " + saiu);
        }
        helper.succeed();
    }
}
