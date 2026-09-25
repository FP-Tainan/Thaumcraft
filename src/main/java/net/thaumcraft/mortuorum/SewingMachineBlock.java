package net.thaumcraft.mortuorum;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A Máquina de Costura: o {@code BlockSewing} do Necromancy — onde os pedaços viram peça de corpo.
 */
public class SewingMachineBlock extends BaseEntityBlock {
    public static final MapCodec<SewingMachineBlock> CODEC = simpleCodec(SewingMachineBlock::new);

    /**
     * O tamanho que ela ocupa: o {@code setBlockBounds(0.3F, 0.5F, 0.2F, 0.7F, 0.0F, 0.95F)} do original.
     *
     * <p><b>Desvio declarado:</b> os números são os dele, mas ali o alto e o baixo estão trocados — pede uma caixa
     * que começa em 0,5 de altura e acaba em 0,0 —, e uma caixa virada do avesso não vale nada. Aqui ficam na
     * ordem certa, que é o que ele queria e o que dá o tamanho do que se desenha.
     */
    private static final net.minecraft.world.phys.shapes.VoxelShape SHAPE = box(4.8, 0.0, 3.2, 11.2, 8.0, 15.2);

    public SewingMachineBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SewingMachineBlockEntity(pos, state);
    }

    @Override
    protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,
                                                                  net.minecraft.world.level.BlockGetter level,
                                                                  BlockPos pos,
                                                                  net.minecraft.world.phys.shapes.CollisionContext context) {
        return SHAPE;
    }

    /** O original devolve -1 no {@code getRenderType}: quem a desenha é o desenhista do tile, e não um modelo. */
    @Override
    protected net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.INVISIBLE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof SewingMachineBlockEntity machine) {
            player.openMenu(machine);
        }
        return InteractionResult.SUCCESS;
    }

    /** Quebrada, ela devolve o que estava dentro. */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean moving) {
        if (level.getBlockEntity(pos) instanceof SewingMachineBlockEntity machine) {
            net.minecraft.world.Containers.dropContents(level, pos, machine);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moving);
    }
}
