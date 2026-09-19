package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.api.visnet.VisNet;
import net.thaumcraft.block.FluxBlock;
import net.thaumcraft.net.TCNetwork;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * O purificador de fluxo: o {@code TileFluxScrubber} da 4.2.3.5. Bebe Aer da rede de vis (dez por vez, até juntar cinco)
 * e com isso some com o fluxo (a gosma e o gás) a até dezesseis blocos, um nível por vez, sorteando dezesseis lugares
 * por tique. A cada quatro fluxos desfeitos, um em quatro de juntar um ponto de Praecantatio (até quatro), que sai por
 * cano pela face de trás.
 */
public class FluxScrubberBlockEntity extends BlockEntity implements EssentiaTransport {
    public int essentia;
    public int charges;
    public int power;
    public Direction facing = Direction.DOWN;
    public int count;
    private final List<BlockPos> checklist = new ArrayList<>();

    public FluxScrubberBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.FLUX_SCRUBBER, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FluxScrubberBlockEntity te) {
        if (te.count == 0) te.count = level.getRandom().nextInt(1000);
        if (!(level instanceof ServerLevel server)) return;
        if (te.charges >= 4) {
            te.charges -= 4;
            if (level.getRandom().nextInt(4) == 0) {
                te.essentia = Math.min(4, te.essentia + 1);
                te.setChanged();
            }
        }
        if (te.power < 5) te.power += VisNet.drainVis(level, pos, Aspects.AIR, 10);
        if (te.power >= 5) te.checkFlux(server, pos);
    }

    private void checkFlux(ServerLevel level, BlockPos pos) {
        int distance = 16;
        if (this.checklist.isEmpty()) {
            for (int a = -distance; a <= distance; a++) {
                for (int c = -distance; c <= distance; c++) {
                    for (int b = -distance; b <= distance; b++) this.checklist.add(pos.offset(a, c, b));
                }
            }
            Collections.shuffle(this.checklist, new java.util.Random(level.getRandom().nextLong()));
        }
        int cc = 0;
        while (cc < 16 && !this.checklist.isEmpty()) {
            cc++;
            BlockPos at = this.checklist.removeFirst();
            BlockState there = level.getBlockState(at);
            if (there.isAir() || !(there.getBlock() instanceof FluxBlock)
                    || at.distToCenterSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) >= distance * distance) continue;
            this.power -= 5;
            int lmd = there.getValue(FluxBlock.LEVEL);
            if (lmd > 0) level.setBlock(at, there.setValue(FluxBlock.LEVEL, lmd - 1), Block.UPDATE_ALL);
            else level.removeBlock(at, false);
            TCNetwork.blockSparkle(level, at, 14483711);
            this.charges++;
            this.setChanged();
            return;
        }
    }

    // ---- o cano: só sai Praecantatio, pela face de trás ----

    @Override
    public boolean isConnectable(Direction face) {
        return face == this.facing;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == this.facing;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public Aspect getSuctionType(@Nullable Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        int re = Math.min(this.essentia, amount);
        this.essentia -= re;
        this.setChanged();
        return re;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public Aspect getEssentiaType(@Nullable Direction face) {
        return Aspects.MAGIC;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return this.essentia;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    public void setFacing(Direction facing) {
        this.facing = facing;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.facing = Direction.from3DDataValue(input.getIntOr("facing", 0));
        this.charges = input.getIntOr("charges", 0);
        this.power = input.getIntOr("power", 0);
        this.essentia = input.getIntOr("essentia", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("facing", this.facing.get3DDataValue());
        output.putInt("charges", this.charges);
        output.putInt("power", this.power);
        output.putInt("essentia", this.essentia);
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
