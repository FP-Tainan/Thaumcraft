package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.nodes.NodeType;
import net.thaumcraft.block.VisRelayBlock;
import net.thaumcraft.block.entity.ArcaneWorkbenchBlockEntity;
import net.thaumcraft.block.entity.EnergizedNodeBlockEntity;
import net.thaumcraft.block.entity.NodeBlockEntity;
import net.thaumcraft.block.entity.VisRelayBlockEntity;
import net.thaumcraft.entity.AspectOrbEntity;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/** O estabilizador, o transdutor, o nó energizado, os relés e o orbe de aspecto, como na 4.2.3.5. */
public class VisNetGameTest {
    private static AspectList aura() {
        return new AspectList().add(Aspects.AIR, 25).add(Aspects.FIRE, 16).add(Aspects.ORDER, 9);
    }

    private static NodeBlockEntity node(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, TCBlocks.NODE.defaultBlockState());
        NodeBlockEntity node = helper.getBlockEntity(pos, NodeBlockEntity.class);
        node.setup(aura(), NodeType.NORMAL, null);
        return node;
    }

    @GameTest(maxTicks = 60)
    public void theStabilizerLocksTheNodeAbove(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), TCBlocks.NODE_STABILIZER.defaultBlockState());
        NodeBlockEntity locked = node(helper, new BlockPos(1, 2, 1));
        helper.setBlock(new BlockPos(3, 1, 1), TCBlocks.NODE_STABILIZER_ADVANCED.defaultBlockState());
        NodeBlockEntity tight = node(helper, new BlockPos(3, 2, 1));
        helper.setBlock(new BlockPos(5, 1, 1), TCBlocks.NODE_STABILIZER.defaultBlockState());
        helper.setBlock(new BlockPos(6, 1, 1), Blocks.REDSTONE_BLOCK.defaultBlockState());
        NodeBlockEntity powered = node(helper, new BlockPos(5, 2, 1));
        helper.succeedWhen(() -> {
            if (locked.lock() != 1) helper.fail("o comum trava em 1");
            if (tight.lock() != 2) helper.fail("o avançado trava em 2");
            if (powered.lock() != 0) helper.fail("com redstone o estabilizador solta");
        });
    }

    @GameTest(maxTicks = 1300)
    public void theTransducerEnergizesAStabilizedNode(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), TCBlocks.NODE_STABILIZER.defaultBlockState());
        node(helper, new BlockPos(1, 2, 1));
        helper.setBlock(new BlockPos(2, 3, 1), Blocks.REDSTONE_BLOCK.defaultBlockState());
        helper.setBlock(new BlockPos(1, 3, 1), TCBlocks.NODE_CONVERTER.defaultBlockState());
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(TCBlocks.ENERGIZED_NODE, new BlockPos(1, 2, 1));
            EnergizedNodeBlockEntity energized = helper.getBlockEntity(new BlockPos(1, 2, 1), EnergizedNodeBlockEntity.class);
            // a raiz quadrada de cada primordial da aura: ar 25 vira 5, fogo 16 vira 4; a ordem 9 vira 3
            if (energized.visBase().getAmount(Aspects.AIR) != 5 || energized.visBase().getAmount(Aspects.FIRE) != 4) {
                helper.fail("o energizado gera a raiz do que o nó tinha; deu " + energized.visBase());
            }
        });
    }

    /** Estabilizador, nó energizado e transdutor, já prontos, com a fonte na altura dois. */
    private static EnergizedNodeBlockEntity source(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), TCBlocks.NODE_STABILIZER.defaultBlockState());
        helper.setBlock(new BlockPos(1, 3, 1), TCBlocks.NODE_CONVERTER.defaultBlockState());
        helper.setBlock(new BlockPos(1, 2, 1), TCBlocks.ENERGIZED_NODE.defaultBlockState());
        EnergizedNodeBlockEntity energized = helper.getBlockEntity(new BlockPos(1, 2, 1), EnergizedNodeBlockEntity.class);
        energized.setup(aura(), NodeType.NORMAL, null);
        return energized;
    }

    @GameTest(maxTicks = 200)
    public void aRelayHangsOnTheSourceAndPassesVis(GameTestHelper helper) {
        EnergizedNodeBlockEntity energized = source(helper);
        helper.setBlock(new BlockPos(4, 1, 1), Blocks.STONE.defaultBlockState());
        helper.setBlock(new BlockPos(4, 2, 1), TCBlocks.VIS_RELAY.defaultBlockState().setValue(VisRelayBlock.FACING, Direction.UP));
        VisRelayBlockEntity relay = helper.getBlockEntity(new BlockPos(4, 2, 1), VisRelayBlockEntity.class);
        helper.succeedWhen(() -> {
            if (relay.parent() != energized) helper.fail("o relé ainda não se pendurou na fonte");
            int got = relay.consumeVis(Aspects.AIR, 3);
            if (got != 3) helper.fail("o relé passa o ar da fonte; veio " + got);
            if (relay.consumeVis(Aspects.WATER, 3) != 0) helper.fail("água a fonte não tem");
        });
    }

    @GameTest(maxTicks = 200)
    public void theChargerFillsTheWorkbenchWand(GameTestHelper helper) {
        source(helper);
        helper.setBlock(new BlockPos(4, 1, 1), TCBlocks.ARCANE_WORKBENCH.defaultBlockState());
        helper.setBlock(new BlockPos(4, 2, 1), TCBlocks.WORKBENCH_CHARGER.defaultBlockState());
        ArcaneWorkbenchBlockEntity workbench = helper.getBlockEntity(new BlockPos(4, 1, 1), ArcaneWorkbenchBlockEntity.class);
        workbench.setItem(ArcaneWorkbenchBlockEntity.WAND_SLOT, new ItemStack(TCItems.WAND));
        helper.succeedWhen(() -> {
            ItemStack wand = workbench.getItem(ArcaneWorkbenchBlockEntity.WAND_SLOT);
            if (WandItem.vis(wand, Aspects.AIR) <= 0) helper.fail("a varinha da bancada ainda não encheu");
            if (WandItem.vis(wand, Aspects.WATER) != 0) helper.fail("e só do que a rede tem");
        });
    }

    @GameTest
    public void anAspectOrbFillsTheWandInTheHotbar(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().setItem(0, new ItemStack(TCItems.WAND));
        AspectOrbEntity orb = new AspectOrbEntity(helper.getLevel(), player.getX(), player.getY(), player.getZ(), Aspects.FIRE, 2);
        helper.getLevel().addFreshEntity(orb);
        orb.playerTouch(player);
        if (WandItem.vis(player.getInventory().getItem(0), Aspects.FIRE) != 2 * WandItem.VIS_UNIT) helper.fail("o orbe dá dois pontos de fogo");
        if (!orb.isRemoved()) helper.fail("e some");
        helper.succeed();
    }
}
