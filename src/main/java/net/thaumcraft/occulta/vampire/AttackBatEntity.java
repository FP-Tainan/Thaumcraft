package net.thaumcraft.occulta.vampire;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * <b>O morcego do enxame</b>: o {@code EntityAttackBat} do Witchery.
 *
 * <p>É um morcego comum do jogo com três coisas a mais, e as três contam a história do Supremo que o chama:
 *
 * <ol>
 *   <li>ele <b>vai ao que o dono está olhando</b> — não tem alvo próprio, tem o olhar de quem o chamou;</li>
 *   <li>ele <b>morre ao tocar</b> em quem for, deixando quatro de dor atribuídos ao <b>dono</b>;</li>
 *   <li>e ele <b>dura trezentas batidas</b> e depois se desfaz em fumaça.</li>
 * </ol>
 *
 * <h2>Por que isto é bonito</h2>
 *
 * <p>Um morcego do enxame não é um servo e não é um bicho de estimação: é um <b>tiro que voa torto</b>. Ele
 * não caça, não escolhe, não volta — ele segue a mira do dono e se gasta no primeiro que apanhar. Quinze
 * deles valem sessenta de dor, <b>se todos acertarem</b>, e eles raramente acertam todos: param em paredes,
 * perdem o alvo quando o dono vira a cara, e morrem no primeiro corpo que encontram pelo caminho.
 *
 * <p>E eles <b>não miram outros morcegos</b>, nem se batem uns nos outros. Sem essa regra, quinze morcegos
 * nascidos no mesmo lugar se matariam sozinhos no primeiro instante.
 *
 * <p>Nada cai deles: eles vêm {@linkplain net.thaumcraft.occulta.NoDrops marcados}, porque quinze morcegos a
 * cada uso de um Supremo com cinco cargas seriam uma fábrica e não um poder.
 */
public class AttackBatEntity extends Bat {
    /** Quanto ele dói, e quanto ele dura. */
    public static final float DANO = 4.0f;
    public static final int VIDA = 300;

    /** A que distância o olhar do dono alcança. */
    public static final double MIRA = 32.0;

    /** E com que força ele se atira: o empurrão por batida, e a andadura. */
    public static final double EMPURRÃO = 0.1;
    public static final float ANDADURA = 0.5f;

    @Nullable
    private UUID dono;

    @Nullable
    private Player quemÉ;

    public AttackBatEntity(EntityType<? extends AttackBatEntity> type, Level level) {
        super(type, level);
    }

    /** Quem o chamou. */
    public void dono(Player quem) {
        this.dono = quem.getUUID();
        this.quemÉ = quem;
    }

    @Nullable
    public Player dono() {
        if (this.quemÉ != null && !this.quemÉ.isRemoved()) return this.quemÉ;
        if (this.dono == null) return null;
        this.quemÉ = this.level().getPlayerByUUID(this.dono);
        return this.quemÉ;
    }

    // ------------------------------------------------------------------ e o toque que o gasta

    /**
     * Ele toca em tudo o que passa, e não só no que o empurra.
     *
     * <p>O original alarga a caixa dele em dois décimos para os lados e pergunta a todo mundo que caiba lá
     * se pode ser empurrado. Um morcego comum não faz isto: ele é que é empurrado.
     */
    @Override
    protected void pushEntities() {
        if (!(this.level() instanceof ServerLevel)) return;
        for (Entity quem : this.level().getEntities(this, this.getBoundingBox().inflate(0.2, 0.0, 0.2))) {
            if (quem.isPushable()) this.doPush(quem);
        }
    }

    /**
     * E tocando em alguém, ele <b>se gasta</b>.
     *
     * <p>A dor é atribuída ao <b>dono</b> e não ao morcego: quem mordeu um aldeão com um enxame mordeu-o com
     * as próprias mãos, para efeito de quem vem atrás.
     *
     * <p><b>Declarado</b>: o original usa o {@code causeIndirectDamage} de então, que era o tipo "mob" com
     * um culpado por trás. Aqui é o {@code mobProjectile}, que é o mesmo desenho no jogo de hoje — um bicho
     * que machuca de longe, com o dono a responder por ele.
     */
    @Override
    protected void doPush(Entity quem) {
        if (!(this.level() instanceof ServerLevel level)) return;
        if (!(quem instanceof LivingEntity vivo)) return;
        if (quem instanceof Bat || quem.isRemoved()) return;

        Player dono = this.dono();
        if (vivo == dono) return;

        vivo.hurtServer(level, level.damageSources().mobProjectile(this, dono), DANO);
        level.sendParticles(net.minecraft.core.particles.DustParticleOptions.REDSTONE,
                quem.getX(), quem.getY() + quem.getBbHeight() * 0.5, quem.getZ(),
                16, quem.getBbWidth(), quem.getBbHeight() * 0.5, quem.getBbWidth(), 0.0);
        level.playSound(null, quem.blockPosition(), SoundEvents.GENERIC_DRINK.value(),
                SoundSource.HOSTILE, 1.0f, 1.0f);
        this.discard();
    }

