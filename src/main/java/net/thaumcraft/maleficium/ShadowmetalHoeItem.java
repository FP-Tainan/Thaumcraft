package net.thaumcraft.maleficium;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A enxada de metal das sombras: o {@code ItemShadowmetalHoe} do Tainted Magic.
 *
 * <p>Ela não ara como as outras — ela vira: terra ou grama viram terra arada, e terra arada volta a ser terra. O
 * original desliga a aração de sempre para fazer isso, e aqui é igual.
 */
public class ShadowmetalHoeItem extends Item {
    public ShadowmetalHoeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        BlockState turned;
        if (state.is(Blocks.FARMLAND)) {
            turned = Blocks.DIRT.defaultBlockState();
        } else if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK)) {
            turned = Blocks.FARMLAND.defaultBlockState();
        } else {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlockAndUpdate(pos, turned);
            level.playSound(null, pos, SoundEvents.GRAVEL_STEP, SoundSource.BLOCKS, 1.0f, 0.8f);
            if (context.getPlayer() != null) {
                context.getItemInHand().hurtAndBreak(1, context.getPlayer(), net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
