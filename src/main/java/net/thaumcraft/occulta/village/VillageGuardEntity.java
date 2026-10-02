package net.thaumcraft.occulta.village;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Guarda da Aldeia</b>: o {@code EntityVillageGuard} do Witchery.
 *
 * <p>É a resposta do ofício a uma pergunta que o Minecraft nunca respondeu: <b>quem defende a aldeia de gente</b>.
 * O golem defende de monstro, e de quem a aldeia já odeia. O guarda é outra coisa — é um aldeão que pegou um arco,
 * mora na aldeia, anda por ela, abre e fecha as portas, e mata o que entrar.
 *
 * <p><b>Quarenta de vida e quatro de dano</b>, e briga de longe ou de perto conforme o que tem na mão: com arco
 * ele atira, sem arco ele avança. Nasce de couro, e <b>uma em cada cinco vezes</b> o peito e a cabeça vêm de
 * malha — é o que faz uma aldeia guardada parecer guardada de verdade, e não uniformizada.
 *
 * <p><b>Dois guardas nunca se batem</b>, e nenhum bate num creeper: é o {@code canAttackClass} do original, e a
 * razão do segundo é óbvia para quem já viu um creeper ao lado de uma casa.
 *
 * <p>Matar um guarda <b>custa reputação</b> na aldeia, como no original.
 *
 * <p><b>Dois tipos.</b> O comum, do tamanho de gente; e o <b>infernal</b>, um terço maior, imune ao fogo e com
 * flechas que pegam fogo. O original guarda isso num byte, e é o que se faz aqui.
 *
 * <p><b>Desvios declarados.</b>
 *
 * <ol>
 *   <li><b>O sangue fica de fora.</b> O guarda do original tem um poço de quinhentos de sangue, que serve de
 *       comida a vampiro. O sistema de vampiro não está portado; um poço de sangue sem quem o beba é peso morto,
 *       e volta quando o vampiro vier.</li>
 *   <li><b>A casa do guarda é um raio fixo.</b> O original pede à aldeia o centro e o tamanho dela, e prende o
 *       guarda a {@code tamanho × 1,5}. O jogo de hoje não tem objeto de aldeia nem tamanho de aldeia — tem
 *       lugares de interesse espalhados por seções. Aqui se acha a seção de aldeia mais perto e se prende o
 *       guarda a <b>quarenta e oito</b>, que é o que aquele produto dava numa aldeia de tamanho comum.</li>
 *   <li><b>A reputação é a de hoje.</b> O original tira cinco da reputação de quem o matou. Hoje reputação é
 *       fuxico de aldeão, e o que mais se parece com matar quem guarda a aldeia é o {@code GOLEM_KILLED}, que é
 *       o que se conta aos aldeões por perto.</li>
 *   <li><b>Não há o recolher-se à noite.</b> O original junta ao abrir portas um {@code EntityAIRestrictOpenDoor},
 *       que no jogo de então mandava o bicho ficar dentro de casa. Esse comportamento deixou de existir e não tem
 *       par; fica o abrir e fechar.</li>
 *   <li><b>O Caçador de Bruxas e o Goblin não entram na conta de quem é alvo</b>, porque não estão portados. No
 *       original o guarda poupa o Caçador (são do mesmo lado) e caça o Goblin mesmo não sendo monstro.</li>
 * </ol>
 */
public class VillageGuardEntity extends PathfinderMob implements RangedAttackMob {
    /** O guarda comum. */
    public static final byte NORMAL = 0;

    /** E o infernal: maior, imune ao fogo, e as flechas dele queimam. */
    public static final byte INFERNAL = 1;

    /** O tamanho do infernal, pelos números do original. */
    private static final float INFERNAL_WIDTH = 0.72f;
    private static final float INFERNAL_HEIGHT = 2.34f;

    /** A que distância, em seções, ele procura aldeia. */
    private static final int VILLAGE_SECTIONS = 2;

    /** E a que distância do centro dela ele fica preso. */
    private static final int HOME_RADIUS = 48;

    /** Quanto tempo ele queima quem atinge, quando é infernal: os cem tiques do original. */
    private static final int INFERNAL_BURN = 100;

    private static final EntityDataAccessor<Byte> GUARD_TYPE =
            SynchedEntityData.defineId(VillageGuardEntity.class, EntityDataSerializers.BYTE);

    /**
     * As duas brigas, guardadas para se poderem trocar quando a mão muda.
     *
     * <p><b>E nascem no {@code registerGoals}, e não aqui.</b> O {@code registerGoals} corre <b>dentro do
     * construtor do {@code Mob}</b> — quer dizer, antes de os campos desta classe serem atribuídos. Criá-las na
     * declaração punha {@code null} nas duas na hora em que a mira as pede.
     */
    private Goal deLonge;
    private Goal dePerto;

