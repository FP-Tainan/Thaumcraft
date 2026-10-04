package net.thaumcraft.occulta;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * O que um <b>Baú de Sanguessugas</b> guarda: a {@code TileEntityLeechChest} do Witchery.
 *
 * <p>Ele guarda o que qualquer baú guarda — vinte e sete lugares — e mais uma coisa: <b>os nomes de quem o
 * abriu</b>. Até três, os mais recentes, e cada um deles aparece como um <b>saco de sangue</b> na frente do
 * baú.
 *
 * <p>É por isso que ele é uma armadilha e não um móvel. Quem o abre deixa o nome nele; quem o plantou volta
 * com um <b>Frasco de Vínculo</b> e sai com o nome de quem passou por lá. O baú não rouba nada: ele
 * <b>anota</b>.
 *
 * <p>E os sacos são a parte honesta da armadilha: eles ficam à vista. Um baú com três sacos na frente está
 * dizendo, a quem souber ler, que três pessoas já o abriram.
 */
public class LeechChestBlockEntity extends BaseContainerBlockEntity {
    /** Quantos nomes ele guarda, e quantos sacos ele mostra. */
    public static final int NOMES = 3;

    /** E quantos lugares ele tem: os de um baú. */
    public static final int LUGARES = 27;

    private NonNullList<ItemStack> dentro = NonNullList.withSize(LUGARES, ItemStack.EMPTY);
    private final List<String> quemAbriu = new ArrayList<>();
    private final ChestLidController tampa = new ChestLidController();

    private final ContainerOpenersCounter abertos = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos onde, BlockState feitio) {
            level.playSound(null, onde, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5f,
                    level.getRandom().nextFloat() * 0.1f + 0.9f);
        }

        @Override
        protected void onClose(Level level, BlockPos onde, BlockState feitio) {
            level.playSound(null, onde, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5f,
                    level.getRandom().nextFloat() * 0.1f + 0.9f);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos onde, BlockState feitio, int antes,
                                          int agora) {
            level.blockEvent(onde, LeechChestBlockEntity.this.getBlockState().getBlock(), 1, agora);
        }

        @Override
        public boolean isOwnContainer(Player quem) {
            return quem.containerMenu instanceof ChestMenu lista
                    && lista.getContainer() == LeechChestBlockEntity.this;
        }
    };

    public LeechChestBlockEntity(BlockPos onde, BlockState feitio) {
        super(OccultaBlocks.LEECH_CHEST_ENTITY, onde, feitio);
    }

    // ------------------------------------------------------------------ os nomes

    /**
     * <b>Anota</b> quem o abriu.
     *
     * <p>Até três, os mais recentes; o quarto empurra o primeiro para fora. E ninguém entra duas vezes na
     * lista — um baú aberto dez vezes pela mesma pessoa tem um saco, não dez.
     */
    public void anota(Player quem) {
        if (this.level == null || this.level.isClientSide()) return;
        String nome = quem.getName().getString();
        if (this.quemAbriu.contains(nome)) return;
        this.quemAbriu.add(nome);
        while (this.quemAbriu.size() > NOMES) this.quemAbriu.removeFirst();
        this.avisa();
    }

    /**
     * E <b>devolve um nome</b> que não seja o de quem está perguntando.
     *
     * <p>Do mais recente para o mais antigo, e só de quem está no mundo agora: um nome de alguém que saiu
     * não serve para prender ninguém, e fica guardado para quando ele voltar.
     */
    public @org.jetbrains.annotations.Nullable String tiraUmNome(Player menosEste) {
        if (!(this.level instanceof ServerLevel mundo)) return null;
        String oDele = menosEste.getName().getString();
        for (int i = this.quemAbriu.size() - 1; i >= 0; i--) {
            String nome = this.quemAbriu.get(i);
            if (nome.equals(oDele)) continue;
            if (mundo.getServer().getPlayerList().getPlayerByName(nome) == null) continue;
            this.quemAbriu.remove(i);
            this.avisa();
            return nome;
        }
        return null;
    }

    /** Quantos nomes ele tem: é o número de sacos que o desenhista mostra. */
    public int quantosNomes() {
        return this.quemAbriu.size();
    }

    public List<String> nomes() {
        return List.copyOf(this.quemAbriu);
    }

    private void avisa() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    // ------------------------------------------------------------------ a tampa

    public static void tick(Level level, BlockPos onde, BlockState feitio, LeechChestBlockEntity baú) {
        baú.tampa.tickLid();
    }

    public float tampa(float parcial) {
        return this.tampa.getOpenness(parcial);
    }

    @Override
    public boolean triggerEvent(int qual, int quanto) {
        if (qual != 1) return super.triggerEvent(qual, quanto);
        this.tampa.shouldBeOpen(quanto > 0);
        return true;
    }

    @Override
    public void startOpen(net.minecraft.world.entity.ContainerUser quem) {
        if (this.remove || this.level == null) return;
        LivingEntity vivo = quem.getLivingEntity();
        if (vivo == null || vivo.isSpectator()) return;
        this.abertos.incrementOpeners(vivo, this.level, this.getBlockPos(), this.getBlockState(),
                quem.getContainerInteractionRange());
    }

    @Override
    public void stopOpen(net.minecraft.world.entity.ContainerUser quem) {
        if (this.remove || this.level == null) return;
        LivingEntity vivo = quem.getLivingEntity();
        if (vivo == null || vivo.isSpectator()) return;
        this.abertos.decrementOpeners(vivo, this.level, this.getBlockPos(), this.getBlockState());
    }

    public void recheckOpen() {
        if (this.remove || this.level == null) return;
        this.abertos.recheckOpeners(this.level, this.getBlockPos(), this.getBlockState());
    }

    // ------------------------------------------------------------------ o que qualquer baú é

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.thaumcraft.leech_chest");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.dentro;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> quais) {
        this.dentro = quais;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory mochila) {
        return ChestMenu.threeRows(id, mochila, this);
    }

    @Override
    public int getContainerSize() {
        return LUGARES;
    }

    @Override
    protected void saveAdditional(ValueOutput dados) {
        super.saveAdditional(dados);
        dados.store("QuemAbriu", com.mojang.serialization.Codec.STRING.listOf(), this.quemAbriu);
    }

    @Override
    protected void loadAdditional(ValueInput dados) {
        super.loadAdditional(dados);
        this.quemAbriu.clear();
        dados.read("QuemAbriu", com.mojang.serialization.Codec.STRING.listOf())
                .ifPresent(this.quemAbriu::addAll);
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
