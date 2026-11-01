package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.DecoyEntity;
import net.thaumcraft.occulta.DupGrenadeItem;
import net.thaumcraft.occulta.DuplicationStaffItem;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaItems;

/**
 * O <b>Cajado</b> e a <b>Granada de Duplicação</b>, com o <b>Sósia</b> que ela larga.
 *
 * <p>A prova que carrega a fatia é a do <b>giro</b>: um molde colado girado tem de pôr cada bloco na casa
 * nova <b>e</b> virar a cara dele. Errando um dos dois, o que sai parece certo de relance e está errado —
 * uma escada virada para o lado errado numa casa inteira não se vê até se entrar nela.
 */
public class OccultaDuplicationGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 11; x++) {
            for (int z = 0; z < 11; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Bate com o cajado naquela casa, como quem o usa. */
    private static void bate(GameTestHelper helper, ItemStack cajado,
                             net.minecraft.server.level.ServerPlayer quem, BlockPos onde) {
        quem.setItemInHand(InteractionHand.MAIN_HAND, cajado);
        cajado.getItem().useOn(new UseOnContext(helper.getLevel(), quem, InteractionHand.MAIN_HAND,
                cajado, new BlockHitResult(Vec3.atCenterOf(onde), Direction.UP, onde, false)));
    }

    /**
     * <b>Marcar, guardar, colar</b> — e a caixa é aberta nas duas pontas.
     *
     * <p>Marcando (1,1,1) e (4,4,4), o que se guarda são as <b>oito</b> casas do meio, e não as vinte e
     * sete da caixa inteira. É assim no original, e é o que torna cômodo marcar as quinas de fora.
     */
    @GameTest(maxTicks = 60)
    public void markSaveAndPaste(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.CREATIVE);
        ItemStack cajado = new ItemStack(OccultaItems.DUP_STAFF);

        // um L de pedra dentro da caixa, com uma escada virada para o nascente
        BlockPos pedra = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos escada = helper.absolutePos(new BlockPos(3, 2, 2));
        level.setBlockAndUpdate(pedra, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(escada, Blocks.OAK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.EAST));

        bate(helper, cajado, quem, helper.absolutePos(new BlockPos(1, 1, 1)));
        if (DuplicationStaffItem.marco(cajado) == null) {
            helper.fail("a primeira batida marca a quina");
            return;
        }
        bate(helper, cajado, quem, helper.absolutePos(new BlockPos(4, 4, 4)));

        var molde = DuplicationStaffItem.molde(cajado);
        if (molde == null) {
            helper.fail("a segunda batida guarda o molde");
            return;
        }
        if (molde.largo() != 2 || molde.alto() != 2 || molde.fundo() != 2) {
            helper.fail("a caixa de dentro é de dois por dois por dois: deu "
                    + molde.largo() + "x" + molde.alto() + "x" + molde.fundo());
            return;
        }
        if (molde.blocos().size() != 8) {
            helper.fail("e são oito blocos: são " + molde.blocos().size());
            return;
        }
        if (DuplicationStaffItem.marco(cajado) != null) helper.fail("e o marco some com o molde");

        /*
         * E colar sem girar põe tudo igual. A colagem é <b>uma casa acima</b> da que se bateu, que é o
         * original dando lugar para a construção pousar.
         */
        BlockPos alvo = helper.absolutePos(new BlockPos(7, 1, 2));
        quem.setShiftKeyDown(true);
        bate(helper, cajado, quem, alvo);
        quem.setShiftKeyDown(false);

        if (!level.getBlockState(alvo.above()).is(Blocks.STONE)) {
            helper.fail("colado, o primeiro bloco é a pedra");
            return;
        }
        BlockState veio = level.getBlockState(alvo.above().east());
        if (!veio.is(Blocks.OAK_STAIRS) || veio.getValue(StairBlock.FACING) != Direction.EAST) {
            helper.fail("e a escada vem ao lado, ainda virada para o nascente: veio " + veio);
            return;
        }
        helper.succeed();
    }

    /**
     * <b>O giro vira o bloco e o lugar dele.</b>
     *
     * <p>Noventa graus no sentido do relógio levam o nascente ao sul — e levam a casa que estava a leste
     * para o sul também. Os dois têm de girar juntos, ou a construção sai torta por dentro.
     */
    @GameTest(maxTicks = 60)
    public void turningTurnsBothTheBlockAndItsPlace(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.CREATIVE);
        ItemStack cajado = new ItemStack(OccultaItems.DUP_STAFF);

        level.setBlockAndUpdate(helper.absolutePos(new BlockPos(2, 2, 2)),
                Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(helper.absolutePos(new BlockPos(3, 2, 2)),
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));

        bate(helper, cajado, quem, helper.absolutePos(new BlockPos(1, 1, 1)));
        bate(helper, cajado, quem, helper.absolutePos(new BlockPos(4, 4, 4)));
        if (DuplicationStaffItem.molde(cajado) == null) {
            helper.fail("o molde devia estar guardado");
            return;
        }

        // a primeira batida de pé cola já girada de noventa
        BlockPos alvo = helper.absolutePos(new BlockPos(7, 1, 6));
        bate(helper, cajado, quem, alvo);
        if (cajado.getOrDefault(OccultaComponents.STAFF_TURN, 0) != 1) {
            helper.fail("e o cajado passa para o giro seguinte");
            return;
        }

