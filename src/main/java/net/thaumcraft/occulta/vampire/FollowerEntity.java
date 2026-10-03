package net.thaumcraft.occulta.vampire;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * <b>Elle</b>: o {@code EntityFollower} do Witchery, no tipo zero.
 *
 * <p>Ela é o que o rito chama, e não é um chefe nem um servo: é um <b>guia</b>. E o que ela procura diz tudo
 * sobre quem a mandou — ela anda à procura de <b>lava</b>.
 *
 * <h2>O que ela faz, e por que é bonito</h2>
 *
 * <p>Recém-chamada, Elle não tem casa. De dez em dez batidas ela olha à volta, a quinze blocos, à procura de
 * um <b>lago de lava</b> — lava de verdade, com dois andares de ar por cima e seis blocos de raio. Achando
 * um, ela <b>faz dele a casa dela</b> e esquece quem a chamou: daí em diante é para lá que ela vai.
 *
 * <p>E chegando lá, ela conta. Às <b>vinte</b> batidas em casa ela fala; às <b>quarenta</b>, ela <b>deixa de
 * existir</b> e no lugar dela fica <b>Lilith</b>, com um estouro.
 *
 * <p>É por isso que o rito não é o fim da história: quem chamar Elle no meio de um campo fica com uma
 * convidada que não tem para onde ir. <b>O jogador tem de a levar até à lava</b> — ou cavar até ela, ou
 * fazer um lago para ela. O mod nunca o diz; ela é que mostra, voando sempre para o mesmo lado.
 *
 * <p>Ela <b>não arde</b>, e tinha de não arder: a casa dela é um lago de lava.
 */
public class FollowerEntity extends Monster {
    /** De quanto em quanto ela procura, e a quantas casas. */
    public static final int PROCURA_DE = 10;
    public static final int TENTATIVAS = 10;
    public static final int PROCURA_A = 15;

    /** O que faz de um lago um lago: o raio de lava, e a altura de ar por cima. */
    public static final int LAGO = 6;
    public static final int AR = 2;

    /** A que distância de casa ela conta como chegada. */
    public static final int EM_CASA = 2;

    /** Quando ela fala, e quando ela vira Lilith. */
    public static final int FALA_AOS = 20;
    public static final int VIRA_AOS = 40;

    /** E o estouro com que Lilith chega. */
    public static final float ESTOURO = 6.0f;

    @Nullable
    private BlockPos casa;
    @Nullable
    private UUID dono;
    private int conta;

    public FollowerEntity(EntityType<? extends FollowerEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    /** Quem a chamou. */
    public void dono(Player quem) {
        this.dono = quem.getUUID();
        this.setHealth(this.getMaxHealth());
    }

    @Nullable
    public UUID dono() {
        return this.dono;
    }

    /** A casa dela, se já achou uma. */
    @Nullable
    public BlockPos casa() {
        return this.casa;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distância) {
        return false;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.tickCount % PROCURA_DE != 1) return;

        if (this.casa == null) {
            this.procuraLava(level);
            return;
        }
        if (this.emCasa()) {
            this.conta(level);
            return;
        }
        this.voaParaCasa();
    }

    /** À procura de um lago de lava, dez vezes por volta. */
    private void procuraLava(ServerLevel level) {
        var sorte = level.getRandom();
        for (int n = 0; n < TENTATIVAS; n++) {
            BlockPos onde = BlockPos.containing(
                    this.getX() + sorte.nextInt(PROCURA_A * 2) - PROCURA_A,
                    this.getBoundingBox().minY + sorte.nextInt(6) - 3,
                    this.getZ() + sorte.nextInt(PROCURA_A * 2) - PROCURA_A);
            if (!éLago(level, onde)) continue;
            this.casa = onde;
            this.dono = null;
            return;
        }
    }

    /** Se ela já está em casa. */
    public boolean emCasa() {
        return this.casa != null && this.casa.closerToCenterThan(this.position(), EM_CASA);
    }

    /** E o voo para lá, que é um empurrão por volta e não um caminho. */
    private void voaParaCasa() {
        if (this.casa == null) return;
        double dx = this.casa.getX() - this.getX();
        double dy = this.casa.getY() - this.getY();
        double dz = this.casa.getZ() - this.getZ();
        double quão = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (quão <= 0.0) return;
        this.setDeltaMovement(this.getDeltaMovement()
                .add(dx / quão * 0.2, dy / quão * 0.2, dz / quão * 0.2));
    }

