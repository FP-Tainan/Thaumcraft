package net.thaumcraft.occulta.treefyd;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.occulta.EntEntity;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.TaglockItem;
import net.thaumcraft.occulta.coven.CovenWitchEntity;
import net.thaumcraft.occulta.familiar.Familiars;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * O <b>Treefyd</b>: o {@code EntityTreefyd} do Witchery.
 *
 * <p>É o <b>espantalho vivo</b> do mod, e a ideia dele é melhor do que parece: ele <b>ataca tudo o que não
 * conhece</b>, e quem o planta <b>ensina-lhe quem é de casa</b>, um frasco de vínculo de cada vez.
 *
 * <p>Planta-se uma semente num chão onde cresça erva, e nasce um Treefyd com <b>quem o plantou</b> por dono.
 * Dali em diante é uma cerca que anda: cinquenta de vida, três de dano, e <b>cega</b> quem morde.
 *
 * <h2>Em quem ele não toca</h2>
 *
 * <p>Nem noutro Treefyd, nem no Caçador de Bruxas, nem num Ent, nem no que voa, nem num bicho do ar ou de
 * água, nem num <b>familiar</b> de alguém, nem numa bruxa de coven. Nem no <b>dono</b>, nem em quem lhe
 * apresentaram.
 *
 * <p><b>Declarado:</b> «o que voa» no original é uma <b>classe</b> do jogo de 2014 que só o ghast tinha, mais
 * a classe de que a coruja do mod descendia. O jogo de hoje não tem essa classe, de modo que a pergunta se
 * faz aos dois por nome: o <b>ghast</b> e a <b>coruja</b>.
 *
 * <h2>Como se lhe apresenta alguém</h2>
 *
 * <p>O dono clica nele com um <b>frasco de vínculo cheio</b>: de pé, ensina; <b>agachado</b>, esquece. Cada
 * clique gasta o frasco, e ele responde no chat com a lista inteira do que conhece.
 *
 * <p>E há duas listas, não uma: os bichos de <b>criação</b> conhecem-se <b>por espécie</b> — apresentada uma
 * ovelha, ele poupa todas — e os outros, <b>um a um</b>. É o que faz dele um guarda de quinta e não um
 * guarda de cada vaca.
 *
 * <h2>O que o faz crescer</h2>
 *
 * <p>Um <b>Coração de Creeper</b> leva-o a cem de vida e quatro de dano; um <b>Coração de Demônio</b>, a
 * cento e cinquenta de vida e cinco de dano. E a <b>Boline</b> liga e desliga o modo <b>sentinela</b> — um Treefyd sentinela
 * <b>não anda</b>, e é assim que se faz dele um poste em vez de um cão.
 *
 * <p>E ele <b>faz filhos</b>: clicando nele com outra semente nasce outro ao lado, com <b>a mesma lista</b>
 * de quem conhece. É assim que se faz uma cerca viva de um só gesto.
 *
 * <h2>Desvio declarado</h2>
 *
 * <p>O original guarda as espécies conhecidas pelo <b>nome de tela</b> do bicho, e compara textos — o que
 * quebra em qualquer língua que não a dele, e quebra outra vez se o bicho for batizado. Aqui o que se guarda
 * é o <b>nome de registro</b> da espécie, que é o mesmo em toda parte; e as oito espécies «de criação» do
 * original são um <b>rótulo</b>, {@code thaumcraft:o_treefyd_conhece_por_especie}.
 */
public class TreefydEntity extends Monster {
    /** Cinquenta de vida, três de dano, e um passo de planta. */
    public static final double VIDA = 50.0;
    public static final double MURRO = 3.0;
    public static final double PASSO = 0.25;

    /** O que o Coração de Creeper o faz. */
    public static final double CREEPER_VIDA = 100.0;
    public static final double CREEPER_MURRO = 4.0;

    /** E o de Demônio. */
    public static final double DEMÔNIO_VIDA = 150.0;
    public static final double DEMÔNIO_MURRO = 5.0;

    /** A cegueira que a mordida dele dá: cinco segundos. */
    public static final int CEGA = 100;

