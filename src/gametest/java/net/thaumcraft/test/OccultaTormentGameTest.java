package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.torment.RefillingChestBlockEntity;
import net.thaumcraft.occulta.torment.Torment;
import net.thaumcraft.occulta.torment.TormentMaze;
import net.thaumcraft.occulta.torment.TormentPortalBlock;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * O Tormento: os números dos seis andares, o desenho do labirinto, a prova de que ele <b>se atravessa</b>,
 * os três baús de cada andar, o portal do outro extremo, o mandado que se escreve em quem é tormentado e o
 * baú que só se enche onde deve.
 */
public class OccultaTormentGameTest {
    /** A semente com que as provas desenham o labirinto. Qualquer uma serve; esta é fixa de propósito. */
    private static final long SEMENTE = 20261009L;

    /** <b>Seis andares</b>, de quinze em quinze a partir do décimo, e o vão de oito entre um e o seguinte. */
    @GameTest
    public void sixFloorsFifteenApart(GameTestHelper helper) {
        if (TormentMaze.LEVELS != 6) helper.fail("são seis andares, e são " + TormentMaze.LEVELS);
        if (TormentMaze.floorOf(0) != 10) helper.fail("o primeiro assenta no décimo");
        if (TormentMaze.floorOf(5) != 85) helper.fail("e o último no oitenta e cinco");

        for (int andar = 0; andar < TormentMaze.LEVELS; andar++) {
            int chão = TormentMaze.floorOf(andar);
            if (TormentMaze.levelAt(chão) != andar) helper.fail("o chão do andar " + andar + " é dele");
            if (TormentMaze.levelAt(chão - 1) != andar) helper.fail("e a casa de baixo do chão também");
            if (TormentMaze.levelAt(chão + TormentMaze.WALL - 1) != andar) helper.fail("e o teto também");
            if (TormentMaze.levelAt(chão + TormentMaze.WALL) != -1) {
                helper.fail("mas a casa acima do teto já não é de andar nenhum");
            }
        }
        // o vão entre o teto de um e o chão do seguinte: oito casas de nada
        int vazias = 0;
        for (int y = TormentMaze.floorOf(0) + TormentMaze.WALL; y < TormentMaze.floorOf(1) - 1; y++) {
            if (TormentMaze.levelAt(y) == -1) vazias++;
        }
        if (vazias != 8) helper.fail("entre dois andares há oito casas de escuro, e há " + vazias);
        helper.succeed();
    }

    /**
     * <b>Parede, chão e teto.</b>
     *
     * <p>Uma parede é Força do chão ao teto; uma passagem é Pedra do Tormento em duas casas, quatro de ar e
     * um teto que também não se vê.
     */
    @GameTest
    public void wallsFloorAndCeiling(GameTestHelper helper) {
        TormentMaze maze = new TormentMaze(SEMENTE);
        int chão = TormentMaze.floorOf(0);

        // o canto do desenho é sempre parede: ele é a borda do labirinto
        for (int h = 0; h < TormentMaze.WALL; h++) {
            if (maze.at(TormentMaze.ORIGIN_X, chão + h, TormentMaze.ORIGIN_Z) != TormentMaze.FORÇA) {
                helper.fail("a borda do labirinto é Força de baixo a cima, e falhou em " + h);
            }
        }

        // e a porta de entrada é passagem: chão em duas casas, ar, e teto de Força
        int x = TormentMaze.DOOR_X;
        int z = TormentMaze.DOOR_Z;
        if (!chãoDePisar(maze.at(x, chão - 1, z))) helper.fail("debaixo do chão há mais chão");
        if (!chãoDePisar(maze.at(x, chão, z))) helper.fail("e o chão de pisar da porta é chão");
        for (int h = 1; h < TormentMaze.WALL - 1; h++) {
            if (maze.at(x, chão + h, z) != TormentMaze.AR) {
                helper.fail("e por cima dele há quatro de ar, e falhou em " + h);
            }
        }
        if (maze.at(x, chão + TormentMaze.WALL - 1, z) != TormentMaze.FORÇA) {
            helper.fail("e o teto é Força");
        }
        helper.succeed();
    }

