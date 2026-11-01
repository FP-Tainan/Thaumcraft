package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.arcana.ArcanaBlocks;
import net.thaumcraft.arcana.machine.BlackAuremBlockEntity;
import net.thaumcraft.arcana.machine.CelestialPrismBlockEntity;
import net.thaumcraft.arcana.machine.WizardChalkBlock;
import net.thaumcraft.arcana.power.PowerNodes;
import net.thaumcraft.arcana.power.PowerType;

/**
 * As <b>outras duas fontes</b> do Ars Magica 2: o Aurem Negro e o Prisma Celeste.
 *
 * <p>Uma come vida e dá Trevas; a outra bebe do céu e dá Luz. Nenhuma das duas queima nada, e nenhuma
 * das duas pede seja o que for.
 */
public class ArcanaSourcesGameTest {
    /** <b>Cada fonte dá o seu tipo, e só o seu.</b> */
    @GameTest(maxTicks = 20)
    public void eachSourceGivesItsOwnKind(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 2, 1), ArcanaBlocks.OBELISK);
        helper.setBlock(new BlockPos(3, 2, 1), ArcanaBlocks.BLACK_AUREM);
        helper.setBlock(new BlockPos(5, 2, 1), ArcanaBlocks.CELESTIAL_PRISM);

        var obelisco = helper.getBlockEntity(new BlockPos(1, 2, 1), net.thaumcraft.arcana.machine.ObeliskBlockEntity.class);
        var aurem = helper.getBlockEntity(new BlockPos(3, 2, 1), BlackAuremBlockEntity.class);
        var prisma = helper.getBlockEntity(new BlockPos(5, 2, 1), CelestialPrismBlockEntity.class);

        if (!obelisco.canProvidePower(PowerType.NEUTRAL) || obelisco.canProvidePower(PowerType.DARK)) {
            helper.fail("o Obelisco dá Neutro e só Neutro");
            return;
        }
        if (!aurem.canProvidePower(PowerType.DARK) || aurem.canProvidePower(PowerType.LIGHT)) {
            helper.fail("o Aurem dá Trevas e só Trevas");
            return;
        }
        if (!prisma.canProvidePower(PowerType.LIGHT) || prisma.canProvidePower(PowerType.NEUTRAL)) {
            helper.fail("o Prisma dá Luz e só Luz");
            return;
        }
        for (var fonte : new net.thaumcraft.arcana.power.PowerNode[]{obelisco, aurem, prisma}) {
            if (fonte.canRequestPower() || fonte.canRelayPower(PowerType.NEUTRAL) || !fonte.isSource()) {
                helper.fail("uma fonte não pede, não passa adiante, e é fonte");
                return;
            }
        }
        helper.succeed();
    }

    /**
     * <b>O Prisma Celeste carrega-se de dia</b>, e os topos de pedra-da-lua invertem-lhe o relógio.
     *
     * <p>É a troca do original: o topo mais forte, que multiplica por três, é o que obriga a esperar pela
     * noite.
     */
    @GameTest(maxTicks = 40)
    public void theCelestialPrismDrinksByDayOrByNight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos meio = new BlockPos(3, 2, 3);
        BlockPos onde = helper.absolutePos(meio);
        helper.setBlock(meio, ArcanaBlocks.CELESTIAL_PRISM);
        CelestialPrismBlockEntity prisma = helper.getBlockEntity(meio, CelestialPrismBlockEntity.class);

        prisma.checkNearbyBlockState();
        if (prisma.nightly()) {
            helper.fail("sem topos de pedra-da-lua ele é de dia");
            return;
        }

        // os quatro pilares de quartzo com os topos de pedra-da-lua
        for (int x : new int[]{-2, 2}) {
            for (int z : new int[]{-2, 2}) {
                helper.setBlock(meio.offset(x, 0, z), Blocks.QUARTZ_BLOCK);
                helper.setBlock(meio.offset(x, 1, z), Blocks.QUARTZ_BLOCK);
                helper.setBlock(meio.offset(x, 2, z), ArcanaBlocks.MATERIAL_BLOCKS.get("moonstone"));
            }
        }
        prisma.checkNearbyBlockState();
        if (!prisma.nightly()) {
            helper.fail("com os topos de pedra-da-lua ele passa a ser de noite");
            return;
        }
        if (Math.abs(prisma.powerMultiplier() - 3.0f) > 1.0e-5f) {
            helper.fail("e multiplica por três: deu " + prisma.powerMultiplier());
            return;
        }

        // e com o anel de giz por cima disso, três vezes 1,25
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                helper.setBlock(meio.offset(i, 0, j), ArcanaBlocks.WIZARD_CHALK.defaultBlockState()
                        .setValue(WizardChalkBlock.VARIANT, 0));
            }
        }
        prisma.checkNearbyBlockState();
        if (Math.abs(prisma.powerMultiplier() - 3.75f) > 1.0e-5f) {
            helper.fail("com giz e pedra-da-lua devia dar 3,75: deu " + prisma.powerMultiplier());
            return;
        }

        // está de dia na arena, e ele é de noite: não carrega
        float antes = PowerNodes.of(level).getPower(onde, PowerType.LIGHT);
        helper.runAfterDelay(20, () -> {
            float agora = PowerNodes.of(level).getPower(onde, PowerType.LIGHT);
            if (prisma.isNight()) {
                helper.succeed();
                return;
            }
            if (agora > antes) {
                helper.fail("de noite por dentro e de dia lá fora, ele não devia carregar");
                return;
            }
            helper.succeed();
        });
    }

    /** <b>Os três topos do Aurem Negro valem o que o original diz</b>: 1,1, 1,5 e 2,0. */
    @GameTest(maxTicks = 20)
    public void theAuremCapsAreWorthWhatTheyShould(GameTestHelper helper) {
        BlockPos meio = new BlockPos(3, 2, 3);
        helper.setBlock(meio, ArcanaBlocks.BLACK_AUREM);
        BlackAuremBlockEntity aurem = helper.getBlockEntity(meio, BlackAuremBlockEntity.class);

        for (int x : new int[]{-2, 2}) {
            for (int z : new int[]{-2, 2}) {
                helper.setBlock(meio.offset(x, 0, z), Blocks.NETHER_BRICKS);
                helper.setBlock(meio.offset(x, 1, z), Blocks.NETHER_BRICKS);
            }
        }
        if (!topo(helper, aurem, meio, ArcanaBlocks.MATERIAL_BLOCKS.get("chimerite"), 1.1f)) return;
        if (!topo(helper, aurem, meio, Blocks.OBSIDIAN, 1.5f)) return;
        if (!topo(helper, aurem, meio, ArcanaBlocks.MATERIAL_BLOCKS.get("sunstone"), 2.0f)) return;
        helper.succeed();
    }

    private static boolean topo(GameTestHelper helper, BlackAuremBlockEntity aurem, BlockPos meio,
                                net.minecraft.world.level.block.Block qual, float vale) {
        for (int x : new int[]{-2, 2}) {
            for (int z : new int[]{-2, 2}) helper.setBlock(meio.offset(x, 2, z), qual);
        }
        aurem.checkNearbyBlockState();
        if (Math.abs(aurem.powerMultiplier() - vale) > 1.0e-5f) {
            helper.fail("o topo de " + qual.getName().getString() + " devia valer " + vale
                    + ": deu " + aurem.powerMultiplier());
            return false;
        }
        return true;
    }

    /**
     * <b>O Aurem Negro puxa o que é bicho e deixa gente em paz.</b>
     *
     * <p>A lista de quem ele não toca é a do original, e é por ela que um jogador pode ficar ao pé dele a
     * ver o moinho trabalhar.
     */
    @GameTest(maxTicks = 80)
    public void theAuremCatchesBeastsAndNotPeople(GameTestHelper helper) {
        // o Aurem fica ao nível do chão: ele só olha de si para cima, e um bicho mais baixo não existe
        // para ele — é a caixa de procura do original, que vai de y a y+4
        BlockPos meio = new BlockPos(3, 0, 3);
        helper.setBlock(meio, ArcanaBlocks.BLACK_AUREM);
        BlackAuremBlockEntity aurem = helper.getBlockEntity(meio, BlackAuremBlockEntity.class);

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(6, 1, 3));
        double começou = porco.getX();
        helper.succeedWhen(() -> {
            helper.assertTrue(aurem.caught() >= 1, "o Aurem devia ter apanhado o porco");
            helper.assertTrue(Math.abs(porco.getX() - começou) > 0.2,
                    "e devia tê-lo puxado do lugar");
        });
    }

    /** <b>E não puxa quem está atrás de uma parede.</b> */
    @GameTest(maxTicks = 80)
    public void awallStopsTheAurem(GameTestHelper helper) {
        BlockPos meio = new BlockPos(2, 0, 3);
        helper.setBlock(meio, ArcanaBlocks.BLACK_AUREM);
        // uma parede de pedra entre os dois, de cima a baixo
        for (int y = 0; y <= 3; y++) {
            for (int z = 2; z <= 4; z++) helper.setBlock(new BlockPos(4, y, z), Blocks.STONE);
        }
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(6, 1, 3));
        double onde = porco.getX();

        helper.runAfterDelay(60, () -> {
            if (Math.abs(porco.getX() - onde) > 0.5) {
                helper.fail("a parede devia ter travado o Aurem, e o porco andou " + (onde - porco.getX()));
                return;
            }
            helper.succeed();
        });
    }
}
