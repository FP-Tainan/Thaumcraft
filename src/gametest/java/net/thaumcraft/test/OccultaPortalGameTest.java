package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.rite.RiteRegistry;
import net.thaumcraft.occulta.spirit.SpiritManifest;
import net.thaumcraft.occulta.spirit.SpiritPortalBlock;
import net.thaumcraft.occulta.spirit.SpiritWalk;
import net.thaumcraft.occulta.spirit.SpiritWorld;

/**
 * O Portal do Espírito e o fantasma: acender, atravessar, contar os segundos e ser puxado de volta.
 */
public class OccultaPortalGameTest {
    /**
     * A moldura de neve com o vão vazio acende: é o {@code tryToCreatePortal}.
     *
     * <p>A moldura é a do original — quatro de baixo, quatro de cima e dois de cada lado, sem os cantos.
     */
    @GameTest
    public void aSnowFrameLights(GameTestHelper helper) {
        BlockPos pé = new BlockPos(2, 1, 2);
        moldura(helper, pé);

        if (!SpiritPortalBlock.tryToCreate(helper.getLevel(), helper.absolutePos(pé.above()))) {
            helper.fail("a moldura inteira devia acender");
            return;
        }
        for (int l = 0; l < SpiritPortalBlock.WIDTH; l++) {
            for (int y = 0; y < SpiritPortalBlock.HEIGHT; y++) {
                BlockPos casa = pé.offset(l, 1 + y, 0);
                if (!helper.getBlockState(casa).is(OccultaBlocks.SPIRIT_PORTAL)) {
                    helper.fail("o vão devia fechar-se de portal, e a casa " + casa + " não fechou");
                    return;
                }
            }
        }
        helper.succeed();
    }

    /** E uma moldura com um buraco não acende. */
    @GameTest
    public void abrokenFrameDoesNotLight(GameTestHelper helper) {
        BlockPos pé = new BlockPos(2, 1, 2);
        moldura(helper, pé);
        helper.setBlock(pé.above(3), Blocks.AIR.defaultBlockState());

        if (SpiritPortalBlock.tryToCreate(helper.getLevel(), helper.absolutePos(pé.above()))) {
            helper.fail("com um buraco na moldura, não acende");
        }
        helper.succeed();
    }

    /** Tirada a moldura, o portal cai. */
    @GameTest(maxTicks = 40)
    public void theFrameHoldsThePortal(GameTestHelper helper) {
        BlockPos pé = new BlockPos(2, 1, 2);
        moldura(helper, pé);
        SpiritPortalBlock.tryToCreate(helper.getLevel(), helper.absolutePos(pé.above()));
        if (!helper.getBlockState(pé.above()).is(OccultaBlocks.SPIRIT_PORTAL)) {
            helper.fail("o portal devia estar de pé");
            return;
        }

        helper.setBlock(pé, Blocks.AIR.defaultBlockState());
        helper.runAfterDelay(5, () -> {
            if (helper.getBlockState(pé.above()).is(OccultaBlocks.SPIRIT_PORTAL)) {
                helper.fail("sem moldura, o portal cai");
                return;
            }
            helper.succeed();
        });
    }

    /** O crédito de manifestação vem do rito, e sem ele o portal não deixa passar. */
    @GameTest
    public void theRiteGivesTheCredit(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        if (SpiritManifest.canManifest(quem)) helper.fail("sem rito, não há crédito");

        SpiritManifest.grant(quem, SpiritManifest.GRANTED);
        if (SpiritManifest.credit(quem) != SpiritManifest.GRANTED) {
            helper.fail("o rito dá " + SpiritManifest.GRANTED + " segundos");
        }
        if (!SpiritManifest.canManifest(quem)) helper.fail("e com eles, passa");

        // e o crédito soma: dois ritos dão o dobro
        SpiritManifest.grant(quem, SpiritManifest.GRANTED);
        if (SpiritManifest.credit(quem) != SpiritManifest.GRANTED * 2) {
            helper.fail("e dois ritos dão o dobro");
        }
        SpiritWalk.set(quem, SpiritWalk.AWAKE);
        helper.succeed();
    }

    /** O relógio desce de cinco em cinco, e no zero puxa o fantasma de volta. */
    @GameTest(maxTicks = 120)
    public void theClockPullsTheGhostBack(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        SpiritManifest.grant(quem, 15);

        // finge-se o fantasma sem atravessar portal nenhum: é o estado que o relógio conta
        SpiritWalk.set(quem, SpiritWalk.of(quem).withWalking(true).withGhost(true, java.util.List.of(), 10.0f));
        SpiritManifest.tick(quem);
        if (SpiritManifest.credit(quem) != 10) {
            helper.fail("uma batida tira cinco, e ficou " + SpiritManifest.credit(quem));
        }
        SpiritManifest.tick(quem);
        SpiritManifest.tick(quem);
        if (SpiritWalk.ghost(quem)) helper.fail("no zero, o fantasma é puxado de volta");
        if (SpiritManifest.credit(quem) != 0) helper.fail("e o crédito acaba");

        SpiritWalk.set(quem, SpiritWalk.AWAKE);
        helper.succeed();
    }

