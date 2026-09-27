package net.thaumcraft.occulta.spinning;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.PowerSources;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A alma da Roca: a {@code TileEntitySpinningWheel} do Witchery.
 *
 * <p>Cinco casas: a <b>fibra</b>, três de <b>tempero</b> e a de saída. Ela não tem lume nem combustível — quem a
 * faz girar é o <b>altar</b>, e sem altar por perto ela para onde estava, como a Destilaria.
 *
 * <p>Os números são os do original: <b>trezentos tiques</b> por fio e <b>seis décimos</b> de poder por batida.
 */
public class SpinningWheelBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    /** As casas, pela ordem do original. */
    public static final int FIBRE = 0;
    public static final int MOD_A = 1;
    public static final int RESULT = 2;
    public static final int MOD_B = 3;
    public static final int MOD_C = 4;
    public static final int SIZE = 5;

    /** Quanto tempo leva um fio, e quanto poder ele come por batida. */
    public static final int SPIN_TIME = 300;
    public static final float POWER_PER_TICK = 0.6f;

    public static final int DATA_SPIN = 0;
    public static final int DATA_POWER = 1;
    public static final int DATA_SIZE = 2;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    public int spinTime;
    public int powerLevel;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int qual) {
            return qual == DATA_SPIN ? SpinningWheelBlockEntity.this.spinTime
                    : SpinningWheelBlockEntity.this.powerLevel;
        }

        @Override
        public void set(int qual, int quanto) {
            if (qual == DATA_SPIN) SpinningWheelBlockEntity.this.spinTime = quanto;
            else SpinningWheelBlockEntity.this.powerLevel = quanto;
        }

        @Override
        public int getCount() {
            return DATA_SIZE;
        }
    };

    public SpinningWheelBlockEntity(BlockPos pos, BlockState state) {
        super(OccultaBlocks.SPINNING_WHEEL_ENTITY, pos, state);
    }

    /** Os três temperos, pela ordem das casas do original. */
    public List<ItemStack> modifiers() {
        return List.of(this.getItem(MOD_A), this.getItem(MOD_B), this.getItem(MOD_C));
    }

    /** A receita que a roca pode fiar agora, ou nada. */
    public SpinningRecipes.@Nullable Recipe recipe() {
        if (this.getItem(FIBRE).isEmpty()) return null;
        SpinningRecipes.Recipe receita = SpinningRecipes.find(this.getItem(FIBRE), this.modifiers());
        if (receita == null) return null;

        ItemStack sai = this.getItem(RESULT);
        if (sai.isEmpty()) return receita;
        ItemStack feito = receita.output();
        if (!ItemStack.isSameItemSameComponents(sai, feito)) return null;
        int soma = sai.getCount() + feito.getCount();
        return soma <= this.getMaxStackSize() && soma <= feito.getMaxStackSize() ? receita : null;
    }

    /** Uma batida da roca. */
    public void tick() {
        if (!(this.level instanceof ServerLevel level)) return;
        SpinningRecipes.Recipe receita = this.recipe();
        if (receita == null) {
            this.powerLevel = PowerSources.closest(level, this.worldPosition) != null ? 1 : 0;
            if (this.spinTime != 0) {
                this.spinTime = 0;
                this.setChanged();
            }
            return;
        }

        if (!PowerSources.consume(level, this.worldPosition, POWER_PER_TICK)) {
            this.powerLevel = 0;
            return;
        }
        this.powerLevel = 1;
        this.spinTime++;
        if (this.spinTime < SPIN_TIME) return;

        this.spinTime = 0;
        this.spin(receita);
        this.setChanged();
    }

    /** O {@code smeltItem}: sai o fio, gasta-se a fibra e some um de cada tempero que a receita pediu. */
    private void spin(SpinningRecipes.Recipe receita) {
        ItemStack feito = receita.output();
        ItemStack sai = this.getItem(RESULT);
        if (sai.isEmpty()) this.items.set(RESULT, feito);
        else sai.grow(feito.getCount());

        this.getItem(FIBRE).shrink(receita.fibreCount());

        List<net.minecraft.world.item.Item> faltam =
                new java.util.ArrayList<>(receita.modifiers());
        for (int casa : new int[]{MOD_A, MOD_B, MOD_C}) {
            ItemStack tempero = this.getItem(casa);
            if (tempero.isEmpty() || !faltam.remove(tempero.getItem())) continue;
            tempero.shrink(1);
        }
    }

    // ------------------------------------------------------------------ o baú

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack item : this.items) {
            if (!item.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int casa) {
        return this.items.get(casa);
    }

    @Override
    public ItemStack removeItem(int casa, int quantos) {
        return ContainerHelper.removeItem(this.items, casa, quantos);
    }

    @Override
    public ItemStack removeItemNoUpdate(int casa) {
        return ContainerHelper.takeItem(this.items, casa);
    }

    @Override
    public void setItem(int casa, ItemStack coisa) {
        this.items.set(casa, coisa);
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player quem) {
        return net.minecraft.world.Container.stillValidBlockEntity(this, quem);
    }

    @Override
    public boolean canPlaceItem(int casa, ItemStack coisa) {
        return casa != RESULT;
    }

    @Override
    public int[] getSlotsForFace(Direction lado) {
        return new int[]{FIBRE, MOD_A, MOD_B, MOD_C};
    }

    @Override
    public boolean canPlaceItemThroughFace(int casa, ItemStack coisa, @Nullable Direction lado) {
        return this.canPlaceItem(casa, coisa);
    }

    @Override
    public boolean canTakeItemThroughFace(int casa, ItemStack coisa, Direction lado) {
        return casa == RESULT;
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    // ------------------------------------------------------------------ a tela

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.thaumcraft.spinning_wheel");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player quem) {
        return new SpinningWheelMenu(id, inventory, this, this.data);
    }

    // ------------------------------------------------------------------ guardar

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.spinTime = input.getIntOr("SpinTime", 0);
        this.powerLevel = input.getIntOr("Power", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("SpinTime", this.spinTime);
        output.putInt("Power", this.powerLevel);
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener>
            getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
