package net.thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.registry.TCItems;

/**
 * O que acontece quando se bate com a varinha em certas coisas do mundo.
 *
 * <p>É como o Thaumcraft 4.2.3.5 entrega as primeiras peças: não há receita de bancada para elas. Bate-se
 * com a varinha numa estante de livros e ela vira o Thaumonomicon; o resto das peças de taumaturgia sai
 * do mesmo jeito, cada uma do seu bloco, e vai entrando conforme as fatias chegarem.
 */
public final class WandTriggers {
    private WandTriggers() {
    }

    /**
     * Tenta o que a varinha faria neste bloco.
     *
     * @return o que dizer ao jogo, ou {@code PASS} se aquele bloco não responde à varinha
     */
    public static InteractionResult use(Level level, Player player, BlockPos pos, ItemStack wand) {
        if (!(wand.getItem() instanceof WandItem)) return InteractionResult.PASS;
        BlockState state = level.getBlockState(pos);

        // a estante de livros vira o caderno de pesquisa
        if (state.is(Blocks.BOOKSHELF)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            level.removeBlock(pos, false);
            net.minecraft.world.entity.item.ItemEntity book = new net.minecraft.world.entity.item.ItemEntity(
                    level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    new ItemStack(TCItems.THAUMONOMICON));
            level.addFreshEntity(book);
            level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.7f, 1.0f);
            net.thaumcraft.research.ResearchManager.grantStarters(player);
            return InteractionResult.SUCCESS;
        }
        // o caldeirão comum vira crisol, como no original
        if (state.is(Blocks.CAULDRON) || state.is(Blocks.WATER_CAULDRON)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            boolean full = state.is(Blocks.WATER_CAULDRON);
            level.setBlockAndUpdate(pos, net.thaumcraft.registry.TCBlocks.CRUCIBLE.defaultBlockState());
            if (full && level.getBlockEntity(pos) instanceof net.thaumcraft.block.entity.CrucibleBlockEntity crucible) {
                crucible.setWater(true);
            }
            level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.7f, 0.8f);
            return InteractionResult.SUCCESS;
        }
        // a bancada comum vira bancada arcana, como a mesa do original
        if (state.is(Blocks.CRAFTING_TABLE)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            level.setBlockAndUpdate(pos, net.thaumcraft.registry.TCBlocks.ARCANE_WORKBENCH.defaultBlockState());
            level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.7f, 1.1f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
