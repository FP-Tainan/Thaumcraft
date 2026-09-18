package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
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
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.inventory.DeconstructionTableMenu;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A mesa de desconstrução: o {@code TileDeconstructionTable} da 4.2.3.5.
 *
 * <p>O que se põe na casa dela é desfeito em dois segundos, um por vez. Cada vez, a coisa é reduzida aos
 * primários de que é feita e, com chance igual ao tamanho disso em oitenta, sobra um primário sorteado entre
 * eles — que fica na mesa até alguém o recolher na tela, virando um ponto de pesquisa. Enquanto houver um à
 * espera, a mesa não desfaz mais nada.
 */
public class DeconstructionTableBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int BREAK_TIME = 40;
    private static final int[] SLOTS = {0};

    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private @Nullable Aspect aspect;
    private int breaktime;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? DeconstructionTableBlockEntity.this.breaktime : indexOf(DeconstructionTableBlockEntity.this.aspect);
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) DeconstructionTableBlockEntity.this.breaktime = value;
            else DeconstructionTableBlockEntity.this.aspect = byIndex(value);
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public DeconstructionTableBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.DECONSTRUCTION_TABLE, pos, state);
    }

    /** O aspecto como número, para a tela: a posição dele na tabela, mais um; zero é nenhum. */
    public static int indexOf(@Nullable Aspect aspect) {
        return aspect == null ? 0 : new ArrayList<>(Aspects.all()).indexOf(aspect) + 1;
    }

    public static @Nullable Aspect byIndex(int index) {
        List<Aspect> all = new ArrayList<>(Aspects.all());
        return index <= 0 || index > all.size() ? null : all.get(index - 1);
    }

    public @Nullable Aspect aspect() {
        return this.aspect;
    }

    public ContainerData data() {
        return this.data;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DeconstructionTableBlockEntity table) {
        boolean changed = false;
        if (table.breaktime == 0 && table.canBreak()) {
            table.breaktime = BREAK_TIME;
            changed = true;
        }
        if (table.breaktime > 0 && table.canBreak()) {
            table.breaktime--;
            if (table.breaktime == 0) {
                table.breakItem(level);
                changed = true;
            }
        } else {
            table.breaktime = 0;
        }
        if (changed) table.setChanged();
    }

    private boolean canBreak() {
        ItemStack stack = this.items.get(0);
        return !stack.isEmpty() && this.aspect == null && !ObjectAspects.of(stack).isEmpty();
    }

    private void breakItem(Level level) {
        if (!this.canBreak()) return;
        AspectList primals = reduceToPrimals(ObjectAspects.of(this.items.get(0)));
        if (level.getRandom().nextInt(80) < primals.visSize()) {
            List<Aspect> found = primals.getAspects();
            this.aspect = found.get(level.getRandom().nextInt(found.size()));
        }
        this.items.get(0).shrink(1);
    }

    /** O {@code ResearchManager.reduceToPrimals}: cada composto vira os dois de que é feito, até só sobrar primário. */
    public static AspectList reduceToPrimals(AspectList list) {
        AspectList out = new AspectList();
        for (Aspect aspect : list.getAspects()) {
            if (aspect.isPrimal()) {
                out.add(aspect, list.getAmount(aspect));
            } else {
                AspectList parts = new AspectList();
                parts.add(aspect.components()[0], list.getAmount(aspect));
                parts.add(aspect.components()[1], list.getAmount(aspect));
                out.add(reduceToPrimals(parts));
            }
        }
        return out;
    }

    /** O botão da tela: o primário à espera vira um ponto de pesquisa de quem clicou. */
    public void collect(Player player) {
        if (this.aspect == null) return;
        var knowledge = net.thaumcraft.research.Knowledges.of(player);
        knowledge.pool().add(this.aspect, 1);
        net.thaumcraft.research.Knowledges.save(player, knowledge);
        this.aspect = null;
        this.setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.decontable");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new DeconstructionTableMenu(id, inventory, this, this.data);
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
        return !ObjectAspects.of(stack).isEmpty();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? new int[0] : SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return side != Direction.UP && this.canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.aspect = input.getString("Aspect").map(Aspect::of).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        if (this.aspect != null) output.putString("Aspect", this.aspect.tag());
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