    /** Atravessar leva o espírito para cá e deixa a mochila lá — menos as Agulhas de Gelo. */
    @GameTest(maxTicks = 120)
    public void theGhostLeavesTheBagBehind(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        if (!SpiritWorld.fallAsleep(quem, 0.0)) {
            helper.fail("devia adormecer");
            return;
        }
        quem.getInventory().add(new ItemStack(OccultaItems.WISPY_COTTON, 5));
        quem.getInventory().add(new ItemStack(OccultaItems.ICY_NEEDLE, 2));
        SpiritManifest.grant(quem, SpiritManifest.GRANTED);

        if (!SpiritWorld.manifest(quem)) {
            helper.fail("com crédito, devia manifestar-se");
            SpiritWorld.wakeUp(quem);
            return;
        }
        if (!SpiritWalk.ghost(quem)) helper.fail("e passar a andar em fantasma");
        if (SpiritWorld.is(quem.level())) helper.fail("e aparecer no mundo de cá");
        if (quem.getInventory().countItem(OccultaItems.WISPY_COTTON) != 0) {
            helper.fail("o algodão fica do outro lado");
        }
        if (quem.getInventory().countItem(OccultaItems.ICY_NEEDLE) != 2) {
            helper.fail("e as agulhas atravessam com ele");
        }

        if (!SpiritWorld.unmanifest(quem)) {
            helper.fail("e devia voltar");
            return;
        }
        if (SpiritWalk.ghost(quem)) helper.fail("e deixar de ser fantasma");
        if (!SpiritWorld.is(quem.level())) helper.fail("e voltar ao outro lado");
        if (quem.getInventory().countItem(OccultaItems.WISPY_COTTON) != 5) {
            helper.fail("e achar o algodão onde o deixou");
        }
        SpiritWorld.wakeUp(quem);
        helper.succeed();
    }

    /** Sem crédito nenhum, atravessar não faz nada. */
    @GameTest(maxTicks = 120)
    public void withoutCreditNothingHappens(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        if (!SpiritWorld.fallAsleep(quem, 0.0)) {
            helper.fail("devia adormecer");
            return;
        }
        if (SpiritWorld.manifest(quem)) helper.fail("sem crédito, o portal não leva ninguém");
        if (SpiritWalk.ghost(quem)) helper.fail("e ninguém vira fantasma");
        SpiritWorld.wakeUp(quem);
        helper.succeed();
    }

    /** O Rito da Manifestação está na lista e pede o que o original pede. */
    @GameTest
    public void theRiteOfManifestationIsThere(GameTestHelper helper) {
        var rito = RiteRegistry.all().stream()
                .filter(r -> r.key().equals("tc.rite.manifest"))
                .findFirst().orElse(null);
        if (rito == null) {
            helper.fail("o Rito da Manifestação devia estar na lista");
            return;
        }
        var pede = net.thaumcraft.occulta.rite.Rites.shown(rito);
        for (var coisa : new net.minecraft.world.item.Item[]{
                OccultaItems.SPECTRAL_DUST, OccultaItems.MELLIFLUOUS_HUNGER,
                OccultaItems.NECROTIC_STONE, OccultaItems.ARTHANA, Items.GUNPOWDER}) {
            if (pede.stream().noneMatch(c -> c.is(coisa))) helper.fail("ele pede " + coisa);
        }
        helper.succeed();
    }

    /** Uma moldura de neve de dois por dois, com o vão vazio. */
    private static void moldura(GameTestHelper helper, BlockPos pé) {
        var neve = Blocks.SNOW_BLOCK.defaultBlockState();
        // o chão do vão e o teto
        for (int l = 0; l < SpiritPortalBlock.WIDTH; l++) {
            helper.setBlock(pé.offset(l, 0, 0), neve);
            helper.setBlock(pé.offset(l, SpiritPortalBlock.HEIGHT + 1, 0), neve);
        }
        // e os dois lados
        for (int y = 1; y <= SpiritPortalBlock.HEIGHT; y++) {
            helper.setBlock(pé.offset(-1, y, 0), neve);
            helper.setBlock(pé.offset(SpiritPortalBlock.WIDTH, y, 0), neve);
        }
        for (int l = 0; l < SpiritPortalBlock.WIDTH; l++) {
            for (int y = 1; y <= SpiritPortalBlock.HEIGHT; y++) {
                helper.setBlock(pé.offset(l, y, 0), Blocks.AIR.defaultBlockState());
            }
        }
    }

    /** Sem uso fora da prova: a direção que a moldura toma. */
    static Direction.Axis axis(ServerLevel level, BlockPos onde) {
        return level.getBlockState(onde).getValue(SpiritPortalBlock.AXIS);
    }
}
