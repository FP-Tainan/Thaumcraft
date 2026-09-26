package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.shattered.PocketThemes;
import net.thaumcraft.shattered.Pockets;
import net.thaumcraft.shattered.RiftBlockEntity;
import net.thaumcraft.shattered.ShatteredBlocks;
import net.thaumcraft.shattered.ShatteredItems;
import net.thaumcraft.shattered.ShatteredRealms;

/**
 * Da fenda presa à sala com tema.
 *
 * <p>A regra que quem joga pediu: para se abrir uma porta é preciso <b>prender a fenda primeiro</b>. Presa, a
 * porta toma-lhe o lugar e fica brava, e o que espera do outro lado já não é o bolso liso do original — é uma
 * das salas com tema, e dessa saem outras portas para outras salas.
 */
public class WildPocketGameTest {
    /** Numa fenda solta a porta não pega, e quem a tentou assentar fica com ela na mão. */
    @GameTest
    public void theLooseRiftTakesNoDoor(GameTestHelper helper) {
        BlockPos onde = new BlockPos(1, 2, 1);
        helper.setBlock(onde, ShatteredBlocks.RIFT);
        helper.setBlock(onde.below(), Blocks.STONE);

        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        var pilha = new ItemStack(ShatteredBlocks.OAK_DIMENSIONAL_DOOR, 2);
        usa(helper, quem, pilha, onde);

        helper.assertBlockPresent(ShatteredBlocks.RIFT, onde);
        if (pilha.getCount() != 2) helper.fail("a porta havia de ficar na mão: sobraram " + pilha.getCount());
        helper.succeed();
    }

    /** Presa pelo Firma-Fendas, a fenda aceita a porta, e a porta fica brava. */
    @GameTest
    public void theHeldRiftTakesTheDoor(GameTestHelper helper) {
        BlockPos onde = new BlockPos(1, 2, 1);
        helper.setBlock(onde, ShatteredBlocks.RIFT);
        helper.setBlock(onde.below(), Blocks.STONE);
        helper.getBlockEntity(onde, RiftBlockEntity.class).setStabilized(true);

        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        var pilha = new ItemStack(ShatteredBlocks.OAK_DIMENSIONAL_DOOR, 2);
        usa(helper, quem, pilha, onde);

        helper.assertBlockPresent(ShatteredBlocks.OAK_DIMENSIONAL_DOOR, onde);
        var fenda = helper.getBlockEntity(onde, RiftBlockEntity.class);
        if (!fenda.wild()) helper.fail("a porta que tomou uma fenda do mundo é brava");
        if (fenda.natural()) helper.fail("mas foi alguém que a assentou, e por isso está à vista");
        if (pilha.getCount() != 1) helper.fail("e gastou uma porta: sobraram " + pilha.getCount());
        helper.succeed();
    }

    /** Uma porta comum, longe de fendas, assenta-se como sempre e não fica brava. */
    @GameTest
    public void thePlainDoorIsStillPlain(GameTestHelper helper) {
        BlockPos chão = new BlockPos(3, 1, 3);
        helper.setBlock(chão, Blocks.STONE);
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        var pilha = new ItemStack(ShatteredBlocks.OAK_DIMENSIONAL_DOOR);
        usa(helper, quem, pilha, chão);

        var fenda = helper.getBlockEntity(chão.above(), RiftBlockEntity.class);
        if (fenda.wild()) helper.fail("uma porta feita na bancada não é brava");
        helper.succeed();
    }

    /** O bolso liso continua liso: paredes de tecido e uma porta só, a de volta. */
    @GameTest
    public void thePlainPocketHasOneDoor(GameTestHelper helper) {
        var bolsos = Pockets.level(helper.getLevel().getServer());
        if (bolsos == null) {
            helper.fail("o mundo dos bolsos devia abrir");
            return;
        }
        var destino = Pockets.open(helper.getLevel(), helper.absolutePos(new BlockPos(1, 2, 1)), false, null);
        if (destino == null) {
            helper.fail("o bolso devia abrir");
            return;
        }
        if (portas(bolsos, destino.pos()) != 1) {
            helper.fail("o bolso liso tem uma porta só; achei " + portas(bolsos, destino.pos()));
        }
        net.thaumcraft.world.DynamicDimensions.remove(helper.getLevel().getServer(), ShatteredRealms.PUBLIC_POCKETS);
        helper.succeed();
    }

