package net.thaumcraft.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.entity.TravelingTrunkEntity;
import net.thaumcraft.registry.TCMenus;
import org.jetbrains.annotations.Nullable;

/**
 * O {@code ContainerTravelingTrunk}: três fileiras (quatro com a terra), afastadas de 23 em 23 pontos como no fundo
 * {@code guitrunkbase.png}; o botão 1 faz o baú ficar ou seguir. A tampa abre e fecha com o som de baú.
 */
public class TrunkMenu extends AbstractContainerMenu {
    @Nullable
    public final TravelingTrunkEntity trunk;
    private final int numRows;

    public TrunkMenu(int id, Inventory inventory, Integer entityId) {
        this(id, inventory, inventory.player.level().getEntity(entityId) instanceof TravelingTrunkEntity t ? t : null);
    }

    public TrunkMenu(int id, Inventory playerInv, @Nullable TravelingTrunkEntity trunk) {
        super(TCMenus.TRUNK, id);
        this.trunk = trunk;
        this.numRows = trunk == null ? 3 : trunk.getRows();
        if (trunk != null) {
            for (int j = 0; j < this.numRows; j++) {
                for (int i1 = 0; i1 < 9; i1++) this.addSlot(new Slot(trunk.inventory, i1 + j * 9, 8 + i1 * 18, 15 + j * 23));
            }
            trunk.setOpen(true);
            trunk.playSound(SoundEvents.CHEST_OPEN, 0.5f, trunk.level().getRandom().nextFloat() * 0.1f + 0.9f);
        }
        for (int k = 0; k < 3; k++) {
            for (int j1 = 0; j1 < 9; j1++) this.addSlot(new Slot(playerInv, j1 + k * 9 + 9, 8 + j1 * 18, 118 + k * 18));
        }
        for (int l = 0; l < 9; l++) this.addSlot(new Slot(playerInv, l, 8 + l * 18, 176));
    }

    public static void open(ServerPlayer player, TravelingTrunkEntity trunk) {
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
                return new TrunkMenu(id, inventory, trunk);
            }
        });
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button == 1 && this.trunk != null) {
            this.trunk.setStay(!this.trunk.getStay());
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack in = slot.getItem();
            itemstack = in.copy();
            if (index < this.numRows * 9) {
                if (!this.moveItemStackTo(in, this.numRows * 9, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(in, 0, this.numRows * 9, false)) {
                return ItemStack.EMPTY;
            }
            if (in.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.trunk != null && this.trunk.isAlive();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.trunk != null) {
            this.trunk.setOpen(false);
            this.trunk.playSound(SoundEvents.CHEST_CLOSE, 0.5f, this.trunk.level().getRandom().nextFloat() * 0.1f + 0.9f);
        }
    }
}
