package net.thaumcraft.occulta.vampire;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.occulta.BloodCrucibleBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Vampiro</b>: o {@code EntityVampire} do Witchery.
 *
 * <p>Ele é o contrário do Lobisomem em tudo. O lobisomem é um aldeão doente, de força bruta, que a prata
 * resolve; o vampiro é uma coisa que <b>pensa</b>: tem casa, tem rotina, e tem um plano.
 *
 * <h2>A rotina dele</h2>
 *
 * <ol>
 *   <li><b>De noite</b>, se não tem aldeia, procura uma a <b>cento e vinte e oito blocos</b> e vai para lá —
 *       num sopro de fumaça, sem andar o caminho.</li>
 *   <li><b>Lá, bebe.</b> Cada mordida num aldeão tem uma chance em dez de ser um gole de verdade: ele se cura
 *       de quatro, e conta quatro para o jantar dele.</li>
 *   <li><b>Cheio</b> — vinte —, volta ao caixão e <b>enche um Crisol de Sangue</b> que esteja a seis blocos
 *       dele. É para isso que o crisol existe, e é por isso que ele estava no porte à espera.</li>
 *   <li><b>De dia</b> volta ao caixão de qualquer maneira, esquece a aldeia, e <b>pega fogo</b> se o sol o
 *       apanhar.</li>
 * </ol>
 *
 * <h2>E ele quase não morre</h2>
 *
 * <p>Esta é a outra metade do bicho, e vem do {@code checkForVampireDeath}: uma espada <b>não o mata</b>. Ele
 * leva o dano, cai a zero de vida — e não morre. Só morrem de verdade:
 *
 * <ul>
 *   <li><b>fogo</b>, venha ele de onde vier — e o sol é fogo;</li>
 *   <li><b>sufocar</b> numa parede, ou cair no vazio;</li>
 *   <li>e a mão de outro <b>vampiro</b>, de um <b>lobisomem</b> ou de um <b>chefe</b>.</li>
 * </ul>
 *
 * <p>Quem quiser matar um vampiro com uma espada tem de o prender ao sol, e isso é a coisa mais vampiro que
 * este mod faz.
 */
public class VampireEntity extends PathfinderMob {
    /** Quanto ele precisa beber antes de ir para casa: os vinte do original. */
    public static final float JANTAR = 20.0f;

    /** O que cada gole vale, e o que ele cura. */
    public static final float POR_GOLE = 4.0f;

    /** Uma mordida em cada dez é um gole. */
    public static final int CHANCE_DO_GOLE = 10;

    /** De quanto em quanto ele procura aldeia, e até onde. */
    public static final int PROCURA_ALDEIA = 500;
    public static final int ALCANCE_DA_ALDEIA = 8;

    /** De quanto em quanto ele olha o sol, e quantos segundos arde. */
    public static final int OLHA_O_SOL = 20;
    public static final int ARDE = 2;

    /** E até onde ele procura o crisol, a partir do caixão. */
    public static final int ACHA_O_CRISOL = 6;

    @Nullable
    private BlockPos caixão;
    @Nullable
    private BlockPos aldeia;
    private float bebido;

