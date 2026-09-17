package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.registry.TCBlockEntities;

/**
 * O pedestal arcano: segura uma coisa só, à vista de todos.
 *
 * <p>É o {@code TilePedestal} do original, e é a peça de que a infusão é feita: um no meio, com o que vai
 * ser transformado, e os outros em volta, com o que vai ser gasto. Fora da infusão ele é só uma mesinha
 * bonita de pôr coisa em cima.
 */
public class PedestalBlockEntity extends BlockEntity implements Container {
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.PEDESTAL, pos, state);
    }

    public ItemStack held() {
        return this.items.get(0);
    }

    public void hold(ItemStack stack) {
        this.items.set(0, stack);
        this.sync();
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.held().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack taken = ContainerHelper.removeItem(this.items, slot, amount);
        if (!taken.isEmpty()) this.sync();
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack taken = ContainerHelper.takeItem(this.items, slot);
        this.sync();
        return taken;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        this.sync();
    }

    @Override
    public int getMaxStackSize() {
        // um pedestal segura uma coisa, e uma só
        return 1;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.sync();
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items.clear();
        ContainerHelper.loadAllItems(input, this.items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
