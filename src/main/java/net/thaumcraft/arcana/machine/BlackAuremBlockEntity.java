package net.thaumcraft.arcana.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.arcana.ArcanaBlocks;
import net.thaumcraft.arcana.ArcanaDamage;
import net.thaumcraft.arcana.power.PowerNodes;
import net.thaumcraft.arcana.power.PowerType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * O <b>Aurem Negro</b>: o {@code TileEntityBlackAurem} da 1.4.0.009.
 *
 * <p>A fonte que se alimenta de <b>vida</b>. Ele puxa para si todo bicho vivo a <b>dez casas</b>,
 * levanta-o do chão e, quando o tem bem por cima, <b>mói-o</b> — quatro de dano por batida, e o que ele
 * tira de quem morre é <b>Trevas</b>.
 *
 * <p><b>Quanto vale uma morte:</b> {@code ceil(vida máxima × (idade em segundos) % 5000) × multiplicador}.
 * Um bicho que acabou de nascer não vale quase nada; um que andou pelo mundo meia hora vale um obelisco
 * inteiro. Aquele resto por cinco mil é o teto de uma morte só.
 *
 * <p><b>Quem ele não puxa:</b> gente, chefes, invulneráveis, e os bichos que o próprio Ars Magica 2 põe
 * no mundo como ferramenta — o Darkling, o trenó de ar, o braço do Guardião do Inverno, a foice atirada,
 * o Flicker e o ajudante de sombra. Nem o que foi invocado por alguém.
 *
 * <p>E ele <b>não puxa quem está atrás de uma parede</b>: há um tiro de linha do topo dele até os olhos
 * do bicho, e se houver bloco pelo caminho o bicho fica onde está.
 *
 * <p>Os topos dele são três: <b>quimerita</b> (1,1), <b>obsidiana</b> (1,5) e <b>pedra-do-sol</b> (2,0),
 * sobre pilares de <b>tijolo do Nether</b>. É a fonte mais forte do mod — dez mil de teto.
 */
public class BlackAuremBlockEntity extends EssenceGeneratorBlockEntity {
    /** Quanto ele guarda: <b>10 000</b>. */
    public static final int CAPACITY = 10_000;
    /** A que distância ele procura bichos. */
    public static final int REACH = 10;
    /** E até que altura acima dele. */
    public static final int REACH_UP = 4;
    /** De quantas em quantas batidas ele volta a procurar. */
    public static final int SCAN_EVERY = 25;
    /** O quanto ele puxa de lado por batida, e o quanto levanta. */
    public static final double PULL = 0.1;
    public static final double LIFT = 0.05;
    /** Quão centrado o bicho tem de estar para o moinho pegar (ao quadrado). */
    public static final double GRIND_RADIUS_SQ = 1.3;
    /** E quão acima dele. */
    public static final double GRIND_HEIGHT = 2.0;
    /** Quanto dói, por batida. */
    public static final float GRIND_DAMAGE = 4.0f;
    /** O teto do que uma morte só pode render. */
    public static final float KILL_CAP = 5000.0f;

    private static final Multiblock FIGURA = new Multiblock("blackaurem_structure");
    private static final Multiblock.Group PILARES = FIGURA.createGroup("pillars", MUTEX_PILLARS);
    private static final Multiblock.Group QUIMERITA = FIGURA.createGroup("caps_chimerite", MUTEX_CAPS);
    private static final Multiblock.Group OBSIDIANA = FIGURA.createGroup("caps_obsidian", MUTEX_CAPS);
    private static final Multiblock.Group SOL = FIGURA.createGroup("caps_sunstone", MUTEX_CAPS);
    private static final Multiblock.Group GIZ = FIGURA.createGroup("wizardChalkCircle", Multiblock.MAIN_MUTEX);

    private static final Map<Multiblock.Group, Float> TOPOS_VALEM =
            caps(QUIMERITA, 1.1f, OBSIDIANA, 1.5f, SOL, 2.0f);

