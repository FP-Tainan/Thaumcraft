package net.thaumcraft.occulta.death;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.ghost.TouchOfDeath;

/**
 * A <b>Morte</b>: o {@code EntityDeath} do Witchery.
 *
 * <p>Mil de vida, e não é por aí que ela se mede. O que ela é, é um <b>enderman</b> com uma foice: encara,
 * some, aparece atrás, e o toque dela leva <b>quinze por cento da vida máxima</b> de quem apanhar — pela
 * armadura, pelos efeitos, pelos encantamentos, por tudo.
 *
 * <h2>Como ela olha</h2>
 *
 * <p>É a conta do enderman, letra por letra: ela procura gente a <b>sessenta e quatro blocos</b> e olha se
 * alguém está a <b>olhar para ela</b>. Estando, ela grita (o grito do Wither) e espera <b>cinco batidas</b>
 * encarando antes de atacar. Deixando de olhar, o relógio zera.
 *
 * <p><b>E há um engano do original que fica:</b> o {@code shouldAttackPlayer} dele lê o <b>capacete</b> de
 * quem o olha — a abóbora do enderman — e <b>não faz nada com ele</b>. A variável é lida e largada. De modo
 * que, ao contrário do enderman, <b>não há chapéu que a engane</b>.
 *
 * <h2>Como ela some</h2>
 *
 * <p>Olhando-a, ela <b>foge</b>: se quem a encara chegar a menos de quatro blocos, ela pula para outro
 * lugar. Não a olhando, ela <b>persegue</b>: passando de dezesseis blocos, salta para o pé de quem caça. E
 * uma vez em cem ela inverte as duas coisas, só para não ser previsível.
 *
 * <p>Enquanto tem alvo, ela anda com um <b>empurrão de velocidade</b> enorme — seis vírgula dois — que sai
 * quando o alvo se perde. É o que faz dela uma coisa que não se deixa para trás.
 *
 * <h2>E o que ela faz a quem voa</h2>
 *
 * <p>Uma vez em vinte, por batida, se o alvo estiver <b>caindo ou voando</b> e não estiver lento, ela o
 * deixa <b>lento no sexto grau</b> por dez segundos. Fugir dela pelo ar é a pior ideia que há.
 */
public class DeathEntity extends Monster {
    /** Os números dela. */
    public static final double VIDA = 1000.0;
    public static final double VELOCIDADE = 0.3;
    public static final double MURRO = 7.0;
    public static final int EXPERIÊNCIA = 80;

    /** Quanto da vida máxima o toque dela leva, e o mínimo. */
    public static final float QUANTO = 0.15f;
    public static final float NO_MÍNIMO = 1.0f;

    /** E o teto de tudo o que lhe fazem: quinze, venha de onde vier. */
    public static final float TETO = 15.0f;

    /** Até onde ela procura quem a olhe. */
    public static final double PROCURA = 64.0;

    /** Quantas batidas ela encara antes de atacar. */
    public static final int ENCARA = 5;

    /** De quanto em quanto ela tenta saltar quando persegue, e a partir de que distância. */
    public static final int SALTA_DE = 30;
    public static final double LONGE_DEMAIS = 256.0;
    public static final double PERTO_DEMAIS = 16.0;

    /** E o empurrão de velocidade que ela ganha com alvo: os seis vírgula dois do original. */
    public static final double CORRIDA = 6.2;

    /** O que ela faz a quem foge pelo ar. */
    public static final int UMA_EM_VINTE = 20;
    public static final int LENTIDÃO = 200;
    public static final int LENTIDÃO_GRAU = 5;

    /** Quantos ossos ela larga. */
    public static final int OSSOS = 5;

    /** E quantas vezes ela tenta saltar quando leva flechada. */
    public static final int TENTA_SALTAR = 64;

