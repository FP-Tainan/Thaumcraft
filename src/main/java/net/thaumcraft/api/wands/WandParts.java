package net.thaumcraft.api.wands;

import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * De que as varinhas são feitas: as hastes e as ponteiras da 4.2.3.5.
 *
 * <p><b>Este arquivo é gerado</b> pelo {@code scratchpad/fatia4-varinhas.js} a partir do
 * {@code Thaumcraft.java} do mod original. As capacidades, os descontos e os custos de feitura são os
 * de lá, sem um número digitado de memória.
 *
 * <p>A haste manda em quanto a varinha guarda de cada aspecto; a ponteira, em quanto se gasta a cada
 * uso — a de ferro cobra dez por cento a mais, a de ouro cobra o cheio, e as de taumínio e de vazio
 * cobram menos. As hastes de bastão guardam mais e custam mais para fazer.
 */
public final class WandParts {
    /** Uma ponteira: o desconto que ela dá e o que custa fazê-la. */
    public record Cap(String tag, float discount, int craftCost) {
    }

    /**
     * Uma haste: quanto ela guarda de cada aspecto e o que custa fazê-la.
     *
     * @param primal   o aspecto que ela recolhe sozinha do ar, quando tem um
     * @param anyPrimal se ela recolhe qualquer primário, como a haste primordial
     * @param staff    se é haste de bastão em vez de varinha
     * @param glowing  se ela acende no escuro
     */
    public record Rod(String tag, int capacity, int craftCost, Aspect primal, boolean anyPrimal,
                     boolean staff, boolean glowing) {
    }

    public static final Map<String, Cap> CAPS = new LinkedHashMap<>();
    public static final Map<String, Rod> RODS = new LinkedHashMap<>();
    public static final Map<String, Rod> STAFF_RODS = new LinkedHashMap<>();

    private WandParts() {
    }

    static {
        cap("iron", 1.1f, 1);
        cap("gold", 1.0f, 3);
        cap("thaumium", 0.9f, 6);
        cap("void", 0.8f, 9);

        rod("wood", 25, 1, null, false, false, false);
        rod("greatwood", 50, 3, null, false, false, false);
        rod("obsidian", 75, 6, Aspects.EARTH, false, false, false);
        rod("blaze", 75, 6, Aspects.FIRE, false, false, true);
        rod("ice", 75, 6, Aspects.WATER, false, false, false);
        rod("quartz", 75, 6, Aspects.ORDER, false, false, false);
        rod("bone", 75, 6, Aspects.ENTROPY, false, false, false);
        rod("reed", 75, 6, Aspects.AIR, false, false, false);
        rod("silverwood", 100, 9, null, false, false, false);
        rod("greatwood", 125, 8, null, false, true, false);
        rod("obsidian", 175, 14, Aspects.EARTH, false, true, false);
        rod("blaze", 175, 14, Aspects.FIRE, false, true, true);
        rod("ice", 175, 14, Aspects.WATER, false, true, false);
        rod("quartz", 175, 14, Aspects.ORDER, false, true, false);
        rod("bone", 175, 14, Aspects.ENTROPY, false, true, false);
        rod("reed", 175, 14, Aspects.AIR, false, true, false);
        rod("silverwood", 250, 24, null, false, true, false);
        rod("primal", 250, 32, null, true, true, false);
    }

    private static void cap(String tag, float discount, int craftCost) {
        CAPS.put(tag, new Cap(tag, discount, craftCost));
    }

    private static void rod(String tag, int capacity, int craftCost, Aspect primal, boolean anyPrimal,
                            boolean staff, boolean glowing) {
        Rod made = new Rod(tag, capacity, craftCost, primal, anyPrimal, staff, glowing);
        (staff ? STAFF_RODS : RODS).put(tag, made);
    }

    public static Cap cap(String tag) {
        return CAPS.get(tag);
    }

    public static Rod rod(String tag) {
        Rod found = RODS.get(tag);
        return found != null ? found : STAFF_RODS.get(tag);
    }
}
