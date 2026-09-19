package net.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.FenceBlock;
import net.thaumcraft.entity.TravelingTrunkEntity;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCEntities;

import java.util.List;
import java.util.function.Consumer;

/**
 * O baú itinerante guardado: o {@code ItemTrunkSpawner} da 4.2.3.5. Posto no chão, o baú nasce ali com o dono, a
 * melhoria e (se a ordem o guardou) o que tinha dentro.
 */
public class TrunkSpawnerItem extends Item {
    public TrunkSpawnerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level) || context.getPlayer() == null) return InteractionResult.SUCCESS;
        BlockPos clicked = context.getClickedPos();
        Direction side = context.getClickedFace();
        BlockPos at = clicked.relative(side);
        double d0 = side == Direction.UP && level.getBlockState(clicked).getBlock() instanceof FenceBlock ? 0.5 : 0.0;
        TravelingTrunkEntity trunk = TCEntities.TRAVELING_TRUNK.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (trunk == null) return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();
        trunk.snapTo(at.getX(), at.getY() + d0, at.getZ(), Mth.wrapDegrees(level.getRandom().nextFloat() * 360.0f), 0.0f);
        trunk.setYHeadRot(trunk.getYRot());
        trunk.setYBodyRot(trunk.getYRot());
        trunk.setOwner(context.getPlayer().getName().getString());
        if (stack.has(DataComponents.CUSTOM_NAME)) trunk.setCustomName(stack.getHoverName());
        Integer upgrade = stack.get(TCComponents.TRUNK_UPGRADE);
        if (upgrade != null) trunk.setUpgrade(upgrade);
        trunk.setInvSize();
        List<ItemStack> inv = stack.get(TCComponents.TRUNK_INVENTORY);
        if (inv != null) for (int a = 0; a < Math.min(36, inv.size()); a++) trunk.inventory.setItem(a, inv.get(a).copy());
        level.addFreshEntity(trunk);
        trunk.playAmbientSound();
        if (!context.getPlayer().getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        Integer upgrade = stack.get(TCComponents.TRUNK_UPGRADE);
        if (upgrade != null && upgrade > -1) {
            lines.accept(Component.translatable("item.thaumcraft.golem_upgrade_" + GolemUpgradeItem.NAMES[upgrade]).withStyle(ChatFormatting.BLUE));
        }
        if (stack.has(TCComponents.TRUNK_INVENTORY)) lines.accept(Component.translatable("item.TrunkSpawner.text.1"));
    }

    /** O item que o sino devolve ao recolher o baú: a melhoria sempre, e o que tem dentro só com a ordem. */
    public static ItemStack pickUp(TravelingTrunkEntity trunk, boolean sneak) {
        ItemStack dropped = new ItemStack(net.thaumcraft.registry.TCItems.TRUNK_SPAWNER);
        if (sneak) return dropped;
        if (trunk.hasCustomName()) dropped.set(DataComponents.CUSTOM_NAME, trunk.getCustomName());
        dropped.set(TCComponents.TRUNK_UPGRADE, trunk.getUpgrade());
        if (trunk.getUpgrade() == 4) dropped.set(TCComponents.TRUNK_INVENTORY, trunk.inventory.all().stream().map(ItemStack::copy).toList());
        return dropped;
    }
}
