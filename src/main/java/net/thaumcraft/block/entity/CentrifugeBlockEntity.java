package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
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
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * A centrífuga alquímica: o {@code TileCentrifuge} da 4.2.3.5, descompilado.
 *
 * <p>Puxa por baixo um ponto de essência composta, gira dois segundos e solta por cima um dos dois aspectos de
 * que ela é feita, sorteado. Primários não entram. Com sinal de redstone, para.
 */
public class CentrifugeBlockEntity extends BlockEntity implements AspectContainer, EssentiaTransport {
    @Nullable
    private Aspect aspectIn;
    @Nullable
    private Aspect aspectOut;
    private int count;
    private int process;
    /** Só de quem vê: a velocidade e o ângulo da parte que gira. */
    public float rotationSpeed;
    public float rotation;

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.CENTRIFUGE, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CentrifugeBlockEntity centrifuge) {
        boolean powered = level.hasNeighborSignal(pos);
        if (!level.isClientSide()) {
            if (powered) return;
            if (centrifuge.aspectOut == null && centrifuge.aspectIn == null && ++centrifuge.count % 5 == 0) {
                centrifuge.drawEssentia(level, pos);
            }
            if (centrifuge.process > 0) centrifuge.process--;
            if (centrifuge.aspectOut == null && centrifuge.aspectIn != null && centrifuge.process == 0) {
                Aspect[] parts = centrifuge.aspectIn.components();
                centrifuge.aspectOut = parts[level.getRandom().nextInt(2)];
                centrifuge.aspectIn = null;
                centrifuge.sync();
            }
            return;
        }
        // quem vê: acelera enquanto trabalha, desacelera parada, e a bomba estala a cada meia volta
        if (centrifuge.aspectIn != null && !powered && centrifuge.rotationSpeed < 20.0f) centrifuge.rotationSpeed += 2.0f;
        if ((centrifuge.aspectIn == null || powered) && centrifuge.rotationSpeed > 0.0f) centrifuge.rotationSpeed -= 0.5f;
        int before = (int) centrifuge.rotation;
        centrifuge.rotation += centrifuge.rotationSpeed;
        if (centrifuge.rotation % 180.0f <= 20.0f && before % 180 >= 160 && centrifuge.rotationSpeed > 0.0f) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.PUMP.value(),
                    SoundSource.BLOCKS, 1.0f, 1.0f, false);
        }
    }

    /** O {@code drawEssentia}: um ponto composto de quem está embaixo, se ela puxar mais forte que ele. */
    private void drawEssentia(Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos.below()) instanceof EssentiaTransport below)) return;
        if (!below.isConnectable(Direction.UP) || !below.canOutputTo(Direction.UP)) return;
        Aspect wanted = null;
        if (below.getEssentiaAmount(Direction.UP) > 0
                && below.getSuctionAmount(Direction.UP) < this.getSuctionAmount(Direction.DOWN)
                && this.getSuctionAmount(Direction.DOWN) >= below.getMinimumSuction()) {
            wanted = below.getEssentiaType(Direction.UP);
        }
        if (wanted != null && !wanted.isPrimal()
                && below.getSuctionAmount(Direction.UP) < this.getSuctionAmount(Direction.DOWN)
                && below.takeEssentia(wanted, 1, Direction.UP) == 1) {
            this.aspectIn = wanted;
            this.process = 39;
            this.sync();
        }
    }

    @Nullable
    public Aspect aspectIn() {
        return this.aspectIn;
    }

    @Nullable
    public Aspect aspectOut() {
        return this.aspectOut;
    }

    // ---- recipiente

    @Override
    public AspectList getAspects() {
        AspectList list = new AspectList();
        if (this.aspectOut != null) list.add(this.aspectOut, 1);
        return list;
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return true;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        if (amount > 0 && this.aspectOut == null) {
            this.aspectOut = aspect;
            this.sync();
            amount--;
        }
        return amount;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        if (this.aspectOut == null || aspect != this.aspectOut) return false;
        this.aspectOut = null;
        this.sync();
        return true;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return amount == 1 && aspect == this.aspectOut;
    }

    @Override
    public int containerContains(@Nullable Aspect aspect) {
        return aspect != null && aspect == this.aspectOut ? 1 : 0;
    }

    // ---- encanamento: entra por baixo, sai por cima

    @Override
    public boolean isConnectable(Direction face) {
        return face == Direction.UP || face == Direction.DOWN;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == Direction.UP;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    @Nullable
    public Aspect getSuctionType(@Nullable Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        if (face != Direction.DOWN) return 0;
        if (this.level != null && this.level.hasNeighborSignal(this.getBlockPos())) return 0;
        return this.aspectIn == null ? 128 : 64;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return this.canOutputTo(face) && this.takeFromContainer(aspect, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        if (this.aspectIn != null || aspect.isPrimal()) return 0;
        this.aspectIn = aspect;
        this.process = 39;
        this.sync();
        return 1;
    }

    @Override
    @Nullable
    public Aspect getEssentiaType(@Nullable Direction face) {
        return this.aspectOut;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return this.aspectOut != null ? 1 : 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    // ---- guardar

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.aspectIn = Aspect.of(input.getStringOr("aspect_in", ""));
        this.aspectOut = Aspect.of(input.getStringOr("aspect_out", ""));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.aspectIn != null) output.putString("aspect_in", this.aspectIn.tag());
        if (this.aspectOut != null) output.putString("aspect_out", this.aspectOut.tag());
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
