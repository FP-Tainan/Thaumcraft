package net.thaumcraft.block.entity.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.entity.eldritch.EldritchCrabEntity;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCParticles;
import net.thaumcraft.registry.TCSounds;

/**
 * A abertura incrustada: o {@code TileEldritchCrabSpawner} da 4.2.3.5. Com alguém a até dezesseis blocos e menos de seis
 * caranguejos a até trinta e dois, chia, solta vapor e, pouco depois, cospe um caranguejo sem elmo pela face para que
 * está virada — de dois e meio a quatro segundos entre um e outro. De vez em quando solta um tufo de vapor à toa.
 */
public class CrabSpawnerBlockEntity extends BlockEntity {
    public int count = 150;
    public int ticks;
    int venting;
    private Direction facing = Direction.DOWN;

    public CrabSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.CRAB_SPAWNER, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CrabSpawnerBlockEntity te) {
        if (te.ticks == 0) te.ticks = level.getRandom().nextInt(500);
        te.ticks++;
        if (level instanceof ServerLevel server) {
            te.count--;
            if (te.count < 0) {
                te.count = 50 + level.getRandom().nextInt(50);
            } else {
                if (te.count == 15 && te.isActivated() && !te.maxEntitiesReached()) {
                    level.blockEvent(pos, state.getBlock(), 1, 0);
                    level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 0.5f, 1.0f);
                }
                if (te.count <= 0 && te.isActivated() && !te.maxEntitiesReached()) {
                    te.count = 150 + level.getRandom().nextInt(100);
                    te.spawnCrab(server);
                    level.playSound(null, pos, TCSounds.GORE.value(), SoundSource.HOSTILE, 0.5f, 1.0f);
                }
            }
        } else if (te.venting > 0) {
            te.venting--;
            for (int a = 0; a < 3; a++) te.drawVent(level, pos);
        } else if (level.getRandom().nextInt(20) == 0) {
            te.drawVent(level, pos);
        }
    }

    /** O {@code drawVentParticles} com a cor 10061994 e o dobro do tamanho, saindo pela face da abertura. */
    private void drawVent(Level level, BlockPos pos) {
        var r = level.getRandom();
        float fx = 0.15f - r.nextFloat() * 0.3f, fz = 0.15f - r.nextFloat() * 0.3f, fy = 0.15f - r.nextFloat() * 0.3f;
        float fx2 = 0.1f - r.nextFloat() * 0.2f, fz2 = 0.1f - r.nextFloat() * 0.2f, fy2 = 0.1f - r.nextFloat() * 0.2f;
        var dir = this.facing;
        level.addParticle(ColorParticleOption.create(TCParticles.VENT_LARGE, 0xFF000000 | 10061994),
                pos.getX() + 0.5f + fx + dir.getStepX() / 2.1f, pos.getY() + 0.5f + fy + dir.getStepY() / 2.1f,
                pos.getZ() + 0.5f + fz + dir.getStepZ() / 2.1f,
                dir.getStepX() / 3.0f + fx2, dir.getStepY() / 3.0f + fy2, dir.getStepZ() / 3.0f + fz2);
    }

    public boolean triggerEvent(int id, int param) {
        if (id == 1) {
            this.venting = 20;
            return true;
        }
        return false;
    }

    private boolean maxEntitiesReached() {
        return this.level.getEntitiesOfClass(EldritchCrabEntity.class, new AABB(this.worldPosition).inflate(32.0)).size() > 5;
    }

    public boolean isActivated() {
        return this.level.getNearestPlayer(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5,
                16.0, false) != null;
    }

    public void spawnCrab(ServerLevel level) {
        Direction dir = this.facing;
        EldritchCrabEntity crab = TCEntities.ELDRITCH_CRAB.create(level, EntitySpawnReason.SPAWNER);
        if (crab == null) return;
        crab.snapTo(this.worldPosition.getX() + dir.getStepX() + 0.5, this.worldPosition.getY() + dir.getStepY() + 0.5,
                this.worldPosition.getZ() + dir.getStepZ() + 0.5, 0.0f, 0.0f);
        crab.finalizeSpawn(level, level.getCurrentDifficultyAt(crab.blockPosition()), EntitySpawnReason.SPAWNER, null);
        crab.setHelm(false);
        crab.setDeltaMovement(dir.getStepX() * 0.2f, dir.getStepY() * 0.2f, dir.getStepZ() * 0.2f);
        level.addFreshEntity(crab);
    }

    public Direction getFacing() {
        return this.facing;
    }

    public void setFacing(Direction facing) {
        this.facing = facing;
        this.setChanged();
        if (this.level != null) this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.facing = Direction.from3DDataValue(input.getIntOr("facing", 0));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("facing", this.facing.get3DDataValue());
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
