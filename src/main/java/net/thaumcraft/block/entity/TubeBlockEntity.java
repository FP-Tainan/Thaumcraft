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
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * O tubo de essência: cabe uma unidade de cada vez, e é ele que faz a fila andar.
 *
 * <p>Este é o coração da fatia, e o mais delicado dela — é a tradução linha a linha do {@code TileTube}
 * da 4.2.3.5. Duas contas se repetem sem parar:
 *
 * <ul>
 *   <li><strong>a conta da sucção</strong>, a cada dois tiques: o tubo olha os vizinhos, acha o que puxa
 *       mais forte e passa a puxar com um a menos do que ele. É assim que a fome do jarro lá no fim da
 *       linha viaja tubo a tubo até a fonte, perdendo um de força a cada peça — e por isso a tubulação
 *       tem alcance, em vez de ser infinita.</li>
 *   <li><strong>a igualada com os vizinhos</strong>, a cada cinco tiques: se o tubo está vazio, ele tira
 *       uma unidade do vizinho que puxa menos do que ele. Uma unidade, nunca mais.</li>
 * </ul>
 *
 * <p>Quando dois lados puxam com a mesma força mas querem aspectos diferentes, o tubo <strong>vaza</strong>
 * por quarenta tiques, soltando fumaça na cor do aspecto e parando tudo — é o jeito do original de avisar
 * que a tubulação foi mal pensada, e ele foi mantido.
 */
public class TubeBlockEntity extends BlockEntity implements EssentiaTransport {
    /** Quanto tempo o tubo fica vazando depois de brigar consigo mesmo. */
    private static final int VENT_TIME = 40;

    private final boolean[] open = {true, true, true, true, true, true};
    @Nullable
    private Aspect essentia;
    private int amount;
    @Nullable
    private Aspect suctionType;
    private int suction;
    private int venting;
    private int count;

