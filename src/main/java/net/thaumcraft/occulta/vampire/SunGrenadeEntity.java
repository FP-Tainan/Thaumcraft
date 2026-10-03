package net.thaumcraft.occulta.vampire;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

/**
 * <b>A Granada Solar</b>: o {@code EntityGrenade} do Witchery, no modo zero.
 *
 * <p>É <b>sol engarrafado</b>, e é a melhor ideia da escada do vampiro. Atirada, ela voa, bate, e <b>para no
 * ar onde bateu</b> — a gravidade dela some no impacto — e fica ali um minuto inteiro a <b>alumiar</b>, como
 * um pequeno sol parado.
 *
 * <h2>O que ela faz, e a quem</h2>
 *
 * <ul>
 *   <li>enquanto voa e enquanto fica, ela <b>põe um bloco de luz</b> por onde passa, e tira-o quando se
 *       muda — de modo que a luz a acompanha;</li>
 *   <li>parada, ela <b>põe fogo</b> a todo <b>morto-vivo</b> a um bloco, três segundos de cada vez;</li>
 *   <li>e ao fim do minuto ela <b>estoura em faíscas</b>, devolve a <b>Esfera de Quartzo</b> que a fez, e
 *       queima os mortos-vivos a <b>três blocos</b> por cinco segundos.</li>
 * </ul>
 *
 * <p><b>E um vampiro é um morto-vivo.</b> É aí que está o degrau: para aguentar o sol, ele tem de aprender a
 * <b>levar</b> o sol — e a única maneira de o levar sem morrer é em doses, de noite, de garrafa em garrafa.
 * Dez vezes. Nenhuma linha do jogo o diz; o livro o diz em reticências, e quem não ler o livro nunca saberá
 * por que haveria de se queimar de propósito.
 *
 * <p><b>Declarado:</b> o original tem um segundo modo nesta mesma criatura — a <b>Granada Duplicadora</b>,
 * que larga um sósia de alguém para distrair. Ela pede a Seguidora do tipo cinco, que este porte não tem, e
 * fica de fora.
 */
public class SunGrenadeEntity extends ThrowableItemProjectile {
    /** Quanto ela dura no ar, e quanto dura depois de parar. */
    public static final int NO_AR = 200;
    public static final int PARADA = 1200;

    /** De quanto em quanto ela arruma a luz, e quanto o fogo dela dura. */
    public static final int ARRUMA_A_LUZ = 5;
    public static final int QUEIMA_PERTO = 3;
    public static final int QUEIMA_NO_FIM = 5;

    /** E a que distância ela queima, de pé e no estouro. */
    public static final double PERTO = 1.0;
    public static final double NO_ESTOURO = 3.0;

    /** A luz que ela deixa, que é a do jogo e vai no máximo. */
    public static final int CLARIDADE = 15;

    private boolean parada;
    private int desdeQueParou;

    @Nullable
    private BlockPos luz;

    public SunGrenadeEntity(EntityType<? extends SunGrenadeEntity> type, Level level) {
        super(type, level);
    }

    public SunGrenadeEntity(Level level, LivingEntity quemAtirou, ItemStack oquê) {
        super(net.thaumcraft.occulta.OccultaEntities.SUN_GRENADE, quemAtirou, level, oquê);
    }

    @Override
    protected Item getDefaultItem() {
        return net.thaumcraft.occulta.OccultaItems.SUN_GRENADE;
    }

    /** Parada, ela não cai: é o {@code getGravityVelocity} que devolve zero depois do impacto. */
    @Override
    protected double getDefaultGravity() {
        return this.parada ? 0.0 : 0.05;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;
        if (this.isRemoved()) return;

        if (this.parada) {
            this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            this.desdeQueParou++;
            if (this.tickCount % ARRUMA_A_LUZ == 2) this.queima(level, PERTO, QUEIMA_PERTO, false);
            if (this.desdeQueParou >= PARADA) {
                this.estoura(level);
                return;
            }
        } else if (this.tickCount >= NO_AR) {
            this.estoura(level);
            return;
        }

        if (this.tickCount % ARRUMA_A_LUZ == 4) this.arrumaALuz(level);
    }