    static {
        FIGURA.add(0, 0, 0, ArcanaBlocks.BLACK_AUREM);
        for (int x : new int[]{-2, 2}) {
            for (int z : new int[]{-2, 2}) {
                FIGURA.add(PILARES, x, 0, z, Blocks.NETHER_BRICKS);
                FIGURA.add(PILARES, x, 1, z, Blocks.NETHER_BRICKS);
                FIGURA.add(QUIMERITA, x, 2, z, ArcanaBlocks.MATERIAL_BLOCKS.get("chimerite"));
                FIGURA.add(OBSIDIANA, x, 2, z, Blocks.OBSIDIAN);
                FIGURA.add(SOL, x, 2, z, ArcanaBlocks.MATERIAL_BLOCKS.get("sunstone"));
            }
        }
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                FIGURA.add(GIZ, i, 0, j, ArcanaBlocks.WIZARD_CHALK);
            }
        }
    }

    private final List<LivingEntity> presos = new ArrayList<>();
    private int ticksSinceLastScan;

    public BlackAuremBlockEntity(BlockPos onde, BlockState feitio) {
        super(ArcanaBlocks.BLACK_AUREM_ENTITY, onde, feitio, CAPACITY);
    }

    public static Multiblock structure() {
        return FIGURA;
    }

    /** Quantos bichos ele tem presos agora: é o que a prova pergunta. */
    public int caught() {
        return this.presos.size();
    }

    @Override
    public void serverTick(ServerLevel level) {
        this.surroundingCheckTicks++;
        if (this.ticksSinceLastScan++ > SCAN_EVERY) {
            this.scan(level);
            this.ticksSinceLastScan = 0;
        }

        this.presos.removeIf(bicho -> bicho.isRemoved() || !bicho.isAlive());
        BlockPos onde = this.getBlockPos();
        for (LivingEntity bicho : List.copyOf(this.presos)) {
            if (atrásDeParede(level, onde, bicho)) continue;
            this.pull(level, onde, bicho);
        }

        if (this.surroundingCheckTicks % SURROUNDING_CHECK == 0) this.surroundings(level);
        super.serverTick(level);
    }

    /** O {@code rayTraceBlocks} do original: do topo do Aurem até os olhos do bicho. */
    private static boolean atrásDeParede(ServerLevel level, BlockPos onde, LivingEntity bicho) {
        Vec3 daqui = new Vec3(onde.getX() + 0.5, onde.getY() + 1.5, onde.getZ() + 0.5);
        Vec3 até = new Vec3(bicho.getX(), bicho.getEyeY(), bicho.getZ());
        return level.clip(new ClipContext(daqui, até, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, bicho))
                .getType() != HitResult.Type.MISS;
    }

    /** Puxa, levanta e, estando o bicho bem por cima, mói. */
    private void pull(ServerLevel level, BlockPos onde, LivingEntity bicho) {
        bicho.setDeltaMovement(Vec3.ZERO);
        double deltaX = onde.getX() + 0.5 - bicho.getX();
        double deltaZ = onde.getZ() + 0.5 - bicho.getZ();
        double ângulo = Math.atan2(deltaZ, deltaX);
        double paraCima = LIFT;

        double distânciaPlana = deltaX * deltaX + deltaZ * deltaZ;
        double acima = onde.getY() - bicho.getY();
        if (distânciaPlana < GRIND_RADIUS_SQ && acima < -GRIND_HEIGHT) {
            paraCima = 0.0;
            if (bicho.hurtServer(level, ArcanaDamage.darkNexus(level, null), GRIND_DAMAGE) && bicho.getHealth() <= 0.0f) {
                bicho.discard();
                this.swallow(level, bicho);
            }
        }

        bicho.move(MoverType.SELF, new Vec3(Math.cos(ângulo) * PULL, paraCima, Math.sin(ângulo) * PULL));
    }

    /** O que uma morte rende: a conta do original, com o teto dela. */
    private void swallow(ServerLevel level, LivingEntity bicho) {
        float quanto = (int) Math.ceil(bicho.getMaxHealth() * (bicho.tickCount / 20) % KILL_CAP)
                * this.powerMultiplier;
        PowerNodes.of(level).insertPower(this.getBlockPos(), this, PowerType.DARK, quanto);
    }

    /** Quem ele puxa, e quem ele deixa em paz. */
    private void scan(ServerLevel level) {
        BlockPos onde = this.getBlockPos();
        AABB roda = new AABB(onde.getX() - REACH, onde.getY(), onde.getZ() - REACH,
                onde.getX() + REACH, onde.getY() + REACH_UP, onde.getZ() + REACH);
        for (LivingEntity bicho : level.getEntitiesOfClass(LivingEntity.class, roda)) {
            if (!pode(bicho) || this.presos.contains(bicho)) continue;
            this.presos.add(bicho);
        }
    }

    /**
     * Se o Aurem pode puxar aquele.
     *
     * <p>A lista de exceções do original é de bichos dele — o Darkling, o trenó de ar, o braço do
     * Guardião do Inverno, a foice atirada, o Flicker, o ajudante de sombra. Deste porte só o
     * <b>Darkling</b> existe até agora; os outros entram com as fatias deles, e a guarda cresce com eles.
     */
    private static boolean pode(LivingEntity bicho) {
        if (bicho.isInvulnerable()) return false;
        if (bicho instanceof Player) return false;
        if (net.thaumcraft.arcana.Summons.éInvocado(bicho)) return false;
        return !bicho.getType().builtInRegistryHolder().is(AUREM_IGNORA);
    }

    /** A lista dos que ele não puxa, que cresce conforme os bichos do ramo vão entrando. */
    public static final net.minecraft.tags.TagKey<net.minecraft.world.entity.EntityType<?>> AUREM_IGNORA =
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,
                    net.thaumcraft.Thaumcraft.id("aurem_ignora"));

    /**
     * <b>Mais um campo que ninguém lê.</b>
     *
     * <p>O original guarda aqui um {@code rotation} que cresce 0,15 por batida — e o desenhista dele não
     * o usa: o giro do desenho sai do <b>contador de batidas de quem olha</b>, não do bloco. O campo não
     * veio para cá.
     */
    @Override
    protected Multiblock figura() {
        return FIGURA;
    }

    @Override
    protected Multiblock.Group pilares() {
        return PILARES;
    }

    @Override
    protected Multiblock.Group giz() {
        return GIZ;
    }

    @Override
    protected Map<Multiblock.Group, Float> topos() {
        return TOPOS_VALEM;
    }

    @Override
    protected PowerType type() {
        return PowerType.DARK;
    }

    /** O Aurem abençoa com grau três. */
    @Override
    protected int blessingAmplifier() {
        return 3;
    }
}
