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
     * Destranca uma pesquisa, cobrando os aspectos que ela pede.
     *
     * <p>No mod original isto acontece na mesa de pesquisa, com papel, tinta e o tabuleiro de hexágonos.
     * O tabuleiro é a última peça da fatia 3 e ainda não chegou; até lá, o que a pesquisa cobra é cobrado
     * aqui, direto do que o thaumômetro já rendeu. Os preços são os do original.
     *
     * @return se a pesquisa foi destrancada agora
     */
    public static boolean unlock(Player player, String key) {
        if (player == null || key == null) return false;
        Research research = Researches.get(key);
        if (research == null) return false;

        PlayerKnowledge knowledge = Knowledges.of(player);
        if (knowledge.hasResearch(key)) return false;
        if (!canUnlock(knowledge, research)) return false;
        if (!isVisible(knowledge, research)) return false;

        // sem os pontos, nada feito
        for (net.thaumcraft.api.aspects.Aspect aspect : research.tags().getAspects()) {
            if (knowledge.points(aspect) < research.tags().getAmount(aspect)) return false;
        }
        for (net.thaumcraft.api.aspects.Aspect aspect : research.tags().getAspects()) {
            knowledge.spend(aspect, research.tags().getAmount(aspect));
        }
        completeWithSiblings(knowledge, research);
        Knowledges.save(player, knowledge);
        player.level().playSound(null, player.blockPosition(),
                net.thaumcraft.registry.TCSounds.LEARN.value(),
                net.minecraft.sounds.SoundSource.PLAYERS, 0.75f, 1.0f);
        return true;
    }

    /**
     * O clique numa pesquisa do livro: o {@code PacketPlayerCompleteToServer} do original.
     *
     * <p>As pesquisas de lado ({@code setSecondary}) se compram direto com os pontos de aspecto. As outras
     * não: com papel e tinta no inventário, o clique escreve uma nota de pesquisa, que se resolve na mesa.
     */
    public static boolean request(net.minecraft.server.level.ServerPlayer player, String key) {
        Research research = Researches.get(key);
        if (research == null) return false;
        PlayerKnowledge knowledge = Knowledges.of(player);
        if (knowledge.hasResearch(key) || !canUnlock(knowledge, research)) return false;
        if (research.tags().size() == 0) return false;
        if (research.is(Research.Mark.SECONDARY)) return unlock(player, key);
        boolean given = ResearchNotes.giveNote(player, key);
        if (given) {
            player.level().playSound(null, player.blockPosition(), net.thaumcraft.registry.TCSounds.WRITE.value(),
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.75f, 1.0f);
        }
        return given;
    }

    /** Marca a pesquisa como sabida, e com ela as irmãs que já dá para abrir, como o original faz. */
    public static void completeWithSiblings(PlayerKnowledge knowledge, Research research) {
        knowledge.completeResearch(research.key());
        for (String sibling : research.siblings()) {
            Research other = Researches.get(sibling);
            if (other != null && !knowledge.hasResearch(sibling) && canUnlock(knowledge, other)) {
                knowledge.completeResearch(sibling);
            }
        }
    }

    /**
     * Este jogador pode usar uma receita que pede esta pesquisa?
     *
     * <p>É a regra do original: o que não se pesquisou não se fabrica. Receita sem pesquisa marcada, ou
     * com uma que não existe na árvore, passa direto — são as que o mod deixa livres.
     */
    public static boolean knows(Player player, String research) {
        if (player == null || research == null || research.isEmpty()) return true;
        if (Researches.get(research) == null) return true;
        return Knowledges.of(player).hasResearch(research);
    }

    /** Já dá para pagar o que esta pesquisa cobra? */
    public static boolean canAfford(PlayerKnowledge knowledge, Research research) {
        for (net.thaumcraft.api.aspects.Aspect aspect : research.tags().getAspects()) {
            if (knowledge.points(aspect) < research.tags().getAmount(aspect)) return false;
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
