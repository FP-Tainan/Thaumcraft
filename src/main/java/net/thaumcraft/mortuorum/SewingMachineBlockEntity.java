package net.thaumcraft.mortuorum;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * O miolo da Máquina de Costura: o {@code TileEntitySewing} do Necromancy.
 *
 * <p>Ela guarda só duas coisas, que são o que a costura gasta: a agulha e a linha. A grade de quatro por quatro
 * vive na tela, como a da bancada do jogo.
 */
public class SewingMachineBlockEntity extends BaseContainerBlockEntity {
    public static final int NEEDLE = 0, THREAD = 1;

    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    public SewingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(MortuorumBlocks.SEWING_MACHINE_ENTITY, pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.thaumcraft.sewing_machine");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new SewingMenu(id, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == NEEDLE ? stack.is(MortuorumItems.BONE_NEEDLE)
                : stack.is(net.minecraft.world.item.Items.STRING);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
    }

    /** Tem agulha e linha para costurar? */
    public boolean ready() {
        return this.getItem(NEEDLE).is(MortuorumItems.BONE_NEEDLE)
                && this.getItem(THREAD).is(net.minecraft.world.item.Items.STRING);
    }

    /** Uma costura gasta uma agulha e uma linha, como no {@code SlotSewing}. */
    public void spend() {
        this.removeItem(NEEDLE, 1);
        this.removeItem(THREAD, 1);
        this.setChanged();
    }
}
