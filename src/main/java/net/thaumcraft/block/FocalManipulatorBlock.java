package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.thaumcraft.block.entity.FocalManipulatorBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.research.ResearchManager;
import org.jetbrains.annotations.Nullable;

/**
 * O manipulador focal: o número 13 do {@code BlockStoneDevice} da 4.2.3.5 — a mesa do {@code ModelArcaneWorkbench}
 * com a textura {@code wandtable.png}, onde se põem as melhorias nos focos. Só abre para quem tem a pesquisa
 * FOCALMANIPULATION.
 */
public class FocalManipulatorBlock extends BaseEntityBlock {
    public static final MapCodec<FocalManipulatorBlock> CODEC = simpleCodec(FocalManipulatorBlock::new);

    public FocalManipulatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FocalManipulatorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.FOCAL_MANIPULATOR, FocalManipulatorBlockEntity::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!ResearchManager.knows(player, "FOCALMANIPULATION")) {
            if (!level.isClientSide()) player.sendSystemMessage(Component.translatable("tc.researchmissing").withStyle(ChatFormatting.RED));
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FocalManipulatorBlockEntity table) player.openMenu(table);
        return InteractionResult.SUCCESS;
    }
}
