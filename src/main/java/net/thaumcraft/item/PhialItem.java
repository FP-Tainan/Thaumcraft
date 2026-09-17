package net.thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.block.entity.CrucibleBlockEntity;
import net.thaumcraft.registry.TCComponents;

import java.util.function.Consumer;

/**
 * O frasco de vidro, que guarda um aspecto de essência.
 *
 * <p>No original ele sai da bancada e se enche no alambique. Aqui, enquanto o alambique não chega, ele se
 * enche direto do crisol fervendo: um toque no caldeirão tira dele o aspecto mais abundante e o guarda —
 * oito pontos por frasco, que é o que um frasco vale no mod.
 */
public class PhialItem extends Item {
    /** O quanto um frasco leva de uma vez, como no original. */
    public static final int PORTION = 8;

    public PhialItem(Properties properties) {
        super(properties);
    }

    /** O aspecto que este frasco guarda, ou nada se ele estiver vazio. */
    public static Aspect aspectOf(ItemStack stack) {
        String tag = stack.get(TCComponents.PHIAL_ASPECT);
        return tag == null ? null : Aspect.of(tag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (aspectOf(stack) != null) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible)) return InteractionResult.PASS;
        if (!crucible.boiling()) return InteractionResult.PASS;

        // o aspecto mais abundante é o que sai primeiro
        AspectList inside = crucible.aspects();
        Aspect chosen = null;
        int most = 0;
        for (Aspect aspect : inside.getAspects()) {
            int amount = inside.getAmount(aspect);
            if (amount <= most) continue;
            chosen = aspect;
            most = amount;
        }
        if (chosen == null || most < PORTION) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        inside.reduce(chosen, PORTION);
        ItemStack filled = new ItemStack(this);
        filled.set(TCComponents.PHIAL_ASPECT, chosen.tag());
        stack.shrink(1);
        if (!player.getInventory().add(filled)) player.drop(filled, false);
        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.7f, 1.2f);
        crucible.setChanged();
        level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
        return InteractionResult.SUCCESS;
    }

    @Override
    public Component getName(ItemStack stack) {
        Aspect aspect = aspectOf(stack);
        return aspect == null
                ? Component.translatable("item.thaumcraft.phial")
                : Component.translatable("item.thaumcraft.phial.filled", aspect.name());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        Aspect aspect = aspectOf(stack);
        if (aspect == null) return;
        lines.accept(Component.literal(aspect.name().getString() + " " + PORTION)
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
