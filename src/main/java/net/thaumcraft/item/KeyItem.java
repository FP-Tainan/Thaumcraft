package net.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.thaumcraft.block.ArcaneDoorBlock;
import net.thaumcraft.block.entity.OwnedBlockEntity;
import net.thaumcraft.registry.TCSounds;

import java.util.function.Consumer;

/**
 * A chave arcana: o {@code ItemKey} da 4.2.3.5, de ferro (0) ou de ouro (1). Em branco, o dono de uma porta ou placa
 * arcana (ou quem tem a chave de ouro dela) a grava para aquele lugar; gravada, dá a quem a usar ali a entrada na lista
 * (a de ferro só usa; a de ouro também grava chaves novas e mexe na placa). Gravada, brilha.
 */
public class KeyItem extends Item {
    private final int type;

    public KeyItem(int type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public int type() {
        return this.type;
    }

    private static CompoundTag data(ItemStack stack) {
        CustomData custom = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        return custom == null ? null : custom.copyTag();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        CompoundTag tag = data(stack);
        return tag != null && tag.contains("location");
    }

    /** O {@code onItemUseFirst}, numa porta ou placa arcana. */
    public InteractionResult useOnWarded(ItemStack stack, Level level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        boolean door = state.getBlock() instanceof ArcaneDoorBlock;
        BlockPos base = door && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        BlockPos other = door ? base.above() : null;
        byte kind = (byte) (door ? 0 : 1);
        String loc = base.getX() + "," + base.getY() + "," + base.getZ();
        if (!(level.getBlockEntity(base) instanceof OwnedBlockEntity owned)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        String name = player.getName().getString();
        CompoundTag tag = data(stack);
        if (tag == null || !tag.contains("location")) {
            // em branco: o dono (ou a chave de ouro, se esta for de ferro) grava uma nova
            if (name.equals(owned.owner) || owned.accessList.contains("1" + name) && this.type == 0) {
                ItemStack made = new ItemStack(this);
                CompoundTag written = new CompoundTag();
                written.putString("location", loc);
                written.putByte("type", kind);
                made.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.of(written));
                if (!player.getInventory().add(made)) player.drop(made, false);
                if (!player.getAbilities().instabuild) stack.shrink(1);
                player.sendSystemMessage(Component.translatable(kind == 0 ? "tc.key1" : "tc.key2").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
                level.playSound(null, pos, TCSounds.KEY.value(), SoundSource.PLAYERS, 1.0f, 0.9f);
                player.swing(InteractionHand.MAIN_HAND, true);
            }
            return InteractionResult.SUCCESS;
        }
        boolean known = name.equals(owned.owner) || owned.accessList.contains(this.type + name) || owned.accessList.contains("1" + name);
        if (!known && loc.equals(tag.getStringOr("location", ""))) {
            owned.accessList.add(this.type + name);
            owned.setChanged();
            if (other != null && level.getBlockEntity(other) instanceof OwnedBlockEntity upper) {
                upper.accessList.add(this.type + name);
                upper.setChanged();
            }
            // a porta: "agora você abre" (e, com a de ouro, "e pode dar acesso"); a placa: "dispara como se fosse o dono"
            Component message = Component.translatable(kind == 0 ? "tc.key3" : "tc.key5");
            // o "tambem pode dar acesso" vem colado, com um espaco (o original guardava o espaco no texto)
            if (this.type == 1) message = message.copy().append(" ").append(Component.translatable(kind == 0 ? "tc.key4" : "tc.key6"));
            player.sendSystemMessage(message.copy().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            level.playSound(null, pos, TCSounds.KEY.value(), SoundSource.PLAYERS, 1.0f, 1.1f);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            player.swing(InteractionHand.MAIN_HAND, true);
        } else if (!loc.equals(tag.getStringOr("location", ""))) {
            player.sendSystemMessage(Component.translatable("tc.key7").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        } else {
            player.sendSystemMessage(Component.translatable("tc.key8").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
        return InteractionResult.SUCCESS;
    }

    /** O {@code addInformation}: para onde a chave foi feita. */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        CompoundTag tag = data(stack);
        if (tag == null || !tag.contains("location")) return;
        String location = tag.getStringOr("location", "");
        String[] parts = location.split(",");
        if (parts.length == 3) location = "x " + parts[0] + ", z " + parts[2] + ", y " + parts[1];
        lines.accept(Component.translatable("tc.key9").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        lines.accept(Component.translatable(tag.getByteOr("type", (byte) 0) == 0 ? "tc.key10" : "tc.key11")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        lines.accept(Component.literal(location).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
