package net.thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.world.CropUtils;

import java.util.function.Predicate;

/**
 * O {@code onBlockDestroyed} da pá elemental e do triturador primordial: quebrado um bloco (sem agachar), os oito em
 * volta na face que se olhava caem também, se a ferramenta serve para eles, e o que cai voa até quem cavou.
 */
final class AreaMining {
    private AreaMining() {
    }

    /** A face que o jogador olha ({@code BlockUtils.getTargetBlock}). */
    static int side(Player player) {
        HitResult hit = player.level().clip(new ClipContext(player.getEyePosition(), player.getEyePosition().add(player.getViewVector(1.0f)
                .scale(player.blockInteractionRange())), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        return hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK ? block.getDirection().get3DDataValue() : 0;
    }

    static void mine(ItemStack stack, ServerLevel level, BlockPos pos, LivingEntity miner, Predicate<BlockState> effective, int color) {
        if (!(miner instanceof Player player) || player.isShiftKeyDown()) return;
        int side = side(player);
        for (int aa = -1; aa <= 1; aa++) {
            for (int bb = -1; bb <= 1; bb++) {
                int xx = 0, yy = 0, zz = 0;
                if (side <= 1) {
                    xx = aa;
                    zz = bb;
                } else if (side <= 3) {
                    xx = aa;
                    yy = bb;
                } else {
                    zz = aa;
                    yy = bb;
                }
                BlockPos at = pos.offset(xx, yy, zz);
                if (!level.mayInteract(player, at)) continue;
                BlockState state = level.getBlockState(at);
                if (state.isAir() || state.getDestroySpeed(level, at) < 0.0f || !effective.test(state)) continue;
                stack.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
                CropUtils.harvestBlock(level, player, at, stack, true, color);
            }
        }
    }
}
