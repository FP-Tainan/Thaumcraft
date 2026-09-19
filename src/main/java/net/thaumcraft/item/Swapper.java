package net.thaumcraft.item;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.net.TCNetwork;
import net.thaumcraft.registry.TCSounds;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fila de trocas do foco de Troca Equivalente: o {@code VirtualSwapper} do {@code ServerTickEventsFML} da
 * 4.2.3.5.
 *
 * <p>Cada troca pedida entra na fila do mundo; a cada tique o servidor faz uma — a primeira da fila que ainda
 * valha. A troca tira um bloco do inventário de quem pediu, põe no lugar e devolve o que saiu, cobrando o vis
 * do foco. Se ela ainda tem fôlego, os vizinhos iguais ao bloco trocado que estejam à mostra entram na fila
 * com um de fôlego a menos: é assim que a troca se espalha por uma parede.
 */
public final class Swapper {
    private record Task(BlockPos pos, BlockState source, Item target, int lifespan, Player player, int slot) {
    }

    private static final Map<ServerLevel, Deque<Task>> QUEUES = new HashMap<>();
    /** A cor do brilho da troca, o 12632319 do original. */
    private static final int SPARKLE = 0xC0C0FF;

    private Swapper() {
    }

    public static void init() {
        ServerTickEvents.END_LEVEL_TICK.register(Swapper::tick);
    }

    /** O {@code addSwapper}: põe uma troca na fila, se ela fizer sentido. */
    public static void add(ServerLevel level, BlockPos pos, BlockState source, Item target, int lifespan, Player player,
                           int slot) {
        if (source.isAir() || source.getDestroySpeed(level, pos) < 0.0f || source.getBlock().asItem() == target) return;
        QUEUES.computeIfAbsent(level, l -> new ArrayDeque<>()).add(new Task(pos, source, target, lifespan, player, slot));
        level.playSound(null, player, TCSounds.WAND.value(), SoundSource.PLAYERS, 0.25f, 1.0f);
    }

    private static void tick(ServerLevel level) {
        Deque<Task> queue = QUEUES.get(level);
        if (queue == null) return;
        while (!queue.isEmpty()) {
            Task task = queue.poll();
            if (swap(level, task, queue)) return;
        }
    }

    private static boolean swap(ServerLevel level, Task task, Deque<Task> queue) {
        Player player = task.player;
        if (player.isRemoved()) return false;
        BlockState here = level.getBlockState(task.pos);
        ItemStack wand = player.getInventory().getItem(task.slot);
        FocusItem focus = wand.getItem() instanceof WandItem ? Focuses.on(wand) : null;
        if (focus == null || !level.mayInteract(player, task.pos) || here.getBlock().asItem() == task.target) return false;
        ItemStack focusStack = WandItem.focusStack(wand);
        if (!WandItem.consumeFocus(wand, focus.cost(focusStack), false, player)) return false;
        if (!(task.target instanceof BlockItem blockItem)) return false;
        int slot = -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(task.target)) {
                slot = i;
                break;
            }
        }
        boolean creative = player.getAbilities().instabuild;
        if (creative) slot = 1;
        if (here != task.source || slot < 0) return false;

        if (!creative) {
            player.getInventory().removeItem(slot, 1);
            // o bloco tirado sai com a sorte do tesouro, ou inteiro com o toque de seda
            ItemStack tool = Focuses.harvestTool(level, WandItem.focusTreasure(wand), FocusItem.isUpgradedWith(focusStack, FocusUpgradeTable.SILKTOUCH));
            List<ItemStack> drops = Block.getDrops(here, level, task.pos, level.getBlockEntity(task.pos), player, tool);
            for (ItemStack drop : drops) {
                if (!player.getInventory().add(drop)) Block.popResource(level, task.pos, drop);
            }
            WandItem.consumeFocus(wand, focus.cost(focusStack), true, player);
        }
        level.setBlock(task.pos, blockItem.getBlock().defaultBlockState(), Block.UPDATE_ALL);
        TCNetwork.blockSparkle(level, task.pos, SPARKLE);
        level.levelEvent(2001, task.pos, Block.getId(task.source));

        if (task.lifespan > 0) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos next = task.pos.offset(dx, dy, dz);
                        if (level.getBlockState(next) == task.source && exposed(level, next)) {
                            queue.add(new Task(next, task.source, task.target, task.lifespan - 1, player, task.slot));
                        }
                    }
                }
            }
        }
        return true;
    }

    /** O {@code BlockUtils.isBlockExposed}: algum dos seis lados dá para algo que não tapa a vista. */
    private static boolean exposed(ServerLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (!level.getBlockState(pos.relative(dir)).canOcclude()) return true;
        }
        return false;
    }
}
