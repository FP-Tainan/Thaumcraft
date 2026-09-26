package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A Pasta de Unção: o {@code useAnnointingPaste} do {@code ItemGeneral} do Witchery.
 *
 * <p>Passada num caldeirão comum, faz dele o <b>Caldeirão da Bruxa</b>. É a única maneira de o ter, como no
 * original — e é feita das quatro sementes que o mato dá.
 */
public class AnointingPasteItem extends Item {
    public AnointingPasteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos onde = context.getClickedPos();
        BlockState qual = level.getBlockState(onde);
        if (!qual.is(Blocks.CAULDRON) && !qual.is(Blocks.WATER_CAULDRON)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        level.setBlock(onde, OccultaBlocks.WITCHES_CAULDRON.defaultBlockState(), Block.UPDATE_ALL);
        // a água que o caldeirão comum já tinha não se perde
        if (qual.is(Blocks.WATER_CAULDRON)
                && level.getBlockEntity(onde) instanceof WitchesCauldronBlockEntity caldeirão) {
            int nível = qual.getValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL);
            caldeirão.fill(WitchesCauldronBlockEntity.BUCKET * nível);
        }
        context.getItemInHand().consume(1, context.getPlayer());

        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER, onde.getX() + 0.5, onde.getY() + 0.9, onde.getZ() + 0.5,
                    16, 0.4, 0.4, 0.4, 0.0);
        }
        level.playSound(null, onde, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.6f);
        level.playSound(null, onde, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.6f, 1.2f);
        return InteractionResult.SUCCESS;
    }
}
