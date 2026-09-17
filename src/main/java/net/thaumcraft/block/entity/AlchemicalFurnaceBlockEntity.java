package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectContainer;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.inventory.AlchemicalFurnaceMenu;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O forno alquímico: onde a coisa deixa de ser coisa e vira essência.
 *
 * <p>É o outro lado do crisol. O crisol desfaz o que se joga nele na água, e ali a essência fica solta e
 * se perde; o forno desfaz do mesmo jeito, mas guarda o que sai e empurra para os alambiques empilhados
 * em cima. É daqui que a essência encanada começa.
 *
 * <p>As contas são as do {@code TileAlchemyFurnace} da 4.2.3.5: cinquenta de essência guardada no forno,
 * dez tiques de fogo por ponto de aspecto que a coisa tem, e um empurrão para os alambiques a cada
 * quarenta tiques. O empurrão tem duas passadas, também como no original: primeiro completa os alambiques
 * que já têm um aspecto começado, depois dá um aspecto novo para os que estiverem vazios — que é o que
 * faz uma pilha de alambiques separar a essência em vez de todos brigarem pela mesma.
 */
public class AlchemicalFurnaceBlockEntity extends BlockEntity implements Container, MenuProvider, AspectContainer {
    /** A casa em que entra o que vai virar essência. */
    public static final int INPUT_SLOT = 0;
    /** A casa do combustível. */
    public static final int FUEL_SLOT = 1;
    public static final int SIZE = 2;

    /** Quanto de essência o forno segura antes de engasgar, como no original. */
    public static final int MAX_VIS = 50;
    /** Quantos tiques de fogo cada ponto de aspecto custa. */
    private static final int TICKS_PER_POINT = 10;
    /** De quantos em quantos tiques ele empurra para os alambiques. */
    private static final int PUSH_EVERY = 40;
    /** Até quantos alambiques empilhados ele alcança. */
    private static final int STACK_REACH = 4;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private AspectList aspects = new AspectList();
    private int burnTime;
    private int burnTimeTotal;
    private int cookTime;
    private int smeltTime = 100;
    private int count;

