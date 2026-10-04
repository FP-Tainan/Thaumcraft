package net.thaumcraft.occulta.wolf;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Lobisomem</b>: o {@code EntityWolfman} do Witchery.
 *
 * <p>Ele não nasce do mundo. Ele é um <b>aldeão</b> que virou — e que volta a ser aldeão quando a lua passar,
 * com a profissão e as trocas que tinha. É por isso que matar um lobisomem numa aldeia custa um aldeão, e é
 * por isso que o ofício inteiro o trata como uma doença e não como um monstro.
 *
 * <h2>Ele é quase invulnerável</h2>
 *
 * <p>E esta é a coisa que define o bicho: <b>tudo o que não for prata lhe tira um ponto de vida</b>, por mais
 * encantada que seja a espada. Com oitenta de vida, isso são oitenta pancadas. A <b>prata</b> tira dano a
 * sério — vez e meia, até quinze de cada vez —, e é só com ela que a conta muda.
 *
 * <p>Uma espada de prata sai de uma espada de ouro com oito <b>pós de prata</b> à volta. E o pó de prata cai
 * de lobisomem. O original não tem pudor com isso: o primeiro lobisomem mata-se a pancada, e os outros com o
 * que ele deixou.
 *
 * <p>Ele ainda <b>arromba portas</b>, caça gente e aldeãos, ganha <b>dez de armadura</b> por cima da que já
 * tem, e <b>não apanha veneno</b> — tira-o de si de dois em dois segundos.
 */
public class WolfmanEntity extends Monster {
    /** De quanto em quanto ele olha a lua: as cem batidas do original. */
    public static final int OLHA_A_LUA = 100;

    /** E de quanto em quanto ele se limpa do veneno. */
    public static final int LIMPA_O_VENENO = 40;

    /** O dano que leva do que não é prata. */
    public static final float SEM_PRATA = 1.0f;

    /** O que a prata multiplica, e o teto dela. */
    public static final float PRATA_VEZES = 1.5f;
    public static final float PRATA_TETO = 15.0f;

    /** A armadura que ele soma por cima da que tem, e o teto do jogo. */
    public static final int COURO_GROSSO = 10;
    public static final int ARMADURA_MÁXIMA = 20;

    @Nullable
    private VillagerData profissão;

