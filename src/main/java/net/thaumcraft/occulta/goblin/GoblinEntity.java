package net.thaumcraft.occulta.goblin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>goblin</b>: o {@code EntityGoblin} do Witchery.
 *
 * <p>Ele é a coisa mais estranha do mod, e a melhor: <b>um bicho que trabalha</b>. Não é domado, não é
 * invocado, não obedece — é <b>preso por uma corda</b>, e um goblin na corda faz três coisas que nenhum outro
 * bicho do jogo faz:
 *
 * <ol>
 *   <li><b>apanha o que está no chão</b> e o carrega;</li>
 *   <li><b>cava</b>, se lhe puserem uma picareta na mão;</li>
 *   <li>e <b>larga o que cavou</b> onde houver onde largar.</li>
 * </ol>
 *
 * <p>Solto, ele não faz nada disso: anda pela aldeia, entra em casa à noite, abre portas, e <b>comercia</b>.
 * É um aldeão de outra espécie — e <b>odeia os aldeões</b>, mas só quando há <b>três deles juntos</b>: um
 * goblin sozinho tem medo de tudo, e três têm coragem. É a mesma conta que decide se ele foge de gente.
 *
 * <p><b>Ele trepa paredes</b>, como uma aranha, e é por isso que uma cerca não o segura.
 *
 * <p><b>Fica de fora, declarado:</b> o <b>Koboldite</b> — o minério, a picareta e o que ela faz ao que se cava
 * — é uma linha de material inteira do original, e não está portada. O goblin cava com qualquer picareta; a
 * picareta de koboldite cavaria quinze vezes mais depressa e fundiria metade do minério, e isso fica para
 * quando a linha vier. Os dois <b>chefes</b> goblins (o Gulg e o Mog) pendem da mesma linha e da infusão, e
 * ficam com ela.
 */
public class GoblinEntity extends AgeableMob {
    /** Quantos goblins juntos bastam para eles terem coragem: os três do original. */
    public static final int CORAGEM = 3;

    /** E a que distância eles se contam. */
    public static final double PERTO = 8.0;

    /** Os quatro ofícios, que são só a cara dele. */
    public static final int OFÍCIOS = 4;

    /** O que ele está a fazer: nada, trabalhar ou adorar. */
    public static final byte PARADO = 0;
    public static final byte TRABALHANDO = 1;
    public static final byte ADORANDO = 2;

