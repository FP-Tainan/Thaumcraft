package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Caveira do Chamado</b>: a {@code BlockAlluringSkull} do Witchery.
 *
 * <p>Uma caveira de esqueleto que, <b>acordada</b>, <b>puxa os mortos-vivos para si</b> de até sessenta e
 * quatro blocos à volta.
 *
 * <h2>Como ela puxa</h2>
 *
 * <p>Não de uma vez. De <b>cinco em cinco segundos</b> ela acorda <b>um oitavo do mundo</b> à volta dela —
 * um quadrante de sessenta e quatro blocos, acima ou abaixo — e manda andar na direção dela tudo o que ali
 * for morto-vivo. Oito voltas e ela deu a volta ao mundo inteiro; quarenta segundos para um giro completo.
 *
 * <p>Essa roda é o que torna a caveira útil em vez de absurda. Se ela puxasse tudo de uma vez, uma armadilha
 * com uma caveira acesa seria uma panela de zumbis ao fim de meio minuto. Puxando um oitavo de cada vez,
 * eles chegam <b>aos poucos e por um lado</b> — que é como se faz uma armadilha e não um massacre.
 *
 * <h2>A Pedra Necrótica, que acende e apaga</h2>
 *
 * <p>A caveira posta está <b>dormindo</b>, e não faz nada. Com a <b>Pedra Necrótica</b> na mão:
 *
 * <ul>
 *   <li>clicando numa caveira dormindo, ela <b>acorda</b> — chamas e um relincho de cavalo esquelético;</li>
 *   <li>clicando numa acordada, ela <b>estoura</b> e volta para o chão como item.</li>
 * </ul>
 *
 * <p>Não há como apagar uma caveira sem a levantar. É de propósito: acender uma é uma decisão, e desligá-la
 * custa ir lá buscá-la.
 *
 * <p>Ela é <b>inquebrável</b> e aguenta mil de explosão — um creeper ao lado dela não a tira do lugar. Quem
 * faz uma armadilha de mortos-vivos não quer que o primeiro deles a parta.
 */
public class AlluringSkullBlock extends Block implements EntityBlock {
    public static final MapCodec<AlluringSkullBlock> CODEC = simpleCodec(AlluringSkullBlock::new);

