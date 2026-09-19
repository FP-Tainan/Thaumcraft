package net.thaumcraft.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;

/**
 * O zumbi zangado: o {@code EntityBrainyZombie} da 4.2.3.5. Um zumbi mais forte (25 de vida, 5 de dano, três de
 * armadura a mais) que não chama reforços e revida quem o fere. Morto, larga carne podre e, metade das vezes (mais com
 * pilhagem), o cérebro de zumbi — o que vai na tabela de saque dele.
 */
public class BrainyZombieEntity extends Zombie {
    public BrainyZombieEntity(EntityType<? extends BrainyZombieEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 25.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** O {@code getTotalArmorValue}: três a mais, até vinte. */
    @Override
    public int getArmorValue() {
        return Math.min(20, super.getArmorValue() + 3);
    }
}
