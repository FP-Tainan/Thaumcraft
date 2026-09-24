package net.thaumcraft.naturalis;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A foice: o {@code SickleItem} do Magia Naturalis 0.5.0.
 *
 * <p>Ela corta o que é de cortar — folha, planta, cipó, teia, cogumelo — e leva junto tudo o que for igual e
 * estiver encostado, até o tanto que a foice alcança ({@code areaSize}). Agachado, ela corta um bloco só.
 *
 * <p>A de abundância colhe direto para o inventário e faz cair mais de uma vez ({@code abundanceLevel}), que é o
 * {@code BlockUtil.harvestBlock} do original.
 */
public class SickleItem extends Item {
    /** Quantos blocos ligados ela leva além do primeiro. */
    private final int areaSize;
    /** Quantas vezes a mais o bloco cai. */
    private final int abundanceLevel;
    /** Se o que cai vai direto para o inventário de quem corta. */
    private final boolean collectLoot;

    public SickleItem(Properties properties, int areaSize, int abundanceLevel, boolean collectLoot) {
        super(properties);
        this.areaSize = areaSize;
        this.abundanceLevel = abundanceLevel;
        this.collectLoot = collectLoot;
    }

    /** O que a foice corta: o {@code isEffectiveVsBlock} do original. */
    public static boolean cuts(BlockState state) {
        return state.is(BlockTags.LEAVES) || state.is(BlockTags.REPLACEABLE_BY_TREES) || state.is(BlockTags.CROPS)
                || state.is(BlockTags.FLOWERS) || state.is(BlockTags.WOOL)
                || state.is(Blocks.COBWEB) || state.is(Blocks.VINE) || state.is(Blocks.SUGAR_CANE)
                || state.is(Blocks.BROWN_MUSHROOM) || state.is(Blocks.RED_MUSHROOM)
                || state.getBlock() instanceof net.minecraft.world.level.block.SaplingBlock;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!(entity instanceof Player player) || !(level instanceof ServerLevel server)) return true;
        if (!cuts(state)) {
            stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            return true;
        }
        // o que cai do bloco que a pessoa quebrou: a abundância vale para ele também
        this.spill(server, player, state, pos);
        if (player.isShiftKeyDown()) return true;
        for (BlockPos at : this.vein(server, player, pos, state)) {
            if (at.equals(pos)) continue;
            BlockState there = server.getBlockState(at);
            this.spill(server, player, there, at);
            server.destroyBlock(at, false, player);
            stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        }
        return true;
    }

    /**
     * O {@code plotVeinArea}: os blocos iguais e encostados no primeiro, em largura, até encher o tanto que a foice
     * alcança.
     */
    private List<BlockPos> vein(ServerLevel level, Player player, BlockPos from, BlockState state) {
        List<BlockPos> found = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        Deque<BlockPos> next = new ArrayDeque<>();
        next.add(from);
        seen.add(from);
        while (!next.isEmpty() && found.size() <= this.areaSize) {
            BlockPos at = next.poll();
            BlockState there = level.getBlockState(at);
            if (!there.is(state.getBlock()) || there.getDestroySpeed(level, at) < 0.0f) continue;
            if (!level.mayInteract(player, at)) continue;
            found.add(at);
            for (net.minecraft.core.Direction side : net.minecraft.core.Direction.values()) {
                BlockPos beside = at.relative(side);
                if (seen.add(beside)) next.add(beside);
            }
        }
        return found;
    }

    /** O que cai a mais, e para onde: o {@code harvestBlock} do original. */
    private void spill(ServerLevel level, Player player, BlockState state, BlockPos pos) {
        if (this.abundanceLevel <= 0 && !this.collectLoot) return;
        ItemStack tool = player.getMainHandItem();
        for (int extra = 0; extra < this.abundanceLevel + (this.collectLoot ? 1 : 0); extra++) {
            // a primeira volta do laço só recolhe quando a foice colhe: o próprio jogo já derruba uma vez
            boolean firstAndPlain = extra == 0 && !this.collectLoot;
            if (firstAndPlain) continue;
            for (ItemStack drop : net.minecraft.world.level.block.Block.getDrops(state, level, pos, null, player, tool)) {
                if (this.collectLoot && player instanceof ServerPlayer) {
                    if (!player.getInventory().add(drop)) player.drop(drop, false);
                } else {
                    net.minecraft.world.level.block.Block.popResource(level, pos, drop);
                }
            }
        }
    }
}