    /**
     * <b>O labirinto atravessa-se.</b>
     *
     * <p>É a prova que carrega a fatia: um labirinto que não ligue a porta ao portal não é um labirinto, é
     * uma masmorra. A conta anda o chão de cada andar da casa de chegada até à do portal, e não há andar em
     * que ela não chegue.
     */
    @GameTest
    public void everyFloorCanBeCrossed(GameTestHelper helper) {
        TormentMaze maze = new TormentMaze(SEMENTE);
        for (int andar = 0; andar < TormentMaze.LEVELS; andar++) {
            int chão = TormentMaze.floorOf(andar);
            BlockPos porta = new BlockPos(TormentMaze.DOOR_X, chão, TormentMaze.DOOR_Z);
            BlockPos saída = new BlockPos(TormentMaze.ORIGIN_X + TormentMaze.SIZE, chão,
                    TormentMaze.ORIGIN_Z + 2 * TormentMaze.SIZE - 1);
            if (!anda(maze, andar, porta, saída)) {
                helper.fail("no andar " + andar + " não se vai da porta ao portal");
            }
        }
        helper.succeed();
    }

    /** <b>Três baús por andar</b>, e o do meio está no meio. */
    @GameTest
    public void threeChestsEveryFloor(GameTestHelper helper) {
        TormentMaze maze = new TormentMaze(SEMENTE);
        for (int andar = 0; andar < TormentMaze.LEVELS; andar++) {
            int chão = TormentMaze.floorOf(andar);
            int quantos = 0;
            for (int x = TormentMaze.ORIGIN_X; x < TormentMaze.ORIGIN_X + TormentMaze.SPAN_X; x++) {
                for (int z = TormentMaze.ORIGIN_Z; z < TormentMaze.ORIGIN_Z + TormentMaze.SPAN_Z; z++) {
                    if (maze.at(x, chão, z) == TormentMaze.BAÚ) quantos++;
                }
            }
            if (quantos != 3) helper.fail("o andar " + andar + " tem três baús, e tem " + quantos);
            if (maze.at(TormentMaze.ORIGIN_X + TormentMaze.SIZE, chão,
                    TormentMaze.ORIGIN_Z + TormentMaze.SIZE) != TormentMaze.BAÚ) {
                helper.fail("e um deles está no meio do labirinto, no andar " + andar);
            }
        }
        helper.succeed();
    }

    /** <b>O portal</b>: duas casas, no outro extremo, com moldura de Pedra do Tormento dos dois lados. */
    @GameTest
    public void thePortalIsAtTheFarEnd(GameTestHelper helper) {
        TormentMaze maze = new TormentMaze(SEMENTE);
        int chão = TormentMaze.floorOf(0);
        int x = TormentMaze.ORIGIN_X + TormentMaze.SIZE;
        int z = TormentMaze.ORIGIN_Z + 2 * TormentMaze.SIZE;

        if (maze.at(x, chão + 1, z) != TormentMaze.PORTAL
                || maze.at(x, chão + 2, z) != TormentMaze.PORTAL) {
            helper.fail("o portal tem duas casas de altura");
        }
        if (maze.at(x, chão + 3, z) != TormentMaze.PEDRA) helper.fail("e um lintel em cima");
        for (int lado = -1; lado <= 1; lado += 2) {
            for (int h = 1; h <= 3; h++) {
                if (maze.at(x + lado, chão + h, z) != TormentMaze.PEDRA) {
                    helper.fail("e uma coluna de três de cada lado");
                }
            }
        }
        // e é por isso que a fatia dele fica deitada em x: há pedra nos dois lados
        if (TormentPortalBlock.MAIS_TORMENTO != 0.05) helper.fail("uma em vinte manda para outro andar");
        helper.succeed();
    }