        /*
         * A pedra estava em (0,0) do molde; girada de noventa ela vai para (fundo-1, 0), que é uma casa a
         * leste. E a escada, que estava a leste dela, vai para o sul — com a cara virada para o sul.
         */
        BlockPos ondeAPedra = alvo.above().east();
        if (!level.getBlockState(ondeAPedra).is(Blocks.STONE)) {
            helper.fail("girada, a pedra vai para a casa de leste");
            return;
        }
        BlockState escada = level.getBlockState(ondeAPedra.south());
        if (!escada.is(Blocks.OAK_STAIRS)) {
            helper.fail("e a escada vai para a de sul: está " + escada);
            return;
        }
        if (escada.getValue(StairBlock.FACING) != Direction.SOUTH) {
            helper.fail("virada para o sul: está virada para " + escada.getValue(StairBlock.FACING));
            return;
        }
        helper.succeed();
    }

    /** <b>Agachado no ar, o cajado esquece tudo.</b> */
    @GameTest(maxTicks = 40)
    public void sneakingInTheAirForgetsEverything(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.CREATIVE);
        ItemStack cajado = new ItemStack(OccultaItems.DUP_STAFF);
        level.setBlockAndUpdate(helper.absolutePos(new BlockPos(2, 2, 2)),
                Blocks.STONE.defaultBlockState());

        bate(helper, cajado, quem, helper.absolutePos(new BlockPos(1, 1, 1)));
        bate(helper, cajado, quem, helper.absolutePos(new BlockPos(4, 4, 4)));
        if (DuplicationStaffItem.molde(cajado) == null) {
            helper.fail("o molde devia estar guardado");
            return;
        }

        quem.setShiftKeyDown(true);
        cajado.getItem().use(level, quem, InteractionHand.MAIN_HAND);
        quem.setShiftKeyDown(false);
        if (DuplicationStaffItem.molde(cajado) != null) helper.fail("e agachado no ar ele esquece");
        if (DuplicationStaffItem.marco(cajado) != null) helper.fail("e esquece o marco também");
        helper.succeed();
    }

    /** <b>A granada sai carimbada</b> com a cara de quem a pediu. */
    @GameTest(maxTicks = 20)
    public void theGrenadeIsStampedWithAFace(GameTestHelper helper) {
        var quem = helper.makeMockServerPlayerInLevel();
        ItemStack granada = DupGrenadeItem.de(quem);
        if (!granada.is(OccultaItems.DUP_GRENADE)) {
            helper.fail("o que sai é uma Granada de Duplicação");
            return;
        }
        if (!quem.getUUID().equals(DupGrenadeItem.deQuem(granada))) {
            helper.fail("com o número de quem a pediu");
            return;
        }
        if (!quem.getName().getString().equals(DupGrenadeItem.nomeDeQuem(granada))) {
            helper.fail("e com o nome dele, que é o que se lê na dica");
            return;
        }
        // uma por carimbar não diz nada
        if (!DupGrenadeItem.nomeDeQuem(new ItemStack(OccultaItems.DUP_GRENADE)).isEmpty()) {
            helper.fail("e uma sem carimbo não diz nada");
        }
        helper.succeed();
    }

    /**
     * <b>O sósia rouba a atenção</b> — e só de quem estava atrás do dono.
     *
     * <p>É tudo o que ele faz, e é por isso que ele existe. Um bicho que já estivesse atrás de outra
     * pessoa não se distrai: a isca é para quem a atirou.
     */
    @GameTest(maxTicks = 60)
    public void theDoubleStealsTheAttention(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        /*
         * De <b>sobrevivência</b>, e não o do nível: um jogador de criativo não se consegue mirar, e um
         * zumbi mandado atrás dele fica sem alvo nenhum. É a armadilha que o porte já conhece.
         */
        var dono = helper.makeMockServerPlayer(GameType.SURVIVAL);

        var sósia = helper.spawn(net.thaumcraft.occulta.OccultaEntities.DECOY, new BlockPos(4, 2, 3));
        sósia.veste(dono);
        if (!dono.getGameProfile().name().equals(sósia.dono())) {
            helper.fail("o sósia é a cara do dono");
            return;
        }
        if (!dono.getName().getString().equals(sósia.getCustomName().getString())) {
            helper.fail("e leva o nome dele");
            return;
        }

        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(5, 2, 3));
        zumbi.setNoAi(true);
        zumbi.setTarget(dono);
        var outro = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(5, 2, 5));
        outro.setNoAi(true);
        outro.setTarget(null);

        if (zumbi.getTarget() != dono) {
            helper.fail("a prova começa com o zumbi atrás do dono");
            return;
        }

        sósia.rouba(level);
        if (zumbi.getTarget() != sósia) {
            helper.fail("quem estava atrás do dono passa a estar atrás do sósia");
            return;
        }
        if (outro.getTarget() != null) {
            helper.fail("e quem não estava atrás dele fica como estava");
            return;
        }

        // e acabado o prazo ele some
        sósia.põePrazo(1);
        helper.runAfterDelay(3, () -> {
            if (sósia.isAlive()) {
                helper.fail("acabado o prazo, ele some");
                return;
            }
            helper.succeed();
        });
    }
}