    /** O bolso bravo tem tema, três portas, e as duas de lado ainda não apontam para lugar nenhum. */
    @GameTest
    public void theWildPocketHasThemeAndTwoWaysOn(GameTestHelper helper) {
        var bolsos = Pockets.level(helper.getLevel().getServer());
        if (bolsos == null) {
            helper.fail("o mundo dos bolsos devia abrir");
            return;
        }
        var destino = Pockets.open(helper.getLevel(), helper.absolutePos(new BlockPos(1, 2, 1)), true, null);
        if (destino == null) {
            helper.fail("o bolso devia abrir");
            return;
        }
        int quantas = portas(bolsos, destino.pos());
        if (quantas != 3) helper.fail("o bolso bravo tem a de volta e mais duas; achei " + quantas);

        int semDestino = 0;
        PocketThemes tema = null;
        for (BlockPos onde : volta(destino.pos())) {
            if (!(bolsos.getBlockEntity(onde) instanceof RiftBlockEntity fenda)) continue;
            if (!fenda.wild() && fenda.destination() == null) helper.fail("a de volta sabe para onde vai");
            if (fenda.destination() == null) semDestino++;
            if (fenda.theme() != null) tema = fenda.theme();
            if (fenda.natural()) helper.fail("as portas da sala estão à vista de quem lá cair");
        }
        if (semDestino != 2) helper.fail("as duas de lado ainda não apontam; achei " + semDestino);
        if (tema == null) helper.fail("o bolso bravo tem tema");

        net.thaumcraft.world.DynamicDimensions.remove(helper.getLevel().getServer(), ShatteredRealms.PUBLIC_POCKETS);
        helper.succeed();
    }

    /** E dois temas seguidos nunca são o mesmo. */
    @GameTest
    public void theNextRoomIsNeverTheSame(GameTestHelper helper) {
        var sorte = helper.getLevel().getRandom();
        for (PocketThemes veioDe : PocketThemes.values()) {
            for (int volta = 0; volta < 40; volta++) {
                if (PocketThemes.roll(sorte, veioDe) == veioDe) {
                    helper.fail("a sala seguinte não repete a de onde se veio: " + veioDe);
                    return;
                }
            }
        }
        helper.succeed();
    }

    /** As portas de um bolso, à volta de onde quem chega aparece. */
    private static int portas(net.minecraft.server.level.ServerLevel bolsos, BlockPos perto) {
        int achadas = 0;
        for (BlockPos onde : volta(perto)) {
            if (bolsos.getBlockState(onde).is(ShatteredBlocks.OAK_DIMENSIONAL_DOOR)
                    && bolsos.getBlockEntity(onde) instanceof RiftBlockEntity) {
                achadas++;
            }
        }
        return achadas;
    }

    /** A sala inteira, à volta do lugar de chegada. */
    private static Iterable<BlockPos> volta(BlockPos perto) {
        return BlockPos.betweenClosed(
                perto.offset(-Pockets.ROOM, -2, -Pockets.ROOM),
                perto.offset(Pockets.ROOM, Pockets.HEIGHT, Pockets.ROOM));
    }

    private static void usa(GameTestHelper helper, Player quem, ItemStack coisa, BlockPos onde) {
        quem.setItemInHand(InteractionHand.MAIN_HAND, coisa);
        var alvo = new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(onde)), Direction.UP,
                helper.absolutePos(onde), false);
        coisa.useOn(new UseOnContext(helper.getLevel(), quem, InteractionHand.MAIN_HAND, coisa, alvo));
    }
}
