package net.thaumcraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.wands.VisDiscountGear;
import net.thaumcraft.event.Hover;
import net.thaumcraft.inventory.HoverHarnessMenu;
import org.jetbrains.annotations.Nullable;

/**
 * O arreio taumostático: o {@code ItemHoverHarness} da 4.2.3.5. Vai no peito; clicando com ele na mão abre-se a casa
 * do jarro de Potentia que alimenta o voo (tecla H, {@link Hover}). Dá 5% de desconto de vis no ar e 2% no resto;
 * conserta com ouro. As linhas da dica ficam no {@code HoverClient}, porque dependem dos aspectos que a pessoa já
 * descobriu.
 */
public class HoverHarnessItem extends Item implements VisDiscountGear {
    public HoverHarnessItem(Properties properties) {
        super(properties);
    }

    @Override
    public int visDiscount(ItemStack stack, @Nullable Player player, @Nullable Aspect aspect) {
        return aspect == Aspects.AIR ? 5 : 2;
    }

    /** O {@code onItemRightClick}: a tela do jarro. */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && !level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider((id, inventory, who) -> new HoverHarnessMenu(id, inventory, player.getMainHandItem()),
                    Component.translatable("item.thaumcraft.hover_harness")));
        }
        return InteractionResult.SUCCESS;
    }

    /** O {@code onArmorTick}, do lado do servidor; o de quem joga fica no {@code HoverClient}. */
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (slot == EquipmentSlot.CHEST && entity instanceof ServerPlayer player && !player.getAbilities().instabuild) {
            Hover.serverTick(player, stack);
        }
    }
}
