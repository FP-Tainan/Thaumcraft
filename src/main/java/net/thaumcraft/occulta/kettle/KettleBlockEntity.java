package net.thaumcraft.occulta.kettle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.PowerSources;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A alma do Caldeirão de Pote: a {@code TileEntityKettle} do Witchery.
 *
 * <p>O pote pendurado nas correntes é o contrário do Caldeirão da Bruxa: <b>nada se mistura por ordem</b>. Enche-se
 * de água, acende-se lume por baixo, atiram-se <b>seis coisas</b> lá para dentro e, se elas forem uma receita, o
 * líquido ganha cor e fica pronto. Aí é chegar com um frasco de vidro e tirar.
 *
 * <p>Errar tem preço: coisa a mais, coisa que não casa com receita nenhuma, ou o lume que se apaga — e o pote
 * <b>estraga</b>. Estragado, esvazia-se e começa-se de novo.
 *
 * <p><b>Desvio declarado:</b> no original o pote tem um tanque de mil medidas que outros mods podem encher aos
 * poucos. Aqui a água é <b>sim ou não</b>: um balde enche, um balde vazio esvazia. Sem outros mods à volta, o que
 * se vê é o mesmo, e assim não se carrega um sistema de fluidos por causa de um balde.
 */
public class KettleBlockEntity extends BlockEntity implements WorldlyContainer {
    /** As seis casas do que entra, a do que sai e a dos frascos. */
    public static final int INGREDIENTS = 6;
    public static final int RESULT = 6;
    public static final int BOTTLES = 7;
    public static final int SIZE = 8;

    /** De quantas em quantas batidas o pote se olha: as vinte do original. */
    public static final int EVERY = 20;

    /** A cor da água parada, quando nenhuma receita lhe deu outra. */
    public static final int PLAIN = 0xFF373743;
    /** E a do pote estragado. */
    public static final int RUINED = 0xFF7F9C00;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private boolean water;
    private boolean ruined;
    private boolean powered;
    private int color;
    private long ticks;

    public KettleBlockEntity(BlockPos pos, BlockState state) {
        super(OccultaBlocks.WITCHES_KETTLE_ENTITY, pos, state);
    }

    // ------------------------------------------------------------------ o que o pote é

    public boolean filled() {
        return this.water;
    }

    public boolean ruined() {
        return this.ruined;
    }

    public boolean powered() {
        return this.powered;
    }

    public int color() {
        return this.color;
    }

    /** Se há coisa lá dentro à espera. */
    public boolean brewing() {
        return this.water && this.some() && !this.ruined;
    }

    /** Se o que saiu já está lá, à espera de um frasco. */
    public boolean ready() {
        return !this.ruined && !this.getItem(RESULT).isEmpty();
    }

    public int bottles() {
        return this.getItem(BOTTLES).getCount();
    }

    private boolean some() {
        for (int i = 0; i < INGREDIENTS; i++) {
            if (!this.getItem(i).isEmpty()) return true;
        }
        return false;
    }

    private boolean all() {
        for (int i = 0; i < INGREDIENTS; i++) {
            if (this.getItem(i).isEmpty()) return false;
        }
        return true;
    }

    /** O que está no pote, pela ordem das casas. */
    public List<ItemStack> inside() {
        List<ItemStack> dentro = new ArrayList<>();
        for (int i = 0; i < INGREDIENTS; i++) dentro.add(this.getItem(i));
        return dentro;
    }

    // ------------------------------------------------------------------ encher, esvaziar, estragar

    /** Enche o pote de água; devolve se havia onde a pôr. */
    public boolean fill() {
        if (this.water) return false;
        this.water = true;
        this.reset(false);
        return true;
    }

    /** Esvazia-o. */
    public boolean empty() {
        if (!this.water) return false;
        this.reset(true);
        return true;
    }

    /**
     * Volta ao princípio: o {@code reset} do original.
     *
     * @param esvazia se a água também se vai
     */
    public void reset(boolean esvazia) {
        if (esvazia) this.water = false;
        this.ruined = false;
        this.powered = false;
        this.color = 0;
        for (int i = 0; i <= RESULT; i++) this.items.set(i, ItemStack.EMPTY);
        this.sync();
    }

    public void ruin() {
        this.ruined = true;
        this.color = 0;
        this.items.set(RESULT, ItemStack.EMPTY);
        this.sync();
    }

    // ------------------------------------------------------------------ a batida

