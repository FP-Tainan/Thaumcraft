package net.thaumcraft.arcana;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/**
 * Um feitiço voando: a {@code EntitySpellProjectile} do Ars Magica 2.
 *
 * <p>Ele leva <b>a frase inteira</b> dentro de si — e não só o efeito. Quando bate, corre as Essências da etapa
 * que o mandou voar e, em seguida, <b>lança o que sobra</b> dali. É o que faz um Projétil seguido de uma Área
 * explodir onde bateu, e não onde foi lançado.
 *
 * <p>Ele não tem física de projétil do jogo: o original se move à mão, sem arrasto e sem gravidade, a não ser a
 * que o modificador de Gravidade lhe der. Um feitiço de projétil sem modificadores <b>voa a direito para
 * sempre</b> até bater ou até acabarem as 100 batidas de vida.
 *
 * <p><b>Ele atravessa e ele salta.</b> Quantas vezes atravessa, quem diz é o modificador de Perfuração; de quantos
 * saltos é capaz, o de Ricochete. Cada bicho e cada bloco só contam <b>uma vez</b>: o original guarda a lista do
 * que já pegou, para um projétil que atravessa não bater três vezes no mesmo zumbi.
 *
 * <p><b>Desvios declarados.</b> Ficaram de fora duas coisas do original, todas presas ao que ainda não está
 * portado: o <b>perseguir</b> ({@code setHoming}) — que no original é <i>código morto</i>, porque não existe
 * nenhum modificador que ligue o {@code HOMING}; o <b>refletir feitiços</b>, que depende da lista de bênçãos
 * ({@code BuffList.spellReflect}), que depende da lista de bênçãos.
 */
public class SpellProjectileEntity extends Projectile {
    /**
     * Quanto tempo ele voa sem bater em nada: as <b>100 batidas</b> do original.
     *
     * <p><b>É um erro do original que este porte mantém.</b> A Forma do Projétil lê o modificador de Duração
     * para decidir a vida do projétil — e <b>joga o número fora</b>: o campo que a guardaria é final e nasce
     * a menos um, e o {@code func_70071_h_} troca o menos um por 100. Duração num Projétil não faz nada. Fica
     * como está, porque mexer nisso mudava o alcance de todo feitiço de projétil do jogo.
     */
    public static final int LIFE = 100;

    /** Quanto da velocidade sobra a cada salto: o {@code FrictionCoefficient} do original, que nasce a 0,8. */
    public static final double FRICTION = 0.8;

    /** A velocidade vertical que a gravidade não passa, para cima nem para baixo. */
    public static final double TERMINAL_VELOCITY = 2.0;

    private Spell spell = Spell.EMPTY;
    private double gravity;
    private int bounces;
    private int pierces;
    private boolean targetNonSolid;

    /** O que ele já pegou, para não pegar duas vezes: os {@code entityHits} e {@code blockhits}. */
    private final Set<Integer> bichosApanhados = new HashSet<>();
    private final Set<BlockPos> blocosApanhados = new HashSet<>();

