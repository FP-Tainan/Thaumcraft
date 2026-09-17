package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.entity.AlembicBlockEntity;
import net.thaumcraft.registry.TCResources;
import net.thaumcraft.registry.TCSounds;

/**
 * O alambique arcano: o pote de latão que se empilha sobre o forno alquímico.
 *
 * <p>Um toque diz em voz baixa o quanto ele tem dentro, como no original faz ao bater com a varinha. Com
 * um rótulo na mão, ele passa a pedir só aquele aspecto ao forno.
 */
public class AlembicBlock extends BaseEntityBlock {
    public static final MapCodec<AlembicBlock> CODEC = simpleCodec(AlembicBlock::new);
    /**
     * Se ele está apoiado noutro alambique.
     *
     * <p>Apoiado, ele termina no colarinho de latão que encaixa no de baixo; no chão ou sobre o forno,
     * abre quatro pés inclinados. É o que se vê na coluna do original: só o de baixo tem pés.
     */
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty STACKED =
            net.minecraft.world.level.block.state.properties.BooleanProperty.create("stacked");

    private static final VoxelShape SHAPE = Block.box(0.5, 0.0, 0.5, 15.5, 16.0, 15.5);

    public AlembicBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STACKED, false));
    }

    @Override
    protected void createBlockStateDefinition(
            net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STACKED);
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return this.defaultBlockState().setValue(STACKED,
                standsOnAlembic(context.getLevel(), context.getClickedPos()));
    }

    @Override
    protected BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level,
                                     net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos pos,
                                     net.minecraft.core.Direction dir, BlockPos neighbourPos,
                                     BlockState neighbourState, net.minecraft.util.RandomSource random) {
        if (dir != net.minecraft.core.Direction.DOWN) return state;
        return state.setValue(STACKED, standsOnAlembic(level, pos));
    }

    private static boolean standsOnAlembic(net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos.below()).getBlock() instanceof AlembicBlock;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /**
     * Quem desenha o alambique no mundo e o {@link net.thaumcraft.client.render.AlembicRenderer}, e nao um
     * arquivo de modelo.
     *
     * <p>O motivo sao os pes: no original eles se abrem em diagonal, e uma peca de modelo do Minecraft so
     * gira em torno de um eixo -- um pe de canto precisaria de dois. O modelo continua existindo, mas so
     * para o item na mao e no inventario.
     */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof AlembicBlockEntity alembic)) return InteractionResult.PASS;

        // agachado, arranca o rótulo
        if (player.isShiftKeyDown() && alembic.label() != null) {
            if (!level.isClientSide()) {
                alembic.setLabel(null);
                if (!player.getAbilities().instabuild) {
                    player.getInventory().placeItemBackInInventory(new ItemStack(TCResources.get("jar_label")));
                }
                level.playSound(null, pos, TCSounds.JAR.value(), SoundSource.BLOCKS, 0.6f, 1.2f);
            }
            return InteractionResult.SUCCESS;
        }

        // com um rótulo na mão, prende nele o aspecto do que já estiver dentro
        if (stack.is(TCResources.get("jar_label")) && alembic.label() == null && alembic.aspect() != null) {
            if (!level.isClientSide()) {
                alembic.setLabel(alembic.aspect());
                if (!player.getAbilities().instabuild) stack.shrink(1);
                level.playSound(null, pos, TCSounds.JAR.value(), SoundSource.BLOCKS, 0.6f, 1.0f);
            }
            return InteractionResult.SUCCESS;
        }

        // um toque simples diz quanto tem dentro, como no original
        if (level.isClientSide()) {
            Aspect held = alembic.aspect();
            if (held == null || alembic.amount() == 0) {
                player.sendOverlayMessage(Component.translatable("tc.alembic.empty"));
            } else {
                player.sendOverlayMessage(Component.translatable("tc.alembic.holds",
                        held.name(), alembic.amount(), AlembicBlockEntity.CAPACITY));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlembicBlockEntity(pos, state);
    }
}
