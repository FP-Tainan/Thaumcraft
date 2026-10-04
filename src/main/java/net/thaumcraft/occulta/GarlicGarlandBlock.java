package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Guirlanda de Alho</b>: a {@code BlockGarlicGarland} do Witchery.
 *
 * <p>Cinco cabeças de alho enfiadas num cordel e penduradas numa parede. É a coisa mais barata do ramo do
 * vampiro — cinco alhos e dois fios — e é a única defesa contra ele que não precisa de ofício nenhum.
 *
 * <h2>O que ela faz</h2>
 *
 * <ul>
 *   <li>um <b>vampiro</b> que encoste nela é <b>empurrado</b>, com o mesmo empurrão do anel de proteção do
 *       ofício — e isso vale tanto para o bicho como para <b>um jogador que seja vampiro</b>;</li>
 *   <li>e um vampiro que <b>bata nela</b> para a arrancar <b>pega fogo</b>, um segundo.</li>
 * </ul>
 *
 * <p>Repare no que isso significa para quem joga de vampiro: a casa de qualquer aldeão com uma guirlanda à
 * porta passa a ser um lugar de onde ele é <b>cuspido para fora</b> sem poder sequer tirar o alho sem se
 * queimar. É a primeira coisa deste mod que torna o jogador <b>indesejado em sua própria aldeia</b>, e ela
 * custa cinco alhos.
 *
 * <p>No <b>criativo</b> ela não faz nada: nem empurra, nem queima.
 */
public class GarlicGarlandBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<GarlicGarlandBlock> CODEC = simpleCodec(GarlicGarlandBlock::new);

    /** Quanto tempo um vampiro arde ao tentar arrancá-la. */
    public static final int ARDE = 1;

    /** O que ela ocupa: uma faixa fina encostada ao alto da parede. */
    private static final VoxelShape NORTE = Block.box(1.0, 12.0, 0.0, 15.0, 16.0, 3.0);
    private static final VoxelShape SUL = Block.box(1.0, 12.0, 13.0, 15.0, 16.0, 16.0);
    private static final VoxelShape OESTE = Block.box(0.0, 12.0, 1.0, 3.0, 16.0, 15.0);
    private static final VoxelShape LESTE = Block.box(13.0, 12.0, 1.0, 16.0, 16.0, 15.0);

    public GarlicGarlandBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** Ela se desenha sozinha: o modelo vem do {@link net.thaumcraft.occulta.client.GarlicGarlandRenderer}. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos onde,
                                  CollisionContext quem) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SUL;
            case WEST -> OESTE;
            case EAST -> LESTE;
            default -> NORTE;
        };
    }

    /** Ela não trava ninguém: quem entra nela é <b>empurrado</b>, e não parado. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos onde,
                                           CollisionContext quem) {
        return net.minecraft.world.phys.shapes.Shapes.empty();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext onde) {
        return this.defaultBlockState().setValue(FACING, onde.getHorizontalDirection().getOpposite());
    }

    /** Ela pende de uma parede, e cai com ela. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos onde) {
        BlockPos parede = onde.relative(state.getValue(FACING));
        return level.getBlockState(parede).isFaceSturdy(level, parede, state.getValue(FACING).getOpposite());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level,
                                     net.minecraft.world.level.ScheduledTickAccess relógio, BlockPos onde,
                                     Direction lado, BlockPos vizinho, BlockState doVizinho,
                                     net.minecraft.util.RandomSource sorte) {
        if (lado == state.getValue(FACING) && !state.canSurvive(level, onde)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, relógio, onde, lado, vizinho, doVizinho, sorte);
    }

    /**
     * <b>Um vampiro não passa.</b>
     *
     * <p>O empurrão é o do {@linkplain net.thaumcraft.occulta.rite.Rites.PushCircle anel de proteção} — a mesma conta,
     * e por isso o mesmo solavanco. O original faz do lado do servidor para o bicho e do lado do cliente
     * para o jogador, para que o empurrão não pareça emperrado a quem o leva; aqui ele vai pelos dois, que é
     * o que o jogo de hoje já sabe fazer sozinho.
     */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos onde, Entity quem,
                                net.minecraft.world.entity.InsideBlockEffectApplier efeitos,
                                boolean dentro) {
        if (!(level instanceof ServerLevel)) return;
        if (quem instanceof Player gente && gente.getAbilities().instabuild) return;
        if (!net.thaumcraft.occulta.vampire.Vampirism.é(
                quem instanceof net.minecraft.world.entity.LivingEntity vivo ? vivo : null)) {
            return;
        }
        net.thaumcraft.occulta.rite.Rites.PushCircle.empurra(quem,
                onde.getX() + 0.5, onde.getY() + 0.5, onde.getZ() + 0.5);
    }

    /**
     * <b>E arrancá-la queima.</b>
     *
     * <p>Um segundo de fogo, que não mata ninguém — mas um vampiro sem sangue arde até morrer com um segundo
     * de fogo, e é isso que torna a guirlanda uma coisa séria para quem já está no fim.
     */
    @Override
    protected void attack(BlockState state, Level level, BlockPos onde, Player quem) {
        if (!(level instanceof ServerLevel)) return;
        if (quem.getAbilities().instabuild) return;
        if (!net.thaumcraft.occulta.vampire.Vampire.é(quem)) return;
        quem.igniteForSeconds(ARDE);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState state) {
        return new GarlicGarlandBlockEntity(onde, state);
    }
}