    /** As oito espécies que ele conhece <b>em bloco</b>, num rótulo. */
    public static final net.minecraft.tags.TagKey<EntityType<?>> POR_ESPÉCIE =
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,
                    net.thaumcraft.Thaumcraft.id("o_treefyd_conhece_por_especie"));

    /** Se ele é sentinela: um Treefyd sentinela não anda. */
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> SENTINELA =
            net.minecraft.network.syncher.SynchedEntityData.defineId(TreefydEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);

    private @Nullable UUID dono;
    private final List<UUID> conhecidos = new ArrayList<>();
    private final List<String> espécies = new ArrayList<>();

    public TreefydEntity(EntityType<? extends TreefydEntity> tipo, Level level) {
        super(tipo, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.MOVEMENT_SPEED, PASSO)
                .add(Attributes.ATTACK_DAMAGE, MURRO);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new Wander(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class,
                0, false, true, (quem, level) -> this.estranho(quem)));
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder quais) {
        super.defineSynchedData(quais);
        quais.define(SENTINELA, false);
    }

    // ------------------------------------------------------------------ quem ele conhece

    public @Nullable UUID dono() {
        return this.dono;
    }

    public void dono(@Nullable UUID quem) {
        this.dono = quem;
    }

    public boolean sentinela() {
        return this.entityData.get(SENTINELA);
    }

    public void sentinela(boolean sim) {
        this.entityData.set(SENTINELA, sim);
    }

    /** Os bichos que ele conhece um a um. */
    public List<UUID> conhecidos() {
        return List.copyOf(this.conhecidos);
    }

    /** E as espécies que ele conhece em bloco. */
    public List<String> espécies() {
        return List.copyOf(this.espécies);
    }

    /**
     * <b>Se este é um estranho</b>: o {@code isEntityApplicable} do original, na ordem dele.
     *
     * <p>Primeiro as famílias que ele nunca toca, depois o dono, depois quem lhe apresentaram.
     */
    public boolean estranho(@Nullable LivingEntity quem) {
        if (quem == null || quem == this) return false;
        if (quem instanceof TreefydEntity || quem instanceof EntEntity
                || quem instanceof CovenWitchEntity
                || quem instanceof AmbientCreature || éDeÁgua(quem)
                || quem instanceof net.minecraft.world.entity.monster.Ghast
                || quem instanceof net.thaumcraft.occulta.familiar.OwlEntity
                || quem instanceof net.thaumcraft.occulta.hunter.WitchHunterEntity) {
            return false;
        }
        if (this.level() instanceof ServerLevel mundo && Familiars.éDeAlguém(mundo, quem)) return false;

        if (quem.getUUID().equals(this.dono)) return false;
        if (this.conhecidos.contains(quem.getUUID())) return false;
        return !this.espécies.contains(nomeDe(quem));
    }

    /**
     * Se este é um <b>bicho de água</b>.
     *
     * <p><b>Declarado:</b> na 1.7.10 lulas e peixes eram todos da mesma classe; hoje a lula vai por um lado
     * e o peixe por outro, de modo que a pergunta se faz às duas.
     */
    public static boolean éDeÁgua(LivingEntity quem) {
        return quem instanceof WaterAnimal
                || quem instanceof net.minecraft.world.entity.animal.AgeableWaterCreature;
    }

    /** O nome de registro da espécie, que é o que ele guarda. */
    public static String nomeDe(Entity quem) {
        return EntityType.getKey(quem.getType()).toString();
    }

    /** Se esta espécie se conhece <b>em bloco</b>: o {@code isGroupableCreature} do original. */
    public static boolean porEspécie(Entity quem) {
        return quem.getType().builtInRegistryHolder().is(POR_ESPÉCIE);
    }

    /**
     * Apresenta-lhe alguém, ou fá-lo esquecer.
     *
     * @return se alguma coisa mudou
     */
    public boolean apresenta(Entity quem, boolean esquecer) {
        if (porEspécie(quem)) {
            String qual = nomeDe(quem);
            if (esquecer) return this.espécies.remove(qual);
            if (this.espécies.contains(qual)) return false;
            this.espécies.add(qual);
            return true;
        }
        UUID qual = quem.getUUID();
        if (esquecer) return this.conhecidos.remove(qual);
        if (this.conhecidos.contains(qual)) return false;
        this.conhecidos.add(qual);
        return true;
    }

    /** Copia para outro o que este conhece: é o que o filho herda. */
    public void ensina(TreefydEntity filho) {
        filho.conhecidos.addAll(this.conhecidos);
        filho.espécies.addAll(this.espécies);
    }

    // ------------------------------------------------------------------ o que o dono lhe faz

    /**
     * <b>Só o dono mexe com ele</b>, e o que ele faz depende do que traz na mão.
     *
     * <p>Um frasco de vínculo apresenta ou esquece; um Coração de Creeper ou de Demônio fá-lo crescer; uma
     * semente faz um filho; a Boline liga o modo sentinela. E em todos os casos ele responde no chat com a
     * lista inteira do que conhece — que é o original dizendo «entendi».
     */
    @Override
    protected InteractionResult mobInteract(Player quem, InteractionHand mão) {
        if (!(this.level() instanceof ServerLevel mundo)) return InteractionResult.SUCCESS;
        if (!quem.getUUID().equals(this.dono)) return InteractionResult.PASS;

        ItemStack naMão = quem.getItemInHand(mão);
        if (naMão.is(OccultaItems.TAGLOCK) && TaglockItem.isBound(naMão)) {
            var preso = TaglockItem.bound(naMão);
            if (preso != null && !preso.owner().equals(this.getUUID())) {
                Entity achado = mundo.getEntity(preso.owner());
                boolean mudou = achado == null
                        ? this.esqueceOuGuarda(preso.owner(), quem.isShiftKeyDown())
                        : this.apresenta(achado, quem.isShiftKeyDown());
                if (mudou && !quem.hasInfiniteMaterials()) naMão.shrink(1);
            }
            this.diz(quem);
            return InteractionResult.SUCCESS;
        }

        if (naMão.is(OccultaItems.TREEFYD_SEEDS)) {
            this.filho(mundo, quem);
            if (!quem.hasInfiniteMaterials()) naMão.shrink(1);
            return InteractionResult.SUCCESS;
        }

        if (naMão.is(OccultaItems.CREEPER_HEART)) {
            this.cresce(mundo, CREEPER_VIDA, CREEPER_MURRO);
            if (!quem.hasInfiniteMaterials()) naMão.shrink(1);
            this.diz(quem);
            return InteractionResult.SUCCESS;
        }

        if (naMão.is(OccultaItems.DEMON_HEART)) {
            this.cresce(mundo, DEMÔNIO_VIDA, DEMÔNIO_MURRO);
            if (!quem.hasInfiniteMaterials()) naMão.shrink(1);
            this.diz(quem);
            return InteractionResult.SUCCESS;
        }

        if (naMão.is(OccultaItems.BOLINE)) {
            this.sentinela(!this.sentinela());
            this.diz(quem);
            return InteractionResult.SUCCESS;
        }

        this.diz(quem);
        return InteractionResult.PASS;
    }

    /** Quem o vínculo pega mas que não está carregado: guarda-se o número dele, e pronto. */
    private boolean esqueceOuGuarda(UUID qual, boolean esquecer) {
        if (esquecer) return this.conhecidos.remove(qual);
        if (this.conhecidos.contains(qual)) return false;
        this.conhecidos.add(qual);
        return true;
    }

    /** Cresce: vida nova, cheia, e mais murro. */
    private void cresce(ServerLevel mundo, double vida, double murro) {
        var atributo = this.getAttribute(Attributes.MAX_HEALTH);
        if (atributo != null) atributo.setBaseValue(vida);
        this.setHealth(this.getMaxHealth());
        var soco = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (soco != null) soco.setBaseValue(murro);
        mundo.sendParticles(net.minecraft.core.particles.ParticleTypes.ITEM_SLIME,
                this.getX(), this.getY() + 1.0, this.getZ(), 16, 0.5, 1.0, 0.5, 0.0);
        mundo.playSound(null, this.blockPosition(), SoundEvents.SILVERFISH_DEATH,
                SoundSource.HOSTILE, 1.0f, 1.0f);
    }

    /** Faz um filho ao lado, com a mesma lista de quem conhece. */
    private void filho(ServerLevel mundo, Player quem) {
        TreefydEntity novo = OccultaEntities.TREEFYD.create(mundo, EntitySpawnReason.TRIGGERED);
        if (novo == null) return;
        novo.snapTo(this.getX() + 0.5, this.getY(), this.getZ() + 0.5, 0.0f, 0.0f);
        novo.dono(quem.getUUID());
        novo.setPersistenceRequired();
        this.ensina(novo);
        mundo.addFreshEntity(novo);
        mundo.sendParticles(net.minecraft.core.particles.ParticleTypes.ITEM_SLIME,
                novo.getX(), novo.getY() + 1.0, novo.getZ(), 16, 0.5, 1.0, 0.5, 0.0);
        mundo.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                novo.getX(), novo.getY() + 1.0, novo.getZ(), 4, 0.5, 1.0, 0.5, 0.0);
        mundo.playSound(null, novo.blockPosition(), SoundEvents.SILVERFISH_DEATH,
                SoundSource.HOSTILE, 1.0f, 1.0f);
    }

    /** E diz no chat tudo o que conhece: o {@code showCurrentKnownEntities}. */
    private void diz(Player quem) {
        StringBuilder lista = new StringBuilder();
        for (String qual : this.espécies) {
            if (!lista.isEmpty()) lista.append(", ");
            lista.append('#').append(qual);
        }
        for (UUID qual : this.conhecidos) {
            if (!lista.isEmpty()) lista.append(", ");
            lista.append(qual);
        }
        quem.sendSystemMessage(Component.translatable("tc.treefyd.knows",
                this.getDisplayName(), lista.toString()));
    }

    // ------------------------------------------------------------------ a mordida

    /** Ela <b>cega</b>, por cinco segundos — e só quem ainda não estiver cego. */
    @Override
    public boolean doHurtTarget(ServerLevel mundo, Entity quem) {
        if (quem instanceof Player gente && !gente.hasEffect(MobEffects.BLINDNESS)) {
            gente.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, CEGA, 0));
        }
        return super.doHurtTarget(mundo, quem);
    }

    // ------------------------------------------------------------------ o que ele guarda

    @Override
    protected void addAdditionalSaveData(ValueOutput dados) {
        super.addAdditionalSaveData(dados);
        if (this.dono != null) dados.store("Owner", UUIDUtil.CODEC, this.dono);
        if (!this.conhecidos.isEmpty()) {
            dados.store("KnownCreatures", UUIDUtil.CODEC.listOf(), List.copyOf(this.conhecidos));
        }
        if (!this.espécies.isEmpty()) {
            dados.store("KnownCreatureTypes", com.mojang.serialization.Codec.STRING.listOf(),
                    List.copyOf(this.espécies));
        }
        dados.putBoolean("SentinalPlant", this.sentinela());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput dados) {
        super.readAdditionalSaveData(dados);
        this.dono = dados.read("Owner", UUIDUtil.CODEC).orElse(null);
        this.conhecidos.clear();
        dados.read("KnownCreatures", UUIDUtil.CODEC.listOf()).ifPresent(this.conhecidos::addAll);
        this.espécies.clear();
        dados.read("KnownCreatureTypes", com.mojang.serialization.Codec.STRING.listOf())
                .ifPresent(this.espécies::addAll);
        this.sentinela(dados.getBooleanOr("SentinalPlant", false));
    }

    @Override
    public int getAmbientSoundInterval() {
        return super.getAmbientSoundInterval() * 2;
    }

    /** Um Treefyd sentinela não anda: é o {@code EntityAITreefydWander} do original. */
    private static class Wander extends RandomStrollGoal {
        private final TreefydEntity treefyd;

        Wander(TreefydEntity treefyd, double passo) {
            super(treefyd, passo);
            this.treefyd = treefyd;
        }

        @Override
        public boolean canUse() {
            return !this.treefyd.sentinela() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.treefyd.sentinela() && super.canContinueToUse();
        }
    }
}
