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
    protected net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
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
