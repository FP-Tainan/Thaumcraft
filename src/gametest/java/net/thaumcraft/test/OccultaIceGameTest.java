package net.thaumcraft.test;

import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.brew.BrewWorldActions;
import net.thaumcraft.occulta.ice.IceSphere;
import net.thaumcraft.occulta.rite.Rites;

/**
 * O <b>Gelo Perpétuo</b>, a família dele e as duas maneiras de o fazer.
 *
 * <p>A prova que carrega a fatia é a de que ele <b>não derrete</b>: é a única diferença entre ele e o gelo do
 * mundo, e é a diferença inteira. Um bloco de gelo ao sol é um relógio; este não é, e por isso se pode
 * construir com ele.
 */
public class OccultaIceGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /**
     * <b>Ele não derrete.</b>
     *
     * <p>O gelo do mundo tem relógio: de vez em quando o jogo lhe pergunta se já é hora, e com luz bastante
     * ele vira água. Este <b>não tem relógio nenhum</b> — e é por não ter que uma casa feita dele continua
     * de pé.
     */
    @GameTest(maxTicks = 20)
    public void itNeverMelts(GameTestHelper helper) {
        var gelo = OccultaBlocks.PERPETUAL_ICE.defaultBlockState();
        if (gelo.isRandomlyTicking()) helper.fail("gelo perpétuo não tem relógio: ele não derrete");
        if (Blocks.ICE.defaultBlockState().isRandomlyTicking() == gelo.isRandomlyTicking()) {
            helper.fail("e o do mundo tem, que é a diferença toda");
        }
        if (gelo.getBlock().getFriction() != 0.98f) {
            helper.fail("mas escorrega igual, e escorrega " + gelo.getBlock().getFriction());
        }
        helper.succeed();
    }

    /**
     * A <b>Porta de Gelo</b> fica de pé sobre gelo perpétuo — e hoje qualquer porta fica.
     *
     * <p>No jogo de 2014 o gelo não contava como chão sólido, e uma porta comum não se aguentava em cima
     * dele; o Witchery precisou de uma classe de porta só para abrir essa exceção. Hoje o que decide é o
     * <b>feitio</b> do que está por baixo, e o gelo perpétuo é um cubo inteiro.
     *
     * <p>A exceção deixou de ter o que fazer, e esta prova guarda isso: as duas portas ficam de pé. Se um
     * dia a conta do jogo mudar e a de sorveira cair, é aqui que se vê.
     */
    @GameTest(maxTicks = 20)
    public void theIceDoorStandsOnIce(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos chão = helper.absolutePos(new BlockPos(3, 1, 3));
        level.setBlockAndUpdate(chão, OccultaBlocks.PERPETUAL_ICE.defaultBlockState());

        var porta = OccultaBlocks.ICE_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        if (!porta.canSurvive(level, chão.above())) {
            helper.fail("a porta de gelo fica de pé sobre gelo perpétuo");
        }

        // e a de sorveira também, porque hoje o gelo é chão que serve
        var sorveira = OccultaBlocks.ROWAN_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        if (!sorveira.canSurvive(level, chão.above())) {
            helper.fail("e hoje qualquer porta fica: o gelo perpétuo é um cubo inteiro");
        }

        // mas no ar nenhuma fica, que é o que mostra que a conta é mesmo do chão
        if (sorveira.canSurvive(level, chão.above().above().above())) {
            helper.fail("no ar não fica porta nenhuma");
        }
        helper.succeed();
    }

    /**
     * A <b>esfera</b> é oca, e o raio dela <b>desconta um</b>.
     *
     * <p>É do original: uma esfera de raio oito tem casca de sete. Não é engano — é a conta de Bresenham a
     * começar de dentro —, mas quem não souber faz uma bolha um bloco menor do que pediu.
     */
    @GameTest(maxTicks = 20)
    public void theSphereIsHollowAndOneSmaller(GameTestHelper helper) {
        BlockPos meio = BlockPos.ZERO;
        Set<BlockPos> casca = new HashSet<>();
        IceSphere.casca(meio, 6, casca::add);

        if (casca.isEmpty()) {
            helper.fail("a casca tem de ter alguma coisa");
            return;
        }
        if (casca.contains(meio)) helper.fail("e é oca: o meio fica vazio");

        int maior = 0;
        for (BlockPos casa : casca) {
            maior = Math.max(maior, Math.max(Math.abs(casa.getX()),
                    Math.max(Math.abs(casa.getY()), Math.abs(casa.getZ()))));
        }
        if (maior != 5) helper.fail("o raio desconta um: de seis, a casca vai a cinco, e foi a " + maior);

        // e de raio um ela é um ponto só
        Set<BlockPos> ponto = new HashSet<>();
        IceSphere.casca(meio, 1, ponto::add);
        if (ponto.size() != 1 || !ponto.contains(meio)) helper.fail("de raio um, é um ponto só");
        helper.succeed();
    }

    /** E posta no mundo, ela deixa uma bola de gelo com ar dentro. */
    @GameTest(maxTicks = 40)
    public void theSphereLeavesABubble(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(4, 4, 4));

        IceSphere.casca(meio, 3, casa -> {
            if (level.getBlockState(casa).canBeReplaced()) {
                level.setBlockAndUpdate(casa, OccultaBlocks.PERPETUAL_ICE.defaultBlockState());
            }
        });
        if (!level.getBlockState(meio.offset(2, 0, 0)).is(OccultaBlocks.PERPETUAL_ICE)) {
            helper.fail("a dois de distância está a casca");
        }
        if (level.getBlockState(meio).is(OccultaBlocks.PERPETUAL_ICE)) {
            helper.fail("e o meio fica vazio");
        }

        // e limpa-se o que ficou
        IceSphere.casca(meio, 3, casa -> {
            if (level.getBlockState(casa).is(OccultaBlocks.PERPETUAL_ICE)) {
                level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
            }
        });
        helper.succeed();
    }

    /**
     * O <b>rito</b> cresce com o coven, e sozinha ninguém o faz.
     *
     * <p>Oito com duas bruxas, doze até cinco, dezesseis acima. É o único rito deste porte que <b>desiste e
     * devolve</b> o que se ofereceu quando o coven é pequeno demais.
     */
    @GameTest(maxTicks = 20)
    public void theRiteGrowsWithTheCoven(GameTestHelper helper) {
        if (Rites.IceShell.COVEN != 2) helper.fail("ele pede duas bruxas");
        if (Rites.IceShell.até(8, 2) != 8) helper.fail("com duas, oito");
        if (Rites.IceShell.até(8, 5) != 12) helper.fail("até cinco, doze");
        if (Rites.IceShell.até(8, 6) != 16) helper.fail("acima de cinco, dezesseis");
        helper.succeed();
    }

    /**
     * E o <b>cozimento</b> cresce com a força — e há quem não se emparede.
     *
     * <p>Um demônio, um blaze, um Ent, um golem de ferro ou um chefe não apanham nada disto. É a lista do
     * original, e ela diz o que a Casca de Gelo é: uma coisa que se faz a quem é de carne.
     */
    @GameTest(maxTicks = 20)
    public void theBrewGrowsWithStrengthAndSomeShrugItOff(GameTestHelper helper) {
        if (BrewWorldActions.IceShell.raio(1) != 2) helper.fail("força um dá bola de dois");
        if (BrewWorldActions.IceShell.raio(3) != 4) helper.fail("força três, quatro");
        if (BrewWorldActions.IceShell.raio(4) != 6) helper.fail("e do quarto grau para cima cresce mais");

        piso(helper);
        var demônio = helper.spawn(net.thaumcraft.occulta.OccultaEntities.DEMON, new BlockPos(2, 2, 2));
        var ovelha = helper.spawn(EntityTypes.SHEEP, new BlockPos(5, 2, 5));
        var golem = helper.spawn(EntityTypes.IRON_GOLEM, new BlockPos(6, 2, 2));

        if (!BrewWorldActions.IceShell.aguenta(demônio)) helper.fail("um demônio aguenta");
        if (!BrewWorldActions.IceShell.aguenta(golem)) helper.fail("um golem de ferro também");
        if (BrewWorldActions.IceShell.aguenta(ovelha)) helper.fail("mas uma ovelha não");

        demônio.discard();
        ovelha.discard();
        golem.discard();
        helper.succeed();
    }
}