    // ------------------------------------------------------------------ e o rumo, que é o olhar do dono

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.tickCount > VIDA) {
            level.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.3, this.getZ(),
                    16, 0.3, 0.3, 0.3, 0.0);
            this.discard();
            return;
        }
        if (!this.voaParaAMira(level)) super.customServerAiStep(level);
    }

    /**
     * O voo até quem o dono está olhando.
     *
     * <p>Devolvendo {@code false}, o morcego volta a ser um morcego: vagueia ao calhas, como o do jogo. É o
     * que acontece quando o dono não olha para nada, olha para uma parede, ou está noutro mundo — e é por
     * isso que um enxame largado fica a esvoaçar no lugar.
     */
    private boolean voaParaAMira(ServerLevel level) {
        Player dono = this.dono();
        if (dono == null || dono.level() != level) return false;

        Entity alvo = naMira(level, dono);
        if (alvo == null) return false;

        double dx = alvo.getX() - this.getX();
        double dy = alvo.getY() - this.getY();
        double dz = alvo.getZ() - this.getZ();
        double quão = Math.sqrt(dx * dx + dy * dy + dz * dz);

        // o rumo vira sempre para o alvo, mesmo quando o caminho não dá
        this.yBodyRot = (float) -Math.atan2(dx, dz) * (180.0f / (float) Math.PI);
        this.setYRot(this.yBodyRot);

        if (quão <= 0.0 || !cabe(level, alvo.position(), quão)) return false;

        this.setDeltaMovement(this.getDeltaMovement().add(
                dx / quão * EMPURRÃO, dy / quão * EMPURRÃO, dz / quão * EMPURRÃO));
        this.zza = ANDADURA;
        return true;
    }

    /**
     * O que o dono tem na mira, a trinta e dois blocos: o {@code raytraceEntities} do original.
     *
     * <p>No Witchery este cálculo vive numa <b>infusão</b> — a do Outrolugar —, que este porte ainda não
     * tem. Está aqui porque é dela que o enxame depende, e quando a infusão vier é daqui que ela sai.
     *
     * <p>Outros <b>morcegos não contam</b>: um enxame que se mirasse a si mesmo morreria no primeiro
     * instante.
     */
    @Nullable
    private static Entity naMira(ServerLevel level, Player dono) {
        Vec3 olhos = dono.getEyePosition();
        Vec3 olhar = dono.getViewVector(1.0f);
        Vec3 longe = olhos.add(olhar.scale(MIRA));

        HitResult parede = level.clip(new ClipContext(olhos, longe, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, dono));
        if (parede.getType() != HitResult.Type.MISS) longe = parede.getLocation();

        AABB roda = dono.getBoundingBox().expandTowards(olhar.scale(MIRA)).inflate(1.0);
        var bateu = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                dono, olhos, longe, roda,
                quem -> quem instanceof LivingEntity && !(quem instanceof Bat)
                        && !quem.isSpectator() && quem.isPickable(),
                olhos.distanceToSqr(longe));
        return bateu == null ? null : bateu.getEntity();
    }

    /**
     * Se o caminho até lá <b>cabe</b>: o {@code isCourseTraversable} do original.
     *
     * <p>Ele arrasta a própria caixa um bloco de cada vez pelo caminho e pergunta se bate em algo. Um
     * morcego do enxame <b>não atravessa paredes</b> — ele para do lado de cá e esvoaça lá, e é por isso que
     * um enxame contra alguém atrás de uma porta não serve de nada.
     */
    private boolean cabe(ServerLevel level, Vec3 até, double quão) {
        double dx = (até.x - this.getX()) / quão;
        double dy = (até.y - this.getY()) / quão;
        double dz = (até.z - this.getZ()) / quão;

        AABB caixa = this.getBoundingBox();
        for (int n = 1; n < quão; n++) {
            caixa = caixa.move(dx, dy, dz);
            if (!level.noCollision(this, caixa)) return false;
        }
        return true;
    }

    // ------------------------------------------------------------------ e o resto

    @Override
    public void addAdditionalSaveData(ValueOutput dados) {
        super.addAdditionalSaveData(dados);
        if (this.dono != null) dados.store("Dono", net.minecraft.core.UUIDUtil.CODEC, this.dono);
    }

    @Override
    public void readAdditionalSaveData(ValueInput dados) {
        super.readAdditionalSaveData(dados);
        this.dono = dados.read("Dono", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        this.quemÉ = null;
    }
}
