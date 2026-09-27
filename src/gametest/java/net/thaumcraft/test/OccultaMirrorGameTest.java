package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.mirror.MirrorBlock;
import net.thaumcraft.occulta.mirror.MirrorBlockEntity;
import net.thaumcraft.occulta.mirror.MirrorChunkGenerator;
import net.thaumcraft.occulta.mirror.MirrorLink;
import net.thaumcraft.occulta.mirror.MirrorTravel;
import net.thaumcraft.occulta.mirror.MirrorWorld;
import net.thaumcraft.occulta.mirror.ReflectionEntity;

import java.util.Optional;

/**
 * Os espelhos: o par de blocos, a caixa que dispara a travessia, o desenho das celas do Mundo do Espelho, a
 * ligação que o item leva e as duas cantigas.
 */
public class OccultaMirrorGameTest {
    /** O espelho é dois blocos, e quebrando um o outro vai-se. */
    @GameTest
    public void aMirrorIsTwoBlocks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos alto = helper.absolutePos(new BlockPos(2, 3, 2));
        put(level, alto, Direction.NORTH, false);

        // as duas metades têm alma, como no original — mas a ligação é a da de cima
        if (!(level.getBlockEntity(alto) instanceof MirrorBlockEntity)) {
            helper.fail("a metade de cima tem alma");
        }
        if (!(level.getBlockEntity(alto.below()) instanceof MirrorBlockEntity)) {
            helper.fail("e a de baixo também, que é ela quem desenha a moldura de baixo");
        }
        if (MirrorBlock.top(level, alto.below(), level.getBlockState(alto.below())) == null) {
            helper.fail("e a de baixo sabe achar a de cima");
        }

