package net.thaumcraft.occulta.spirit;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.OccultaEntities;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * O corpo que fica deitado: o {@code EntityCorpse} do Witchery.
 *
 * <p>Quem anda em espírito deixa a carne onde estava. O corpo não anda, não se defende e não faz nada — mas a
 * vida dele é a vida de quem o deixou, e <b>matá-lo acorda</b> quem está do outro lado. É a parte que torna o
 * sonho uma coisa arriscada: enquanto se anda em espírito, o corpo está no mundo, ao alcance de qualquer um.
 */
public class CorpseEntity extends Mob {
    private @Nullable UUID owner;

    public CorpseEntity(EntityType<? extends CorpseEntity> type, Level level) {
        super(type, level);
        this.setNoAi(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    public @Nullable UUID owner() {
        return this.owner;
    }

    public void setOwner(UUID quem) {
        this.owner = quem;
    }

    /** Deita o corpo daquela pessoa onde ela estava. */
    public static @Nullable CorpseEntity lay(ServerLevel level, ServerPlayer quem) {
        CorpseEntity corpo = OccultaEntities.CORPSE.create(level, EntitySpawnReason.TRIGGERED);
        if (corpo == null) return null;
        corpo.setOwner(quem.getUUID());
        corpo.setCustomName(quem.getName());
        corpo.setHealth(quem.getHealth());
        corpo.snapTo(Math.floor(quem.getX()) + 0.5, quem.getY(), Math.floor(quem.getZ()) + 0.5,
                quem.getYRot(), 0.0f);
        level.addFreshEntity(corpo);
        return corpo;
    }

    /** O corpo daquela pessoa, se ele estiver deitado naquele mundo. */
    public static @Nullable CorpseEntity of(ServerLevel level, ServerPlayer quem) {
        for (CorpseEntity corpo : level.getEntitiesOfClass(CorpseEntity.class,
                new AABB(quem.blockPosition()).inflate(64.0))) {
            if (quem.getUUID().equals(corpo.owner())) return corpo;
        }
        return null;
    }

    /** E o corpo se levanta quando o espírito volta a ele. */
    public static void rise(ServerLevel level, ServerPlayer quem) {
        CorpseEntity corpo = of(level, quem);
        if (corpo != null) corpo.discard();
    }

    /** Matando o corpo, quem anda em espírito acorda de um susto: o {@code onDeath} do original. */
    @Override
    public void die(DamageSource fonte) {
        super.die(fonte);
        if (!(this.level() instanceof ServerLevel level) || this.owner == null) return;
        var quem = level.getServer().getPlayerList().getPlayer(this.owner);
        if (quem instanceof ServerPlayer gente) SpiritWorld.wakeUp(gente);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity outro) {
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.owner != null) output.store("owner", net.minecraft.core.UUIDUtil.CODEC, this.owner);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.owner = input.read("owner", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
    }

    /** Onde o corpo está, para quem precisar. */
    public BlockPos where() {
        return this.blockPosition();
    }
}
