package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A Prateleira de Bonecas: o {@code TileEntityPoppetShelf} do Witchery.
 *
 * <p>Nove lugares, e as bonecas que estiverem nela <b>valem</b> onde quer que o dono delas esteja — é isso que
 * faz dela uma prateleira e não um baú: guardar a boneca aqui é melhor que carregá-la.
 *
 * <p>Ela entra numa lista quando nasce e sai quando é desfeita, para que quem apanha uma pancada não tenha de
 * varrer o mundo à procura dela.
 */
public class PoppetShelfBlockEntity extends BaseContainerBlockEntity {
    public static final int SIZE = 9;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public PoppetShelfBlockEntity(BlockPos pos, BlockState state) {
        super(OccultaBlocks.POPPET_SHELF_ENTITY, pos, state);
    }

    @Override
    public void setLevel(net.minecraft.world.level.Level level) {
        super.setLevel(level);
        if (!level.isClientSide()) Poppets.register(this);
    }

    @Override
    public void setRemoved() {
        Poppets.remove(this);
        super.setRemoved();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.thaumcraft.poppet_shelf");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new ChestMenu(net.minecraft.world.inventory.MenuType.GENERIC_9x1, id, inventory, this, 1);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
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
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    /** Numa prateleira de bonecas só vão bonecas. */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof PoppetItem;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        net.minecraft.world.ContainerHelper.loadAllItems(input, this.items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        net.minecraft.world.ContainerHelper.saveAllItems(output, this.items);
    }
}
