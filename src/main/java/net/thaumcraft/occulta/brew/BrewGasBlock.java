package net.thaumcraft.occulta.brew;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

/**
 * A nuvem de cozimento: o {@code BlockBrewGas} do Witchery.
 *
 * <p>Ela nasce onde o frasco arrebentou e <b>cresce</b> de cinco em cinco batidas, para os lados e para cima e
 * para baixo, com a chance que o original dá a cada direção — pouca para cima, mais para os lados. Cada passo
 * que ela dá conta um a mais no estágio dela, e ela para no alcance que o cozimento lhe deu.
 *
 * <p>Chegada ao alcance, começa a morrer: a cada batida há a chance de um sobre a duração de ela sumir. E ao fim
 * de cento e vinte batidas some de qualquer jeito, cheia ou não.
 *
 * <p>Quem passa dentro dela apanha o cozimento de vez em quando — uma vez em dez —, e apanha <b>fraco</b>: um
 * quarto da força e metade do tempo. Uma nuvem não é um frasco na cara.
 */
public class BrewGasBlock extends BaseEntityBlock {
    public static final MapCodec<BrewGasBlock> CODEC = simpleCodec(BrewGasBlock::new);

    /** O estágio em que a nuvem está, que no original é o metadata: quanto maior, mais longe ela já foi. */
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 10);

    /** De quantas em quantas batidas ela cresce. */
    public static final int SPREAD_EVERY = 5;

    /** A chance de crescer para cada lado: cima, baixo e os quatro do meio — a do original, na ordem dele. */
    private static final double[] CHANCE = {0.2, 0.4, 0.8, 0.8, 0.8, 0.8};
    private static final Direction[] WAY = {Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST,
            Direction.SOUTH, Direction.NORTH};

    /** Uma vez em dez, quem está dentro apanha — e apanha fraco. */
    public static final int TOUCH_CHANCE = 10;
    public static final double TOUCH_POWER = 0.25;
    public static final double TOUCH_DURATION = 0.5;

    public BrewGasBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0));
    }

    @Override
    public MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrewFluidBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /** Não se pisa nela nem se bate nela: é ar com cor. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return Shapes.empty();
    }

    /**
     * A caixa dela é a do bloco inteiro, ainda que não se esbarre nela: é assim que o jogo sabe que alguém está
     * <b>dentro</b> dela — o mesmo que o portal do Nether faz.
     */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        level.scheduleTick(pos, this, SPREAD_EVERY);
    }

    /** O {@code updateTick}: a nuvem cresce, e depois morre. */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource sorte) {
        if (!(level.getBlockEntity(pos) instanceof BrewFluidBlockEntity nuvem)) {
            level.removeBlock(pos, false);
            return;
        }
        if (nuvem.tickRun() > BrewFluidBlockEntity.MAX_RUN_TICKS) {
            level.removeBlock(pos, false);
            return;
        }

        int estágio = state.getValue(STAGE);
        int alcance = Math.min(nuvem.expansion(), 10);
        if (estágio >= alcance) {
            // cheia: daqui em diante é só esperar acabar
            if (nuvem.duration() == 0 || sorte.nextInt(nuvem.duration()) == 0) {
                level.removeBlock(pos, false);
                return;
            }
        } else {
            boolean cresceu = false;
            int passo = estágio;
            for (int i = 0; i < CHANCE.length && passo < alcance; i++) {
                if (sorte.nextDouble() >= CHANCE[i]) continue;
                BlockPos lado = pos.relative(WAY[i]);
                BlockState oQueTem = level.getBlockState(lado);
                if (!oQueTem.isAir() && !oQueTem.is(Blocks.SNOW)) continue;
                level.setBlock(lado, this.defaultBlockState().setValue(STAGE, Math.min(passo + 1, alcance)),
                        Block.UPDATE_ALL);
                if (level.getBlockEntity(lado) instanceof BrewFluidBlockEntity nova) nova.copyFrom(nuvem);
                cresceu = true;
            }
            if (cresceu) {
                level.setBlock(pos, state.setValue(STAGE, Math.min(estágio + 1, alcance)), Block.UPDATE_ALL);
            }
        }
        level.scheduleTick(pos, this, SPREAD_EVERY);
    }

    /** O {@code onEntityCollidedWithBlock}: quem passa dentro apanha, de vez em quando e fraco. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity quem,
                                InsideBlockEffectApplier applier, boolean dentroMesmo) {
        if (!(level instanceof ServerLevel server) || !(quem instanceof LivingEntity vivo)) return;
        if (server.getRandom().nextInt(TOUCH_CHANCE) != 4) return;
        if (!(level.getBlockEntity(pos) instanceof BrewFluidBlockEntity nuvem)) return;
        if (nuvem.contents().isEmpty()) return;
        BrewModifiers temperos = new BrewModifiers(TOUCH_POWER, TOUCH_DURATION);
        temperos.protectedFromBadEffects = vivo.hasEffect(net.thaumcraft.occulta.OccultaEffects.GAS_MASK);
        Brew.apply(level, vivo, nuvem.contents(), temperos);
    }

    /** Onde uma nuvem cabe: no ar, ou onde só há neve. */
    public static boolean fits(Level level, BlockPos onde) {
        BlockState oQueTem = level.getBlockState(onde);
        return oQueTem.isAir() || oQueTem.is(Blocks.SNOW) || oQueTem.canBeReplaced();
    }

    /** O ponto onde a nuvem nasce, a partir de onde o frasco bateu. */
    public static BlockPos where(net.minecraft.world.phys.HitResult onde) {
        if (onde instanceof net.minecraft.world.phys.BlockHitResult bateu) {
            return bateu.getBlockPos().relative(bateu.getDirection());
        }
        Vec3 meio = onde.getLocation();
        return BlockPos.containing(meio);
    }
}
