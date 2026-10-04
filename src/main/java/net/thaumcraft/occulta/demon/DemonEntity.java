package net.thaumcraft.occulta.demon;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * O <b>Demônio</b>: o {@code EntityDemon} do Witchery.
 *
 * <p>Ele não é um monstro. É um <b>mercador</b> — e é aí que mora tudo.
 *
 * <p>Por dentro ele é um <b>golem</b>: o original estende o golem de ferro, e isso explica tudo o que ele faz
 * de estranho. Anda com o passo duro do golem, atira as pernas na curva de triângulo em vez do seno de gente,
 * bate com o soco que manda pelos ares, e <b>oscila seis graus e meio</b> de lado a cada passo.
 *
 * <h2>O que ele vende</h2>
 *
 * <p>Clicar nele abre uma lista de trocas como a de um aldeão: livros encantados, Pó Espectral, Língua de Cão,
 * Sopa de Pedra Vermelha, diamantes por lágrimas de ghast e por pérolas do fim — e, num dos três primeiros
 * lugares, um <b>Coração de Demônio</b>, que é a única coisa no mundo que se compra e não se arranca de
 * ninguém. A conta inteira está no {@link DemonTrades}.
 *
 * <h2>E pagar com o fogo dele é uma armadilha</h2>
 *
 * <p>Cada troca cobra numa moeda sorteada só para ela, e uma em cinco cobra em <b>vara de blaze</b>, outra em
 * cinco em <b>creme de magma</b>. Pagar com a matéria do inferno e ele aceita, entrega, e <b>estoura</b> dois
 * segundos e meio depois, com força três e fogo. Nenhuma linha do mod avisa.
 *
 * <p>No original o campo que conta essas batidas se chama {@code tryEscape}: o estouro é a <b>fuga</b> dele.
 * Ele abre um buraco no chão e vai embora — e quem fica no buraco é quem pagou.
 *
 * <h2>O que ele faz quando não está negociando</h2>
 *
 * <p>Cem de vida, <b>imune ao fogo</b>, não se afoga, brilha sempre como se estivesse ao sol, com <b>Salto V
 * para sempre</b> e um teto de <b>quinze de dano por pancada</b> — nenhum golpe lhe tira mais do que isso,
 * por maior que seja. De perto bate por <b>sete mais até quinze</b> e atira para cima quem apanha; de longe,
 * <b>bolas de fogo grandes</b>, as do ghast.
 *
 * <p>E ele <b>some sozinho</b> se ninguém estiver perto, como qualquer bicho: só o demônio que foi
 * <b>chamado por alguém</b> fica. O rito de chamar o marca; o Inferno na Terra não.
 *
 * <p><b>Declarado:</b> a <b>Língua do Diabo</b> — que no original desvia dezenove de cada vinte bolas de
 * fogo, desconta nos preços e se gasta cinco a cada troca — é um item do ramo das infusões que este porte
 * ainda não tem; e no Mundo dos Sonhos ele deixa de negociar e some quando o pesadelo deixa de ser demoníaco,
 * que é uma regra daquele lugar e não dele. As duas ficam para quando as peças delas vierem.
 */
