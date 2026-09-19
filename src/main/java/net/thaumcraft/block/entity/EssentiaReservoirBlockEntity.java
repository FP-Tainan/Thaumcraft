package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectContainer;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.api.wands.Wandable;
import net.thaumcraft.block.EssentiaReservoirBlock;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * O {@code TileEssentiaReservoir} da 4.2.3.5: 256 de essência de qualquer mistura. A cada cinco tiques puxa uma
 * unidade do cano do bocal (com força 24, enquanto houver espaço) e a solta pelo mesmo bocal. Do lado de quem vê, o
 * líquido vai mudando de cor, um aspecto guardado por segundo, e o vidro range de vez em quando, mais quanto mais
 * cheio.
 */
public class EssentiaReservoirBlockEntity extends BlockEntity implements net.thaumcraft.api.aspects.AspectSource, EssentiaTransport, Wandable {
    public AspectList essentia = new AspectList();
    public final int maxAmount = 256;
    private int count;
    private float tr = 1.0f, tri, tg = 1.0f, tgi, tb = 1.0f, tbi;
    public float cr = 1.0f, cg = 1.0f, cb = 1.0f;
    @Nullable
    public Aspect displayAspect;

    public EssentiaReservoirBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ESSENTIA_RESERVOIR, pos, state);
    }

    public Direction facing() {
        BlockState state = this.getBlockState();
        return state.hasProperty(EssentiaReservoirBlock.FACING) ? state.getValue(EssentiaReservoirBlock.FACING) : Direction.DOWN;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EssentiaReservoirBlockEntity te) {
        te.count++;
        if (!level.isClientSide() && te.count % 5 == 0 && te.essentia.visSize() < te.maxAmount) te.fillReservoir();
        if (level.isClientSide()) te.clientTick(level, pos);
    }

    private void clientTick(Level level, BlockPos pos) {
        int vs = this.essentia.visSize();
        if (vs <= 0) return;
        var random = level.getRandom();
        if (random.nextInt(Math.max(1, 500 - vs)) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.CREAK.value(), SoundSource.BLOCKS,
                    1.0f, 1.4f + random.nextFloat() * 0.2f, false);
        }
        if (this.count % 20 == 0 && this.essentia.size() > 0) {
            var aspects = this.essentia.getAspects();
            this.displayAspect = aspects.get(this.count / 20 % aspects.size());
            int c = this.displayAspect.color();
            this.tr = (c >> 16 & 255) / 255.0f;
            this.tg = (c >> 8 & 255) / 255.0f;
            this.tb = (c & 255) / 255.0f;
            this.tri = (this.cr - this.tr) / 20.0f;
            this.tgi = (this.cg - this.tg) / 20.0f;
            this.tbi = (this.cb - this.tb) / 20.0f;
        }
        if (this.displayAspect == null) {
            this.tr = this.tg = this.tb = 1.0f;
            this.tri = this.tgi = this.tbi = 0.0f;
        } else {
            this.cr -= this.tri;
            this.cg -= this.tgi;
            this.cb -= this.tbi;
        }
    }

    /** O {@code fillReservoir}: uma unidade do cano do bocal, se ele puxa menos que o reservatório. */
    void fillReservoir() {
        Direction facing = this.facing();
        if (!(this.level.getBlockEntity(this.worldPosition.relative(facing)) instanceof EssentiaTransport ic)
                || !ic.isConnectable(facing.getOpposite())) {
            return;
        }
        if (!ic.canOutputTo(facing.getOpposite())) return;
        Aspect ta = null;
        if (ic.getEssentiaAmount(facing.getOpposite()) > 0
                && ic.getSuctionAmount(facing.getOpposite()) < this.getSuctionAmount(facing)
                && this.getSuctionAmount(facing) >= ic.getMinimumSuction()) {
            ta = ic.getEssentiaType(facing.getOpposite());
        }
        if (ta != null && ic.getSuctionAmount(facing.getOpposite()) < this.getSuctionAmount(facing)) {
            this.addToContainer(ta, ic.takeEssentia(ta, 1, facing.getOpposite()));
        }
    }

    public void sync() {
        this.setChanged();
        if (this.level != null) this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    // ----------------------------------------------------------------- o recipiente

    @Override
    public AspectList getAspects() {
        return this.essentia;
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return true;
    }

    @Override
    public int addToContainer(Aspect tt, int am) {
        if (am == 0) return am;
        int space = this.maxAmount - this.essentia.visSize();
        if (space >= am) {
            this.essentia.add(tt, am);
            am = 0;
        } else {
            this.essentia.add(tt, space);
            am -= space;
        }
        if (space > 0) this.sync();
        return am;
    }

    @Override
    public boolean takeFromContainer(Aspect tt, int am) {
        if (this.essentia.getAmount(tt) < am) return false;
        this.essentia.remove(tt, am);
        this.sync();
        return true;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return this.essentia.getAmount(aspect) >= amount;
    }

    @Override
    public int containerContains(@Nullable Aspect aspect) {
        return aspect == null ? 0 : this.essentia.getAmount(aspect);
    }

    // ----------------------------------------------------------------- o cano

    @Override
    public boolean isConnectable(Direction face) {
        return face == this.facing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == this.facing();
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == this.facing();
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    @Override
    public int getMinimumSuction() {
        return 24;
    }

    @Nullable
    @Override
    public Aspect getSuctionType(@Nullable Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return this.essentia.visSize() < this.maxAmount ? 24 : 0;
    }

    /** O original só diz o tipo para quem pergunta sem lado ({@code UNKNOWN}); o cano pergunta com lado e não vê. */
    @Nullable
    @Override
    public Aspect getEssentiaType(@Nullable Direction face) {
        return this.essentia.visSize() > 0 && face == null ? this.essentia.getAspects().getFirst() : null;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return this.essentia.visSize();
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return this.canOutputTo(face) && this.takeFromContainer(aspect, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return this.canInputFrom(face) ? amount - this.addToContainer(aspect, amount) : 0;
    }

    /** O {@code onWandRightClick}: o bocal vira para a face batida; agachado, para a oposta. */
    @Override
    public boolean onWand(Level level, ItemStack wand, Player player, BlockPos pos, Direction face) {
        Direction to = player.isShiftKeyDown() ? face : face.getOpposite();
        if (!level.isClientSide()) {
            level.setBlock(pos, this.getBlockState().setValue(EssentiaReservoirBlock.FACING, to), 3);
            this.setChanged();
        }
        player.swing(InteractionHand.MAIN_HAND);
        return true;
    }

    /** O estouro de quando quebra com essência dentro. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null) EssentiaReservoirBlock.burst(this.level, pos, this.essentia.visSize());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.essentia = input.read("aspects", AspectList.CODEC).orElseGet(AspectList::new);
        if (this.essentia.visSize() > this.maxAmount) this.essentia = new AspectList();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("aspects", AspectList.CODEC, this.essentia);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
