package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

/**
 * O miolo da Destilaria: o {@code TileEntityDistillery} do Witchery.
 *
 * <p>Sete casas — as <b>duas</b> que entram, os <b>potes de barro</b> e as <b>quatro</b> que saem. Ela não tem
 * fogo nem combustível: quem a move é o <b>altar</b>, e ela gasta seis décimos de poder por batida enquanto
 * destila. Sem altar por perto, para.
 *
 * <p>Uma destilação leva quarenta segundos — os oitocentos tiques do original — e gasta os potes que a receita
 * pedir: é dentro deles que o que se destila sai.
 */
public class DistilleryBlockEntity extends BaseContainerBlockEntity {
    public static final int INPUT_A = 0, INPUT_B = 1, JARS = 2;
    public static final int OUTPUT_FIRST = 3, OUTPUT_COUNT = 4;
    public static final int SIZE = 7;

    /** Quarenta segundos por destilação, e seis décimos de poder por batida. */
    public static final int COOK_TIME = 800;
    public static final float POWER_PER_TICK = 0.6f;

    /** De quanto em quanto se procura altar quando não há o que destilar. */
    public static final int RESCAN_EVERY = 40;

    /** Os números que a tela precisa: quanto já se destilou e se há poder. */
    public static final int DATA_SIZE = 2;
    public static final int DATA_COOK = 0, DATA_POWER = 1;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public int cookTime;
    public int powerLevel;

    public DistilleryBlockEntity(BlockPos pos, BlockState state) {
        super(OccultaBlocks.DISTILLERY_ENTITY, pos, state);
    }

    // ------------------------------------------------------------------ o que ela é

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.thaumcraft.distillery");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new DistilleryMenu(id, inventory, this, this.data());
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

    /** O que sai não entra, e nos potes só vão potes. */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot >= OUTPUT_FIRST) return false;
        if (slot == JARS) return stack.is(OccultaItems.CLAY_JAR);
        return !stack.is(OccultaItems.CLAY_JAR);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        net.minecraft.world.ContainerHelper.loadAllItems(input, this.items);
        this.cookTime = input.getIntOr("CookTime", 0);
        this.powerLevel = input.getIntOr("Power", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        net.minecraft.world.ContainerHelper.saveAllItems(output, this.items);
        output.putInt("CookTime", this.cookTime);
        output.putInt("Power", this.powerLevel);
    }

    /**
     * Quantos potes o desenhista viu por último.
     *
     * <p>O desenhista põe <b>uma garrafa por pote</b>, e para isso quem joga precisa saber quantos há — e o que
     * está dentro de uma alma não chega ao cliente sozinho. Quando o número muda, manda-se o bloco de novo.
     */
    private int jarsShown = -1;

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener>
            getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level == null || this.level.isClientSide()) return;
        int agora = this.getItem(JARS).getCount();
        if (agora == this.jarsShown) return;
        this.jarsShown = agora;
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(),
                net.minecraft.world.level.block.Block.UPDATE_ALL);
    }

    public ContainerData data() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_COOK -> DistilleryBlockEntity.this.cookTime;
                    case DATA_POWER -> DistilleryBlockEntity.this.powerLevel;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case DATA_COOK -> DistilleryBlockEntity.this.cookTime = value;
                    case DATA_POWER -> DistilleryBlockEntity.this.powerLevel = value;
                    default -> {
                    }
                }
            }

            @Override
            public int getCount() {
                return DATA_SIZE;
            }
        };
    }

    // ------------------------------------------------------------------ o que ela faz

    /** O {@code updateEntity}: com receita e com altar, destila; sem uma das duas, para. */
    public static void tick(Level level, BlockPos pos, BlockState state, DistilleryBlockEntity destilaria) {
        if (!(level instanceof ServerLevel server)) return;
        boolean mudou = false;

        if (!destilaria.canDistil()) {
            if (level.getGameTime() % RESCAN_EVERY == 0) {
                destilaria.powerLevel = PowerSources.closest(server, pos) != null ? 1 : 0;
            }
            if (destilaria.cookTime != 0) {
                destilaria.cookTime = 0;
                mudou = true;
            }
        } else {
            AltarBlockEntity altar = PowerSources.closest(server, pos);
            destilaria.powerLevel = altar != null ? 1 : 0;
            if (altar != null && altar.consume(POWER_PER_TICK)) {
                destilaria.cookTime++;
                if (destilaria.cookTime >= COOK_TIME) {
                    destilaria.cookTime = 0;
                    destilaria.distil();
                }
                mudou = true;
            }
        }

        if (mudou) destilaria.setChanged();
    }

    /** A receita que está na máquina agora, ou nada. */
    public DistilleryRecipes.Recipe recipe() {
        return DistilleryRecipes.find(this.items.get(INPUT_A), this.items.get(INPUT_B), this.items.get(JARS));
    }

    /** O {@code canSmelt}: há receita, e o que ela dá cabe onde tem de caber. */
    public boolean canDistil() {
        DistilleryRecipes.Recipe receita = this.recipe();
        if (receita == null) return false;
        List<ItemStack> sai = receita.outputs();
        for (int i = 0; i < sai.size(); i++) {
            ItemStack casa = this.items.get(OUTPUT_FIRST + i);
            if (casa.isEmpty()) continue;
            if (!ItemStack.isSameItemSameComponents(casa, sai.get(i))) return false;
            if (casa.getCount() + sai.get(i).getCount() > casa.getMaxStackSize()) return false;
        }
        return true;
    }

    /** O {@code smeltItem}: gasta o que entrou e os potes, e põe o que saiu nas quatro casas. */
    public void distil() {
        DistilleryRecipes.Recipe receita = this.recipe();
        if (receita == null) return;
        List<ItemStack> sai = receita.outputs();
        for (int i = 0; i < sai.size() && i < OUTPUT_COUNT; i++) {
            ItemStack casa = this.items.get(OUTPUT_FIRST + i);
            if (casa.isEmpty()) this.items.set(OUTPUT_FIRST + i, sai.get(i).copy());
            else casa.grow(sai.get(i).getCount());
        }
        this.items.get(INPUT_A).shrink(1);
        if (!this.items.get(INPUT_B).isEmpty()) this.items.get(INPUT_B).shrink(1);
        if (receita.jars() > 0) this.items.get(JARS).shrink(receita.jars());
    }

    /** Quanto já se destilou, de zero ao tamanho pedido — para a seta da tela. */
    public int cookScaled(int tamanho) {
        return this.cookTime * tamanho / COOK_TIME;
    }
}