    /** Para onde ela está pregada: no chão, ou numa das quatro paredes. */
    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class,
            Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);

    /** E, no chão, para que lado ela olha: as dezesseis do original. */
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 15);

    /** Se ela está acordada. */
    public static final BooleanProperty ACORDADA = BooleanProperty.create("awake");

    /** De quantas em quantas batidas ela muda de quadrante: cinco segundos. */
    public static final int VOLTA = 100;

    /** Quantos quadrantes há. */
    public static final int QUADRANTES = 8;

    /** Até onde ela chama. */
    public static final double ALCANCE = 64.0;

    /** E quão fundo o quadrante é. */
    public static final double FUNDO = 10.0;

    private static final VoxelShape NO_CHÃO = Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0);
    private static final VoxelShape NA_PAREDE_NORTE = Block.box(4.0, 4.0, 8.0, 12.0, 12.0, 16.0);
    private static final VoxelShape NA_PAREDE_SUL = Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 8.0);
    private static final VoxelShape NA_PAREDE_OESTE = Block.box(8.0, 4.0, 4.0, 16.0, 12.0, 12.0);
    private static final VoxelShape NA_PAREDE_LESTE = Block.box(0.0, 4.0, 4.0, 8.0, 12.0, 12.0);

    public AlluringSkullBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP).setValue(ROTATION, 0).setValue(ACORDADA, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> construtor) {
        construtor.add(FACING, ROTATION, ACORDADA);
    }

    /** Ela se desenha sozinha, pelo {@link net.thaumcraft.occulta.client.AlluringSkullRenderer}. */
    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter mundo, BlockPos onde,
                                  CollisionContext quem) {
        return switch (feitio.getValue(FACING)) {
            case NORTH -> NA_PAREDE_NORTE;
            case SOUTH -> NA_PAREDE_SUL;
            case WEST -> NA_PAREDE_OESTE;
            case EAST -> NA_PAREDE_LESTE;
            default -> NO_CHÃO;
        };
    }

    /**
     * Onde ela se prega: no chão, ou na parede em que se bateu.
     *
     * <p>No chão ela guarda <b>para que lado quem a pôs estava olhando</b>, com as dezesseis voltas que o
     * jogo dá a qualquer caveira. Na parede o lado já está decidido pela parede.
     */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext onde) {
        Direction lado = onde.getClickedFace();
        if (lado == Direction.DOWN) return null;
        BlockState feitio = this.defaultBlockState();
        if (lado == Direction.UP) {
            return feitio.setValue(FACING, Direction.UP).setValue(ROTATION,
                    Math.round(onde.getRotation() * 16.0f / 360.0f) & 15);
        }
        return feitio.setValue(FACING, lado);
    }

    /** Ela precisa do bloco a que está pregada. */
    @Override
    protected boolean canSurvive(BlockState feitio, LevelReader mundo, BlockPos onde) {
        Direction para = feitio.getValue(FACING);
        BlockPos atrás = para == Direction.UP ? onde.below() : onde.relative(para.getOpposite());
        return mundo.getBlockState(atrás).isFaceSturdy(mundo, atrás,
                para == Direction.UP ? Direction.UP : para);
    }

    @Override
    protected BlockState updateShape(BlockState feitio, LevelReader mundo, ScheduledTickAccess relógio,
                                     BlockPos onde, Direction lado, BlockPos vizinho, BlockState doVizinho,
                                     RandomSource sorte) {
        if (!feitio.canSurvive(mundo, onde)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(feitio, mundo, relógio, onde, lado, vizinho, doVizinho, sorte);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new AlluringSkullBlockEntity(onde, feitio);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level mundo, BlockState feitio,
                                                                           BlockEntityType<T> tipo) {
        if (mundo.isClientSide()) return null;
        return (level, onde, qual, alma) -> {
            if (alma instanceof AlluringSkullBlockEntity caveira) caveira.bate((ServerLevel) level);
        };
    }

    /**
     * <b>A Pedra Necrótica acende, e volta a tirar.</b>
     *
     * <p>Qualquer outra coisa na mão não faz nada: a caveira é surda a tudo menos à pedra.
     */
    @Override
    protected InteractionResult useItemOn(ItemStack oquê, BlockState feitio, Level mundo, BlockPos onde,
                                          Player quem, net.minecraft.world.InteractionHand mão,
                                          BlockHitResult bateu) {
        if (!oquê.is(OccultaItems.NECROTIC_STONE)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!(mundo instanceof ServerLevel level)) return InteractionResult.SUCCESS;

        if (!feitio.getValue(ACORDADA)) {
            level.setBlockAndUpdate(onde, feitio.setValue(ACORDADA, true));
            level.sendParticles(ParticleTypes.FLAME, onde.getX() + 0.5, onde.getY() + 0.3,
                    onde.getZ() + 0.5, 16, 0.5, 0.5, 0.5, 0.0);
            level.playSound(null, onde, net.minecraft.sounds.SoundEvents.SKELETON_HORSE_DEATH,
                    SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        level.sendParticles(ParticleTypes.EXPLOSION, onde.getX() + 0.5, onde.getY() + 0.3,
                onde.getZ() + 0.5, 16, 0.5, 0.5, 0.5, 0.0);
        level.playSound(null, onde, net.minecraft.sounds.SoundEvents.SKELETON_HORSE_HURT,
                SoundSource.BLOCKS, 1.0f, 1.0f);
        level.removeBlock(onde, false);
        Block.popResource(level, onde.above(), new ItemStack(OccultaItems.ALLURING_SKULL));
        return InteractionResult.SUCCESS;
    }
}
