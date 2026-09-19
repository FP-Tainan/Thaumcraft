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
     * Examina de verdade: anota os aspectos, paga os pontos e devolve o que dizer ao jogador.
     *
     * @param key   a chave com que aquilo fica anotado
     * @param name  o nome da coisa, para a mensagem
     */
    public static Result scan(Player player, String key, AspectList aspects, Component name) {
        PlayerKnowledge knowledge = Knowledges.of(player);
        if (aspects == null || aspects.isEmpty()) {
            return Result.of(false, Component.translatable("tc.scan.nothing", name));
        }
        if (!canUnderstand(knowledge, aspects)) {
            Aspect missing = missingParent(knowledge, aspects);
            Component aviso = missing == null
                    ? Component.translatable("tc.scan.cannot", name)
                    : Component.translatable("tc.scan.missing", name, missing.name());
            return Result.of(false, aviso);
        }

        boolean first = knowledge.markScanned(key);
        java.util.List<Aspect> won = new java.util.ArrayList<>();
        java.util.List<Integer> gained = new java.util.ArrayList<>();
        java.util.List<Integer> totals = new java.util.ArrayList<>();
        for (Map.Entry<Aspect, Integer> entry : aspects.entries()) {
            Aspect aspect = entry.getKey();
            // só a primeira vez rende ponto; depois o aparelho só mostra o que a coisa tem
            boolean known = knowledge.hasDiscovered(aspect);
            int given = first ? knowledge.award(aspect, entry.getValue()) : 0;
            if (!first) knowledge.discover(aspect);
            // os avisos do checkAndSyncAspectKnowledge: o aspecto novo e os pontos que entraram
            if (player instanceof net.minecraft.server.level.ServerPlayer server) {
                if (!known) net.thaumcraft.net.TCNetwork.aspectDiscovery(server, aspect);
                if (given > 0) net.thaumcraft.net.TCNetwork.aspectPool(server, aspect, given, knowledge.points(aspect));
            }
            if (given > 0) {
                won.add(aspect);
                gained.add(given);
                totals.add(knowledge.points(aspect));
            }
        }
        Knowledges.save(player, knowledge);

        if (!first) return Result.of(true, Component.translatable("tc.scan.again", name));
        return new Result(true, Component.translatable("tc.scan.learned", name), won, gained, totals);
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
