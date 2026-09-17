package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
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
 * O jarro lacrado: guarda sessenta e quatro de um aspecto só.
 *
 * <p>Os números são os do {@code TileJarFillable} da 4.2.3.5. Ele só se liga pelo alto — a boca do jarro
 * é por cima — e é a fome dele que faz a tubulação inteira andar: enquanto não estiver cheio, ele puxa
 * com força trinta e dois, e sessenta e quatro se tiver rótulo. A cada cinco tiques ele bebe uma unidade
 * do que estiver logo acima.
 *
 * <p>Com rótulo, o jarro passa a só aceitar aquele aspecto — e a puxar com o dobro da força, que é o
 * jeito do original de fazer um jarro rotulado ganhar de um sem rótulo na disputa pela mesma essência.
 */
public class JarBlockEntity extends BlockEntity implements AspectContainer, EssentiaTransport {
    /** O que cabe num jarro, como no original. */
    public static final int CAPACITY = 64;
    /** A força com que um jarro sem rótulo puxa. */
    private static final int SUCTION = 32;
    /** A força com que um jarro com rótulo puxa: o dobro, para ganhar a disputa. */
    private static final int LABELLED_SUCTION = 64;
    /** De quantos em quantos tiques ele bebe do que está acima. */
    private static final int DRINK_EVERY = 5;

    @Nullable
    private Aspect aspect;
    @Nullable
    private Aspect label;
    private int amount;
    private int count;

    public JarBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.JAR, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, JarBlockEntity jar) {
        if (level.isClientSide()) return;
        if (++jar.count % DRINK_EVERY != 0) return;
        if (jar.amount >= CAPACITY) return;
        jar.drinkFromAbove(level, pos);
    }

    /**
     * O gole de cima.
     *
     * <p>É a tradução do {@code fillJar} do original: olha o que está logo acima, confere se aquilo deixa
     * sair para baixo, escolhe o aspecto (o do rótulo, o que já está dentro, ou o que houver lá em cima se
     * a fome daqui for maior) e puxa uma unidade.
     */
    private void drinkFromAbove(Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos.above()) instanceof EssentiaTransport above)) return;
        if (!above.canOutputTo(Direction.DOWN)) return;

        Aspect wanted = null;
        if (this.label != null) {
            wanted = this.label;
        } else if (this.aspect != null && this.amount > 0) {
            wanted = this.aspect;
        } else if (above.getEssentiaAmount(Direction.DOWN) > 0
                && above.getSuctionAmount(Direction.DOWN) < this.getSuctionAmount(Direction.UP)
                && this.getSuctionAmount(Direction.UP) >= above.getMinimumSuction()) {
            wanted = above.getEssentiaType(Direction.DOWN);
        }
        if (wanted == null) return;
        if (above.getSuctionAmount(Direction.DOWN) >= this.getSuctionAmount(Direction.UP)) return;

        int taken = above.takeEssentia(wanted, 1, Direction.DOWN);
        if (taken > 0) this.addToContainer(wanted, taken);
    }

    // ---- o que está dentro ----

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

    /** Põe ou tira o rótulo. Um jarro com coisa dentro não aceita rótulo de outro aspecto. */
    public boolean setLabel(@Nullable Aspect wanted) {
        if (wanted != null && this.aspect != null && this.amount > 0 && this.aspect != wanted) return false;
        this.label = wanted;
        this.sync();
        return true;
    }

    /** O quanto do jarro está cheio, de zero a um — para desenhar. */
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
        if (wanted != this.aspect || this.amount < requested) return false;
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
        return face == Direction.UP;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == Direction.UP;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == Direction.UP;
    }

    @Override
    public void setSuction(@Nullable Aspect wanted, int strength) {
        // o jarro não deixa ninguém mandar na fome dele: ela é dele
    }

    @Override
    @Nullable
    public Aspect getSuctionType(@Nullable Direction face) {
        return this.label != null ? this.label : this.aspect;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        if (this.amount >= CAPACITY) return 0;
        return this.label != null ? LABELLED_SUCTION : SUCTION;
    }

    @Override
    public int takeEssentia(Aspect wanted, int requested, Direction face) {
        return this.canOutputTo(face) && this.takeFromContainer(wanted, requested) ? requested : 0;
    }

    @Override
    public int addEssentia(Aspect wanted, int requested, Direction face) {
        return this.canInputFrom(face) ? requested - this.addToContainer(wanted, requested) : 0;
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
        return this.label != null ? LABELLED_SUCTION : SUCTION;
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
