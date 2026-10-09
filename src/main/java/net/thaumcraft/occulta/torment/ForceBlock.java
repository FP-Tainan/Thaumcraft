package net.thaumcraft.occulta.torment;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * A parede do labirinto do Tormento: a {@code BlockForce} do Witchery, no ramo transparente dela.
 *
 * <p>É pedra maciça que <b>não se vê</b>. Tem colisão, tem som de vidro, não se quebra e não deixa nada —
 * e o original ainda a tira da aba do criativo e do pick-block, de modo que ela não existe na mão de
 * ninguém: existe só onde o mundo a põe.
 *
 * <p>É com ela que o labirinto é feito, e é por isso que o Tormento é o que é. Um labirinto de paredes de
 * pedra é um problema de paciência; um labirinto de paredes que não se veem é um problema de <b>memória</b>.
 * Quem está lá dentro anda com a cara no ar e aprende o caminho às topadas, e a única coisa que se vê do
 * labirinto é o <b>chão</b> dele.
 *
 * <p>A outra metade da {@code BlockForce} — o ramo opaco, que é a <b>Pedra do Tormento</b> — não precisa de
 * classe: é bloco comum e indestrutível, e fica no {@code OccultaBlocks} como tal.
 */
public class ForceBlock extends Block {
    public static final MapCodec<ForceBlock> CODEC = simpleCodec(ForceBlock::new);

    public ForceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    /** Não se vê: é o {@code getRenderType} devolvendo menos um. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /**
     * E a luz atravessa-a: é o {@code setLightOpacity(0)} do original.
     *
     * <p>É o que faz o labirinto ser escuro sem ser breu. As paredes não fazem sombra nenhuma, de modo que
     * o pouco que há de claridade chega por igual a todo lugar — e quem lá anda vê o chão à sua volta e
     * mais nada.
     */
    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    /** E não se apanha, nem quebrada nem com o botão do meio. */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of();
    }

    @Override
    protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level,
                                          net.minecraft.core.BlockPos pos, BlockState state,
                                          boolean comOsDados) {
        return ItemStack.EMPTY;
    }
}
