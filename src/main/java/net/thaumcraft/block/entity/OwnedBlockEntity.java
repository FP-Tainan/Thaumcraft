package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.registry.TCBlockEntities;

import java.util.ArrayList;
import java.util.List;

/**
 * O {@code TileOwned} da 4.2.3.5: o dono de uma porta arcana ou de uma placa de pressão arcana, pelo nome, e a lista de
 * quem tem chave. Cada entrada é o tipo da chave ("0" ferro, "1" ouro) seguido do nome de quem a usou.
 */
public class OwnedBlockEntity extends BlockEntity {
    public String owner = "";
    public final List<String> accessList = new ArrayList<>();

    public OwnedBlockEntity(BlockPos pos, BlockState state) {
        this(TCBlockEntities.OWNED, pos, state);
    }

    protected OwnedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Dono, ou com chave de qualquer tipo. */
    public boolean mayUse(Player player) {
        String name = player.getName().getString();
        return name.equals(this.owner) || this.accessList.contains("0" + name) || this.accessList.contains("1" + name);
    }

    /** Dono, ou com a chave de ouro. */
    public boolean mayManage(Player player) {
        String name = player.getName().getString();
        return name.equals(this.owner) || this.accessList.contains("1" + name);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.owner = input.getStringOr("owner", "");
        this.accessList.clear();
        int count = input.getIntOr("access_count", 0);
        for (int i = 0; i < count; i++) this.accessList.add(input.getStringOr("access" + i, ""));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("owner", this.owner);
        output.putInt("access_count", this.accessList.size());
        for (int i = 0; i < this.accessList.size(); i++) output.putString("access" + i, this.accessList.get(i));
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
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
