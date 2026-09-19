package net.thaumcraft.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.entity.FallingTaintEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEffects;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.world.TCBiomes;

/**
 * A mácula: o {@code BlockTaint} da 4.2.3.5 — a <strong>crosta</strong> (número 0), o <strong>solo maculado</strong>
 * (1) e o <strong>bloco de carne</strong> (2, que não faz nada disso).
 *
 * <p>A cada tique ao acaso ela tenta maculear o bioma em volta (com dois vizinhos maculados, uma vez em mil), a crosta
 * cai se não tem nada embaixo (ou escorrega de lado de uma coluna de crosta), e um bloco sorteado perto dela, dentro do
 * bioma maculado, ganha fibras. Dentro do bioma, a crosta com ar em cima às vezes vira um enxameador de esporos, e a
 * crosta cercada de mácula vira gosma de fluxo; fora dele, a crosta vira gosma e o solo vira terra. Quem anda por cima
 * às vezes pega o fluxo da mácula.
 */
public class TaintBlock extends Block {
    public static final MapCodec<TaintBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("kind").forGetter(block -> block.kind), propertiesCodec()).apply(instance, TaintBlock::new));
    public static final int CRUST = 0, SOIL = 1, FLESH = 2;
    /** O número (o metadado do original): 0 a crosta, 1 o solo, 2 a carne. */
    public final int kind;
    /** O {@code taint_spread_rate} do original. */
    public static final int SPREAD_RATE = 200;

    public TaintBlock(int kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    public boolean crust(BlockState state) {
        return this.kind == CRUST;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.kind == FLESH) return;
        TaintFibreBlock.taintBiomeSpread(level, pos, random, this);
        boolean crust = this.crust(state);
        if (crust) {
            if (this.tryToFall(level, pos, pos)) return;
            if (level.isEmptyBlock(pos.above())) {
                boolean doIt = true;
                Direction dir = Direction.from2DDataValue(random.nextInt(4));
                for (int a = 0; a < 4; a++) {
                    if (!level.isEmptyBlock(pos.offset(dir.getStepX(), -a, dir.getStepZ()))) {
                        doIt = false;
                        break;
                    }
                    if (!(level.getBlockState(pos.below(a)).getBlock() instanceof TaintBlock)) { // o mesmo bloco (qualquer número)
                        doIt = false;
                        break;
                    }
                }
                if (doIt && this.tryToFall(level, pos, pos.relative(dir))) return;
            }
        }
        BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
        if (level.getBiome(target).is(TCBiomes.TAINTED_LAND)) {
            TaintFibreBlock.spreadFibres(level, target);
            if (crust) {
                if (level.isEmptyBlock(pos.above()) && random.nextInt(200) == 0) {
                    if (sporeSwarmer.test(level, pos)) {
                        level.removeBlock(pos, false);
                    }
                } else {
                    boolean doIt = level.getBlockState(pos.above()).getBlock() instanceof TaintBlock;
                    if (doIt) {
                        for (Direction dir : Direction.Plane.HORIZONTAL) {
                            if (!(level.getBlockState(pos.relative(dir)).getBlock() instanceof TaintBlock)) {
                                doIt = false;
                                break;
                            }
                        }
                    }
                    if (doIt) level.setBlockAndUpdate(pos, TCBlocks.FLUX_GOO.defaultBlockState());
                }
            }
        } else if (crust && random.nextInt(20) == 0) {
            level.setBlockAndUpdate(pos, TCBlocks.FLUX_GOO.defaultBlockState());
        } else if (!crust && random.nextInt(10) == 0) {
            level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        }
    }

    /**
     * O enxameador de esporos que a crosta solta (as criaturas da mácula ligam isto): devolve se nasceu — e então a
     * crosta some. Só nasce se não houver outro a dezesseis blocos.
     */
    public static java.util.function.BiPredicate<ServerLevel, BlockPos> sporeSwarmer = (level, pos) -> false;

    /** O {@code canFallBelow}: sem tronco por perto, cai no ar, no fogo, nas fibras, no que se substitui e em fluido. */
    public static boolean canFallBelow(LevelReader level, BlockPos pos) {
        for (int xx = -1; xx <= 1; xx++) for (int zz = -1; zz <= 1; zz++) for (int yy = -1; yy <= 1; yy++) {
            if (level.getBlockState(pos.offset(xx, yy, zz)).is(BlockTags.LOGS)) return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return true;
        if (state.is(TCBlocks.FLUX_GOO) && state.getValue(FluxBlock.LEVEL) >= 4) return false;
        if (state.is(Blocks.FIRE) || state.is(TCBlocks.TAINT_FIBRES)) return true;
        if (state.canBeReplaced()) return true;
        return !state.getFluidState().isEmpty();
    }

    /** O {@code tryToFall}: vira um bloco caindo a partir de {@code dest}, se ali embaixo dá para cair. */
    private boolean tryToFall(ServerLevel level, BlockPos pos, BlockPos dest) {
        if (!canFallBelow(level, dest.below()) || dest.getY() < level.getMinY()) return false;
        FallingTaintEntity falling = new FallingTaintEntity(level, dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5,
                level.getBlockState(pos), pos);
        level.addFreshEntity(falling);
        return true;
    }

    /** O {@code onEntityWalking}: o fluxo da mácula, raramente no jogador (1 em 100) e mais nos bichos (1 em 20). */
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (this.kind != FLESH && !level.isClientSide() && entity instanceof LivingEntity living && !living.isInvertedHealAndHarm()) {
            if (entity instanceof Player) {
                if (level.getRandom().nextInt(100) == 0) living.addEffect(new MobEffectInstance(TCEffects.FLUX_TAINT, 80, 0));
            } else if (level.getRandom().nextInt(20) == 0) {
                living.addEffect(new MobEffectInstance(TCEffects.FLUX_TAINT, 160, 0));
            }
        }
        if (this.kind != FLESH) super.stepOn(level, pos, state, entity);
    }

    /** O evento 1: o som de raízes de quando a mácula cresce. */
    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id == 1) {
            if (level.isClientSide()) {
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), TCSounds.ROOTS.value(), SoundSource.BLOCKS, 0.1f,
                        0.9f + level.getRandom().nextFloat() * 0.2f, false);
            }
            return true;
        }
        return super.triggerEvent(state, level, pos, id, param);
    }

    /** O {@code randomDisplayTick}: a crosta pingando por baixo, com ar embaixo. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (this.crust(state) && level.isEmptyBlock(pos.below()) && random.nextInt(10) == 0) {
            clientDrip.accept(level, pos.getX() + 0.1 + random.nextFloat() * 0.8, pos.getY(), pos.getZ() + 0.1 + random.nextFloat() * 0.8);
        }
    }

    public interface Drip {
        void accept(Level level, double x, double y, double z);
    }

    public static Drip clientDrip = (level, x, y, z) -> {
    };

    /** Quantos dos seis vizinhos já são mácula (crosta, solo, carne ou fibra): o {@code getAdjacentTaint}. */
    public static int adjacentTaint(LevelReader level, BlockPos pos) {
        int count = 0;
        for (Direction dir : Direction.values()) {
            if (isTaint(level.getBlockState(pos.relative(dir)))) count++;
        }
        return count;
    }

    /** Este bloco é mácula (o {@code blockTaint} ou o {@code blockTaintFibres} do original)? */
    public static boolean isTaint(BlockState state) {
        return state.getBlock() instanceof TaintBlock || state.is(TCBlocks.TAINT_FIBRES);
    }
}
