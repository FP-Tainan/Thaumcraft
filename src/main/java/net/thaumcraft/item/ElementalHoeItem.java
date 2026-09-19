package net.thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.thaumcraft.block.MagicalSaplingBlock;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCSounds;

/**
 * A enxada do núcleo de terra: o {@code ItemElementalHoe} da 4.2.3.5. Ara três por três; onde não há o que arar, faz as
 * vezes de farinha de osso (um de uso); numa muda de árvore-grande (vinte de uso) ou de árvore-prata (cento e cinquenta)
 * faz a árvore crescer na hora.
 */
public class ElementalHoeItem extends HoeItem {
    public ElementalHoeItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties properties) {
        super(material, attackDamage, attackSpeed, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) return super.useOn(context);
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        boolean did = false;
        for (int xx = -1; xx <= 1; xx++) {
            for (int zz = -1; zz <= 1; zz++) {
                BlockPos at = pos.offset(xx, 0, zz);
                UseOnContext there = new UseOnContext(level, player, context.getHand(), stack,
                        new BlockHitResult(context.getClickLocation().add(xx, 0, zz), context.getClickedFace(), at, context.isInside()));
                if (super.useOn(there).consumesAction()) {
                    if (level.isClientSide() && ToolFx.client != null) ToolFx.client.sparkle(at, 8401408, 2);
                    did = true;
                }
            }
        }
        if (!did) {
            var state = level.getBlockState(pos);
            // as mudas mágicas crescem na hora, como no original (antes da farinha de osso, que aqui também as faz crescer)
            if (state.is(TCBlocks.GREATWOOD_SAPLING) && stack.getDamageValue() + 20 <= stack.getMaxDamage()) {
                if (level instanceof ServerLevel server) ((MagicalSaplingBlock) state.getBlock()).grow(server, pos, state, level.getRandom());
                if (player != null) stack.hurtAndBreak(5, player, context.getHand());
                if (level.isClientSide() && ToolFx.client != null) ToolFx.client.sparkle(pos, 0, 2);
                did = true;
            } else if (state.is(TCBlocks.SILVERWOOD_SAPLING) && stack.getDamageValue() + 150 <= stack.getMaxDamage()) {
                if (level instanceof ServerLevel server) ((MagicalSaplingBlock) state.getBlock()).grow(server, pos, state, level.getRandom());
                if (player != null) stack.hurtAndBreak(25, player, context.getHand());
                if (level.isClientSide() && ToolFx.client != null) ToolFx.client.sparkle(pos, 0, 2);
                did = true;
            } else if (net.minecraft.world.item.BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, pos)) {
                if (player != null) stack.hurtAndBreak(1, player, context.getHand());
                if (level.isClientSide() && ToolFx.client != null) ToolFx.client.sparkle(pos, 0, 3);
                did = true;
            }
            if (did) {
                level.playSound(player, pos, TCSounds.WAND.value(), SoundSource.BLOCKS, 0.75f, 0.9f + level.getRandom().nextFloat() * 0.2f);
            }
        }
        return did ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
}
