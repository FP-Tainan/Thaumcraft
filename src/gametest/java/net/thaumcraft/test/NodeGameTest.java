package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.nodes.NodeModifier;
import net.thaumcraft.api.nodes.NodeType;
import net.thaumcraft.block.entity.NodeBlockEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.world.NodeFeature;

import java.util.EnumMap;
import java.util.Map;

/**
 * As contas do nó de aura têm de ser as do Thaumcraft 4.2.3.5.
 */
public class NodeGameTest {
    /** O tempo que cada feitio leva para devolver um ponto é o do original. */
    @GameTest
    public void regenerationMatchesTheOriginal(GameTestHelper helper) {
        NodeBlockEntity node = place(helper, new AspectList().add(Aspects.AIR, 10), NodeType.NORMAL, null);
        if (node.regenerationInterval() != NodeBlockEntity.REGEN_NORMAL) {
            helper.fail("o nó comum devia esperar " + NodeBlockEntity.REGEN_NORMAL + " tiques");
        }
        node.setup(new AspectList().add(Aspects.AIR, 10), NodeType.NORMAL, NodeModifier.BRIGHT);
        if (node.regenerationInterval() != NodeBlockEntity.REGEN_BRIGHT) helper.fail("o brilhante é mais rápido");
        node.setup(new AspectList().add(Aspects.AIR, 10), NodeType.NORMAL, NodeModifier.PALE);
        if (node.regenerationInterval() != NodeBlockEntity.REGEN_PALE) helper.fail("o pálido é mais lento");
        node.setup(new AspectList().add(Aspects.AIR, 10), NodeType.NORMAL, NodeModifier.FADING);
        if (node.regenerationInterval() != 0) helper.fail("o esmaecido não se refaz nunca mais");
        helper.succeed();
    }

    /** Tirar e devolver vis respeita o teto de berço do nó. */
    @GameTest
    public void visNeverPassesWhatTheNodeHolds(GameTestHelper helper) {
        NodeBlockEntity node = place(helper, new AspectList().add(Aspects.AIR, 10), NodeType.NORMAL, null);
        if (!node.take(Aspects.AIR, 4)) helper.fail("devia dar para tirar quatro de dez");
        if (node.aspects().getAmount(Aspects.AIR) != 6) helper.fail("deviam sobrar seis");
        if (node.take(Aspects.AIR, 7)) helper.fail("não devia dar para tirar sete de seis");

        if (node.give(Aspects.AIR, 10) != 4) helper.fail("só cabem mais quatro, até o teto de berço");
        if (node.aspects().getAmount(Aspects.AIR) != 10) helper.fail("devia estar cheio de novo");
        if (node.give(Aspects.AIR, 1) != 0) helper.fail("cheio não cabe mais nada");
        helper.succeed();
    }

    /** Os sorteios de tipo e feitio caem nas mesmas proporções do original. */
    @GameTest
    public void typesAndModifiersAreRolledLikeTheOriginal(GameTestHelper helper) {
        RandomSource random = RandomSource.create(1234L);
        Map<NodeType, Integer> types = new EnumMap<>(NodeType.class);
        int withModifier = 0;
        int rolls = 20000;
        for (int i = 0; i < rolls; i++) {
            types.merge(NodeFeature.rollType(random), 1, Integer::sum);
            if (NodeFeature.rollModifier(random) != null) withModifier++;
        }
        // um em dezoito sai fora do comum: sobram dezessete em dezoito comuns
        int normal = types.getOrDefault(NodeType.NORMAL, 0);
        double normalShare = normal / (double) rolls;
        if (normalShare < 0.92 || normalShare > 0.97) {
            helper.fail("o comum devia ficar perto de dezessete em dezoito, ficou em " + normalShare);
        }
        // faminto é um em dez dos fora do comum; escuro, instável e puro são três em dez cada
        int hungry = types.getOrDefault(NodeType.HUNGRY, 0);
        int dark = types.getOrDefault(NodeType.DARK, 0);
        if (dark < hungry * 2) helper.fail("o escuro devia ser bem mais comum que o faminto");
        // um em nove ganha feitio
        double modShare = withModifier / (double) rolls;
        if (modShare < 0.09 || modShare > 0.13) {
            helper.fail("um em nove devia ganhar feitio, deu " + modShare);
        }
        helper.succeed();
    }

    private static NodeBlockEntity place(GameTestHelper helper, AspectList aspects, NodeType type, NodeModifier mod) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, TCBlocks.NODE.defaultBlockState());
        NodeBlockEntity node = helper.getBlockEntity(pos, NodeBlockEntity.class);
        node.setup(aspects, type, mod);
        return node;
    }
}
