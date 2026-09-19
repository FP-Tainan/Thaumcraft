package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * O jarro de cérebro: o {@code TileJarBrain} da 4.2.3.5. O cérebro no vidro puxa as bolinhas de experiência a até seis
 * blocos e as come (até 2000 pontos), vira para quem chega perto e suspira de vez em quando. Tocado, devolve um tanto
 * ao acaso (até 64) e fica dois segundos sem comer; quebrado, devolve tudo.
 */
public class BrainJarBlockEntity extends BlockEntity {
    public static final int XP_MAX = 2000;
    public int xp;
    public int eatDelay;
    /** Do lado de quem vê: o giro do cérebro, o de antes e o alvo. */
    public float rota, rotb, target, turn, bob;
    private long lastsigh = System.currentTimeMillis() + 1500L;

    public BrainJarBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.BRAIN_JAR, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BrainJarBlockEntity jar) {
        jar.update(level, pos);
    }

    private void update(Level level, BlockPos pos) {
        if (this.xp > XP_MAX) this.xp = XP_MAX;
        ExperienceOrb orb = null;
        if (this.xp < XP_MAX) {
            orb = this.closestOrb(level, pos);
            if (orb != null && this.eatDelay == 0) {
                double dx = (pos.getX() + 0.5 - orb.getX()) / 7.0;
                double dy = (pos.getY() + 0.5 - orb.getY()) / 7.0;
                double dz = (pos.getZ() + 0.5 - orb.getZ()) / 7.0;
                double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                double pull = 1.0 - d;
                if (pull > 0.0) {
                    pull *= pull;
                    orb.setDeltaMovement(orb.getDeltaMovement().add(dx / d * pull * 0.15, dy / d * pull * 0.33, dz / d * pull * 0.15));
                }
            }
        }
        if (level.isClientSide()) {
            this.rotb = this.rota;
            Vec3 look = null;
            if (orb != null) {
                look = orb.position();
            } else {
                Player near = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6.0, false);
                if (near != null) {
                    look = near.position();
                    if (this.lastsigh < System.currentTimeMillis()) {
                        level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.BRAIN.value(), SoundSource.BLOCKS,
                                0.15f, 0.8f + level.getRandom().nextFloat() * 0.4f, false);
                        this.lastsigh = System.currentTimeMillis() + 5000L + level.getRandom().nextInt(25000);
                    }
                }
            }
            if (look != null) {
                this.target = (float) Math.atan2(look.z - (pos.getZ() + 0.5), look.x - (pos.getX() + 0.5));
            } else {
                this.target += 0.01f;
            }
            this.rota = Mth.wrapDegrees(this.rota * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
            this.target = Mth.wrapDegrees(this.target * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
            float f = Mth.wrapDegrees((this.target - this.rota) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
            this.rota += f * 0.04f;
        }
        if (this.eatDelay > 0) {
            this.eatDelay--;
        } else if (this.xp < XP_MAX && !level.isClientSide()) {
            List<ExperienceOrb> eaten = level.getEntitiesOfClass(ExperienceOrb.class,
                    new AABB(pos.getX() - 0.1, pos.getY() - 0.1, pos.getZ() - 0.1, pos.getX() + 1.1, pos.getY() + 1.1, pos.getZ() + 1.1));
            if (!eaten.isEmpty()) {
                for (ExperienceOrb eo : eaten) {
                    this.xp += eo.getValue();
                    level.playSound(null, eo.getX(), eo.getY(), eo.getZ(), SoundEvents.GENERIC_EAT.value(), SoundSource.BLOCKS, 0.1f,
                            (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2f + 1.0f);
                    eo.discard();
                }
                this.sync();
            }
        }
    }

    private @Nullable ExperienceOrb closestOrb(Level level, BlockPos pos) {
        ExperienceOrb best = null;
        double dist = Double.MAX_VALUE;
        for (ExperienceOrb eo : level.getEntitiesOfClass(ExperienceOrb.class, new AABB(pos).inflate(6.0))) {
            double d = eo.distanceToSqr(Vec3.atCenterOf(pos));
            if (d < dist) {
                best = eo;
                dist = d;
            }
        }
        return best;
    }

    /** O toque: devolve até 64 pontos, ao acaso, e fica dois segundos sem comer. */
    public void spill(Level level, BlockPos pos) {
        this.eatDelay = 40;
        if (!(level instanceof ServerLevel server)) return;
        int amount = level.getRandom().nextInt(Math.min(this.xp + 1, 64));
        if (amount <= 0) return;
        this.xp -= amount;
        ExperienceOrb.award(server, Vec3.atCenterOf(pos), amount);
        this.sync();
    }

    /** O comparador: de 1 a 15, pelo quanto está cheio. */
    public int comparator() {
        float r = (float) this.xp / XP_MAX;
        return (int) Math.floor(r * 14.0f) + (this.xp > 0 ? 1 : 0);
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level instanceof ServerLevel server && this.xp > 0) ExperienceOrb.award(server, Vec3.atLowerCornerOf(pos), this.xp);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.xp = input.getIntOr("XP", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("XP", this.xp);
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
