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
 * <p>Como no original, o foco entra e sai da varinha pela tecla de trocar foco (F): segurando, abre o menu
 * radial com os focos do inventário e das bolsas; agachado, a tecla tira o foco preso.
 *
 * @param type o que este foco faz, pelo nome que o original dá a ele
 */
public class FocusItem extends Item {
    private final String type;
    private final AspectList cost;
    private final boolean continuous;

    public FocusItem(Properties properties, String type, AspectList cost, boolean continuous) {
        super(properties);
        this.type = type;
        this.cost = cost;
        this.continuous = continuous;
    }

    /**
     * Este foco é jato contínuo ou tiro único?
     *
     * <p>O de fogo e o de escavação seguram o botão e vão cobrando por tique; o de gelo e o de raio saem
     * de uma vez, como no original.
     */
    public boolean isContinuous() {
        return this.continuous;
    }

    public String type() {
        return this.type;
    }

    /** O que este foco cobra por tique de uso, em centésimos de vis, como no original. */
    public AspectList cost() {
        return this.cost.copy();
    }

    /**
     * O {@code getSortingHelper} do original: a ordem em que o foco aparece no menu radial e em que a tecla passa
     * de um para outro. Cada foco tem as suas letras; as melhorias, quando existirem, entram depois delas.
     */
    public String sortKey() {
        return switch (this.type) {
            case "fire" -> "AF";
            case "excavation" -> "BE";
            case "frost" -> "BF";
            case "shock" -> "BL";
            case "portable_hole" -> "BPH";
            case "trade" -> "BT";
            case "warding" -> "BWA";
            case "primal" -> "FP";
            case "hellbat" -> "HH";
            case "pech" -> "PP";
            default -> this.type;
        };
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.thaumcraft.focus." + this.type);
    }
}
