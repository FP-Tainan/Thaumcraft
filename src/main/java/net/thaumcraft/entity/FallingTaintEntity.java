package net.thaumcraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.block.FluxBlock;
import net.thaumcraft.block.TaintBlock;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCSounds;

/**
 * A crosta da mácula caindo: o {@code EntityFallingTaint} da 4.2.3.5. No primeiro tique tira o bloco de onde saiu (se
 * ele ainda está lá), cai como areia e, no chão (ou na gosma de fluxo funda), faz o som de carne e vira bloco de novo —
 * se ali cabe e embaixo não dá para cair mais. Some sem deixar nada se não couber. Do lado de quem vê, espirra pedaços
 * escuros ao sair e ao chegar.
 */
public class FallingTaintEntity extends Entity {
    private BlockState block = Blocks.AIR.defaultBlockState();
    private BlockPos old = BlockPos.ZERO;
    public int fallTime;

    public FallingTaintEntity(EntityType<? extends FallingTaintEntity> type, Level level) {
        super(type, level);
    }

    public FallingTaintEntity(Level level, double x, double y, double z, BlockState block, BlockPos old) {
        this(TCEntities.FALLING_TAINT, level);
        this.block = block;
        this.old = old;
        this.blocksBuilding = true;
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    public BlockState block() {
        return this.block;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    /** O rastro de fogo nunca aparece. */
    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public void tick() {
        if (this.block.isAir()) {
            this.discard();
            return;
        }
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        this.fallTime++;
        Vec3 m = this.getDeltaMovement().add(0.0, -0.04, 0.0);
        this.setDeltaMovement(m);
        this.move(MoverType.SELF, m);
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        Level level = this.level();
        if (level instanceof ServerLevel server) {
            BlockPos pos = this.blockPosition();
            if (this.fallTime == 1) {
                if (!server.getBlockState(this.old).is(this.block.getBlock())) {
                    this.discard();
                    return;
                }
                server.removeBlock(this.old, false);
            }
            BlockState below = server.getBlockState(pos.below());
            if (!this.onGround() && (!below.is(TCBlocks.FLUX_GOO) || below.getValue(FluxBlock.LEVEL) < 4)) {
                if (this.fallTime > 100 && (pos.getY() < level.getMinY() + 1 || pos.getY() > level.getMaxY()) || this.fallTime > 600) {
                    this.discard();
                }
            } else {
                Vec3 d = this.getDeltaMovement();
                this.setDeltaMovement(d.x * 0.7, d.y * -0.5, d.z * 0.7);
                if (!server.getBlockState(pos).is(Blocks.MOVING_PISTON)) {
                    server.playSound(null, this.getX(), this.getY(), this.getZ(), TCSounds.GORE.value(), SoundSource.BLOCKS, 0.5f,
                            ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f) * 0.8f);
                    this.discard();
                    if (this.canPlace(server, pos) && !TaintBlock.canFallBelow(server, pos.below())) {
                        server.setBlock(pos, this.block, Block.UPDATE_ALL);
                    }
                }
            }
        } else if (this.onGround() || this.fallTime == 1) {
            for (int j = 0; j < 10; j++) landEffect.accept(this);
        }
    }

    /** Ali cabe: fibra, gosma ou o que o bloco pode ocupar. */
    private boolean canPlace(ServerLevel level, BlockPos pos) {
        BlockState there = level.getBlockState(pos);
        return there.is(TCBlocks.TAINT_FIBRES) || there.is(TCBlocks.FLUX_GOO) || there.canBeReplaced() || there.isAir();
    }

    /** O {@code taintLandFX} (o cliente liga). */
    public static java.util.function.Consumer<Entity> landEffect = e -> {
    };

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("BlockState", BlockState.CODEC, this.block);
        output.putInt("Time", this.fallTime);
        output.store("Old", BlockPos.CODEC, this.old);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.block = input.read("BlockState", BlockState.CODEC).orElse(TCBlocks.TAINT_CRUST.defaultBlockState());
        this.fallTime = input.getIntOr("Time", 0);
        this.old = input.read("Old", BlockPos.CODEC).orElse(BlockPos.ZERO);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity, Block.getId(this.block));
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        this.block = Block.stateById(packet.getData());
        this.blocksBuilding = true;
        this.setPos(packet.getX(), packet.getY(), packet.getZ());
    }
}
