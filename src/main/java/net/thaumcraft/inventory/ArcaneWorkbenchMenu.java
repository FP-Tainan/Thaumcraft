package net.thaumcraft.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.block.entity.ArcaneWorkbenchBlockEntity;
import net.thaumcraft.crafting.ArcaneRecipe;
import net.thaumcraft.crafting.ArcaneRecipes;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCMenus;

import java.util.ArrayList;
import java.util.List;

/**
 * As casas da bancada arcana, nos mesmos lugares do Thaumcraft 4.2.3.5.
 *
 * <p>O resultado fica em (160, 64) e a varinha em (160, 24); a grade de três por três começa em (40, 40)
 * com vinte e quatro pontos de passo, que é bem mais folgado que a grade de dezoito da bancada comum. O
 * inventário de quem joga fica embaixo, a partir de (16, 151).
 */
public class ArcaneWorkbenchMenu extends AbstractContainerMenu {
    private final Container bench;
    private final ResultContainer result = new ResultContainer();
    private final Player player;

    public ArcaneWorkbenchMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(ArcaneWorkbenchBlockEntity.SIZE));
    }

    public ArcaneWorkbenchMenu(int id, Inventory inventory, Container bench) {
        super(TCMenus.ARCANE_WORKBENCH, id);
        checkContainerSize(bench, ArcaneWorkbenchBlockEntity.SIZE);
        this.bench = bench;
        this.player = inventory.player;
        bench.startOpen(inventory.player);

        // o resultado, que só sai se a varinha puder pagar
        this.addSlot(new ArcaneResultSlot(this, inventory.player, this.result, 0, 160, 64));
        // a varinha, que fica de lado
        this.addSlot(new Slot(bench, ArcaneWorkbenchBlockEntity.WAND_SLOT, 160, 24) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof WandItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        // a grade
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(bench, col + row * 3, 40 + col * 24, 40 + row * 24));
            }
        }
        // o inventário de quem joga
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 16 + col * 18, 151 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 16 + col * 18, 209));
        }
        this.refresh();
    }

    /** Refaz o que a grade está pedindo, e se a varinha dá conta de pagar. */
    public void refresh() {
        ArcaneRecipe recipe = ArcaneRecipes.find(this.grid());
        // sem a pesquisa, a bancada não monta: é a regra do original
        if (recipe != null
                && !net.thaumcraft.research.ResearchManager.knows(this.player, recipe.research())) {
            recipe = null;
        }
        if (recipe == null || !this.canAfford(recipe)) {
            this.result.setItem(0, ItemStack.EMPTY);
            return;
        }
        this.result.setItem(0, recipe.result().copy());
    }

    private List<ItemStack> grid() {
        List<ItemStack> grid = new ArrayList<>(9);
        for (int slot = 0; slot < 9; slot++) grid.add(this.bench.getItem(slot));
        return grid;
    }

    /** A varinha na bancada tem vis bastante para esta receita? */
    public boolean canAfford(ArcaneRecipe recipe) {
        ItemStack wand = this.bench.getItem(ArcaneWorkbenchBlockEntity.WAND_SLOT);
        if (!(wand.getItem() instanceof WandItem)) return false;
        return WandItem.consume(wand, recipe.cost(), false);
    }

    /** Cobra o vis da varinha e gasta o que estava na grade. */
    public void take() {
        ArcaneRecipe recipe = ArcaneRecipes.find(this.grid());
        if (recipe == null) return;
        if (!net.thaumcraft.research.ResearchManager.knows(this.player, recipe.research())) return;
        ItemStack wand = this.bench.getItem(ArcaneWorkbenchBlockEntity.WAND_SLOT);
        if (!WandItem.consume(wand, recipe.cost(), true)) return;
        for (int slot = 0; slot < 9; slot++) this.bench.removeItem(slot, 1);
        this.refresh();
    }

    @Override
    public void slotsChanged(Container container) {
        this.refresh();
        super.slotsChanged(container);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.bench.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.bench.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // as casas da bancada vão para o inventário e vice-versa, como em qualquer bancada
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int benchSlots = 11;
        if (index < benchSlots) {
            if (!this.moveItemStackTo(stack, benchSlots, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if (!this.moveItemStackTo(stack, 1, benchSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    /** O que a grade daria, se a varinha desse conta. */
    public ArcaneRecipe pending() {
        return ArcaneRecipes.find(this.grid());
    }
}
