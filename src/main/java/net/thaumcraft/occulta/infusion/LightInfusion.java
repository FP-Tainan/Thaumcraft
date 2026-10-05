package net.thaumcraft.occulta.infusion;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.BarrierBlock;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Infusão da Luz</b>: a {@code InfusionLight} do Witchery.
 *
 * <p>Se a do Outro Lugar é a de <b>não estar onde se está</b>, esta é a de <b>não ser visto, e pôr paredes
 * onde não há</b>. Ela não mata ninguém: tudo o que ela faz é com <b>luz</b> — luz que se dobra à volta de
 * quem a tem, e luz que endurece e vira muro.
 *
 * <h2>O que ela dá</h2>
 *
 * <ul>
 *   <li><b>segurar a Mão</b>: de trinta em trinta batidas, por <b>um</b>, se fica <b>invisível</b> — e, mais
 *       importante, <b>tudo o que estava perseguindo você num raio de vinte blocos perde o alvo</b>;</li>
 *   <li><b>agachado, largar antes de um segundo</b>, olhando para:
 *     <ul>
 *       <li>um <b>bicho</b>: ergue à volta dele uma <b>gaiola de luz</b>. Custa <b>três</b>;</li>
 *       <li>o <b>topo</b> de um bloco: levanta ali um <b>escudo de três colunas</b> entre você e o que vem.
 *           Custa <b>três</b>;</li>
 *       <li>o <b>lado</b> de um bloco: faz brotar dali uma <b>parede de dezesseis blocos</b> naquele rumo.
 *           Custa <b>três</b>;</li>
 *     </ul>
 *   </li>
 *   <li><b>socar um bicho</b>: se houver <b>três blocos de ar quatro acima dele</b>, ele é posto lá e
 *       <b>fechado numa gaiola</b>. Custa <b>cinco</b>.</li>
 * </ul>
 *
 * <p>Esse último é o poder mais útil deste mod e ninguém diz isso em voz alta: <b>tirar uma coisa do chão e
 * trancá-la no ar</b> resolve qualquer luta sem um golpe. Custa cinco de duzentos e só pede que haja céu.
 *
 * <h2>E a luz some se o poder acabar</h2>
 *
 * <p>A invisibilidade dura <b>trinta batidas</b> e é renovada de trinta em trinta — de modo que, no instante
 * em que a carga acaba, ela <b>é tirada</b> e você aparece. O original é explícito nisso: quem se esconde
 * com luz emprestada fica visível quando o empréstimo acaba.
 */
public class LightInfusion extends Infusion {
    /** De quantas em quantas batidas a luz se dobra outra vez, e desde quando. */
    public static final int DOBRA = 30;
    public static final int DEPOIS_DE = 19;

    /** Quanto a invisibilidade dura de cada vez. */
    public static final int INVISÍVEL = 30;

    /** A que distância os bichos perdem o alvo. */
    public static final double PERDEM = 20.0;

    /** A gaiola: raio e altura. */
    public static final int RAIO = 2;
    public static final int ALTO = 3;

    /** Quanto tempo a luz endurecida dura. */
    public static final int DURA = 200;

    /** Quão acima do bicho a gaiola do soco se faz. */
    public static final int ACIMA = 4;

    /** O alcance do olhar que ergue muros. */
    public static final double OLHAR = 16.0;

    /** E o comprimento da parede que brota de um lado. */
    public static final int BROTA = 16;

    /** O que cada coisa custa. */
    public static final int CUSTO_DOBRA = 1;
    public static final int CUSTO_MURO = 3;
    public static final int CUSTO_GAIOLA = 5;

    public LightInfusion(int id) {
        super(id);
    }

    // ------------------------------------------------------------------ o soco

    @Override
    public void soca(ServerLevel level, ServerPlayer quem, ItemStack mão, Entity noquê) {
        if (!(noquê instanceof LivingEntity bicho)) {
            falha(level, quem);
            return;
        }
        BlockPos lá = bicho.blockPosition().above(ACIMA);
        if (!cabeAliUmBicho(level, lá)) {
            falha(level, quem);
            return;
        }
        if (!this.gasta(level, quem, CUSTO_GAIOLA)) return;

        gaiola(level, lá, null);
        bicho.teleportTo(lá.getX() + 0.5, lá.getY(), lá.getZ() + 0.5);
    }

