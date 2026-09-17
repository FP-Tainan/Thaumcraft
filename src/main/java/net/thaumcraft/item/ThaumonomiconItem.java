package net.thaumcraft.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * O Thaumonomicon: o livro em que tudo o que se sabe fica anotado.
 *
 * <p>Abri-lo não custa nada e não gasta nada — quem abre a tela é a máquina de quem joga, no
 * {@code ThaumcraftClient}, para que nada de desenho encoste no lado do servidor.
 */
public class ThaumonomiconItem extends Item {
    public ThaumonomiconItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return InteractionResult.SUCCESS;
    }
}
