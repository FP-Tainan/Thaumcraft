package net.thaumcraft.occulta.ghost;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaSounds;

/**
 * O <b>Poltergeist</b>: o {@code EntityPoltergeist} do Witchery.
 *
 * <p>Ele é <b>invisível para sempre</b> — nasce com a poção, e ela não acaba — e o que ele faz não é
 * brigar: é <b>bagunçar</b>. De cinco em cinco segundos ele procura uma coisa para estragar, nesta ordem:
 *
 * <ol>
 *   <li>um <b>quadro</b> ou moldura de item a dezesseis blocos: chegando perto, <b>parte-o</b>;</li>
 *   <li>senão, com <b>quem o chamou</b> a oito blocos, o <b>baú mais perto</b> que tenha alguma coisa
 *       dentro: chegando perto, <b>atira uma coisa para fora dele</b>;</li>
 *   <li>senão, qualquer <b>item largado</b> no chão: chegando perto, <b>chuta-o</b>.</li>
 * </ol>
 *
 * <p>Repare na segunda: ele só mexe nos baús <b>enquanto quem o chamou está por perto</b>. Não é um ladrão
 * — é uma assombração doméstica, e ela precisa de plateia.
 *
 * <p>O que ele não faz é dano a sério: três de ataque, e só a quem lhe bater primeiro. Os cinco por cento
 * de chance de ele vir atrás de um espectro ou de uma banshee são uma piada do original — ele é o preço
 * de chamar os outros.
 *
 * @see SummonedUndeadEntity o teto de quinze, que o torna demorado de matar
 */
public class PoltergeistEntity extends SummonedUndeadEntity {
    /** A que distância ele procura, e a que distância ele mexe. */
    public static final double PROCURA = 16.0;
    public static final double MEXE = 3.0;

    /** E a que distância o dono tem de estar para ele abrir baús. */
    public static final double O_DONO = 8.0;

    /** De quantas em quantas batidas ele procura o que estragar. */
    public static final int DE_CINCO_EM_CINCO = 100;

    /** Quanto o quadro apanha, e quanto dura o braço levantado dele. */
    public static final float NO_QUADRO = 3.0f;
    public static final int BRAÇO = 15;

    private int braço;

    public PoltergeistEntity(EntityType<? extends PoltergeistEntity> tipo, Level level) {
        super(tipo, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return net.minecraft.world.entity.monster.Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(3, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public @org.jetbrains.annotations.Nullable net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance quão,
            net.minecraft.world.entity.EntitySpawnReason razão,
            @org.jetbrains.annotations.Nullable net.minecraft.world.entity.SpawnGroupData dado) {
        var saída = super.finalizeSpawn(level, quão, razão, dado);
        this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,
                MobEffectInstance.INFINITE_DURATION, 0, false, false));
        return saída;
    }

