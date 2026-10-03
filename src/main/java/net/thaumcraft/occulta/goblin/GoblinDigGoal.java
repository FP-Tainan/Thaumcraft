package net.thaumcraft.occulta.goblin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * <b>O goblin cava</b>: o {@code EntityAIDigBlocks} do Witchery.
 *
 * <p>Só <b>na corda</b> e só com uma <b>picareta</b> na mão. E o jeito como ele escolhe onde cavar é a melhor
 * parte: ele <b>vira-se para um lado a esmo</b> e olha em frente quatro blocos. O que estiver ali, se for
 * cavável, é o que ele cava.
 *
 * <p>Não há plano, não há área marcada, não há ordem. Um goblin na corda numa caverna <b>abre buraco</b>, e
 * para onde ele abre é problema de quem o levou lá. Ao fim de <b>quinze</b> olhadas falhadas ele desiste de
 * olhar em frente e <b>olha para baixo</b> — que é como ele acaba por cavar um poço.
 *
 * <p>O que ele cava é <b>pedra, areia, terra, barro e gravilha</b>. Madeira não, folha não, e nada que seja
 * indestrutível. Cada bloco leva <b>três segundos</b>.
 *
 * <p><b>Fica de fora, declarado:</b> a <b>picareta de koboldite</b>, que no original cava de quatro em quatro
 * batidas em vez de sessenta — quinze vezes mais depressa — e funde metade do minério que apanha. É uma linha
 * de material inteira, e vem com ela.
 */
public class GoblinDigGoal extends Goal {
    /** Até onde ele olha: os quatro blocos do original. */
    public static final double ALCANCE = 4.0;

    /** E quando desiste de olhar em frente, para olhar para baixo. */
    public static final int DESISTE_AOS = 15;

    /** Quanto ele demora a tirar um bloco: os sessenta tiques do original. */
    public static final int DEMORA = 60;

    /** A que distância ele consegue cavar. */
    public static final double À_MÃO = 2.5;

    /** O passo com que ele vai até lá. */
    public static final double PASSO = 0.6;

    private final GoblinEntity goblin;
    private final double alcance;

    @Nullable
    private BlockPos onde;
    private int falhadas;
    private int espera = DEMORA;

    public GoblinDigGoal(GoblinEntity goblin, double alcance) {
        this.goblin = goblin;
        this.alcance = alcance;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.podeCavar()) return false;
        // o original só olha de duas em duas vezes, para não varrer o mundo a cada batida
        if (this.goblin.getRandom().nextInt(2) != 0) return false;

        BlockPos achou = this.olha(this.falhadas >= DESISTE_AOS);
        if (achou == null) {
            this.falhadas++;
            this.onde = null;
            return false;
        }
        this.falhadas = 0;
        this.onde = achou;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.podeCavar() && this.onde != null;
    }

    /** Na corda, com picareta, e sem estar a adorar. */
    private boolean podeCavar() {
        if (!this.goblin.isLeashed() || this.goblin.adorando()) return false;
        var naMão = this.goblin.getMainHandItem();
        return naMão.is(net.minecraft.tags.ItemTags.PICKAXES);
    }

    @Override
    public void start() {
        if (this.onde == null) return;
        this.goblin.getNavigation().moveTo(this.onde.getX() + 0.5, this.onde.getY(),
                this.onde.getZ() + 0.5, PASSO);
    }

    @Override
    public void stop() {
        this.goblin.trabalhando(false);
        this.onde = null;
    }

    @Override
    public void tick() {
        if (this.onde == null) return;

        double longe = this.goblin.distanceToSqr(this.onde.getX() + 0.5, this.onde.getY() + 0.5,
                this.onde.getZ() + 0.5);
        if (longe > À_MÃO * À_MÃO) {
            if (this.goblin.getNavigation().isDone()) {
                // não chegou lá: esquece aquele e procura outro
                this.onde = null;
                this.espera = DEMORA;
                this.goblin.trabalhando(false);
            } else {
                this.goblin.trabalhando(true);
            }
            return;
        }

        this.goblin.trabalhando(true);
        if (--this.espera > 0) return;
        this.espera = DEMORA;

        if (this.goblin.level() instanceof ServerLevel level) cava(level, this.onde, this.goblin);
        this.onde = this.olha(false);
    }

    /**
     * Ele vira-se a esmo e olha em frente: o {@code raytraceBlocks} do original.
     *
     * <p>É literalmente isso — um rumo sorteado de zero a trezentos e sessenta, e um olhar reto. Com as
     * olhadas falhadas a mais, o olhar aponta <b>para baixo</b>.
     */
    @Nullable
    private BlockPos olha(boolean paraBaixo) {
        float rumo = this.goblin.getRandom().nextInt(360);
        this.goblin.setYRot(rumo);

        float passo = paraBaixo ? 90.0f : 0.0f;
        double alcance = paraBaixo ? 1.0 : this.alcance;

        float f1 = net.minecraft.util.Mth.cos(-rumo * ((float) Math.PI / 180.0f) - (float) Math.PI);
        float f2 = net.minecraft.util.Mth.sin(-rumo * ((float) Math.PI / 180.0f) - (float) Math.PI);
        float f3 = -net.minecraft.util.Mth.cos(-passo * ((float) Math.PI / 180.0f));
        float f4 = net.minecraft.util.Mth.sin(-passo * ((float) Math.PI / 180.0f));

        Vec3 daqui = new Vec3(this.goblin.getX(), this.goblin.getEyeY(), this.goblin.getZ());
        Vec3 até = daqui.add(f2 * f3 * alcance, f4 * alcance, f1 * f3 * alcance);

        var bateu = this.goblin.level().clip(new net.minecraft.world.level.ClipContext(daqui, até,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, this.goblin));
        if (bateu.getType() != HitResult.Type.BLOCK) return null;
        if (!(bateu instanceof BlockHitResult emCheio)) return null;

        BlockPos lugar = emCheio.getBlockPos();
        return cavável(this.goblin.level(), lugar) ? lugar : null;
    }

    /**
     * O que um goblin cava: pedra, areia, terra, barro e gravilha — e nada que seja indestrutível.
     *
     * <p>É a lista de <b>materiais</b> do original, dita com as etiquetas de hoje: lá era
     * {@code Material.rock}, {@code sand}, {@code grass}, {@code clay} e {@code ground}.
     */
    public static boolean cavável(net.minecraft.world.level.Level level, BlockPos onde) {
        BlockState qual = level.getBlockState(onde);
        if (qual.isAir()) return false;
        if (qual.getDestroySpeed(level, onde) < 0.0f) return false;
        if (!qual.getFluidState().isEmpty()) return false;
        return qual.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE)
                || qual.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL)
                || qual.is(net.minecraft.tags.BlockTags.DIRT)
                || qual.is(net.minecraft.tags.BlockTags.SAND);
    }

    /** Tira o bloco, com o que cai dele: o {@code tryHarvestBlock} do original. */
    public static boolean cava(ServerLevel level, BlockPos onde, GoblinEntity quem) {
        if (!cavável(level, onde)) return false;
        return level.destroyBlock(onde, true, quem);
    }
}
