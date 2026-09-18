package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.thaumcraft.block.TubeValveBlock;
import net.thaumcraft.block.entity.TubeValveBlockEntity;
import net.thaumcraft.registry.TCBlocks;

/**
 * A válvula do cano.
 *
 * <p>Fechada, ela tem de parar tudo: nem conectar, nem puxar. E o lado onde fica o manípulo nunca conecta,
 * aberta ou fechada — é o {@code facing} do {@code TileTubeValve} do original, e é por isso que a válvula
 * se põe com o manípulo virado para fora do encanamento.
 */
public class TubeValveGameTest {
    /** Aberta ela é um tubo comum; fechada não liga com nada. */
    @GameTest
    public void closedItStopsEverything(GameTestHelper helper) {
        BlockPos at = new BlockPos(1, 1, 1);
        helper.setBlock(at, TCBlocks.TUBE_VALVE.defaultBlockState()
                .setValue(TubeValveBlock.FACING, Direction.UP));
        TubeValveBlockEntity valve = helper.getBlockEntity(at, TubeValveBlockEntity.class);

        if (!valve.allowsFlow()) helper.fail("a válvula nasce aberta");
        if (!valve.isConnectable(Direction.NORTH)) helper.fail("aberta, ela liga pelos lados");
        if (valve.isConnectable(Direction.UP)) helper.fail("o lado do manípulo nunca liga");

        valve.onWand(helper.getLevel(), net.minecraft.world.item.ItemStack.EMPTY,
                helper.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE), helper.absolutePos(at),
                Direction.UP);
        if (valve.allowsFlow()) helper.fail("a varinha devia ter fechado a válvula");
        if (valve.isConnectable(Direction.NORTH)) helper.fail("fechada, ela não liga com ninguém");
        helper.succeed();
    }

    /** Fechada ela também não guarda sucção nenhuma, senão o resto do encanamento continuaria a lhe mandar. */
    @GameTest
    public void closedItPullsNothing(GameTestHelper helper) {
        BlockPos at = new BlockPos(1, 1, 1);
        helper.setBlock(at, TCBlocks.TUBE_VALVE.defaultBlockState()
                .setValue(TubeValveBlock.FACING, Direction.UP));
        TubeValveBlockEntity valve = helper.getBlockEntity(at, TubeValveBlockEntity.class);

        valve.setSuction(net.thaumcraft.api.aspects.Aspects.FIRE, 40);
        if (valve.getSuctionAmount(Direction.NORTH) != 40) helper.fail("aberta, ela puxa");

        valve.onWand(helper.getLevel(), net.minecraft.world.item.ItemStack.EMPTY,
                helper.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE), helper.absolutePos(at),
                Direction.UP);
        if (valve.getSuctionAmount(Direction.NORTH) != 0) {
            helper.fail("fechada, ela não puxa nada; puxou com " + valve.getSuctionAmount(Direction.NORTH));
        }
        valve.setSuction(net.thaumcraft.api.aspects.Aspects.FIRE, 40);
        if (valve.getSuctionAmount(Direction.NORTH) != 0) helper.fail("fechada, ela nem aceita sucção nova");
        helper.succeed();
    }
}
