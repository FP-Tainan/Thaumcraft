package net.thaumcraft.occulta.vampire;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * <b>O Caixão</b>: a {@code BlockCoffin} do Witchery.
 *
 * <p>É uma cama — o jogo trata-o como cama, ele guarda o ponto de renascer como uma cama, e no Nether
 * <b>estoura</b> como uma cama. Mas é uma cama com <b>tampa</b>, e a tampa muda tudo.
 *
 * <h2>A tampa</h2>
 *
 * <p><b>Agachado</b>, o clique abre e fecha a tampa, e abre e fecha as duas metades ao mesmo tempo. De
 * tampa <b>fechada</b> ele é um bloco inteiro — não se dorme nele, e quem tentar ouve que o caixão está
 * fechado. De tampa <b>aberta</b> é uma banheira baixa, e dorme-se.
 *
 * <p>E ela <b>não abre</b> se houver coisa sólida por cima de qualquer das metades, como um baú não abre
 * debaixo de um bloco. Um caixão enterrado fica enterrado.
 *
 * <h2>Por que ele existe</h2>
 *
 * <p>Para quem não é vampiro, é uma cama feia. Para um vampiro, é a <b>casa</b> — e é a última coisa que o
 * {@linkplain VampireLadder décimo degrau} pede: para fazer outro vampiro, é preciso um caixão ao lado da
 * presa. O que o fez a ele foi um cálice de sangue dado por alguém ao pé de um caixão, longe do olhar do
 * sol, e a última página do livro acaba exatamente aí.
 *
 * <p><b>Declarado:</b> no original o caixão ainda <b>deixa dormir de dia</b>, que é a coisa de que um
 * vampiro mais precisa. Isso mora no relógio do sono e não no bloco, e vem na fatia em que a Rosa de Sangue
 * e a Guirlanda de Alho vierem; por agora ele dorme como qualquer cama dorme.
 */
public class CoffinBlock extends BedBlock implements EntityBlock {
    public static final MapCodec<CoffinBlock> CODEC = simpleCodec(CoffinBlock::new);

    /** Se a tampa está aberta. */
    public static final BooleanProperty ABERTO = BooleanProperty.create("aberto");

    /** A banheira de tampa aberta: sete de altura, que é o que o modelo mede. */
    private static final VoxelShape ABERTA = Block.box(0.0, 0.0, 0.0, 16.0, 7.0, 16.0);

    /** E fechado ele é um bloco inteiro, como no original. */
    private static final VoxelShape FECHADA = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public CoffinBlock(Properties properties) {
        super(DyeColor.BLACK, properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, net.minecraft.core.Direction.NORTH)
                .setValue(PART, BedPart.FOOT)
                .setValue(OCCUPIED, false)
                .setValue(ABERTO, false));
    }

    @Override
    public MapCodec<BedBlock> codec() {
        @SuppressWarnings("unchecked")
        MapCodec<BedBlock> meu = (MapCodec<BedBlock>) (MapCodec<?>) CODEC;
        return meu;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ABERTO);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext onde) {
        BlockState posto = super.getStateForPlacement(onde);
        return posto == null ? null : posto.setValue(ABERTO, false);
    }

    /** Ele se desenha sozinho: o modelo vem do {@link net.thaumcraft.occulta.client.CoffinRenderer}. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos onde,
                                  CollisionContext quem) {
        return state.getValue(ABERTO) ? ABERTA : FECHADA;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos onde,
                                           CollisionContext quem) {
        return this.getShape(state, level, onde, quem);
    }

    /**
     * O clique: agachado abre a tampa, de pé deita-se dentro.
     *
     * <p>De tampa fechada não há sono nenhum, e é a única coisa que o caixão diz por palavras.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos onde,
                                               Player quem, BlockHitResult bateu) {
        if (quem.isShiftKeyDown()) {
            if (!(level instanceof net.minecraft.server.level.ServerLevel mundo)) {
                return InteractionResult.SUCCESS;
            }
            viraATampa(mundo, onde, state);
            return InteractionResult.SUCCESS;
        }

        if (!state.getValue(ABERTO)) {
            if (level instanceof net.minecraft.server.level.ServerLevel) {
                quem.sendSystemMessage(Component.translatable("tc.coffin.closed")
                        .withStyle(ChatFormatting.GRAY));
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, onde, quem, bateu);
    }

    /**
     * Abre ou fecha as <b>duas</b> metades, e só se houver céu por cima das duas.
     *
     * <p>É o que o original faz com o {@code isSideSolid} em cada uma: uma tampa não se levanta contra um
     * bloco.
     */
    public static void viraATampa(net.minecraft.server.level.ServerLevel level, BlockPos onde,
                                  BlockState state) {
        BlockPos outra = onde.relative(BedBlock.getConnectedDirection(state));
        BlockState doOutro = level.getBlockState(outra);
        boolean duas = doOutro.getBlock() instanceof CoffinBlock;

        boolean vai = !state.getValue(ABERTO);
        if (vai && (!level.isEmptyBlock(onde.above())
                || (duas && !level.isEmptyBlock(outra.above())))) {
            return;
        }

        level.setBlock(onde, state.setValue(ABERTO, vai), 3);
        if (duas) level.setBlock(outra, doOutro.setValue(ABERTO, vai), 3);
        level.playSound(null, onde, vai ? SoundEvents.WOODEN_TRAPDOOR_OPEN : SoundEvents.WOODEN_TRAPDOOR_CLOSE,
                SoundSource.BLOCKS, 0.7f, 0.5f);
    }

    // ------------------------------------------------------------------ e a alma, que só serve para desenhar

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState state) {
        return new CoffinBlockEntity(onde, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                            BlockEntityType<T> tipo) {
        if (level instanceof net.minecraft.server.level.ServerLevel) return null;
        return (mundo, pos, qual, alma) -> {
            if (alma instanceof CoffinBlockEntity caixão) caixão.anda();
        };
    }
}
