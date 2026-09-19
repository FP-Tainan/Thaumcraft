package net.thaumcraft.research;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.api.aspects.Aspects;

import java.util.Map;

/**
 * O exame do thaumômetro, com as regras do Thaumcraft 4.2.3.5.
 *
 * <p>Três coisas mandam aqui, e são as do original: só dá para examinar o que você tem cabeça para entender —
 * de todo aspecto composto que a coisa tiver, você precisa já conhecer os dois de que ele nasce; cada coisa
 * só rende ponto na primeira vez; e descobrir um aspecto pela primeira vez rende dois pontos a mais.
 */
public final class ScanManager {
    private ScanManager() {
    }

    /**
     * O resultado do exame: se valeu, o que dizer ao jogador e quanto entrou de cada aspecto.
     *
     * <p>Os ganhos vêm em três listas do mesmo tamanho — o aspecto, quanto entrou agora e quanto ele tem no
     * total — que é o que o canto da tela mostra.
     */
    public record Result(boolean scanned, Component message, java.util.List<Aspect> aspects,
                         java.util.List<Integer> gained, java.util.List<Integer> totals) {
        static Result of(boolean scanned, Component message) {
            return new Result(scanned, message, java.util.List.of(), java.util.List.of(), java.util.List.of());
        }
    }

