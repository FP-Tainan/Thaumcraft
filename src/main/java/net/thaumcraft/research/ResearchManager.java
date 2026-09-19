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
        completeWithSiblings(player, knowledge, research);
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
    public static void completeWithSiblings(Player player, PlayerKnowledge knowledge, Research research) {
        complete(player, knowledge, research.key());
        for (String sibling : research.siblings()) {
            Research other = Researches.get(sibling);
            if (other != null && !knowledge.hasResearch(sibling) && canUnlock(knowledge, other)) {
                complete(player, knowledge, sibling);
            }
        }
    }

    /**
     * O {@code completeResearch(player, key)}: marca como sabida e, sendo pesquisa proibida, cobra a distorção — metade
     * (arredondada para cima) permanente e a outra metade da que gruda; com um só ponto, permanente.
     */
    public static boolean complete(Player player, PlayerKnowledge knowledge, String key) {
        if (!knowledge.completeResearch(key)) return false;
        Research research = Researches.get(key);
        int warp = research == null ? 0 : research.warp();
        if (warp > 0 && player != null && !player.level().isClientSide()) {
            if (warp > 1) {
                int w2 = warp / 2;
                if (warp - w2 > 0) Warp.add(player, knowledge, warp - w2, false);
                if (w2 > 0) Warp.addSticky(player, knowledge, w2);
            } else {
                Warp.add(player, knowledge, warp, false);
            }
        }
        if (player instanceof net.minecraft.server.level.ServerPlayer server) net.thaumcraft.net.TCNetwork.researchComplete(server, key);
        return true;
    }

    /** Completa e guarda. */
    public static boolean complete(Player player, String key) {
        PlayerKnowledge knowledge = Knowledges.of(player);
        boolean done = complete(player, knowledge, key);
        if (done) Knowledges.save(player, knowledge);
        return done;
    }

    /** Uma pista ({@code @CHAVE}): a pesquisa escondida que ela desperta passa a aparecer no livro. */
    public static boolean clue(Player player, String key) {
        return complete(player, key);
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
     * Esta pesquisa aparece no mapa? A conta do {@code GuiResearchBrowser}: a virtual nunca; a sabida sempre; a que ainda
     * não se sabe aparece se já tem a pista ({@code @CHAVE}), e fora isso some se for perdida, escondida, ou encoberta
     * sem os pais feitos.
     */
    public static boolean isVisible(PlayerKnowledge knowledge, Research research) {
        if (research.is(Research.Mark.VIRTUAL)) return false;
        if (knowledge.hasResearch(research.key())) return true;
        if (knowledge.hasResearch("@" + research.key())) return true;
        if (research.is(Research.Mark.LOST) || research.is(Research.Mark.HIDDEN)) return false;
        return !research.is(Research.Mark.CONCEALED) || canUnlock(knowledge, research);
    }

    /**
     * O {@code createClue}: examinar alguma coisa pela primeira vez pode dar a pista de uma pesquisa escondida ou perdida.
     * Valem as que ainda não se sabem nem têm pista, e cujo gatilho ({@link ResearchTriggers}) bate — o item examinado,
     * a criatura examinada ou um dos aspectos que o exame rendeu. Das que baterem, uma é sorteada.
     *
     * @param clue o {@link net.minecraft.world.item.ItemStack} ou o nome ({@code minecraft:enderman}) da criatura
     */
    public static boolean createClue(Player player, Object clue, net.thaumcraft.api.aspects.AspectList aspects) {
        PlayerKnowledge knowledge = Knowledges.of(player);
        java.util.List<String> keys = new java.util.ArrayList<>();
        for (Research research : Researches.ALL.values()) {
            if (research.tags().size() == 0 || !(research.is(Research.Mark.LOST) || research.is(Research.Mark.HIDDEN))
                    || knowledge.hasResearch(research.key()) || knowledge.hasResearch("@" + research.key())) continue;
            ResearchTriggers.Triggers triggers = ResearchTriggers.of(research.key());
            if (triggers == null) continue;
            if (clue instanceof net.minecraft.world.item.ItemStack stack && !triggers.items().isEmpty()) {
                if (triggers.items().stream().anyMatch(t -> t.test(stack))) {
                    keys.add(research.key());
                    continue;
                }
            } else if (clue instanceof String entity && !triggers.entities().isEmpty()) {
                if (triggers.entities().contains(entity)) {
                    keys.add(research.key());
                    continue;
                }
            }
            if (aspects != null && aspects.size() > 0) {
                for (net.thaumcraft.api.aspects.Aspect aspect : triggers.aspects()) {
                    if (aspects.getAmount(aspect) > 0) {
                        keys.add(research.key());
                        break;
                    }
                }
            }
        }
        if (keys.isEmpty()) return false;
        String key = keys.get(player.getRandom().nextInt(keys.size()));
        return complete(player, "@" + key);
    }

    /**
     * O {@code findHiddenResearch}: a pesquisa escondida que uma nota de conhecimento desconhecido revela — uma das que
     * ainda não se sabem, com os pais feitos e algum gatilho, sorteada pela hora do mundo (a mesma em cada cinquenta
     * tiques). Sem nenhuma, {@code "FAIL"}.
     */
    public static String findHiddenResearch(Player player) {
        PlayerKnowledge knowledge = Knowledges.of(player);
        java.util.List<String> keys = new java.util.ArrayList<>();
        for (Research research : Researches.ALL.values()) {
            if (!research.is(Research.Mark.HIDDEN) || research.tags().size() == 0) continue;
            if (!knowledge.hasResearch(research.key()) && canUnlock(knowledge, research)
                    && ResearchTriggers.of(research.key()) != null) keys.add(research.key());
        }
        java.util.Random rand = new java.util.Random(player.level().getOverworldClockTime() / 10L / 5L);
        return keys.isEmpty() ? "FAIL" : keys.get(rand.nextInt(keys.size()));
    }
}