    public SpellProjectileEntity(EntityType<? extends SpellProjectileEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /**
     * O projétil recém-lançado, posto onde o original o põe.
     *
     * <p>Ele nasce à altura dos olhos, dezesseis centésimos de bloco atrás da linha do ombro e um décimo abaixo
     * dos olhos — é a mesma conta da flecha do jogo antigo, e é o que faz o projétil sair <i>da mão</i> e não
     * da cara.
     */
    public SpellProjectileEntity(ServerLevel level, LivingEntity quem, Spell feitiço, double velocidade) {
        this(ArcanaEntities.SPELL_PROJECTILE, level);
        this.setOwner(quem);
        this.setSpell(feitiço);

        float giro = quem.getYRot();
        float mira = quem.getXRot();
        double x = quem.getX() - Math.cos(giro * Math.PI / 180.0) * 0.16;
        double y = quem.getEyeY() - 0.1;
        double z = quem.getZ() - Math.sin(giro * Math.PI / 180.0) * 0.16;
        this.snapTo(x, y, z, giro, mira);

        // o original parte de um empurrãozinho de um centésimo e depois normaliza: o que conta é a direção
        float f = 0.01f;
        double mx = -Math.sin(giro * Math.PI / 180.0) * Math.cos(mira * Math.PI / 180.0) * f;
        double my = -Math.sin(mira * Math.PI / 180.0) * f;
        double mz = Math.cos(giro * Math.PI / 180.0) * Math.cos(mira * Math.PI / 180.0) * f;
        this.heading(mx, my, mz, velocidade);
    }

    /** O {@code setSpellProjectileHeading}: aponta e lhe dá aquela velocidade. */
    public void heading(double mx, double my, double mz, double velocidade) {
        Vec3 rumo = new Vec3(mx, my, mz).normalize().scale(velocidade);
        this.setDeltaMovement(rumo);
        double plano = Math.sqrt(rumo.x * rumo.x + rumo.z * rumo.z);
        this.setYRot((float) (Math.atan2(rumo.x, rumo.z) * 180.0 / Math.PI));
        this.setXRot((float) (Math.atan2(rumo.y, plano) * 180.0 / Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public Spell spell() {
        return this.spell;
    }

    public void setSpell(Spell feitiço) {
        this.spell = feitiço;
        this.entityData.set(AFFINITY, feitiço.mainAffinity().ordinal());
        Integer cor = feitiço.data(Modifiers.COLOUR.name());
        this.entityData.set(COLOR, cor == null ? SEM_COR : cor);
    }

    public void setGravity(double quanto) {
        this.gravity = quanto;
    }

    public void setBounces(int quantos) {
        this.bounces = quantos;
    }

    public void setPierces(int quantos) {
        this.pierces = quantos;
    }

    public void setTargetNonSolid(boolean sim) {
        this.targetNonSolid = sim;
    }

    /**
     * A Afinidade que ele mostra, mandada ao cliente: os {@code DW_ICON_NAME} e {@code DW_COLOR} do original,
     * num número só.
     *
     * <p>O feitiço inteiro não vai para o cliente — ele não precisa dele para nada, e ir seria mandar uma
     * frase por projétil. O que vai é a Afinidade, que é o que decide a figura e a cor.
     */
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> AFFINITY =
            net.minecraft.network.syncher.SynchedEntityData.defineId(SpellProjectileEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);

    /**
     * E a <b>cor escolhida</b>, se a frase trouxer a Cor: o {@code DW_COLOR} do original.
     *
     * <p>Vai à parte da Afinidade porque são duas coisas diferentes: a Afinidade decide a <b>figura</b>, e a
     * cor só a pinta. Um projétil de fogo com a Cor azul continua com a cara do fogo.
     */
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> COLOR =
            net.minecraft.network.syncher.SynchedEntityData.defineId(SpellProjectileEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);

    /** O valor que quer dizer "ninguém escolheu cor": o {@code -1} do original. */
    public static final int SEM_COR = -1;

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(AFFINITY, Affinity.NONE.ordinal());
        builder.define(COLOR, SEM_COR);
    }

    /** A cor escolhida, ou {@link #SEM_COR} se a frase não trouxer a Cor. */
    public int color() {
        return this.entityData.get(COLOR);
    }

    /** Força a cor com que ele se desenha, sem olhar para o feitiço — como o {@link #forceAffinity}. */
    public void forceColor(int cor) {
        this.entityData.set(COLOR, cor);
    }

    /**
     * Força a Afinidade com que ele se desenha, sem olhar para o feitiço.
     *
     * <p>É o {@code ForcedAffinity} do original, que lá vive numa chave do NBT do item e serve para um
     * feitiço escrito sair com a cara que quem o escreveu quis. Aqui ela existe para o mesmo, e as provas de
     * tela usam-na para pôr as dez lado a lado.
     */
    public void forceAffinity(Affinity qual) {
        this.entityData.set(AFFINITY, qual.ordinal());
    }

    /** A Afinidade com que ele se desenha. */
    public Affinity affinity() {
        return Affinity.values()[Math.clamp(this.entityData.get(AFFINITY), 0, Affinity.values().length - 1)];
    }

    // ------------------------------------------------------------------ o voo

    @Override
    public void tick() {
        super.tick();

        // ele morre com quem o lançou: um feitiço sem mago por trás não continua voando
        if (!this.level().isClientSide()) {
            Entity quem = this.getOwner();
            if (quem == null || quem.isRemoved()) {
                this.discard();
                return;
            }
            if (this.tickCount >= LIFE) {
                this.discard();
                return;
            }
        }

        HitResult bateu = this.mira();
        if (bateu != null) this.resolver(bateu);
        if (this.isRemoved()) return;

        // a gravidade do original, com o seu sinal ao contrário: um valor negativo faz cair
        Vec3 anda = this.getDeltaMovement();
        if (this.gravity < 0.0 && anda.y > -TERMINAL_VELOCITY) {
            anda = anda.add(0.0, this.gravity, 0.0);
        } else if (this.gravity > 0.0 && anda.y < TERMINAL_VELOCITY) {
            anda = anda.subtract(0.0, this.gravity, 0.0);
        }
        this.setDeltaMovement(anda);

        this.setPos(this.getX() + anda.x, this.getY() + anda.y, this.getZ() + anda.z);
        this.aponta(anda);
    }

    /** A cara dele se vira um quinto de caminho para onde ele vai, como no original. */
    private void aponta(Vec3 anda) {
        double plano = Math.sqrt(anda.x * anda.x + anda.z * anda.z);
        float giro = (float) Math.atan2(anda.x, anda.z);
        float mira = (float) Math.atan2(anda.y, plano);
        this.setYRot(this.yRotO + (giro - this.yRotO) * 0.2f);
        this.setXRot(this.xRotO + (mira - this.xRotO) * 0.2f);
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    /**
     * O que ele vai encontrar no passo que vem: bloco e bicho, o mais perto dos dois.
     *
     * <p>O original olha sempre para os líquidos ao traçar o raio, e só depois decide se pega neles — é o que o
     * {@link #resolver} faz ao ver se o bloco tem caixa.
     */
    private HitResult mira() {
        Vec3 daqui = this.position();
        Vec3 até = daqui.add(this.getDeltaMovement());

        BlockHitResult bloco = this.level().clip(new ClipContext(daqui, até,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, this));
        Vec3 fim = bloco.getType() == HitResult.Type.MISS ? até : bloco.getLocation();

        EntityHitResult bicho = Shapes.nearest(this.level(), this, daqui, fim,
                e -> e.canBeHitByProjectile() && e != this.getOwner());

        if (bicho != null) return bicho;
        return bloco.getType() == HitResult.Type.MISS ? null : bloco;
    }

    /** O {@code doHit} do original: decide se salta, se passa ao lado, ou se bate de verdade. */
    private void resolver(HitResult bateu) {
        boolean atravessa = this.pierces > 0;
        boolean bate = true;

        if (bateu instanceof BlockHitResult nisso) {
            if (this.bounces > 0) {
                bate = false;
                this.saltar(nisso.getDirection());
                this.bounces--;
            }
            // um bloco sem caixa — água, erva alta, ar de tocha — só se pega com o modificador certo
            var estado = this.level().getBlockState(nisso.getBlockPos());
            if (estado.getCollisionShape(this.level(), nisso.getBlockPos()).isEmpty() && !this.targetNonSolid) {
                bate = false;
            }
        }

        if (bate) this.bater(bateu, atravessa);
    }

    /** O salto: inverte o que lhe interessa da velocidade e perde parte dela. */
    private void saltar(Direction face) {
        Vec3 anda = this.getDeltaMovement();
        this.setDeltaMovement(switch (face.getAxis()) {
            case Y -> new Vec3(anda.x, anda.y * FRICTION * -1.0, anda.z);
            case Z -> new Vec3(anda.x, anda.y, anda.z * FRICTION * -1.0);
            case X -> new Vec3(anda.x * FRICTION * -1.0, anda.y, anda.z);
        });
    }

    /**
     * O {@code HitObject}: corre a etapa da frente no que pegou e lança o resto da frase dali.
     *
     * <p>Repare que o original <b>não</b> se importa se as Essências pegaram: o resto da frase corre de
     * qualquer maneira, ao contrário do que as Formas de Toque e de Área fazem. Fica assim.
     */
    private void bater(HitResult bateu, boolean atravessa) {
        if (!(this.level() instanceof ServerLevel level)) return;
        if (!(this.getOwner() instanceof LivingEntity quem)) return;

        if (bateu instanceof EntityHitResult nele) {
            Entity alvo = nele.getEntity();
            if (alvo == quem) return;
            if (!this.bichosApanhados.add(alvo.getId())) return;

            SpellCast.onEntity(level, this.spell, quem, alvo);
            SpellCast.cast(level, this.spell.pop(), quem, alvo, nele.getLocation());
        } else {
            BlockHitResult nisso = (BlockHitResult) bateu;
            if (!this.blocosApanhados.add(nisso.getBlockPos())) return;

            SpellCast.onBlock(level, this.spell, quem, nisso.getBlockPos(),
                    nisso.getDirection(), nisso.getLocation());
            SpellCast.cast(level, this.spell.pop(), quem, null, nisso.getLocation());
        }

        this.pierces--;
        if (!atravessa) this.discard();
    }

    @Override
    protected boolean canHitEntity(Entity quem) {
        return super.canHitEntity(quem) && quem != this.getOwner();
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    // ------------------------------------------------------------------ guardar

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("spell", Spell.CODEC, this.spell);
        output.putDouble("gravity", this.gravity);
        output.putInt("bounces", this.bounces);
        output.putInt("pierces", this.pierces);
        output.putBoolean("target_nonsolid", this.targetNonSolid);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSpell(input.read("spell", Spell.CODEC).orElse(Spell.EMPTY));
        this.gravity = input.getDoubleOr("gravity", 0.0);
        this.bounces = input.getIntOr("bounces", 0);
        this.pierces = input.getIntOr("pierces", 0);
        this.targetNonSolid = input.getBooleanOr("target_nonsolid", false);
    }
}
