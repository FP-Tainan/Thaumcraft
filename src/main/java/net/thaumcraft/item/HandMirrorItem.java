package net.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.thaumcraft.block.MirrorBlock;
import net.thaumcraft.block.entity.LinkedMirrorBlockEntity;
import net.thaumcraft.block.entity.MirrorBlockEntity;
import net.thaumcraft.inventory.HandMirrorMenu;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * O {@code ItemHandMirror} da 4.2.3.5: o espelho mágico de mão. Ligado a um espelho mágico (qualquer um, mesmo já com
 * par), abre uma casa onde o que se põe sai por aquele espelho. Só manda, não recebe.
 */
public class HandMirrorItem extends Item {
    public HandMirrorItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    /** O {@code onItemUseFirst}: clicado num espelho mágico, guarda onde ele está. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockState(pos).getBlock() instanceof MirrorBlock)) return InteractionResult.PASS;
        Player player = context.getPlayer();
        if (level.isClientSide() || player == null) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof MirrorBlockEntity) {
            ItemStack stack = context.getItemInHand();
            CompoundTag tag = new CompoundTag();
            tag.putInt("linkX", pos.getX());
            tag.putInt("linkY", pos.getY());
            tag.putInt("linkZ", pos.getZ());
            tag.putString("linkDim", level.dimension().identifier().toString());
            tag.putString("dimname", LinkedMirrorBlockEntity.dimensionName(level.dimension()));
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            level.playSound(null, pos, TCSounds.JAR.value(), SoundSource.BLOCKS, 1.0f, 2.0f);
            player.sendSystemMessage(Component.translatable("tc.handmirrorlinked").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            player.swing(InteractionHand.MAIN_HAND, true);
        }
        return InteractionResult.SUCCESS;
    }

    /** O {@code onItemRightClick}: ligado e com o espelho no lugar, abre a casa; sem ele, a ligação se desfaz. */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && hand == InteractionHand.MAIN_HAND && stack.has(DataComponents.CUSTOM_DATA)) {
            if (target(stack, level) == null) {
                breakLink(stack, player, level);
            } else {
                player.openMenu(new SimpleMenuProvider((id, inventory, who) -> new HandMirrorMenu(id, inventory),
                        Component.translatable("container.handmirror")));
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** O espelho ligado, se ele ainda estiver lá. */
    @Nullable
    private static MirrorBlockEntity target(ItemStack mirror, Level level) {
        CustomData custom = mirror.get(DataComponents.CUSTOM_DATA);
        if (custom == null || level.getServer() == null) return null;
        CompoundTag tag = custom.copyTag();
        ServerLevel targetWorld = level.getServer().getLevel(LinkedMirrorBlockEntity.dimensionOf(tag.getStringOr("linkDim", "minecraft:overworld")));
        if (targetWorld == null) return null;
        BlockPos at = new BlockPos(tag.getIntOr("linkX", 0), tag.getIntOr("linkY", 0), tag.getIntOr("linkZ", 0));
        return targetWorld.getBlockEntity(at) instanceof MirrorBlockEntity tm ? tm : null;
    }

    private static void breakLink(ItemStack mirror, Player player, Level level) {
        mirror.remove(DataComponents.CUSTOM_DATA);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), TCSounds.ZAP.value(), SoundSource.PLAYERS, 1.0f, 0.8f);
        player.sendSystemMessage(Component.translatable("tc.handmirrorerror").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }

    /** O {@code transport}: o item sai do espelho ligado, empurrado para a frente, como o que sai de um par. */
    public static boolean transport(ItemStack mirror, ItemStack items, Player player, Level level) {
        if (!mirror.has(DataComponents.CUSTOM_DATA)) return false;
        MirrorBlockEntity tm = target(mirror, level);
        if (tm == null) {
            if (level.getServer() != null) breakLink(mirror, player, level);
            return false;
        }
        Level targetWorld = tm.getLevel();
        BlockPos at = tm.getBlockPos();
        Direction linkedFacing = tm.getBlockState().getValue(MirrorBlock.FACING);
        ItemEntity ie2 = new ItemEntity(targetWorld,
                at.getX() + 0.5 - linkedFacing.getStepX() * 0.3,
                at.getY() + 0.5 - linkedFacing.getStepY() * 0.3,
                at.getZ() + 0.5 - linkedFacing.getStepZ() * 0.3, items.copy());
        ie2.setDeltaMovement(linkedFacing.getStepX() * 0.15f, linkedFacing.getStepY() * 0.15f, linkedFacing.getStepZ() * 0.15f);
        ie2.setPortalCooldown(20);
        targetWorld.addFreshEntity(ie2);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.1f, 1.0f);
        targetWorld.blockEvent(at, tm.getBlockState().getBlock(), 1, 0);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        MirrorItem.linkTooltip(stack, lines);
    }
}
