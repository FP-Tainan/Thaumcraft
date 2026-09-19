package net.thaumcraft.block.entity;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.inventory.ArcaneSpaMenu;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import org.jetbrains.annotations.Nullable;

/**
 * O spa arcano: o {@code TileSpa} da 4.2.3.5. Um tanque de cinco baldes e uma casa de sais de banho. A cada dois
 * segundos, sem redstone, ele verte um balde por cima: misturando, água com um sal vira fonte de fluido purificante;
 * sem misturar, sai o fluido do tanque como está. Com o bloco de cima já cheio do mesmo, a fonte nova vai para uma casa
 * vizinha (até dois blocos para cada lado) encostada nele. Não se tira nem se põe nada por cima.
 */
public class ArcaneSpaBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer,
        net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<BlockPos> {
    public static final long CAPACITY = 5 * FluidConstants.BUCKET;
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private boolean mix = true;
    private int counter;

    public final SingleVariantStorage<FluidVariant> tank = new SingleVariantStorage<>() {
        @Override
        protected FluidVariant getBlankVariant() {
            return FluidVariant.blank();
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            return CAPACITY;
        }

        @Override
        protected void onFinalCommit() {
            ArcaneSpaBlockEntity.this.setChanged();
        }
    };

    public ArcaneSpaBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ARCANE_SPA, pos, state);
    }

    public boolean mix() {
        return this.mix;
    }

    public void toggleMix() {
        this.mix = !this.mix;
        this.setChanged();
    }

    /** Em milibaldes, como a tela do original mostra. */
    public int fluidMb() {
        return (int) (this.tank.amount * 1000 / FluidConstants.BUCKET);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ArcaneSpaBlockEntity spa) {
        if (level.isClientSide() || spa.counter++ % 40 != 0 || level.hasNeighborSignal(pos) || !spa.hasIngredients()) return;
        BlockPos up = pos.above();
        Block target = spa.mix ? TCBlocks.PURIFYING_FLUID : spa.tank.variant.getFluid().defaultFluidState().createLegacyBlock().getBlock();
        BlockState above = level.getBlockState(up);
        if (above.is(target) && above.getFluidState().isSource()) {
            for (int xx = -2; xx <= 2; xx++) {
                for (int zz = -2; zz <= 2; zz++) {
                    BlockPos at = up.offset(xx, 0, zz);
                    if (spa.isValidLocation(level, at, true, target)) {
                        spa.consumeIngredients();
                        level.setBlockAndUpdate(at, target.defaultBlockState());
                        return;
                    }
                }
            }
        } else if (spa.isValidLocation(level, up, false, target)) {
            spa.consumeIngredients();
            level.setBlockAndUpdate(up, target.defaultBlockState());
        }
    }

    private boolean hasIngredients() {
        if (this.tank.isResourceBlank() || this.tank.amount < FluidConstants.BUCKET) return false;
        Fluid fluid = this.tank.variant.getFluid();
        if (this.mix) {
            return fluid.isSame(Fluids.WATER) && this.items.get(0).is(TCItems.BATH_SALTS);
        }
        return fluid.defaultFluidState().createLegacyBlock().getBlock() instanceof LiquidBlock;
    }

    private void consumeIngredients() {
        if (this.mix) this.items.get(0).shrink(1);
        try (Transaction transaction = Transaction.openOuter()) {
            this.tank.extract(this.tank.variant, FluidConstants.BUCKET, transaction);
            transaction.commit();
        }
        this.setChanged();
    }

    /** O {@code isValidLocation}: chão sólido embaixo, lugar livre (e não já cheio do mesmo), encostado se preciso. */
    private boolean isValidLocation(Level level, BlockPos pos, boolean mustBeAdjacent, Block target) {
        if (target.defaultBlockState().getFluidState().is(FluidTags.WATER) && Boolean.TRUE.equals(level.environmentAttributes().getValue(net.minecraft.world.attribute.EnvironmentAttributes.WATER_EVAPORATES, pos))) return false;
        BlockState state = level.getBlockState(pos);
        if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) return false;
        if (!state.canBeReplaced() || state.is(target) && state.getFluidState().isSource()) return false;
        if (!mustBeAdjacent) return true;
        for (Direction d : Direction.values()) {
            BlockState n = level.getBlockState(pos.relative(d));
            if (n.is(target) && n.getFluidState().isSource()) return true;
        }
        return false;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    // ----------------------------------------------------------------- o inventário

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.thaumcraft.arcane_spa");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new ArcaneSpaMenu(id, inventory, this);
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
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.is(TCItems.BATH_SALTS);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side != Direction.UP ? new int[]{0} : new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return side != Direction.UP && this.canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side != Direction.UP;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.mix = input.getBooleanOr("mix", true);
        SingleVariantStorage.readValue(this.tank, FluidVariant.CODEC, FluidVariant::blank, input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putBoolean("mix", this.mix);
        SingleVariantStorage.writeValue(this.tank, FluidVariant.CODEC, output);
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
