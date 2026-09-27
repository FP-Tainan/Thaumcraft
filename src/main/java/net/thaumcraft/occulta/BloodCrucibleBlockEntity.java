package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A alma do Crisol de Sangue: a {@code TileEntityBloodCrucible} do Witchery.
 *
 * <p>Ela guarda uma coisa só: <b>quanto sangue</b> há no crisol, de zero a vinte. Cada gole que um vampiro lhe
 * traz enche cinco, e o crisol cheio é o que lhe abre a escolha do dom maior.
 */
public class BloodCrucibleBlockEntity extends BlockEntity {
    /** O que ele leva, e quanto cada gole enche: os números do original. */
    public static final int MAX = 20;
    public static final int PER_FEED = 5;

    private int blood;

    public BloodCrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(OccultaBlocks.BLOOD_CRUCIBLE_ENTITY, pos, state);
    }

    public int blood() {
        return this.blood;
    }

    public boolean full() {
        return this.blood >= MAX;
    }

    /** O quanto dele está cheio, de zero a um: serve ao desenhista. */
    public float filled() {
        return this.blood / (float) MAX;
    }

    /** Mais um gole: o {@code increaseBloodLevel}. */
    public void feed() {
        if (this.blood >= MAX) return;
        this.blood = Math.min(this.blood + PER_FEED, MAX);
        this.sync();
    }

    /** E o crisol esvaziado de uma vez: o {@code drainAll}. */
    public void drain() {
        this.blood = 0;
        this.sync();
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.blood = input.getIntOr("BloodLevel", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("BloodLevel", this.blood);
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
