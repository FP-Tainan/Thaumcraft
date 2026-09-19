package net.thaumcraft.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.block.entity.ThaumatoriumBlockEntity;
import net.thaumcraft.crafting.CrucibleRecipe;
import net.thaumcraft.crafting.CrucibleRecipes;
import net.thaumcraft.registry.TCMenus;
import net.thaumcraft.research.ResearchManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * O {@code ContainerThaumatorium} da 4.2.3.5: a casa do catalisador e a lista das receitas de crisol que ele pode
 * fazer — as que o jogador já pesquisou e que aceitam o catalisador posto, mais as já marcadas. O botão de número
 * {@code n} marca (ou desmarca) a receita {@code n} da lista.
 */
public class ThaumatoriumMenu extends AbstractContainerMenu {
    @Nullable
    public final ThaumatoriumBlockEntity thaumatorium;
    private final Player player;
    public final List<CrucibleRecipe> recipes = new ArrayList<>();

    public ThaumatoriumMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, inventory.player.level().getBlockEntity(pos) instanceof ThaumatoriumBlockEntity found ? found : null);
    }

    public ThaumatoriumMenu(int id, Inventory inventory, @Nullable ThaumatoriumBlockEntity tile) {
        super(TCMenus.THAUMATORIUM, id);
        this.player = inventory.player;
        this.thaumatorium = tile;
        Container slotSource = tile != null ? tile : new SimpleContainer(1);
        if (tile != null) tile.eventHandler = this;
        this.addSlot(new Slot(slotSource, 0, 48, 16));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
        }
        for (int i = 0; i < 9; i++) this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
        this.updateRecipes();
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        this.updateRecipes();
    }

    /** O {@code updateRecipes}. */
    public void updateRecipes() {
        this.recipes.clear();
        if (this.thaumatorium == null) return;
        ItemStack input = this.thaumatorium.inputStack;
        for (CrucibleRecipe recipe : CrucibleRecipes.ALL) {
            if (ResearchManager.knows(this.player, recipe.research()) && recipe.catalystMatches(input)) {
                this.recipes.add(recipe);
            } else if (this.thaumatorium.recipeHash.contains(recipe.hash())) {
                this.recipes.add(recipe);
            }
        }
    }

    /** O {@code enchantItem}: marca ou desmarca a receita da lista. */
    @Override
    public boolean clickMenuButton(Player player, int button) {
        this.updateRecipes();
        if (this.thaumatorium == null || button < 0 || button >= this.recipes.size()) return false;
        CrucibleRecipe recipe = this.recipes.get(button);
        boolean marked = this.thaumatorium.recipeHash.contains(recipe.hash());
        if (!marked && this.thaumatorium.recipeHash.size() >= this.thaumatorium.maxRecipes) return false;
        if (!player.level().isClientSide()) this.thaumatorium.toggle(recipe, player);
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.thaumatorium == null || this.thaumatorium.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.thaumatorium != null && !player.level().isClientSide()) this.thaumatorium.eventHandler = null;
    }

    /** O {@code transferStackInSlot} do original: do inventário para a casa, e da casa para o inventário. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index != 0) {
            if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else if (!this.moveItemStackTo(stack, 1, 37, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }
}
