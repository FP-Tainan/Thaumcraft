package net.thaumcraft.occulta.demon;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Coração de Demônio</b> posto no chão: a {@code BlockDemonHeart} do Witchery.
 *
 * <p>Ele <b>bate</b>. De vinte e cinco em vinte e cinco batidas o jogo toca uma batida de coração no lugar
 * onde ele está, e quem passar por uma casa com um coração de demônio no canto ouve a casa a pulsar antes de
 * ver por quê.
 *
 * <p>Fora isso, ele brilha de leve e larga uma faísca de fogo e um fio de fumaça uma vez em dez.
 *
 * <h2>Para que serve</h2>
 *
 * <ul>
 *   <li>é <b>fonte de poder do Altar</b>, e a mais forte que existe: <b>quarenta</b> de poder cada, contando
 *       até <b>dois</b>. Um altar com dois corações ganha oitenta — mais do que oitenta blocos de grama;</li>
 *   <li>e no <b>Mundo dos Espíritos</b> ele torna o pesadelo <b>demoníaco</b>, que é a coisa mais perigosa
 *       que aquele lugar tem.</li>
 * </ul>
 *
 * <p>É o único bloco do ofício que se <b>come</b>: quebrado, ele volta a ser o item, e o item come-se.
 */
public class DemonHeartBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<DemonHeartBlock> CODEC = simpleCodec(DemonHeartBlock::new);

    /** A batida, de vinte e cinco em vinte e cinco. */
    public static final int BATE_DE = 25;

    /** E a luz que ele dá: pouca, mas há. */
    public static final int LUZ = 3;

    private static final VoxelShape FORMA = Block.box(4.0, 0.0, 4.0, 12.0, 13.0, 12.0);

    public DemonHeartBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext onde) {
        return this.defaultBlockState().setValue(FACING, onde.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos onde,
                                  CollisionContext quem) {
        return FORMA;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    /** A faísca e a fumaça, uma vez em dez. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos onde, RandomSource sorte) {
        if (sorte.nextInt(10) != 0) return;
        double x = onde.getX() + 0.35 + 0.3 * sorte.nextDouble();
        double z = onde.getZ() + 0.35 + 0.3 * sorte.nextDouble();
        level.addParticle(ParticleTypes.FLAME, x, onde.getY() + 0.8, z, 0.0, 0.0, 0.0);
        level.addParticle(ParticleTypes.SMOKE, x, onde.getY() + 0.8, z, 0.0, 0.0, 0.0);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState state) {
        return new DemonHeartBlockEntity(onde, state);
    }

    /**
     * A batida só se ouve do lado de cá.
     *
     * <p>No original é a alma do bloco que toca o som, e só no cliente — um som que o servidor mandasse
     * chegaria a toda a gente de uma vez e perderia o que ele tem de bom, que é <b>vir daquele canto</b>.
     */
    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                            BlockEntityType<T> tipo) {
        if (level instanceof net.minecraft.server.level.ServerLevel) return null;
        return (mundo, pos, qual, alma) -> {
            if (alma instanceof DemonHeartBlockEntity coração) coração.bate();
        };
    }
}
