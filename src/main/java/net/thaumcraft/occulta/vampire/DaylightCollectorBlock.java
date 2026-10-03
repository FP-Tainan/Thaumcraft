package net.thaumcraft.occulta.vampire;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * <b>O Coletor de Luz</b>: a {@code BlockDaylightCollector} do Witchery.
 *
 * <p>É uma garra de ferro onde se encaixa uma <b>Esfera de Quartzo</b> vazia, e a esfera enche-se de
 * <b>sol</b> — mas não enche sozinha. Ela enche-se do que os <b>Sensores de Luz Solar</b> encostados a ela
 * lhe disserem, e eles dizem um número que sobe com o sol: um ao raiar, quinze ao meio-dia.
 *
 * <h2>Como ele enche, e por que isso é lindo</h2>
 *
 * <p>Cada vez que um sensor ao lado marca <b>exatamente um a mais</b> do que a esfera já tem, ela sobe um.
 * Não serve pôr a esfera ao meio-dia e esperar: ela sobe de <b>um em um</b>, e cada degrau precisa de o sol
 * estar num ponto diferente do céu. <b>Encher a esfera é ver um dia inteiro nascer.</b>
 *
 * <p>Cheia — quinze —, o clique dá uma {@linkplain SunGrenadeItem Granada Solar}. Antes disso, o clique
 * devolve a esfera como estava e recomeça: <b>não há meio-sol</b>.
 *
 * <p>E então repare no que o mod fez. Para subir o quinto degrau, um vampiro tem de <b>passar a manhã
 * acordado ao lado de um sensor de sol</b>, a ver o céu que o mata encher um vidro. E tem de o fazer dez
 * vezes. Não há nenhuma linha de texto em lado nenhum do jogo a explicar isto.
 */
public class DaylightCollectorBlock extends Block {
    public static final MapCodec<DaylightCollectorBlock> CODEC = simpleCodec(DaylightCollectorBlock::new);

    /** Quanto sol a esfera já tem: zero é sem esfera, quinze é cheia. */
    public static final IntegerProperty SOL = IntegerProperty.create("sol", 0, 15);

    /** O número em que ela está cheia. */
    public static final int CHEIA = 15;

    private static final VoxelShape FORMA = Block.box(3.0, 0.0, 3.0, 13.0, 13.0, 13.0);

    public DaylightCollectorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SOL, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SOL);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return FORMA;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    /**
     * <b>O sol sobe de um em um.</b>
     *
     * <p>Um sensor encostado que marque exatamente um a mais do que a esfera tem dá-lhe esse um. Marcando
     * mais, não dá nada: o salto não conta. É o que obriga a esfera a <b>acompanhar</b> o céu em vez de o
     * apanhar no fim.
     */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos onde, Block quem,
                                   @Nullable Orientation rumo, boolean mover) {
        if (!(level instanceof ServerLevel mundo)) return;
        int sol = state.getValue(SOL);
        if (sol <= 0 || sol >= CHEIA) return;

        for (Direction lado : Direction.Plane.HORIZONTAL) {
            BlockState vizinho = mundo.getBlockState(onde.relative(lado));
            if (!(vizinho.getBlock() instanceof DaylightDetectorBlock)) continue;
            if (vizinho.getValue(DaylightDetectorBlock.POWER) != sol + 1) continue;
            mundo.setBlock(onde, state.setValue(SOL, sol + 1), 3);
            return;
        }
    }

    /**
     * O clique, nos seus três casos: pôr a esfera, tirá-la de volta, ou levar o sol.
     *
     * <p>E não há o quarto caso — guardar o que já se juntou. Quem tirar a esfera a meio <b>perde a manhã</b>.
     */
    @Override
    protected InteractionResult useItemOn(ItemStack naMão, BlockState state, Level level, BlockPos onde,
                                          Player quem, InteractionHand mão, BlockHitResult bateu) {
        if (!(level instanceof ServerLevel mundo)) return InteractionResult.SUCCESS;
        int sol = state.getValue(SOL);

        if (sol == 0) {
            if (!naMão.is(net.thaumcraft.occulta.OccultaItems.QUARTZ_SPHERE)) {
                return InteractionResult.PASS;
            }
            naMão.consume(1, quem);
            mundo.setBlock(onde, state.setValue(SOL, 1), 3);
            mundo.playSound(null, onde, SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, 0.7f, 1.4f);
            return InteractionResult.SUCCESS;
        }

        ItemStack dá = sol >= CHEIA
                ? new ItemStack(net.thaumcraft.occulta.OccultaItems.SUN_GRENADE)
                : new ItemStack(net.thaumcraft.occulta.OccultaItems.QUARTZ_SPHERE);
        if (!quem.getInventory().add(dá)) quem.drop(dá, false);
        mundo.setBlock(onde, state.setValue(SOL, 0), 3);
        mundo.playSound(null, onde, sol >= CHEIA ? SoundEvents.BEACON_ACTIVATE : SoundEvents.ITEM_PICKUP,
                SoundSource.BLOCKS, 0.7f, 1.2f);
        return InteractionResult.SUCCESS;
    }
}
