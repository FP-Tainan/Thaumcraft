package net.thaumcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.PlayerKnowledge;
import net.thaumcraft.research.Research;
import net.thaumcraft.research.Researches;

import java.util.Collection;
import java.util.List;

/**
 * O comando do mod, para testar sem ter de jogar tudo de novo.
 *
 * <p>Em criativo os itens aparecem na aba, mas isso não basta: o crisol, a bancada arcana e a infusão
 * conferem a pesquisa antes de deixar sair qualquer coisa. Este comando é o atalho para essa conferência
 * — e para os pontos de aspecto e os aspectos descobertos, que são as outras duas coisas que travam.
 *
 * <p>Ele pede nível dois de permissão, que é o mesmo que os comandos de trapaça do jogo pedem: em mundo
 * de um jogador só basta ter os truques ligados.
 */
public final class ThaumcraftCommand {
    /** Quantos pontos de cada aspecto o {@code pontos} dá, se ninguém disser quantos. */
    private static final int DEFAULT_POINTS = 64;

    private static final SuggestionProvider<CommandSourceStack> RESEARCH_KEYS =
            (context, builder) -> SharedSuggestionProvider.suggest(Researches.ALL.keySet(), builder);
    private static final SuggestionProvider<CommandSourceStack> ASPECT_TAGS =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    Aspects.all().stream().map(Aspect::tag).toList(), builder);

    private ThaumcraftCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("thaumcraft")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));

        // tudo de uma vez: aspectos descobertos, pontos no bolso e todas as pesquisas sabidas
        root.then(Commands.literal("tudo")
                .executes(context -> everything(context, List.of(context.getSource().getPlayerOrException())))
                .then(Commands.argument("quem", EntityArgument.players())
                        .executes(context -> everything(context, EntityArgument.getPlayers(context, "quem")))));

        // só as pesquisas
        root.then(Commands.literal("pesquisa")
                .then(Commands.literal("tudo")
                        .executes(context -> allResearch(context, List.of(context.getSource().getPlayerOrException())))
                        .then(Commands.argument("quem", EntityArgument.players())
                                .executes(context -> allResearch(context, EntityArgument.getPlayers(context, "quem")))))
                .then(Commands.literal("dar")
                        .then(Commands.argument("chave", StringArgumentType.word())
                                .suggests(RESEARCH_KEYS)
                                .executes(context -> oneResearch(context,
                                        List.of(context.getSource().getPlayerOrException()),
                                        StringArgumentType.getString(context, "chave")))
                                .then(Commands.argument("quem", EntityArgument.players())
                                        .executes(context -> oneResearch(context,
                                                EntityArgument.getPlayers(context, "quem"),
                                                StringArgumentType.getString(context, "chave"))))))
                .then(Commands.literal("limpar")
                        .executes(context -> forget(context, List.of(context.getSource().getPlayerOrException())))
                        .then(Commands.argument("quem", EntityArgument.players())
                                .executes(context -> forget(context, EntityArgument.getPlayers(context, "quem"))))));

        // os pontos de aspecto, que é o que a pesquisa cobra
        root.then(Commands.literal("pontos")
                .executes(context -> points(context, List.of(context.getSource().getPlayerOrException()),
                        null, DEFAULT_POINTS))
                .then(Commands.argument("quanto", IntegerArgumentType.integer(0, PlayerKnowledge.ASPECT_CAP))
                        .executes(context -> points(context, List.of(context.getSource().getPlayerOrException()),
                                null, IntegerArgumentType.getInteger(context, "quanto")))
                        .then(Commands.argument("aspecto", StringArgumentType.word())
                                .suggests(ASPECT_TAGS)
                                .executes(context -> points(context,
                                        List.of(context.getSource().getPlayerOrException()),
                                        StringArgumentType.getString(context, "aspecto"),
                                        IntegerArgumentType.getInteger(context, "quanto"))))));

        // e o vis da varinha na mão, que é o outro custo do mod
        root.then(Commands.literal("varinha")
                .executes(ThaumcraftCommand::fillWand));

        dispatcher.register(root);
    }

    /** Descobre todos os aspectos, enche o bolso de pontos e destranca todas as pesquisas. */
    private static int everything(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            PlayerKnowledge knowledge = Knowledges.of(player);
            for (Aspect aspect : Aspects.all()) {
                knowledge.discover(aspect);
                knowledge.award(aspect, PlayerKnowledge.ASPECT_CAP);
            }
            for (String key : Researches.ALL.keySet()) knowledge.completeResearch(key);
            Knowledges.save(player, knowledge);
        }
        say(context, "tudo destrancado para " + who(targets) + ": "
                + Aspects.count() + " aspectos e " + Researches.ALL.size() + " pesquisas");
        return targets.size();
    }

    private static int allResearch(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            PlayerKnowledge knowledge = Knowledges.of(player);
            for (String key : Researches.ALL.keySet()) knowledge.completeResearch(key);
            Knowledges.save(player, knowledge);
        }
        say(context, Researches.ALL.size() + " pesquisas destrancadas para " + who(targets));
        return targets.size();
    }

    private static int oneResearch(CommandContext<CommandSourceStack> context,
                                   Collection<ServerPlayer> targets, String key) {
        Research research = Researches.get(key);
        if (research == null) {
            context.getSource().sendFailure(Component.literal("não existe pesquisa chamada " + key));
            return 0;
        }
        for (ServerPlayer player : targets) {
            PlayerKnowledge knowledge = Knowledges.of(player);
            knowledge.completeResearch(key);
            Knowledges.save(player, knowledge);
        }
        say(context, key + " destrancada para " + who(targets));
        return targets.size();
    }

    private static int forget(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) Knowledges.save(player, new PlayerKnowledge());
        say(context, "o caderno de " + who(targets) + " foi apagado");
        return targets.size();
    }

    /** Dá pontos de um aspecto, ou de todos se nenhum for dito. */
    private static int points(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets,
                              String tag, int amount) {
        Aspect only = tag == null ? null : Aspect.of(tag);
        if (tag != null && only == null) {
            context.getSource().sendFailure(Component.literal("não existe aspecto chamado " + tag));
            return 0;
        }
        for (ServerPlayer player : targets) {
            PlayerKnowledge knowledge = Knowledges.of(player);
            for (Aspect aspect : only == null ? Aspects.all() : List.of(only)) {
                knowledge.discover(aspect);
                knowledge.award(aspect, amount);
            }
            Knowledges.save(player, knowledge);
        }
        say(context, amount + " pontos de " + (only == null ? "cada aspecto" : only.tag())
                + " para " + who(targets));
        return targets.size();
    }

    /** Enche de vis a varinha que estiver na mão. */
    private static int fillWand(CommandContext<CommandSourceStack> context)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        var stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof net.thaumcraft.item.WandItem)) {
            context.getSource().sendFailure(Component.literal("é preciso estar com uma varinha na mão"));
            return 0;
        }
        for (Aspect aspect : Aspects.primals()) {
            net.thaumcraft.item.WandItem.addVis(stack, aspect, Integer.MAX_VALUE / 2);
        }
        say(context, "varinha cheia");
        return 1;
    }

    private static String who(Collection<ServerPlayer> targets) {
        if (targets.size() == 1) return targets.iterator().next().getGameProfile().name();
        return targets.size() + " jogadores";
    }

    private static void say(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(() -> Component.literal(message), true);
    }
}
