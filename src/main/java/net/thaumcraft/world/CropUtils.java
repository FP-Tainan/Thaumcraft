package net.thaumcraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.block.ManaPodBlock;
import net.thaumcraft.registry.TCBlocks;

/**
 * O {@code CropUtils} e o pedaço do {@code BlockUtils} da 4.2.3.5 que colhe e derruba: o que conta como planta madura
 * (para a lâmpada do crescimento, que não mexe nela, e para o golem colhedor, que a colhe), o que é tronco, e a
 * derrubada "do bloco mais longe" com que o machado elemental e o golem lenhador cortam uma árvore aos poucos.
 */
public final class CropUtils {
    private static int lastx, lasty, lastz;
    private static double lastdistance;

    private CropUtils() {
    }

    /**
     * O {@code isGrownCrop}: o que já não aceita farinha de osso (menos os caules), a verruga madura, o cacau maduro,
     * a abóbora e a melancia inteiras, a vagem de mana madura (a {@code addStandardCrop} do original), e a cana e o
     * cacto de cima.
     */
    public static boolean isGrownCrop(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;
        Block block = state.getBlock();
        if (block instanceof BonemealableBlock growable && !growable.isValidBonemealTarget(level, pos, state)
                && !(block instanceof StemBlock)) {
            return true;
        }
        if (block instanceof NetherWartBlock && state.getValue(NetherWartBlock.AGE) >= 3) return true;
        if (block instanceof CocoaBlock && state.getValue(CocoaBlock.AGE) >= 2) return true;
        if (block == Blocks.MELON || block == Blocks.PUMPKIN) return true;
        if (block instanceof ManaPodBlock && state.getValue(ManaPodBlock.AGE) >= 7) return true;
        return (block == Blocks.SUGAR_CANE || block == Blocks.CACTUS) && level.getBlockState(pos.below()).is(block);
    }

    /** O {@code Utils.isWoodLog}: o que segura folha (os troncos). */
    public static boolean isWoodLog(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && state.is(BlockTags.LOGS);
    }

    /** O {@code harvestBlock}: quebra como um jogador quebraria, com o estalo e o que cai. */
    public static boolean harvestBlock(ServerLevel level, Player player, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getDestroySpeed(level, pos) < 0.0f) return false;
        level.levelEvent(2001, pos, Block.getId(state));
        var be = level.getBlockEntity(pos);
        boolean canHarvest = player.hasCorrectToolForDrops(state);
        state.getBlock().playerWillDestroy(level, pos, state, player);
        boolean removed = level.removeBlock(pos, false);
        if (removed) {
            state.getBlock().destroy(level, pos, state);
            if (canHarvest) Block.dropResources(state, level, pos, be, player, ItemStack.EMPTY);
        }
        return true;
    }

    /** O {@code breakFurthestBlock}: acha, seguindo o tronco, o bloco mais longe dele e quebra esse. */
    public static boolean breakFurthestBlock(ServerLevel level, BlockPos pos, Block block, Player player) {
        lastx = pos.getX();
        lasty = pos.getY();
        lastz = pos.getZ();
        lastdistance = 0.0;
        findBlocks(level, pos, block);
        BlockPos last = new BlockPos(lastx, lasty, lastz);
        boolean worked = harvestBlock(level, player, last);
        level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
        if (worked) {
            // as folhas em volta são avisadas, para caírem com o tempo
            for (int xx = -3; xx <= 3; xx++) {
                for (int yy = -3; yy <= 3; yy++) {
                    for (int zz = -3; zz <= 3; zz++) {
                        BlockPos at = last.offset(xx, yy, zz);
                        level.scheduleTick(at, level.getBlockState(at).getBlock(), 150 + level.getRandom().nextInt(150));
                    }
                }
            }
        }
        return worked;
    }

    private static void findBlocks(Level level, BlockPos origin, Block block) {
        for (int xx = -2; xx <= 2; xx++) {
            for (int yy = 2; yy >= -2; yy--) {
                for (int zz = -2; zz <= 2; zz++) {
                    if (Math.abs(lastx + xx - origin.getX()) > 24) return;
                    if (Math.abs(lasty + yy - origin.getY()) > 48) return;
                    if (Math.abs(lastz + zz - origin.getZ()) > 24) return;
                    BlockPos at = new BlockPos(lastx + xx, lasty + yy, lastz + zz);
                    BlockState there = level.getBlockState(at);
                    if (there.is(block) && isWoodLog(level, at) && there.getDestroySpeed(level, at) >= 0.0f) {
                        double xd = lastx + xx - origin.getX();
                        double yd = lasty + yy - origin.getY();
                        double zd = lastz + zz - origin.getZ();
                        double d = xd * xd + yd * yd + zd * zd;
                        if (d > lastdistance) {
                            lastdistance = d;
                            lastx += xx;
                            lasty += yy;
                            lastz += zz;
                            findBlocks(level, origin, block);
                            return;
                        }
                    }
                }
            }
        }
    }

    /** A vagem de mana pode ser pendurada ali (há galho de árvore mágica em cima)? */
    public static boolean manaPodFits(Level level, BlockPos pos) {
        return TCBlocks.MANA_POD.defaultBlockState().canSurvive(level, pos) && level.getBlockState(pos).canBeReplaced();
    }

    /** Onde fica o tronco em que o cacau está preso. */
    public static BlockPos cocoaLog(BlockState cocoa, BlockPos pos) {
        Direction facing = cocoa.getValue(CocoaBlock.FACING);
        return pos.relative(facing);
    }
}
