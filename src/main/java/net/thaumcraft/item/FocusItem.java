package net.thaumcraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.registry.TCComponents;

/**
 * Um foco de varinha: a peça que diz o que a varinha faz quando se aperta o botão.
 *
 * <p>No original o foco entra numa casa da própria varinha, alcançada por uma tecla. Aqui ele se encaixa
 * com um clique: com o foco na mão, um toque procura a varinha no inventário e prende o foco nela;
 * agachado, solta o que estiver preso.
 *
 * @param type o que este foco faz, pelo nome que o original dá a ele
 */
public class FocusItem extends Item {
    private final String type;
    private final AspectList cost;

    public FocusItem(Properties properties, String type, AspectList cost) {
        super(properties);
        this.type = type;
        this.cost = cost;
    }

    public String type() {
        return this.type;
    }

    /** O que este foco cobra por tique de uso, em centésimos de vis, como no original. */
    public AspectList cost() {
        return this.cost.copy();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack focus = player.getItemInHand(hand);
        // agachado, solta o foco que estiver preso na varinha
        if (player.isShiftKeyDown()) return InteractionResult.PASS;

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack found = player.getInventory().getItem(slot);
            if (!(found.getItem() instanceof WandItem)) continue;
            if (level.isClientSide()) return InteractionResult.SUCCESS;

            // o que já estava preso volta para a mão de quem trocou
            String had = found.get(TCComponents.WAND_FOCUS);
            found.set(TCComponents.WAND_FOCUS, this.type);
            focus.shrink(1);
            if (had != null) {
                Item old = Focuses.byType(had);
                if (old != null && !player.getInventory().add(new ItemStack(old))) {
                    player.drop(new ItemStack(old), false);
                }
            }
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_PLACE,
                    SoundSource.PLAYERS, 0.6f, 1.4f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.thaumcraft.focus." + this.type);
    }
}
