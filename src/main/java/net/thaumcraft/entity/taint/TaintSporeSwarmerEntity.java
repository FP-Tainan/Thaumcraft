package net.thaumcraft.entity.taint;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCSounds;

/**
 * O enxameador de esporos: o {@code EntityTaintSporeSwarmer} da 4.2.3.5. O esporo grande (75 de vida, um bloco) que a
 * crosta solta: não estoura ao toque; a cada 25 segundos, com um jogador a dezesseis blocos, solta um enxame da
 * mácula (e solta outro ao morrer). As mosquinhas em volta aumentam até a hora do enxame.
 */
public class TaintSporeSwarmerEntity extends TaintSporeEntity {
    private int spawnCounter = 500;

    public TaintSporeSwarmerEntity(EntityType<? extends TaintSporeSwarmerEntity> type, Level level) {
        super(type, level);
        this.setSporeSize(10);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 75.0).add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return EntityDimensions.scalable(1.0f, 1.0f);
    }

    @Override
    public void handleDamageEvent(DamageSource source) {
        super.handleDamageEvent(source);
        this.sploosh(10);
    }

    @Override
    protected void sporeTick() {
        if (this.spawnCounter > 0) this.spawnCounter--;
        if (this.spawnCounter <= 0 && this.level().getNearestPlayer(this, 16.0) != null) {
            this.spawnCounter = 500;
            this.swarmBurst(1);
        }
        if (this.level().isClientSide()) {
            this.pruneSwarm();
            if (this.swarm.size() < (500 - this.spawnCounter) / 25) {
                Object fx = swarmEffect.apply(this);
                if (fx != null) this.swarm.add(fx);
            }
        }
        if (this.deathTime == 1) this.swarmBurst(1);
    }

    /** O enxameador não estoura ao toque. */
    @Override
    public void playerTouch(Player player) {
    }

    /** Solta enxames, e avisa quem vê (o evento 6: o contador volta e as mosquinhas somem num respingo). */
    protected void swarmBurst(int amount) {
        if (!(this.level() instanceof ServerLevel level)) return;
        this.playSound(TCSounds.GORE.value(), 1.0f, 0.9f + level.getRandom().nextFloat() * 0.1f);
        for (int a = 0; a < amount; a++) {
            TaintSwarmEntity swarm = new TaintSwarmEntity(TCEntities.TAINT_SWARM, level);
            swarm.snapTo(this.getX(), this.getY() + 0.5, this.getZ(), level.getRandom().nextFloat() * 360.0f, 0.0f);
            level.addFreshEntity(swarm);
        }
        level.broadcastEntityEvent(this, (byte) 6);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 6) {
            this.spawnCounter = 500;
            this.sploosh(25);
            this.swarm.forEach(killSwarm);
            this.swarm.clear();
        } else {
            super.handleEntityEvent(id);
        }
    }

    /** Some com uma mosquinha (o cliente liga isto). */
    public static java.util.function.Consumer<Object> killSwarm = fx -> {
    };

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.ROOTS.value();
    }

    /** O {@code dropFewItems}: duas vezes. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        for (int a = 0; a <= 1; a++) TaintDrops.either(level, this);
    }
}
