package net.thaumcraft.inventory;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.baubles.BaubleItem;
import net.thaumcraft.api.baubles.BaubleType;
import net.thaumcraft.baubles.Baubles;
import net.thaumcraft.registry.TCMenus;

import java.util.List;

/**
 * O inventário expandido do Baubles 1.0.1.10: o {@code ContainerPlayerExpanded}. A grade de craft de dois por dois
 * foi para a direita, a armadura segue na coluna da esquerda, e no meio ficam as quatro casas: amuleto, dois anéis e
 * cinto.
 */
public class BaublesMenu extends AbstractCraftingMenu {
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final Identifier[] ARMOR_ICONS = {
            Identifier.withDefaultNamespace("container/slot/helmet"), Identifier.withDefaultNamespace("container/slot/chestplate"),
            Identifier.withDefaultNamespace("container/slot/leggings"), Identifier.withDefaultNamespace("container/slot/boots")};
    private final Player owner;

    public BaublesMenu(int id, Inventory inventory) {
        super(TCMenus.BAUBLES, id, 2, 2);
        this.owner = inventory.player;
        this.addResultSlot(this.owner, 144, 36);
        this.addCraftingGridSlots(106, 26);
        for (int i = 0; i < 4; i++) {
            this.addSlot(new ArmorSlot(inventory, this.owner, ARMOR[i], 39 - i, 8, 8 + i * 18, ARMOR_ICONS[i]));
        }
        Container worn = Baubles.container(this.owner);
        this.addSlot(new BaubleSlot(worn, BaubleType.AMULET, Baubles.AMULET, 80, 8));
        this.addSlot(new BaubleSlot(worn, BaubleType.RING, Baubles.RING_1, 80, 26));
        this.addSlot(new BaubleSlot(worn, BaubleType.RING, Baubles.RING_2, 80, 44));
        this.addSlot(new BaubleSlot(worn, BaubleType.BELT, Baubles.BELT, 80, 62));
        this.addStandardInventorySlots(inventory, 8, 84);
    }

    /** O {@code SlotBauble}: só entra peça do tipo da casa, e só sai o que aceita sair. */
    public class BaubleSlot extends Slot {
        private final BaubleType type;

        BaubleSlot(Container container, BaubleType type, int index, int x, int y) {
            super(container, index, x, y);
            this.type = type;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof BaubleItem bauble && bauble.baubleType(stack) == this.type
                    && bauble.canEquip(stack, BaublesMenu.this.owner);
        }

        @Override
        public boolean mayPickup(Player player) {
            ItemStack stack = this.getItem();
            return !stack.isEmpty() && stack.getItem() instanceof BaubleItem bauble && bauble.canUnequip(stack, player);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    @Override
    public void slotsChanged(Container container) {
        // o slotChangedCraftingGrid do jogo, que é protegido: a receita da grade, se houver, vai para o resultado
        if (!(this.owner.level() instanceof ServerLevel level) || !(this.owner instanceof net.minecraft.server.level.ServerPlayer player)) return;
        var input = this.craftSlots.asCraftInput();
        ItemStack result = ItemStack.EMPTY;
        var found = level.getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, level);
        if (found.isPresent() && this.resultSlots.setRecipeUsed(player, found.get())) {
            ItemStack made = found.get().value().assemble(input);
            if (made.isItemEnabled(level.enabledFeatures())) result = made;
        }
        this.resultSlots.setItem(0, result);
        this.setRemoteSlot(0, result);
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), 0, result));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.resultSlots.clearContent();
        if (!player.level().isClientSide()) this.clearContainer(player, this.craftSlots);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /** O {@code transferStackInSlot} do Baubles: craft, armadura e peças vão para o inventário; do inventário, cada peça procura a sua casa. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack clicked = stack.copy();
        EquipmentSlot eq = player.getEquipmentSlotForItem(clicked);
        if (index == 0) {
            if (!this.moveItemStackTo(stack, 13, 49, true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, clicked);
        } else if (index < 13) {
            if (!this.moveItemStackTo(stack, 13, 49, false)) return ItemStack.EMPTY;
        } else if (eq.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !this.slots.get(8 - eq.getIndex()).hasItem()) {
            int pos = 8 - eq.getIndex();
            if (!this.moveItemStackTo(stack, pos, pos + 1, false)) return ItemStack.EMPTY;
        } else if (clicked.getItem() instanceof BaubleItem bauble && bauble.canEquip(clicked, player)
                && this.freeBaubleSlot(bauble.baubleType(clicked)) >= 0) {
            int pos = this.freeBaubleSlot(bauble.baubleType(clicked));
            if (!this.moveItemStackTo(stack, pos, pos + 1, false)) return ItemStack.EMPTY;
        } else if (index < 40) {
            if (!this.moveItemStackTo(stack, 40, 49, false)) return ItemStack.EMPTY;
        } else if (!this.moveItemStackTo(stack, 13, 40, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY, clicked);
        else slot.setChanged();
        if (stack.getCount() == clicked.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return clicked;
    }

    private int freeBaubleSlot(BaubleType type) {
        return switch (type) {
            case AMULET -> this.slots.get(9).hasItem() ? -1 : 9;
            case RING -> !this.slots.get(10).hasItem() ? 10 : this.slots.get(11).hasItem() ? -1 : 11;
            case BELT -> this.slots.get(12).hasItem() ? -1 : 12;
        };
    }

    @Override
    public Slot getResultSlot() {
        return this.slots.get(0);
    }

    @Override
    public List<Slot> getInputGridSlots() {
        return this.slots.subList(1, 5);
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    @Override
    protected Player owner() {
        return this.owner;
    }
}
