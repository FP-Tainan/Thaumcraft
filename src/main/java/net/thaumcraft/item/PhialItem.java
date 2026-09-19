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
import net.thaumcraft.registry.TCComponents;

import java.util.function.Consumer;

/**
 * O frasco de vidro: o {@code ItemEssence} da 4.2.3.5 — vazio ou com oito de um aspecto. Enche-se no alambique e nos
 * jarros e se despeja nos jarros.
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

    /**
     * O {@code onItemUseFirst} do {@code ItemEssence}: o frasco vazio tira oito do alambique ou de um jarro (o comum ou o
     * do vazio) que tenha oito; o cheio despeja os seus oito num jarro que os aceite e volta vazio.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        var te = level.getBlockEntity(pos);
        Aspect held = aspectOf(stack);
        if (held == null && te instanceof net.thaumcraft.block.entity.AlembicBlockEntity alembic && alembic.amount() >= PORTION) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            Aspect aspect = alembic.aspect();
            if (alembic.takeFromContainer(aspect, PORTION)) give(level, player, stack, filled(aspect), pos);
            return InteractionResult.SUCCESS;
        }
        if (held == null && te instanceof net.thaumcraft.block.entity.JarBlockEntity jar && jar.amount() >= PORTION) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            Aspect aspect = jar.aspect();
            if (jar.takeFromContainer(aspect, PORTION)) give(level, player, stack, filled(aspect), pos);
            return InteractionResult.SUCCESS;
        }
        if (held != null && te instanceof net.thaumcraft.block.entity.JarBlockEntity jar
                && jar.amount() <= net.thaumcraft.block.entity.JarBlockEntity.CAPACITY - PORTION && jar.doesContainerAccept(held)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            if (jar.addToContainer(held, PORTION) == 0) give(level, player, stack, new ItemStack(this), pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /** Um frasco cheio de oito do aspecto. */
    public ItemStack filled(Aspect aspect) {
        ItemStack phial = new ItemStack(this);
        phial.set(TCComponents.PHIAL_ASPECT, aspect.tag());
        return phial;
    }

    private static void give(Level level, Player player, ItemStack used, ItemStack result, BlockPos pos) {
        used.shrink(1);
        if (!player.getInventory().add(result)) {
            level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, result));
        }
        level.playSound(null, player, SoundEvents.GENERIC_SWIM, SoundSource.PLAYERS, 0.25f, 1.0f);
        player.containerMenu.broadcastChanges();
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
