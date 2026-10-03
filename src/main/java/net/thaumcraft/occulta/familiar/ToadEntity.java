package net.thaumcraft.occulta.familiar;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Sapo</b>: o {@code EntityToad} do Witchery, e o familiar do <b>cozimento</b>.
 *
 * <p>Doma-se com <b>carne podre</b>, que é o do original e combina com o bicho. Solto tem dez de vida; vinculado
 * sobe para <b>cinquenta</b>, porque um familiar leva pancada por quem o tem e dez não dariam para nada.
 *
 * <p>Quem o tiver vinculado tira <b>um frasco a mais</b> de cada caldeirão.
 */
public class ToadEntity extends TamableAnimal {
    /** O que ele doma com, e o que o faz curar. */
    public static final net.minecraft.world.item.Item COMIDA = Items.ROTTEN_FLESH;

    /** A vida de um familiar vinculado: os cinquenta do original. */
    public static final float VIDA_DE_FAMILIAR = 50.0f;

    public ToadEntity(EntityType<? extends ToadEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0, 10.0f, 5.0f));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    /**
     * Carne podre doma, e carne podre cura.
     *
     * <p>E <b>vincular é outra coisa</b>: domar faz dele seu, vincular faz dele familiar. O vínculo é o agachar
     * com a mão vazia em cima dele, e está no {@link Familiars}.
     */
    @Override
    public InteractionResult mobInteract(Player quem, InteractionHand mão) {
        ItemStack naMao = quem.getItemInHand(mão);

        if (this.isTame()) {
            if (naMao.is(COMIDA) && this.getHealth() < this.getMaxHealth()) {
                if (!quem.hasInfiniteMaterials()) naMao.shrink(1);
                this.heal(4.0f);
                return InteractionResult.SUCCESS;
            }
            if (naMao.isEmpty() && quem.isShiftKeyDown()
                    && this.isOwnedBy(quem) && this.level() instanceof ServerLevel) {
                return Familiars.vincula(quem, this)
                        ? InteractionResult.SUCCESS : InteractionResult.PASS;
            }
            if (naMao.isEmpty() && this.isOwnedBy(quem)) {
                this.setOrderedToSit(!this.isOrderedToSit());
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        if (naMao.is(COMIDA)) {
            if (!quem.hasInfiniteMaterials()) naMao.shrink(1);
            if (this.level() instanceof ServerLevel level && this.random.nextInt(3) == 0) {
                this.tame(quem);
                this.setOrderedToSit(true);
                level.broadcastEntityEvent(this, (byte) 7);
            } else if (this.level() instanceof ServerLevel level) {
                level.broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /** Vinculado, ele aguenta o que um familiar tem de aguentar. */
    /**
     * Um sapo que <b>caiu do céu</b> tem hora para acabar: o {@code setTimeToLive} do original.
     *
     * <p>Sem isto, a Chuva de Sapos deixava o mapa cheio de sapos para sempre — e são dezessete de cada vez,
     * de trinta em trinta batidas, por duzentas voltas.
     */
    public void choveu(int batidas) {
        this.tempoQueResta = batidas;
    }

    private int tempoQueResta = -1;

    @Override
    public void tick() {
        super.tick();
        if (this.tempoQueResta < 0 || this.level().isClientSide()) return;
        if (--this.tempoQueResta > 0) return;
        if (this.level() instanceof net.minecraft.server.level.ServerLevel mundo) {
            mundo.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                    this.getX(), this.getY() + 0.2, this.getZ(), 8, 0.2, 0.2, 0.2, 0.02);
        }
        this.discard();
    }

    public void viraFamiliar() {
        var vida = this.getAttribute(Attributes.MAX_HEALTH);
        if (vida != null) vida.setBaseValue(VIDA_DE_FAMILIAR);
        this.setHealth(VIDA_DE_FAMILIAR);
        this.setPersistenceRequired();
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob outro) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack coisa) {
        return coisa.is(COMIDA);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.FROG_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource fonte) {
        return SoundEvents.FROG_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.FROG_DEATH;
    }
}
