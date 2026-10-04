package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * O que um <b>Apanha-Erva</b> segura: a {@code TileEntityGrassper} do Witchery.
 *
 * <p>Uma coisa, uma só, e ele a mostra. É tudo o que ele guarda — e é tudo o que ele <b>é</b>: uma planta
 * que pega o que lhe dão e fica com aquilo à vista, para quem passar ver.
 *
 * <p>E o que ele tem à vista <b>viaja para o lado de cá</b>, porque é a coisa na boca dele que diz o que ele
 * está fazendo ali. Um Apanha-Erva com uma pérola do fim na boca não é um enfeite: é metade de uma receita.
 */
public class GrassperBlockEntity extends BlockEntity {
    private ItemStack naBoca = ItemStack.EMPTY;

    public GrassperBlockEntity(BlockPos onde, BlockState feitio) {
        super(OccultaBlocks.GRASSPER_ENTITY, onde, feitio);
    }

    public ItemStack naBoca() {
        return this.naBoca;
    }

    public void põe(ItemStack oquê) {
        this.naBoca = oquê;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    /** O que ele tinha, deixando-o de boca vazia. */
    public ItemStack tira() {
        ItemStack tinha = this.naBoca;
        this.põe(ItemStack.EMPTY);
        return tinha;
    }

    /**
     * E, antes de sumir, ela <b>larga o que segurava</b>.
     *
     * <p>É aqui e não no bloco: quando o bloco some, a alma já foi, e o que ela tinha iria com ela. O jogo
     * de hoje dá este aviso à alma <b>antes</b> de a tirar, e é o único lugar de onde ainda se vê o que
     * estava na boca.
     */
    @Override
    public void preRemoveSideEffects(BlockPos onde, BlockState feitio) {
        if (this.level != null && !this.naBoca.isEmpty()) {
            net.minecraft.world.level.block.Block.popResource(this.level, onde, this.naBoca);
            this.naBoca = ItemStack.EMPTY;
        }
        super.preRemoveSideEffects(onde, feitio);
    }

    @Override
    protected void saveAdditional(ValueOutput dados) {
        super.saveAdditional(dados);
        if (!this.naBoca.isEmpty()) dados.store("NaBoca", ItemStack.CODEC, this.naBoca);
    }

    @Override
    protected void loadAdditional(ValueInput dados) {
        super.loadAdditional(dados);
        this.naBoca = dados.read("NaBoca", ItemStack.CODEC).orElse(ItemStack.EMPTY);
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
