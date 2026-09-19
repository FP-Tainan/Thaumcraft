package net.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.block.MirrorBlock;
import net.thaumcraft.block.entity.LinkedMirrorBlockEntity;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * O {@code BlockMirrorItem} da 4.2.3.5. Clicado num espelho da mesma espécie que ainda não tem par, dá um espelho novo
 * já ligado a ele (e gasta este); posto no mundo, o espelho ligado refaz a ligação com o par.
 */
public class MirrorItem extends BlockItem {
    public MirrorItem(MirrorBlock block, Properties properties) {
        super(block, properties);
    }

    @Nullable
    static CompoundTag data(ItemStack stack) {
        CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        return custom == null ? null : custom.copyTag();
    }

    /** O {@code onItemUseFirst}: clicado num espelho, liga em vez de pôr. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof MirrorBlock)) return super.useOn(context);
        Player player = context.getPlayer();
        if (level.isClientSide() || player == null) return InteractionResult.SUCCESS;
        ItemStack stack = context.getItemInHand();
        if (state.getBlock() == this.getBlock() && level.getBlockEntity(pos) instanceof LinkedMirrorBlockEntity tm) {
            if (!tm.isLinkValid()) {
                ItemStack st = new ItemStack(this);
                CompoundTag tag = new CompoundTag();
                tag.putInt("linkX", pos.getX());
                tag.putInt("linkY", pos.getY());
                tag.putInt("linkZ", pos.getZ());
                tag.putString("linkDim", level.dimension().identifier().toString());
                tag.putString("dimname", LinkedMirrorBlockEntity.dimensionName(level.dimension()));
                st.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                level.playSound(null, pos, TCSounds.JAR.value(), SoundSource.BLOCKS, 1.0f, 2.0f);
                if (!player.getInventory().add(st)) player.drop(st, false);
                if (!player.getAbilities().instabuild) stack.shrink(1);
                player.swing(InteractionHand.MAIN_HAND, true);
            } else {
                player.sendSystemMessage(Component.translatable("tc.mirror.already_linked").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** O {@code placeBlockAt}: o espelho ligado, posto, procura o par. */
    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        boolean changed = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        CompoundTag tag = data(stack);
        if (!level.isClientSide() && tag != null && level.getBlockEntity(pos) instanceof LinkedMirrorBlockEntity te) {
            te.setLink(new BlockPos(tag.getIntOr("linkX", 0), tag.getIntOr("linkY", 0), tag.getIntOr("linkZ", 0)),
                    LinkedMirrorBlockEntity.dimensionOf(tag.getStringOr("linkDim", "minecraft:overworld")));
            te.restoreLink();
        }
        return changed;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        linkTooltip(stack, lines);
    }

    /** "Ligado a x,y,z em mundo", como no original. */
    static void linkTooltip(ItemStack stack, Consumer<Component> lines) {
        CompoundTag tag = data(stack);
        if (tag == null || !tag.contains("linkX")) return;
        lines.accept(Component.translatable("tc.handmirrorlinkedto").append(" " + tag.getIntOr("linkX", 0) + "," + tag.getIntOr("linkY", 0) + ","
                + tag.getIntOr("linkZ", 0) + " ").append(Component.translatable("tc.mirror.in")).append(" " + tag.getStringOr("dimname", "")));
    }
}