    public void tick() {
        if (!(this.level instanceof ServerLevel level)) return;
        this.ticks++;
        if (this.ruined || this.ticks % EVERY != 0) return;
        if (!this.water || (!this.some() && this.getItem(RESULT).isEmpty())) return;

        // sem lume por baixo, o que estava a cozinhar estraga-se
        if (!level.getBlockState(this.worldPosition.below()).is(BlockTags.FIRE)) {
            this.ruin();
            return;
        }
        if (!this.getItem(RESULT).isEmpty()) return;

        boolean cheio = this.all();
        KettleRecipes.Recipe receita = KettleRecipes.find(this.inside(), !cheio, level);
        if (receita == null || (!cheio && receita.color() == 0)) {
            this.ruin();
            return;
        }

        boolean era = this.powered;
        if (this.color != receita.color()) {
            this.color = receita.color();
            this.sync();
        }
        this.powered = receita.power() <= 0.0f
                || (cheio ? PowerSources.consume(level, this.worldPosition, receita.power())
                : PowerSources.closest(level, this.worldPosition) != null);
        if (!cheio) {
            if (era != this.powered) this.sync();
            return;
        }
        if (!this.powered) {
            if (era != this.powered) this.sync();
            return;
        }

        this.items.set(RESULT, receita.output());
        for (int i = 0; i < INGREDIENTS; i++) this.items.set(i, ItemStack.EMPTY);
        this.sync();
    }

    // ------------------------------------------------------------------ tirar com um frasco

    /**
     * Tira o que saiu, gastando um frasco de vidro: o clique do original com o frasco na mão.
     *
     * @return o que foi tirado, ou nada
     */
    public ItemStack takeWithBottle(Player quem) {
        if (!this.ready() || this.bottles() <= 0) return ItemStack.EMPTY;
        ItemStack saída = this.getItem(RESULT).copy();
        ItemStack frascos = this.getItem(BOTTLES);
        int quantos = Math.min(saída.getCount(), frascos.getCount());
        if (quantos <= 0) return ItemStack.EMPTY;

        frascos.shrink(quantos);
        if (frascos.isEmpty()) this.items.set(BOTTLES, ItemStack.EMPTY);
        saída.setCount(quantos);
        this.reset(true);
        return saída;
    }

    /** O que se atira para dentro do pote: frascos para a casa deles, o resto para as seis. */
    public boolean throwIn(ItemStack coisa) {
        if (coisa.is(Items.GLASS_BOTTLE)) {
            ItemStack frascos = this.getItem(BOTTLES);
            if (frascos.isEmpty()) {
                this.items.set(BOTTLES, coisa.copy());
                this.sync();
                return true;
            }
            if (frascos.getCount() + coisa.getCount() > this.getMaxStackSize()) return false;
            frascos.grow(coisa.getCount());
            this.sync();
            return true;
        }
        if (!this.water || !this.getItem(RESULT).isEmpty()) return false;
        for (int i = 0; i < INGREDIENTS; i++) {
            if (!this.getItem(i).isEmpty()) continue;
            this.items.set(i, coisa.copyWithCount(1));
            this.sync();
            return true;
        }
        // não há onde pôr: o pote estraga-se, que é o que o original faz
        if (!this.ruined) this.ruin();
        return true;
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
        ItemStack tirado = ContainerHelper.removeItem(this.items, casa, quantos);
        if (!tirado.isEmpty()) this.sync();
        return tirado;
    }

    @Override
    public ItemStack removeItemNoUpdate(int casa) {
        return ContainerHelper.takeItem(this.items, casa);
    }

    @Override
    public void setItem(int casa, ItemStack coisa) {
        this.items.set(casa, coisa);
        this.sync();
    }

    @Override
    public boolean stillValid(Player quem) {
        return net.minecraft.world.Container.stillValidBlockEntity(this, quem);
    }

    @Override
    public boolean canPlaceItem(int casa, ItemStack coisa) {
        if (casa == RESULT) return false;
        if (casa == BOTTLES) return coisa.is(Items.GLASS_BOTTLE);
        return !coisa.is(Items.GLASS_BOTTLE) && this.getItem(RESULT).isEmpty() && this.water;
    }

    @Override
    public int[] getSlotsForFace(Direction lado) {
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7};
    }

    @Override
    public boolean canPlaceItemThroughFace(int casa, ItemStack coisa, @Nullable Direction lado) {
        return this.canPlaceItem(casa, coisa);
    }

    @Override
    public boolean canTakeItemThroughFace(int casa, ItemStack coisa, Direction lado) {
        return casa == RESULT && this.ready() && this.bottles() >= coisa.getCount();
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    // ------------------------------------------------------------------ guardar e mandar

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.water = input.getBooleanOr("water", false);
        this.ruined = input.getBooleanOr("ruined", false);
        this.powered = input.getBooleanOr("powered", false);
        this.color = input.getIntOr("color", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putBoolean("water", this.water);
        output.putBoolean("ruined", this.ruined);
        output.putBoolean("powered", this.powered);
        output.putInt("color", this.color);
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
