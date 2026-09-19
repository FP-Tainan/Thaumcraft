package net.thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEffects;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.Warp;

/**
 * O sabão higienizante: o {@code ItemSanitySoap} da 4.2.3.5. Esfrega-se por dez segundos (com bolhas e o som de
 * raízes); no fim, um em três de tirar um ponto da distorção que gruda (mais um quarto com a proteção contra a dobra e
 * mais um quarto dentro do fluido purificante) e toda a temporária vai embora.
 */
public class SanitySoapItem extends Item {
    /** As bolhas do {@code crucibleBubble}, desenhadas por quem vê. */
    public interface ClientEffects {
        void bubble(Level level, double x, double y, double z, float r, float g, float b);
    }

    public static ClientEffects clientEffects;

    public SanitySoapItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 200;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        int ticks = this.getUseDuration(stack, entity) - remaining;
        if (ticks > 195) entity.releaseUsingItem();
        if (level.isClientSide()) {
            var random = level.getRandom();
            if (random.nextFloat() < 0.2f) {
                level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), TCSounds.ROOTS.value(), SoundSource.PLAYERS, 0.1f,
                        1.5f + random.nextFloat() * 0.2f, false);
            }
            if (clientEffects != null) {
                for (int a = 0; a < 10; a++) {
                    clientEffects.bubble(level, entity.getX() - 0.5f + random.nextFloat(),
                            entity.getBoundingBox().minY + random.nextFloat() * entity.getBbHeight(),
                            entity.getZ() - 0.5f + random.nextFloat(), 1.0f, 0.8f, 0.9f);
                }
            }
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (this.getUseDuration(stack, entity) - remaining <= 195 || !(entity instanceof Player player)) return false;
        stack.shrink(1);
        if (!level.isClientSide()) {
            float chance = 0.33f;
            if (player.hasEffect(TCEffects.WARP_WARD)) chance += 0.25f;
            if (level.getBlockState(BlockPos.containing(player.getX(), player.getY(), player.getZ())).is(TCBlocks.PURIFYING_FLUID)) chance += 0.25f;
            if (level.getRandom().nextFloat() < chance && Knowledges.of(player).warpSticky() > 0) Warp.addSticky(player, -1);
            int temp = Knowledges.of(player).warpTemp();
            if (temp > 0) Warp.add(player, -temp, true);
        } else {
            var random = level.getRandom();
            level.playLocalSound(player.getX(), player.getY(), player.getZ(), TCSounds.CRAFT_START.value(), SoundSource.PLAYERS, 0.25f, 1.0f, false);
            if (clientEffects != null) {
                for (int a = 0; a < 40; a++) {
                    clientEffects.bubble(level, player.getX() - 0.5f + random.nextFloat() * 1.5f,
                            player.getBoundingBox().minY + random.nextFloat() * player.getBbHeight(),
                            player.getZ() - 0.5f + random.nextFloat() * 1.5f, 1.0f, 0.7f, 0.9f);
                }
            }
        }
        return true;
    }
}
