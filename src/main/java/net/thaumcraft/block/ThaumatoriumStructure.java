package net.thaumcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.net.TCNetwork;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCSounds;

/**
 * O {@code createThaumatorium} do {@code WandManager} da 4.2.3.5: a varinha em qualquer uma de duas construções
 * alquímicas empilhadas sobre um crisol gasta 15 de Ignis, 30 de Ordo e 30 de Aqua e as transforma no taumatório,
 * virado para a face em que se bateu.
 */
public final class ThaumatoriumStructure {
    private ThaumatoriumStructure() {
    }

    public static boolean create(ItemStack wand, Player player, Level level, BlockPos pos, Direction side) {
        BlockPos bottom;
        if (level.getBlockState(pos.above()).is(TCBlocks.ALCHEMICAL_CONSTRUCT) && level.getBlockState(pos.below()).is(TCBlocks.CRUCIBLE)) {
            bottom = pos;
        } else if (level.getBlockState(pos.below()).is(TCBlocks.ALCHEMICAL_CONSTRUCT) && level.getBlockState(pos.below(2)).is(TCBlocks.CRUCIBLE)) {
            bottom = pos.below();
        } else {
            return false;
        }
        if (!WandItem.consume(wand, new AspectList().add(Aspects.FIRE, 15).add(Aspects.ORDER, 30).add(Aspects.WATER, 30), true, player)) {
            return false;
        }
        build(level, bottom, side);
        if (level instanceof ServerLevel server) {
            TCNetwork.blockSparkle(server, bottom, -9999);
            TCNetwork.blockSparkle(server, bottom.above(), -9999);
        }
        level.playSound(null, bottom, TCSounds.WAND.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
        return true;
    }

    /** As duas metades, sem avisar os vizinhos até as duas estarem no lugar. */
    public static void build(Level level, BlockPos bottom, Direction side) {
        var base = TCBlocks.THAUMATORIUM.defaultBlockState().setValue(ThaumatoriumBlock.FACING, side);
        level.setBlock(bottom, base.setValue(ThaumatoriumBlock.TOP, false), Block.UPDATE_CLIENTS);
        level.setBlock(bottom.above(), base.setValue(ThaumatoriumBlock.TOP, true), Block.UPDATE_CLIENTS);
        level.updateNeighborsAt(bottom, TCBlocks.THAUMATORIUM);
        level.updateNeighborsAt(bottom.above(), TCBlocks.THAUMATORIUM);
    }
}