    /** Em casa, ela conta — e ao fim da conta, ela deixa de ser ela. */
    private void conta(ServerLevel level) {
        this.conta++;
        if (this.conta == FALA_AOS) {
            this.fala(level, "tc.lilith.summon");
            level.playSound(null, this.blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON,
                    SoundSource.HOSTILE, 1.0f, 0.7f);
            return;
        }
        if (this.conta < VIRA_AOS) return;

        this.conta = 0;
        this.viraLilith(level);
    }

    /**
     * <b>E então ela não é mais ela.</b>
     *
     * <p>Lilith toma o lugar dela — a mesma casa, o mesmo olhar — e o estouro que vem junto é de seis, com
     * fogo. Quem estiver ao lado quando isso acontecer aprende a ficar longe.
     */
    private void viraLilith(ServerLevel level) {
        var lilith = net.thaumcraft.occulta.OccultaEntities.LILITH.create(level,
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (lilith == null) return;

        lilith.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        lilith.setPersistenceRequired();
        lilith.finalizeSpawn(level, level.getCurrentDifficultyAt(this.blockPosition()),
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED, null);
        level.addFreshEntity(lilith);

        this.fala(level, "tc.lilith.summon2");
        level.sendParticles(net.minecraft.core.particles.SpellParticleOption.create(
                        net.minecraft.core.particles.ParticleTypes.INSTANT_EFFECT, 1.0f, 1.0f, 1.0f, 1.0f),
                this.getX(), this.getY() + 1.0, this.getZ(), 32, 1.0, 1.0, 1.0, 0.0);
        this.discard();
        level.explode(lilith, lilith.getX(), lilith.getY(), lilith.getZ(), ESTOURO,
                true, Level.ExplosionInteraction.MOB);
    }

    /** O que ela diz, e só a quem a chamou. */
    private void fala(ServerLevel level, String oquê) {
        if (this.dono == null) return;
        Player quem = level.getPlayerByUUID(this.dono);
        if (quem == null) return;
        quem.sendSystemMessage(Component.translatable(oquê)
                .withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
    }

    // ------------------------------------------------------------------ o lago

    /**
     * Se aqui há um <b>lago de lava</b>: a cruz de cinco colunas, e seis blocos de lava à volta.
     *
     * <p>A conta do original pede muito: um lago natural à superfície raramente chega. Quem quiser Lilith
     * costuma ter de <b>fazer</b> o lago — e cavar um lago de lava de treze blocos de boca é, por si só, uma
     * declaração de intenções.
     */
    public static boolean éLago(ServerLevel level, BlockPos meio) {
        if (!éColuna(level, meio)) return false;
        if (!éColuna(level, meio.east()) || !éColuna(level, meio.west())) return false;
        if (!éColuna(level, meio.south()) || !éColuna(level, meio.north())) return false;

        for (int dx = -LAGO; dx <= LAGO; dx++) {
            for (int dz = -LAGO; dz <= LAGO; dz++) {
                if (dx * dx + dz * dz > LAGO * LAGO) continue;
                if (!level.getBlockState(meio.offset(dx, 0, dz)).is(Blocks.LAVA)) return false;
            }
        }
        return true;
    }

    /**
     * Uma coluna de lago: <b>lava com dois andares de ar por cima</b>.
     *
     * <p>O original ainda tenta medir a fundura, e não mede: o laço dele é {@code for (dy = y - 4; dy < dy;)},
     * que não corre nenhuma vez. Fica como está — e o que o jogador sente é o que o original faz.
     */
    private static boolean éColuna(ServerLevel level, BlockPos onde) {
        if (!level.getBlockState(onde).is(Blocks.LAVA)) return false;
        for (int dy = 1; dy <= AR; dy++) {
            if (!level.isEmptyBlock(onde.above(dy))) return false;
        }
        return true;
    }

    // ------------------------------------------------------------------ e o resto

    @Override
    public Component getName() {
        return this.hasCustomName() ? super.getName()
                : Component.translatable("entity.thaumcraft.follower");
    }

    @Override
    public void addAdditionalSaveData(ValueOutput dados) {
        super.addAdditionalSaveData(dados);
        if (this.casa != null) dados.store("Casa", BlockPos.CODEC, this.casa);
        if (this.dono != null) dados.store("Dono", net.minecraft.core.UUIDUtil.CODEC, this.dono);
        dados.putInt("Conta", this.conta);
    }

    @Override
    public void readAdditionalSaveData(ValueInput dados) {
        super.readAdditionalSaveData(dados);
        this.casa = dados.read("Casa", BlockPos.CODEC).orElse(null);
        this.dono = dados.read("Dono", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        this.conta = dados.getIntOr("Conta", 0);
    }
}
