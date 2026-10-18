package net.thaumcraft.occulta.familiar;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.OccultaSounds;
import net.thaumcraft.occulta.TaglockItem;
import net.thaumcraft.occulta.spirit.FlyerGoals;
import net.thaumcraft.occulta.waystone.Waystones;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Macaco de Asas</b>: o {@code EntityWingedMonkey} do Witchery.
 *
 * <p>É o <b>táxi</b> do ramo. Doma-se com <b>carne crua</b> — uma vez em três —, pinta-se com
 * <b>tinta</b>, e quem lhe der uma <b>Pedra de Caminho presa</b> ou um <b>frasco de alguém</b> monta-lhe
 * às costas e <b>é levado até lá pelo ar</b>.
 *
 * <p>Vinte de vida, quatro de murro na folha de números e <b>cinco de armadura</b> por cima do que
 * vestir. Não some, respira debaixo de água e não se magoa a cair, que é o que todo bicho voador deste
 * ramo faz.
 *
 * <h2>E o murro dele é dois</h2>
 *
 * <p>Não quatro. O original põe quatro na folha de números e depois escreve, no murro, <b>dois a seco</b>
 * — sem olhar para a folha, sem encantamento, sem nada. Está assim aqui, porque é assim que ele bate.
 *
 * <h2>A cor das asas</h2>
 *
 * <p>Sorteada ao nascer entre as dezesseis da lã de 2014, com <b>uma em cem</b> de sair a <b>branca</b> —
 * que é a do índice zero, a única que o desenhista não pinta. Tinta na mão do dono troca-a.
 *
 * <h2>E a pele de domado, que ninguém vê</h2>
 *
 * <p>Domando-o, ele sorteia uma de <b>três</b>. O desenhista do original <b>nunca a lê</b> — ele tem uma
 * folha só —, de modo que ela não se vê em lugar nenhum. Fica aqui porque está lá.
 *
 * <h2>E tem hora para acabar, se lhe puserem uma</h2>
 *
 * <p>Como a Alma Perdida: um macaco de empréstimo some quando o prazo acaba <b>ou quando o alvo dele
 * morre</b>, o que vier primeiro.
 */
public class WingedMonkeyEntity extends TamableAnimal implements FlyerGoals.ComCasa {
    /** Os números dele. */
    public static final double VIDA = 20.0;
    public static final double VELOCIDADE = 0.3;
    public static final double MURRO = 4.0;

    /** E o que o murro <b>tira</b>, que é outra coisa. */
    public static final float MURRO_A_SECO = 2.0f;

    /** A armadura que ele soma por cima do que vestir. */
    public static final int COURAÇA = 5;

    /** Quantas penas ele larga, no máximo, sem sorte de espada. */
    public static final int PENAS = 3;

    /** Uma em quantas o domar pega, e quantas peles de domado há. */
    public static final int DOMA_EM = 3;
    public static final int PELES = 3;

    /** E a chance de nascer <b>branco</b>: uma em cem. */
    public static final int BRANCO_EM = 100;
    public static final int CORES = 15;

    /** Quanto a carne crua cura. */
    public static final float CURA = 10.0f;

    /** Até onde ele olha, e a que distância, ao quadrado, a mão lhe chega. */
    public static final double OLHA_A = 10.0;
    public static final double TOCA_A = 9.0;

    /** Quantas batidas o braço fica levantado depois de um murro. */
    public static final int BRAÇO = 10;

    /** O recado do murro, que é o do original. */
    public static final byte RECADO_MURRO = 4;

