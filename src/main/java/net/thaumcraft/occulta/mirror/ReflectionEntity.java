package net.thaumcraft.occulta.mirror;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaEntities;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * O Reflexo: o {@code EntityReflection} do Witchery — o demônio que mora no espelho.
 *
 * <p>Quem passa à frente de um espelho <b>habitado</b> cai na cela dele no Mundo do Espelho, e é ele que a guarda.
 * E ele a guarda <b>com a cara de quem entrou</b>: veste a armadura dessa pessoa, pega na melhor arma que ela
 * trouxer e copia os efeitos que ela tiver. Bater nele é bater em si mesmo.
 *
 * <p>Cem de vida, e <b>nenhuma pancada lhe tira mais de seis</b>: não se mata num golpe, mata-se com paciência.
 * Morto ele, o espelho de onde se veio fica <b>vazado</b> — e passa a ser ponte.
 *
 * <p><b>Do original ficam de fora, declarados:</b> os feitiços da Vara Mística, que ele lança de longe — a Vara e
 * o sistema de símbolos são fatia à parte, e sem eles ele briga de perto ou de arco, como qualquer um; e a forma
 * de lobisomem, que ele toma quando quem entrou for lobisomem, por o lobisomem não estar portado.
 */
public class ReflectionEntity extends Monster implements RangedAttackMob {
    /** O mais que uma pancada lhe tira: os seis do original. */
    public static final float DAMAGE_CAP = 6.0f;

    /** De quantas em quantas batidas ele se olha ao espelho de quem está perto. */
    public static final int COPY_EVERY = 30;

    /** E de quantas em quantas ele copia os efeitos. */
    public static final int EFFECTS_EVERY = 60;

    /** O tamanho da cela onde ele procura quem copiar: os dez por oito do original. */
    public static final double NEAR = 10.0;
    public static final double NEAR_Y = 8.0;

    /** E o da caixa em que se conta se ainda há Reflexo numa cela: os sete por seis. */
    public static final double CELL = 7.0;
    public static final double CELL_Y = 6.0;

    /** De quem ele é o reflexo, para o desenhista saber que nome lhe pôr. */
    private static final EntityDataAccessor<String> OWNER =
            SynchedEntityData.defineId(ReflectionEntity.class, EntityDataSerializers.STRING);

    private @Nullable UUID owner;
    private boolean ranged;

    public ReflectionEntity(EntityType<? extends ReflectionEntity> type, Level level) {
        super(type, level);
        this.xpReward = 50;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 50.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER, "");
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    public String ownerName() {
        return this.entityData.get(OWNER);
    }

    // ------------------------------------------------------------------ acordar e morrer

    /**
     * Acorda o Reflexo da cela daquele espelho selado, se ainda não houver um lá: o pedaço do
     * {@code entityInside} do original que põe o demônio na cela antes de mandar a pessoa para ela.
     */
    public static @Nullable ReflectionEntity wake(ServerLevel mundo, BlockPos espelhoDaCela) {
        BlockPos meio = MirrorWorld.cellMiddle(espelhoDaCela);
        AABB cela = new AABB(meio.getX() - CELL, meio.getY() - CELL_Y, meio.getZ() - CELL,
                meio.getX() + CELL, meio.getY() + CELL_Y, meio.getZ() + CELL);
        if (!mundo.getEntitiesOfClass(ReflectionEntity.class, cela).isEmpty()) return null;

        ReflectionEntity reflexo = OccultaEntities.REFLECTION.create(mundo, EntitySpawnReason.TRIGGERED);
        if (reflexo == null) return null;
        reflexo.snapTo(meio.getX() + 0.5, meio.getY() + 1.1, meio.getZ() + 0.5, 0.0f, 0.0f);
        reflexo.setPersistenceRequired();
        mundo.addFreshEntity(reflexo);
        return reflexo;
    }

    /**
     * Morto o último Reflexo de uma cela, o espelho do outro lado fica vazado: o {@code demonSlain} do original.
     *
     * <p>Olha-se o espelho selado da cela onde ele caiu, lê-se a ligação dele e se põe a marca no espelho de lá.
     */
    public static void demonSlain(ServerLevel mundo, BlockPos onde) {
        if (!MirrorWorld.is(mundo)) return;
        BlockPos espelhoDaCela = MirrorWorld.cellMirror(onde);
        if (!mundo.getBlockState(espelhoDaCela).is(OccultaBlocks.SEALED_WITCH_MIRROR)) return;

        BlockPos meio = MirrorWorld.cellMiddle(espelhoDaCela);
        AABB cela = new AABB(meio.getX() - CELL, meio.getY() - CELL_Y, meio.getZ() - CELL,
                meio.getX() + CELL, meio.getY() + CELL_Y, meio.getZ() + CELL);
        for (ReflectionEntity outro : mundo.getEntitiesOfClass(ReflectionEntity.class, cela)) {
            if (outro.isAlive()) return;
        }

        if (!(mundo.getBlockEntity(espelhoDaCela) instanceof MirrorBlockEntity alma)) return;
        MirrorLink link = alma.link();
        if (link == null) return;
        MirrorBlockEntity lá = MirrorBlockEntity.other(mundo, link);
        if (lá != null) lá.setHollow(true);
    }

    @Override
    public void die(DamageSource fonte) {
        super.die(fonte);
        if (this.level() instanceof ServerLevel mundo) demonSlain(mundo, this.blockPosition());
    }

    // ------------------------------------------------------------------ o espelho de quem ele é

