package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.registry.TCBlockEntities;

/**
 * O {@code TileInfusionPillar} da 4.2.3.5: só guarda para que lado o pilar olha — de dois a cinco, conforme o
 * canto do altar —, que é o giro do modelo.
 */
public class InfusionPillarBlockEntity extends BlockEntity {
    private byte orientation;

    public InfusionPillarBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.INFUSION_PILLAR, pos, state);
    }

    public byte orientation() {
        return this.orientation;
    }

    public void setOrientation(byte orientation) {
        this.orientation = orientation;
        this.setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.orientation = (byte) input.getIntOr("orientation", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("orientation", this.orientation);
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
