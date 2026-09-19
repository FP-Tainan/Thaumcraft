package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCComponents;
import org.jetbrains.annotations.Nullable;

/**
 * O estandarte: o {@code TileBanner} da 4.2.3.5. Guarda para onde olha (dezesseis rumos; na parede, quatro), a cor da
 * lã (nenhuma é o estandarte dos cultistas), o aspecto pintado nele e se está pendurado na parede.
 */
public class BannerBlockEntity extends BlockEntity {
    private byte facing;
    private byte color = -1;
    @Nullable
    private Aspect aspect;
    private boolean onWall;

    public BannerBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.BANNER, pos, state);
    }

    public byte getFacing() {
        return this.facing;
    }

    public void setFacing(byte face) {
        this.facing = face;
        this.sync();
    }

    public boolean getWall() {
        return this.onWall;
    }

    public void setWall(boolean wall) {
        this.onWall = wall;
        this.sync();
    }

    public byte getColor() {
        return this.color;
    }

    public void setColor(byte color) {
        this.color = color;
        this.sync();
    }

    @Nullable
    public Aspect getAspect() {
        return this.aspect;
    }

    public void setAspect(@Nullable Aspect aspect) {
        this.aspect = aspect;
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
        this.facing = input.getByteOr("facing", (byte) 0);
        this.color = input.getByteOr("color", (byte) -1);
        String as = input.getStringOr("aspect", "");
        this.aspect = as.isEmpty() ? null : Aspect.of(as);
        this.onWall = input.getBooleanOr("wall", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putByte("facing", this.facing);
        output.putByte("color", this.color);
        output.putString("aspect", this.aspect == null ? "" : this.aspect.tag());
        output.putBoolean("wall", this.onWall);
    }

    /** O que o item leva quando o estandarte é quebrado: a cor e o aspecto (o {@code getDrops} do original). */
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (this.color >= 0 || this.aspect != null) {
            components.set(TCComponents.BANNER_COLOR, (int) this.color);
            if (this.aspect != null) components.set(TCComponents.BANNER_ASPECT, this.aspect.tag());
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        Integer c = components.get(TCComponents.BANNER_COLOR);
        if (c != null) this.color = (byte) (int) c;
        String as = components.get(TCComponents.BANNER_ASPECT);
        if (as != null && !as.isEmpty()) this.aspect = Aspect.of(as);
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
