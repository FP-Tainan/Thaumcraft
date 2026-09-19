package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * O que o {@code TileMirror} e o {@code TileMirrorEssentia} da 4.2.3.5 têm igual: a ligação com o par (onde ele está e
 * em que mundo), e o jeito de refazê-la, desfazê-la e conferi-la. O original repete esse código nas duas classes; aqui
 * ele fica uma vez só, e cada espelho só aceita par da mesma classe que a dele.
 */
public abstract class LinkedMirrorBlockEntity extends BlockEntity {
    public boolean linked;
    public int linkX;
    public int linkY;
    public int linkZ;
    /** O {@code linkDim}: aqui o nome do mundo, que é o que hoje identifica uma dimensão. */
    public ResourceKey<Level> linkDim = Level.OVERWORLD;
    protected int count;
    protected int inc = 40;

    protected LinkedMirrorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public BlockPos linkPos() {
        return new BlockPos(this.linkX, this.linkY, this.linkZ);
    }

    public void setLink(BlockPos pos, ResourceKey<Level> dim) {
        this.linkX = pos.getX();
        this.linkY = pos.getY();
        this.linkZ = pos.getZ();
        this.linkDim = dim;
    }

    /** O {@code DimensionManager.getWorld(linkDim)}: o mundo do par, se o servidor o tiver aberto. */
    @Nullable
    public ServerLevel targetWorld() {
        if (this.level == null || this.level.getServer() == null) return null;
        return this.level.getServer().getLevel(this.linkDim);
    }

    /** O par, se lá houver um espelho desta mesma classe. */
    @Nullable
    protected LinkedMirrorBlockEntity partner(@Nullable Level world) {
        if (world == null) return null;
        BlockEntity te = world.getBlockEntity(this.linkPos());
        return te != null && te.getClass() == this.getClass() ? (LinkedMirrorBlockEntity) te : null;
    }

    private boolean pointsHere(LinkedMirrorBlockEntity other) {
        return other.linkX == this.worldPosition.getX() && other.linkY == this.worldPosition.getY()
                && other.linkZ == this.worldPosition.getZ() && other.linkDim.equals(this.level.dimension());
    }

    /** Marca a mudança e manda para quem vê (o {@code markDirty} mais o {@code markBlockForUpdate}). */
    public void sync() {
        this.setChanged();
        if (this.level != null) this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    /** O {@code restoreLink}: se o par não está ligado a ninguém, os dois se ligam. */
    public void restoreLink() {
        if (!this.isDestinationValid()) return;
        ServerLevel targetWorld = this.targetWorld();
        LinkedMirrorBlockEntity tm = this.partner(targetWorld);
        if (tm == null) return;
        tm.linked = true;
        tm.setLink(this.worldPosition, this.level.dimension());
        this.linked = true;
        this.onRestored(targetWorld);
        this.sync();
        tm.sync();
    }

    /** O que cada espelho faz a mais quando a ligação volta. */
    protected void onRestored(ServerLevel targetWorld) {
    }

    /** O {@code invalidateLink}: o par, se estiver carregado, deixa de estar ligado. */
    public void invalidateLink() {
        ServerLevel targetWorld = this.targetWorld();
        if (targetWorld == null || !targetWorld.isLoaded(this.linkPos())) return;
        LinkedMirrorBlockEntity tm = this.partner(targetWorld);
        if (tm != null) {
            tm.linked = false;
            tm.onUnlinked();
            this.setChanged();
            tm.sync();
        }
    }

    protected void onUnlinked() {
    }

    private void breakLink() {
        this.linked = false;
        this.sync();
    }

    /** O {@code isLinkValid}: a ligação vale se o par existe, está ligado e aponta para cá; se não, ela se desfaz. */
    public boolean isLinkValid() {
        if (!this.linked) return false;
        ServerLevel targetWorld = this.targetWorld();
        if (targetWorld == null) return false;
        LinkedMirrorBlockEntity tm = this.partner(targetWorld);
        if (tm == null || !tm.linked || !this.pointsHere(tm)) {
            this.breakLink();
            return false;
        }
        return true;
    }

    /** O {@code isLinkValidSimple}: a mesma conferência, sem desfazer nada. */
    public boolean isLinkValidSimple() {
        if (!this.linked) return false;
        LinkedMirrorBlockEntity tm = this.partner(this.targetWorld());
        return tm != null && tm.linked && this.pointsHere(tm);
    }

    /** O {@code isDestinationValid}: há um espelho lá, e ele está livre. */
    public boolean isDestinationValid() {
        ServerLevel targetWorld = this.targetWorld();
        if (targetWorld == null) return false;
        LinkedMirrorBlockEntity tm = this.partner(targetWorld);
        if (tm == null) {
            this.breakLink();
            return false;
        }
        return !tm.isLinkValid();
    }

    /** O pedaço do {@code updateEntity} que, de tempos em tempos (cada vez mais espaçados), tenta refazer a ligação. */
    protected void relinkTick() {
        if (this.count++ % this.inc == 0) {
            if (!this.isLinkValidSimple()) {
                if (this.inc < 600) this.inc += 20;
                this.restoreLink();
            } else {
                this.inc = 40;
            }
        }
    }

    /** O nome que a dica do item mostra: o {@code getDimensionName} do mundo. */
    public static String dimensionName(ResourceKey<Level> dim) {
        if (dim.equals(Level.OVERWORLD)) return "Overworld";
        if (dim.equals(Level.NETHER)) return "Nether";
        if (dim.equals(Level.END)) return "The End";
        return dim.identifier().toString();
    }

    /** Os dados da ligação no item, como o original grava na NBT dele. */
    public void writeLinkTo(CompoundTag tag, ResourceKey<Level> here) {
        tag.putInt("linkX", this.linkX);
        tag.putInt("linkY", this.linkY);
        tag.putInt("linkZ", this.linkZ);
        tag.putString("linkDim", this.linkDim.identifier().toString());
        tag.putString("dimname", dimensionName(here));
    }

    public static ResourceKey<Level> dimensionOf(String id) {
        Identifier parsed = Identifier.tryParse(id);
        return ResourceKey.create(Registries.DIMENSION, parsed == null ? Level.OVERWORLD.identifier() : parsed);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.linked = input.getBooleanOr("linked", false);
        this.linkX = input.getIntOr("linkX", 0);
        this.linkY = input.getIntOr("linkY", 0);
        this.linkZ = input.getIntOr("linkZ", 0);
        this.linkDim = dimensionOf(input.getStringOr("linkDim", "minecraft:overworld"));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("linked", this.linked);
        output.putInt("linkX", this.linkX);
        output.putInt("linkY", this.linkY);
        output.putInt("linkZ", this.linkZ);
        output.putString("linkDim", this.linkDim.identifier().toString());
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