    public AlchemicalFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ALCHEMICAL_FURNACE, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AlchemicalFurnaceBlockEntity furnace) {
        if (level.isClientSide()) return;

        boolean wasBurning = furnace.burnTime > 0;
        boolean dirty = false;
        furnace.count++;

        if (furnace.burnTime > 0) furnace.burnTime--;

        furnace.pushToAlembics(level, pos);

        if (furnace.burnTime == 0 && furnace.canSmelt()) {
            ItemStack fuel = furnace.items.get(FUEL_SLOT);
            int burn = burnTimeOf(level, fuel);
            if (burn > 0) {
                furnace.burnTime = burn;
                furnace.burnTimeTotal = burn;
                dirty = true;
                fuel.shrink(1);
            }
        }

        if (furnace.burnTime > 0 && furnace.canSmelt()) {
            furnace.cookTime++;
            if (furnace.cookTime >= furnace.smeltTime) {
                furnace.cookTime = 0;
                furnace.smeltItem();
                dirty = true;
            }
        } else {
            furnace.cookTime = 0;
        }

        if (wasBurning != furnace.burnTime > 0) {
            dirty = true;
            level.setBlock(pos, state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT,
                    furnace.burnTime > 0), 3);
        }
        if (dirty) furnace.sync();
    }

    /**
     * O empurrão para os alambiques, nas duas passadas do original.
     *
     * <p>A primeira completa quem já começou um aspecto — assim um alambique que está guardando terra não
     * perde a vez. A segunda dá um aspecto qualquer do forno a quem estiver vazio, pulando os que a
     * primeira passada já atendeu, que é como o original espalha os aspectos pela pilha em vez de amontoar
     * tudo no de baixo.
     */
    private void pushToAlembics(Level level, BlockPos pos) {
        if (this.aspects.isEmpty() || this.count % PUSH_EVERY != 0) return;

        AspectList served = new AspectList();
        for (int up = 1; up <= STACK_REACH; up++) {
            if (!(level.getBlockEntity(pos.above(up)) instanceof AlembicBlockEntity alembic)) break;
            Aspect has = alembic.aspect();
            if (has == null || alembic.amount() >= AlembicBlockEntity.CAPACITY) continue;
            if (this.aspects.getAmount(has) <= 0) continue;
            if (this.takeFromContainer(has, 1) && alembic.addToContainer(has, 1) == 0) {
                served.add(has, 1);
            }
        }

        for (int up = 1; up <= STACK_REACH; up++) {
            if (!(level.getBlockEntity(pos.above(up)) instanceof AlembicBlockEntity alembic)) break;
            if (alembic.aspect() != null && alembic.amount() != 0) continue;

            Aspect wanted;
            if (alembic.label() != null) {
                wanted = alembic.label();
                if (!this.takeFromContainer(wanted, 1)) continue;
            } else {
                wanted = this.takeAnyAspect(level, served);
            }
            if (wanted == null) continue;
            if (alembic.addToContainer(wanted, 1) == 0) break;
        }
    }

    /** Tira um aspecto qualquer do forno, sorteado, pulando os que já foram servidos nesta rodada. */
    @Nullable
    private Aspect takeAnyAspect(Level level, AspectList exclude) {
        if (this.aspects.isEmpty()) return null;
        var choices = new java.util.ArrayList<Aspect>();
        for (Aspect aspect : this.aspects.getAspects()) {
            if (exclude.getAmount(aspect) > 0) continue;
            choices.add(aspect);
        }
        if (choices.isEmpty()) return null;
        Aspect picked = choices.get(level.getRandom().nextInt(choices.size()));
        this.aspects.remove(picked, 1);
        this.sync();
        return picked;
    }

    /**
     * Dá para desfazer o que está na casa de entrada?
     *
     * <p>Também é aqui que o tempo de fogo se decide: dez tiques por ponto de aspecto, como no original.
     * E se o que sairia não coubesse no forno, ele nem começa — é isso que faz o forno parar quando os
     * alambiques em cima estão cheios.
     */
    private boolean canSmelt() {
        ItemStack input = this.items.get(INPUT_SLOT);
        if (input.isEmpty()) return false;

        AspectList found = ObjectAspects.of(input);
        if (found.isEmpty()) return false;

        int size = found.visSize();
        if (size > MAX_VIS - this.aspects.visSize()) return false;

        this.smeltTime = Math.max(1, size * TICKS_PER_POINT);
        return true;
    }

    private void smeltItem() {
        ItemStack input = this.items.get(INPUT_SLOT);
        AspectList found = ObjectAspects.of(input);
        if (found.isEmpty()) return;
        for (Aspect aspect : found.getAspects()) this.aspects.add(aspect, found.getAmount(aspect));
        input.shrink(1);
        this.sync();
    }

    private static int burnTimeOf(Level level, ItemStack fuel) {
        if (fuel.isEmpty()) return 0;
        return level.fuelValues().burnDuration(fuel);
    }

    // ---- o que se mostra na tela ----

    public AspectList aspects() {
        return this.aspects;
    }

    public int visSize() {
        return this.aspects.visSize();
    }

    public int burnTime() {
        return this.burnTime;
    }

    public int burnTimeTotal() {
        return this.burnTimeTotal;
    }

    public int cookTime() {
        return this.cookTime;
    }

    public int smeltTime() {
        return this.smeltTime;
    }

    // ---- recipiente de aspecto ----

    @Override
    public AspectList getAspects() {
        return this.aspects.copy();
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return true;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        int room = MAX_VIS - this.aspects.visSize();
        int added = Math.min(amount, Math.max(0, room));
        if (added > 0) {
            this.aspects.add(aspect, added);
            this.sync();
        }
        return amount - added;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        if (aspect == null || this.aspects.getAmount(aspect) < amount) return false;
        this.aspects.remove(aspect, amount);
        this.sync();
        return true;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return this.aspects.getAmount(aspect) >= amount;
    }

    @Override
    public int containerContains(@Nullable Aspect aspect) {
        return aspect == null ? 0 : this.aspects.getAmount(aspect);
    }

    // ---- as duas casas ----

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack taken = ContainerHelper.removeItem(this.items, slot, amount);
        if (!taken.isEmpty()) this.setChanged();
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    // ---- a tela ----

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.thaumcraft.alchemical_furnace");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AlchemicalFurnaceMenu(id, inventory, this);
    }

    /** Os números que a tela precisa ver, na ordem em que o menu os espera. */
    public net.minecraft.world.inventory.ContainerData data() {
        return new net.minecraft.world.inventory.ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case AlchemicalFurnaceMenu.DATA_BURN -> AlchemicalFurnaceBlockEntity.this.burnTime;
                    case AlchemicalFurnaceMenu.DATA_BURN_TOTAL -> AlchemicalFurnaceBlockEntity.this.burnTimeTotal;
                    case AlchemicalFurnaceMenu.DATA_COOK -> AlchemicalFurnaceBlockEntity.this.cookTime;
                    case AlchemicalFurnaceMenu.DATA_SMELT -> AlchemicalFurnaceBlockEntity.this.smeltTime;
                    case 4 -> AlchemicalFurnaceBlockEntity.this.getBlockPos().getX();
                    case 5 -> AlchemicalFurnaceBlockEntity.this.getBlockPos().getY();
                    case 6 -> AlchemicalFurnaceBlockEntity.this.getBlockPos().getZ();
                    case AlchemicalFurnaceMenu.DATA_VIS -> AlchemicalFurnaceBlockEntity.this.visSize();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // só o servidor manda nestes números; do lado de cá eles são lidos e mais nada
            }

            @Override
            public int getCount() {
                return AlchemicalFurnaceMenu.DATA_SIZE;
            }
        };
    }

    // ---- guardar e contar ----

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.aspects = input.read("aspects", AspectList.CODEC).orElseGet(AspectList::new);
        this.burnTime = input.getIntOr("burn", 0);
        this.burnTimeTotal = input.getIntOr("burn_total", 0);
        this.cookTime = input.getIntOr("cook", 0);
        this.smeltTime = input.getIntOr("smelt", 100);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.store("aspects", AspectList.CODEC, this.aspects);
        output.putInt("burn", this.burnTime);
        output.putInt("burn_total", this.burnTimeTotal);
        output.putInt("cook", this.cookTime);
        output.putInt("smelt", this.smeltTime);
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
