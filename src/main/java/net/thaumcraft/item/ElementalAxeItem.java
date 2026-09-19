package net.thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.entity.FollowingItemEntity;
import net.thaumcraft.net.TCNetwork;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.world.CropUtils;

/**
 * O machado do núcleo de água: o {@code ItemElementalAxe} da 4.2.3.5. Segurando o clique direito, puxa os itens soltos a
 * até dez blocos (com bolhas azuis); em pé, num tronco, derruba a árvore de fora para dentro, um bloco por golpe, e o que
 * cai voa até quem cortou.
 */
public class ElementalAxeItem extends Item {
    public ElementalAxeItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int remaining) {
        for (ItemEntity e : level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(10.0))) {
            if (e.isRemoved() || e.distanceTo(player) > 10.0 || e instanceof FollowingItemEntity f && f.isFollowing()) continue;
            double d6 = e.getX() - player.getX();
            double d8 = e.getY() - player.getY() + player.getBbHeight() / 2.0f;
            double d10 = e.getZ() - player.getZ();
            double d11 = Math.sqrt(d6 * d6 + d8 * d8 + d10 * d10);
            d6 /= d11;
            d8 /= d11;
            d10 /= d11;
            var m = e.getDeltaMovement();
            e.setDeltaMovement(Mth.clamp(m.x - d6 * 0.3, -0.35, 0.35), Mth.clamp(m.y - d8 * 0.3, -0.35, 0.35), Mth.clamp(m.z - d10 * 0.3, -0.35, 0.35));
            if (level.isClientSide() && ToolFx.client != null) {
                var r = level.getRandom();
                ToolFx.client.bubble(level, e.getX() + (r.nextFloat() - r.nextFloat()) * 0.125f, e.getY() + (r.nextFloat() - r.nextFloat()) * 0.125f,
                        e.getZ() + (r.nextFloat() - r.nextFloat()) * 0.125f, 0.33f, 0.33f, 1.0f);
            }
        }
    }

    /** O {@code onBlockStartBreak}: em pé, num tronco, derruba a árvore em vez de quebrar o bloco. Devolve se cancelou. */
    public static boolean fell(Level level, Player player, BlockPos pos, BlockState state, BlockEntity be) {
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof ElementalAxeItem) || player.isShiftKeyDown() || !CropUtils.isWoodLog(level, pos)) return false;
        if (level instanceof ServerLevel server) {
            CropUtils.breakFurthestBlock(server, pos, state.getBlock(), player, held, true, 10);
            TCNetwork.blockBubble(server, pos, 0x5454FF);
            server.playSound(null, pos, TCSounds.BUBBLE.value(), SoundSource.BLOCKS, 0.15f, 1.0f);
        }
        held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
        return true;
    }
}
