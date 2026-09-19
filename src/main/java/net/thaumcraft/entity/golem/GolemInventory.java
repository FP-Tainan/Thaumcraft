package net.thaumcraft.entity.golem;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * O {@code InventoryMob} da 4.2.3.5 no papel que tem no golem: não é onde ele guarda nada, é a lista do que ele
 * procura. As casas são "fantasmas" (o jogador põe uma cópia, nada sai da mão dele) e dizem ao golem o que buscar,
 * o que deixar e em que quantidade.
 *
 * <p>No núcleo de encher a casa pode pedir até 256 de uma coisa, mais que uma pilha; por isso a quantidade é salva
 * à parte da coisa.
 */
public class GolemInventory implements Container {
    public final Entity owner;
    public final int slotCount;
    private final NonNullList<ItemStack> items;

    public GolemInventory(Entity owner, int slots) {
        this.owner = owner;
        this.slotCount = slots;
        this.items = NonNullList.withSize(slots, ItemStack.EMPTY);
    }

    /** O {@code getAmountNeededSmart}: quanto das casas pede aquela coisa (pelo dicionário, com a melhoria de entropia). */
    public int getAmountNeededSmart(ItemStack stack, boolean fuzzy) {
        int amount = 0;
        for (ItemStack in : this.items) {
            if (in.isEmpty()) continue;
            if (fuzzy) {
                if (InventoryUtils.isItemEqual(in, stack)) amount += in.getCount();
                else if (InventoryUtils.oreMatch(in, stack)) amount += in.getCount();
            } else if (InventoryUtils.isItemEqual(in, stack) && InventoryUtils.tagsEqual(in, stack)) {
                amount += in.getCount();
            }
        }
        return amount;
    }

    /** O {@code getItemsNeeded}: as coisas pedidas; com a entropia, tudo que divide o "minério" de cada uma. */
    public List<ItemStack> getItemsNeeded(boolean fuzzy) {
        List<ItemStack> needed = new ArrayList<>();
        for (ItemStack in : this.items) {
            if (in.isEmpty()) continue;
            if (fuzzy) {
                var tags = InventoryUtils.commonTags(in.getItem());
                if (!tags.isEmpty()) {
                    for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tags.getFirst())) {
                        needed.add(new ItemStack(holder.value()));
                    }
                    continue;
                }
            }
            needed.add(in.copy());
        }
        return needed;
    }

    public boolean hasSomething() {
        for (ItemStack in : this.items) if (!in.isEmpty()) return true;
        return false;
    }

    public boolean allEmpty() {
        return !this.hasSomething();
    }

    public List<ItemStack> items() {
        return this.items;
    }

    /** Copia as casas de outro (quando o golem troca de núcleo e o inventário muda de tamanho). */
    public void copyFrom(GolemInventory other) {
        for (int a = 0; a < Math.min(this.slotCount, other.slotCount); a++) this.items.set(a, other.items.get(a));
    }

    public void save(ValueOutput output, String key) {
        ValueOutput.ValueOutputList list = output.childrenList(key);
        for (int a = 0; a < this.slotCount; a++) {
            ItemStack in = this.items.get(a);
            if (in.isEmpty()) continue;
            ValueOutput entry = list.addChild();
            entry.putByte("Slot", (byte) a);
            entry.store("Item", ItemStack.CODEC, in.copyWithCount(1));
            entry.putInt("Count", in.getCount());
        }
    }

    public void load(ValueInput input, String key) {
        for (int a = 0; a < this.slotCount; a++) this.items.set(a, ItemStack.EMPTY);
        for (ValueInput entry : input.childrenListOrEmpty(key)) {
            int slot = entry.getByteOr("Slot", (byte) 0) & 255;
            ItemStack in = entry.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
            if (in.isEmpty() || slot >= this.slotCount) continue;
            in.setCount(Math.max(1, entry.getIntOr("Count", 1)));
            this.items.set(slot, in);
        }
    }

    @Override
    public int getContainerSize() {
        return this.slotCount;
    }

    @Override
    public boolean isEmpty() {
        return this.allEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < this.slotCount ? this.items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack in = this.getItem(slot);
        if (in.isEmpty()) return ItemStack.EMPTY;
        ItemStack out = in.split(amount);
        if (in.isEmpty()) this.items.set(slot, ItemStack.EMPTY);
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < this.slotCount) this.items.set(slot, stack);
    }

    @Override
    public int getMaxStackSize() {
        return 256;
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return this.owner.isAlive() && player.distanceToSqr(this.owner) <= 64.0;
    }

    @Override
    public void clearContent() {
        for (int a = 0; a < this.slotCount; a++) this.items.set(a, ItemStack.EMPTY);
    }
}
