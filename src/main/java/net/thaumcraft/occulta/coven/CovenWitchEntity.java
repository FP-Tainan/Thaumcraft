package net.thaumcraft.occulta.coven;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * A <b>Bruxa do Coven</b>: o {@code EntityCovenWitch} do Witchery.
 *
 * <p>Ela não é um monstro nem um aldeão — é alguém com quem se <b>negocia</b>. Fala-se com ela, ela pede uma
 * coisa; aceita-se, e ela fica à espera. Trazido o que pediu, <b>entra no coven de quem trouxe</b> — e um coven
 * pesa nos ritos do ofício, que fazem mais com mais bruxas em volta.
 *
 * <p><b>Ela ganha nome ao ser falada</b>, e é o nome que leva para sempre: um dos cento e oito mil que saem das
 * duas listas do original. E tem uma de <b>cinco caras</b>, sorteada ao nascer.
 *
 * <p>Trinta de vida, atira poções como a bruxa do próprio jogo, e <b>não ataca quem não a ataca</b>. Mas quem a
 * enganar — aceitar e trazer o pedido com o coven já cheio — faz dela inimiga, e aí ela vira.
 *
 * <p><b>Ela só negocia com quem tem familiar</b>, como no original. Isto esteve costurado — um método que
 * respondia sempre que sim — enquanto os familiares não existiam; com a fatia deles, a tranca ficou de pé.
 */
public class CovenWitchEntity extends PathfinderMob implements RangedAttackMob {
    /** As cinco caras do original. */
    public static final int CARAS = 5;

    private static final EntityDataAccessor<Integer> CARA =
            SynchedEntityData.defineId(CovenWitchEntity.class, EntityDataSerializers.INT);

    /** De quem ela está à espera, e o que pediu. */
    private @org.jetbrains.annotations.Nullable java.util.UUID pedidoA;
    private int qualPedido = -1;
    private int faltamAinda;
    private boolean aceitou;
    private boolean virada;

