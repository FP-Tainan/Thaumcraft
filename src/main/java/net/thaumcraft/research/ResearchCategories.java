package net.thaumcraft.research;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * As abas do Thaumonomicon, na ordem e com os desenhos do Thaumcraft 4.2.3.5.
 *
 * <p>São as seis que o {@code ConfigResearch.initCategories} registra. A última, a dos Eldritch, só aparece
 * para quem já sabe o que há do outro lado — no original ela fica escondida até a pesquisa {@code
 * ELDRITCHMINOR} estar feita.
 */
public final class ResearchCategories {
    /** Uma aba: o desenho que a representa e o pergaminho que serve de fundo ao mapa. */
    public record Category(String key, Identifier icon, Identifier background) {
        public Component name() {
            return Component.translatable("tc.research_category." + key);
        }
    }

    public static final Map<String, Category> ALL = new LinkedHashMap<>();

    /** A aba dos Eldritch só se abre para quem já esbarrou no que há do outro lado. */
    public static final String ELDRITCH_GATE = "ELDRITCHMINOR";

    static {
        register("BASICS", "item/thaumonomicon.png", "gui/gui_researchback.png");
        register("THAUMATURGY", "misc/r_thaumaturgy.png", "gui/gui_researchback.png");
        register("ALCHEMY", "misc/r_crucible.png", "gui/gui_researchback.png");
        register("ARTIFICE", "misc/r_artifice.png", "gui/gui_researchback.png");
        register("GOLEMANCY", "misc/r_golemancy.png", "gui/gui_researchback.png");
        register("ELDRITCH", "misc/r_eldritch.png", "gui/gui_researchbackeldritch.png");
    }

    private ResearchCategories() {
    }

    private static void register(String key, String icon, String background) {
        ALL.put(key, new Category(key, Thaumcraft.id("textures/" + icon), Thaumcraft.id("textures/" + background)));
    }

    /** As abas que este jogador pode ver agora. */
    public static List<Category> visible(PlayerKnowledge knowledge) {
        return ALL.values().stream()
                .filter(category -> !category.key().equals("ELDRITCH") || knowledge.hasResearch(ELDRITCH_GATE))
                .toList();
    }

    public static Category get(String key) {
        return ALL.get(key);
    }
}