    /** Quanto o passageiro desce em relação a ele. */
    public static final double LEVA_A = -0.6;

    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> COR =
            net.minecraft.network.syncher.SynchedEntityData.defineId(WingedMonkeyEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> PELE =
            net.minecraft.network.syncher.SynchedEntityData.defineId(WingedMonkeyEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);

    /** O que ele leva na cabeça: a pedra presa ou o frasco de alguém. Vazio, ele não vai a lugar nenhum. */
    private ItemStack rumo = ItemStack.EMPTY;

    /** E o lugar de recuo, para quando o rumo não souber responder. */
    private double casaX;
    private double casaY;
    private double casaZ;

    private int prazo = -1;
    private int braço;

    public WingedMonkeyEntity(EntityType<? extends WingedMonkeyEntity> tipo, Level level) {
        super(tipo, level);
        /* Sem gravidade, como todo EntityFlyingTameable: o andar dele é o empurrão das metas. */
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.MOVEMENT_SPEED, VELOCIDADE)
                .add(Attributes.ATTACK_DAMAGE, MURRO)
                .add(Attributes.ARMOR, COURAÇA);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation caminho = new FlyingPathNavigation(this, level);
        caminho.setCanOpenDoors(false);
        caminho.setCanFloat(true);
        return caminho;
    }

