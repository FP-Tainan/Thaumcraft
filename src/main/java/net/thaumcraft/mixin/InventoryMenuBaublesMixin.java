package net.thaumcraft.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.baubles.BaubleItem;
import net.thaumcraft.baubles.Baubles;
import net.thaumcraft.inventory.BaubleSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * As quatro casas de bijuteria dentro do inventário de sempre — amuleto, dois anéis e cinto, na fileira embaixo da
 * grade de fabricação.
 *
 * <p>No Baubles 1.0.1.10 elas moravam num inventário expandido à parte, com um botão para alternar. Aqui ficam no
 * inventário do próprio jogo, a pedido de quem joga: não há tela extra nem botão. As casas entram no fim da lista, para
 * não mexer nos números que o jogo usa nas suas contas.
 */
@Mixin(InventoryMenu.class)
public abstract class InventoryMenuBaublesMixin extends AbstractContainerMenu {
    /** A primeira casa de bijuteria; daí em diante são quatro. */
    @org.spongepowered.asm.mixin.Unique
    private int thaumcraft$firstBauble = -1;

    protected InventoryMenuBaublesMixin() {
        super(null, 0);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void thaumcraft$addBaubleSlots(Inventory inventory, boolean active, Player owner, CallbackInfo info) {
        var worn = Baubles.container(owner);
        this.thaumcraft$firstBauble = this.slots.size();
        this.addSlot(new BaubleSlot(worn, net.thaumcraft.api.baubles.BaubleType.AMULET, Baubles.AMULET, 98, 62, owner));
        this.addSlot(new BaubleSlot(worn, net.thaumcraft.api.baubles.BaubleType.RING, Baubles.RING_1, 116, 62, owner));
        this.addSlot(new BaubleSlot(worn, net.thaumcraft.api.baubles.BaubleType.RING, Baubles.RING_2, 134, 62, owner));
        this.addSlot(new BaubleSlot(worn, net.thaumcraft.api.baubles.BaubleType.BELT, Baubles.BELT, 152, 62, owner));
    }

    /**
     * Agachar com a peça na mão veste: a bijuteria vai para a casa dela, se houver uma vazia. No caminho de volta —
     * agachar sobre a casa — vale o mesmo que valeria ao arrastar: o que não aceita sair não sai.
     */
    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void thaumcraft$wearOnShiftClick(Player player, int slotIndex, CallbackInfoReturnable<ItemStack> info) {
        if (this.thaumcraft$firstBauble < 0) return;
        if (slotIndex >= this.thaumcraft$firstBauble) {
            if (!this.slots.get(slotIndex).mayPickup(player)) info.setReturnValue(ItemStack.EMPTY);
            return;
        }
        if (slotIndex < 9) return;
        Slot clicked = this.slots.get(slotIndex);
        if (!clicked.hasItem() || !(clicked.getItem().getItem() instanceof BaubleItem)) return;
        for (int at = this.thaumcraft$firstBauble; at < this.thaumcraft$firstBauble + 4; at++) {
            Slot casa = this.slots.get(at);
            if (casa.hasItem() || !casa.mayPlace(clicked.getItem())) continue;
            casa.setByPlayer(clicked.getItem().copyWithCount(1));
            clicked.remove(1);
            casa.setChanged();
            info.setReturnValue(ItemStack.EMPTY);
            return;
        }
    }
}
