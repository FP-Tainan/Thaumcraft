package net.thaumcraft.shattered;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Set;

/**
 * O Monólito: o {@code EntityMonolith} das Portas Dimensionais.
 *
 * <p>Uma lousa preta que paira nos Reinos Fragmentados e no Limbo, e que não faz nada senão olhar. Quanto mais
 * tempo se fica no campo de vista dela, mais ela abre o olho — e chegando ao fim, quem estava a olhar acorda no
 * Limbo, a setecentos blocos de altura, a cair.
 *
 * <p>Não se mata, não se empurra, não se atravessa e não morre: o original dá-lhe 57005 de vida e diz que nunca
 * está viva. Fora dos mundos do ramo, ela some.
 */
public class MonolithEntity extends Mob implements Enemy {
    /** Os números do original. */
    public static final int MAX_AGGRO = 250;
    public static final int MAX_AGGRO_HARMLESS = 180;
    public static final int RANGE = 35;
    /** As dezoito caras da lousa, do olho fechado ao olho aberto. */
    public static final int FACES = 18;

    private static final EntityDataAccessor<Integer> AGGRO =
            SynchedEntityData.defineId(MonolithEntity.class, EntityDataSerializers.INT);

    private int aggro;
    private final int aggroCap;
    public float pitchLevel;

    public MonolithEntity(EntityType<? extends MonolithEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        // sem corpo, ela atravessaria o chão e cairia para fora do mundo: ela paira
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.aggroCap = Mth.nextInt(this.random, 25, 100);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 57005.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(AGGRO, 0);
    }

    /** Se aquele mundo é dos que mandam o intruso para o Limbo. */
    public boolean isDangerous() {
        return this.level().dimension() == ShatteredRealms.LIMBO;
    }

    public int aggro() {
        return this.entityData.get(AGGRO);
    }

    /** De zero a dezoito, a cara que a lousa mostra. */
    public int face() {
        return Mth.clamp(FACES * this.aggro() / MAX_AGGRO, 0, FACES);
    }

    public float aggroProgress() {
        return this.aggro() / (float) MAX_AGGRO;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    /**
     * O original diz que ele nunca está vivo, para ninguém o contar nem lhe bater. O jogo de hoje não desenha nem
     * acompanha o que não está vivo, então aqui ele está — e o que o original queria com isso vem do invulnerável
     * e do não se empurrar, que ficam.
     */
    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    /** Bater nela só a irrita. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        this.aggro = MAX_AGGRO;
        return false;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!(this.level() instanceof ServerLevel server)) {
            this.aggro = this.entityData.get(AGGRO);
            return;
        }
        if (!ShatteredRealms.isOurs(this.level())) {
            this.discard();
            return;
        }

        Player quem = this.level().getNearestPlayer(this, RANGE);
        boolean visto = quem != null && quem.hasLineOfSight(this);
        this.updateAggro(quem, visto);
        if (quem == null) return;

        this.facePlayer(quem);
        if (this.aggro >= MAX_AGGRO && visto && this.isDangerous() && !quem.getAbilities().instabuild) {
            this.aggro = 0;
            this.send(server, quem);
        }
    }

    /** O {@code updateAggroLevel}: no Limbo ela sobe devagar, fora dele depressa, e desce quando não a olham. */
    private void updateAggro(Player quem, boolean visto) {
        if (visto) {
            this.aggro += this.level().dimension() == ShatteredRealms.LIMBO ? (this.isDangerous() ? 1 : 36) : 3;
        } else if (this.isDangerous()) {
            if (this.aggro > this.aggroCap) this.aggro--;
            else if (quem != null && this.aggro < this.aggroCap) this.aggro++;
        } else {
            this.aggro -= 3;
        }
        this.aggro = Mth.clamp(this.aggro, 0, this.isDangerous() ? MAX_AGGRO : MAX_AGGRO_HARMLESS);
        this.entityData.set(AGGRO, this.aggro);
    }

    /** Manda quem a olhou para o Limbo, a cair de setecentos. */
    private void send(ServerLevel server, Player quem) {
        ServerLevel limbo = ShatteredRealms.limbo(server.getServer());
        if (limbo == null) return;
        double x = quem.getX() + (server.getRandom().nextDouble() - 0.5) * 200.0;
        double z = quem.getZ() + (server.getRandom().nextDouble() - 0.5) * 200.0;
        if (quem instanceof ServerPlayer jogador) {
            jogador.teleportTo(limbo, x, 700.0, z, Set.of(), jogador.getYRot(), jogador.getXRot(), false);
        }
        limbo.playSound(null, net.minecraft.core.BlockPos.containing(x, 700.0, z),
                SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 13.0f, 1.0f);
    }

    /** Ela vira-se para quem a olha, e inclina-se. */
    private void facePlayer(Player quem) {
        double dx = quem.getX() - this.getX();
        double dz = quem.getZ() - this.getZ();
        double dy = quem.getEyeY() - (this.getY() + 1.5);
        double plano = Math.sqrt(dx * dx + dz * dz);
        float volta = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        this.pitchLevel = (float) (-(Math.atan(dy / Math.max(0.001, plano)) * 180.0 / Math.PI));
        this.setYRot(volta);
        this.setYHeadRot(volta);
        this.yBodyRot = volta;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Aggro", this.aggro);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.aggro = input.getIntOr("Aggro", 0);
        this.entityData.set(AGGRO, this.aggro);
    }
}
