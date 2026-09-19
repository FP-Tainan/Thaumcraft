package net.thaumcraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.thaumcraft.block.entity.BannerBlockEntity;
import net.thaumcraft.registry.TCComponents;

/**
 * O estandarte na mão: o {@code BlockWoodenDeviceItem} da 4.2.3.5 para o aparelho 8. Posto no chão, vira para quem o pôs
 * (dezesseis rumos); na parede, fica pendurado nela. O nome diz a cor ({@code tile.blockWoodenDevice.8.cor}).
 */
public class BannerItem extends BlockItem {
    public BannerItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        InteractionResult result = super.place(context);
        if (!result.consumesAction() || !(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof BannerBlockEntity banner)) {
            return result;
        }
        int side = context.getClickedFace().get3DDataValue();
        if (side <= 1) {
            float yaw = context.getPlayer() == null ? 0.0f : context.getPlayer().getYRot();
            banner.setFacing((byte) (Mth.floor((yaw + 180.0f) * 16.0f / 360.0f + 0.5) & 15));
        } else {
            banner.setWall(true);
            int i = 0;
            if (side == 2) i = 8;
            if (side == 4) i = 4;
            if (side == 5) i = 12;
            banner.setFacing((byte) i);
        }
        return result;
    }

    @Override
    public Component getName(ItemStack stack) {
        Integer color = stack.get(TCComponents.BANNER_COLOR);
        return Component.translatable(color == null ? "block.thaumcraft.banner" : "block.thaumcraft.banner." + color);
    }
}
