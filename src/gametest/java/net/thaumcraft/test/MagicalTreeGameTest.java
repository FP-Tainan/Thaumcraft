package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.nodes.NodeType;
import net.thaumcraft.block.MagicalSaplingBlock;
import net.thaumcraft.block.entity.NodeBlockEntity;
import net.thaumcraft.registry.TCBlocks;

/** As árvores mágicas têm de crescer como o {@code WorldGenGreatwoodTrees} e o {@code WorldGenSilverwoodTrees}. */
public class MagicalTreeGameTest {
    /** As árvores crescem bem acima da área do teste, para não cair em cima dos vizinhos. */
    private static final int SKY = 120;

    @GameTest
    public void theGreatwoodSaplingGrowsABigTree(GameTestHelper helper) {
        this.grow(helper, TCBlocks.GREATWOOD_SAPLING, TCBlocks.GREATWOOD_LOG, TCBlocks.GREATWOOD_LEAVES, 40);
    }

    @GameTest
    public void theSilverwoodSaplingGrowsAWhiteTree(GameTestHelper helper) {
        this.grow(helper, TCBlocks.SILVERWOOD_SAPLING, TCBlocks.SILVERWOOD_LOG, TCBlocks.SILVERWOOD_LEAVES, 30);
    }

    private void grow(GameTestHelper helper, net.minecraft.world.level.block.Block sapling,
                      net.minecraft.world.level.block.Block log, net.minecraft.world.level.block.Block leaf, int minLogs) {
        var level = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(1, 1, 1)).above(SKY);
        for (int x = -1; x <= 2; x++) {
            for (int z = -1; z <= 2; z++) level.setBlock(base.offset(x, -1, z), Blocks.DIRT.defaultBlockState(), 3);
        }
        level.setBlock(base, sapling.defaultBlockState(), 3);
        boolean grew = false;
        for (int tries = 0; tries < 20 && !grew; tries++) {
            grew = ((MagicalSaplingBlock) sapling).grow(level, base, sapling.defaultBlockState(), level.getRandom());
        }
        if (!grew) helper.fail("a muda devia virar árvore com o céu livre em cima");

        int logs = 0, leaves = 0, loose = 0;
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-16, -2, -16), base.offset(17, 40, 17))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(log) || state.is(TCBlocks.SILVERWOOD_KNOT)) logs++;
            if (state.is(leaf)) {
                leaves++;
                if (state.getValue(LeavesBlock.DISTANCE) >= LeavesBlock.DECAY_DISTANCE) loose++;
            }
        }
        if (logs < minLogs) helper.fail("tronco pequeno demais: " + logs + " toras");
        if (leaves < 40) helper.fail("copa rala demais: " + leaves + " folhas");
        // a folha tem de saber que está presa ao tronco, senão apodrece no primeiro tique
        if (loose * 10 > leaves) helper.fail(loose + " de " + leaves + " folhas acham que estão soltas");
        if (!level.getBlockState(base.below()).is(BlockTags.SUPPORTS_VEGETATION)) helper.fail("o pé da árvore fica na terra");
        helper.succeed();
    }

    @GameTest(maxTicks = 20)
    public void theKnotHoldsAPureNode(GameTestHelper helper) {
        BlockPos knot = new BlockPos(1, 2, 1);
        helper.setBlock(knot, TCBlocks.SILVERWOOD_KNOT.defaultBlockState());
        helper.succeedWhen(() -> {
            if (!(helper.getBlockEntity(knot, NodeBlockEntity.class) instanceof NodeBlockEntity node)) {
                throw helper.assertionException("o nó do tronco guarda um nó de aura");
            }
            if (node.aspects().isEmpty()) throw helper.assertionException("o nó ainda não se formou");
            if (node.type() != NodeType.PURE) throw helper.assertionException("o nó do pinheiro é sempre puro");
        });
    }

    @GameTest
    public void theCinderpearlLivesOnSand(GameTestHelper helper) {
        BlockPos sand = new BlockPos(1, 1, 1);
        helper.setBlock(sand, Blocks.SAND.defaultBlockState());
        if (!TCBlocks.CINDERPEARL.defaultBlockState().canSurvive(helper.getLevel(), helper.absolutePos(sand.above()))) {
            helper.fail("a pérola de cinzas é planta de deserto e pega na areia");
        }
        helper.setBlock(sand, Blocks.STONE.defaultBlockState());
        if (TCBlocks.CINDERPEARL.defaultBlockState().canSurvive(helper.getLevel(), helper.absolutePos(sand.above()))) {
            helper.fail("na pedra ela não pega");
        }
        helper.succeed();
    }
}
