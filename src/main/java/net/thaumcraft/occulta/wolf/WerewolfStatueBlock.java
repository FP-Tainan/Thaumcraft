package net.thaumcraft.occulta.wolf;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Estátua do Lobisomem</b>: a {@code BlockStatueWerewolf} do Witchery.
 *
 * <p>Um senhor de cabeça de lobo, de lança na mão, num pedestal de três lajes, com <b>dois lobos</b> aos pés.
 * É a coisa mais bonita que o mod tem, e é também a mais útil: quem fala com ela sobe a
 * {@linkplain WerewolfLadder escada dos dez graus}.
 *
 * <p>Ela mede <b>dois blocos de altura</b> numa casa só — é o que o original faz, com a caixa dele a passar o
 * bloco —, e <b>olha para quem a assenta</b>, porque é por ali que ela larga o que dá.
 *
 * <p>Aguenta pancada como pedra e <b>mil de estouro</b>: não se tira um lugar de culto com um creeper.
 */
public class WerewolfStatueBlock extends BaseEntityBlock {
    public static final MapCodec<WerewolfStatueBlock> CODEC = simpleCodec(WerewolfStatueBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** Dois blocos de altura, como no original. */
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 32.0, 16.0);

    public WerewolfStatueBlock(Properties properties) {
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

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                  CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new WerewolfStatueBlockEntity(onde, feitio);
    }

    /**
     * Falar com ela <b>com coisa na mão</b>: é por aqui que passam os três primeiros degraus.
     *
     * <p>E <b>não gasta o uso do item</b>: quem chega com ouro na mão não o atira ao chão nem o come — a
     * estátua é que tira dele o que quer.
     */
    @Override
    protected InteractionResult useItemOn(ItemStack mão, BlockState feitio, Level level, BlockPos onde,
                                          Player quem, InteractionHand qualMão, BlockHitResult onde2) {
        return this.fala(level, onde, quem, mão, feitio);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState feitio, Level level, BlockPos onde, Player quem,
                                               BlockHitResult hit) {
        return this.fala(level, onde, quem, ItemStack.EMPTY, feitio);
    }

    private InteractionResult fala(Level level, BlockPos onde, Player quem, ItemStack mão, BlockState feitio) {
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        WerewolfLadder.fala(server, quem, mão, onde, feitio.getValue(FACING));
        return InteractionResult.SUCCESS;
    }
}