    /** <b>Uma casa de chão em cem é micélio de verdade</b> — e a conta é por andar, não por mundo. */
    @GameTest
    public void oneFloorTileInAHundred(GameTestHelper helper) {
        TormentMaze maze = new TormentMaze(SEMENTE);
        int chãos = 0;
        int micélios = 0;
        for (int andar = 0; andar < TormentMaze.LEVELS; andar++) {
            int chão = TormentMaze.floorOf(andar);
            for (int x = TormentMaze.ORIGIN_X; x < TormentMaze.ORIGIN_X + TormentMaze.SPAN_X; x++) {
                for (int z = TormentMaze.ORIGIN_Z; z < TormentMaze.ORIGIN_Z + TormentMaze.SPAN_Z; z++) {
                    byte marca = maze.at(x, chão, z);
                    if (marca == TormentMaze.PEDRA) chãos++;
                    if (marca == TormentMaze.MICÉLIO) {
                        chãos++;
                        micélios++;
                    }
                }
            }
        }
        if (chãos < 10000) helper.fail("os seis andares têm chão de sobra para a conta, e têm " + chãos);
        double parte = (double) micélios / chãos;
        // com uns vinte mil chãos, uma em cem dá por volta de duzentos; a folga é larga de propósito
        if (parte < 0.004 || parte > 0.02) {
            helper.fail("uma casa de chão em cem é micélio, e deu uma em " + (int) (1.0 / parte));
        }
        helper.succeed();
    }

    /** <b>A mesma semente dá o mesmo labirinto</b>, e outra dá outro. */
    @GameTest
    public void thePlanIsTheSameEveryTime(GameTestHelper helper) {
        TormentMaze um = new TormentMaze(SEMENTE);
        TormentMaze outro = new TormentMaze(SEMENTE);
        TormentMaze terceiro = new TormentMaze(SEMENTE + 1L);

        int igual = 0;
        int diferente = 0;
        int chão = TormentMaze.floorOf(0);
        for (int x = TormentMaze.ORIGIN_X; x < TormentMaze.ORIGIN_X + TormentMaze.SPAN_X; x++) {
            for (int z = TormentMaze.ORIGIN_Z; z < TormentMaze.ORIGIN_Z + TormentMaze.SPAN_Z; z++) {
                if (um.at(x, chão, z) != outro.at(x, chão, z)) igual = -1;
                if (um.at(x, chão, z) != terceiro.at(x, chão, z)) diferente++;
            }
        }
        if (igual < 0) helper.fail("a mesma semente tem de dar o mesmo labirinto");
        if (diferente < 100) helper.fail("e uma semente diferente, outro — e deu " + diferente + " casas");
        helper.succeed();
    }

    /**
     * <b>O mandado</b>: escreve-se em quem é tormentado e lê-se de volta, e some depois de cumprido.
     *
     * <p>O original o guarda no NBT do jogador com as duas chaves {@code WITCForceTorment} e
     * {@code WITCForceTormentLevel}; aqui é um guardado só, com as mesmas duas contas.
     */
    @GameTest
    public void theOrderIsWrittenAndReadBack(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        if (Torment.order(quem).oquê() != Torment.NADA) helper.fail("de início não há mandado");

        Torment.order(quem, Torment.COM_O_CHEFE, 4);
        if (Torment.order(quem).oquê() != Torment.COM_O_CHEFE) helper.fail("o mandado fica escrito");
        if (Torment.order(quem).andar() != 4) helper.fail("e o andar com ele");

        Torment.order(quem, Torment.NADA, -1);
        if (Torment.order(quem).oquê() != Torment.NADA) helper.fail("e apaga-se");
        if (Torment.order(quem).andar() != -1) helper.fail("e o andar também");

        // e o andar sorteado cai sempre dentro dos seis
        for (int volta = 0; volta < 50; volta++) {
            int andar = Torment.randomLevel(helper.getLevel());
            if (andar < 0 || andar >= TormentMaze.LEVELS) helper.fail("o andar sorteado é um dos seis");
        }
        helper.succeed();
    }

