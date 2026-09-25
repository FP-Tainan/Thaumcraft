package net.thaumcraft.mortuorum;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * O Necronomicon: o {@code ItemNecronomicon} do Necromancy.
 *
 * <p>Não se lê — se usa. Clicado numa tábua de carvalho que tenha duas pedregulhos enfileirados ao lado, o livro
 * se gasta e as três pedras viram o Altar de Invocação, olhando para o lado em que as pedregulhos estavam.
 */
public class NecronomiconItem extends Item {
    public NecronomiconItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos onde = context.getClickedPos();
        if (!level.getBlockState(onde).is(Blocks.OAK_PLANKS)) return InteractionResult.PASS;

        for (Direction lado : Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(onde.relative(lado)).is(Blocks.COBBLESTONE)) continue;
            if (!level.getBlockState(onde.relative(lado, 2)).is(Blocks.COBBLESTONE)) continue;
            if (level.isClientSide()) return InteractionResult.SUCCESS;

            BlockState coluna = MortuorumBlocks.SUMMONING_ALTAR.defaultBlockState()
                    .setValue(SummoningAltarBlock.FACING, lado);
            BlockState mesa = MortuorumBlocks.SUMMONING_ALTAR_PART.defaultBlockState()
                    .setValue(SummoningAltarPartBlock.FACING, lado);
            level.setBlock(onde, coluna, 3);
            level.setBlock(onde.relative(lado), mesa, 3);
            level.setBlock(onde.relative(lado, 2), mesa, 3);
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
