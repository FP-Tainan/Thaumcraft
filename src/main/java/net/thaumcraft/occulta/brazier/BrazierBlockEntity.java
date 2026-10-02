package net.thaumcraft.occulta.brazier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.PowerSources;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A alma do Braseiro: a {@code TileEntityBrazier} do Witchery.
 *
 * <p>Três casas para o que se queima e uma quarta que guarda a <b>cinza</b> — e é essa quarta que diz se ele está
 * <b>aceso</b>: sem ela ele é um cesto de ferro com coisas dentro; com ela, arde.
 *
 * <p>Acende-se com <b>isqueiro</b> ou com <b>redstone</b>, e se apaga com um <b>balde de água</b> ou um frasco
 * vazio — que devolvem o que estava dentro. Quebrado <b>aceso</b>, larga cinza e mais nada.
 */
public class BrazierBlockEntity extends BlockEntity implements WorldlyContainer {
    /** As três casas do que se queima, e a quarta, que é a cinza. */
    public static final int SLOTS = 3;
    public static final int ASH = 3;
    public static final int SIZE = 4;

    /** O poder que ele come por batida, quando a receita pede poder. */
    public static final float POWER_PER_TICK = 1.0f;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int burnTime;
    private int powerLevel;
    private long ticks;
    private boolean lastRedstone;

    public BrazierBlockEntity(BlockPos pos, BlockState state) {
        super(OccultaBlocks.BRAZIER_ENTITY, pos, state);
    }

    /** Se o braseiro está aceso: as quatro casas cheias, como no original. */
    public boolean burning() {
        for (int i = 0; i < SIZE; i++) {
            if (this.getItem(i).isEmpty()) return false;
        }
        return true;
    }

    public boolean empty() {
        for (int i = 0; i < SLOTS; i++) {
            if (!this.getItem(i).isEmpty()) return false;
        }
        return true;
    }

    public int burnTime() {
        return this.burnTime;
    }

    public boolean powered() {
        return this.powerLevel > 0;
    }

    public boolean lastRedstone() {
        return this.lastRedstone;
    }

    public void setLastRedstone(boolean ligado) {
        this.lastRedstone = ligado;
    }

    /** O que está posto nele, pela ordem das casas. */
    public List<ItemStack> inside() {
        return List.of(this.getItem(0), this.getItem(1), this.getItem(2));
    }

    /** O {@code begin}: põe-se a cinza, e ele acende. */
    public void light() {
        if (this.empty() || this.burning()) return;
        this.items.set(ASH, new ItemStack(OccultaItems.WOOD_ASH));
        this.sync();
    }

    /** O {@code reset}: apaga-se, e o que estava dentro volta. */
    public void douse() {
        this.burnTime = 0;
        this.items.set(ASH, ItemStack.EMPTY);
        this.sync();
    }

    /** Põe mais uma coisa a queimar; devolve se coube. */
    public boolean add(ItemStack coisa) {
        for (int i = 0; i < SLOTS; i++) {
            if (!this.getItem(i).isEmpty()) continue;
            this.items.set(i, coisa.copyWithCount(1));
            this.sync();
            return true;
        }
        return false;
    }

    /** A receita que ele está queimando, ou nada. */
    public BrazierRecipes.@Nullable Recipe recipe() {
        return BrazierRecipes.find(this.inside());
    }

    public void tick() {
        if (!(this.level instanceof ServerLevel level)) return;
        this.ticks++;
        BrazierRecipes.Recipe receita = this.recipe();

        if (receita == null || !this.burning()) {
            // acendeu-se sem receita: a cinza se apaga sozinha, com um chiado
            if (!this.getItem(ASH).isEmpty() && receita == null) {
                this.douse();
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                        this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0,
                        this.worldPosition.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0.0);
            }
            if (this.ticks % 40L == 0L) {
                this.powerLevel = PowerSources.closest(level, this.worldPosition) != null ? 1 : 0;
            }
            this.burnTime = 0;
            return;
        }

        if (receita.power() && !PowerSources.consume(level, this.worldPosition, POWER_PER_TICK)) {
            this.powerLevel = 0;
            return;
        }
        this.powerLevel = 1;
        this.burnTime++;
        if (this.burnTime >= receita.burn()) {
            this.burnTime = 0;
            for (int i = 0; i < SLOTS; i++) this.items.set(i, ItemStack.EMPTY);
            this.sync();
            return;
        }
        receita.burning().onBurning(level, this.worldPosition, this.ticks);
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
        this.sync();
    }

    @Override
    public boolean stillValid(Player quem) {
        return net.minecraft.world.Container.stillValidBlockEntity(this, quem);
    }

    @Override
    public boolean canPlaceItem(int casa, ItemStack coisa) {
        return casa < SLOTS && !this.burning();
    }

    @Override
    public int[] getSlotsForFace(Direction lado) {
        return new int[]{0, 1, 2};
    }

    @Override
    public boolean canPlaceItemThroughFace(int casa, ItemStack coisa, @Nullable Direction lado) {
        return this.canPlaceItem(casa, coisa);
    }

    @Override
    public boolean canTakeItemThroughFace(int casa, ItemStack coisa, Direction lado) {
        return false;
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    // ------------------------------------------------------------------ guardar

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
        this.burnTime = input.getIntOr("BurnTime", 0);
        this.lastRedstone = input.getBooleanOr("Redstone", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("BurnTime", this.burnTime);
        output.putBoolean("Redstone", this.lastRedstone);
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