    public CovenWitchEntity(EntityType<? extends CovenWitchEntity> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder attributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CARA, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 60, 10.0f));
        this.goalSelector.addGoal(6, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(7, new MoveThroughVillageGoal(this, 0.6, false, 4, () -> true));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    // ------------------------------------------------------------------ a cara e o nome

    public int cara() {
        return this.entityData.get(CARA);
    }

    @Override
    public @org.jetbrains.annotations.Nullable net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance quão, EntitySpawnReason razão,
            @org.jetbrains.annotations.Nullable net.minecraft.world.entity.SpawnGroupData dado) {
        this.entityData.set(CARA, this.random.nextInt(CARAS));
        return super.finalizeSpawn(level, quão, razão, dado);
    }

    /** Ela só ganha nome quando alguém fala com ela: é o {@code generateWitchName} do original. */
    private void ganhaNome() {
        if (this.hasCustomName()) return;
        this.setCustomName(Component.literal(WitchNames.sorteia(this.random)));
    }

    // ------------------------------------------------------------------ a conversa

    /**
     * Se esta pessoa pode ter coven: ela só negocia com quem tem <b>familiar</b>.
     *
     * <p>Esta era a costura que a fatia da Bruxa do Coven deixou — um método que respondia sempre que sim,
     * porque os familiares ainda não existiam. <b>Agora existem</b>, e a tranca do original está de pé: foi a
     * única linha que mudou, como estava escrito que seria.
     */
    private static boolean temFamiliar(Player quem) {
        return net.thaumcraft.occulta.familiar.Familiars.temAlgum(quem);
    }

    @Override
    public InteractionResult mobInteract(Player quem, InteractionHand mão) {
        if (this.level().isClientSide() || mão != InteractionHand.MAIN_HAND) {
            return InteractionResult.SUCCESS;
        }
        if (this.virada) return InteractionResult.PASS;

        this.ganhaNome();

        if (Coven.cheio(quem)) {
            diz(quem, "covenfull");
            this.esquece();
            return InteractionResult.SUCCESS;
        }
        if (!temFamiliar(quem)) {
            diz(quem, "notinterested");
            return InteractionResult.SUCCESS;
        }

        // ainda não falei com ninguém: ofereço
        if (this.pedidoA == null) {
            this.pedidoA = quem.getUUID();
            this.qualPedido = this.random.nextInt(CovenQuest.TODAS.size());
            this.faltamAinda = CovenQuest.TODAS.get(this.qualPedido).quantos();
            this.aceitou = false;
            diz(quem, CovenQuest.TODAS.get(this.qualPedido).chave());
            return InteractionResult.SUCCESS;
        }

        if (!this.pedidoA.equals(quem.getUUID())) {
            diz(quem, "begone");
            return InteractionResult.SUCCESS;
        }

        // ofereci e ainda não aceitou: a segunda palavra é o aceitar
        if (!this.aceitou) {
            this.aceitou = true;
            if (this.level() instanceof ServerLevel level) {
                CovenQuest.TODAS.get(this.qualPedido).aceita(level, this, quem);
            }
            diz(quem, "go");
            return InteractionResult.SUCCESS;
        }

        return this.confere(quem);
    }

    /** E a terceira palavra: ver se o que ela pediu está feito. */
    private InteractionResult confere(Player quem) {
        CovenQuest pedido = CovenQuest.TODAS.get(this.qualPedido);

        if (pedido.quantos() > 0) {
            ItemStack naMao = quem.getMainHandItem();
            if (!pedido.serve(naMao)) {
                diz(quem, "questnotfinished");
                return InteractionResult.SUCCESS;
            }
            int leva = Math.min(this.faltamAinda, naMao.getCount());
            naMao.shrink(leva);
            this.faltamAinda -= leva;
            if (this.faltamAinda > 0) {
                diz(quem, "questitemsremaining");
                return InteractionResult.SUCCESS;
            }
        } else if (!this.matouOPedido()) {
            diz(quem, "questnotfinished");
            return InteractionResult.SUCCESS;
        }

        if (Coven.junta(quem, this.getUUID())) {
            diz(quem, "joinedcoven");
            this.setPersistenceRequired();
        } else {
            // o "tricked" do original: ela se sente enganada e vira
            diz(quem, "tricked");
            this.virada = true;
            this.setTarget(quem);
        }
        this.esquece();
        return InteractionResult.SUCCESS;
    }

    /** Se o bicho que ela soltou já morreu: não há nenhum vivo por perto que ela tenha soltado. */
    private boolean matouOPedido() {
        CovenQuest pedido = CovenQuest.TODAS.get(this.qualPedido);
        if (!(pedido instanceof CovenQuest.Briga briga)) return true;
        var roda = this.getBoundingBox().inflate(24.0);
        return this.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, roda,
                bicho -> bicho.getType() == briga.bicho() && bicho.isAlive()).isEmpty();
    }

    /** Se o que ela pediu é dos de buscar: serve às provas, que só sabem resolver esse sem matar nada. */
    public boolean pedidoÉDeBuscar() {
        return this.qualPedido >= 0
                && CovenQuest.TODAS.get(this.qualPedido) instanceof CovenQuest.Busca;
    }

    private void esquece() {
        this.pedidoA = null;
        this.qualPedido = -1;
        this.faltamAinda = 0;
        this.aceitou = false;
    }

    private void diz(Player aQuem, String oQue) {
        aQuem.sendSystemMessage(Component.translatable("witch.thaumcraft.say." + oQue,
                this.getDisplayName()));
    }

    // ------------------------------------------------------------------ a poção

    /**
     * A poção dela é a da bruxa do próprio jogo — no original a conta é a mesma, feita com os números de 2014.
     *
     * <p>E ela <b>só atira se estiver virada</b>: uma bruxa do coven não ataca quem não a atacou.
     */
    @Override
    public void performRangedAttack(LivingEntity alvo, float força) {
        if (!(this.level() instanceof ServerLevel level)) return;

        Vec3 andar = alvo.getDeltaMovement();
        double dx = alvo.getX() + andar.x - this.getX();
        double dy = alvo.getEyeY() - 1.1 - this.getY();
        double dz = alvo.getZ() + andar.z - this.getZ();
        double plano = Math.sqrt(dx * dx + dz * dz);

        Holder<Potion> poção = Potions.HARMING;
        if (plano >= 8.0 && !alvo.hasEffect(MobEffects.SLOWNESS)) {
            poção = Potions.SLOWNESS;
        } else if (alvo.getHealth() >= 8.0f && !alvo.hasEffect(MobEffects.POISON)) {
            poção = Potions.POISON;
        } else if (plano <= 3.0 && !alvo.hasEffect(MobEffects.WEAKNESS) && this.random.nextFloat() < 0.25f) {
            poção = Potions.WEAKNESS;
        }

        ItemStack frasco = PotionContents.createItemStack(Items.SPLASH_POTION, poção);
        Projectile.spawnProjectileUsingShoot(ThrownSplashPotion::new, level, frasco, this,
                dx, dy + plano * 0.2, dz, plano <= 2.0 ? 0.45f : 0.75f, 8.0f);

        if (!this.isSilent()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0f,
                    0.8f + this.random.nextFloat() * 0.4f);
        }
    }

    // ------------------------------------------------------------------ guardar

    @Override
    protected void addAdditionalSaveData(ValueOutput saída) {
        super.addAdditionalSaveData(saída);
        saída.putInt("Cara", this.cara());
        saída.putInt("Pedido", this.qualPedido);
        saída.putInt("Faltam", this.faltamAinda);
        saída.putBoolean("Aceitou", this.aceitou);
        saída.putBoolean("Virada", this.virada);
        if (this.pedidoA != null) saída.store("PedidoA", net.minecraft.core.UUIDUtil.CODEC, this.pedidoA);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput entrada) {
        super.readAdditionalSaveData(entrada);
        this.entityData.set(CARA, entrada.getIntOr("Cara", 0));
        this.qualPedido = entrada.getIntOr("Pedido", -1);
        this.faltamAinda = entrada.getIntOr("Faltam", 0);
        this.aceitou = entrada.getBooleanOr("Aceitou", false);
        this.virada = entrada.getBooleanOr("Virada", false);
        this.pedidoA = entrada.read("PedidoA", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return SoundEvents.WITCH_AMBIENT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource fonte) {
        return SoundEvents.WITCH_HURT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return SoundEvents.WITCH_DEATH;
    }
}