    public VampireEntity(EntityType<? extends VampireEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder attributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RestrictSunGoal(this));
        this.goalSelector.addGoal(3, new FleeSunGoal(this, 1.0));
        this.goalSelector.addGoal(8, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(9, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(10, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(12, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Villager.class, 0,
                false, true, (quem, onde) -> this.aldeia != null));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0,
                false, true, (quem, onde) -> !Vampirism.é(quem)));
    }

    /**
     * Ele é <b>morto-vivo</b>, e o jogo de hoje diz isso por uma etiqueta e não por um método: o tipo dele
     * entra em {@code minecraft:undead}, no arquivo de dados — e com isso a poção de cura fere, a de veneno
     * não pega, e a espada de Fio da Ruína morde.
     */
    public static final net.minecraft.tags.TagKey<net.minecraft.world.entity.EntityType<?>> MORTO_VIVO =
            net.minecraft.tags.EntityTypeTags.UNDEAD;

    /** Onde ele dorme. É posto onde ele nasce. */
    public BlockPos caixão() {
        return this.caixão == null ? this.blockPosition() : this.caixão;
    }

    public void caixão(BlockPos onde) {
        this.caixão = onde;
    }

    public float bebido() {
        return this.bebido;
    }

    @Nullable
    public BlockPos aldeia() {
        return this.aldeia;
    }

    @Override
    public void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.caixão == null) this.caixão = this.blockPosition();

        if (level.isBrightOutside()) {
            this.deDia(level);
            return;
        }
        if (this.bebido >= JANTAR) {
            this.cheio(level);
            return;
        }
        if (this.aldeia == null && this.tickCount % PROCURA_ALDEIA == 2) {
            this.procuraAldeia(level);
        }
    }

    /** De dia ele esquece a aldeia, volta ao caixão, e arde se o sol o apanhar. */
    private void deDia(ServerLevel level) {
        if (this.getTarget() != null) this.setTarget(null);

        if (this.tickCount % 100 == 2) {
            this.aldeia = null;
            this.bebido = 0.0f;
            if (this.caixão().distToCenterSqr(this.getX(), this.getY(), this.getZ()) > 16.0) this.vaiPara(level, this.caixão());
        }
        if (this.tickCount % OLHA_O_SOL == 2 && this.aoSol(level)) {
            this.igniteForSeconds(ARDE);
        }
    }

    /** Cheio, volta ao caixão e enche o crisol. */
    private void cheio(ServerLevel level) {
        if (this.aldeia == null) return;
        this.setTarget(null);
        this.setLastHurtByMob(null);
        this.aldeia = null;
        this.vaiPara(level, this.caixão());
        this.enchaOCrisol(level);
    }

    /** O sopro de fumaça com que ele anda: o {@code moveToBlockPositionAndUpdate} do original. */
    private void vaiPara(ServerLevel level, BlockPos onde) {
        this.fumaça(level);
        this.snapTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, this.getYRot(), this.getXRot());
        this.fumaça(level);
        this.setHomeTo(onde, 4);
    }

    private void fumaça(ServerLevel level) {
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                this.getX(), this.getY() + 0.8, this.getZ(), 24, 0.4, 0.6, 0.4, 0.02);
        level.playSound(null, this.blockPosition(), net.thaumcraft.occulta.OccultaSounds.POOF.value(), SoundSource.HOSTILE, 1.0f, 1.0f);
    }

    /** Se o sol lhe bate: céu aberto, de dia, e sem chuva. */
    public boolean aoSol(ServerLevel level) {
        if (!level.isBrightOutside()) return false;
        if (level.isRainingAt(this.blockPosition())) return false;
        return level.canSeeSky(this.blockPosition());
    }

    /** Procura uma aldeia e vai para lá, sem andar o caminho. */
    private void procuraAldeia(ServerLevel level) {
        BlockPos achada = level.findNearestMapStructure(StructureTags.VILLAGE, this.blockPosition(),
                ALCANCE_DA_ALDEIA, false);
        if (achada == null) return;
        this.aldeia = achada;
        BlockPos chão = new BlockPos(achada.getX(),
                level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                        achada.getX(), achada.getZ()), achada.getZ());
        this.vaiPara(level, chão);
    }

    /** E enche o crisol que estiver a seis blocos do caixão. */
    public boolean enchaOCrisol(ServerLevel level) {
        BlockPos casa = this.caixão();
        for (BlockPos onde : BlockPos.betweenClosed(casa.offset(-ACHA_O_CRISOL, -ACHA_O_CRISOL, -ACHA_O_CRISOL),
                casa.offset(ACHA_O_CRISOL, ACHA_O_CRISOL, ACHA_O_CRISOL))) {
            if (!(level.getBlockEntity(onde) instanceof BloodCrucibleBlockEntity crisol)) continue;
            crisol.feed();
            return true;
        }
        return false;
    }

    /**
     * A mordida: num aldeão, uma vez em dez é um <b>gole</b>.
     *
     * <p>O gole cura quatro e conta quatro para o jantar. As outras nove vezes não fazem dano nenhum ao
     * aldeão — é do original, e é o que faz uma aldeia com um vampiro ficar de pé por semanas.
     */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity alvo) {
        if (!(alvo instanceof Villager aldeão)) return super.doHurtTarget(level, alvo);

        if (this.random.nextInt(CHANCE_DO_GOLE) == 0) this.bebe(level, aldeão);
        return true;
    }

    /** Um gole. */
    public void bebe(ServerLevel level, LivingEntity quem) {
        this.bebido += POR_GOLE;
        this.heal(POR_GOLE);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR,
                quem.getX(), quem.getY() + quem.getBbHeight() * 0.8, quem.getZ(), 8, 0.2, 0.2, 0.2, 0.0);
        level.playSound(null, quem.blockPosition(), net.thaumcraft.occulta.OccultaSounds.DRINK.value(), SoundSource.HOSTILE, 1.0f, 1.0f);
    }

    /**
     * <b>Uma espada não o mata.</b>
     *
     * <p>O {@code checkForVampireDeath} do original: ele morre de <b>fogo</b> (e o sol é fogo), de
     * <b>sufocar</b>, do <b>vazio</b>, e da mão de outro <b>vampiro</b>, de um <b>lobisomem</b> ou de um
     * <b>chefe</b>. De tudo o resto, ele leva o dano e <b>não morre</b>.
     */
    public static boolean podeMorrer(LivingEntity quem, DamageSource fonte) {
        if (fonte.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) return true;
        if (fonte.is(DamageTypes.IN_WALL) || fonte.is(DamageTypes.FELL_OUT_OF_WORLD)) return true;
        if (fonte.is(DamageTypes.GENERIC_KILL)) return true;

        Entity quemBateu = fonte.getEntity();
        if (quemBateu == null) return false;
        if (quemBateu instanceof VampireEntity) return true;
        if (quemBateu instanceof net.thaumcraft.occulta.wolf.WolfmanEntity) return true;
        // e um chefe: o jogo de hoje não tem etiqueta de chefe, e os dois que há são estes
        return quemBateu.getType() == net.minecraft.world.entity.EntityTypes.ENDER_DRAGON
                || quemBateu.getType() == net.minecraft.world.entity.EntityTypes.WITHER;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float quanto) {
        boolean doeu = super.hurtServer(level, fonte, quanto);
        // levou o dano, mas não cai: fica com meio de vida e segue
        if (doeu && this.getHealth() <= 0.0f && !podeMorrer(this, fonte)) {
            this.setHealth(0.5f);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                    this.getX(), this.getY() + 1.0, this.getZ(), 16, 0.3, 0.5, 0.3, 0.02);
        }
        return doeu;
    }

    @Override
    public boolean removeWhenFarAway(double distância) {
        return false;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource fonte) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.6f;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput dados) {
        super.addAdditionalSaveData(dados);
        if (this.caixão != null) dados.store("Coffin", BlockPos.CODEC, this.caixão);
        dados.putFloat("Drunk", this.bebido);
    }

    @Override
    public void readAdditionalSaveData(ValueInput dados) {
        super.readAdditionalSaveData(dados);
        this.caixão = dados.read("Coffin", BlockPos.CODEC).orElse(null);
        this.bebido = dados.getFloatOr("Drunk", 0.0f);
    }
}
