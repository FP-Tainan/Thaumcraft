package net.thaumcraft.shattered;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * O tecido do bolso: o {@code BlockFabric} das Portas Dimensionais.
 *
 * <p>É o que forra as paredes de um Reino Fragmentado, e não se quebra a pico: troca-se. Dentro de um bolso, quem
 * encostar nele um bloco cheio qualquer — que não tenha entidade de bloco e não seja ele mesmo — vê o tecido ceder
 * o lugar. Fora de um bolso ele não responde.
 */
public class FabricBlock extends Block {
    public static final MapCodec<FabricBlock> CODEC = simpleCodec(FabricBlock::new);

    public FabricBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!ShatteredRealms.isPocket(level)) return InteractionResult.PASS;
        if (!(stack.getItem() instanceof BlockItem item)) return InteractionResult.PASS;

        Block novo = item.getBlock();
        BlockState estadoNovo = novo.defaultBlockState();
        if (novo == this || estadoNovo.hasBlockEntity() || !estadoNovo.isSolidRender()) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        var contexto = new BlockPlaceContext(player, hand, stack, hit);
        BlockState posto = novo.getStateForPlacement(contexto);
        level.setBlockAndUpdate(pos, posto == null ? estadoNovo : posto);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.SUCCESS;
    }
}
