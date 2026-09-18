package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.registry.TCBlockEntities;

/**
 * O baú faminto: o {@code TileChestHungry} da 4.2.3.5. Vinte e sete casas, como um baú comum, e a tampa que se
 * abre com quem olha dentro — e dá uma mordidinha (abre dois décimos) cada vez que engole alguma coisa.
 */
public class HungryChestBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    public float lidAngle;
    public float prevLidAngle;
    private int users;

    public HungryChestBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.HUNGRY_CHEST, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HungryChestBlockEntity chest) {
        chest.prevLidAngle = chest.lidAngle;
        if (chest.users > 0 && chest.lidAngle == 0.0f && !level.isClientSide()) {
            level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5f, level.getRandom().nextFloat() * 0.1f + 0.9f);
        }
        if (chest.users == 0 && chest.lidAngle > 0.0f || chest.users > 0 && chest.lidAngle < 1.0f) {
            float before = chest.lidAngle;
            chest.lidAngle += chest.users > 0 ? 0.1f : -0.1f;
            if (chest.lidAngle > 1.0f) chest.lidAngle = 1.0f;
            if (chest.lidAngle < 0.5f && before >= 0.5f && !level.isClientSide()) {
                level.playSound(null, pos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5f, level.getRandom().nextFloat() * 0.1f + 0.9f);
            }
            if (chest.lidAngle < 0.0f) chest.lidAngle = 0.0f;
        }
    }

    /** Os eventos de bloco do original: 1 é quantos estão olhando dentro, 2 é a mordida ao engolir. */
    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 1) {
            this.users = param;
            return true;
        }
        if (id == 2) {
            if (this.lidAngle < param / 10.0f) this.lidAngle = param / 10.0f;
            return true;
        }
        return super.triggerEvent(id, param);
    }

    @Override
    public void startOpen(ContainerUser user) {
        if (this.remove || this.level == null) return;
        this.users++;
        this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.users);
    }

    @Override
    public void stopOpen(ContainerUser user) {
        if (this.remove || this.level == null) return;
        this.users = Math.max(0, this.users - 1);
        this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.users);
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
    protected Component getDefaultName() {
        return Component.translatable("block.thaumcraft.hungry_chest");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(input)) net.minecraft.world.ContainerHelper.loadAllItems(input, this.items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.trySaveLootTable(output)) net.minecraft.world.ContainerHelper.saveAllItems(output, this.items);
    }
}
