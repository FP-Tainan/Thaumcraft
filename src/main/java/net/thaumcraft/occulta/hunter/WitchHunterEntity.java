package net.thaumcraft.occulta.hunter;

import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.occulta.OccultaItems;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * O <b>Caçador de Bruxas</b>: o {@code EntityWitchHunter} do Witchery.
 *
 * <p>Ele é a <b>resposta do mundo ao ofício</b>. Não nasce de ódio nem de escuridão: nasce porque <b>alguém
 * fez magia negra</b> — e vem atrás dessa pessoa pelo nome.
 *
 * <p><b>O que ele caça</b> é uma lista curta e muito clara: morto-vivo, coisa do inferno, bruxa, lobisomem — e
 * <b>gente</b>, mas só a que for bruxa, lobisomem, vampira, ou <b>a que ele veio buscar</b>. Um caçador não
 * mata quem passa: ele é um homem com um trabalho.
 *
 * <p><b>Nenhuma pancada lhe tira mais de nove.</b> Não é vida que o faz durar — são trinta — é o teto, e é o
 * mesmo truque da Baba Yaga. E ele <b>não se fere</b> por mão de guarda de aldeia nem de outro caçador: eles
 * são do mesmo lado.
 *
 * <p><b>E o veneno não pega nele.</b> De segundo em segundo ele se limpa — é a primeira coisa que uma bruxa
 * tenta, e a primeira que não funciona.
 *
 * <p>Dele caem <b>virotes de madeira</b>, e de vez em quando dois anuladores: quem o mata fica com a munição
 * dele, e é assim que a besta se enche sem se fabricar nada.
 *
 * <p><b>Traduções declaradas:</b>
 * <ol>
 *   <li>O original guarda o <b>nome</b> de quem ele veio buscar; aqui se guarda o <b>UUID</b>, pela mesma
 *       razão que no Escravizado — um nome muda.</li>
 *   <li>O original tem <b>três peles</b> sorteadas ao nascer, e fica igual.</li>
 *   <li>O <b>vampirismo de jogador</b> é a outra razão por que ele aparece no original: um vampiro de grau
 *       dez, malvisto numa aldeia, atrai caçadores. Isso pede a vampirice de jogador, que este porte ainda não
 *       tem, e está marcado em {@link WitchHunters}.</li>
 * </ol>
 */
public class WitchHunterEntity extends PathfinderMob implements RangedAttackMob {
    /** A vida dele: os trinta do original. */
    public static final double VIDA = 30.0;

    /** E o teto da pancada: nove, e nem mais um. */
    public static final float TETO_DA_PANCADA = 9.0f;

    /** De quanto em quanto ele se limpa do veneno. */
    public static final int LIMPA_O_VENENO = 20;

    /** Quantas peles ele tem. */
    public static final int QUANTAS_PELES = 3;

    /** A velocidade do tiro, e a mira que aperta com a dificuldade. */
    private static final float VELOCIDADE = 1.6f;
    private static final float ERRO = 14.0f;

    /** Uma vez em três, o virote pega fogo contra vampiro; e uma em quatro ele sai anulador. */
    public static final int FOGO_CONTRA_VAMPIRO = 3;
    public static final int CHANCE_DO_ANULADOR = 4;

    private static final net.minecraft.network.syncher.EntityDataAccessor<Byte> PELE =
            net.minecraft.network.syncher.SynchedEntityData.defineId(WitchHunterEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.BYTE);

    /** Quem ele veio buscar, se veio buscar alguém. */
    @Nullable
    private UUID quemEleQuer;

    /**
     * As duas brigas, criadas no {@code registerGoals} e não aqui — pela mesma razão do Guarda da Aldeia: o
     * {@code registerGoals} corre dentro do construtor do {@code Mob}, antes de os campos desta classe
     * existirem.
     */
    private Goal deLonge;
    private Goal dePerto;