    /**
     * <b>O Baú de Reabastecimento só se enche no Tormento.</b>
     *
     * <p>A prova corre no mundo de cima, que é onde o labirinto não está: o relógio dele é adiantado para a
     * batida em que ele se encheria, e ele continua vazio.
     */
    @GameTest
    public void theChestOnlyRefillsInTorment(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(onde, OccultaBlocks.REFILLING_CHEST.defaultBlockState());
        if (!(level.getBlockEntity(onde) instanceof RefillingChestBlockEntity baú)) {
            helper.fail("o baú tem alma");
            return;
        }

        baú.adianta(RefillingChestBlockEntity.DE_HORA_EM_HORA - 1);
        RefillingChestBlockEntity.serverTick(level, onde, level.getBlockState(onde), baú);
        if (baú.quanto() > 0) helper.fail("fora do Tormento ele não se enche");

        // mas a conta de encher funciona: mandada à mão, ela põe de duas a cinco coisas lá dentro
        baú.enche(level, onde);
        int quantas = baú.quanto();
        if (quantas < 1 || quantas > RefillingChestBlockEntity.DE + RefillingChestBlockEntity.ATÉ) {
            helper.fail("e enche-se com duas a cinco coisas, e pôs " + quantas);
        }

        // e estando cheio ele não se enche outra vez
        baú.adianta(RefillingChestBlockEntity.DE_HORA_EM_HORA - 1);
        RefillingChestBlockEntity.serverTick(level, onde, level.getBlockState(onde), baú);
        if (baú.quanto() != quantas) helper.fail("cheio, ele não transborda");

        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** <b>Nada do labirinto se apanha</b>: as quatro peças dele não deixam despojo. */
    @GameTest
    public void nothingOfTheMazeCanBeTaken(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var peças = new net.minecraft.world.level.block.Block[]{
                OccultaBlocks.FORCE, OccultaBlocks.TORMENT_STONE,
                OccultaBlocks.TORMENT_PORTAL, OccultaBlocks.REFILLING_CHEST};
        for (var peça : peças) {
            var feitio = peça.defaultBlockState();
            var caiu = net.minecraft.world.level.block.Block.getDrops(feitio, level,
                    helper.absolutePos(BlockPos.ZERO), null);
            if (!caiu.isEmpty()) helper.fail("o " + peça + " não deixa nada quando se quebra");
            if (feitio.getDestroySpeed(level, helper.absolutePos(BlockPos.ZERO)) >= 0.0f) {
                helper.fail("e não se quebra: o " + peça + " tem dureza negativa");
            }
        }
        // e a parede de Força nem com o botão do meio
        ItemStack pegou = OccultaBlocks.FORCE.defaultBlockState()
                .getCloneItemStack(level, helper.absolutePos(BlockPos.ZERO), false);
        if (!pegou.isEmpty()) helper.fail("a parede de Força não se apanha nem no criativo");
        helper.succeed();
    }

    // ------------------------------------------------------------------ a conta de andar

    private static boolean chãoDePisar(byte marca) {
        return marca == TormentMaze.PEDRA || marca == TormentMaze.MICÉLIO;
    }

    /** Se dá para ir de uma casa de chão à outra andando só por passagens. */
    private static boolean anda(TormentMaze maze, int andar, BlockPos de, BlockPos para) {
        int chão = TormentMaze.floorOf(andar);
        Set<Long> visto = new HashSet<>();
        Deque<BlockPos> fila = new ArrayDeque<>();
        fila.add(de);
        visto.add(de.asLong());

        while (!fila.isEmpty()) {
            BlockPos onde = fila.poll();
            if (onde.getX() == para.getX() && onde.getZ() == para.getZ()) return true;
            for (var rumo : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                BlockPos ao = onde.relative(rumo);
                if (!visto.add(ao.asLong())) continue;
                // anda-se onde há chão em baixo e ar à altura da cabeça
                if (!chãoDePisar(maze.at(ao.getX(), chão, ao.getZ()))) continue;
                if (maze.at(ao.getX(), chão + 1, ao.getZ()) != TormentMaze.AR) continue;
                fila.add(ao);
            }
        }
        return false;
    }
}