    /** A chave com que aquilo fica anotado como já examinado. */
    public static String keyOf(ItemStack stack) {
        return "item:" + BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    public static String keyOf(Entity entity) {
        // coisa caída no chão conta como o item que ela é, e não como um bicho à parte
        ItemStack dropped = droppedItem(entity);
        if (dropped != null) return keyOf(dropped);
        return EntityAspects.key(entity);
    }

    /** O que uma coisa caída no chão carrega, ou nada se não for uma coisa caída. */
    public static ItemStack droppedItem(Entity entity) {
        if (!(entity instanceof net.minecraft.world.entity.item.ItemEntity item)) return null;
        ItemStack stack = item.getItem();
        return stack.isEmpty() ? null : stack;
    }

    /** O nome do que está na mira: o do item, quando é coisa caída no chão. */
    public static Component nameOf(Entity entity) {
        ItemStack dropped = droppedItem(entity);
        return dropped == null ? entity.getDisplayName() : dropped.getHoverName();
    }

    public static String keyOf(BlockState state) {
        return "block:" + BuiltInRegistries.BLOCK.getKey(state.getBlock());
    }

    /** De que a criatura é feita: a coisa caída é o item dela; o resto, a tabela do original ({@link EntityAspects}). */
    public static AspectList aspectsOf(Entity entity) {
        ItemStack dropped = droppedItem(entity);
        if (dropped != null) return ObjectAspects.of(dropped);
        return EntityAspects.of(entity);
    }

    /**
     * O nome de um bloco.
     *
     * <p>Vem do bloco, não do item que ele vira: a água e a lava não viram item nenhum, e pelo item o
     * aparelho acabava anunciando "Ar".
     */
    public static Component nameOf(BlockState state) {
        return state.getBlock().getName();
    }

    /** De que o bloco é feito: o que a tabela do original diz do item que ele vira. */
    public static AspectList aspectsOf(BlockState state) {
        return ObjectAspects.ofBlock(state.getBlock());
    }

    /**
     * Você tem cabeça para entender isto?
     *
     * <p>É a regra do original: de todo aspecto composto que a coisa tiver, é preciso já conhecer os dois de
     * que ele nasce. Sem isso o aparelho não lê.
     */
    public static boolean canUnderstand(PlayerKnowledge knowledge, AspectList aspects) {
        if (aspects == null || aspects.isEmpty()) return false;
        for (Aspect aspect : aspects.getAspects()) {
            if (!aspect.isPrimal() && !knowledge.hasDiscoveredParents(aspect)) return false;
        }
        return true;
    }

    /** Qual aspecto está faltando para entender aquilo — o que o aparelho avisa quando recusa. */
    public static Aspect missingParent(PlayerKnowledge knowledge, AspectList aspects) {
        for (Aspect aspect : aspects.getAspects()) {
            if (aspect.isPrimal() || knowledge.hasDiscoveredParents(aspect)) continue;
            for (Aspect parent : aspect.components()) {
                if (!knowledge.hasDiscovered(parent)) return parent;
            }
        }
        return null;
    }

    /**
     * O {@code completeScan} do original, do lado do servidor. Coisa já examinada nem chega aqui (o
     * {@code isValidScanTarget} a recusa antes); sem aspectos, ou sem entender algum deles, o aviso do canto diz por quê.
     * De resto, os aspectos entram, os avisos de ponto saem, e o que foi examinado pode dar a pista de uma pesquisa
     * escondida.
     *
     * @param key   a chave com que aquilo fica anotado
     * @param name  o nome da coisa
     * @param clue  o que vale de pista: a pilha do item, ou o nome da criatura; nada, para um nodo
     */
    public static Result scan(Player player, String key, AspectList aspects, Component name, Object clue) {
        PlayerKnowledge knowledge = Knowledges.of(player);
        if (knowledge.hasScanned(key)) return Result.of(false, null);
        var server = player instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null;
        if (aspects == null || aspects.isEmpty()) {
            if (server != null) net.thaumcraft.net.TCNetwork.notice(server, "tc.unknownobject", "");
            return Result.of(false, null);
        }
        if (!canUnderstand(knowledge, aspects)) {
            Aspect missing = missingParent(knowledge, aspects);
            if (server != null && missing != null) {
                net.thaumcraft.net.TCNetwork.notice(server, "tc.discoveryerror", "tc.aspect.help." + missing.tag());
            }
            return Result.of(false, null);
        }

        knowledge.markScanned(key);
        java.util.List<Aspect> won = new java.util.ArrayList<>();
        java.util.List<Integer> gained = new java.util.ArrayList<>();
        java.util.List<Integer> totals = new java.util.ArrayList<>();
        AspectList earned = new AspectList();
        for (Map.Entry<Aspect, Integer> entry : aspects.entries()) {
            Aspect aspect = entry.getKey();
            // o checkAndSyncAspectKnowledge: o aspecto novo vale dois a mais, e o teto encolhe o ganho
            boolean known = knowledge.hasDiscovered(aspect);
            int given = knowledge.award(aspect, entry.getValue());
            if (server != null) {
                if (!known) net.thaumcraft.net.TCNetwork.aspectDiscovery(server, aspect);
                if (given > 0) net.thaumcraft.net.TCNetwork.aspectPool(server, aspect, given, knowledge.points(aspect));
            }
            if (given > 0) {
                won.add(aspect);
                gained.add(given);
                totals.add(knowledge.points(aspect));
                earned.add(aspect, given);
            }
        }
        Knowledges.save(player, knowledge);
        if (clue != null && server != null) ResearchManager.createClue(player, clue, earned);
        return new Result(true, null, won, gained, totals);
    }

    /** O mesmo, sem pista. */
    public static Result scan(Player player, String key, AspectList aspects, Component name) {
        return scan(player, key, aspects, name, null);
    }

    /**
     * O alvo do aparelho: o {@code ScanResult} do original. A marca diz se a mira continua no mesmo lugar; as runas
     * são o {@code blockRunes} que sobe dele enquanto o exame corre.
     */
    public record Target(String marker, String key, AspectList aspects, Component name, Object clue,
                         double x, double y, double z, int runes) {
    }

    /** Até onde o aparelho enxerga criatura ({@code getPointedEntity}, dez blocos) e bloco (o alcance do braço, cinco). */
    public static final double ENTITY_REACH = 10.0;
    public static final double BLOCK_REACH = 5.0;

    /** O {@code doScan} do {@code ItemThaumometer}: o que está na mira — criatura primeiro, depois nodo, depois bloco. */
    public static Target target(Level level, Player player) {
        Entity creature = entityInSight(level, player, ENTITY_REACH);
        if (creature != null) {
            ItemStack dropped = droppedItem(creature);
            Object clue = dropped != null ? dropped.copyWithCount(1)
                    : BuiltInRegistries.ENTITY_TYPE.getKey(creature.getType()).toString();
            return new Target("e" + creature.getId(), keyOf(creature), aspectsOf(creature), nameOf(creature), clue,
                    creature.getX() - 0.5, creature.getY() + creature.getEyeHeight() / 2.0, creature.getZ() - 0.5,
                    (int) (creature.getBbHeight() * 15.0f));
        }
        BlockPos pos = blockInSight(player, BLOCK_REACH);
        if (pos == null) return null;
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return null;
        if (level.getBlockEntity(pos) instanceof net.thaumcraft.block.entity.NodeBlockEntity node) {
            String id = "NODE" + level.dimension().identifier() + ":" + pos.getX() + ":" + pos.getY() + ":" + pos.getZ();
            return new Target("n" + pos.asLong(), id, nodeAspects(node), state.getBlock().getName(), null,
                    pos.getX(), pos.getY() + 0.25, pos.getZ(), 15);
        }
        return new Target("b" + pos.asLong(), keyOf(state), aspectsOf(state), nameOf(state), new ItemStack(state.getBlock().asItem()),
                pos.getX(), pos.getY() + 0.25, pos.getZ(), 15);
    }

    /** O {@code generateNodeAspects}: um décimo de cada aspecto (quatro no mínimo) e o toque do tipo do nodo. */
    public static AspectList nodeAspects(net.thaumcraft.block.entity.NodeBlockEntity node) {
        AspectList tags = new AspectList();
        AspectList have = node.aspects();
        for (Aspect aspect : have.getAspects()) tags.merge(aspect, Math.max(4, have.getAmount(aspect) / 10));
        switch (node.type()) {
            case UNSTABLE -> tags.merge(Aspects.ENTROPY, 4);
            case HUNGRY -> tags.merge(Aspects.HUNGER, 4);
            case TAINTED -> tags.merge(Aspects.TAINT, 4);
            case PURE -> {
                tags.merge(Aspects.HEAL, 2);
                tags.add(Aspects.ORDER, 2);
            }
            case DARK -> {
                tags.merge(Aspects.DEATH, 2);
                tags.add(Aspects.DARKNESS, 2);
            }
            default -> {
            }
        }
        return tags;
    }

    /** O que está na mira de quem segura o aparelho: primeiro bicho, depois bloco. */
    /**
     * A varredura é nossa, e não a do jogo, de propósito: a mira do jogo descarta o que flecha nenhuma
     * acerta, e é justamente o caso das coisas caídas no chão, que no original se examinam como qualquer
     * outra. Aqui passa tudo o que não é fantasma, e vence quem estiver mais perto do olho.
     */
    public static Entity entityInSight(Level level, Player player, double reach) {
        net.minecraft.world.phys.Vec3 eyes = player.getEyePosition();
        net.minecraft.world.phys.Vec3 aim = eyes.add(player.getViewVector(1.0f).scale(reach));
        net.minecraft.world.phys.AABB box = player.getBoundingBox()
                .expandTowards(player.getViewVector(1.0f).scale(reach)).inflate(1.0);
        Entity closest = null;
        double nearest = reach * reach;
        for (Entity target : level.getEntities(player, box, entity -> !entity.isSpectator())) {
            // uma folga na caixa, senão coisa miúda como uma maçã quase não se deixa mirar
            net.minecraft.world.phys.AABB hitbox = target.getBoundingBox().inflate(0.3);
            var hit = hitbox.clip(eyes, aim);
            double distance;
            if (hit.isPresent()) {
                distance = eyes.distanceToSqr(hit.get());
            } else if (hitbox.contains(eyes)) {
                distance = 0.0;
            } else {
                continue;
            }
            if (distance > nearest) continue;
            nearest = distance;
            closest = target;
        }
        return closest;
    }

    public static BlockPos blockInSight(Player player, double reach) {
        net.minecraft.world.phys.HitResult hit = player.pick(reach, 1.0f, true);
        if (!(hit instanceof net.minecraft.world.phys.BlockHitResult block)) return null;
        return block.getBlockPos();
    }
}
