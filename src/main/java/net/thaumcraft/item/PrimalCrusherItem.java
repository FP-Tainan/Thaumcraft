package net.thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.research.WarpEvents;

/**
 * O triturador primordial: o {@code ItemPrimalCrusher} da 4.2.3.5. Picareta e pá de uma vez, de metal do vazio: cava três
 * por três na face que se olha (o que cai voa até quem cavou), o minério às vezes cai como aglomerado, conserta-se sozinho
 * um ponto por segundo e distorce quem o carrega (dois).
 */
public class PrimalCrusherItem extends Item implements WarpEvents.WarpingGear {
    public PrimalCrusherItem(Properties properties) {
        super(properties);
    }

    /** O {@code func_150897_b}: tudo o que picareta ou pá cavam, menos madeira, folhas e plantas; e a mácula. */
    private static boolean effective(BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.LEAVES)) return false;
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL) || state.is(TCBlocks.TAINT_FIBRES)
                || state.getBlock() instanceof net.thaumcraft.block.TaintBlock;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return effective(state) ? 8.0f : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return effective(state) || super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (level instanceof ServerLevel server && effective(state)) AreaMining.mine(stack, server, pos, miner, PrimalCrusherItem::effective, 2);
        return super.mineBlock(stack, level, state, pos, miner);
    }

    /** O {@code onUpdate}: um ponto consertado por segundo. */
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (stack.isDamaged() && entity instanceof LivingEntity && entity.tickCount % 20 == 0) stack.setDamageValue(stack.getDamageValue() - 1);
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return 2;
    }
}
