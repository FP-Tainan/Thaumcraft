package net.thaumcraft.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.entity.GolemEntity;
import net.thaumcraft.entity.golem.GolemHelper;
import net.thaumcraft.entity.golem.GolemInventory;
import net.thaumcraft.item.GolemCoreItem;
import net.thaumcraft.registry.TCMenus;
import org.jetbrains.annotations.Nullable;

/**
 * A tela do golem: o {@code ContainerGolem} (e o {@code ContainerGhostSlots}) da 4.2.3.5. As seis casas do golem são
 * fantasmas — o que se põe nelas é uma cópia, e o clique muda a quantidade — e rolam de seis em seis quando a melhoria
 * de fogo dá mais casas. Os botões (cores, chaves, rolagem) chegam pelo {@code clickMenuButton}, com os números do
 * original. Enquanto a tela está aberta o golem fica parado.
 */
public class GolemMenu extends AbstractContainerMenu {
    @Nullable
    public final GolemEntity golem;
    public int currentScroll;
    public int maxScroll;
    private final int ghostCount;

    public GolemMenu(int id, Inventory inventory, Integer entityId) {
        this(id, inventory, inventory.player.level().getEntity(entityId) instanceof GolemEntity g ? g : null);
    }

    public GolemMenu(int id, Inventory playerInv, @Nullable GolemEntity golem) {
        super(TCMenus.GOLEM, id);
        this.golem = golem;
        int ghosts = 0;
        if (golem != null) {
            golem.paused = true;
            if (GolemCoreItem.hasInventory(golem.getCore())) {
                int slots = golem.inventory.slotCount;
                this.maxScroll = slots / 6 - 1;
                for (int a = 0; a < Math.min(6, slots); a++) {
                    this.addSlot(new Ghost(this, a, 100 + a / 2 * 28, 16 + a % 2 * 31, golem.getCore() == 5));
                }
                ghosts = Math.min(6, slots);
            }
        }
        this.ghostCount = ghosts;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
        }
        for (int i = 0; i < 9; i++) this.addSlot(new Slot(playerInv, i, 8 + i * 18, 142));
    }

    public static void open(ServerPlayer player, GolemEntity golem) {
        player.openMenu(new net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<Integer>() {
            @Override
            public Integer getScreenOpeningData(ServerPlayer p) {
                return golem.getId();
            }

            @Override
            public Component getDisplayName() {
                return golem.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player p) {
                return new GolemMenu(id, inventory, golem);
            }
        });
    }

    /** Uma casa fantasma: mostra a casa {@code index + rolagem × 6} da lista do golem. */
    public static class Ghost extends Slot {
        private final GolemMenu menu;
        private final int index;
        private final boolean fluid;

        Ghost(GolemMenu menu, int index, int x, int y, boolean fluid) {
            super(menu.golem.inventory, index, x, y);
            this.menu = menu;
            this.index = index;
            this.fluid = fluid;
        }

        private GolemInventory inv() {
            return this.menu.golem.inventory;
        }

        public int actual() {
            return this.index + this.menu.currentScroll * 6;
        }

        public boolean isFluid() {
            return this.fluid;
        }

        @Override
        public ItemStack getItem() {
            return this.inv().getItem(this.actual());
        }

        @Override
        public void set(ItemStack stack) {
            this.inv().setItem(this.actual(), stack);
            this.setChanged();
        }

        @Override
        public void setByPlayer(ItemStack stack, ItemStack previous) {
            this.set(stack);
        }

        @Override
        public ItemStack remove(int amount) {
            return this.inv().removeItem(this.actual(), amount);
        }

        /** O {@code getSlotStackLimit}: 256 no núcleo de encher, 1 nos outros (e no de líquidos). */
        @Override
        public int getMaxStackSize() {
            return this.menu.golem.getCore() == 0 ? 256 : 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return this.getMaxStackSize();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !this.fluid || GolemHelper.isFluidContainer(stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput input, Player player) {
        if (slotId < 0 || slotId >= this.ghostCount || !(this.slots.get(slotId) instanceof Ghost slot)) {
            // a tecla de mover (shift) não faz nada nas casas do jogador: o original não tem quickMove
            if (input == ContainerInput.QUICK_MOVE) return;
            super.clicked(slotId, button, input, player);
            return;
        }
        ItemStack carried = this.getCarried();
        ItemStack in = slot.getItem();
        int limit = slot.getMaxStackSize();
        switch (input) {
            case PICKUP -> {
                if (button != 0 && button != 1) return;
                if (in.isEmpty()) {
                    if (!carried.isEmpty() && slot.mayPlace(carried)) {
                        int k1 = Math.min(button == 0 ? carried.getCount() : 1, limit);
                        if (carried.getCount() >= k1) slot.set(carried.copyWithCount(k1));
                    }
                } else if (carried.isEmpty()) {
                    // esquerdo tira um, direito põe um
                    int k1 = button == 0 ? 1 : -1;
                    if (in.getCount() - k1 <= limit) {
                        ItemStack next = in.copyWithCount(in.getCount() - k1);
                        slot.set(next.getCount() <= 0 ? ItemStack.EMPTY : next);
                    }
                } else if (slot.mayPlace(carried)) {
                    if (ItemStack.isSameItemSameComponents(in, carried)) {
                        int k1 = button == 0 ? carried.getCount() : 1;
                        k1 = Math.min(k1, limit - in.getCount());
                        k1 = Math.min(k1, carried.getMaxStackSize() - in.getCount());
                        if (k1 > 0) slot.set(in.copyWithCount(in.getCount() + k1));
                    } else if (carried.getCount() <= limit) {
                        slot.set(carried.copy());
                    }
                }
            }
            case QUICK_MOVE -> {
                if (in.isEmpty()) return;
                if (button == 0) slot.set(ItemStack.EMPTY);
                else if (button == 1) slot.set(in.copyWithCount(Math.min(in.getCount() + 16, limit)));
            }
            case THROW -> {
                if (!carried.isEmpty() || in.isEmpty()) return;
                int left = button == 0 ? in.getCount() - 1 : 0;
                slot.set(left <= 0 ? ItemStack.EMPTY : in.copyWithCount(left));
            }
            default -> {
            }
        }
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return !(slot instanceof Ghost) && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return !(slot instanceof Ghost);
    }

    /** Os botões do original: 66/67 rolam, 50–57 viram as chaves, e as cores descem (0..n−1) ou sobem (n..2n−1). */
    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (this.golem == null) return false;
        if (button == 66 && this.currentScroll > 0) this.currentScroll--;
        if (button == 67 && this.currentScroll < this.maxScroll) this.currentScroll++;
        if (button >= 50 && button <= 57) this.golem.setToggle(button - 50, !this.golem.getToggles()[button - 50]);
        int slots = this.golem.inventory.slotCount;
        if (button >= 0 && button < slots) {
            int c = this.golem.getColors(button) - 1;
            if (c < -1) c = 15;
            this.golem.setColors(button, c);
        }
        if (button >= slots && button < slots * 2) {
            int c = this.golem.getColors(button - slots) + 1;
            if (c > 15) c = -1;
            this.golem.setColors(button - slots, c);
        }
        this.golem.level().playSound(null, this.golem.getX(), this.golem.getY(), this.golem.getZ(), SoundEvents.UI_BUTTON_CLICK.value(),
                SoundSource.NEUTRAL, 0.2f, 0.8f);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.golem != null && this.golem.inventory.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.golem != null) this.golem.paused = false;
    }
}
