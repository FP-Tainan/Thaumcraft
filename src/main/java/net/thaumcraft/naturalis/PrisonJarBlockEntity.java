package net.thaumcraft.naturalis;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.registry.TCComponents;
import org.jetbrains.annotations.Nullable;

/**
 * O que o Bicho num Jarro guarda: a criatura inteira, do jeito que ela estava — o {@code PrisonJarBlockEntity} do
 * Magia Naturalis 0.5.0.
 */
public class PrisonJarBlockEntity extends BlockEntity implements net.thaumcraft.api.wands.Wandable {
    private @Nullable CompoundTag stored;

    public PrisonJarBlockEntity(BlockPos pos, BlockState state) {
        super(NaturalisBlocks.PRISON_JAR_ENTITY, pos, state);
    }

    public boolean hasStored() {
        return this.stored != null && !this.stored.isEmpty();
    }

    public @Nullable CompoundTag stored() {
        return this.stored == null ? null : this.stored.copy();
    }

    public void setStored(@Nullable CompoundTag tag) {
        this.stored = tag == null ? null : tag.copy();
        this.setChanged();
        if (this.level != null) this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    /** Solta o bicho ao lado do jarro, como ele entrou. */
    public boolean release(ServerLevel level) {
        if (!this.hasStored()) return false;
        Entity entity = EntityType.loadEntityRecursive(this.stored, level,
                new net.minecraft.world.entity.EntitySpawnRequest(EntitySpawnReason.TRIGGERED, true), e -> {
            e.snapTo(this.getBlockPos().getX() + 0.5, this.getBlockPos().getY(), this.getBlockPos().getZ() + 0.5,
                    e.getYRot(), e.getXRot());
            return e;
        });
        if (entity == null) return false;
        level.addFreshEntity(entity);
        this.setStored(null);
        return true;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.stored = input.read("entity", CompoundTag.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.stored != null) output.store("entity", CompoundTag.CODEC, this.stored);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    /** O {@code onWandRightClick}: a varinha quebra o jarro e o bicho sai. */
    @Override
    public boolean onWand(net.minecraft.world.level.Level level, net.minecraft.world.item.ItemStack wand,
                          net.minecraft.world.entity.player.Player player, BlockPos pos, net.minecraft.core.Direction face) {
        if (!this.hasStored()) return false;
        if (level instanceof ServerLevel server) {
            this.release(server);
            level.destroyBlock(pos, false);
        }
        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.GLASS_BREAK,
                net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.9f + level.getRandom().nextFloat() * 0.2f);
        return true;
    }

    /** O que o item do jarro carrega: a mesma etiqueta. */
    public static net.minecraft.core.component.DataComponentType<CompoundTag> component() {
        return TCComponents.JARRED_MOB;
    }
}