    public WitchHunterEntity(EntityType<? extends WitchHunterEntity> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PELE, (byte) 0);
    }

    /** A mira do original, pela ordem dele. */
    @Override
    protected void registerGoals() {
        this.deLonge = new RangedAttackGoal(this, 1.0, 20, 60, 15.0f);
        this.dePerto = new MeleeAttackGoal(this, 1.2, false);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0,
                false, true, (quem, level) -> this.éCaça(quem)));
        this.arrumaBriga();
    }

    // ------------------------------------------------------------------ quem ele caça

    /**
     * O {@code isEntityApplicable} do original: a lista do que um caçador de bruxas caça.
     *
     * <p>Repare no que <b>não</b> está nela: aldeão, bicho, creeper, esqueleto comum. Ele não é um monstro —
     * é um homem com um trabalho, e o trabalho é curto.
     */
    public boolean éCaça(@Nullable LivingEntity quem) {
        if (quem == null) return false;
        if (quem.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) return true;
        if (quem instanceof Witch) return true;
        if (quem instanceof net.thaumcraft.occulta.coven.CovenWitchEntity) return true;
        if (net.thaumcraft.occulta.wolf.Lycanthropy.é(quem)) return true;
        if (net.thaumcraft.occulta.vampire.Vampirism.é(quem)) return true;

        if (!(quem instanceof Player gente)) return false;
        return this.quemEleQuer != null && this.quemEleQuer.equals(gente.getUUID());
    }

    /** Quem ele veio buscar. */
    @Nullable
    public UUID quemEleQuer() {
        return this.quemEleQuer;
    }

    /** E a quem ele passa a vir buscar. */
    public void vemBuscar(@Nullable Player quem) {
        this.quemEleQuer = quem == null ? null : quem.getUUID();
    }

    // ------------------------------------------------------------------ o que ele leva e o que ele aguenta

    /** A pele dele: uma das três do original. */
    public int pele() {
        return this.entityData.get(PELE);
    }

    public void pele(int qual) {
        this.entityData.set(PELE, (byte) qual);
    }

    /**
     * <b>Nenhuma pancada lhe tira mais de nove</b> — e a de um guarda de aldeia ou de outro caçador não lhe
     * tira nada.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float quanto) {
        if (doMesmoLado(fonte)) return false;
        return super.hurtServer(level, fonte, Math.min(quanto, TETO_DA_PANCADA));
    }

    /** Se quem bateu é do mesmo lado: o guarda da aldeia e o outro caçador. */
    public static boolean doMesmoLado(DamageSource fonte) {
        var quem = fonte.getEntity();
        return quem instanceof WitchHunterEntity
                || quem instanceof net.thaumcraft.occulta.village.VillageGuardEntity;
    }

    /** O veneno não pega nele: o {@code onLivingUpdate} do original, de segundo em segundo. */
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        if (this.tickCount % LIMPA_O_VENENO != 2) return;
        if (this.hasEffect(MobEffects.POISON)) this.removeEffect(MobEffects.POISON);
    }

    /** Com besta atira, sem besta avança: o {@code setCombatTask} do original. */
    public void arrumaBriga() {
        if (this.level().isClientSide() || this.deLonge == null) return;
        this.goalSelector.removeGoal(this.deLonge);
        this.goalSelector.removeGoal(this.dePerto);
        this.goalSelector.addGoal(4,
                this.getMainHandItem().is(OccultaItems.CROSSBOW_PISTOL) ? this.deLonge : this.dePerto);
    }

    @Override
    public void setItemSlot(EquipmentSlot casa, ItemStack coisa) {
        super.setItemSlot(casa, coisa);
        if (casa == EquipmentSlot.MAINHAND) this.arrumaBriga();
    }

    /** Se o que está posto é a briga de longe, para as provas poderem perguntar. */
    public boolean aimingFromAfar() {
        return this.deLonge != null && this.goalSelector.getAvailableGoals().stream()
                .anyMatch(posta -> posta.getGoal() == this.deLonge);
    }

    /**
     * O virote dele, e <b>o virote certo para o que ele tem pela frente</b>: prata contra lobisomem, osso
     * contra morto-vivo, e — uma vez em quatro — o anulador contra tudo o mais.
     *
     * <p>É a coisa que mais o faz parecer gente: ele <b>escolhe a munição</b>, e escolhe certo.
     */
    @Override
    public void performRangedAttack(LivingEntity alvo, float força) {
        int tipo = BoltEntity.ESTACA;
        if (net.thaumcraft.occulta.wolf.Lycanthropy.é(alvo)) {
            tipo = BoltEntity.PRATA;
        } else if (alvo.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) {
            tipo = BoltEntity.SAGRADO;
        } else if (this.getRandom().nextInt(CHANCE_DO_ANULADOR) == 0) {
            tipo = BoltEntity.DRENAGEM_FORTE;
        }

        BoltEntity tiro = new BoltEntity(this.level(), this, new ItemStack(BoltEntity.munição(tipo)),
                this.getMainHandItem(), tipo);
        int dureza = this.level().getDifficulty().getId();
        tiro.setBaseDamage(força * 2.0f + this.getRandom().nextGaussian() * 0.25 + dureza * 0.11f);

        // e contra vampiro ele põe fogo, uma vez em três
        if (net.thaumcraft.occulta.vampire.Vampirism.é(alvo)
                && this.getRandom().nextInt(FOGO_CONTRA_VAMPIRO) == 0) {
            tiro.igniteForTicks(100);
        }

        double dx = alvo.getX() - this.getX();
        double dy = alvo.getY(0.3333333333333333) - tiro.getY();
        double dz = alvo.getZ() - this.getZ();
        double plano = Math.sqrt(dx * dx + dz * dz);
        tiro.shoot(dx, dy + plano * 0.2, dz, VELOCIDADE, ERRO - dureza * 4.0f);

        this.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0f,
                1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.level().addFreshEntity(tiro);
    }

    // ------------------------------------------------------------------ nascer, morrer e guardar

    /**
     * O que ele leva: a besta, e mais nada.
     *
     * <p><b>As roupas dele estão pintadas na pele</b>, e não vestidas — o {@code ModelWitchHunter} tem o
     * casaco e o chapéu como caixas próprias, e as três peles do original já os trazem. Vesti-lo com as peças
     * do conjunto punha duas camadas de casaco uma por cima da outra.
     *
     * <p>E a besta <b>não cai</b>: o que cai dele são os virotes.
     */
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource sorte, DifficultyInstance quão) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(OccultaItems.CROSSBOW_PISTOL));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance quão,
                                                  EntitySpawnReason razão, @Nullable SpawnGroupData dado) {
        SpawnGroupData saída = super.finalizeSpawn(level, quão, razão, dado);
        this.pele(level.getRandom().nextInt(QUANTAS_PELES));
        this.populateDefaultEquipmentSlots(level.getRandom(), quão);
        this.arrumaBriga();
        return saída;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource fonte) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput saída) {
        super.addAdditionalSaveData(saída);
        saída.putByte("HunterType", (byte) this.pele());
        if (this.quemEleQuer != null) saída.store("HunterTarget", UUIDUtil.CODEC, this.quemEleQuer);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput entrada) {
        super.readAdditionalSaveData(entrada);
        this.pele(entrada.getByteOr("HunterType", (byte) 0));
        this.quemEleQuer = entrada.read("HunterTarget", UUIDUtil.CODEC).orElse(null);
        this.arrumaBriga();
    }
}