    /**
     * <b>A luz anda com ela</b>: o bloco de luz do original, posto e tirado a cada cinco batidas.
     *
     * <p>No Witchery isto era um bloco do próprio mod; aqui é o {@code minecraft:light} do jogo de hoje, que
     * existe exatamente para isto. É a única coisa desta criatura que não precisou de ser portada.
     */
    private void arrumaALuz(ServerLevel level) {
        BlockPos agora = this.blockPosition();
        if (!level.isEmptyBlock(agora)) agora = agora.above();
        if (agora.equals(this.luz)) return;

        this.apagaALuz(level);
        if (!level.isEmptyBlock(agora)) return;
        level.setBlockAndUpdate(agora,
                Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, CLARIDADE));
        this.luz = agora;
    }

    private void apagaALuz(ServerLevel level) {
        if (this.luz == null) return;
        if (level.getBlockState(this.luz).is(Blocks.LIGHT)) {
            level.setBlockAndUpdate(this.luz, Blocks.AIR.defaultBlockState());
        }
        this.luz = null;
    }

    /**
     * <b>O estouro</b>: faíscas, a Esfera de volta, e o sol em quem estiver perto.
     *
     * <p>A esfera volta <b>sempre</b>, e isso diz o que ela é: a granada não se gasta, ela se <b>descarrega
     * </b>. Quem tiver um Coletor de Luz tem sol de graça para sempre — leva só o tempo de um dia a juntar.
     */
    private void estoura(ServerLevel level) {
        this.apagaALuz(level);
        this.queima(level, NO_ESTOURO, QUEIMA_NO_FIM, true);

        level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(),
                24, 0.2, 0.2, 0.2, 0.08);
        level.playSound(null, this.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.NEUTRAL, 1.0f, 1.2f);
        this.spawnAtLocation(level, new ItemStack(net.thaumcraft.occulta.OccultaItems.QUARTZ_SPHERE));
        this.discard();
    }

    /**
     * Queima os mortos-vivos à volta — e é aqui que o <b>quinto degrau</b> da escada conta.
     *
     * @param aConta se esta queimadura vale um passo; só a do estouro vale
     */
    private void queima(ServerLevel level, double quão, int segundos, boolean aConta) {
        var roda = this.getBoundingBox().inflate(quão, quão * 0.7, quão);
        for (LivingEntity quem : level.getEntitiesOfClass(LivingEntity.class, roda)) {
            if (!quem.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)
                    && !Vampire.é(quem)) {
                continue;
            }
            quem.igniteForSeconds(segundos);
            if (aConta && quem instanceof Player gente) VampireLadder.oSolEngarrafado(gente);
        }
    }

    /** Batendo, ela <b>para no ar</b> em vez de arrebentar. */
    @Override
    protected void onHit(HitResult onde) {
        if (this.parada) return;
        this.parada = true;
        this.desdeQueParou = 0;
        this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        if (this.level() instanceof ServerLevel level) {
            level.playSound(null, this.blockPosition(), SoundEvents.GLASS_PLACE,
                    SoundSource.NEUTRAL, 0.6f, 1.6f);
        }
    }

    @Override
    public void remove(RemovalReason porquê) {
        if (this.level() instanceof ServerLevel level) this.apagaALuz(level);
        super.remove(porquê);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput dados) {
        super.addAdditionalSaveData(dados);
        dados.putBoolean("Parada", this.parada);
        dados.putInt("Desde", this.desdeQueParou);
        if (this.luz != null) dados.store("Luz", BlockPos.CODEC, this.luz);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput dados) {
        super.readAdditionalSaveData(dados);
        this.parada = dados.getBooleanOr("Parada", false);
        this.desdeQueParou = dados.getIntOr("Desde", 0);
        this.luz = dados.read("Luz", BlockPos.CODEC).orElse(null);
    }
}
