package net.thaumcraft.naturalis;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
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
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.entity.DeconstructionTableBlockEntity;
import net.thaumcraft.inventory.TranscribingTableMenu;
import net.thaumcraft.registry.TCBlocks;
import org.jetbrains.annotations.Nullable;

/**
 * A Mesa de Transcrição: o {@code TranscribingTableBlockEntity} do Magia Naturalis 0.5.0.
 *
 * <p>Com um Diário de Pesquisa em cima, de dois em dois segundos ela olha uma das mesas de decomposição postas
 * em cruz a dois blocos dali e leva para o diário o primário que a mesa tinha acabado de tirar. Quando o diário
 * enche — sessenta e quatro de cada um dos seis —, ele passa para a casa de baixo, pronto.
 */
public class TranscribingTableBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    /** De quantos em quantos tiques ela colhe: o {@code timer} do original. */
    public static final int PERIOD = 40;
    /** O que enche o diário: o original quer sessenta e quatro de cada primário. */
    public static final int FULL = 64;
    private static final int[] SIDES = {0};

    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private int timer = PERIOD;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return TranscribingTableBlockEntity.this.timer;
        }

        @Override
        public void set(int index, int value) {
            TranscribingTableBlockEntity.this.timer = value;
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public TranscribingTableBlockEntity(BlockPos pos, BlockState state) {
        super(NaturalisBlocks.TRANSCRIBING_TABLE_ENTITY, pos, state);
    }

    public ContainerData data() {
        return this.data;
    }

    public int timer() {
        return this.timer;
    }

    // ------------------------------------------------------------------ a colheita

    public static void tick(Level level, BlockPos pos, BlockState state, TranscribingTableBlockEntity table) {
        ItemStack log = table.items.get(0);
        if (!(log.getItem() instanceof ResearchLogItem)) return;
        if (--table.timer > 0) return;
        table.timer = PERIOD;
        if (table.harvest(level, pos, log)) table.setChanged();
    }

    /** Uma das mesas em cruz, a dois blocos daqui — a mesma conta do original. */
    public static @Nullable BlockPos pick(Level level, BlockPos pos) {
        int x = level.getRandom().nextInt(2) - level.getRandom().nextInt(2);
        int z = level.getRandom().nextInt(2) - level.getRandom().nextInt(2);
        x += x;
        z += z;
        if (x == 0 && z == 0) return null;
        return pos.offset(x, 0, z);
    }

    private boolean harvest(Level level, BlockPos pos, ItemStack log) {
        BlockPos alvo = pick(level, pos);
        if (alvo == null) return false;
        if (!level.getBlockState(alvo).is(TCBlocks.DECONSTRUCTION_TABLE)) return false;
        if (!(level.getBlockEntity(alvo) instanceof DeconstructionTableBlockEntity mesa)) return false;
        Aspect aspect = mesa.aspect();
        if (aspect == null) return false;
        if (ResearchLogItem.note(log, aspect)) {
            mesa.takeAspect();
            return true;
        }
        // o diário está cheio daquele primário: se estiver cheio de todos, ele desce pronto para a casa de baixo
        if (this.items.get(1).isEmpty() && full(log)) {
            this.items.set(1, log.copy());
            this.items.set(0, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    /** O diário cheio: sessenta e quatro de cada um dos seis primários. */
    public static boolean full(ItemStack log) {
        var notas = ResearchLogItem.notes(log);
        for (Aspect primal : Aspects.primals()) {
            if (notas.getAmount(primal) < FULL) return false;
        }
        return true;
    }

    // ------------------------------------------------------------------ as duas casas

    @Override
    public int getContainerSize() {
        return 2;
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
        return Component.translatable("block.thaumcraft.transcribing_table");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new TranscribingTableMenu(id, inventory, this, this.data);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && stack.getItem() instanceof ResearchLogItem;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? new int[0] : SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return side != Direction.UP && this.canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == 1;
    }

    @Override
    public boolean stillValid(Player player) {
        return net.minecraft.world.Container.stillValidBlockEntity(this, player);
    }

    // ------------------------------------------------------------------ o que fica gravado

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(2, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.timer = input.getIntOr("timer", PERIOD);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("timer", this.timer);
    }
}
