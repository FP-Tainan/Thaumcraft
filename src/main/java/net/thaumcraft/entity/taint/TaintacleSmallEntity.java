package net.thaumcraft.entity.taint;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * O tentáculo pequeno: o {@code EntityTaintacleSmall} da 4.2.3.5. O que o grande faz brotar aos pés de quem está longe:
 * um bloco de altura, 8 de vida, 2 de dano; vive dez segundos e não deixa nada.
 */
public class TaintacleSmallEntity extends TaintacleEntity {
    private int lifetime = 200;

    public TaintacleSmallEntity(EntityType<? extends TaintacleSmallEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level && this.lifetime-- <= 0) {
            this.hurtServer(level, this.damageSources().magic(), 10.0f);
        }
    }
}
