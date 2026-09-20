package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.visnet.VisNet;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.FocusUpgradeTable;
import net.thaumcraft.inventory.FocalManipulatorMenu;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCSounds;

import java.util.List;

/**
 * O manipulador focal: o {@code TileFocalManipulator} da 4.2.3.5.
 *
 * <p>Um foco na casa; o jogador escolhe na tela a melhoria do próximo posto vazio e paga a experiência (oito níveis por
 * posto). A mesa então puxa da rede de vis os primários da melhoria — 200 centésimos de cada aspecto dela, dobrando a
 * cada posto, reduzidos a primários — até cem por aspecto a cada cinco tiques, e no fim aplica a melhoria. Tirar o
 * foco no meio perde o trabalho.
 */
public class FocalManipulatorBlockEntity extends BaseContainerBlockEntity
        implements net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<BlockPos> {
    public static final int XP_MULT = 8;
    public static final int VIS_MULT = 200;

    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    /** O que ainda falta puxar. */
    public AspectList aspects = new AspectList();
    /** O tamanho do total, para a barra; zero é parado. */
    public int size;
    public int upgrade = -1;
    public int rank = -1;
    private int ticks;
    /** Do lado de quem vê: o foco mudou e a tela recomeça. */
    public boolean reset;

    public FocalManipulatorBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.FOCAL_MANIPULATOR, pos, state);
    }

    /** O vis que a melhoria pede no posto dado: 200 de cada aspecto dela, dobrando por posto, em primários. */
    public static AspectList costOf(FocusUpgradeTable.Type type, int rank) {
        int amount = VIS_MULT;
        for (int a = 1; a < rank; a++) amount *= 2;
        AspectList list = new AspectList();
        for (Aspect aspect : type.aspects().getAspects()) list.add(aspect, amount);
        return DeconstructionTableBlockEntity.reduceToPrimals(list);
    }

    /** O primeiro posto vazio do foco (6 quando estão todos cheios). */
    public static int nextRank(ItemStack focus) {
        short[] s = FocusItem.upgrades(focus);
        int rank = 1;
        while (rank <= 5 && s[rank - 1] != -1) rank++;
        return rank;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FocalManipulatorBlockEntity table) {
        if (level.isClientSide()) {
            if (table.size > 0) clientEffects.accept(level, pos);
            return;
        }
        boolean complete = false;
        if (table.rank < 0) table.rank = 0;
        table.ticks++;
        if (table.ticks % 5 == 0) {
            ItemStack focus = table.items.get(0);
            if (table.size > 0 && (table.aspects.visSize() <= 0 || focus.isEmpty())) {
                complete = true;
                level.playSound(null, pos, TCSounds.CRAFT_FAIL.value(), SoundSource.BLOCKS, 0.33f, 1.0f);
            }
            if (table.size > 0) {
                for (Aspect aspect : table.aspects.getAspectsSortedAmount()) {
                    int drain = VisNet.drainVis(level, pos, aspect, Math.min(100, table.aspects.getAmount(aspect)));
                    if (drain > 0) {
                        table.aspects.reduce(aspect, drain);
                        table.setChanged();
                    }
                }
                if (table.aspects.visSize() <= 0 && !focus.isEmpty()) {
                    complete = true;
                    FocusUpgradeTable.Type type = net.thaumcraft.api.FocusUpgrades.byId((short) table.upgrade);
                    if (type != null) FocusItem.apply(focus, type, table.rank);
                    level.playSound(null, pos, TCSounds.WAND.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            }
        }
        if (complete) {
            table.size = 0;
            table.rank = -1;
            table.aspects = new AspectList();
            table.setChanged();
        }
    }

    /** O {@code startCraft}: a melhoria pedida, se cabe no próximo posto e o jogador tem a experiência. */
    public boolean startCraft(int id, Player player) {
        ItemStack stack = this.items.get(0);
        if (this.size > 0 || !(stack.getItem() instanceof FocusItem focus)) return false;
        this.rank = nextRank(stack);
        int xp = this.rank * XP_MULT;
        if (player.experienceLevel < xp) return false;
        List<FocusUpgradeTable.Type> possible = focus.possibleByRank(stack, this.rank);
        FocusUpgradeTable.Type type = net.thaumcraft.api.FocusUpgrades.byId((short) id);
        if (type == null || !possible.contains(type) || !focus.canApply(stack, player, type, this.rank)) return false;
        this.aspects = costOf(type, this.rank);
        this.size = this.aspects.visSize();
        this.upgrade = id;
        if (!player.getAbilities().instabuild) player.giveExperienceLevels(-xp);
        this.setChanged();
        this.level.playSound(null, this.worldPosition, TCSounds.CRAFT_START.value(), SoundSource.BLOCKS, 0.25f, 1.0f);
        return true;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        if (this.level != null && this.level.isClientSide()) this.reset = true;
        else this.aspects = new AspectList();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof FocusItem;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.thaumcraft.focal_manipulator");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new FocalManipulatorMenu(id, inventory, this);
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
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ItemStack before = this.items.get(0);
        this.items = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.aspects = input.read("Aspects", AspectList.CODEC).orElseGet(AspectList::new);
        this.size = input.getIntOr("size", 0);
        this.upgrade = input.getIntOr("upgrade", -1);
        this.rank = input.getIntOr("rank", -1);
        if (this.level != null && this.level.isClientSide() && !ItemStack.matches(before, this.items.get(0))) this.reset = true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.store("Aspects", AspectList.CODEC, this.aspects);
        output.putInt("size", this.size);
        output.putInt("upgrade", this.upgrade);
        output.putInt("rank", this.rank);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /** As estrelinhas de cima da mesa enquanto trabalha. */
    public static java.util.function.BiConsumer<Level, BlockPos> clientEffects = (level, pos) -> {
    };
}