    /** Se há ar bastante para pôr um bicho ali e o fechar. */
    private static boolean cabeAliUmBicho(ServerLevel level, BlockPos onde) {
        for (BlockPos pé : new BlockPos[] {onde, onde.east(), onde.south(), onde.west(), onde.north()}) {
            for (int y = 0; y < ALTO; y++) {
                if (!level.getBlockState(pé.above(y)).isAir()) return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ a luz que se dobra

    @Override
    public void segurando(ServerLevel level, ServerPlayer quem, ItemStack mão, int faltam) {
        int passou = this.quantoSeSegura() - faltam;
        if (passou <= DEPOIS_DE || passou % DOBRA != 0) return;
        dobraALuz(level, quem, this.gasta(level, quem, CUSTO_DOBRA));
    }

    @Override
    public void largou(ServerLevel level, ServerPlayer quem, ItemStack mão, int faltam) {
        dobraALuz(level, quem, false);
        int passou = this.quantoSeSegura() - faltam;
        if (passou >= 20 || !quem.isShiftKeyDown()) return;

        HitResult onde = olha(level, quem);
        if (onde == null) {
            falha(level, quem);
            return;
        }
        if (onde instanceof EntityHitResult bateu && bateu.getEntity() instanceof LivingEntity bicho) {
            if (!this.gasta(level, quem, CUSTO_MURO)) return;
            gaiola(level, bicho.blockPosition(), quem);
            return;
        }
        if (!(onde instanceof BlockHitResult bloco)) return;

        if (bloco.getDirection() == net.minecraft.core.Direction.UP) {
            if (!this.gasta(level, quem, CUSTO_MURO)) return;
            escudo(level, quem, bloco);
            return;
        }
        if (!this.gasta(level, quem, CUSTO_MURO)) return;
        brota(level, quem, bloco);
    }

    /**
     * <b>Dobra a luz à volta dele</b>, ou deixa de a dobrar.
     *
     * <p>Dobrada, ele fica invisível por trinta batidas e <b>tudo o que o perseguia a vinte blocos perde o
     * alvo</b>. Deixando de a dobrar, a invisibilidade é <b>tirada</b> — e não deixada a acabar sozinha.
     */
    private static void dobraALuz(ServerLevel level, ServerPlayer quem, boolean dobra) {
        if (!dobra) {
            quem.removeEffect(MobEffects.INVISIBILITY);
            return;
        }
        quem.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, INVISÍVEL, 0, true, true));
        esquecem(level, quem);
    }

    /**
     * <b>E tudo o que o perseguia perde o alvo.</b>
     *
     * <p>É a metade boa do poder, e a que ninguém espera: a invisibilidade de trinta batidas só esconde,
     * mas isto <b>desfaz a perseguição</b> — quem já vinha atrás de você deixa de saber para onde ia.
     *
     * <p>Lê o <b>alvo cru</b>, e não o que o jogo deixa ver: o original lê o campo direto, e o jogo de hoje
     * passa o {@code getTarget} por um filtro que recusa, entre outros, quem está no criativo. Quem está
     * perseguindo você está perseguindo você.
     */
    public static void esquecem(ServerLevel level, ServerPlayer quem) {
        AABB perto = new AABB(quem.getX() - PERDEM, quem.getY(), quem.getZ() - PERDEM,
                quem.getX() + PERDEM, quem.getY() + 2.0, quem.getZ() + PERDEM);
        for (Mob bicho : level.getEntitiesOfClass(Mob.class, perto)) {
            if (bicho.getTargetUnchecked() != quem) continue;
            if (bicho.distanceTo(quem) > PERDEM) continue;
            bicho.setTarget(null);
        }
    }

    // ------------------------------------------------------------------ a luz que endurece

    /** O que ele está olhando, até dezesseis blocos. */
    private static @Nullable HitResult olha(ServerLevel level, ServerPlayer quem) {
        Vec3 olhos = quem.getEyePosition();
        Vec3 rumo = olhos.add(quem.getLookAngle().scale(OLHAR));
        for (Entity bicho : level.getEntities(quem, new AABB(olhos, rumo).inflate(1.0))) {
            if (!(bicho instanceof LivingEntity)) continue;
            var onde = bicho.getBoundingBox().inflate(0.3).clip(olhos, rumo);
            if (onde.isPresent()) return new EntityHitResult(bicho, onde.get());
        }
        BlockHitResult bateu = level.clip(new ClipContext(olhos, rumo, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, quem));
        return bateu.getType() == HitResult.Type.MISS ? null : bateu;
    }

    /**
     * <b>A gaiola.</b>
     *
     * <p>Um chão cheio por baixo, um teto cheio por cima e três anéis de parede entre eles — tudo em luz
     * endurecida, que dura dez segundos.
     */
    private static void gaiola(ServerLevel level, BlockPos onde, @Nullable ServerPlayer dono) {
        disco(level, onde.below(), RAIO, dono);
        for (int y = 0; y < ALTO; y++) anel(level, onde.above(y), RAIO, dono);
        disco(level, onde.above(ALTO), RAIO, dono);
    }

    /** Um anel de luz de um raio, naquela altura. */
    private static void anel(ServerLevel level, BlockPos meio, int raio, @Nullable ServerPlayer dono) {
        for (int x = -raio; x <= raio; x++) {
            for (int z = -raio; z <= raio; z++) {
                int longe = x * x + z * z;
                if (longe > raio * raio || longe < (raio - 1) * (raio - 1)) continue;
                põe(level, meio.offset(x, 0, z), dono);
            }
        }
    }

    /** E um disco cheio. */
    private static void disco(ServerLevel level, BlockPos meio, int raio, @Nullable ServerPlayer dono) {
        for (int x = -raio; x <= raio; x++) {
            for (int z = -raio; z <= raio; z++) {
                if (x * x + z * z > raio * raio) continue;
                põe(level, meio.offset(x, 0, z), dono);
            }
        }
    }

    /**
     * <b>O escudo:</b> três colunas de três, lado a lado, à frente de quem o ergueu.
     *
     * <p>O original acha o rumo pelo ângulo da cara e põe uma coluna no lugar em que se bateu e duas a um
     * bloco de cada lado — de modo que o escudo sai <b>atravessado</b> ao olhar, e não ao longo dele.
     */
    public static void escudo(ServerLevel level, ServerPlayer quem, BlockHitResult bateu) {
        BlockPos onde = bateu.getBlockPos();
        int acima = level.getBlockState(onde).isSolidRender() ? 1 : 0;
        BlockPos pé = onde.above(acima);

        net.minecraft.core.Direction rumo = quem.getDirection();
        net.minecraft.core.Direction lado = rumo.getClockWise();
        coluna(level, quem, pé);
        coluna(level, quem, pé.relative(lado));
        coluna(level, quem, pé.relative(lado.getOpposite()));
    }

    private static void coluna(ServerLevel level, ServerPlayer quem, BlockPos pé) {
        for (int y = 0; y < ALTO; y++) põe(level, pé.above(y), quem);
    }

    /**
     * <b>A parede que brota:</b> dezesseis blocos de luz a partir da face em que se bateu, no rumo dela.
     *
     * <p>Ela <b>para no primeiro bloco cheio</b> que encontrar, de modo que o comprimento dela é o que o
     * lugar deixar.
     */
    private static void brota(ServerLevel level, ServerPlayer quem, BlockHitResult bateu) {
        net.minecraft.core.Direction rumo = bateu.getDirection();
        BlockPos onde = bateu.getBlockPos();
        boolean cheio = level.getBlockState(onde).isSolidRender();
        int começa = rumo == net.minecraft.core.Direction.UP && !cheio ? 0 : 1;
        for (int i = começa; i < BROTA; i++) {
            BlockPos ali = onde.relative(rumo, i);
            if (!level.getBlockState(ali).isAir() && !level.getBlockState(ali).canBeReplaced()) break;
            põe(level, ali, quem);
        }
    }

    private static void põe(ServerLevel level, BlockPos onde, @Nullable ServerPlayer dono) {
        BarrierBlock.put(level, onde, DURA, true, dono == null ? null : dono.getUUID());
    }
}