    /** As onze metas do original, nas prioridades dele. */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FlyerGoals.SentaEFica(this));
        this.goalSelector.addGoal(2, new FlyerGoals.VaiParaCasa(this));
        this.goalSelector.addGoal(3, new FlyerGoals.Investe(this, true));
        this.goalSelector.addGoal(4, new FlyerGoals.Tentação(this, WingedMonkeyEntity::tenta, true));
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0, 14.0f, 5.0f));
        this.goalSelector.addGoal(8, new FlyerGoals.Acasala(this));
        this.goalSelector.addGoal(9, new FlyerGoals.Pousa(this, true));
        this.goalSelector.addGoal(10, new FlyerGoals.Vagueia(this, OLHA_A));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, (float) OLHA_A, 0.2f));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    /**
     * A cor nasce aqui, que é onde o original a sorteia — no {@code entityInit}, dos dois lados. O que
     * vale é a do servidor, que é a que chega a quem joga logo a seguir.
     */
    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder construtor) {
        super.defineSynchedData(construtor);
        construtor.define(COR, this.random.nextInt(BRANCO_EM) == 0 ? 0 : this.random.nextInt(CORES) + 1);
        construtor.define(PELE, 0);
    }

    /** O que o tenta, e o que o doma: <b>carne crua</b>, de porco ou de boi. */
    public static boolean tenta(ItemStack oquê) {
        return oquê.is(Items.PORKCHOP) || oquê.is(Items.BEEF);
    }

    // ------------------------------------------------------------------ a cor e a pele

    public int cor() {
        return this.entityData.get(COR) & 15;
    }

    public void cor(int qual) {
        this.entityData.set(COR, qual & 15);
    }

    public int pele() {
        return this.entityData.get(PELE);
    }

    public void pele(int qual) {
        this.entityData.set(PELE, qual);
    }

    // ------------------------------------------------------------------ o prazo

    /** <b>Dá-lhe um prazo</b>: o {@code setTimeToLive} do original. */
    public void prazo(int quantas) {
        this.prazo = quantas;
    }

    public boolean deEmpréstimo() {
        return this.prazo != -1;
    }

    /**
     * <b>O relógio do prazo.</b> Um macaco de empréstimo some quando o prazo acaba <b>ou quando o alvo
     * dele morre</b> — o que vier primeiro, que é o que o original escreve numa condição só.
     *
     * @return se acabou agora
     */
    public boolean oPrazo(ServerLevel level) {
        if (this.prazo == -1 || this.isRemoved()) return false;
        LivingEntity alvo = this.getTarget();
        if (--this.prazo != 0 && alvo != null && alvo.isAlive()) return false;

        level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.5, this.getZ(),
                16, 1.0, 1.0, 1.0, 0.0);
        this.discard();
        return true;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        /* O original para a navegação a cada batida de IA: quem deriva não anda por caminho. */
        this.getNavigation().stop();
        super.customServerAiStep(level);
        this.oPrazo(level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.braço > 0) this.braço--;
    }

    /** Quantas batidas faltam para o braço do murro baixar. */
    public int braço() {
        return this.braço;
    }

    @Override
    public void handleEntityEvent(byte recado) {
        if (recado == RECADO_MURRO) {
            this.braço = BRAÇO;
        } else {
            super.handleEntityEvent(recado);
        }
    }

    // ------------------------------------------------------------------ o rumo

    /**
     * <b>Onde ele vai dar.</b> Uma pedra presa responde com o lugar dela; um frasco de alguém responde com
     * <b>onde essa pessoa está agora</b> — e é por isso que o rumo é uma pilha guardada, e não três
     * números. Não sabendo responder, vale o lugar de recuo.
     */
    private Vec3 ondeDá() {
        achar: {
            if (this.rumo.isEmpty()) break achar;

            var lugar = Waystones.lugar(this.rumo);
            if (lugar != null) {
                if (lugar.mundo() != this.level().dimension()) break achar;
                return new Vec3(lugar.onde().getX() + 0.5, lugar.onde().getY() + 1.5,
                        lugar.onde().getZ() + 0.5);
            }

            var preso = TaglockItem.bound(this.rumo);
            if (preso != null && this.level() instanceof ServerLevel level) {
                Entity quem = level.getEntity(preso.owner());
                if (quem instanceof LivingEntity vivo) {
                    return new Vec3(vivo.getX(), vivo.getY() + 1.0, vivo.getZ());
                }
            }
        }
        return new Vec3(this.casaX, this.casaY, this.casaZ);
    }

    @Override
    public boolean temCasa() {
        return !this.rumo.isEmpty();
    }

    @Override
    public double casaX() {
        return this.ondeDá().x;
    }

    @Override
    public double casaY() {
        return this.ondeDá().y;
    }

    @Override
    public double casaZ() {
        return this.ondeDá().z;
    }

    @Override
    public void esqueceACasa() {
        this.rumo = ItemStack.EMPTY;
    }

    /** Põe-lhe um rumo, com o lugar de recuo onde ele está agora. */
    public void rumo(ItemStack oquê) {
        this.rumo = oquê.copy();
        this.casaX = this.getX();
        this.casaY = this.getY();
        this.casaZ = this.getZ();
    }

    /** E põe-lhe um rumo a seco, por coordenadas. */
    public void casa(BlockPos onde) {
        ItemStack pedra = new ItemStack(OccultaItems.BOUND_WAYSTONE);
        pedra.set(OccultaComponents.WAYSTONE, new Waystones.Lugar(this.level().dimension(), onde));
        this.rumo = pedra;
        this.casaX = onde.getX() + 0.5;
        this.casaY = onde.getY() + 1.5;
        this.casaZ = onde.getZ() + 0.5;
    }

    // ------------------------------------------------------------------ o toque

    /**
     * <b>O que a mão faz com ele</b>, e é muita coisa.
     *
     * <p>Domado e do dono: <b>tinta</b> pinta-lhe as asas; uma <b>Pedra de Caminho presa</b> dá-lhe o
     * rumo, <b>prende-se de volta onde ele está</b> — para haver viagem de volta — e põe quem a deu às
     * costas dele; um <b>frasco de alguém</b> dá-lhe o rumo dessa pessoa; <b>carne crua</b> cura-o dez; e
     * a mão vazia <b>senta-o</b>, estando a menos de três blocos.
     *
     * <p>Por domar: <b>carne crua</b>, uma vez em três, e ele é seu.
     */
    @Override
    public InteractionResult mobInteract(Player quem, InteractionHand mão) {
        if (this.deEmpréstimo()) return InteractionResult.SUCCESS;

        ItemStack oquê = quem.getItemInHand(mão);
        if (!this.isTame()) {
            if (!tenta(oquê)) return super.mobInteract(quem, mão);
            /* Com carne na mão mas longe, o original devolve <b>falso</b>: não doma nem apaixona. */
            if (quem.distanceToSqr(this) >= TOCA_A) return InteractionResult.PASS;
            if (!(this.level() instanceof ServerLevel level)) return InteractionResult.SUCCESS;

            oquê.consume(1, quem);
            if (this.random.nextInt(DOMA_EM) == 0) {
                this.tame(quem);
                this.pele(1 + this.random.nextInt(PELES));
                this.setPersistenceRequired();
                this.setOrderedToSit(true);
                level.broadcastEntityEvent(this, (byte) 7);
            } else {
                level.broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.SUCCESS;
        }

        if (!this.isOwnedBy(quem)) return super.mobInteract(quem, mão);

        /*
         * A carne crua <b>cura-o dez</b> — e <b>de barriga cheia</b> ela deixa de curar e passa a ser o
         * que o apaixona, porque é aí que o original deixa cair a mão no acasalar do jogo.
         */
        if (tenta(oquê)) {
            if (this.getHealth() >= this.getMaxHealth()) return super.mobInteract(quem, mão);
            if (this.level().isClientSide()) return InteractionResult.SUCCESS;
            this.heal(CURA);
            oquê.consume(1, quem);
            return InteractionResult.SUCCESS;
        }

        if (!(this.level() instanceof ServerLevel level)) return InteractionResult.SUCCESS;

        // a tinta pinta-lhe as asas
        var tinta = oquê.get(net.minecraft.core.component.DataComponents.DYE);
        if (tinta != null) {
            this.cor(tinta.getId());
            oquê.consume(1, quem);
            return InteractionResult.SUCCESS;
        }

        // a pedra presa dá-lhe o rumo, e prende-se de volta onde ele está
        if ((oquê.is(OccultaItems.BOUND_WAYSTONE) || oquê.is(OccultaItems.BLOODED_WAYSTONE))
                && Waystones.presa(oquê)) {
            this.rumo(oquê);
            if (oquê.is(OccultaItems.BOUND_WAYSTONE)) {
                oquê.set(OccultaComponents.WAYSTONE,
                        new Waystones.Lugar(level.dimension(), this.blockPosition()));
            }
            this.setOrderedToSit(false);
            quem.startRiding(this);
            level.sendParticles(net.minecraft.core.particles.SpellParticleOption.create(
                            ParticleTypes.INSTANT_EFFECT, 1.0f, 1.0f, 1.0f, 1.0f),
                    this.getX(), this.getY() + 0.5, this.getZ(), 16, 0.5, 0.5, 0.5, 0.0);
            return InteractionResult.SUCCESS;
        }

        // e um frasco cheio dá-lhe o rumo de quem está lá dentro
        if (oquê.is(OccultaItems.TAGLOCK) && TaglockItem.isBound(oquê)) {
            this.rumo(oquê);
            oquê.consume(1, quem);
            level.sendParticles(net.minecraft.core.particles.SpellParticleOption.create(
                            ParticleTypes.INSTANT_EFFECT, 1.0f, 1.0f, 1.0f, 1.0f),
                    this.getX(), this.getY() + 0.5, this.getZ(), 16, 0.5, 0.5, 0.5, 0.0);
            return InteractionResult.SUCCESS;
        }

        // a mão vazia senta-o
        if (this.distanceToSqr(quem) < TOCA_A) {
            this.setOrderedToSit(!this.isOrderedToSit());
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(quem, mão);
    }

    // ------------------------------------------------------------------ o murro, e quem monta

    /**
     * <b>Dois a seco.</b> O original escreve o murro com o número na mão e nunca olha para a folha de
     * números — de modo que os quatro que ela diz não chegam a ninguém.
     */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity alvo) {
        this.braço = BRAÇO;
        level.broadcastEntityEvent(this, RECADO_MURRO);
        return alvo.hurtServer(level, this.damageSources().mobAttack(this), MURRO_A_SECO);
    }

    /**
     * <b>Sentar é uma coisa só.</b> Em 2014 o {@code setSitting} escrevia <b>direto no campo
     * sincronizado</b>, de modo que mandar sentar e <b>parecer sentado</b> eram a mesma chamada. O jogo de
     * hoje partiu-as em duas e deixa a meta de sentar ligar uma à outra; aqui elas voltam a andar juntas,
     * que é o que faz a pose chegar a quem olha.
     */
    @Override
    public void setOrderedToSit(boolean sim) {
        super.setOrderedToSit(sim);
        this.setInSittingPose(sim);
    }

    /** <b>Um de cada vez</b>, e quem monta vai pendurado por baixo dele. */
    @Override
    protected boolean canAddPassenger(Entity quem) {
        return this.getPassengers().isEmpty();
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity quem, EntityDimensions tamanho, float escala) {
        return new Vec3(0.0, quem.getBbHeight() * LEVA_A, 0.0);
    }

    // ------------------------------------------------------------------ o resto

    /** Bater nele <b>levanta-o</b>: um macaco sentado que apanha não fica sentado. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float quanto) {
        if (this.isInvulnerableTo(level, fonte)) return false;
        this.setOrderedToSit(false);
        return super.hurtServer(level, fonte, quanto);
    }

    /** E ele larga <b>penas</b>, a não ser que seja de empréstimo. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource fonte, boolean matouGente) {
        super.dropCustomDeathLoot(level, fonte, matouGente);
        if (this.deEmpréstimo()) return;
        int quantas = this.random.nextInt(PENAS);
        for (int i = 0; i < quantas; i++) {
            this.spawnAtLocation(level, new ItemStack(Items.FEATHER), 0.0f);
        }
    }

    @Override
    public boolean isFood(ItemStack oquê) {
        return tenta(oquê);
    }

    /** <b>Dois domados</b>, e não um domado e um bravo: o {@code canMateWith} do original. */
    @Override
    public boolean canMate(Animal outro) {
        if (outro == this || !this.isTame()) return false;
        if (!(outro instanceof WingedMonkeyEntity macaco) || !macaco.isTame()) return false;
        return this.isInLove() && macaco.isInLove();
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob par) {
        WingedMonkeyEntity filho = OccultaEntities.WINGED_MONKEY.create(level, EntitySpawnReason.BREEDING);
        if (filho == null) return null;
        if (this.isTame()) {
            filho.setPersistenceRequired();
            filho.pele(this.pele());
            filho.cor(this.cor());
        }
        return filho;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean causeFallDamage(double quanto, float fator, DamageSource fonte) {
        return false;
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double longe) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return OccultaSounds.MONKEY_SAY.value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource fonte) {
        return OccultaSounds.MONKEY_HIT.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return OccultaSounds.MONKEY_DEATH.value();
    }

    /** Ele fala de tanto em tanto: o dobro do compasso de todo mundo. */
    @Override
    public int getAmbientSoundInterval() {
        return super.getAmbientSoundInterval() * 2;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput saída) {
        super.addAdditionalSaveData(saída);
        saída.putInt("FeatherColor", this.cor());
        saída.putInt("TameSkin", this.pele());
        saída.putInt("SuicideIn", this.prazo);
        if (!this.rumo.isEmpty()) saída.store("WITCWaypoint", ItemStack.CODEC, this.rumo);
        saída.putDouble("HomeX", this.casaX);
        saída.putDouble("HomeY", this.casaY);
        saída.putDouble("HomeZ", this.casaZ);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput entrada) {
        super.readAdditionalSaveData(entrada);
        this.cor(entrada.getIntOr("FeatherColor", this.cor()));
        this.pele(entrada.getIntOr("TameSkin", 0));
        this.prazo = entrada.getIntOr("SuicideIn", -1);
        this.rumo = entrada.read("WITCWaypoint", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.casaX = entrada.getDoubleOr("HomeX", 0.0);
        this.casaY = entrada.getDoubleOr("HomeY", 0.0);
        this.casaZ = entrada.getDoubleOr("HomeZ", 0.0);
    }
}