    /** De quantos em quantos tiques ele volta a perguntar onde é a aldeia. */
    private int procuraCasa;

    public VillageGuardEntity(EntityType<? extends VillageGuardEntity> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GUARD_TYPE, NORMAL);
    }

    /**
     * A mira do original, pela ordem dele.
     *
     * <p>A casa oito ficou vazia: era o {@code EntityAIRestrictOpenDoor}, que não tem par hoje.
     */
    @Override
    protected void registerGoals() {
        this.deLonge = new RangedAttackGoal(this, 1.0, 20, 60, 15.0f);
        this.dePerto = new MeleeAttackGoal(this, 1.2, false);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 0.6, true, 4, () -> true));
        this.goalSelector.addGoal(7, new MoveTowardsRestrictionGoal(this, 1.0));
        this.goalSelector.addGoal(9, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(10, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(12, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new DefendVillageGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0, false, true,
                (quem, level) -> this.ehAlvo(quem)));
        this.arrumaBriga();
    }

    // ------------------------------------------------------------------ quem ele mata

    /**
     * Quem o guarda caça: o {@code func_82704_a} do original.
     *
     * <p>Monstro, sim. E <b>gente que está batendo em gente da aldeia</b> — é a única forma de um jogador entrar
     * nesta lista sem ser por reputação, e é o que faz o guarda guardar quem mora ali.
     */
    private boolean ehAlvo(LivingEntity quem) {
        if (quem instanceof Enemy) return true;
        if (quem instanceof Player gente) {
            LivingEntity vítima = gente.getLastHurtMob();
            return vítima instanceof Villager || vítima instanceof VillageGuardEntity;
        }
        return false;
    }

    /** Dois guardas não se batem, e ninguém bate num creeper: o {@code canAttackClass} do original. */
    @Override
    public boolean canAttack(LivingEntity quem) {
        if (quem instanceof Creeper || quem instanceof VillageGuardEntity) return false;
        return super.canAttack(quem);
    }

    /** E nem se ferem uns aos outros. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float dano) {
        if (fonte.getEntity() instanceof VillageGuardEntity) return false;
        return super.hurtServer(level, fonte, dano);
    }

    // ------------------------------------------------------------------ o tipo

    public byte guardType() {
        return this.entityData.get(GUARD_TYPE);
    }

    /** Trocar o tipo troca o tamanho e a relação com o fogo, como no original. */
    public void setGuardType(byte qual) {
        this.entityData.set(GUARD_TYPE, qual);
        this.refreshDimensions();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> qual) {
        super.onSyncedDataUpdated(qual);
        if (GUARD_TYPE.equals(qual)) this.refreshDimensions();
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        if (this.guardType() == INFERNAL) {
            return EntityDimensions.scalable(INFERNAL_WIDTH, INFERNAL_HEIGHT)
                    .withEyeHeight(INFERNAL_HEIGHT * 0.9f);
        }
        return super.getDefaultDimensions(pose);
    }

    @Override
    public boolean fireImmune() {
        return this.guardType() == INFERNAL || super.fireImmune();
    }

    // ------------------------------------------------------------------ a aldeia é a casa dele

    /**
     * De quando em quando ele pergunta onde é a aldeia, e se prende a ela: o {@code updateAITasks} do original.
     *
     * <p>Sem aldeia por perto ele se solta e anda à vontade. Com aldeia, <b>cura um de vida</b> em cada volta
     * destas em que não tem ninguém para matar — é o que o original faz, e é o que mantém de pé uma guarda que
     * levou tiro ontem.
     */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (--this.procuraCasa <= 0) {
            this.procuraCasa = 70 + this.random.nextInt(50);
            BlockPos onde = this.blockPosition();
            SectionPos minha = SectionPos.of(onde);
            SectionPos aldeia = BehaviorUtils.findSectionClosestToVillage(level, minha, VILLAGE_SECTIONS);
            if (!level.isVillage(onde) && aldeia == minha) {
                this.clearHome();
            } else {
                this.setHomeTo(aldeia.center(), HOME_RADIUS);
                if (this.getTarget() == null) this.heal(1.0f);
            }
        }
        super.customServerAiStep(level);
    }

    // ------------------------------------------------------------------ o arco e a mão

    /**
     * Com arco atira, sem arco avança: o {@code setCombatTask} do original.
     *
     * <p>E ele volta a decidir isto <b>toda vez que a mão muda</b>, e não só ao nascer — desarmar um guarda
     * fá-lo avançar.
     */
    public void arrumaBriga() {
        // o deLonge nulo quer dizer que ainda não se passou pelo registerGoals, e aí não há mira a arrumar
        if (this.level().isClientSide() || this.deLonge == null) return;
        this.goalSelector.removeGoal(this.deLonge);
        this.goalSelector.removeGoal(this.dePerto);
        this.goalSelector.addGoal(4, this.getMainHandItem().is(Items.BOW) ? this.deLonge : this.dePerto);
    }

    @Override
    public void setItemSlot(EquipmentSlot casa, ItemStack coisa) {
        super.setItemSlot(casa, coisa);
        if (casa == EquipmentSlot.MAINHAND) this.arrumaBriga();
    }

    /**
     * Se o que está posto é a briga de longe: o que o {@code setCombatTask} decidiu, visto de fora.
     *
     * <p>Serve às provas, que de outro jeito só podiam julgar isto pelo que aparece no ar.
     */
    public boolean aimingFromAfar() {
        return this.deLonge != null && this.goalSelector.getAvailableGoals().stream()
                .anyMatch(posta -> posta.getGoal() == this.deLonge);
    }

    /**
     * A flecha do guarda, com os números do original: velocidade 1,6 e a mira que aperta com a dificuldade.
     *
     * <p>O infernal põe fogo nela.
     */
    @Override
    public void performRangedAttack(LivingEntity alvo, float força) {
        AbstractArrow tiro = new Arrow(this.level(), this, new ItemStack(Items.ARROW), this.getMainHandItem());
        double dx = alvo.getX() - this.getX();
        double dy = alvo.getY(0.3333333333333333) - tiro.getY();
        double dz = alvo.getZ() - this.getZ();
        double plano = Math.sqrt(dx * dx + dz * dz);
        tiro.shoot(dx, dy + plano * 0.2, dz, 1.6f, 16 - this.level().getDifficulty().getId() * 4);
        if (this.guardType() == INFERNAL) tiro.igniteForTicks(INFERNAL_BURN);
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0f,
                1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.level().addFreshEntity(tiro);
    }

    // ------------------------------------------------------------------ nascer, morrer e guardar

    /** O arco, o couro, e a malha que vem de vez em quando: o {@code addRandomArmor} do original. */
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource sorte, DifficultyInstance quão) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(
                sorte.nextInt(5) == 0 ? Items.CHAINMAIL_CHESTPLATE : Items.LEATHER_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(
                sorte.nextInt(5) == 0 ? Items.CHAINMAIL_HELMET : Items.LEATHER_HELMET));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance quão,
                                                  EntitySpawnReason razão, @Nullable SpawnGroupData dado) {
        SpawnGroupData saída = super.finalizeSpawn(level, quão, razão, dado);
        this.populateDefaultEquipmentSlots(level.getRandom(), quão);
        this.arrumaBriga();
        return saída;
    }

    /**
     * Quem o matou fica malvisto na aldeia, como no original.
     *
     * <p>E se conta como <b>morte de aldeão</b>, que é o que ele é: no original um guarda nasce de um aldeão que
     * pegou um arco — o {@code createFrom(EntityVillager)} —, e por isso matá-lo pesa como matar quem mora ali.
     *
     * <p><b>E não como morte de golem</b>, que era o que parecia certo e não é: o {@code GOLEM_KILLED} existe no
     * jogo de hoje, mas aparece <b>uma única vez em todo o código, na própria declaração</b>. Nada o dispara e
     * nada o trata — o {@code onReputationEventFrom} do aldeão só conhece quatro eventos, e esse não é um deles.
     * É constante morta, e usá-la seria escrever uma linha que não faz nada.
     */
    @Override
    public void die(DamageSource fonte) {
        if (this.level() instanceof ServerLevel level && fonte.getEntity() instanceof Player quem) {
            AABB roda = this.getBoundingBox().inflate(16.0, 8.0, 16.0);
            for (Villager aldeão : level.getEntitiesOfClass(Villager.class, roda)) {
                level.onReputationEvent(ReputationEventType.VILLAGER_KILLED, quem, aldeão);
            }
        }
        super.die(fonte);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput saída) {
        super.addAdditionalSaveData(saída);
        saída.putByte("GuardType", this.guardType());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput entrada) {
        super.readAdditionalSaveData(entrada);
        this.setGuardType(entrada.getByteOr("GuardType", NORMAL));
        this.arrumaBriga();
    }

    // ------------------------------------------------------------------ a voz

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource fonte) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    /** A voz dele é a do aldeão, um pouco mais grave: os 0,8 do original. */
    @Override
    public float getVoicePitch() {
        return 0.8f;
    }
}
