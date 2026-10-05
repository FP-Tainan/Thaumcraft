package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A alma do Item Posto: a {@code TileEntityPlacedItem} do Witchery.
 *
 * <p>Guarda <b>uma coisa</b>, e a manda pela rede — o que é preciso, porque quem desenha o bloco é o
 * cliente e ele não tem como adivinhar o que está deitado ali.
 */
public class PlacedItemBlockEntity extends BlockEntity {
    private ItemStack oquê = ItemStack.EMPTY;

    public PlacedItemBlockEntity(BlockPos onde, BlockState feitio) {
        super(OccultaBlocks.PLACED_ITEM_ENTITY, onde, feitio);
    }

    public ItemStack oquê() {
        return this.oquê;
    }

    /** Põe o que ele guarda, e avisa quem está olhando. */
    public void põe(ItemStack oquê) {
        this.oquê = oquê;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(),
                    Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput dados) {
        super.saveAdditional(dados);
        if (!this.oquê.isEmpty()) dados.store("WITCPlacedItem", ItemStack.CODEC, this.oquê);
    }

    @Override
    protected void loadAdditional(ValueInput dados) {
        super.loadAdditional(dados);
        this.oquê = dados.read("WITCPlacedItem", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registos) {
        return this.saveCustomOnly(registos);
    }
}
