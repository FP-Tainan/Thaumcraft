package net.thaumcraft.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.naturalis.EvilTrunkEntity;
import net.thaumcraft.registry.TCMenus;
import org.jetbrains.annotations.Nullable;

/**
 * O Baú Maligno aberto: o {@code ContainerEvilTrunk} do Magia Naturalis 0.5.0 — quatro fileiras afastadas de 23 em
 * 23 pontos, como as do baú itinerante do Thaumcraft, e o botão um manda o baú ficar ou seguir.
 */
public class EvilTrunkMenu extends AbstractContainerMenu {
    public final @Nullable EvilTrunkEntity trunk;

    public EvilTrunkMenu(int id, Inventory inventory, Integer entityId) {
        this(id, inventory, inventory.player.level().getEntity(entityId) instanceof EvilTrunkEntity t ? t : null);
    }

    public EvilTrunkMenu(int id, Inventory playerInv, @Nullable EvilTrunkEntity trunk) {
        super(TCMenus.EVIL_TRUNK, id);
        this.trunk = trunk;
        if (trunk != null) {
            for (int row = 0; row < EvilTrunkEntity.ROWS; row++) {
                for (int col = 0; col < 9; col++) {
                    this.addSlot(new Slot(trunk.inventory, col + row * 9, 8 + col * 18, 15 + row * 23));
                }
            }
            trunk.setOpen(true);
            trunk.playSound(SoundEvents.CHEST_OPEN, 0.5f, trunk.level().getRandom().nextFloat() * 0.1f + 0.9f);
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 118 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 176));
        }
    }

    public static void open(ServerPlayer player, EvilTrunkEntity trunk) {
        player.openMenu(new net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<Integer>() {
            @Override
            public Integer getScreenOpeningData(ServerPlayer p) {
                return trunk.getId();
            }

            @Override
            public Component getDisplayName() {
                return trunk.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player p) {
                return new EvilTrunkMenu(id, inventory, trunk);
            }
        });
    }

    /** O botão um: o baú fica onde está ou volta a seguir. */
    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button == 1 && this.trunk != null) {
            this.trunk.setWaiting(!this.trunk.isWaiting());
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();
            int dentro = EvilTrunkEntity.ROWS * 9;
            if (index < dentro) {
                if (!this.moveItemStackTo(stack, dentro, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(stack, 0, dentro, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.trunk != null && !this.trunk.isRemoved() && this.trunk.distanceTo(player) < 8.0f;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.trunk == null) return;
        this.trunk.setOpen(false);
        this.trunk.playSound(SoundEvents.CHEST_CLOSE, 0.5f,
                this.trunk.level().getRandom().nextFloat() * 0.1f + 0.9f);
    }
}
