package net.thaumcraft.occulta.broom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.familiar.Familiars;

/**
 * A vassoura que se monta: o {@code EntityBroom} do Witchery.
 *
 * <p>Ela não é um bicho e não é um barco — é uma coisa do mod que voa, e voa por uma conta própria que vale ser
 * lida, porque é dela que vem a sensação de montar uma vassoura e não de pilotar um avião:
 *
 * <ul>
 *   <li><b>Ela acelera devagar e para devagar.</b> O {@code speedMultiplier} começa em <b>0,07</b> e sobe
 *       para <b>0,35</b> um centésimo da diferença de cada vez, enquanto se vai ganhando velocidade; soltando,
 *       ele desce de volta pelo mesmo caminho. Não há botão de turbo: há inércia.</li>
 *   <li><b>O teto é de velocidade, não de aceleração.</b> Sem nada, <b>0,9</b>.</li>
 *   <li><b>Subir e descer saem do olhar</b>, e com uma zona morta: a inclinação só conta fora da faixa de
 *       −0,5 a 0,2, e descer conta pela metade. É o que impede a vassoura de mergulhar a cada olhadela.</li>
 * </ul>
 *
 * <p><b>E é aqui que a coruja serve.</b> Quem tem a maestria da vassoura — a do familiar coruja — ganha
 * <b>0,2 de aceleração</b> e <b>0,3 de teto</b>, e passa a <b>frear sozinho</b> ao largar o acelerador, em vez
 * de deslizar. É o {@code riderHasOwlFamiliar} do original, e é a resposta que faltava desde a fatia dos
 * familiares, onde a maestria da coruja ficou escrita sem ter o que destrancar.
 *
 * <p>O cozimento do Voo Alto do original dá metade disso ({@code riderHasSoaringBrew}: 0,1 e 0,3) e
 * <b>não está portado</b> — está declarado no {@code PORTE.md}. A conta dele fica escrita aqui, pronta, para o
 * dia em que o cozimento vier.
 */
public class BroomEntity extends Entity {
    /** Onde ela começa, e de onde volta sempre que se solta o acelerador. */
    public static final double EMPURRÃO_PARADO = 0.07;

    /** E até onde ele sobe. */
    public static final double EMPURRÃO_MÁXIMO = 0.35;

    /** O teto de velocidade de quem não tem nada. */
    public static final double TETO = 0.9;

    /** O que a coruja soma: ao empurrão e ao teto. */
    public static final double CORUJA_EMPURRÃO = 0.2;
    public static final double CORUJA_TETO = 0.3;

    /** Quanto dano aguenta antes de se desfazer. */
    public static final float AGUENTA = 40.0f;

