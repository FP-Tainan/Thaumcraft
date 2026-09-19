package net.thaumcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCSounds;

/**
 * O {@code createArcaneFurnace} do {@code WandManager} da 4.2.3.5: a varinha, batida numa obsidiana, num tijolo do
 * Nether ou numa grade de ferro de um cubo 3 × 3 × 3 montado certo, gasta 50 de Ignis e 50 de Terra e o transforma na
 * fornalha infernal.
 *
 * <p>O cubo, de cima para baixo: em cima, os cantos de tijolo do Nether, o meio das bordas de obsidiana e o centro
 * vazio; no meio, o mesmo com lava no centro e uma das obsidianas das bordas trocada pela grade (a boca); embaixo,
 * todo o meio de obsidiana.
 */
public final class InfernalFurnaceStructure {
    private InfernalFurnaceStructure() {
    }

    /** Um bloco do cubo: B tijolo, O obsidiana, A ar, L lava. */
    private enum Kind { B, O, A, L }

    private static final Kind[][][] BLUEPRINT = {
            {{Kind.B, Kind.O, Kind.B}, {Kind.O, Kind.A, Kind.O}, {Kind.B, Kind.O, Kind.B}},
            {{Kind.B, Kind.O, Kind.B}, {Kind.O, Kind.L, Kind.O}, {Kind.B, Kind.O, Kind.B}},
            {{Kind.B, Kind.O, Kind.B}, {Kind.O, Kind.O, Kind.O}, {Kind.B, Kind.O, Kind.B}},
    };

    /** A varinha responde nestes blocos. */
    public static boolean isTrigger(BlockState state) {
        return state.is(Blocks.OBSIDIAN) || state.is(Blocks.NETHER_BRICKS) || state.is(Blocks.IRON_BARS);
    }

    public static boolean create(ItemStack wand, Player player, Level level, BlockPos pos) {
        for (int xx = pos.getX() - 2; xx <= pos.getX(); xx++) {
            for (int yy = pos.getY() - 2; yy <= pos.getY(); yy++) {
                for (int zz = pos.getZ() - 2; zz <= pos.getZ(); zz++) {
                    BlockPos origin = new BlockPos(xx, yy, zz);
                    if (fits(level, origin) && WandItem.consume(wand, new AspectList().add(Aspects.FIRE, 50).add(Aspects.EARTH, 50), true, player)) {
                        replace(level, origin);
                        level.playSound(null, pos, TCSounds.WAND.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean is(BlockState state, Kind kind) {
        return switch (kind) {
            case B -> state.is(Blocks.NETHER_BRICKS);
            case O -> state.is(Blocks.OBSIDIAN);
            case A -> state.isAir();
            case L -> state.is(Blocks.LAVA) && state.getFluidState().isSource();
        };
    }

    /** O {@code fitArcaneFurnace}: o cubo com o canto noroeste de baixo em {@code origin}, com exatamente uma grade. */
    public static boolean fits(Level level, BlockPos origin) {
        boolean fencefound = false;
        for (int yy = 0; yy < 3; yy++) {
            for (int xx = 0; xx < 3; xx++) {
                for (int zz = 0; zz < 3; zz++) {
                    BlockState state = level.getBlockState(origin.offset(xx, 2 - yy, zz));
                    if (is(state, BLUEPRINT[yy][xx][zz])) continue;
                    if (yy != 1 || fencefound || !state.is(Blocks.IRON_BARS) || xx == zz || xx != 1 && zz != 1) return false;
                    fencefound = true;
                }
            }
        }
        return fencefound;
    }

    /** O {@code replaceArcaneFurnace}: cada bloco que não é ar vira a parte da fornalha da posição dele. */
    public static void replace(Level level, BlockPos origin) {
        BlockPos centre = origin.offset(1, 1, 1);
        for (int yy = 0; yy < 3; yy++) {
            int step = 1;
            for (int zz = 0; zz < 3; zz++) {
                for (int xx = 0; xx < 3; xx++) {
                    BlockPos at = origin.offset(xx, yy, zz);
                    BlockState was = level.getBlockState(at);
                    int md = step++;
                    if (was.is(Blocks.LAVA)) md = 0;
                    if (was.is(Blocks.IRON_BARS)) md = 10;
                    if (was.isAir()) continue;
                    BlockState part = TCBlocks.INFERNAL_FURNACE.defaultBlockState().setValue(InfernalFurnaceBlock.PART, md);
                    if (md == 10) {
                        Direction toCentre = Direction.getNearest(centre.getX() - at.getX(), 0, centre.getZ() - at.getZ(), Direction.NORTH);
                        part = part.setValue(InfernalFurnaceBlock.FACING, toCentre);
                    }
                    // sem avisar os vizinhos: o centro, posto antes da camada de cima, se desfaria ao ver o cubo pela metade
                    level.setBlock(at, part, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                    level.blockEvent(at, TCBlocks.INFERNAL_FURNACE, 1, 4);
                }
            }
        }
    }
}
