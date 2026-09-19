package net.thaumcraft.world.outer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.thaumcraft.registry.TCBlocks;

/** Os blocos do labirinto pelo número que tinham no 1.7, para as salas e para a fechadura. */
public final class MazeBlocks {
    private MazeBlocks() {
    }

    /** O {@code blockCosmeticSolid}: 11 pedra antiga, 12 rocha antiga, 13 pedra antiga lisa, 14 incrustada, 15 pedestal. */
    public static BlockState cosmetic(int meta) {
        return switch (meta) {
            case 11 -> TCBlocks.ANCIENT_STONE.defaultBlockState();
            case 12 -> TCBlocks.ANCIENT_ROCK.defaultBlockState();
            case 13 -> TCBlocks.ANCIENT_STONE_NOSPAWN.defaultBlockState();
            case 14 -> TCBlocks.CRUSTED_STONE.defaultBlockState();
            case 15 -> TCBlocks.ANCIENT_STONE_PEDESTAL.defaultBlockState();
            default -> throw new IllegalArgumentException("cosmético " + meta);
        };
    }

    /** O {@code blockStairsEldritch}: a escada do 1.7 (0 leste, 1 oeste, 2 sul, 3 norte; +4 de ponta-cabeça). */
    public static BlockState stairs(int meta) {
        Direction facing = switch (meta & 3) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.WEST;
            case 2 -> Direction.SOUTH;
            default -> Direction.NORTH;
        };
        return TCBlocks.ANCIENT_STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, (meta & 4) != 0 ? Half.TOP : Half.BOTTOM);
    }

    /** O {@code blockSlabStone} de pedra antiga: 1 embaixo, 9 em cima. */
    public static BlockState slab(int meta) {
        return TCBlocks.ANCIENT_STONE_SLAB.defaultBlockState()
                .setValue(net.minecraft.world.level.block.SlabBlock.TYPE, (meta & 8) != 0 ? SlabType.TOP : SlabType.BOTTOM);
    }

    /** O {@code BlockEldritch} pelo número. */
    public static BlockState eldritch(int meta) {
        return (switch (meta) {
            case 0 -> TCBlocks.ELDRITCH_ALTAR;
            case 1 -> TCBlocks.ELDRITCH_OBELISK;
            case 2 -> TCBlocks.ELDRITCH_OBELISK_UPPER;
            case 3 -> TCBlocks.ELDRITCH_CAPSTONE;
            case 4 -> TCBlocks.GLOWING_CRUSTED_STONE;
            case 5 -> TCBlocks.GLYPHED_STONE;
            case 6 -> TCBlocks.ELDRITCH_DECO;
            case 7 -> TCBlocks.ANCIENT_DOORWAY;
            case 8 -> TCBlocks.ANCIENT_LOCK;
            case 9 -> TCBlocks.CRUSTED_OPENING;
            case 10 -> TCBlocks.RUNED_STONE;
            default -> throw new IllegalArgumentException("eldritch " + meta);
        }).defaultBlockState();
    }

    /** A urna ({@code blockLootUrn}) ou o caixote ({@code blockLootCrate}) da raridade dada. */
    public static BlockState loot(boolean crate, int rarity) {
        return (crate ? TCBlocks.LOOT_CRATES : TCBlocks.LOOT_URNS).get(rarity).defaultBlockState();
    }

    /** O {@code BlockUtils.isAdjacentToSolidBlock}. */
    public static boolean nextToSolid(BlockGetter level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            BlockPos p = pos.relative(d);
            if (level.getBlockState(p).isFaceSturdy(level, p, d.getOpposite())) return true;
        }
        return false;
    }
}
