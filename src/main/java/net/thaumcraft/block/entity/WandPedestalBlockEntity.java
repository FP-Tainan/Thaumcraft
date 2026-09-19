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
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
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
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.VisAmuletItem;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCComponents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * O pedestal de recarga: o {@code TileWandPedestal} da 4.2.3.5. Uma varinha (ou um amuleto de vis) posto nele bebe
 * dos nós de aura a até oito blocos, um ponto a cada cinco tiques, dos aspectos em que ainda cabe. Com o foco composto
 * de recarga em cima, também quebra os aspectos compostos do nó nos primários. Nunca esvazia o nó (deixa um), menos
 * com a ponta de ferro ou a haste de madeira.
 */
public class WandPedestalBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    private static final int[] SLOTS = {0};
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private int counter;
    private boolean somethingChanged;
    /** Do lado de quem vê: de onde está bebendo agora, e de que cor. */
    public boolean draining;
    public BlockPos drain = BlockPos.ZERO;
    public int drainColor;
    private @Nullable List<BlockPos> nodes;

    public WandPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.WAND_PEDESTAL, pos, state);
    }

    public ItemStack held() {
        return this.items.get(0);
    }

    public static boolean accepts(ItemStack stack) {
        return stack.getItem() instanceof WandItem || stack.getItem() instanceof VisAmuletItem;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WandPedestalBlockEntity ped) {
        if (ped.nodes == null) ped.findNodes();
        ped.counter++;
        boolean recalc = false;
        ItemStack held = ped.held();
        if (ped.counter % 20 == 0 && ped.somethingChanged && !ped.nodes.isEmpty() && !held.isEmpty()) {
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
            ped.somethingChanged = false;
        }
        if (ped.counter % 5 == 0 && !ped.nodes.isEmpty() && !held.isEmpty()) {
            boolean hasThingy = level.getBlockState(pos.above()).is(TCBlocks.RECHARGE_FOCUS);
            int min = 1;
            if (held.getItem() instanceof WandItem && ("iron".equals(WandItem.capTag(held)) || "wood".equals(WandItem.rodTag(held)))) min = 0;
            List<Aspect> room = room(held);
            ped.draining = false;
            if (!room.isEmpty()) {
                search:
                for (BlockPos co : ped.nodes) {
                    if (!(level.getBlockEntity(co) instanceof NodeBlockEntity node)) continue;
                    for (Aspect aspect : room) {
                        if (node.aspects().getAmount(aspect) > min) {
                            ped.drink(level, node, co, aspect, aspect);
                            break search;
                        }
                    }
                    if (hasThingy) {
                        for (Aspect aspect : node.aspects().getAspects()) {
                            if (aspect == null || aspect.isPrimal()) continue;
                            AspectList primals = DeconstructionTableBlockEntity.reduceToPrimals(new AspectList().add(aspect, 1));
                            for (Aspect aspect2 : room) {
                                if (primals.getAmount(aspect2) > 0 && node.aspects().getAmount(aspect) > min) {
                                    ped.drink(level, node, co, aspect, aspect2);
                                    break search;
                                }
                            }
                        }
                    }
                }
                if (!ped.draining) recalc = true;
            }
        }
        if (ped.counter % 100 == 0 && (recalc || ped.nodes.isEmpty())) ped.findNodes();
    }

    /** Um ponto de {@code from} do nó vira um ponto de {@code to} na varinha (ou no amuleto). */
    private void drink(Level level, NodeBlockEntity node, BlockPos co, Aspect from, Aspect to) {
        this.draining = true;
        this.drain = co;
        this.drainColor = from.color();
        if (level.isClientSide()) return;
        ItemStack held = this.held();
        if (held.getItem() instanceof WandItem) {
            WandItem.addVis(held, to, 1);
        } else if (held.getItem() instanceof VisAmuletItem amulet) {
            AspectList vis = VisAmuletItem.vis(held);
            vis.add(to, Math.min(WandItem.VIS_UNIT, amulet.maxVis(held) - vis.getAmount(to)));
            held.set(TCComponents.WAND_VIS, vis);
        }
        node.take(from, 1);
        this.somethingChanged = true;
        this.setChanged();
    }

    /** Os primários em que ainda cabe vis. */
    private static List<Aspect> room(ItemStack held) {
        if (held.getItem() instanceof WandItem) return WandItem.aspectsWithRoom(held);
        List<Aspect> out = new ArrayList<>();
        if (held.getItem() instanceof VisAmuletItem amulet) {
            AspectList vis = VisAmuletItem.vis(held);
            for (Aspect aspect : Aspects.primals()) if (vis.getAmount(aspect) < amulet.maxVis(held)) out.add(aspect);
        }
        return out;
    }

    /** O {@code findNodes}: os nós num cubo de oito blocos para cada lado. */
    private void findNodes() {
        this.nodes = new ArrayList<>();
        if (this.level == null) return;
        for (int xx = -8; xx <= 8; xx++) for (int yy = -8; yy <= 8; yy++) for (int zz = -8; zz <= 8; zz++) {
            BlockPos at = this.worldPosition.offset(xx, yy, zz);
            if (this.level.getBlockEntity(at) instanceof NodeBlockEntity && !(this.level.getBlockEntity(at) instanceof NodeJarBlockEntity)) this.nodes.add(at);
        }
    }

    /** O {@code getComparatorInputOverride}: de 1 a 15, pelo quanto a varinha está cheia. */
    public int comparator() {
        ItemStack held = this.held();
        if (!(held.getItem() instanceof WandItem)) return 0;
        float r = WandItem.vis(held).visSize() / (WandItem.maxVis(held) * 6.0f);
        return (int) Math.floor(r * 14.0f) + 1;
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
        return Component.translatable("container.wandpedestal");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return null;
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
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return accepts(stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return this.held().isEmpty() && accepts(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return net.minecraft.world.Container.stillValidBlockEntity(this, player);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(1, ItemStack.EMPTY);
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
