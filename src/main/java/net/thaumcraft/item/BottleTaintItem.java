package net.thaumcraft.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thaumcraft.entity.BottleTaintEntity;

/** A garrafa de mácula: o {@code ItemBottleTaint} da 4.2.3.5 (de oito em oito), que se arremessa com o som do arco. */
public class BottleTaintItem extends Item {
    public BottleTaintItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack thrown = stack.copyWithCount(1);
        stack.consume(1, player);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.5f,
                0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));
        if (level instanceof ServerLevel) level.addFreshEntity(new BottleTaintEntity(level, player, thrown));
        return InteractionResult.SUCCESS;
    }
}
