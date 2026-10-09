package net.thaumcraft.occulta.louse;

import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.occulta.OccultaItems;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Piolho Parasita</b>: a {@code EntityParasyticLouse} do Witchery.
 *
 * <p>Quatro de vida, meio bloco de altura e <b>zero de murro</b>. Ele não machuca ninguém — o que ele faz
 * é <b>morder</b>, e a mordida leva a <b>poção que ele traz dentro</b>, uma vez só: mordido alguém, o
 * piolho fica vazio e volta a ser um bicho que anda à toa.
 *
 * <p>É a coisa mais parecida com uma seringa que o mod tem. Enche-se na bancada com uma poção qualquer,
 * larga-se no chão ou na água ao pé de quem se quer, e espera-se.
 *
 * <p>E ele <b>volta a ser item</b>: quem lhe clicar em cima o apanha de volta — vazio, se já tiver
 * mordido.
 */
public class LouseEntity extends Monster {
    /** A vida, o passo e o murro: os quatro, os seis décimos e o <b>zero</b>. */
    public static final double VIDA = 4.0;
    public static final double VELOCIDADE = 0.6;
    public static final double MURRO = 0.0;

    /** A que distância ele morde, e de quanto em quanto. */
    public static final double MORDE_A = 1.2;
    public static final int ENTRE_MORDIDAS = 20;

    /** E a quantos blocos de gente ele se recusa a nascer sozinho. */
    public static final double LONGE_DE_GENTE = 5.0;

    /** O que ele leva dentro, para o desenho e para a rede saberem. */
    private static final EntityDataAccessor<ItemStack> POÇÃO =
            SynchedEntityData.defineId(LouseEntity.class, EntityDataSerializers.ITEM_STACK);

    private int espera;

    public LouseEntity(EntityType<? extends LouseEntity> tipo, Level mundo) {
        super(tipo, mundo);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.MOVEMENT_SPEED, VELOCIDADE)
                .add(Attributes.ATTACK_DAMAGE, MURRO);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder construtor) {
        super.defineSynchedData(construtor);
        construtor.define(POÇÃO, ItemStack.EMPTY);
    }

    /** A poção que ele leva, ou nada. */
    public PotionContents poção() {
        ItemStack tem = this.entityData.get(POÇÃO);
        return tem.isEmpty() ? PotionContents.EMPTY
                : tem.getOrDefault(net.minecraft.core.component.DataComponents.POTION_CONTENTS,
                        PotionContents.EMPTY);
    }

    /** Põe-lhe uma poção dentro. */
    public void poção(PotionContents qual) {
        if (qual.equals(PotionContents.EMPTY)) {
            this.entityData.set(POÇÃO, ItemStack.EMPTY);
            return;
        }
        ItemStack guarda = new ItemStack(net.minecraft.world.item.Items.POTION);
        guarda.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS, qual);
        this.entityData.set(POÇÃO, guarda);
    }

    /** Se ele ainda tem o que dar. */
    public boolean cheio() {
        return !this.entityData.get(POÇÃO).isEmpty();
    }

    /**
     * A mordida: o {@code attackEntity} do original.
     *
     * <p>Ela não tira vida — o murro dele é zero —; o que ela faz é passar a <b>poção</b>, e só uma vez.
     */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity alvo) {
        boolean pegou = super.doHurtTarget(level, alvo);
        if (!(alvo instanceof LivingEntity vivo)) return pegou;
        if (!this.cheio()) return pegou;

        PotionContents tem = this.poção();
        for (MobEffectInstance cada : tem.getAllEffects()) {
            vivo.addEffect(new MobEffectInstance(cada));
        }
        this.poção(PotionContents.EMPTY);
        return pegou;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.espera > 0) this.espera--;
    }

    /**
     * <b>Clicando nele, ele volta a ser item</b>: o {@code interact} do original.
     *
     * <p>E volta com o que tiver dentro — vazio, se já tiver mordido alguém.
     */
    @Override
    public InteractionResult mobInteract(Player quem, InteractionHand mão) {
        if (!(this.level() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        ItemStack volta = new ItemStack(OccultaItems.LOUSE);
        PotionContents tem = this.poção();
        if (!tem.equals(PotionContents.EMPTY)) {
            volta.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS, tem);
        }
        level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level,
                this.getX(), this.getY() + 0.4, this.getZ(), volta));
        this.discard();
        return InteractionResult.SUCCESS;
    }

    /*
     * Ele é <b>artrópode</b>, como a lacrainha de que é parente — e hoje isso é um rótulo e não um
     * método: o {@code minecraft:arthropod} do mod, em {@code data/minecraft/tags/entity_type}.
     */

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource fonte) {
        return SoundEvents.SILVERFISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos onde,
                                 net.minecraft.world.level.block.state.BlockState oquê) {
        this.playSound(SoundEvents.SILVERFISH_STEP, 0.15f, 1.0f);
    }

    @Override
    public boolean removeWhenFarAway(double distância) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput saída) {
        super.addAdditionalSaveData(saída);
        PotionContents tem = this.poção();
        if (!tem.equals(PotionContents.EMPTY)) {
            saída.store("BitePotion", PotionContents.CODEC, tem);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput entrada) {
        super.readAdditionalSaveData(entrada);
        entrada.read("BitePotion", PotionContents.CODEC).ifPresent(this::poção);
    }

    /** Sem uso fora do porte: a prova precisa de saber a poção que ele leva. */
    public @Nullable Holder<net.minecraft.world.item.alchemy.Potion> qual() {
        return this.poção().potion().orElse(null);
    }
}
