package net.thaumcraft.arcana;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * O <b>Óculus</b>: o {@code BlockOcculus} do Ars Magica 2.
 *
 * <p>É onde se olha para o que se pode aprender. Ele não guarda nada e não gasta nada, mas é o único lugar
 * onde os pontos de perícia viram peças de feitiço.
 *
 * <p>No original ele é um pedestal com um olho de pedra em cima, e é a primeira coisa que um arcanista
 * constrói depois da Mesa de Inscrição. Aqui é o mesmo, e pela mesma razão: sem ele, os pontos ficam parados.
 */
public class OcculusBlock extends Block {
    public static final MapCodec<OcculusBlock> CODEC = simpleCodec(OcculusBlock::new);

    public OcculusBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos onde, Player quem,
                                               BlockHitResult bateu) {
        if (!level.isClientSide()) quem.openMenu(provider());
        return InteractionResult.SUCCESS;
    }

    /** A árvore aberta: um menu sem casa nenhuma, que é só o caminho para a tela e para o botão de comprar. */
    public static MenuProvider provider() {
        return new SimpleMenuProvider(
                (id, mochila, quem) -> new OcculusMenu(id, mochila),
                Component.translatable("block.thaumcraft.occulus"));
    }
}
