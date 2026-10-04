package net.thaumcraft.occulta.familiar;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Coruja</b>: o {@code EntityOwl} do Witchery, e o familiar da <b>vassoura</b>.
 *
 * <p>Doma-se com <b>carne crua</b> — porco ou boi —, voa, e bate por quatro. Solta tem dez de vida; vinculada
 * sobe para <b>cinquenta</b>.
 *
 * <p><b>A maestria dela é a da vassoura</b>: quem tem coruja voa mais depressa e consegue parar. Esteve
 * escrita e sem nada para destrancar desde a fatia dos familiares, e é a fatia da vassoura que a responde.
 */
public class OwlEntity extends TamableAnimal {
    /** O que a doma: carne crua, de porco ou de boi. */
    public static final float VIDA_DE_FAMILIAR = 50.0f;

    public OwlEntity(EntityType<? extends OwlEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation caminho = new FlyingPathNavigation(this, level);
        caminho.setCanOpenDoors(false);
        caminho.setCanFloat(true);
        return caminho;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0, 10.0f, 5.0f));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    private static boolean éCarneCrua(ItemStack coisa) {
        return coisa.is(Items.PORKCHOP) || coisa.is(Items.BEEF);
    }

    @Override
    public InteractionResult mobInteract(Player quem, InteractionHand mão) {
        ItemStack naMao = quem.getItemInHand(mão);

        if (this.isTame()) {
            if (éCarneCrua(naMao) && this.getHealth() < this.getMaxHealth()) {
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

        if (éCarneCrua(naMao)) {
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

    /** Vinculada, ela aguenta o que um familiar tem de aguentar. */
    public void viraFamiliar() {
        var vida = this.getAttribute(Attributes.MAX_HEALTH);
        if (vida != null) vida.setBaseValue(VIDA_DE_FAMILIAR);
        this.setHealth(VIDA_DE_FAMILIAR);
        this.setPersistenceRequired();
    }

    /** Coruja não se machuca de cair. */
    @Override
    public boolean causeFallDamage(double distância, float fator,
                                   net.minecraft.world.damagesource.DamageSource fonte) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob outro) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack coisa) {
        return éCarneCrua(coisa);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return net.thaumcraft.occulta.OccultaSounds.OWL_HOOT.value();
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource fonte) {
        return net.thaumcraft.occulta.OccultaSounds.OWL_HURT.value();
    }

    /** O original dá a ela o <b>mesmo som</b> para o golpe e para a morte. */
    @Override
    protected SoundEvent getDeathSound() {
        return net.thaumcraft.occulta.OccultaSounds.OWL_HURT.value();
    }
}
