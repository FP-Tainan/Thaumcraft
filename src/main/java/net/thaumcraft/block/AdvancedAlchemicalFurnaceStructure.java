package net.thaumcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.net.TCNetwork;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCSounds;

/**
 * O {@code createAdvancedAlchemicalFurnace} do {@code WandManager} da 4.2.3.5: a varinha numa construção alquímica
 * (comum ou avançada) perto de uma fornalha alquímica montada certo gasta 50 de Ignis, Aqua e Ordo e a transforma na
 * fornalha avançada. Embaixo, a fornalha cercada das oito construções avançadas; em cima, alambiques nos cantos e
 * construções alquímicas nos lados (o meio de cima tanto faz).
 */
public final class AdvancedAlchemicalFurnaceStructure {
    private AdvancedAlchemicalFurnaceStructure() {
    }

    /** O {@code blueprint}: [camada][x][z]; 4 construção avançada, 3 a fornalha, 1 alambique, 2 construção, 0 qualquer. */
    private static final int[][][] BLUEPRINT = {
            {{4, 4, 4}, {4, 3, 4}, {4, 4, 4}},
            {{1, 2, 1}, {2, 0, 2}, {1, 2, 1}},
    };

    public static boolean isTrigger(BlockState state) {
        return state.is(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT) || state.is(TCBlocks.ALCHEMICAL_CONSTRUCT);
    }

    private static boolean matches(BlockState state, int kind) {
        return switch (kind) {
            case 1 -> state.is(TCBlocks.ALEMBIC);
            case 2 -> state.is(TCBlocks.ALCHEMICAL_CONSTRUCT);
            case 3 -> state.is(TCBlocks.ALCHEMICAL_FURNACE);
            case 4 -> state.is(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT);
            default -> true;
        };
    }

    public static boolean create(ItemStack wand, Player player, Level level, BlockPos pos) {
        if (level.isClientSide()) return false;
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                for (int c = -1; c <= 1; c++) {
                    BlockPos centre = pos.offset(a, b, c);
                    if (!level.getBlockState(centre).is(TCBlocks.ALCHEMICAL_FURNACE)) continue;
                    // como no original, a primeira fornalha achada decide: se ela não fecha, não se procura outra
                    if (!fits(level, centre)) return false;
                    if (!WandItem.consume(wand, new AspectList().add(Aspects.FIRE, 50).add(Aspects.WATER, 50).add(Aspects.ORDER, 50), true, player)) {
                        return false;
                    }
                    build(level, centre);
                    level.playSound(null, centre, TCSounds.WAND.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    if (level instanceof ServerLevel server) {
                        for (BlockPos at : BlockPos.betweenClosed(centre.offset(-1, 0, -1), centre.offset(1, 1, 1))) {
                            TCNetwork.blockSparkle(server, at.immutable(), -9999);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /** O molde em volta desta fornalha alquímica está completo? */
    public static boolean fits(Level level, BlockPos centre) {
        for (int aa = -1; aa <= 1; aa++) {
            for (int bb = 0; bb <= 1; bb++) {
                for (int cc = -1; cc <= 1; cc++) {
                    if (!matches(level.getBlockState(centre.offset(aa, bb, cc)), BLUEPRINT[bb][aa + 1][cc + 1])) return false;
                }
            }
        }
        return true;
    }

    /** Põe as partes: o meio, os bicos nos lados e os cantos embaixo; em cima, os lados e os cantos. */
    public static void build(Level level, BlockPos centre) {
        BlockState base = TCBlocks.ADVANCED_ALCHEMICAL_FURNACE.defaultBlockState();
        level.setBlock(centre, base, 3);
        int[][] sides = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int[][] corners = {{-1, -1}, {1, 1}, {1, -1}, {-1, 1}};
        for (int[] s : sides) set(level, centre.offset(s[0], 0, s[1]), base, 1);
        for (int[] s : corners) set(level, centre.offset(s[0], 0, s[1]), base, 4);
        for (int[] s : sides) set(level, centre.offset(s[0], 1, s[1]), base, 3);
        for (int[] s : corners) set(level, centre.offset(s[0], 1, s[1]), base, 2);
    }

    private static void set(Level level, BlockPos at, BlockState base, int part) {
        level.setBlock(at, base.setValue(AdvancedAlchemicalFurnaceBlock.PART, part), Block.UPDATE_ALL);
    }
}
