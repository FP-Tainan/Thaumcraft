package net.thaumcraft.entity.taint;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.thaumcraft.api.TaintedMob;

/**
 * O que as criaturas maculadas que eram bichos têm em comum: o {@code EntityMob} com a IA nova, a voz grossa (tom 0,7)
 * e, nos cinco primeiros tiques, o respingo roxo de quem acabou de virar (o {@code splooshFX}, que o cliente liga).
 */
public abstract class TaintedMonster extends Monster implements TaintedMob {
    protected TaintedMonster(EntityType<? extends TaintedMonster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes(double health, double damage, double speed) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.ATTACK_DAMAGE, damage)
                .add(Attributes.MOVEMENT_SPEED, speed);
    }

    /** O respingo do {@code splooshFX} (o cliente liga isto). */
    public static java.util.function.Consumer<net.minecraft.world.entity.Entity> sploosh = e -> {
    };

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && this.tickCount < 5) {
            for (int a = 0; a < 20; a++) sploosh.accept(this);
        }
    }

    /** Um som do jogo pelo nome (os dos bichos de hoje vêm em variantes; o maculado usa o de sempre). */
    public static net.minecraft.sounds.SoundEvent vanilla(String id) {
        return net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getValue(net.minecraft.resources.Identifier.withDefaultNamespace(id));
    }

    @Override
    public float getVoicePitch() {
        return 0.7f;
    }
}