    private static final EntityDataAccessor<Byte> COR =
            SynchedEntityData.defineId(BroomEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DESDE_A_PANCADA =
            SynchedEntityData.defineId(BroomEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> APANHADO =
            SynchedEntityData.defineId(BroomEntity.class, EntityDataSerializers.FLOAT);

    /** O empurrão de agora, que sobe e desce sozinho. */
    private double empurrão = EMPURRÃO_PARADO;

    /** Se quem a monta tem a maestria da vassoura. Lido ao montar, como no original. */
    private boolean temCoruja;

    public BroomEntity(EntityType<? extends BroomEntity> tipo, Level level) {
        super(tipo, level);
        this.blocksBuilding = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder construtor) {
        // -1 é "sem tinta": aí ela usa o castanho, que é o 12 da tabela do original
        construtor.define(COR, (byte) -1);
        construtor.define(DESDE_A_PANCADA, 0);
        construtor.define(APANHADO, 0.0f);
    }

    /** Há quantas batidas ela levou a última pancada, e quanto já apanhou: é a sacudidela do desenho. */
    public float desdeAPancada() {
        return this.entityData.get(DESDE_A_PANCADA);
    }

    public float apanhado() {
        return this.entityData.get(APANHADO);
    }

    /** Se quem a monta trouxe a maestria da coruja. Para as provas, e para quem desenhar. */
    public boolean temCoruja() {
        return this.temCoruja;
    }

    /** A cor das cerdas: a tinta que lhe passaram, ou nada. */
    public int cor() {
        return this.entityData.get(COR);
    }

    public void pinta(int qual) {
        this.entityData.set(COR, (byte) qual);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    /** Quem monta senta-se a 55% da altura dela, que é o {@code getMountedYOffset} do original. */
    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity quem, net.minecraft.world.entity.EntityDimensions tamanho,
                                               float escala) {
        return new Vec3(0.0, tamanho.height() * 0.55 * escala, 0.0);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float quanto) {
        if (this.isInvulnerableToBase(fonte)) return false;
        if (this.isRemoved()) return false;

        this.entityData.set(DESDE_A_PANCADA, 10);
        this.entityData.set(APANHADO, this.entityData.get(APANHADO) + quanto * 10.0f);
        this.markHurt();

        boolean deCriativo = fonte.getEntity() instanceof Player gente && gente.getAbilities().instabuild;
        if (deCriativo || this.entityData.get(APANHADO) > AGUENTA) {
            this.ejectPassengers();
            // e o que fica no chão é a vassoura encantada, que é o que se montava
            if (!deCriativo) {
                this.spawnAtLocation(level, new ItemStack(OccultaItems.ENCHANTED_BROOM));
            }
            this.discard();
        }
        return true;
    }

    @Override
    public InteractionResult interact(Player quem, InteractionHand mão, Vec3 onde) {
        if (!this.getPassengers().isEmpty() && this.getFirstPassenger() != quem) {
            return InteractionResult.SUCCESS;
        }
        if (this.level().isClientSide()) return InteractionResult.SUCCESS;

        // tinta na mão pinta as cerdas, e não monta
        ItemStack naMão = quem.getItemInHand(mão);
        DyeColor tinta = qualTinta(naMão);
        if (tinta != null) {
            this.pinta(tinta.getId());
            if (!quem.getAbilities().instabuild) naMão.shrink(1);
            return InteractionResult.SUCCESS;
        }

        // é aqui que se pergunta pela coruja, uma vez só, como no original
        this.temCoruja = Familiars.temMaestriaDeVassoura(quem);
        quem.startRiding(this);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.entityData.get(DESDE_A_PANCADA) > 0) {
            this.entityData.set(DESDE_A_PANCADA, this.entityData.get(DESDE_A_PANCADA) - 1);
        }
        if (this.entityData.get(APANHADO) > 0.0f) {
            this.entityData.set(APANHADO, this.entityData.get(APANHADO) - 1.0f);
        }

        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        Vec3 anda = this.getDeltaMovement();
        double antes = Math.sqrt(anda.x * anda.x + anda.z * anda.z);

        if (this.getFirstPassenger() instanceof LivingEntity quem) {
            this.levaQuemMonta(quem);
        } else {
            this.temCoruja = false;
            this.caiSozinha();
        }

        anda = this.getDeltaMovement();
        double agora = Math.sqrt(anda.x * anda.x + anda.z * anda.z);

        double teto = TETO + (this.temCoruja ? CORUJA_TETO : 0.0);
        if (agora > teto) {
            double corta = teto / agora;
            this.setDeltaMovement(anda.multiply(corta, corta, corta));
            agora = teto;
        }

        this.ajustaOEmpurrão(agora > antes);

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.99));
        this.setXRot(0.0f);
        this.viraParaOndeAnda();
    }

    /**
     * O empurrão sobe enquanto se ganha velocidade e desce quando não.
     *
     * <p>Os dois lados andam <b>um centésimo da diferença de cada vez</b> — o {@code MAX_ACCELERATION_FACTOR}
     * do original é 0,35 × 100, e é o que faz a vassoura demorar a pegar e demorar a largar.
     */
    private void ajustaOEmpurrão(boolean ganhando) {
        double passo = EMPURRÃO_MÁXIMO * 100.0;
        if (ganhando && this.empurrão < EMPURRÃO_MÁXIMO) {
            this.empurrão += (EMPURRÃO_MÁXIMO - this.empurrão) / passo;
            if (this.empurrão > EMPURRÃO_MÁXIMO) this.empurrão = EMPURRÃO_MÁXIMO;
        } else {
            this.empurrão -= (this.empurrão - EMPURRÃO_PARADO) / passo;
            if (this.empurrão < EMPURRÃO_PARADO) this.empurrão = EMPURRÃO_PARADO;
        }
    }

    /** O acelerador é andar para a frente; a direção é para onde quem monta está virado. */
    private void levaQuemMonta(LivingEntity quem) {
        float frente = quem.zza;
        Vec3 anda = this.getDeltaMovement();

        if (frente > 0.0f) {
            double soma = this.empurrão * (0.1 + (this.temCoruja ? CORUJA_EMPURRÃO : 0.0));
            double dx = -Math.sin(quem.getYRot() * Mth.DEG_TO_RAD);
            double dz = Math.cos(quem.getYRot() * Mth.DEG_TO_RAD);

            // a zona morta do olhar: entre -0,5 e 0,2 a vassoura fica à altura que está
            double olhar = -Math.sin(quem.getXRot() * Mth.DEG_TO_RAD);
            if (olhar > -0.5 && olhar < 0.2) olhar = 0.0;
            else if (olhar < 0.0) olhar *= 0.5;

            this.setDeltaMovement(anda.x + dx * soma, olhar * this.empurrão * 2.0, anda.z + dz * soma);
        } else if (frente == 0.0f && this.temCoruja) {
            // quem tem a coruja freia; quem não tem, desliza
            this.setDeltaMovement(anda.x * 0.9, anda.y, anda.z * 0.9);
        }
    }

    /** Sem ninguém em cima ela para e desce, devagar. */
    private void caiSozinha() {
        Vec3 anda = this.getDeltaMovement();
        double x = anda.x * 0.9;
        double z = anda.z * 0.9;
        this.setDeltaMovement(Math.abs(x) < 0.01 ? 0.0 : x,
                this.onGround() ? anda.y : -0.2, Math.abs(z) < 0.01 ? 0.0 : z);
    }

    /** O cabo aponta para onde ela andou, e não para onde quem monta olha. */
    private void viraParaOndeAnda() {
        double dx = this.xo - this.getX();
        double dz = this.zo - this.getZ();
        if (dx * dx + dz * dz <= 0.001) return;
        float rumo = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI);
        this.setYRot(this.getYRot() + Mth.wrapDegrees(rumo - this.getYRot()));
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput dados) {
        this.pinta(dados.getByteOr("BrushColor", (byte) -1));
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput dados) {
        dados.putByte("BrushColor", (byte) this.cor());
    }

    /** O que ela deixa quando se quebra à mão. */
    public static void larga(ServerLevel level, BlockPos onde) {
        net.minecraft.world.level.block.Block.popResource(level, onde,
                new ItemStack(OccultaItems.ENCHANTED_BROOM));
    }

    /**
     * Que tinta é esta, se for alguma.
     *
     * <p>No jogo de 2014 bastava perguntar ao {@code ItemDye} a cor dele. Hoje as dezesseis tintas são uma
     * {@code ColorCollection}, e a volta é percorrê-la — são dezesseis, e é uma vez por clique.
     */
    private static DyeColor qualTinta(ItemStack naMão) {
        for (DyeColor qual : DyeColor.values()) {
            if (naMão.is(Items.DYE.pick(qual))) return qual;
        }
        return null;
    }

    /** A tabela de cores do {@code ModelBroom}, para quem desenha. */
    public static DyeColor tinta(int qual) {
        if (qual < 0 || qual > 15) return DyeColor.BROWN;
        return DyeColor.byId(qual);
    }

    @Override
    public void push(Entity outro) {
        if (outro instanceof BroomEntity) super.push(outro);
    }

    @Override
    protected boolean canAddPassenger(Entity quem) {
        return this.getPassengers().isEmpty();
    }

    /**
     * A vassoura não se machuca de cair, e quem vai nela também não.
     *
     * <p>A parte de quem vai nela mora no {@link Brooms}, porque o original também a pôs fora: é um ouvinte de
     * queda, o {@code EntityBroom.EventHooks.onLivingFall}, e não um método da vassoura.
     */
    @Override
    public boolean causeFallDamage(double distância, float fator, DamageSource fonte) {
        return false;
    }
}
