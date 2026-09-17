package net.thaumcraft.api.golems;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * De que os golens são feitos, e o que cada matéria vale.
 *
 * <p><b>Este arquivo é gerado</b> pelo {@code scratchpad/fatia7-golens.js} a partir do
 * {@code EnumGolemType} do mod original. Não mexer na mão.
 *
 * <p>Os números são os do Thaumcraft 4.2.3.5. A palha é o golem de todo dia — dez de vida, carrega
 * uma coisa só e anda rápido; o táumio é o topo — quarenta de vida, trinta e duas coisas na mão e
 * duas melhorias. A argila, a pedra, o ferro e o táumio não pegam fogo.
 */
public final class GolemTypes {
    /**
     * Uma matéria de golem.
     *
     * @param health    quanto de vida ele tem
     * @param carry     quantas coisas ele carrega de uma vez
     * @param strength  quanto ele bate
     * @param armor     quanto ele aguenta apanhar
     * @param speed     o passo dele
     * @param fireResist se o fogo o ignora
     * @param upgrades  quantas melhorias cabem nele
     * @param regenDelay de quantos em quantos tiques ele se remenda
     * @param visCost   o vis que ele custa para ser feito
     */
    public record Type(String name, int health, int carry, int strength, int armor, double speed,
                      boolean fireResist, int upgrades, int regenDelay, int visCost) {
    }

    /** Cada matéria pelo nome dela, na ordem do original. */
    public static final Map<String, Type> ALL = new LinkedHashMap<>();

    /** Os núcleos, pelo nome que o original dá a cada um. */
    public static final String[] CORES = {
            "fill", "empty", "gather", "harvest", "guard", "decanting", "alchemy", "chop", "use", "butcher", "sorting", "fishing",
    };

    private GolemTypes() {
    }

    static {
        add("straw", 10, 1, 0, 0, 0.38, false, 1, 75, 0);
        add("wood", 20, 4, 1, 6, 0.35, false, 1, 75, 1);
        add("tallow", 20, 8, 2, 9, 0.33, false, 2, 75, 2);
        add("clay", 25, 8, 2, 9, 0.33, true, 1, 100, 2);
        add("flesh", 15, 4, 1, 6, 0.35, false, 2, 40, 1);
        add("stone", 30, 16, 3, 12, 0.32, true, 1, 100, 3);
        add("iron", 35, 32, 4, 15, 0.31, true, 1, 125, 4);
        add("thaumium", 40, 32, 4, 15, 0.32, true, 2, 100, 4);
    }

    private static void add(String name, int health, int carry, int strength, int armor, double speed,
                            boolean fireResist, int upgrades, int regenDelay, int visCost) {
        ALL.put(name, new Type(name, health, carry, strength, armor, speed, fireResist, upgrades,
                regenDelay, visCost));
    }

    public static Type of(String name) {
        return ALL.get(name);
    }

    /** Chamado na abertura do mod só para a tabela sair do papel. */
    public static void init() {
    }
}
