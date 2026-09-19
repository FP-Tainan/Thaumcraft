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
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * O tampão de essência: o {@code TileTubeBuffer} da 4.2.3.5, descompilado.
 *
 * <p>Uma caixinha no meio da tubulação que guarda até oito de essência, de qualquer mistura de aspectos, e
 * puxa com força um de cada lado. Serve de pulmão: segura o que chega e solta para quem puxa mais. A
 * varinha, agachado, estrangula um lado — primeiro ele só puxa com um mesmo com fole por perto, depois não
 * puxa nada —, e de pé abre e fecha o lado, como num tubo.
 */
public class TubeBufferBlockEntity extends BlockEntity implements EssentiaTransport {
    public static final int MAX_AMOUNT = 8;

    private AspectList aspects = new AspectList();
    private final boolean[] open = {true, true, true, true, true, true};
    private final byte[] choked = new byte[6];
    private int count;
    /** Os foles que sopram aqui (o {@code getBellows}), conferidos a cada segundo. */
    private int bellows = -1;

    public TubeBufferBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.TUBE_BUFFER, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TubeBufferBlockEntity buffer) {
        buffer.count++;
        if (buffer.bellows < 0 || buffer.count % 20 == 0) buffer.bellows = net.thaumcraft.block.BellowsBlock.blowingInto(level, pos);
        // como o tubo, confere de dois em dois tiques se o que há em volta mudou
        if (!level.isClientSide() && buffer.count % 2 == 0) {
            BlockState wanted = net.thaumcraft.block.TubeBlock.connect(state, level, pos);
            if (wanted != state) level.setBlock(pos, wanted, 3);
        }
        if (!level.isClientSide() && buffer.count % 5 == 0 && buffer.aspects.visSize() < MAX_AMOUNT) {
            buffer.fillBuffer(level, pos);
        }
    }

    private void fillBuffer(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            EssentiaTransport ic = neighbour(level, pos, dir);
            if (ic == null) continue;
            Direction back = dir.getOpposite();
            if (ic.getEssentiaAmount(back) > 0 && ic.getSuctionAmount(back) < this.getSuctionAmount(dir)
                    && this.getSuctionAmount(dir) >= ic.getMinimumSuction()) {
                Aspect wanted = ic.getEssentiaType(back);
                if (wanted == null) continue;
                this.addToContainer(wanted, ic.takeEssentia(wanted, 1, back));
                return;
            }
        }
    }

    @Nullable
    private EssentiaTransport neighbour(Level level, BlockPos pos, Direction dir) {
        if (!this.isConnectable(dir)) return null;
        if (!(level.getBlockEntity(pos.relative(dir)) instanceof EssentiaTransport transport)) return null;
        return transport.isConnectable(dir.getOpposite()) ? transport : null;
    }

    private int addToContainer(Aspect aspect, int amount) {
        if (amount != 1) return amount;
        if (this.aspects.visSize() >= MAX_AMOUNT) return amount;
        this.aspects.add(aspect, amount);
        this.sync();
        return 0;
    }

    public AspectList aspects() {
        return this.aspects;
    }

    // ----------------------------------------------------------------- a varinha

    public boolean isOpen(Direction face) {
        return this.open[face.get3DDataValue()];
    }

    public void toggle(Direction face) {
        this.open[face.get3DDataValue()] = !this.open[face.get3DDataValue()];
        this.sync();
    }

    public int choke(Direction face) {
        return this.choked[face.get3DDataValue()];
    }

    /** Agachado com a varinha: solto, estrangulado, fechado, e de volta. */
    public void cycleChoke(Direction face) {
        int side = face.get3DDataValue();
        this.choked[side]++;
        if (this.choked[side] > 2) this.choked[side] = 0;
        this.sync();
    }

    // ----------------------------------------------------------------- encanamento

    @Override
    public boolean isConnectable(Direction face) {
        return face != null && this.open[face.get3DDataValue()];
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return this.isConnectable(face);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return this.isConnectable(face);
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    @Nullable
    public Aspect getSuctionType(@Nullable Direction face) {
        return null;
    }

    /** Um; com foles soprando aqui, 32 por fole (menos no lado meio estrangulado); estrangulado de vez, nada. */
    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        int choke = face == null ? 0 : this.choked[face.get3DDataValue()];
        if (choke == 2) return 0;
        return this.bellows > 0 && choke != 1 ? this.bellows * 32 : 1;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        if (!this.canOutputTo(face) || this.level == null) return 0;
        EssentiaTransport asking = neighbour(this.level, this.getBlockPos(), face);
        int suction = asking == null ? 0 : asking.getSuctionAmount(face.getOpposite());
        // quem puxa mais forte por outro lado, e quer isto, tem a vez
        for (Direction dir : Direction.values()) {
            if (dir == face || !this.canOutputTo(dir)) continue;
            EssentiaTransport other = neighbour(this.level, this.getBlockPos(), dir);
            if (other == null) continue;
            int sa = other.getSuctionAmount(dir.getOpposite());
            Aspect su = other.getSuctionType(dir.getOpposite());
            if ((su == aspect || su == null) && suction < sa && this.getSuctionAmount(dir) < sa) return 0;
        }
        amount = Math.min(amount, this.aspects.getAmount(aspect));
        if (amount <= 0 || !this.aspects.reduce(aspect, amount)) return 0;
        this.sync();
        return amount;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return this.canInputFrom(face) ? amount - this.addToContainer(aspect, amount) : 0;
    }

    /** Um dos aspectos guardados, sorteado, como o original. */
    @Override
    @Nullable
    public Aspect getEssentiaType(@Nullable Direction face) {
        List<Aspect> held = this.aspects.getAspects();
        if (held.isEmpty() || this.level == null) return null;
        return held.get(this.level.getRandom().nextInt(held.size()));
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return this.aspects.visSize();
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    // ----------------------------------------------------------------- guardar

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.aspects = input.read("aspects", AspectList.CODEC).orElseGet(AspectList::new);
        int packed = input.getIntOr("open", 0b111111);
        for (int side = 0; side < 6; side++) this.open[side] = (packed & 1 << side) != 0;
        int chokes = input.getIntOr("choke", 0);
        for (int side = 0; side < 6; side++) this.choked[side] = (byte) (chokes >> side * 2 & 3);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("aspects", AspectList.CODEC, this.aspects);
        int packed = 0, chokes = 0;
        for (int side = 0; side < 6; side++) {
            if (this.open[side]) packed |= 1 << side;
            chokes |= (this.choked[side] & 3) << side * 2;
        }
        output.putInt("open", packed);
        output.putInt("choke", chokes);
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
