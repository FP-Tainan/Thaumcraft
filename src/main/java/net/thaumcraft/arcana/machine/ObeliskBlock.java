package net.thaumcraft.arcana.machine;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.arcana.ArcanaBlocks;
import net.thaumcraft.arcana.ArcanaItems;

/**
 * O <b>Obelisco</b>: o {@code BlockEssenceGenerator} com tipo zero, da 1.4.0.009.
 *
 * <p>Uma agulha de pedra de <b>dois blocos</b> de altura num lugar só, que <b>acende</b> — o original
 * dá-lhe luz 0,73, que arredonda para onze. Ele vira-se para onde quem o põe está virado.
 *
 * <p>Tocar nele abre a casa do combustível. Com a <b>Chave de Cristal</b> na mão, não: a chave está a
 * ligar.
 */
public class ObeliskBlock extends BaseEntityBlock {
    public static final MapCodec<ObeliskBlock> CODEC = simpleCodec(ObeliskBlock::new);

    public static final net.minecraft.world.level.block.state.properties.EnumProperty<Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    /** Um bloco de largura e <b>dois</b> de altura, como o {@code setBlockBounds} do original. */
    private static final VoxelShape FORMA = Block.box(0.0, 0.0, 0.0, 16.0, 32.0, 16.0);

    public ObeliskBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** Vira-se para onde quem o põe <b>está virado</b>, e não ao contrário. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext contexto) {
        return this.defaultBlockState().setValue(FACING, contexto.getHorizontalDirection());
    }

    /**
     * O giro que o desenhista aplica, em graus.
     *
     * <p>É a tabela do {@code onBlockPlacedBy} do original, lida de trás para a frente: quem o põe virado
     * a norte guarda o metadado 4 e o desenhista gira 360 graus; a oeste, o metadado 1 e 90 graus; a sul,
     * o 2 e 180; a leste, o 3 e 270.
     */
    public static int angle(BlockState feitio) {
        return switch (feitio.getValue(FACING)) {
            case WEST -> 90;
            case SOUTH -> 180;
            case EAST -> 270;
            default -> 0;
        };
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter nível, BlockPos onde, CollisionContext quem) {
        return FORMA;
    }

    /** O corpo dele vem de um modelo que não é uma caixa: desenha-o o desenhista do bloco-entidade. */
    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new ObeliskBlockEntity(onde, feitio);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level nível, BlockState feitio, BlockEntityType<T> tipo) {
        return createTickerHelper(tipo, ArcanaBlocks.OBELISK_ENTITY,
                (mundo, onde, estado, obelisco) -> {
                    if (mundo instanceof net.minecraft.server.level.ServerLevel servidor) obelisco.serverTick(servidor);
                    else ObeliskBlockEntity.clientTick(obelisco);
                });
    }

    @Override
    protected InteractionResult useItemOn(ItemStack pilha, BlockState feitio, Level nível, BlockPos onde,
                                          Player quem, InteractionHand mão, BlockHitResult toque) {
        if (pilha.is(ArcanaItems.CRYSTAL_WRENCH)) return InteractionResult.PASS;
        return super.useItemOn(pilha, feitio, nível, onde, quem, mão, toque);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState feitio, Level nível, BlockPos onde, Player quem, BlockHitResult toque) {
        if (nível.isClientSide()) return InteractionResult.SUCCESS;
        if (nível.getBlockEntity(onde) instanceof ObeliskBlockEntity obelisco) quem.openMenu(obelisco);
        return InteractionResult.CONSUME;
    }

    /** Partido, larga o que tinha dentro. */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState feitio, net.minecraft.server.level.ServerLevel nível,
                                               BlockPos onde, boolean moveu) {
        if (nível.getBlockEntity(onde) instanceof ObeliskBlockEntity obelisco) {
            net.minecraft.world.Containers.dropContents(nível, onde, obelisco);
        }
        super.affectNeighborsAfterRemoval(feitio, nível, onde, moveu);
    }
}
