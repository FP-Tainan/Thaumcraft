package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.registry.TCBlockEntities;

/**
 * O que o bloco protegido guarda: o {@code TileWarded} da 4.2.3.5 — o bloco de antes e quem o protegeu.
 *
 * <p>O dono é o número do nome do jogador ({@code hashCode}), como no original: só ele desfaz a proteção.
 */
public class WardedBlockEntity extends BlockEntity {
    private BlockState stored = Blocks.STONE.defaultBlockState();
    private int owner;

    public WardedBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.WARDED, pos, state);
    }

    public BlockState stored() {
        return this.stored;
    }

    public int owner() {
        return this.owner;
    }

    public void ward(BlockState stored, int owner) {
        this.stored = stored;
        this.owner = owner;
        this.setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.stored = input.read("block", BlockState.CODEC).orElse(Blocks.STONE.defaultBlockState());
        this.owner = input.getIntOr("owner", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("block", BlockState.CODEC, this.stored);
        output.putInt("owner", this.owner);
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
