package net.thaumcraft.api.wands;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Coisa que responde a uma batida de varinha.
 *
 * <p>É o {@code IWandable} da 4.2.3.5. Na taumaturgia a varinha não é só arma: ela é a chave de fenda do
 * lugar. Abre e fecha válvula, gira o lado do tubo, acorda a matriz, recolhe o que o crisol deixou. Em
 * vez de cada bloco ter de aparecer numa lista dentro do item, quem quiser atender à varinha implementa
 * isto na sua peça de bloco.
 */
public interface Wandable {
    /**
     * A varinha bateu aqui.
     *
     * @param face a face em que ela bateu
     * @return {@code true} se houve resposta — aí a varinha não tenta mais nada neste bloco
     */
    boolean onWand(Level level, ItemStack wand, Player player, BlockPos pos, Direction face);
}
