package net.thaumcraft.occulta.ghost;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.occulta.OccultaItems;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * O que os três <b>fantasmas</b> têm em comum: o {@code EntitySummonedUndead} do Witchery.
 *
 * <p>Eles não nascem no mundo. Saem todos do <b>Braseiro</b>, de uma fogueira que arde meio minuto com
 * losna e pó de cemitério dentro, e por isso carregam duas coisas que um morto-vivo do jogo não carrega:
 * o <b>nome de quem os chamou</b> e, às vezes, um <b>prazo</b>.
 *
 * <h2>Nenhum golpe passa de quinze</h2>
 *
 * <p>É a regra mais importante dos três, e vale dizê-la em voz alta: <b>o dano que eles levam é cortado em
 * quinze</b>, seja ele qual for. Não há espada que os mate depressa, não há poção que ajude, não há queda
 * que resolva. Um espectro de quarenta de vida leva <b>três golpes no mínimo</b>, e uma banshee também.
 *
 * <p>Isso muda o que eles são: não são bichos que se matam, são bichos de que se <b>foge</b> — ou que se
 * gastam numa coisa, que é o que o fetiche faz com eles.
 *
 * <h2>E eles somem</h2>
 *
 * <p>Um fantasma com prazo some quando o prazo acaba <b>ou quando perde o alvo</b>. Quem o chamou para uma
 * briga não fica com ele depois dela.
 */
public abstract class SummonedUndeadEntity extends Monster {
    /** O teto de dano por golpe, que nenhuma arma levanta. */
    public static final float TETO = 15.0f;

    /** Quantas vezes mais devagar eles resmungam do que um bicho comum. */
    public static final int CALADOS = 3;

    /** De quantos em quantos sai pó espectral, e o mínimo a que a sorte chega. */
    public static final int UM_EM_QUATRO = 4;
    public static final int NO_MÁXIMO_METADE = 2;

    private static final EntityDataAccessor<Boolean> GRITANDO =
            SynchedEntityData.defineId(SummonedUndeadEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> APAGADO =
            SynchedEntityData.defineId(SummonedUndeadEntity.class, EntityDataSerializers.BOOLEAN);

    private @Nullable UUID quemChamou;
    private int prazo = -1;

    protected SummonedUndeadEntity(EntityType<? extends SummonedUndeadEntity> tipo, Level level) {
        super(tipo, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder construtor) {
        super.defineSynchedData(construtor);
        construtor.define(GRITANDO, false);
        construtor.define(APAGADO, false);
    }

    // ------------------------------------------------------------------ quem os chamou

    public @Nullable UUID quemChamou() {
        return this.quemChamou;
    }

    public void quemChamou(@Nullable UUID quem) {
        this.quemChamou = quem;
        if (quem != null) this.setPersistenceRequired();
    }

    public @Nullable Player oDono() {
        return this.quemChamou == null ? null : this.level().getPlayerByUUID(this.quemChamou);
    }

    // ------------------------------------------------------------------ o prazo

    public void prazo(int batidas) {
        this.prazo = batidas;
    }

    /** Se ele tem hora para ir embora. */
    public boolean deEmpréstimo() {
        return this.prazo != -1;
    }

    // ------------------------------------------------------------------ o que o desenhista vê

    /** Se ele está <b>gritando</b>: a boca abre e os braços levantam. */
    public boolean gritando() {
        return this.entityData.get(GRITANDO);
    }

    public void gritando(boolean sim) {
        this.entityData.set(GRITANDO, sim);
    }

    /** Se ele está <b>apagado</b>, que é como o espectro nasce: quase invisível. */
    public boolean apagado() {
        return this.entityData.get(APAGADO);
    }

    public void apagado(boolean sim) {
        this.entityData.set(APAGADO, sim);
    }

    // ------------------------------------------------------------------ o resto

    /** <b>Nenhum golpe passa de quinze.</b> */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float quanto) {
        return super.hurtServer(level, fonte, Math.min(quanto, TETO));
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public int getAmbientSoundInterval() {
        return super.getAmbientSoundInterval() * CALADOS;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.prazo == -1) return;
        /*
         * O prazo acaba, <b>ou o alvo acaba</b>. O original escreve as duas condições na mesma linha, e é
         * de propósito: um fantasma emprestado para uma briga vai embora quando a briga acaba.
         */
        if (--this.prazo <= 0 || this.getTarget() == null || !this.getTarget().isAlive()) {
            level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0, this.getZ(),
                    8, 0.5, 0.5, 0.5, 0.0);
            this.discard();
        }
    }

    /**
     * E o que eles largam: <b>pó espectral</b>, e só os que vieram para ficar.
     *
     * <p>Um em quatro, e a Pilhagem aperta a conta até um em dois — não mais do que isso, porque o
     * original escreve {@code nextInt(max(4 - pilhagem, 2))} e aquele dois é o chão.
     *
     * <p>Quem veio de empréstimo não larga nada, e é justo: ele não morreu, acabou.
     */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource fonte, boolean quemMatou) {
        super.dropCustomDeathLoot(level, fonte, quemMatou);
        if (this.deEmpréstimo()) return;

        int pilhagem = 0;
        if (fonte.getEntity() instanceof net.minecraft.world.entity.LivingEntity quem) {
            var encantos = level.registryAccess().lookupOrThrow(
                    net.minecraft.core.registries.Registries.ENCHANTMENT);
            pilhagem = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                    encantos.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING),
                    quem.getMainHandItem());
        }
        if (this.random.nextInt(Math.max(UM_EM_QUATRO - pilhagem, NO_MÁXIMO_METADE)) != 0) return;
        this.spawnAtLocation(level, new ItemStack(OccultaItems.SPECTRAL_DUST), 0.0f);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput saída) {
        super.addAdditionalSaveData(saída);
        if (this.quemChamou != null) {
            saída.store("Summoner", net.minecraft.core.UUIDUtil.CODEC, this.quemChamou);
        }
        saída.putBoolean("Obscured", this.apagado());
        saída.putInt("SuicideIn", this.prazo);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput entrada) {
        super.readAdditionalSaveData(entrada);
        Optional<UUID> quem = entrada.read("Summoner", net.minecraft.core.UUIDUtil.CODEC);
        this.quemChamou = quem.orElse(null);
        this.apagado(entrada.getBooleanOr("Obscured", false));
        this.prazo = entrada.getIntOr("SuicideIn", -1);
    }
}