    /** O braço levantado dele, que o desenhista mostra quando ele acabou de estragar alguma coisa. */
    public int braço() {
        return this.braço;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.braço > 0) this.braço--;
    }

    @Override
    public void handleEntityEvent(byte qual) {
        if (qual == 4) this.braço = BRAÇO;
        else super.handleEntityEvent(qual);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.isRemoved()) return;
        if (this.tickCount % DE_CINCO_EM_CINCO != 0) return;

        if (oQuadro(level)) return;
        if (oBaú(level)) return;
        oQueEstáNoChão(level);
    }

    /** <b>Um quadro</b>: chegando perto, parte-o; de longe, vai até ele. Público para as provas. */
    public boolean oQuadro(ServerLevel level) {
        AABB roda = this.getBoundingBox().inflate(PROCURA);
        for (HangingEntity quadro : level.getEntitiesOfClass(HangingEntity.class, roda)) {
            if (this.distanceToSqr(quadro) > PROCURA * PROCURA) continue;
            if (this.distanceToSqr(quadro) <= MEXE * MEXE) {
                quadro.hurtServer(level, this.damageSources().mobAttack(this), NO_QUADRO);
                this.mexeu(level);
            } else {
                this.getNavigation().moveTo(quadro.getX(), quadro.getY(), quadro.getZ(), 1.0);
            }
            return true;
        }
        return false;
    }

    /**
     * <b>Um baú</b>, e só com o dono por perto: chegando perto, atira uma coisa para fora dele.
     *
     * <p>O caldeirão e o braseiro ficam de fora, e é o original sendo bonzinho: as duas coisas em que se
     * perde uma receita inteira por um item a menos.
     */
    public boolean oBaú(ServerLevel level) {
        Player dono = this.oDono();
        if (dono == null || this.distanceToSqr(dono) > O_DONO * O_DONO) return false;

        /*
         * O original varre a <b>lista de almas carregadas</b> do mundo inteiro. O jogo de hoje não a dá,
         * e varrer trinta e três blocos ao cubo seriam trinta e cinco mil perguntas de cinco em cinco
         * segundos. Aqui pergunta-se às <b>fatias</b> em volta, que é onde as almas moram.
         */
        BlockPos achado = null;
        double menos = Double.MAX_VALUE;
        int fatia = net.minecraft.core.SectionPos.blockToSectionCoord((int) PROCURA) + 1;
        int meioX = net.minecraft.core.SectionPos.blockToSectionCoord(this.getBlockX());
        int meioZ = net.minecraft.core.SectionPos.blockToSectionCoord(this.getBlockZ());

        for (int cx = meioX - fatia; cx <= meioX + fatia; cx++) {
            for (int cz = meioZ - fatia; cz <= meioZ + fatia; cz++) {
                var pedaço = level.getChunkSource().getChunkNow(cx, cz);
                if (pedaço == null) continue;
                for (var entrada : pedaço.getBlockEntities().entrySet()) {
                    if (!(entrada.getValue() instanceof Container caixa)) continue;
                    if (caixa instanceof net.thaumcraft.occulta.kettle.KettleBlockEntity) continue;
                    if (caixa instanceof net.thaumcraft.occulta.brazier.BrazierBlockEntity) continue;
                    if (caixa.isEmpty()) continue;

                    BlockPos ali = entrada.getKey();
                    double longe = this.distanceToSqr(Vec3.atCenterOf(ali));
                    if (longe > PROCURA * PROCURA || longe >= menos) continue;
                    menos = longe;
                    achado = ali.immutable();
                }
            }
        }
        if (achado == null) return false;

        if (menos <= MEXE * MEXE) {
            atira(level, achado);
            this.mexeu(level);
        } else {
            this.getNavigation().moveTo(achado.getX(), achado.getY(), achado.getZ(), 1.0);
        }
        return true;
    }

    /** Tira uma coisa do baú e a atira para fora. */
    private static void atira(ServerLevel level, BlockPos onde) {
        if (!(level.getBlockEntity(onde) instanceof Container caixa)) return;
        var casas = new java.util.ArrayList<Integer>();
        for (int casa = 0; casa < caixa.getContainerSize(); casa++) {
            if (!caixa.getItem(casa).isEmpty()) casas.add(casa);
        }
        if (casas.isEmpty()) return;

        int qual = casas.get(level.getRandom().nextInt(casas.size()));
        ItemStack tirou = caixa.removeItem(qual, 1);
        if (tirou.isEmpty()) return;
        caixa.setChanged();

        var caiu = new ItemEntity(level, onde.getX() + 0.5, onde.getY() + 0.5, onde.getZ() + 0.5, tirou);
        caiu.setDeltaMovement(-0.3 + level.getRandom().nextDouble() * 0.6,
                0.1 + level.getRandom().nextDouble() * 0.2,
                -0.3 + level.getRandom().nextDouble() * 0.6);
        caiu.setExtendedLifetime();
        level.addFreshEntity(caiu);
    }

    /** <b>E o que estiver no chão</b>: chegando perto, chuta-o. */
    public void oQueEstáNoChão(ServerLevel level) {
        AABB roda = this.getBoundingBox().inflate(PROCURA);
        for (ItemEntity largado : level.getEntitiesOfClass(ItemEntity.class, roda)) {
            if (this.distanceToSqr(largado) > PROCURA * PROCURA) continue;
            if (this.distanceToSqr(largado) <= MEXE * MEXE) {
                largado.setDeltaMovement(-0.3 + this.random.nextDouble() * 0.6,
                        0.1 + this.random.nextDouble() * 0.2,
                        -0.3 + this.random.nextDouble() * 0.6);
                largado.hurtMarked = true;
                this.mexeu(level);
            } else {
                this.getNavigation().moveTo(largado.getX(), largado.getY(), largado.getZ(), 1.0);
            }
            return;
        }
    }

    /** O braço levantado e o aviso ao lado de cá, para o desenho acompanhar. */
    private void mexeu(ServerLevel level) {
        this.braço = BRAÇO;
        level.broadcastEntityEvent(this, (byte) 4);
    }

    @Override
    protected @org.jetbrains.annotations.Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource fonte) {
        return OccultaSounds.SPECTRE_DIE.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return OccultaSounds.SPECTRE_DIE.value();
    }
}