    public TubeBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.TUBE, pos, state);
    }

    public TubeBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,
            BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TubeBlockEntity tube) {
        if (tube.venting > 0) tube.venting--;
        if (level.isClientSide()) {
            if (tube.venting > 0) tube.puff(level, pos);
            return;
        }
        if (tube.venting > 0) return;

        if (++tube.count % 2 == 0) {
            tube.reconnect(level, pos, state);
            tube.calculateSuction(null, false, false);
            tube.checkVenting(level, pos);
            if (tube.essentia != null && tube.amount == 0) tube.essentia = null;
        }
        if (tube.count % 5 == 0 && tube.suction > 0) {
            tube.equalizeWithNeighbours(level, pos, false);
        }
    }

    /**
     * Refaz as chaves de estado do bloco, se o que há em volta tiver mudado.
     *
     * <p>Quem põe o tubo já acerta as chaves na hora, e um vizinho novo também as acerta. Mas há um caso
     * em que isso não basta: quando o vizinho é um bloco com miolo — um jarro, um alambique —, o miolo
     * dele ainda não existe no instante em que o aviso de vizinho chega, e o tubo conclui que ali não há
     * nada de encanar. Por isso ele confere de novo de dois em dois tiques, e só escreve quando muda.
     */
    private void reconnect(Level level, BlockPos pos, BlockState state) {
        BlockState wanted = net.thaumcraft.block.TubeBlock.connect(state, level, pos);
        if (wanted != state) level.setBlock(pos, wanted, 3);
    }

    /**
     * A conta da sucção.
     *
     * @param filter só aceita puxar este aspecto (o tubo com filtro usa isto)
     * @param restrict perde metade da força em vez de um (o tubo estreito usa isto)
     * @param directional só olha para o lado de trás (o tubo de mão única usa isto)
     */
    protected void calculateSuction(@Nullable Aspect filter, boolean restrict, boolean directional) {
        this.suction = 0;
        this.suctionType = null;
        if (this.level == null) return;

        for (Direction dir : Direction.values()) {
            if (directional && this.facing() != dir.getOpposite()) continue;
            if (!this.isConnectable(dir)) continue;
            EssentiaTransport neighbour = neighbour(this.level, this.getBlockPos(), dir);
            if (neighbour == null) continue;

            Direction remote = dir.getOpposite();
            Aspect remoteSuction = neighbour.getSuctionType(remote);
            // não se puxa por um lado o que ele não quer
            if (filter != null && remoteSuction != null && remoteSuction != filter) continue;
            if (this.amount > 0 && remoteSuction != null && this.essentia != remoteSuction) continue;

            int strength = neighbour.getSuctionAmount(remote);
            if (strength <= 0 || strength <= this.suction + 1) continue;
            this.setSuction(remoteSuction == null ? filter : remoteSuction,
                    restrict ? strength / 2 : strength - 1);
        }
    }

    /** Dois lados puxando igual e querendo coisas diferentes: o tubo desiste e vaza. */
    protected void checkVenting(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (!this.isConnectable(dir)) continue;
            EssentiaTransport neighbour = neighbour(level, pos, dir);
            if (neighbour == null) continue;

            int theirs = neighbour.getSuctionAmount(dir.getOpposite());
            if (this.suction <= 0) continue;
            if (theirs != this.suction && theirs != this.suction - 1) continue;
            if (this.suctionType == neighbour.getSuctionType(dir.getOpposite())) continue;

            this.venting = VENT_TIME;
            level.playSound(null, pos, TCSounds.SPILL.value(), SoundSource.BLOCKS, 0.4f, 1.0f);
            this.sync();
            return;
        }
    }

    /** O gole do vizinho: uma unidade, do que puxa menos para o que puxa mais. */
    protected void equalizeWithNeighbours(Level level, BlockPos pos, boolean directional) {
        if (this.amount > 0) return;

        for (Direction dir : Direction.values()) {
            if (directional && this.facing() == dir.getOpposite()) continue;
            if (!this.isConnectable(dir)) continue;
            EssentiaTransport neighbour = neighbour(level, pos, dir);
            if (neighbour == null) continue;

            Direction remote = dir.getOpposite();
            if (!neighbour.canOutputTo(remote)) continue;

            Aspect wanted = this.getSuctionType(null);
            Aspect theirs = neighbour.getEssentiaType(remote);
            if (wanted != null && theirs != null && wanted != theirs) continue;
            if (this.getSuctionAmount(null) <= neighbour.getSuctionAmount(remote)) continue;
            if (this.getSuctionAmount(null) < neighbour.getMinimumSuction()) continue;
            if (wanted == null) wanted = theirs != null ? theirs : neighbour.getEssentiaType(null);
            if (wanted == null) continue;

            int taken = neighbour.takeEssentia(wanted, 1, remote);
            if (taken > 0 && this.addEssentia(wanted, taken, dir) > 0) {
                if (level.getRandom().nextInt(100) == 0) {
                    level.playSound(null, pos, TCSounds.BUBBLE.value(), SoundSource.BLOCKS, 0.2f, 1.6f);
                }
                return;
            }
        }
    }

    /** A fumaça de quem está vazando, na cor do aspecto que ele queria. */
    private void puff(Level level, BlockPos pos) {
        int colour = this.suctionType != null ? this.suctionType.color() : 0x888888;
        var random = level.getRandom();
        level.addParticle(new net.minecraft.core.particles.DustParticleOptions(0xFF000000 | colour, 1.0f),
                pos.getX() + 0.5 + (random.nextDouble() - random.nextDouble()) * 0.3,
                pos.getY() + 0.5 + (random.nextDouble() - random.nextDouble()) * 0.3,
                pos.getZ() + 0.5 + (random.nextDouble() - random.nextDouble()) * 0.3,
                (random.nextDouble() - random.nextDouble()) * 0.1,
                random.nextDouble() * 0.1,
                (random.nextDouble() - random.nextDouble()) * 0.1);
    }

    /** O vizinho daquele lado, se for coisa de encanar e se aceitar este lado. */
    @Nullable
    protected static EssentiaTransport neighbour(Level level, BlockPos pos, Direction dir) {
        if (!(level.getBlockEntity(pos.relative(dir)) instanceof EssentiaTransport transport)) return null;
        return transport.isConnectable(dir.getOpposite()) ? transport : null;
    }

    /** Para onde o tubo aponta — só os tubos com lado certo usam isto. */
    protected Direction facing() {
        return Direction.NORTH;
    }

    /** Este lado do tubo está aberto? */
    public boolean isOpen(Direction face) {
        return this.open[face.get3DDataValue()];
    }

    /** Abre ou fecha um lado — é o que a varinha faz ao bater no tubo. */
    public void toggle(Direction face) {
        this.open[face.get3DDataValue()] = !this.open[face.get3DDataValue()];
        this.sync();
    }

    public int venting() {
        return this.venting;
    }

    // ---- encanamento ----

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
    public void setSuction(@Nullable Aspect aspect, int strength) {
        this.suctionType = aspect;
        this.suction = strength;
    }

    @Override
    @Nullable
    public Aspect getSuctionType(@Nullable Direction face) {
        return this.suctionType;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return this.suction;
    }

    @Override
    @Nullable
    public Aspect getEssentiaType(@Nullable Direction face) {
        return this.essentia;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return this.amount;
    }

    @Override
    public int takeEssentia(Aspect wanted, int requested, Direction face) {
        if (!this.canOutputTo(face) || this.essentia != wanted || this.amount <= 0 || requested <= 0) {
            return 0;
        }
        // o tubo entrega de uma em uma, sempre, como no original
        this.amount--;
        if (this.amount <= 0) this.essentia = null;
        this.sync();
        return 1;
    }

    @Override
    public int addEssentia(Aspect wanted, int requested, Direction face) {
        if (!this.canInputFrom(face) || this.amount != 0 || requested <= 0) return 0;
        this.essentia = wanted;
        this.amount = 1;
        this.sync();
        return 1;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    // ---- guardar e contar ----

    protected void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.essentia = Aspect.of(input.getStringOr("type", ""));
        this.amount = input.getIntOr("amount", 0);
        this.suctionType = Aspect.of(input.getStringOr("stype", ""));
        this.suction = input.getIntOr("samount", 0);
        this.venting = input.getIntOr("venting", 0);
        int packed = input.getIntOr("open", 0b111111);
        for (int side = 0; side < 6; side++) this.open[side] = (packed & 1 << side) != 0;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.essentia != null) output.putString("type", this.essentia.tag());
        if (this.suctionType != null) output.putString("stype", this.suctionType.tag());
        output.putInt("amount", this.amount);
        output.putInt("samount", this.suction);
        output.putInt("venting", this.venting);
        int packed = 0;
        for (int side = 0; side < 6; side++) if (this.open[side]) packed |= 1 << side;
        output.putInt("open", packed);
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