    private static final EntityDataAccessor<Integer> OFÍCIO =
            SynchedEntityData.defineId(GoblinEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> FAZENDO =
            SynchedEntityData.defineId(GoblinEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> NA_PAREDE =
            SynchedEntityData.defineId(GoblinEntity.class, EntityDataSerializers.BOOLEAN);

    public GoblinEntity(EntityType<? extends GoblinEntity> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.DOOR_OPEN, 0.0f);
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.DOOR_WOOD_CLOSED, 0.0f);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OFÍCIO, 0);
        builder.define(FAZENDO, PARADO);
        builder.define(NA_PAREDE, false);
    }

    /**
     * A mira do original, pela ordem dele.
     *
     * <p>A casa <b>dois</b> é a do trabalho — apanhar, largar e cavar —, e as três vontades dela só valem com
     * o goblin <b>na corda</b>. A casa três é a do medo, e ela <b>se desliga quando são três</b>.
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new GoblinPickUpGoal(this, GoblinPickUpGoal.ALCANCE));
        this.goalSelector.addGoal(2, new GoblinDropOffGoal(this, GoblinDropOffGoal.ALCANCE));
        this.goalSelector.addGoal(2, new GoblinDigGoal(this, GoblinDigGoal.ALCANCE));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class,
                8.0f, 0.6, 0.6, quem -> !this.temCoragem()));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this,
                net.thaumcraft.occulta.village.VillageGuardEntity.class,
                12.0f, 0.8, 0.8, quem -> !this.temCoragem()));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(6, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(7, new MoveTowardsRestrictionGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, GoblinEntity.class, 5.0f, 0.02f));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, LivingEntity.class, 8.0f));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Villager.class, 0,
                true, true, (quem, level) -> this.temCoragem()));
    }

    // ------------------------------------------------------------------ a conta da coragem

    /**
     * Se há <b>três goblins</b> a oito blocos: o {@code shouldAvoid} e o {@code isEntityApplicable} do
     * original, que são a mesma conta vista dos dois lados.
     *
     * <p>Sozinho, ele <b>foge</b> de gente e de guarda. Em três, ele <b>caça aldeão</b>. É a única coisa que
     * decide o que um goblin é, e é por isso que eles andam sempre em grupo.
     */
    public boolean temCoragem() {
        return this.level().getEntitiesOfClass(GoblinEntity.class,
                this.getBoundingBox().inflate(PERTO)).size() >= CORAGEM;
    }

    // ------------------------------------------------------------------ o que ele é e o que ele faz

    public int ofício() {
        return this.entityData.get(OFÍCIO);
    }

    public void ofício(int qual) {
        this.entityData.set(OFÍCIO, qual);
    }

    /** O que ele está a fazer: nada, trabalhar ou adorar. */
    public byte fazendo() {
        return this.entityData.get(FAZENDO);
    }

    public boolean trabalhando() {
        return this.fazendo() == TRABALHANDO;
    }

    public void trabalhando(boolean sim) {
        if (sim == this.trabalhando()) return;
        this.entityData.set(FAZENDO, sim ? TRABALHANDO : PARADO);
    }

    public boolean adorando() {
        return this.fazendo() == ADORANDO;
    }

    public void adorando(boolean sim) {
        if (sim == this.adorando()) return;
        this.entityData.set(FAZENDO, sim ? ADORANDO : PARADO);
    }

    /** <b>Ele trepa paredes</b>: é o mesmo laço da aranha, e por isso uma cerca não o segura. */
    @Override
    public boolean onClimbable() {
        return this.entityData.get(NA_PAREDE);
    }

    @Override
    public void tick() {
        super.tick();
        this.olhaAParede();
    }

    /**
     * Olha se ele está encostado a alguma coisa, e é isso que o faz trepar.
     *
     * <p>Corre <b>depois</b> de o bicho se mexer, como no original: a batida do jogo é que diz se ele bateu
     * numa parede, e só depois disso a resposta vale.
     */
    public void olhaAParede() {
        if (this.level().isClientSide()) return;
        this.entityData.set(NA_PAREDE, this.horizontalCollision);
    }

    /**
     * O gesto do original: <b>com ele na corda</b>, a mão troca de dono.
     *
     * <p>De mãos vazias, uma picareta que se lhe dê fica com ele — e ele põe-se a cavar. Com alguma coisa na
     * mão, clicar <b>tira-lha</b> e devolve-a a quem clicou. É assim que se manda num goblin: não há ordem, há
     * uma picareta.
     */
    @Override
    public InteractionResult mobInteract(Player quem, InteractionHand mão) {
        ItemStack naMão = quem.getItemInHand(mão);
        if (naMão.is(Items.LEAD)) return super.mobInteract(quem, mão);
        if (!this.isAlive() || this.isBaby() || quem.isShiftKeyDown()) {
            return super.mobInteract(quem, mão);
        }
        if (!this.isLeashed()) return super.mobInteract(quem, mão);
        if (this.level().isClientSide()) return InteractionResult.SUCCESS;

        ItemStack dele = this.getMainHandItem();
        if (dele.isEmpty()) {
            if (!naMão.is(net.minecraft.tags.ItemTags.PICKAXES)) {
                return InteractionResult.PASS;
            }
            this.setItemSlot(EquipmentSlot.MAINHAND, naMão.copy());
            this.setDropChance(EquipmentSlot.MAINHAND, 2.0f);
            naMão.setCount(0);
            return InteractionResult.SUCCESS;
        }

        if (!quem.getInventory().add(dele.copy())) quem.drop(dele.copy(), false);
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        return InteractionResult.SUCCESS;
    }

    // ------------------------------------------------------------------ o resto

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob outro) {
        var filho = net.thaumcraft.occulta.OccultaEntities.GOBLIN.create(level,
                net.minecraft.world.entity.EntitySpawnReason.BREEDING);
        if (filho != null) filho.ofício(this.getRandom().nextInt(OFÍCIOS));
        return filho;
    }

    @Override
    public @Nullable net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance quão,
            net.minecraft.world.entity.EntitySpawnReason razão,
            @Nullable net.minecraft.world.entity.SpawnGroupData dado) {
        var saída = super.finalizeSpawn(level, quão, razão, dado);
        this.ofício(level.getRandom().nextInt(OFÍCIOS));
        return saída;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return net.thaumcraft.occulta.OccultaSounds.GOBLIN_IDLE.value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource fonte) {
        return net.thaumcraft.occulta.OccultaSounds.GOBLIN_HIT.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return net.thaumcraft.occulta.OccultaSounds.GOBLIN_DEATH.value();
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 1.4f;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput saída) {
        super.addAdditionalSaveData(saída);
        saída.putInt("Profession", this.ofício());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput entrada) {
        super.readAdditionalSaveData(entrada);
        this.ofício(entrada.getIntOr("Profession", 0));
    }

    /** Quantos goblins há à volta daquele ponto, para quem precisar de contar sem ser um deles. */
    public static int quantosPerto(Level level, AABB onde) {
        return level.getEntitiesOfClass(GoblinEntity.class, onde).size();
    }
}