    /** Nenhuma pancada lhe tira mais de seis: o {@code attackEntityFrom} do original. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float quanto) {
        return super.hurtServer(level, fonte, Math.min(quanto, DAMAGE_CAP));
    }

    /**
     * O {@code onLivingUpdate}: de trinta em trinta batidas ele volta a copiar quem está perto.
     *
     * <p>Fora do Mundo do Espelho ele não tem razão de existir, e desaparece — como no original.
     */
    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel mundo)) return;
        if (this.tickCount % COPY_EVERY != 1) return;
        if (!MirrorWorld.is(mundo)) {
            this.discard();
            return;
        }
        this.copyNearest(mundo);
    }

    /** Veste-se do mais perto: armadura, a melhor arma da barra e, de minuto a minuto, os efeitos. */
    private void copyNearest(ServerLevel mundo) {
        AABB perto = new AABB(this.getX() - NEAR, this.getY() - NEAR_Y, this.getZ() - NEAR,
                this.getX() + NEAR, this.getY() + NEAR_Y, this.getZ() + NEAR);
        Player quem = null;
        double distância = Double.MAX_VALUE;
        for (Player outro : mundo.getEntitiesOfClass(Player.class, perto)) {
            double agora = outro.distanceToSqr(this);
            if (quem != null && agora >= distância) continue;
            quem = outro;
            distância = agora;
        }

        if (quem == null) {
            this.entityData.set(OWNER, "");
            this.owner = null;
            for (EquipmentSlot casa : EquipmentSlot.values()) this.setItemSlot(casa, ItemStack.EMPTY);
            this.ranged = false;
            return;
        }

        this.owner = quem.getUUID();
        this.entityData.set(OWNER, quem.getGameProfile().name());
        for (EquipmentSlot casa : EquipmentSlot.values()) {
            if (casa.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            this.setItemSlot(casa, quem.getItemBySlot(casa).copy());
        }

        ItemStack arma = bestWeapon(quem);
        this.setItemSlot(EquipmentSlot.MAINHAND, arma.copy());
        boolean deLonge = arma.getItem() instanceof BowItem || arma.is(Items.CROSSBOW);
        if (deLonge != this.ranged) {
            this.ranged = deLonge;
            this.goalSelector.removeAllGoals(goal -> goal instanceof MeleeAttackGoal
                    || goal instanceof RangedBowAttackGoal);
            this.goalSelector.addGoal(2, deLonge ? new RangedBowAttackGoal<>(this, 1.0, 20, 15.0f)
                    : new MeleeAttackGoal(this, 1.2, false));
        }

        if (this.tickCount % EFFECTS_EVERY != 1) return;
        this.removeAllEffects();
        for (MobEffectInstance efeito : quem.getActiveEffects()) {
            this.addEffect(new MobEffectInstance(efeito));
        }
    }

    /** A melhor arma da barra de quem se copia: a que mais dano soma, como no original. */
    private static ItemStack bestWeapon(Player quem) {
        ItemStack melhor = quem.getMainHandItem();
        double maior = 0.0;
        for (int casa = 0; casa < 9; casa++) {
            ItemStack item = quem.getInventory().getItem(casa);
            if (item.isEmpty()) continue;
            double dano = damageOf(item);
            if (dano <= maior) continue;
            maior = dano;
            melhor = item;
        }
        return melhor;
    }

    /** Quanto dano uma coisa soma: o que os modificadores dela dizem. */
    private static double damageOf(ItemStack item) {
        var atributos = item.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS);
        if (atributos == null) return 0.0;
        double soma = 0.0;
        for (var entrada : atributos.modifiers()) {
            if (!entrada.attribute().is(Attributes.ATTACK_DAMAGE.unwrapKey().orElseThrow())) continue;
            if (entrada.modifier().operation() != net.minecraft.world.entity.ai.attributes.AttributeModifier
                    .Operation.ADD_VALUE) {
                continue;
            }
            soma += entrada.modifier().amount();
        }
        return soma;
    }

    @Override
    public void performRangedAttack(net.minecraft.world.entity.LivingEntity alvo, float força) {
        ItemStack arco = this.getMainHandItem();
        AbstractArrow tiro = new net.minecraft.world.entity.projectile.arrow.Arrow(
                this.level(), this, new ItemStack(Items.ARROW), arco);
        double dx = alvo.getX() - this.getX();
        double dy = alvo.getY(0.3333333333333333) - tiro.getY();
        double dz = alvo.getZ() - this.getZ();
        double plano = Math.sqrt(dx * dx + dz * dz);
        tiro.shoot(dx, dy + plano * 0.20000000298023224, dz, 1.6f,
                14 - this.level().getDifficulty().getId() * 4);
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.level().addFreshEntity(tiro);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("owner_name", this.ownerName());
        if (this.owner != null) output.store("owner", net.minecraft.core.UUIDUtil.CODEC, this.owner);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(OWNER, input.getStringOr("owner_name", ""));
        this.owner = input.read("owner", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
    }

    /** Quantos Reflexos vivos há naquela cela: serve às provas e ao {@code demonSlain}. */
    public static List<ReflectionEntity> inCell(ServerLevel mundo, BlockPos espelhoDaCela) {
        BlockPos meio = MirrorWorld.cellMiddle(espelhoDaCela);
        return mundo.getEntitiesOfClass(ReflectionEntity.class,
                new AABB(meio.getX() - CELL, meio.getY() - CELL_Y, meio.getZ() - CELL,
                        meio.getX() + CELL, meio.getY() + CELL_Y, meio.getZ() + CELL));
    }
}
