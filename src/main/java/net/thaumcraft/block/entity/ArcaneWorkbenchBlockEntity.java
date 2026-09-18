package net.thaumcraft.block.entity;

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
import net.thaumcraft.inventory.ArcaneWorkbenchMenu;
import net.thaumcraft.registry.TCBlockEntities;

/**
 * A bancada arcana: onde se monta o que precisa de vis para existir.
 *
 * <p>Ela guarda dez coisas, como no original: as nove da grade de três por três e a varinha, que fica de
 * lado e paga a conta. A varinha não se gasta nem se consome — o que se gasta é o vis dentro dela.
 */
public class ArcaneWorkbenchBlockEntity extends BaseContainerBlockEntity {
    /** Nove da grade mais a varinha. */
    public static final int SIZE = 10;
    /** A casa em que a varinha fica. */
    public static final int WAND_SLOT = 9;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public ArcaneWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ARCANE_WORKBENCH, pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.thaumcraft.arcane_workbench");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new ArcaneWorkbenchMenu(id, inventory, this);
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
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    /** Quem está perto vê a varinha deitada na mesa: a mudança vai para o cliente. */
    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
    }
}
