package net.thaumcraft.maleficium;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCSounds;

/**
 * O adubo da distorção: o {@code ItemWarpFertilizer} do Tainted Magic.
 *
 * <p>Posto numa muda de madeira-prata, ela se torce e vira uma muda de madeira distorcida — é assim que a árvore
 * entra no mundo, já que ela não nasce sozinha em lugar nenhum.
 */
public class WarpFertilizerItem extends Item {
    /** Os efeitos do lado de quem vê, que o cliente pendura aqui ao abrir. */
    public interface ClientEffects {
        void twist(Level level, BlockPos pos, int count);
    }

    public static ClientEffects clientEffects = (level, pos, count) -> {
    };

    public WarpFertilizerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(TCBlocks.SILVERWOOD_SAPLING)) return InteractionResult.PASS;
        if (level.isClientSide()) {
            clientEffects.twist(level, pos, 10 + level.getRandom().nextInt(11));
        } else {
            level.playSound(null, pos, TCSounds.ROOTS.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
            level.setBlock(pos, MaleficiumBlocks.WARPWOOD_SAPLING.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
        }
        context.getItemInHand().consume(1, context.getPlayer());
        return InteractionResult.SUCCESS;
    }
}
