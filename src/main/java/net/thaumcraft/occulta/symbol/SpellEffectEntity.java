package net.thaumcraft.occulta.symbol;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;

/**
 * A <b>bola de feitiço</b>: a {@code EntitySpellEffect} do Witchery.
 *
 * <p>É a mesma bola para todos os símbolos que atiram alguma coisa — o que muda é a <b>cor</b> e o
 * <b>tamanho</b>, que ela vai buscar ao símbolo que a lançou. É por ela que se reconhece de longe qual
 * feitiço vem vindo, e é a única coisa deste ramo que se vê antes de acontecer.
 *
 * <p>Ela anda com <b>aceleração própria</b>, como uma bola de fogo, e <b>não atravessa nada</b>: bate no
 * primeiro bloco ou bicho que encontrar e some ali, deixando o que o símbolo mandar.
 *
 * <p>O <b>número do símbolo</b> atravessa a rede, porque quem a desenha é o lado de cá e ele precisa de
 * saber de que cor ela é.
 */
public class SpellEffectEntity extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Integer> SÍMBOLO =
            SynchedEntityData.defineId(SpellEffectEntity.class, EntityDataSerializers.INT);

    /** O prazo dela, se tiver. */
    private int vive = -1;

    /** E o grau com que foi lançada, que só o servidor precisa. */
    private int grau = 1;

    public SpellEffectEntity(EntityType<? extends SpellEffectEntity> tipo, Level mundo) {
        super(tipo, mundo);
    }

    public SpellEffectEntity(ServerLevel level, LivingEntity quem, Vec3 rumo, Symbol qual, int grau) {
        super(OccultaEntities.SPELL_EFFECT, quem, rumo.normalize(), level);
        this.grau = grau;
        this.entityData.set(SÍMBOLO, qual.id);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder construtor) {
        super.defineSynchedData(construtor);
        construtor.define(SÍMBOLO, 0);
    }

    public int símbolo() {
        return this.entityData.get(SÍMBOLO);
    }

    public int grau() {
        return this.grau;
    }

    public SpellEffectEntity vive(int batidas) {
        this.vive = batidas;
        return this;
    }

    /** A bola não pega fogo em nada: ela é só o que o símbolo manda. */
    @Override
    protected boolean shouldBurn() {
        return false;
    }

    /**
     * <b>E ela não deixa fumo.</b>
     *
     * <p>O rastro do jogo é de <b>fumaça</b>, e o da bola do original é de <b>gosma</b> — e meio bloco mais
     * acima. Devolvendo nada aqui, o jogo não põe rastro nenhum, e o certo é posto na batida.
     */
    @Override
    protected net.minecraft.core.particles.@org.jetbrains.annotations.Nullable ParticleOptions
            getTrailParticle() {
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            /*
             * A gosma do original, meio bloco acima da bola. Uma <b>maldição</b> leva, em vez dela, o pó de
             * poção e uma chama — e nenhum dos seis símbolos desta fatia é maldição, mas a pergunta fica
             * feita para quando forem.
             */
            Symbol qual = Symbols.daquele(this.símbolo());
            boolean maldita = qual != null && qual.maldição;
            this.level().addParticle(maldita
                            ? net.minecraft.core.particles.ColorParticleOption.create(
                                    net.minecraft.core.particles.ParticleTypes.ENTITY_EFFECT, 0xFFFFFFFF)
                            : net.minecraft.core.particles.ParticleTypes.ITEM_SLIME,
                    this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
            if (maldita) {
                this.level().addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                        this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
            }
            return;
        }
        if (this.vive <= 0) return;
        if (--this.vive <= 0) this.discard();
    }

    @Override
    protected void onHit(HitResult onde) {
        super.onHit(onde);
        if (!(this.level() instanceof ServerLevel level)) return;
        Symbol qual = Symbols.daquele(this.símbolo());
        if (qual instanceof ProjectileSymbol atirado) {
            atirado.aoBater(level,
                    this.getOwner() instanceof LivingEntity dono ? dono : null, onde, this.grau);
        }
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput dados) {
        super.addAdditionalSaveData(dados);
        dados.putInt("Simbolo", this.símbolo());
        dados.putInt("Grau", this.grau);
        dados.putInt("Vive", this.vive);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput dados) {
        super.readAdditionalSaveData(dados);
        this.entityData.set(SÍMBOLO, dados.getIntOr("Simbolo", 0));
        this.grau = dados.getIntOr("Grau", 1);
        this.vive = dados.getIntOr("Vive", -1);
    }
}
