package net.thaumcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.nodes.NodeModifier;
import net.thaumcraft.block.entity.NodeBlockEntity;
import net.thaumcraft.block.entity.NodeJarBlockEntity;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * O jarro para nós: o gatilho quatro da varinha, o {@code createNodeJar}/{@code fitNodeJar}/{@code replaceNodeJar} do
 * {@code WandManager} da 4.2.3.5. Uma caixa de vidro de três por três por três com o nó no meio e uma tampa de lajes
 * de madeira; a varinha no vidro, com a pesquisa NODEJAR e 70 de cada primário, encolhe tudo num jarro com o nó dentro.
 * Três vezes em quatro o nó sai um tanto mais fraco (sem feitio vira pálido, o brilhante perde o brilho, o pálido
 * esmaece).
 */
public final class NodeJarStructure {
    /** Por camada, de cima para baixo: 1 laje de madeira, 2 vidro, 3 o nó. */
    private static final int[][][] BLUEPRINT = {
            {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}},
            {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}},
            {{2, 2, 2}, {2, 3, 2}, {2, 2, 2}},
            {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}}};

    private NodeJarStructure() {
    }

    public static boolean isTrigger(BlockState state) {
        return state.is(Blocks.GLASS);
    }

    private static BlockPos at(BlockPos anchor, int xx, int yy, int zz) {
        return anchor.offset(xx, -yy + 2, zz);
    }

    /** O {@code fitNodeJar}: a construção inteira, a partir de um canto. */
    public static boolean fits(Level level, BlockPos anchor) {
        for (int yy = 0; yy < 4; yy++) {
            for (int xx = 0; xx < 3; xx++) {
                for (int zz = 0; zz < 3; zz++) {
                    BlockPos pos = at(anchor, xx, yy, zz);
                    BlockState state = level.getBlockState(pos);
                    int kind = BLUEPRINT[yy][xx][zz];
                    if (kind == 1 && !state.is(BlockTags.WOODEN_SLABS)) return false;
                    if (kind == 2 && !state.is(Blocks.GLASS)) return false;
                    if (kind == 3 && (!(level.getBlockEntity(pos) instanceof NodeBlockEntity) || level.getBlockEntity(pos) instanceof NodeJarBlockEntity)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** O {@code createNodeJar}: procura a construção em volta do vidro batido e, cabendo o custo, faz o jarro. */
    public static boolean create(ItemStack wand, Player player, Level level, BlockPos pos) {
        for (int xx = pos.getX() - 2; xx <= pos.getX(); xx++) {
            for (int yy = pos.getY() - 3; yy <= pos.getY(); yy++) {
                for (int zz = pos.getZ() - 2; zz <= pos.getZ(); zz++) {
                    BlockPos anchor = new BlockPos(xx, yy, zz);
                    if (fits(level, anchor) && WandItem.consume(wand, new AspectList().add(Aspects.FIRE, 70).add(Aspects.EARTH, 70)
                            .add(Aspects.ORDER, 70).add(Aspects.AIR, 70).add(Aspects.ENTROPY, 70).add(Aspects.WATER, 70), true, player)) {
                        replace(level, anchor);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** O {@code replaceNodeJar}: o vidro e as lajes somem e o nó vira o jarro. */
    public static void replace(Level level, BlockPos anchor) {
        for (int yy = 0; yy < 4; yy++) {
            for (int xx = 0; xx < 3; xx++) {
                for (int zz = 0; zz < 3; zz++) {
                    BlockPos pos = at(anchor, xx, yy, zz);
                    if (BLUEPRINT[yy][xx][zz] != 3) {
                        level.removeBlock(pos, false);
                        continue;
                    }
                    if (!(level.getBlockEntity(pos) instanceof NodeBlockEntity node)) continue;
                    AspectList aspects = node.aspects().copy();
                    var type = node.type();
                    NodeModifier modifier = weaker(node.modifier(), level);
                    level.removeBlockEntity(pos);
                    level.setBlock(pos, TCBlocks.NODE_JAR.defaultBlockState(), Block.UPDATE_ALL);
                    if (level.getBlockEntity(pos) instanceof NodeJarBlockEntity jar) jar.setup(aspects, type, modifier);
                    level.blockEvent(pos, TCBlocks.NODE_JAR, 9, 0);
                }
            }
        }
        level.playSound(null, anchor, TCSounds.WAND.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    /** Três vezes em quatro o nó enfraquece um passo. */
    static @Nullable NodeModifier weaker(@Nullable NodeModifier modifier, Level level) {
        if (level.getRandom().nextFloat() >= 0.75f) return modifier;
        if (modifier == null) return NodeModifier.PALE;
        if (modifier == NodeModifier.BRIGHT) return null;
        if (modifier == NodeModifier.PALE) return NodeModifier.FADING;
        return modifier;
    }
}