    private static final AttributeModifier CORRENDO = new AttributeModifier(
            Thaumcraft.id("death_speed"), CORRIDA, AttributeModifier.Operation.ADD_VALUE);

    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> GRITANDO =
            net.minecraft.network.syncher.SynchedEntityData.defineId(DeathEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent barra = new ServerBossEvent(this.getUUID(), this.getDisplayName(),
            BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS);

    private int encarandoHá;
    private int esperaDoSalto;
    private boolean brava;
    private @org.jetbrains.annotations.Nullable LivingEntity últimoAlvo;

    public DeathEntity(EntityType<? extends DeathEntity> tipo, Level level) {
        super(tipo, level);
        this.xpReward = EXPERIÊNCIA;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.MOVEMENT_SPEED, VELOCIDADE)
                .add(Attributes.ATTACK_DAMAGE, MURRO)
                .add(Attributes.FOLLOW_RANGE, PROCURA)
                // o passo de um bloco inteiro do original, que hoje é um atributo
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new EncaraGoal(this));
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder construtor) {
        super.defineSynchedData(construtor);
        construtor.define(GRITANDO, false);
    }

    public boolean gritando() {
        return this.entityData.get(GRITANDO);
    }

    public void gritando(boolean sim) {
        this.entityData.set(GRITANDO, sim);
    }

    // ------------------------------------------------------------------ o olhar

    /**
     * <b>Se aquele está olhando para ela.</b> É a conta do enderman, e o original a copia inteira — menos
     * o chapéu, que ele lê e esquece.
     */
    public boolean oOlha(Player quem) {
        Vec3 olhar = quem.getViewVector(1.0f).normalize();
        Vec3 até = new Vec3(this.getX() - quem.getX(),
                this.getBoundingBox().minY + this.getBbHeight() / 2.0f - quem.getEyeY(),
                this.getZ() - quem.getZ());
        double longe = até.length();
        double quanto = olhar.dot(até.normalize());
        return quanto > 1.0 - 0.025 / longe;
    }

    /** A meta que a faz encarar antes de atacar: o {@code findPlayerToAttack} do original. */
    private static class EncaraGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final DeathEntity morte;

        EncaraGoal(DeathEntity morte) {
            this.morte = morte;
            this.setFlags(java.util.EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!(this.morte.level() instanceof ServerLevel level)) return false;
            Player quem = level.getNearestPlayer(this.morte, PROCURA);
            if (quem == null || !this.morte.oOlha(quem)) {
                this.morte.encarandoHá = 0;
                return false;
            }
            this.morte.brava = true;
            if (this.morte.encarandoHá == 0) {
                level.playSound(null, quem.blockPosition(), SoundEvents.WITHER_SPAWN,
                        net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.0f);
            }
            if (this.morte.encarandoHá++ < ENCARA) return false;
            this.morte.encarandoHá = 0;
            this.morte.gritando(true);
            this.morte.setTarget(quem);
            return false;
        }
    }

    // ------------------------------------------------------------------ a batida dela

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.tickCount % 20 == 0) this.heal(1.0f);

        // o empurrão de velocidade entra com o alvo e sai com ele
        LivingEntity alvo = this.getTarget();
        if (this.últimoAlvo != alvo) {
            var passo = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (passo != null) {
                passo.removeModifier(CORRENDO.id());
                if (alvo != null) passo.addTransientModifier(CORRENDO);
            }
        }
        this.últimoAlvo = alvo;

        if (this.gritando() && !this.brava) this.gritando(false);
        if (!(this.level() instanceof ServerLevel level) || !this.isAlive()) return;

        this.barra.setProgress(this.getHealth() / this.getMaxHealth());
        this.oSalto(level, alvo);
        this.aQuemFoge(level, alvo);
    }

    /**
     * <b>O salto.</b> Olhada, ela foge de perto; não olhada, ela cai em cima. E uma em cem ela troca as
     * duas, que é o que a torna impossível de ler.
     */
    private void oSalto(ServerLevel level, @org.jetbrains.annotations.Nullable LivingEntity alvo) {
        if (alvo == null) {
            this.gritando(false);
            this.esperaDoSalto = 0;
            return;
        }
        boolean oOlham = alvo instanceof Player quem && this.oOlha(quem);
        if (!oOlham && level.getRandom().nextInt(100) != 0) {
            if (alvo.distanceToSqr(this) > LONGE_DEMAIS && this.esperaDoSalto++ >= SALTA_DE
                    && this.saltaPara(level, alvo)) {
                this.esperaDoSalto = 0;
            }
            return;
        }
        if (alvo.distanceToSqr(this) < PERTO_DEMAIS) this.saltaAoAcaso(level);
        this.esperaDoSalto = 0;
    }

    /** E a lentidão de quem tenta fugir pelo ar. */
    private void aQuemFoge(ServerLevel level, @org.jetbrains.annotations.Nullable LivingEntity alvo) {
        if (alvo == null || level.getRandom().nextInt(UMA_EM_VINTE) != 0) return;
        boolean noAr = !alvo.onGround()
                || alvo instanceof Player quem && quem.getAbilities().flying;
        if (!noAr || alvo.hasEffect(MobEffects.SLOWNESS)) return;
        alvo.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, LENTIDÃO, LENTIDÃO_GRAU));
    }

    /** Um salto para um lugar qualquer a trinta e dois blocos. */
    public boolean saltaAoAcaso(ServerLevel level) {
        var sorte = level.getRandom();
        return this.saltaPara(level, this.getX() + (sorte.nextDouble() - 0.5) * 32.0,
                this.getY() + (sorte.nextInt(64) - 32), this.getZ() + (sorte.nextDouble() - 0.5) * 32.0);
    }

    /** E um salto para o pé de alguém, a dezesseis blocos dele na linha de quem olha. */
    public boolean saltaPara(ServerLevel level, Entity quem) {
        Vec3 rumo = new Vec3(this.getX() - quem.getX(),
                this.getBoundingBox().minY + this.getBbHeight() / 2.0f - quem.getY() + quem.getEyeHeight(),
                this.getZ() - quem.getZ()).normalize();
        var sorte = level.getRandom();
        return this.saltaPara(level,
                this.getX() + (sorte.nextDouble() - 0.5) * 8.0 - rumo.x * 16.0,
                this.getY() + (sorte.nextInt(16) - 8) - rumo.y * 16.0,
                this.getZ() + (sorte.nextDouble() - 0.5) * 8.0 - rumo.z * 16.0);
    }

    /**
     * <b>O salto em si</b>: desce até achar chão, e só vai se couber e não houver água.
     *
     * <p>É a conta do enderman da 1.7.10, e o rasto de pó dela é o mesmo: cento e vinte e oito pontos de
     * portal ao longo da linha entre onde ela estava e onde ficou.
     */
    public boolean saltaPara(ServerLevel level, double x, double y, double z) {
        double eraX = this.getX();
        double eraY = this.getY();
        double eraZ = this.getZ();

        int alturaDoChão = net.minecraft.util.Mth.floor(y);
        BlockPos onde = new BlockPos(net.minecraft.util.Mth.floor(x), alturaDoChão,
                net.minecraft.util.Mth.floor(z));
        if (!level.hasChunkAt(onde)) return false;

        boolean achouChão = false;
        while (!achouChão && onde.getY() > level.getMinY()) {
            if (level.getBlockState(onde.below()).blocksMotion()) achouChão = true;
            else onde = onde.below();
        }
        if (!achouChão) return false;

        this.snapTo(onde.getX() + (x - Math.floor(x)), onde.getY(), onde.getZ() + (z - Math.floor(z)),
                this.getYRot(), this.getXRot());
        if (!level.noCollision(this) || level.containsAnyLiquid(this.getBoundingBox())) {
            this.snapTo(eraX, eraY, eraZ, this.getYRot(), this.getXRot());
            return false;
        }

        rasto(level, eraX, eraY, eraZ, this);
        level.playSound(null, eraX, eraY, eraZ, SoundEvents.ENDERMAN_TELEPORT,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.0f);
        this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f);
        return true;
    }

    /** O rasto de portal entre onde ela estava e onde ficou: os cento e vinte e oito do original. */
    public static void rasto(ServerLevel level, double eraX, double eraY, double eraZ, Entity quem) {
        for (int passo = 0; passo < 128; passo++) {
            double quanto = passo / 127.0;
            var sorte = level.getRandom();
            level.sendParticles(ParticleTypes.PORTAL,
                    eraX + (quem.getX() - eraX) * quanto
                            + (sorte.nextDouble() - 0.5) * quem.getBbWidth() * 2.0,
                    eraY + (quem.getY() - eraY) * quanto + sorte.nextDouble() * quem.getBbHeight(),
                    eraZ + (quem.getZ() - eraZ) * quanto
                            + (sorte.nextDouble() - 0.5) * quem.getBbWidth() * 2.0,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    // ------------------------------------------------------------------ bater e apanhar

    /**
     * <b>O toque dela</b>: quinze por cento da vida máxima de quem apanhar, e nunca menos de um.
     *
     * <p>É o <b>toque da morte</b> — não passa pela armadura porque não é um golpe, é o fim.
     */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity noquê) {
        if (!(noquê instanceof LivingEntity vivo)) return super.doHurtTarget(level, noquê);
        float quanto = Math.max(vivo.getMaxHealth() * QUANTO, NO_MÍNIMO);
        return TouchOfDeath.toca(level, this, vivo, quanto);
    }

    /**
     * <b>Nada lhe tira mais de quinze</b>, e <b>flecha nenhuma a acha</b>: levando um golpe de longe, ela
     * salta — até sessenta e quatro tentativas — e o golpe não conta.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float quanto) {
        if (this.isInvulnerableTo(level, fonte)) return false;
        this.gritando(true);
        if (fonte.getEntity() instanceof Player) this.brava = true;

        if (!fonte.isDirect() && fonte.getEntity() != null) {
            this.brava = false;
            for (int volta = 0; volta < TENTA_SALTAR; volta++) {
                if (this.saltaAoAcaso(level)) return true;
            }
        }
        return super.hurtServer(level, fonte, Math.min(quanto, TETO));
    }

    @Override
    public void die(DamageSource fonte) {
        super.die(fonte);
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(),
                    16, 0.5, 1.0, 0.5, 0.0);
            level.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                    net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.0f);
        }
    }

    /**
     * <b>O que ela larga</b>: um <b>crânio</b>, cinco ossos, um livro encantado, uma em quatro uma
     * <b>Espada da Morte</b> — um diamante encantado a quarenta —, e <b>uma</b> das cinco coisas dela: a
     * sela com a cabeça do Binky, o capuz, o manto, as botas ou a Mão.
     */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource fonte, boolean matouGente) {
        super.dropCustomDeathLoot(level, fonte, matouGente);
        this.spawnAtLocation(level, new ItemStack(Items.SKELETON_SKULL), 0.0f);
        this.spawnAtLocation(level, new ItemStack(Items.BONE, OSSOS), 0.0f);
        this.spawnAtLocation(level, net.thaumcraft.occulta.leonard.LeonardEntity.livroAoAcaso(level), 0.0f);
        if (level.getRandom().nextInt(4) == 0) this.spawnAtLocation(level, aEspada(level), 0.0f);

        switch (level.getRandom().nextInt(5)) {
            case 0 -> {
                this.spawnAtLocation(level, new ItemStack(Items.SADDLE), 0.0f);
                this.spawnAtLocation(level, new ItemStack(OccultaItems.BINKYS_HEAD), 0.0f);
            }
            case 1 -> this.spawnAtLocation(level, new ItemStack(OccultaItems.DEATHS_HOOD), 0.0f);
            case 2 -> this.spawnAtLocation(level, new ItemStack(OccultaItems.DEATHS_ROBE), 0.0f);
            case 3 -> this.spawnAtLocation(level, new ItemStack(OccultaItems.DEATHS_BOOTS), 0.0f);
            default -> this.spawnAtLocation(level, new ItemStack(OccultaItems.DEATHS_HAND), 0.0f);
        }
    }

    /** A <b>Espada da Morte</b>: um diamante encantado a quarenta, com o nome dela. */
    public static ItemStack aEspada(ServerLevel level) {
        ItemStack espada = new ItemStack(Items.DIAMOND_SWORD);
        espada = net.minecraft.world.item.enchantment.EnchantmentHelper.enchantItem(
                level.getRandom(), espada, 40, level.registryAccess(), java.util.Optional.empty());
        espada.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                net.minecraft.network.chat.Component.translatable("tc.occulta.swordofdeath"));
        return espada;
    }

    // ------------------------------------------------------------------ a barra, e o resto

    @Override
    public void startSeenByPlayer(ServerPlayer quem) {
        super.startSeenByPlayer(quem);
        this.barra.addPlayer(quem);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer quem) {
        super.stopSeenByPlayer(quem);
        this.barra.removePlayer(quem);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double longe) {
        return false;
    }

    @Override
    protected @org.jetbrains.annotations.Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource fonte) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }
}
