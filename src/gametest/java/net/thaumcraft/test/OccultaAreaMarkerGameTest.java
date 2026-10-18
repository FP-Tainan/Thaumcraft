package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.rite.AreaMarkers;

/**
 * Os <b>dois marcadores de área</b>: o que cada um abafa, até onde, e quem perde a proteção.
 */
public class OccultaAreaMarkerGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 6; x++) {
            for (int z = 0; z < 6; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /**
     * <b>Cada um abafa o seu, e só o seu.</b> O lobo vale contra a maldição dirigida e não vale contra o
     * teleporte; a mandrágora é o avesso.
     */
    @GameTest(maxTicks = 40)
    public void eachMarkerDampensItsOwnRiteAndNoOther(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        BlockPos lobo = helper.absolutePos(new BlockPos(2, 2, 2));
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 3.5)));

        if (AreaMarkers.protege(quem, AreaMarkers.Kind.CURSE)) {
            helper.fail("sem marcador nenhum, ninguém está protegido");
        }

        level.setBlockAndUpdate(lobo, OccultaBlocks.WOLF_MARKER.defaultBlockState());
        if (!AreaMarkers.protege(quem, AreaMarkers.Kind.CURSE)) {
            helper.fail("com o lobo perto, a maldição dirigida é abafada");
        }
        if (AreaMarkers.protege(quem, AreaMarkers.Kind.TELEPORT)) {
            helper.fail("mas o teleporte não — o lobo não é a mandrágora");
        }

        level.setBlockAndUpdate(lobo, Blocks.AIR.defaultBlockState());
        if (AreaMarkers.protege(quem, AreaMarkers.Kind.CURSE)) {
            helper.fail("tirado o marcador, a proteção vai com ele");
        }

        BlockPos mandrágora = helper.absolutePos(new BlockPos(4, 2, 2));
        level.setBlockAndUpdate(mandrágora, OccultaBlocks.MANDRAKE_MARKER.defaultBlockState());
        if (!AreaMarkers.protege(quem, AreaMarkers.Kind.TELEPORT)) {
            helper.fail("com a mandrágora perto, o teleporte é abafado");
        }
        if (AreaMarkers.protege(quem, AreaMarkers.Kind.CURSE)) {
            helper.fail("e a maldição dirigida não");
        }

        level.setBlockAndUpdate(mandrágora, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /**
     * <b>Quem matou alguém perto dele perde a proteção</b>, e não a recupera — o nome fica guardado no
     * marcador. É a melhor ideia do bloco: a proteção é para quem vive em paz.
     */
    @GameTest(maxTicks = 40)
    public void aKillerLosesTheProtectionForGood(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        BlockPos lobo = helper.absolutePos(new BlockPos(2, 2, 4));
        level.setBlockAndUpdate(lobo, OccultaBlocks.WOLF_MARKER.defaultBlockState());

        var matador = helper.makeMockPlayer(GameType.SURVIVAL);
        matador.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 4.5)));
        var vítima = helper.makeMockPlayer(GameType.SURVIVAL);
        vítima.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 4.5)));

        if (!AreaMarkers.protege(matador, AreaMarkers.Kind.CURSE)) {
            helper.fail("antes de matar, ele está protegido");
        }

        AreaMarkers.matou(vítima, matador);
        if (AreaMarkers.protege(matador, AreaMarkers.Kind.CURSE)) {
            helper.fail("depois de matar, já não está");
        }
        if (!AreaMarkers.protege(vítima, AreaMarkers.Kind.CURSE)) {
            helper.fail("e quem morreu continua protegido");
        }

        // e matar um bicho não conta
        var outro = helper.makeMockPlayer(GameType.SURVIVAL);
        outro.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 5.5)));
        var porco = helper.spawn(net.minecraft.world.entity.EntityTypes.PIG, new BlockPos(3, 2, 5));
        AreaMarkers.matou(porco, outro);
        if (!AreaMarkers.protege(outro, AreaMarkers.Kind.CURSE)) {
            helper.fail("matar um porco não tira a proteção de ninguém");
        }

        porco.discard();
        level.setBlockAndUpdate(lobo, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /**
     * <b>Trinta e dois blocos</b>, e a conta é chata de propósito: ela mede em X e Z e <b>finge que o Y é
     * um</b> nos dois lados, de modo que um marcador no fundo de uma mina protege quem está no céu por
     * cima dele.
     */
    @GameTest(maxTicks = 40)
    public void theProtectionIsThirtyTwoWideAndInfinitelyTall(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        BlockPos lobo = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(lobo, OccultaBlocks.WOLF_MARKER.defaultBlockState());

        var quem = helper.makeMockPlayer(GameType.SURVIVAL);

        quem.snapTo(new Vec3(lobo.getX() + AreaMarkers.RAIO - 1, lobo.getY(), lobo.getZ()));
        if (!AreaMarkers.protege(quem, AreaMarkers.Kind.CURSE)) helper.fail("a trinta e um, protege");

        quem.snapTo(new Vec3(lobo.getX() + AreaMarkers.RAIO + 1, lobo.getY(), lobo.getZ()));
        if (AreaMarkers.protege(quem, AreaMarkers.Kind.CURSE)) helper.fail("a trinta e três, já não");

        // e a altura não conta para nada
        quem.snapTo(new Vec3(lobo.getX(), lobo.getY() + 100.0, lobo.getZ()));
        if (!AreaMarkers.protege(quem, AreaMarkers.Kind.CURSE)) {
            helper.fail("cem blocos acima dele, continua a proteger");
        }

        level.setBlockAndUpdate(lobo, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }
}
