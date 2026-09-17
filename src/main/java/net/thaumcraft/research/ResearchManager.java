package net.thaumcraft.research;

import net.minecraft.world.entity.player.Player;

/**
 * As regras de quem pode ver e aprender o quê no Thaumonomicon, como na 4.2.3.5.
 *
 * <p>Três perguntas mandam no mapa: a pesquisa já está sabida, ela pode ser aberta agora (todos os pais
 * feitos), e ela sequer aparece — porque as escondidas e as encobertas ficam fora do mapa até a hora delas.
 */
public final class ResearchManager {
    private ResearchManager() {
    }

    /** As que já vêm sabidas de berço. No original é a marca {@code setAutoUnlock}. */
    public static boolean grantStarters(PlayerKnowledge knowledge) {
        boolean changed = false;
        for (Research research : Researches.ALL.values()) {
            if (research.is(Research.Mark.AUTO) && knowledge.completeResearch(research.key())) changed = true;
        }
        return changed;
    }

    public static void grantStarters(Player player) {
        PlayerKnowledge knowledge = Knowledges.of(player);
        if (grantStarters(knowledge)) Knowledges.save(player, knowledge);
    }

    /** Já se sabe isto? */
    public static boolean isComplete(PlayerKnowledge knowledge, Research research) {
        return knowledge.hasResearch(research.key());
    }

    /**
     * Dá para abrir esta pesquisa agora?
     *
     * <p>É a conta do original: todos os pais têm de estar feitos. Os pais ocultos contam do mesmo jeito —
     * a diferença deles é só não aparecerem no mapa antes da hora.
     */
    public static boolean canUnlock(PlayerKnowledge knowledge, Research research) {
        for (String parent : research.parents()) {
            if (!knowledge.hasResearch(parent)) return false;
        }
        for (String parent : research.parentsHidden()) {
            if (!knowledge.hasResearch(parent)) return false;
        }
        return true;
    }

    /**
     * Esta pesquisa aparece no mapa?
     *
     * <p>As virtuais nunca aparecem: são só degrau. As perdidas somem depois de sabidas. As escondidas
     * esperam topar com o que as desperta, e as encobertas esperam os pais.
     */
    public static boolean isVisible(PlayerKnowledge knowledge, Research research) {
        if (research.is(Research.Mark.VIRTUAL)) return false;
        if (knowledge.hasResearch(research.key())) return !research.is(Research.Mark.LOST);
        if (research.is(Research.Mark.LOST)) return false;
        if (research.is(Research.Mark.HIDDEN)) return knowledge.hasScanned("hint:" + research.key());
        if (research.is(Research.Mark.CONCEALED)) return canUnlock(knowledge, research);
        return true;
    }
}
