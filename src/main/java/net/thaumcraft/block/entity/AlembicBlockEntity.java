package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectContainer;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O alambique arcano: o funil que recolhe o que o forno alquímico solta.
 *
 * <p>Os números são os do {@code TileAlembic} da 4.2.3.5: cabem trinta e dois de um aspecto só. O
 * alambique <strong>só deixa sair</strong> — quem enche ele é o forno logo abaixo, empurrando, e não ele
 * que puxa; por isso a sucção dele é zero. É a diferença entre ele e o jarro, e é o que faz uma pilha de
 * alambiques sobre um forno funcionar: cada um pega um aspecto diferente.
 *
 * <p>Com um rótulo ele passa a pedir um aspecto certo ao forno, em vez de aceitar o que vier.
 */
public class AlembicBlockEntity extends BlockEntity implements AspectContainer, EssentiaTransport {
    /** O que cabe num alambique, como no original. */
    public static final int CAPACITY = 32;

    @Nullable
    private Aspect aspect;
    @Nullable
    private Aspect label;
    private int amount;

    public AlembicBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ALEMBIC, pos, state);
    }

    @Nullable
    public Aspect aspect() {
        return this.aspect;
    }

    public int amount() {
        return this.amount;
    }

    @Nullable
    public Aspect label() {
        return this.label;
    }

    public boolean setLabel(@Nullable Aspect wanted) {
        if (wanted != null && this.aspect != null && this.amount > 0 && this.aspect != wanted) return false;
        this.label = wanted;
        this.sync();
        return true;
    }

    public float fullness() {
        return this.amount / (float) CAPACITY;
    }

    // ---- recipiente ----

    @Override
    public AspectList getAspects() {
        AspectList list = new AspectList();
        if (this.aspect != null && this.amount > 0) list.add(this.aspect, this.amount);
        return list;
    }

    @Override
    public boolean doesContainerAccept(Aspect wanted) {
        return this.label == null || this.label == wanted;
    }

    @Override
    public int addToContainer(Aspect wanted, int requested) {
        if (requested == 0) return 0;
        if (!this.doesContainerAccept(wanted)) return requested;
        if ((this.amount < CAPACITY && wanted == this.aspect) || this.amount == 0) {
            this.aspect = wanted;
            int added = Math.min(requested, CAPACITY - this.amount);
            this.amount += added;
            requested -= added;
            this.sync();
        }
        return requested;
    }

    @Override
    public boolean takeFromContainer(Aspect wanted, int requested) {
        if (wanted == null || wanted != this.aspect || this.amount < requested) return false;
        this.amount -= requested;
        if (this.amount <= 0) {
            this.amount = 0;
            this.aspect = null;
        }
        this.sync();
        return true;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect wanted, int wantedAmount) {
        return wanted == this.aspect && this.amount >= wantedAmount;
    }

    @Override
    public int containerContains(@Nullable Aspect wanted) {
        return wanted != null && wanted == this.aspect ? this.amount : 0;
    }

    // ---- encanamento ----

    @Override
    public boolean isConnectable(Direction face) {
        return face != Direction.DOWN;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        // o alambique não aceita nada pelo cano: quem o enche é o forno, de baixo, empurrando
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face != Direction.DOWN;
    }

    @Override
    public void setSuction(@Nullable Aspect wanted, int strength) {
    }

    @Override
    @Nullable
    public Aspect getSuctionType(@Nullable Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect wanted, int requested, Direction face) {
        return this.canOutputTo(face) && this.takeFromContainer(wanted, requested) ? requested : 0;
    }

    @Override
    public int addEssentia(Aspect wanted, int requested, Direction face) {
        return 0;
    }

    @Override
    @Nullable
    public Aspect getEssentiaType(@Nullable Direction face) {
        return this.aspect;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return this.amount;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return true;
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
        this.aspect = Aspect.of(input.getStringOr("aspect", ""));
        this.label = Aspect.of(input.getStringOr("label", ""));
        this.amount = input.getIntOr("amount", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.aspect != null) output.putString("aspect", this.aspect.tag());
        if (this.label != null) output.putString("label", this.label.tag());
        output.putInt("amount", this.amount);
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
