package net.thaumcraft.mortuorum;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * O Lacaio: o {@code EntityMinion} do Necromancy.
 *
 * <p>Ele não nasce do mundo — é costurado de pedaços e acordado no Altar de Invocação, e sai de lá manso, com
 * dono. Cada peça que o compõe lhe dá o que tinha: a cabeça de esqueleto dá vista, o tronco de golem dá couro, as
 * pernas de enderman dão pressa. O que ele é, então, é a soma do que sobrou dos outros.
 *
 * <p>Segue o dono, senta a mando, ataca quem ataca o dono e vai atrás de quem lhe mostrar um Cérebro no Espeto.
 */
public class MinionEntity extends TamableAnimal {
    private static final EntityDataAccessor<String> HEAD = SynchedEntityData.defineId(MinionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> TORSO = SynchedEntityData.defineId(MinionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ARM_LEFT = SynchedEntityData.defineId(MinionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ARM_RIGHT = SynchedEntityData.defineId(MinionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> LEGS = SynchedEntityData.defineId(MinionEntity.class, EntityDataSerializers.STRING);

    /** O {@code attackTimer} do original: enquanto corre, os braços do lacaio ficam levantados. */
    private int attackTimer;

    public MinionEntity(EntityType<? extends MinionEntity> type, Level level) {
        super(type, level);
        this.setTame(true, false);
    }

    /** Os números do {@code calculateAttributes} antes de as peças somarem. */
    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                // o TemptGoal do jogo de hoje lê o alcance num atributo, e sem ele derruba o servidor ao tiquear;
                // dez é o que o Animal declara, e é o alcance que o EntityAITempt da 1.7.10 tinha fixo no código
                .add(Attributes.TEMPT_RANGE, 10.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new TemptGoal(this, 1.0,
                stack -> stack.is(MortuorumItems.BRAIN_ON_A_STICK), false));
        this.goalSelector.addGoal(9, new net.minecraft.world.entity.ai.goal.FollowOwnerGoal(this, 1.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HEAD, "");
        builder.define(TORSO, "");
        builder.define(ARM_LEFT, "");
        builder.define(ARM_RIGHT, "");
        builder.define(LEGS, "");
    }

    // ----------------------------------------------------------------- as peças

    public MinionParts parts() {
        return new MinionParts(this.entityData.get(HEAD), this.entityData.get(TORSO),
                this.entityData.get(ARM_LEFT), this.entityData.get(ARM_RIGHT), this.entityData.get(LEGS));
    }

    /** Veste o lacaio com estas peças e refaz o que elas lhe dão. */
    public void setParts(MinionParts parts) {
        this.entityData.set(HEAD, parts.head());
        this.entityData.set(TORSO, parts.torso());
        this.entityData.set(ARM_LEFT, parts.armLeft());
        this.entityData.set(ARM_RIGHT, parts.armRight());
        this.entityData.set(LEGS, parts.legs());
        this.calculateAttributes();
    }

    /**
     * O {@code calculateAttributes}: cada peça soma o que o bicho dela dava naquele lugar, e o lacaio fica com a
     * soma. Sem peça nenhuma, ele fica só com o que já era.
     */
    public void calculateAttributes() {
        MinionParts parts = this.parts();
        this.clearPartModifiers();
        String[] lugares = {"Head", "Torso", "ArmLeft", "ArmRight", "Legs"};
        var todas = parts.all();
        for (int i = 0; i < lugares.length; i++) {
            String peca = todas.get(i);
            if (peca.isEmpty()) continue;
            var bonus = MinionAttributes.of(MinionParts.mobOf(peca), lugares[i]);
            this.addModifier(Attributes.MAX_HEALTH, lugares[i], bonus.health());
            this.addModifier(Attributes.FOLLOW_RANGE, lugares[i], bonus.followRange());
            this.addModifier(Attributes.KNOCKBACK_RESISTANCE, lugares[i], bonus.knockback() / 10.0);
            this.addModifier(Attributes.MOVEMENT_SPEED, lugares[i], bonus.speed() / 100.0);
            this.addModifier(Attributes.ATTACK_DAMAGE, lugares[i], bonus.damage());
        }
        this.setHealth(this.getMaxHealth());
    }

    private void addModifier(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> which,
                             String place, double amount) {
        if (amount == 0.0) return;
        AttributeInstance instance = this.getAttribute(which);
        if (instance == null) return;
        Identifier id = Identifier.fromNamespaceAndPath("thaumcraft", "minion_" + place.toLowerCase(java.util.Locale.ROOT));
        instance.removeModifier(id);
        instance.addPermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
    }

    private void clearPartModifiers() {
        for (var which : java.util.List.of(Attributes.MAX_HEALTH, Attributes.FOLLOW_RANGE,
                Attributes.KNOCKBACK_RESISTANCE, Attributes.MOVEMENT_SPEED, Attributes.ATTACK_DAMAGE)) {
            AttributeInstance instance = this.getAttribute(which);
            if (instance == null) continue;
            for (String place : new String[]{"head", "torso", "armleft", "armright", "legs"}) {
                instance.removeModifier(Identifier.fromNamespaceAndPath("thaumcraft", "minion_" + place));
            }
        }
    }

    // ----------------------------------------------------------------- o golpe

    /** Quantos passos ainda faltam do gesto de bater. */
    public int attackTimer() {
        return this.attackTimer;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.attackTimer > 0) this.attackTimer--;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
        this.attackTimer = 10;
        this.level().broadcastEntityEvent(this, (byte) 4);
        return super.doHurtTarget(level, target);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 4) this.attackTimer = 10;
        else super.handleEntityEvent(event);
    }

    // ----------------------------------------------------------------- o dono

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (this.isOwnedBy(player) && held.isEmpty() && !this.level().isClientSide()) {
            // sem nada na mão, o dono o manda sentar e levantar, como se faz com o lobo
            this.setOrderedToSit(!this.isOrderedToSit());
            this.jumping = false;
            this.navigation.stop();
            this.setTarget(null);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(MortuorumItems.BRAIN_ON_A_STICK);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        // o que se costura não se cria
        return null;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Parts", MinionParts.CODEC, this.parts());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read("Parts", MinionParts.CODEC).ifPresent(this::setParts);
    }

    /** O que o lacaio deixa cair: nada — o que ele era já foi tirado de outros. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, boolean recentlyHit) {
    }

    /** Quem o costurou pode montar nele, se ele tiver pernas de bicho grande. */
    public boolean rideable() {
        String legs = this.parts().legs();
        return legs.startsWith("cow") || legs.startsWith("pig") || legs.startsWith("iron_golem");
    }
}
