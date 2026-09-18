package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectContainer;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.block.EssentiaCrystalizerBlock;
import net.thaumcraft.item.CrystalEssenceItem;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCParticles;
import org.jetbrains.annotations.Nullable;

/**
 * O cristalizador de essência: o {@code TileEssentiaCrystalizer} da 4.2.3.5, descompilado.
 *
 * <p>Puxa pela boca um ponto de essência e, a cada cinco tiques, avança um passo de duzentos; pronto, solta uma
 * essência cristalizada daquele aspecto pelo lado de trás — num baú, se houver, ou no chão — com um chiado e um
 * sopro de vapor. No original a rede de vis ainda somava terra para ir mais depressa; sem os relés neste porte,
 * ele vai sempre no passo de base, como o original sem relé por perto.
 */
public class EssentiaCrystalizerBlockEntity extends BlockEntity implements AspectContainer, EssentiaTransport {
    private static final int PROGRESS_MAX = 200;

    @Nullable
    private Aspect aspect;
    private int count;
    private int progress;
    /** Só de quem vê: o giro dos cristais, a cor que eles vão tomando e o vapor da saída. */
    public float spin;
    public float spinInc;
    public float cr = 1.0f, cg = 1.0f, cb = 1.0f;
    private int venting;

    public EssentiaCrystalizerBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ESSENTIA_CRYSTALIZER, pos, state);
    }

    public Direction facing() {
        BlockState state = this.getBlockState();
        return state.hasProperty(EssentiaCrystalizerBlock.FACING) ? state.getValue(EssentiaCrystalizerBlock.FACING) : Direction.DOWN;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EssentiaCrystalizerBlockEntity crystalizer) {
        boolean powered = level.hasNeighborSignal(pos);
        if (!level.isClientSide()) {
            if (++crystalizer.count % 5 == 0 && !powered) {
                if (crystalizer.aspect == null) {
                    crystalizer.fillReservoir(level, pos);
                    crystalizer.progress = 0;
                } else {
                    // o drainVis de terra da rede de vis, que aqui ainda não existe, somaria o dobro do que bebesse
                    crystalizer.progress += 1;
                }
            }
            if (crystalizer.aspect != null && crystalizer.progress >= PROGRESS_MAX) {
                crystalizer.eject(level, pos);
                crystalizer.aspect = null;
                crystalizer.progress = 0;
                crystalizer.sync();
            }
            return;
        }
        float tr = 1.0f, tg = 1.0f, tb = 1.0f;
        if (crystalizer.aspect != null) {
            int colour = crystalizer.aspect.color();
            tr = (colour >> 16 & 255) / 220.0f;
            tg = (colour >> 8 & 255) / 220.0f;
            tb = (colour & 255) / 220.0f;
        }
        crystalizer.cr = approach(crystalizer.cr, tr);
        crystalizer.cg = approach(crystalizer.cg, tg);
        crystalizer.cb = approach(crystalizer.cb, tb);
        crystalizer.spin += crystalizer.spinInc;
        if (crystalizer.spin > 360.0f) crystalizer.spin -= 360.0f;
        if (crystalizer.aspect != null && crystalizer.spinInc < 20.0f && !powered) {
            crystalizer.spinInc = Math.min(20.0f, crystalizer.spinInc + 0.1f);
        } else if ((crystalizer.aspect == null || powered) && crystalizer.spinInc > 0.0f) {
            crystalizer.spinInc = Math.max(0.0f, crystalizer.spinInc - 0.2f);
        }
        if (crystalizer.venting > 0) {
            crystalizer.venting--;
            var random = level.getRandom();
            Direction out = crystalizer.facing().getOpposite();
            float fx = 0.1f - random.nextFloat() * 0.2f, fz = 0.1f - random.nextFloat() * 0.2f, fy = 0.1f - random.nextFloat() * 0.2f;
            float fx2 = 0.1f - random.nextFloat() * 0.2f, fz2 = 0.1f - random.nextFloat() * 0.2f, fy2 = 0.1f - random.nextFloat() * 0.2f;
            level.addParticle(ColorParticleOption.create(TCParticles.VENT, 0xFFFFFFFF),
                    pos.getX() + 0.5f + fx + out.getStepX() / 2.1f,
                    pos.getY() + 0.5f + fy + out.getStepY() / 2.1f,
                    pos.getZ() + 0.5f + fz + out.getStepZ() / 2.1f,
                    out.getStepX() / 4.0f + fx2, out.getStepY() / 4.0f + fy2, out.getStepZ() / 4.0f + fz2);
        }
    }

    /** O passo de cor do original: cinco centésimos por tique, na direção da cor do aspecto. */
    private static float approach(float current, float target) {
        if (current < target) current += 0.05f;
        if (current > target) current -= 0.05f;
        return current;
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id >= 0) {
            if (this.level != null && this.level.isClientSide()) this.venting = 7;
            return true;
        }
        return super.triggerEvent(id, param);
    }

    /** O {@code eject}: a essência cristalizada vai para o baú de trás ou cai no chão, com o chiado. */
    private void eject(Level level, BlockPos pos) {
        ItemStack stack = CrystalEssenceItem.of(this.aspect);
        Direction out = this.facing().getOpposite();
        if (level.getBlockEntity(pos.relative(out)) instanceof Container inventory) {
            stack = HopperBlockEntity.addItem(null, inventory, stack, this.facing());
        }
        if (!stack.isEmpty()) {
            ItemEntity item = new ItemEntity(level, pos.getX() + 0.5 + out.getStepX() * 0.65,
                    pos.getY() + 0.5 + out.getStepY() * 0.65, pos.getZ() + 0.5 + out.getStepZ() * 0.65, stack);
            item.setDeltaMovement(out.getStepX() * 0.04f, out.getStepY() * 0.04f, out.getStepZ() * 0.04f);
            level.blockEvent(pos, this.getBlockState().getBlock(), 0, 0);
            level.addFreshEntity(item);
        }
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.25f,
                2.6f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8f);
    }

    /** O {@code fillReservoir}: um ponto de quem está na boca, se ela puxar mais forte que ele. */
    private void fillReservoir(Level level, BlockPos pos) {
        Direction facing = this.facing();
        if (!(level.getBlockEntity(pos.relative(facing)) instanceof EssentiaTransport source)) return;
        Direction back = facing.getOpposite();
        if (!source.isConnectable(back) || !source.canOutputTo(back)) return;
        Aspect wanted = null;
        if (source.getEssentiaAmount(back) > 0 && source.getSuctionAmount(back) < this.getSuctionAmount(facing)
                && this.getSuctionAmount(facing) >= source.getMinimumSuction()) {
            wanted = source.getEssentiaType(back);
        }
        if (wanted != null && source.getSuctionAmount(back) < this.getSuctionAmount(facing)) {
            this.addToContainer(wanted, source.takeEssentia(wanted, 1, back));
        }
    }

    @Nullable
    public Aspect aspect() {
        return this.aspect;
    }

    // ---- recipiente

    @Override
    public AspectList getAspects() {
        AspectList list = new AspectList();
        if (this.aspect != null) list.add(this.aspect, 1);
        return list;
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return true;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        if (amount == 0) return amount;
        if (this.aspect == null) {
            amount--;
            this.aspect = aspect;
            this.sync();
        }
        return amount;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        if (this.aspect == null || amount != 1) return false;
        this.aspect = null;
        this.sync();
        return true;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return amount == 1 && this.aspect != null && aspect == this.aspect;
    }

    @Override
    public int containerContains(@Nullable Aspect aspect) {
        return this.aspect != null && aspect == this.aspect ? 1 : 0;
    }

    // ---- encanamento: só pela boca, e só entrando

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
        return false;
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
        if (this.level != null && this.level.hasNeighborSignal(this.getBlockPos())) return 0;
        return face == this.facing() && this.aspect == null ? 128 : 64;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return this.canInputFrom(face) ? amount - this.addToContainer(aspect, amount) : 0;
    }

    @Override
    @Nullable
    public Aspect getEssentiaType(@Nullable Direction face) {
        return this.aspect;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return this.aspect == null ? 0 : 1;
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
        this.aspect = Aspect.of(input.getStringOr("aspect", ""));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.aspect != null) output.putString("aspect", this.aspect.tag());
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
