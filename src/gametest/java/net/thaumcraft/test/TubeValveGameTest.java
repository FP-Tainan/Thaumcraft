package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.TubeValveBlock;
import net.thaumcraft.block.entity.TubeValveBlockEntity;
import net.thaumcraft.registry.TCBlocks;

/**
 * A válvula do cano, com o comportamento do {@code TileTubeValve} original.
 *
 * <p>Fechada, ela continua encaixada nos canos — o original não solta os braços —, mas não puxa nem deixa
 * passar nada. Quem abre e fecha é a mão e a redstone; a varinha gira a roda para outro lado. E o lado da
 * roda nunca conecta.
 */
public class TubeValveGameTest {
    /** A mão abre e fecha; fechada, a válvula continua encaixada mas não deixa passar nada. */
    @GameTest
    public void theHandClosesItWithoutUnplugging(GameTestHelper helper) {
        BlockPos at = new BlockPos(1, 1, 1);
        helper.setBlock(at, TCBlocks.TUBE_VALVE.defaultBlockState().setValue(TubeValveBlock.FACING, Direction.UP));
        TubeValveBlockEntity valve = helper.getBlockEntity(at, TubeValveBlockEntity.class);

        if (!valve.allowsFlow()) helper.fail("a válvula nasce aberta");
        if (valve.isConnectable(Direction.UP)) helper.fail("o lado da roda nunca conecta");

        valve.toggleByHand(helper.getLevel(), helper.absolutePos(at));
        if (valve.allowsFlow()) helper.fail("a mão devia ter fechado a válvula");
        if (!valve.isConnectable(Direction.NORTH)) {
            helper.fail("fechada, ela continua encaixada no cano, como no original");
        }
        if (valve.addEssentia(Aspects.FIRE, 5, Direction.NORTH) != 0) helper.fail("fechada, não entra nada");

        valve.toggleByHand(helper.getLevel(), helper.absolutePos(at));
        if (!valve.allowsFlow()) helper.fail("a mão devia ter reaberto a válvula");
        helper.succeed();
    }

    /** Fechada ela também não guarda sucção nenhuma, senão o resto do encanamento continuaria a lhe mandar. */
    @GameTest
    public void closedItPullsNothing(GameTestHelper helper) {
        BlockPos at = new BlockPos(1, 1, 1);
        helper.setBlock(at, TCBlocks.TUBE_VALVE.defaultBlockState().setValue(TubeValveBlock.FACING, Direction.UP));
        TubeValveBlockEntity valve = helper.getBlockEntity(at, TubeValveBlockEntity.class);

        valve.setSuction(Aspects.FIRE, 40);
        if (valve.getSuctionAmount(Direction.NORTH) != 40) helper.fail("aberta, ela puxa");

        valve.toggleByHand(helper.getLevel(), helper.absolutePos(at));
        if (valve.getSuctionAmount(Direction.NORTH) != 0) {
            helper.fail("fechada, ela não puxa nada; puxou com " + valve.getSuctionAmount(Direction.NORTH));
        }
        valve.setSuction(Aspects.FIRE, 40);
        if (valve.getSuctionAmount(Direction.NORTH) != 0) helper.fail("fechada, ela nem aceita sucção nova");
        helper.succeed();
    }

    /** A varinha não abre nem fecha: ela gira a roda para o próximo lado livre. */
    @GameTest
    public void theWandTurnsTheWheelAside(GameTestHelper helper) {
        BlockPos at = new BlockPos(1, 1, 1);
        helper.setBlock(at, TCBlocks.TUBE_VALVE.defaultBlockState().setValue(TubeValveBlock.FACING, Direction.UP));
        TubeValveBlockEntity valve = helper.getBlockEntity(at, TubeValveBlockEntity.class);

        valve.onWand(helper.getLevel(), net.minecraft.world.item.ItemStack.EMPTY,
                helper.makeMockPlayer(GameType.CREATIVE), helper.absolutePos(at), Direction.UP);
        Direction now = helper.getBlockState(at).getValue(TubeValveBlock.FACING);
        if (now == Direction.UP) helper.fail("a varinha devia ter girado a roda para outro lado");
        if (!valve.allowsFlow()) helper.fail("a varinha não fecha a válvula; isso é com a mão");
        helper.succeed();
    }
}
