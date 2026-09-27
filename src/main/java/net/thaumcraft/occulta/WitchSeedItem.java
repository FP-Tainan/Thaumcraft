package net.thaumcraft.occulta;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Uma semente do ofício: planta a sua planta, e nunca <b>em cima de outra</b>.
 *
 * <p><b>Desvio declarado, pedido.</b> O {@code ItemWitchSeeds} do Witchery deixava semear sobre a própria planta
 * e sobre a losna — e o que saía disso era uma planta boiando um bloco acima da horta, plantada à mão sobre outra.
 * Aqui a semente recusa o clique quando o que está debaixo do lugar é planta do ofício, seja ela qual for.
 *
 * <p>A losna continua a <b>empilhar-se sozinha</b>: isso é coisa do crescimento dela, não de quem semeia (ver o
 * {@code randomTick} do {@link WitchCropBlock}).
 */
public class WitchSeedItem extends BlockItem {
    public WitchSeedItem(Block crop, Properties properties) {
        super(crop, properties);
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        BlockState chão = context.getLevel().getBlockState(context.getClickedPos().below());
        if (chão.getBlock() instanceof WitchCropBlock) return false;
        return super.canPlace(context, state);
    }
}
