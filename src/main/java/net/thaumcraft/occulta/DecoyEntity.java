package net.thaumcraft.occulta;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


/**
 * O <b>Sósia</b>: o {@code EntityFollower} do Witchery, no <b>tipo cinco</b>.
 *
 * <p>É o que a <b>Granada de Duplicação</b> larga onde bate: uma cópia de quem a atirou, com a cara dele, o
 * nome dele e a armadura dele — e que serve para uma coisa só, <b>levar a surra no lugar dele</b>.
 *
 * <p>De dois em dois segundos ele <b>rouba a atenção</b>: todo bicho hostil a dezesseis casas que estivesse
 * perseguindo o dono passa a persegui-lo a ele. Dez segundos depois ele some, e quem estava atrás dele fica
 * sem nada — que é o tempo de alguém fugir.
 *
 * <p>Ele <b>não bate</b> e não se defende. Não é um aliado: é uma isca com a sua cara.
 */
public class DecoyEntity extends PathfinderMob {
    /** Quanto ele vive, e de quanto em quanto rouba a atenção. */
    public static final int PRAZO = 200;
    public static final int ROUBA_A_CADA = 40;
    public static final int ROUBA_NO_RESTO = 5;

    /** E a que distância: a caixa do original. */
    public static final double LONGE_XZ = 16.0;
    public static final double LONGE_Y = 8.0;

    /** O passo dele, que é o do seguidor do original. */
    public static final double PASSO = 0.3;

    /**
     * <b>De quem ele é a cópia</b>, pelo nome: o {@code setSkin} do original, que guarda o nome e não o
     * número.
     *
     * <p>Viaja para o lado de cá porque é dele que sai a <b>pele</b>: o desenhista procura essa pessoa no
     * mundo e usa a pele dela. É a única coisa deste bicho que o lado de cá precisa de saber.
     *
     * <p>É o nome e não o número porque é assim no original — e porque um sósia dura <b>dez segundos</b>,
     * tempo em que ninguém troca de nome.
     */
    private static final EntityDataAccessor<String> DONO =
            SynchedEntityData.defineId(DecoyEntity.class, EntityDataSerializers.STRING);

    private int prazo = PRAZO;

    /**
     * E o <b>número</b> dele, que fica só deste lado.
     *
     * <p>O original procura o dono <b>pelo nome</b>, porque em 2014 era o que havia à mão. Aqui o nome
     * continua viajando — é dele que sai a pele —, mas quem o ódio segue é o número: um nome trocado no
     * meio dos dez segundos deixaria a isca sem dono.
     */
    @Nullable
    private UUID número;

    public DecoyEntity(EntityType<? extends DecoyEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, PASSO);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DONO, "");
    }

    /**
     * Ele <b>vagueia e olha</b>, e nada mais.
     *
     * <p>No original o seguidor do tipo cinco ganha só o {@code aiWander}: sem alvo, sem revide, sem fugir.
     * Um sósia que lutasse não seria uma isca.
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    // ------------------------------------------------------------------ de quem ele é

    /** O nome de quem ele copia, ou vazio. */
    public String dono() {
        return this.entityData.get(DONO);
    }

    /** O número dele, que é por onde o ódio o acha. */
    public @Nullable UUID número() {
        return this.número;
    }

    /** E a pessoa em si, se ela estiver neste mundo. */
    public @Nullable Player deQuem(net.minecraft.server.level.ServerLevel level) {
        return this.número == null ? null : level.getPlayerByUUID(this.número);
    }

    /**
     * Veste-o de alguém: o nome, a cara e a <b>armadura</b>.
     *
     * <p>A armadura é copiada <b>uma vez</b>, no instante em que ele nasce. O original a copia na primeira
     * batida dele; aqui copia-se logo, que dá no mesmo e poupa uma pergunta por batida.
     */
    public void veste(Player quem) {
        this.entityData.set(DONO, quem.getGameProfile().name());
        this.número = quem.getUUID();
        this.setCustomName(quem.getName().copy());
        for (EquipmentSlot casa : EquipmentSlot.values()) {
            if (casa.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            this.setItemSlot(casa, quem.getItemBySlot(casa).copy());
        }
    }

    // ------------------------------------------------------------------ a isca

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel mundo)) return;
        if (this.tickCount % ROUBA_A_CADA == ROUBA_NO_RESTO) this.rouba(mundo);
        if (--this.prazo > 0) return;
        mundo.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1.0, this.getZ(),
                16, 0.4, 0.8, 0.4, 0.0);
        this.discard();
    }

    /**
     * <b>Rouba a atenção</b>: o {@code attractAttention} do original.
     *
     * <p>Todo bicho hostil que estivesse mirando o dono passa a mirar o sósia. E só esses: um bicho que já
     * estivesse atrás de outra pessoa não se distrai — a isca é para quem a atirou.
     */
    public void rouba(ServerLevel level) {
        if (this.número == null) return;

        /*
         * Pergunta-se ao <b>alvo</b> de cada bicho se ele é o dono, e não ao mundo onde o dono está. Dá no
         * mesmo e não depende de o dono estar carregado: um bicho que esteja mirando alguém tem esse
         * alguém à mão.
         */
        AABB caixa = this.getBoundingBox().inflate(LONGE_XZ, LONGE_Y, LONGE_XZ);
        for (Monster bicho : level.getEntitiesOfClass(Monster.class, caixa)) {
            net.minecraft.world.entity.LivingEntity alvo = bicho.getTarget();
            if (alvo == null || !this.número.equals(alvo.getUUID())) continue;
            bicho.setTarget(this);
            bicho.setLastHurtByMob(this);
        }
    }

    /** Quanto lhe resta, para a prova o poder ver. */
    public int prazo() {
        return this.prazo;
    }

    public void põePrazo(int batidas) {
        this.prazo = batidas;
    }

    // ------------------------------------------------------------------ guardar

    @Override
    protected void addAdditionalSaveData(ValueOutput saída) {
        super.addAdditionalSaveData(saída);
        saída.putInt("Prazo", this.prazo);
        saída.putString("Dono", this.dono());
        if (this.número != null) saída.store("Número", net.minecraft.core.UUIDUtil.CODEC, this.número);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput entrada) {
        super.readAdditionalSaveData(entrada);
        this.prazo = entrada.getIntOr("Prazo", PRAZO);
        this.entityData.set(DONO, entrada.getStringOr("Dono", ""));
        this.número = entrada.read("Número", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
    }

    /** O sósia não grita e não geme: ele é uma cópia calada. */
    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return null;
    }

    /** E ele não conta como bicho para a contagem do mundo: some sozinho. */
    @Override
    public boolean removeWhenFarAway(double distância) {
        return false;
    }

    /** Nem é alvo de nada que procure por tipo: só por ódio roubado. */
    @Override
    public boolean canBeLeashed() {
        return false;
    }
}
