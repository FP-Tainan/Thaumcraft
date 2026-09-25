package net.thaumcraft.naturalis;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.TaintedMob;
import net.thaumcraft.entity.taint.TaintDrops;
import net.thaumcraft.entity.taint.TaintSpiderEntity;
import net.thaumcraft.registry.TCEntities;

/**
 * O Criador de Mácula: o {@code EntityTaintBreeder} do Magia Naturalis 0.5.0.
 *
 * <p>Uma aranha grande e ligeira da terra maculada. Ferida — abaixo de oito décimos da vida —, ela põe no mundo
 * uma ou duas aranhas de mácula de um segundo em um segundo, com pressa e força de mais, enquanto tiver alguém
 * para caçar. O veneno da mácula não a pega, e o que ela deixa cair é o que a mácula deixa.
 */
public class TaintBreederEntity extends Spider implements TaintedMob {
    /** De quantas em quantas ninhadas ela pare: o {@code breedTime} do original. */
    private static final int BREED_TIME = 8;

    private int broodTimer;

    public TaintBreederEntity(EntityType<? extends TaintBreederEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Spider.createAttributes()
                .add(Attributes.MAX_HEALTH, 42.0)
                .add(Attributes.MOVEMENT_SPEED, 0.6);
    }

    /** O veneno da mácula não pega nela. */
    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.is(net.thaumcraft.registry.TCEffects.FLUX_TAINT) && super.canBeAffected(effect);
    }

    @Override
    public void aiStep() {
        if (this.level() instanceof ServerLevel server && this.tickCount % 20 == 0
                && this.getHealth() < this.getMaxHealth() * 0.8f
                && this.broodTimer++ % BREED_TIME == 0 && this.getTarget() != null) {
            this.breed(server);
        }
        super.aiStep();
    }

    /** A ninhada: uma ou duas aranhas de mácula, com pressa e força de mais. */
    private void breed(ServerLevel level) {
        for (int i = level.getRandom().nextInt(2); i >= 0; i--) {
            TaintSpiderEntity filhote = TCEntities.TAINT_SPIDER.create(level, EntitySpawnReason.MOB_SUMMONED);
            if (filhote == null) continue;
            filhote.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            // o original dá um nível menos no difícil, que já é duro o bastante
            int nivel = level.getDifficulty() == Difficulty.HARD ? 2 : 3;
            filhote.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 144, nivel));
            filhote.addEffect(new MobEffectInstance(MobEffects.SPEED, 144, nivel - 2));
            level.addFreshEntity(filhote);
        }
    }

    /** O {@code dropFewItems}: uma vez em seis, um pedaço da mácula. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (level.getRandom().nextInt(6) == 0) TaintDrops.either(level, this);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("BroodTimer", this.broodTimer);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.broodTimer = input.getIntOr("BroodTimer", 0);
    }
}