public class DemonEntity extends AbstractGolem
        implements RangedAttackMob, net.minecraft.world.item.trading.Merchant {
    /** O que ele aguenta, e o teto de dano por pancada. */
    public static final float VIDA = 100.0f;
    public static final float TETO_DE_DANO = 15.0f;

    /** A pancada de perto: sete mais até quinze, e o empurrão para cima. */
    public static final float SOCO = 7.0f;
    public static final int SOCO_A_MAIS = 15;
    public static final double ATIRA_PARA_CIMA = 0.4;

    /** Quanto o braço fica no ar depois do golpe. */
    public static final int BRAÇO_NO_AR = 10;

    /** A bola de fogo: de quantas em quantas batidas, e a que distância. */
    public static final int FOGO_DE = 20;
    public static final int FOGO_ATÉ = 60;
    public static final float FOGO_A = 15.0f;

    /** E o estouro de quem foi pago com o fogo dele. */
    public static final int ESPERA_DO_ESTOURO = 50;
    public static final float ESTOURO = 3.0f;

    /** Quantas trocas ele traz: de seis a nove. */
    public static final int TROCAS_DE = 6;
    public static final int TROCAS_A_MAIS = 4;

    /**
     * O alcance com que ele procura alvo.
     *
     * <p>O original não o escreve: trinta e dois era o que o jogo de 2014 dava a todo mundo por omissão. Hoje
     * a omissão são dezesseis, e por isso o número tem de estar aqui para ele continuar o mesmo bicho.
     */
    public static final double VÊ_A = 32.0;

    private int vaiEstourar = -1;
    private int braçoNoAr;
    private boolean chamado;

    @org.jetbrains.annotations.Nullable
    private Player quemNegocia;

    @org.jetbrains.annotations.Nullable
    private net.minecraft.world.item.trading.MerchantOffers trocas;

    public DemonEntity(EntityType<? extends DemonEntity> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, VÊ_A);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, FOGO_DE, FOGO_ATÉ, FOGO_A));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    /** Ele não se afoga, e nem gasta o ar que tem. */
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected int decreaseAirSupply(int quanto) {
        return quanto;
    }

    /**
     * E ele <b>brilha sempre</b>, esteja onde estiver.
     *
     * <p>O {@code getBrightness} do original devolve um, de modo que ele é desenhado com a luz toda mesmo no
     * fundo de uma caverna. É o que faz um demônio parado no escuro parecer que traz a luz com ele.
     */
    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0f;
    }

    /**
     * <b>Só o demônio chamado por alguém fica.</b>
     *
     * <p>O do rito de chamar é marcado e persiste; o que o Inferno na Terra cospe não é, e por isso o rito
     * mais caro do mod dá demônios que vão embora se ninguém estiver olhando.
     */
    public boolean foiChamado() {
        return this.chamado;
    }

    public void marcaComoChamado() {
        this.chamado = true;
        this.setPersistenceRequired();
    }

    /** O Salto V que ele tem desde que nasce, e que o faz saltar por cima de muros. */
    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance dificuldade,
            net.minecraft.world.entity.EntitySpawnReason porquê,
            @org.jetbrains.annotations.Nullable net.minecraft.world.entity.SpawnGroupData dados) {
        this.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, -1, 4, false, false));
        return super.finalizeSpawn(level, dificuldade, porquê, dados);
    }

    /**
     * <b>Nada lhe tira mais do que quinze de uma vez.</b>
     *
     * <p>É o que o torna uma coisa que se negocia em vez de se matar: uma espada de diamante encantada leva o
     * mesmo tempo que uma de madeira, e o que muda é só a paciência de quem bate.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float quanto) {
        return super.hurtServer(level, fonte, Math.min(quanto, TETO_DE_DANO));
    }

    /**
     * O soco do golem: sete mais até quinze, e quem apanha <b>vai para cima</b>.
     *
     * <p>E o braço fica no ar dez batidas, que é o que o {@code attackTimer} do original conta para o modelo
     * desenhar o golpe.
     */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity quem) {
        this.braçoNoAr = BRAÇO_NO_AR;
        level.broadcastEntityEvent(this, (byte) 4);
        float dano = SOCO + this.random.nextInt(SOCO_A_MAIS);
        boolean deu = quem.hurtServer(level, this.damageSources().mobAttack(this), dano);
        if (deu) quem.setDeltaMovement(quem.getDeltaMovement().add(0.0, ATIRA_PARA_CIMA, 0.0));
        this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0f, 1.0f);
        return deu;
    }

    @Override
    public void handleEntityEvent(byte qual) {
        if (qual == 4) {
            this.braçoNoAr = BRAÇO_NO_AR;
            this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0f, 1.0f);
        } else {
            super.handleEntityEvent(qual);
        }
    }

    /** Quanto falta do golpe, para o modelo levantar o braço. */
    public int braçoNoAr() {
        return this.braçoNoAr;
    }

    /**
     * A <b>bola de fogo grande</b>, que é a mesma do ghast.
     *
     * <p>No original, quem segura a <b>Língua do Diabo</b> só a leva uma vez em vinte. A língua é do ramo das
     * infusões e não está portada; quando vier, é aqui que ela entra.
     */
    @Override
    public void performRangedAttack(LivingEntity alvo, float força) {
        if (!(this.level() instanceof ServerLevel level)) return;

        double dx = alvo.getX() - this.getX();
        double dy = alvo.getBoundingBox().minY + alvo.getBbHeight() / 2.0 - (this.getY() + this.getBbHeight() / 2.0);
        double dz = alvo.getZ() - this.getZ();
        float erro = net.minecraft.util.Mth.sqrt(força) * 0.5f;
        var rumo = new net.minecraft.world.phys.Vec3(
                dx + this.random.nextGaussian() * erro, dy, dz + this.random.nextGaussian() * erro);

        var bola = new net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball(
                level, this, rumo.normalize(), 1);
        var olhar = this.getViewVector(1.0f);
        bola.setPos(this.getX() + olhar.x, this.getY() + this.getBbHeight() / 2.0 + 0.5, this.getZ() + olhar.z);
        level.levelEvent(null, net.minecraft.world.level.block.LevelEvent.SOUND_GHAST_FIREBALL,
                this.blockPosition(), 0);
        level.addFreshEntity(bola);
    }

    // ------------------------------------------------------------------ o negócio

    /**
     * <b>O negócio</b>: o {@code IMerchant} do original, com a lista que o {@link DemonTrades} monta.
     *
     * <p>No jogo de hoje quem negocia é um {@code Villager}, e o que o Demônio precisa é só da tela. Ele a
     * abre à mão, como o original fazia.
     *
     * <p>E quem traz na mão um <b>ovo de bicho</b> ou uma <b>plaquinha de nome</b> não abre lista nenhuma: o
     * original abre caminho para esses dois usos antes de tudo, de modo que se pode batizar um demônio sem
     * cair na tela de trocas.
     */
    @Override
    protected InteractionResult mobInteract(Player quem, InteractionHand mão) {
        ItemStack naMão = quem.getItemInHand(mão);
        if (naMão.is(Items.NAME_TAG) || naMão.getItem() instanceof net.minecraft.world.item.SpawnEggItem) {
            return super.mobInteract(quem, mão);
        }
        if (!(this.level() instanceof ServerLevel)) return InteractionResult.SUCCESS;
        if (!this.isAlive() || this.isBaby()) return InteractionResult.PASS;
        if (this.quemNegocia != null) return InteractionResult.PASS;

        this.setTradingPlayer(quem);
        this.openTradingScreen(quem, this.getDisplayName(), 1);
        return InteractionResult.SUCCESS;
    }

    // ------------------------------------------------------------------ o que o mercador do jogo pede

    @Override
    public void setTradingPlayer(@org.jetbrains.annotations.Nullable Player quem) {
        this.quemNegocia = quem;
    }

    @Override
    public @org.jetbrains.annotations.Nullable Player getTradingPlayer() {
        return this.quemNegocia;
    }

    @Override
    public net.minecraft.world.item.trading.MerchantOffers getOffers() {
        if (this.trocas == null) {
            this.trocas = this.level() instanceof ServerLevel mundo
                    ? DemonTrades.monta(mundo)
                    : new net.minecraft.world.item.trading.MerchantOffers();
        }
        return this.trocas;
    }

    @Override
    public void overrideOffers(net.minecraft.world.item.trading.MerchantOffers quais) {
        this.trocas = quais;
    }

    /**
     * <b>Pago com o fogo dele, ele estoura.</b>
     *
     * <p>E a cada troca ele gastaria cinco da <b>Língua do Diabo</b> de quem negocia — que é a única coisa que
     * o impede de atirar bolas de fogo. Negociar com um demônio custa a defesa contra ele; a língua ainda não
     * está portada.
     */
    @Override
    public void notifyTrade(net.minecraft.world.item.trading.MerchantOffer qual) {
        qual.increaseUses();
        if (DemonTrades.éFogo(qual.getCostA().getItem())) {
            this.pagaramComFogo();
            return;
        }
        this.playSound(SoundEvents.PLAYER_BREATH, this.getSoundVolume(), this.getVoicePitch());
    }

    @Override
    public void notifyTradeUpdated(ItemStack oquê) {
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int quanto) {
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.WITHER_SHOOT;
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide();
    }

    @Override
    public boolean stillValid(Player quem) {
        return this.quemNegocia == quem && this.isAlive() && quem.closerThan(this, 8.0);
    }

    /**
     * <b>Pago com o fogo dele, ele estoura.</b>
     *
     * <p>Cinquenta batidas depois da troca — dois segundos e meio, que é o tempo de quem fez o negócio se
     * virar e começar a andar.
     */
    public void pagaramComFogo() {
        this.vaiEstourar = ESPERA_DO_ESTOURO;
        this.playSound(SoundEvents.WITHER_SHOOT, 1.0f, 1.0f);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.braçoNoAr > 0) this.braçoNoAr--;
        if (!(this.level() instanceof ServerLevel level)) return;

        if (this.vaiEstourar == 0) {
            this.vaiEstourar = -1;
            level.explode(this, this.getX(), this.getY(), this.getZ(), ESTOURO,
                    true, Level.ExplosionInteraction.MOB);
        } else if (this.vaiEstourar > 0) {
            this.vaiEstourar--;
        }
    }

    // ------------------------------------------------------------------ e o resto

    @Override
    public Component getName() {
        return this.hasCustomName() ? super.getName()
                : Component.translatable("entity.thaumcraft.demon");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource fonte) {
        return SoundEvents.WITHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos onde, net.minecraft.world.level.block.state.BlockState oquê) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0f, 1.0f);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput dados) {
        super.addAdditionalSaveData(dados);
        dados.putInt("VaiEstourar", this.vaiEstourar);
        dados.putBoolean("PlayerCreated", this.chamado);
        if (this.trocas != null) {
            dados.store("Trocas", net.minecraft.world.item.trading.MerchantOffers.CODEC, this.trocas);
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput dados) {
        super.readAdditionalSaveData(dados);
        this.vaiEstourar = dados.getIntOr("VaiEstourar", -1);
        this.chamado = dados.getBooleanOr("PlayerCreated", false);
        this.trocas = dados.read("Trocas", net.minecraft.world.item.trading.MerchantOffers.CODEC)
                .orElse(null);
    }
}