    public WolfmanEntity(EntityType<? extends WolfmanEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreakDoorGoal(this, dificuldade -> true));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Villager.class, false));
    }

    /** Guarda quem ele era, para poder voltar a ser. */
    public void foiAldeão(VillagerData qual) {
        this.profissão = qual;
    }

    /**
     * Se a <b>mordida dele pega</b>: o {@code infectious} do original.
     *
     * <p>É o que separa um lobisomem qualquer do lobisomem que importa. Quase nenhum deles é contagioso — a
     * mordida de um lobisomem do mato <b>não dá licantropia a ninguém</b>, e o original é claro nisso.
     *
     * <p>Só há <b>uma</b> maneira de aparecer um contagioso, e ela é a <b>Armadilha de Prata</b>: posta ao pé
     * de um Altar do Lobo com uma ovelha na corda, ela chama um lobisomem na lua cheia, espera que ele pise
     * nela, e só então o torna contagioso.
     *
     * <p>Que é dizer: quem quer ser lobisomem <b>tem de o preparar</b>. Ninguém apanha a doença por azar.
     */
    public boolean contagioso() {
        return this.contagioso;
    }

    public void contagioso(boolean pega) {
        this.contagioso = pega;
    }

    private boolean contagioso;

    @Nullable
    public VillagerData profissão() {
        return this.profissão;
    }

    /** O couro grosso: dez por cima da armadura que ele tem, até o teto do jogo. */
    @Override
    public int getArmorValue() {
        return Math.min(super.getArmorValue() + COURO_GROSSO, ARMADURA_MÁXIMA);
    }

    /**
     * <b>Só a prata o fere.</b>
     *
     * <p>Tudo o resto tira um ponto, seja o que for. A prata tira vez e meia, até quinze de cada vez.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float quanto) {
        if (Silver.éDePrata(fonte)) {
            return super.hurtServer(level, fonte, Math.min(quanto * PRATA_VEZES, PRATA_TETO));
        }
        return super.hurtServer(level, fonte, Math.min(quanto, SEM_PRATA));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel mundo)) return;

        // passada a lua cheia, ele volta a ser quem era — menos se tiver acônito no corpo
        if (this.profissão != null && this.tickCount % OLHA_A_LUA == 3
                && !Moon.cheia(mundo) && !this.hasEffect(OccultaEffects.WOLFSBANE)) {
            this.voltaAoQueEra(mundo);
            return;
        }
        if (this.tickCount % LIMPA_O_VENENO == 4) {
            this.removeEffect(net.minecraft.world.effect.MobEffects.POISON);
        }
    }

    /** Volta a ser aldeão, com a profissão que tinha. */
    private void voltaAoQueEra(ServerLevel level) {
        var aldeão = OccultaEntities.WERE_VILLAGER.create(level, EntitySpawnReason.CONVERSION);
        if (aldeão == null) return;
        aldeão.copyPosition(this);
        aldeão.finalizeSpawn(level, level.getCurrentDifficultyAt(aldeão.blockPosition()),
                EntitySpawnReason.CONVERSION, null);
        if (this.profissão != null) aldeão.setVillagerData(this.profissão);
        aldeão.contagioso(this.contagioso);
        aldeão.setPersistenceRequired();
        this.discard();
        level.addFreshEntity(aldeão);
        level.levelEvent(null, 1027, this.blockPosition(), 0);
    }

    /**
     * O que ele deixa: pó de prata, osso ou couro — um dos três, a esmo.
     *
     * <p>É a única fonte de prata do ofício, e é de propósito que ela venha do próprio bicho.
     */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource fonte, boolean matouGente) {
        super.dropCustomDeathLoot(level, fonte, matouGente);
        switch (this.random.nextInt(3)) {
            case 0 -> this.spawnAtLocation(level,
                    new net.minecraft.world.item.ItemStack(OccultaItems.SILVER_DUST,
                            this.random.nextInt(3) + 1));
            case 1 -> this.spawnAtLocation(level, net.minecraft.world.item.Items.BONE);
            default -> this.spawnAtLocation(level, net.minecraft.world.item.Items.LEATHER);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distância) {
        return false;
    }

    /**
     * A fala dele é uma de duas, e <b>uma vez em vinte ele uiva</b> em vez de falar: é o original, e é o
     * que faz uma noite de lua cheia soar como soa.
     */
    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return (this.random.nextInt(UIVA_UMA_EM) == 0 ? net.thaumcraft.occulta.OccultaSounds.WOLFMAN_HOWL
                : net.thaumcraft.occulta.OccultaSounds.WOLFMAN_SAY).value();
    }

    /** De quantas em quantas falas ele uiva. */
    public static final int UIVA_UMA_EM = 20;

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource fonte) {
        return net.thaumcraft.occulta.OccultaSounds.WOLFMAN_HIT.value();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return net.thaumcraft.occulta.OccultaSounds.WOLFMAN_DEATH.value();
    }

    @Override
    public void addAdditionalSaveData(ValueOutput dados) {
        super.addAdditionalSaveData(dados);
        if (this.profissão != null) dados.store("FormerVillager", VillagerData.CODEC, this.profissão);
        dados.putBoolean("Infectious", this.contagioso);
    }

    @Override
    public void readAdditionalSaveData(ValueInput dados) {
        super.readAdditionalSaveData(dados);
        this.profissão = dados.read("FormerVillager", VillagerData.CODEC).orElse(null);
        this.contagioso = dados.getBooleanOr("Infectious", false);
    }

    /**
     * Faz de um aldeão um lobisomem, guardando quem ele era — e <b>se a mordida dele pega</b>.
     *
     * <p>O contágio anda nos dois sentidos: um aldeão contagioso vira um lobisomem contagioso, e esse, ao
     * voltar a ser aldeão com a lua, leva o contágio consigo. É o {@code convertToVillager} do original
     * passando a chave de mão em mão.
     */
    public static void doAldeão(ServerLevel level, Villager aldeão, boolean contagioso) {
        var lobo = OccultaEntities.WOLFMAN.create(level, EntitySpawnReason.CONVERSION);
        if (lobo == null) return;
        lobo.contagioso(contagioso);
        lobo.copyPosition(aldeão);
        lobo.finalizeSpawn(level, level.getCurrentDifficultyAt(lobo.blockPosition()),
                EntitySpawnReason.CONVERSION, null);
        lobo.foiAldeão(aldeão.getVillagerData());
        aldeão.discard();
        level.addFreshEntity(lobo);
        level.levelEvent(null, 1027, lobo.blockPosition(), 0);
        level.playSound(null, lobo.blockPosition(), net.thaumcraft.occulta.OccultaSounds.WOLFMAN_HOWL.value(),
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.0f);
    }

    /**
     * E o alvo dele nunca é quem está em <b>forma de bicho</b>.
     *
     * <p>Agora que a licantropia do jogador existe, isto vale para gente também: um lobisomem de lobo passa
     * por um bando de lobisomens sem que nenhum lhe toque. De gente, não.
     */
    @Override
    public boolean canAttack(LivingEntity quem) {
        if (quem instanceof WolfmanEntity) return false;
        if (Werewolf.emBicho(quem)) return false;
        return super.canAttack(quem);
    }

    /**
     * <b>A mordida que pega</b>: o {@code processWolfInfection} do {@code Shapeshift}.
     *
     * <p>Um aldeão que ele derrube abaixo de um quarto de vida <b>vira lobisomem</b>. E uma pessoa, nas
     * mesmas contas, <b>apanha a licantropia</b> — o primeiro grau, e com ele uma vida inteira de luas.
     *
     * <p>É uma vez em quatro, e só abaixo de um quarto de vida: não é a primeira mordida que pega, é a que
     * quase mata. E o <b>conjunto prateado do caçador</b> protege de todo.
     */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity alvo) {
        boolean acertou = super.doHurtTarget(level, alvo);
        if (!acertou || !this.contagioso || !(alvo instanceof LivingEntity vivo)) return acertou;

        /*
         * O aldeão vira <b>abaixo de um quarto de vida</b>, e vira num aldeão que <b>não é contagioso</b>:
         * uma aldeia mordida por um lobisomem de armadilha enche-se de lobisomens, mas eles não espalham
         * mais nada. O contágio para na primeira geração, e é de propósito.
         */
        if (alvo instanceof Villager aldeão) {
            if (vivo.getHealth() > 0.0f && vivo.getHealth() < vivo.getMaxHealth() * QUASE_MORTO) {
                doAldeão(level, aldeão, false);
            }
            return acertou;
        }

        /*
         * E a pessoa apanha a doença <b>sem conta de vida nenhuma e sem sorteio</b>. Uma mordida basta.
         * Quem foi preparar um lobisomem contagioso sabia o que ia acontecer.
         */
        if (!(alvo instanceof net.minecraft.world.entity.player.Player gente)) return acertou;
        if (net.thaumcraft.occulta.hunter.HunterClothes.protegeDeLobo(gente)) return acertou;
        if (Werewolf.grauDe(gente) > 0) return acertou;

        Werewolf.grau(gente, 1);
        gente.sendSystemMessage(
                net.minecraft.network.chat.Component.translatable("message.thaumcraft.werewolf_infection")
                        .withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
        return acertou;
    }

    /** Abaixo de que parte da vida um aldeão vira. */
    public static final float QUASE_MORTO = 0.25f;

    /**
     * E de quantas em quantas pega a <b>mordida de uma pessoa</b> em forma de bicho.
     *
     * <p>Fica aqui porque as duas contas vinham juntas no original, e porque é a mesma vida de um quarto. A
     * mordida <b>deste</b> bicho não tem sorteio nenhum — veja {@link #doHurtTarget}.
     */
    public static final int PEGA_UMA_EM = 4;

    /** O tipo do aldeão que ele vira e de que veio, para quem precisar. */
    public static EntityType<?> aldeãoDele() {
        return EntityTypes.VILLAGER;
    }
}
