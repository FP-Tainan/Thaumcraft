package net.thaumcraft.registry;

import net.minecraft.world.item.Item;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A matéria-prima do mod: o que só existe para entrar em receita.
 *
 * <p><b>Este arquivo é gerado</b> pelo {@code scratchpad/fatia6-recursos.js}. No original tudo isto é
 * um item só com números diferentes; aqui cada um é um item, que é como o jogo de hoje faz.
 */
public final class TCResources {
    /** Cada matéria-prima pelo nome dela. */
    public static final Map<String, Item> ALL = new LinkedHashMap<>();

    private TCResources() {
    }

    public static Item get(String name) {
        return ALL.get(name);
    }

    /** Os nomes na ordem em que eles entram na aba do criativo. */
    public static final String[] NAMES = {
            "thaumium_ingot",
            "quicksilver",
            "magic_tallow",
            "amber",
            "enchanted_fabric",
            "vis_filter",
            "knowledge_fragment",
            "mirrored_glass",
            "jar_label",
            "salis_mundus",
            "primal_charm",
            "void_ingot",
            "gold_coin",
            "thaumium_nugget",
            "void_nugget",
            "quicksilver_drop",
            "native_iron_cluster",
            "native_copper_cluster",
            "native_cinnabar_cluster",
            "native_gold_cluster",
    };
}