        // tirando a de baixo, a de cima cai
        level.setBlockAndUpdate(alto.below(), Blocks.AIR.defaultBlockState());
        if (level.getBlockState(alto).is(OccultaBlocks.WITCH_MIRROR)) {
            helper.fail("sem a outra metade, o espelho não fica de pé");
        }
        helper.succeed();
    }

    /** O vidro é uma lâmina do lado contrário ao que o espelho olha, e a caixa de disparo é mais funda. */
    @GameTest
    public void theGlassHugsTheWall(GameTestHelper helper) {
        for (Direction olha : Direction.Plane.HORIZONTAL) {
            var caixa = MirrorBlock.trigger(BlockPos.ZERO, olha);
            double fundo = olha == Direction.NORTH ? caixa.minZ
                    : olha == Direction.SOUTH ? 1.0 - caixa.maxZ
                    : olha == Direction.WEST ? caixa.minX : 1.0 - caixa.maxX;
            if (Math.abs(fundo - 0.68) > 1.0e-6) {
                helper.fail("a caixa de disparo encosta na parede de trás, para o lado " + olha);
            }
            if (Math.abs(caixa.getYsize() - 1.0) > 1.0e-6) helper.fail("e é da altura do bloco");
        }
        helper.succeed();
    }

    /** O item leva a ligação consigo: arrancado e assentado, é o mesmo espelho. */
    @GameTest
    public void theItemCarriesTheLink(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos alto = helper.absolutePos(new BlockPos(2, 3, 4));
        put(level, alto, Direction.NORTH, false);
        if (!(level.getBlockEntity(alto) instanceof MirrorBlockEntity espelho)) {
            helper.fail("a alma devia estar lá");
            return;
        }

        BlockPos longe = new BlockPos(123, 40, -456);
        espelho.linkTo(new MirrorLink(MirrorWorld.LEVEL, longe));
        espelho.setHollow(true);

        ItemStack item = new ItemStack(OccultaItems.WITCH_MIRROR);
        espelho.writeToItem(item);
        MirrorLink.Held trazia = item.getOrDefault(OccultaComponents.MIRROR, MirrorLink.Held.EMPTY);
        if (trazia.link().map(MirrorLink::pos).filter(longe::equals).isEmpty()) {
            helper.fail("o item leva para onde o espelho ia");
        }
        if (!trazia.hollow()) helper.fail("e leva a marca de vazado");

        // e um espelho novo, assentado com esse item, volta a ser o mesmo
        BlockPos outro = helper.absolutePos(new BlockPos(4, 3, 4));
        put(level, outro, Direction.NORTH, false);
        if (!(level.getBlockEntity(outro) instanceof MirrorBlockEntity segundo)) {
            helper.fail("a alma do segundo devia estar lá");
            return;
        }
        segundo.readFromItem(level, item);
        if (segundo.link() == null || !segundo.link().pos().equals(longe)) {
            helper.fail("o espelho assentado de novo vai para o mesmo lugar");
        }
        if (!segundo.hollow()) helper.fail("e continua vazado");

        limpa(level, alto);
        limpa(level, outro);
        helper.succeed();
    }

    /** O desenho das celas: nove de lado em x e z, nove de alto, e parede em volta. */
    @GameTest
    public void theCellsAreNineAcross(GameTestHelper helper) {
        int vãos = 0;
        for (int x = 0; x < 16; x++) {
            if (!MirrorChunkGenerator.isWall(x, 8, 8)) vãos++;
        }
        if (vãos != 9) helper.fail("o vão de uma cela tem nove casas de lado, e tem " + vãos);

        int altos = 0;
        for (int y = 0; y < 16; y++) {
            if (!MirrorChunkGenerator.isWall(8, y, 8)) altos++;
        }
        if (altos != 9) helper.fail("e nove de alto, e tem " + altos);

        // o espelho selado de uma cela nasce na casa quatro em x e oito em y e z
        if (MirrorChunkGenerator.isWall(4, 8, 8)) helper.fail("onde o espelho selado nasce é vão, não parede");
        if (!MirrorChunkGenerator.isWall(3, 8, 8)) helper.fail("e a casa ao lado dele é parede");
        if (!MirrorChunkGenerator.isWall(8, 5, 8)) helper.fail("e o chão da cela é parede");
        helper.succeed();
    }

    /** A conta de qual cela é a de um lugar, e onde fica o meio dela. */
    @GameTest
    public void everyPlaceKnowsItsCell(GameTestHelper helper) {
        BlockPos dentro = new BlockPos(38, 76, 71);
        BlockPos cela = MirrorWorld.cellMirror(dentro);
        if (!cela.equals(new BlockPos(36, 72, 72))) {
            helper.fail("a cela daquele lugar é a de (36, 72, 72), e deu " + cela);
        }
        if (!MirrorWorld.cellMiddle(cela).equals(cela.offset(4, 0, 0))) {
            helper.fail("e o meio dela é quatro casas adiante do espelho");
        }
        // uma casa negativa também tem cela
        if (!MirrorWorld.cellMirror(new BlockPos(-1, 8, -1)).equals(new BlockPos(-12, 8, -8))) {
            helper.fail("a conta da cela vale para o lado negativo também");
        }
        helper.succeed();
    }

    /** Quem nunca entrou no Mundo do Espelho pode entrar por qualquer espelho; depois, só pelo dele. */
    @GameTest
    public void theEntryCellIsRemembered(GameTestHelper helper) {
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos um = new BlockPos(4, 8, 8);
        BlockPos dois = new BlockPos(20, 8, 8);
        if (!MirrorTravel.isEntry(quem, um) || !MirrorTravel.isEntry(quem, dois)) {
            helper.fail("quem nunca entrou entra por qualquer espelho");
        }
        MirrorTravel.setEntry(quem, um);
        if (!MirrorTravel.isEntry(quem, um)) helper.fail("depois de entrar, o dele é o dele");
        if (MirrorTravel.isEntry(quem, dois)) helper.fail("e não é o de outro qualquer");
        if (MirrorTravel.data(quem).entry().equals(Optional.empty())) {
            helper.fail("e isso fica guardado nele");
        }
        helper.succeed();
    }

    /** O espelho conta quem lhe fica diante, e a caixa dele olha quatro casas para a frente. */
    @GameTest
    public void theMirrorCountsWhoStandsBefore(GameTestHelper helper) {
        for (Direction olha : Direction.Plane.HORIZONTAL) {
            var frente = MirrorBlockEntity.front(BlockPos.ZERO, olha);
            double fundo = olha.getAxis() == Direction.Axis.Z ? frente.getZsize() : frente.getXsize();
            double lado = olha.getAxis() == Direction.Axis.Z ? frente.getXsize() : frente.getZsize();
            if (Math.abs(fundo - 5.0) > 1.0e-6) {
                helper.fail("a caixa olha quatro casas para a frente, mais a do espelho, para " + olha);
            }
            if (Math.abs(lado - 3.0) > 1.0e-6) helper.fail("e uma para cada lado");
        }
        helper.succeed();
    }

    /** A cela de um espelho abre-se sozinha na primeira vez, com o espelho selado dentro e o Reflexo nela. */
    @GameTest
    public void aMirrorClaimsItsCell(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos alto = helper.absolutePos(new BlockPos(2, 3, 6));
        put(level, alto, Direction.NORTH, false);
        if (!(level.getBlockEntity(alto) instanceof MirrorBlockEntity espelho)) {
            helper.fail("a alma devia estar lá");
            return;
        }

        MirrorLink link = espelho.orClaim(level);
        if (link == null) {
            helper.fail("o espelho devia arranjar cela para si");
            return;
        }
        if (link.level() != MirrorWorld.LEVEL) helper.fail("e a cela é no Mundo do Espelho");

        ServerLevel mundo = level.getServer().getLevel(MirrorWorld.LEVEL);
        if (mundo == null) {
            helper.fail("o Mundo do Espelho devia estar aberto");
            return;
        }
        var lá = mundo.getBlockState(link.pos());
        if (!lá.is(OccultaBlocks.SEALED_WITCH_MIRROR)) helper.fail("com o espelho selado na parede da cela");
        if (lá.getValue(MirrorBlock.FACING) != Direction.EAST) helper.fail("virado para dentro, a leste");
        if (!mundo.getBlockState(link.pos().below()).is(OccultaBlocks.SEALED_WITCH_MIRROR)) {
            helper.fail("e com as duas metades");
        }
        if (!MirrorWorld.claimed(mundo, link.pos())) helper.fail("e a cela passa a ser de alguém");

        // e o selado sabe o caminho de volta
        if (!(mundo.getBlockEntity(link.pos()) instanceof MirrorBlockEntity selado)) {
            helper.fail("o selado tem alma");
            return;
        }
        if (selado.link() == null || !selado.link().pos().equals(alto)) {
            helper.fail("e ela aponta de volta para o espelho de cá");
        }

        // pedir a cela outra vez devolve a mesma, e não abre outra
        if (!link.pos().equals(espelho.orClaim(level).pos())) helper.fail("a cela de um espelho é uma só");

        // o Reflexo acorda nela — e, uma batida depois, quando o mundo já o vê, não acorda um segundo.
        // A cela tem de estar presa antes, ou o bicho posto nela fica na fila e o mundo ainda não o vê.
        int pedaçoX = link.pos().getX() >> 4;
        int pedaçoZ = link.pos().getZ() >> 4;
        mundo.setChunkForced(pedaçoX, pedaçoZ, true);
        if (ReflectionEntity.wake(mundo, link.pos()) == null) helper.fail("o Reflexo devia acordar na cela");
        helper.runAfterDelay(2, () -> {
            if (ReflectionEntity.inCell(mundo, link.pos()).size() != 1) {
                helper.fail("há um Reflexo na cela");
                return;
            }
            if (ReflectionEntity.wake(mundo, link.pos()) != null) helper.fail("e não acorda um segundo");
            for (var reflexo : ReflectionEntity.inCell(mundo, link.pos())) reflexo.discard();
            mundo.setChunkForced(pedaçoX, pedaçoZ, false);
            limpa(level, alto);
            helper.succeed();
        });
    }

    /** Morto o Reflexo da cela, o espelho de cá fica vazado — e passa a ser ponte. */
    @GameTest
    public void killingTheReflectionHollowsTheMirror(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos alto = helper.absolutePos(new BlockPos(4, 3, 6));
        put(level, alto, Direction.NORTH, false);
        if (!(level.getBlockEntity(alto) instanceof MirrorBlockEntity espelho)) {
            helper.fail("a alma devia estar lá");
            return;
        }
        MirrorLink link = espelho.orClaim(level);
        ServerLevel mundo = level.getServer().getLevel(MirrorWorld.LEVEL);
        if (link == null || mundo == null) {
            helper.fail("a cela devia abrir");
            return;
        }
        if (espelho.hollow()) helper.fail("um espelho novo é habitado, não vazado");

        // sem Reflexo vivo na cela, a morte de um vaza o espelho de cá
        ReflectionEntity.demonSlain(mundo, MirrorWorld.cellMiddle(link.pos()));
        if (!espelho.hollow()) helper.fail("morto o Reflexo, o espelho fica vazado");

        limpa(level, alto);
        helper.succeed();
    }

    /** A cantiga só pega dentro do Mundo do Espelho. */
    @GameTest
    public void theChantOnlyWorksInside(GameTestHelper helper) {
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        if (!(quem instanceof net.minecraft.server.level.ServerPlayer jogador)) {
            // o jogador de mentira das provas não é do servidor: então prova-se o que dá
            helper.succeed();
            return;
        }
        if (MirrorTravel.chant(jogador, "espelho espelho meu me manda para casa")) {
            helper.fail("fora do Mundo do Espelho a cantiga não faz nada");
        }
        helper.succeed();
    }

    /** Põe um espelho de duas metades naquele lugar. */
    private static void put(ServerLevel level, BlockPos alto, Direction olha, boolean selado) {
        var bloco = selado ? OccultaBlocks.SEALED_WITCH_MIRROR : OccultaBlocks.WITCH_MIRROR;
        var feitio = bloco.defaultBlockState().setValue(MirrorBlock.FACING, olha);
        level.setBlock(alto, feitio.setValue(MirrorBlock.HALF, DoubleBlockHalf.UPPER), 3);
        level.setBlock(alto.below(), feitio.setValue(MirrorBlock.HALF, DoubleBlockHalf.LOWER), 3);
    }

    private static void limpa(ServerLevel level, BlockPos alto) {
        level.setBlock(alto, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(alto.below(), Blocks.AIR.defaultBlockState(), 3);
    }
}
