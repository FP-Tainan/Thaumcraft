package net.thaumcraft.inventory;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.entity.PechEntity;
import net.thaumcraft.entity.PechTrades;
import net.thaumcraft.registry.TCMenus;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A troca com o pech: o {@code ContainerPech} + {@code InventoryPech} da 4.2.3.5. Uma casa para o item de valor e
 * quatro para o que ele dá; o botão dos dados (o botão 0) faz a troca. O que ficar nas casas volta para quem trocou
 * quando a tela fecha — e o pech não cata de volta.
 */
public class PechMenu extends AbstractContainerMenu {
    private final Container inventory = new SimpleContainer(5);
    @Nullable
    private final PechEntity pech;
    private final Player player;

    /** O lado de quem joga: o pech vem pelo número dele. */
    public PechMenu(int id, Inventory inventory, Integer entityId) {
        this(id, inventory, inventory.player.level().getEntity(entityId) instanceof PechEntity pech ? pech : null);
    }

    public PechMenu(int id, Inventory inventory, @Nullable PechEntity pech) {
        super(TCMenus.PECH, id);
        this.pech = pech;
        this.player = inventory.player;
        if (pech != null) pech.trading = true;
        this.addSlot(new Slot(this.inventory, 0, 36, 29));
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                this.addSlot(new Slot(this.inventory, 1 + j + i * 2, 106 + 18 * j, 20 + 18 * i) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
        }
        for (int i = 0; i < 9; i++) this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
    }

    @Nullable
    public PechEntity pech() {
        return this.pech;
    }

    /** O botão dos dados acende quando há um item de valor sozinho na casa e as quatro de saída estão vazias. */
    public boolean canTrade() {
        if (this.pech == null) return false;
        ItemStack in = this.getSlot(0).getItem();
        if (in.isEmpty() || !this.pech.isValued(in)) return false;
        for (int i = 1; i < 5; i++) if (!this.getSlot(i).getItem().isEmpty()) return false;
        return true;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != 0) return false;
        this.generateContents();
        return true;
    }

    private boolean hasStuffInPack() {
        for (ItemStack stack : this.pech.loot) if (!stack.isEmpty()) return true;
        return false;
    }

    /** O {@code generateContents}: o valor do item vira tesouros, até cinco por vez. */
    private void generateContents() {
        if (!(this.player.level() instanceof ServerLevel level) || !this.canTrade()) return;
        var random = level.getRandom();
        ItemStack in = this.inventory.getItem(0);
        int value = this.pech.getValue(in);
        // de vez em quando, ele se cansa da amizade
        if (random.nextInt(100) <= value / 2) {
            this.pech.setTamed(false);
            this.pech.updateAINextTick = true;
            this.pech.playSound(TCSounds.PECH_TRADE.value(), 0.4f, 1.0f);
        }
        if (random.nextInt(5) == 0) value += random.nextInt(3);
        else if (random.nextBoolean()) value -= random.nextInt(3);
        List<PechTrades.Trade> table = PechTrades.of(this.pech.pechType());
        while (value > 0) {
            int am = Math.min(5, Math.max((value + 1) / 2, random.nextInt(value) + 1));
            value -= am;
            if (am == 1 && random.nextBoolean() && this.hasStuffInPack()) {
                List<Integer> full = new ArrayList<>();
                for (int a = 0; a < this.pech.loot.size(); a++) if (!this.pech.loot.get(a).isEmpty()) full.add(a);
                int r = full.get(random.nextInt(full.size()));
                ItemStack one = this.pech.loot.get(r).copyWithCount(1);
                this.give(one);
                this.pech.loot.get(r).shrink(1);
            } else if (am >= 4 && random.nextBoolean()) {
                this.give(PechTrades.dungeonTreasure(level, random));
            } else {
                List<PechTrades.Trade> fits = table.stream().filter(t -> t.value() == am).toList();
                if (fits.isEmpty()) continue;
                this.give(fits.get(random.nextInt(fits.size())).make().apply(level));
            }
        }
        in.shrink(1);
        this.inventory.setChanged();
        this.broadcastChanges();
    }

    /** O {@code mergeItemStack} do {@code ContainerPech}: junta nas casas de saída que já têm o mesmo, e senão na primeira vazia. */
    private void give(ItemStack stack) {
        for (int i = 1; i < 5 && !stack.isEmpty(); i++) {
            ItemStack there = this.inventory.getItem(i);
            if (!there.isEmpty() && ItemStack.isSameItemSameComponents(there, stack) && there.getCount() < there.getMaxStackSize()) {
                int moved = Math.min(stack.getCount(), there.getMaxStackSize() - there.getCount());
                there.grow(moved);
                stack.shrink(moved);
            }
        }
        for (int i = 1; i < 5 && !stack.isEmpty(); i++) {
            if (this.inventory.getItem(i).isEmpty()) {
                this.inventory.setItem(i, stack.copy());
                stack.setCount(0);
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.pech != null && this.pech.isAlive() && this.pech.isTamed() && player.distanceToSqr(this.pech) < 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();
            if (index < 5) {
                if (!this.moveItemStackTo(stack, 5, 41, true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(stack, 0, 1, true)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
            if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }
        return copy;
    }

    /** O {@code onContainerClosed}: o que sobrou nas casas vai para o chão, marcado para o pech não pegar de volta. */
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.pech != null) this.pech.trading = false;
        if (player.level().isClientSide()) return;
        for (int a = 0; a < 5; a++) {
            ItemStack stack = this.inventory.removeItemNoUpdate(a);
            if (stack.isEmpty()) continue;
            ItemEntity dropped = player.drop(stack, false);
            if (dropped != null) PechEntity.PechDrops.mark(dropped);
        }
    }
}
