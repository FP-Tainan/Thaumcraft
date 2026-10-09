package net.thaumcraft.occulta.hunter;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * O <b>virote</b>: o {@code EntityBolt} do Witchery.
 *
 * <p>Não é uma flecha pequena. O virote é a <b>munição de quem caça o que a espada não mata</b>, e por isso
 * ele tem tipo — e o tipo é tudo o que ele é:
 *
 * <ul>
 *   <li><b>A estaca</b> ({@link #ESTACA}): madeira. É o virote comum, e é de madeira que se mata vampiro.</li>
 *   <li><b>A drenagem</b> ({@link #DRENAGEM}): também madeira, mas <b>chupa magia</b> de quem acerta.</li>
 *   <li><b>A drenagem forte</b> ({@link #DRENAGEM_FORTE}): a mesma, <b>limpa</b> — tira todos os efeitos de
 *       quem acerta, menos os três que são castigo, e derruba o poder dele pela metade. Só sai assim de quem
 *       veste as roupas de caçador inteiras.</li>
 *   <li><b>O sagrado</b> ({@link #SAGRADO}): vale <b>uma vez e meia</b> contra morto-vivo e coisa do inferno.</li>
 *   <li><b>O de prata</b> ({@link #PRATA}): a única coisa de longe que fere um <b>lobisomem</b>.</li>
 * </ul>
 *
 * <p>O virote que <b>parte</b> não é um tipo — são três virotes comuns saindo de uma vez, e isso é da besta e
 * não dele.
 *
 * <p><b>Traduções declaradas:</b> o original escreve a flecha inteira de novo, com o próprio cálculo de voo,
 * de cravar e de bater — porque em 2014 a flecha do jogo não se herdava bem. Aqui ele é uma
 * {@link AbstractArrow}: o voo, o cravar, o apanhar e o encantamento são os do jogo, e o que fica deste é só o
 * que é dele — o tipo, e o que cada tipo faz. E o original guarda {@code isPoweredDraining} na fonte de dano;
 * aqui quem guarda é o próprio virote, que é quem a fonte aponta.
 */
public class BoltEntity extends AbstractArrow {
    /** Os cinco tipos, pelos números do original. */
    public static final int ESTACA = 0;
    public static final int DRENAGEM = 1;
    public static final int DRENAGEM_FORTE = 2;
    public static final int SAGRADO = 3;
    public static final int PRATA = 4;

    /** O que o sagrado vale a mais contra morto-vivo e coisa do inferno. */
    public static final double SAGRADO_VALE = 1.5;

    /** E o quanto a drenagem forte derruba o poder de quem apanha. */
    public static final float DERRUBA = 0.5f;

    private static final EntityDataAccessor<Integer> TIPO =
            SynchedEntityData.defineId(BoltEntity.class, EntityDataSerializers.INT);

    public BoltEntity(EntityType<? extends BoltEntity> type, Level level) {
        super(type, level);
    }

    public BoltEntity(Level level, LivingEntity quemAtirou, ItemStack munição, @Nullable ItemStack arma,
                      int tipo) {
        super(OccultaEntities.BOLT, quemAtirou, level, munição, arma);
        this.entityData.set(TIPO, tipo);
    }

    public BoltEntity(Level level, double x, double y, double z, ItemStack munição, int tipo) {
        super(OccultaEntities.BOLT, x, y, z, level, munição, null);
        this.entityData.set(TIPO, tipo);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TIPO, ESTACA);
    }

    /**
     * O dano que este virote leva.
     *
     * <p>O {@code AbstractArrow} do jogo guarda o dano-base e <b>não o devolve</b>; o virote precisa de o saber
     * para o pôr de volta depois de o sagrado o ter multiplicado, e por isso o anota de passagem.
     */
    private double dano;

    @Override
    public void setBaseDamage(double quanto) {
        super.setBaseDamage(quanto);
        this.dano = quanto;
    }

    public int tipo() {
        return this.entityData.get(TIPO);
    }

    public void tipo(int qual) {
        this.entityData.set(TIPO, qual);
    }

    /** Se este virote chupa magia: o {@code isDraining}. */
    public boolean drena() {
        return drena(this.tipo());
    }

    /** E se ele chupa com força, que é o que limpa: o {@code isPoweredDraining}. */
    public boolean drenaForte() {
        return drenaForte(this.tipo());
    }

    /** Se é dano sagrado: o {@code isHolyDamage}. */
    public boolean éSagrado() {
        return éSagrado(this.tipo());
    }

    /** Se é dano de madeira — o que mata vampiro: o {@code isWoodenDamage}. */
    public boolean éDeMadeira() {
        return éDeMadeira(this.tipo());
    }

    /** E se é dano de prata — o que fere lobisomem: o {@code isSilverDamage}. */
    public boolean éDePrata() {
        return éDePrata(this.tipo());
    }

    /**
     * As mesmas cinco perguntas, feitas ao <b>tipo</b> e não ao virote.
     *
     * <p>O que um virote é está todo no número dele, e por isso as perguntas não precisam do bicho: assim se
     * podem fazer sem haver mundo, que é o que as provas querem.
     */
    public static boolean drena(int tipo) {
        return tipo == DRENAGEM || tipo == DRENAGEM_FORTE;
    }

    public static boolean drenaForte(int tipo) {
        return tipo == DRENAGEM_FORTE;
    }

    public static boolean éSagrado(int tipo) {
        return tipo == SAGRADO;
    }

    public static boolean éDeMadeira(int tipo) {
        return tipo == ESTACA || tipo == DRENAGEM || tipo == DRENAGEM_FORTE;
    }

    public static boolean éDePrata(int tipo) {
        return tipo == PRATA;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(munição(this.tipo()));
    }

    /** Qual virote volta ao chão de cada tipo. */
    public static net.minecraft.world.item.Item munição(int tipo) {
        return switch (tipo) {
            case DRENAGEM, DRENAGEM_FORTE -> OccultaItems.ANTI_MAGIC_BOLT;
            case SAGRADO -> OccultaItems.HOLY_BOLT;
            case PRATA -> OccultaItems.SILVER_BOLT;
            default -> OccultaItems.STAKE_BOLT;
        };
    }

    /**
     * O que o sagrado vale a mais.
     *
     * <p>O original multiplica o dano <b>antes</b> de bater, e é o que se faz aqui: o dano-base sobe antes de
     * o golpe sair, e só contra morto-vivo e coisa do inferno.
     */
    @Override
    protected void onHitEntity(EntityHitResult acertou) {
        double eraTanto = this.dano;
        if (this.éSagrado() && impuro(acertou.getEntity())) {
            this.setBaseDamage(eraTanto * SAGRADO_VALE);
        }
        super.onHitEntity(acertou);
        this.setBaseDamage(eraTanto);
    }

    /**
     * Morto-vivo ou coisa do inferno: o {@code isUndead || isDemonic} do original.
     *
     * <p>O que é do inferno mora no rótulo {@code thaumcraft:demonic}, que tem os quatro do jogo e os do
     * mod — o Demônio, o Diabrete, a Lilith e o Senhor do Tormento.
     */
    private static boolean impuro(net.minecraft.world.entity.Entity quem) {
        if (!(quem instanceof LivingEntity vivo)) return false;
        if (vivo.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) return true;
        return net.thaumcraft.occulta.torment.Demonic.é(vivo);
    }

    /**
     * O que a drenagem faz em quem apanha.
     *
     * <p>A <b>forte</b> limpa: tira todos os efeitos, menos os três que são castigo — o veneno, o definhar e a
     * cegueira. Tirar esses seria <b>curar</b> quem se acertou, e é por isso que o original os deixa.
     */
    @Override
    protected void doPostHurtEffects(LivingEntity quem) {
        super.doPostHurtEffects(quem);
        this.limpa(quem);
    }

    /**
     * A limpeza em si, à parte do golpe.
     *
     * <p>Está fora do {@code doPostHurtEffects} porque esse é do jogo e não se pode chamar de fora — e o que
     * esta fatia muda é isto, e não o voo nem o golpe.
     */
    public void limpa(LivingEntity quem) {
        if (!this.drenaForte()) return;

        List<net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>> tira = new ArrayList<>();
        for (MobEffectInstance tem : quem.getActiveEffects()) {
            var qual = tem.getEffect();
            if (qual == MobEffects.POISON || qual == MobEffects.WITHER || qual == MobEffects.BLINDNESS) {
                continue;
            }
            tira.add(qual);
        }
        for (var qual : tira) quem.removeEffect(qual);

        Drain.derruba(quem, DERRUBA);
    }
}
