package net.thaumcraft.api;

import net.thaumcraft.item.FocusUpgradeTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * As melhorias de foco que não são do Thaumcraft — a porta por onde um ramo de fora registra as dele, como o
 * {@code new FocusUpgradeType(...)} do original.
 *
 * <p>A tabela do próprio mod ({@link FocusUpgradeTable}) é gerada do jar e não se mexe; o que vem de fora mora aqui,
 * e quem pergunta por uma melhoria ou pelos postos de um foco passa por estas portas, que olham as duas.
 */
public final class FocusUpgrades {
    private static final Map<Short, FocusUpgradeTable.Type> EXTRA = new HashMap<>();
    private static final Map<String, List<List<FocusUpgradeTable.Type>>> EXTRA_RANKS = new HashMap<>();

    private FocusUpgrades() {
    }

    /** Uma melhoria nova. O número tem de ser alto, para não bater com os vinte e um do original. */
    public static FocusUpgradeTable.Type register(short id, String name, String icon,
                                                  net.thaumcraft.api.aspects.AspectList aspects) {
        FocusUpgradeTable.Type type = new FocusUpgradeTable.Type(id, name, icon, aspects);
        EXTRA.put(id, type);
        return type;
    }

    /** O que cabe em cada um dos cinco postos de um foco (o {@code getPossibleUpgradesByRank}). */
    public static void ranks(String focusType, List<List<FocusUpgradeTable.Type>> ranks) {
        EXTRA_RANKS.put(focusType, ranks);
    }

    /** A melhoria de número tal, seja do mod ou de fora. */
    public static FocusUpgradeTable.Type byId(short id) {
        FocusUpgradeTable.Type type = FocusUpgradeTable.BY_ID.get(id);
        return type != null ? type : EXTRA.get(id);
    }

    /** Todas, as do mod e as de fora. */
    public static Map<Short, FocusUpgradeTable.Type> all() {
        Map<Short, FocusUpgradeTable.Type> todas = new HashMap<>(FocusUpgradeTable.BY_ID);
        todas.putAll(EXTRA);
        return todas;
    }

    /** Os postos de um foco: os do mod, e os de fora quando o foco é de fora. */
    public static List<List<FocusUpgradeTable.Type>> ranksOf(String focusType) {
        List<List<FocusUpgradeTable.Type>> ranks = FocusUpgradeTable.RANKS.get(focusType);
        if (ranks != null) return ranks;
        List<List<FocusUpgradeTable.Type>> extra = EXTRA_RANKS.get(focusType);
        return extra != null ? extra : new ArrayList<>();
    }
}
