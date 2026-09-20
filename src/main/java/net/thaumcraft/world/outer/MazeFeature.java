package net.thaumcraft.world.outer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.thaumcraft.block.eldritch.EldritchNothingBlock;
import net.thaumcraft.registry.TCBlocks;

/**
 * O {@code MazeHandler.generateEldritch}: cada chunk das Terras de Fora que é casa do labirinto vira a sala dela, no
 * andar cinquenta — o portal, as partes da sala do chefe, a sala da chave, o ninho, a biblioteca ou o corredor — e depois
 * os enfeites. Chunk sem casa fica vazio, como no original.
 *
 * <p>O original sabia quais blocos do nada davam para fora pelo aviso de vizinho que o 1.7 mandava na construção; aqui
 * isso é conferido no fim, no chunk e na beirada dos vizinhos (que as passagens tocam).
 */
public class MazeFeature extends Feature<NoneFeatureConfiguration> {
    public static final int FLOOR = 50;

    public MazeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        Labyrinth maze = Labyrinth.get(level.getServer());
        if (maze == null) return false;
        int cx = context.origin().getX() >> 4, cz = context.origin().getZ() >> 4;
        Cell cell = maze.cell(cx, cz);
        if (cell == null) return false;
        generate(new MazeWorld(level, context.random()), cx, cz, cell);
        return true;
    }

    public static void generate(MazeWorld w, int cx, int cz, Cell cell) {
        switch (cell.feature) {
            case 1 -> GenPortal.generatePortal(w, cx, cz, FLOOR, cell);
            case 2, 3, 4, 5 -> GenRooms.bossRoom(w, cx, cz, FLOOR, cell);
            case 6 -> GenRooms.keyRoom(w, cx, cz, FLOOR, cell);
            case 7 -> GenRooms.nestRoom(w, cx, cz, FLOOR, cell);
            case 8 -> GenRooms.libraryRoom(w, cx, cz, FLOOR, cell);
            default -> GenPassage.generateDefaultPassage(w, cx, cz, FLOOR, cell);
        }
        GenCommon.processDecorations(w);
        exposeNothing(w, cx, cz);
    }

    /** Os blocos do nada que dão para algum lugar aberto ganham o céu de estrelas. */
    static void exposeNothing(MazeWorld w, int cx, int cz) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = cx * 16 - 1; x <= cx * 16 + 16; x++) {
            for (int z = cz * 16 - 1; z <= cz * 16 + 16; z++) {
                for (int y = FLOOR - 1; y <= FLOOR + 14; y++) {
                    pos.set(x, y, z);
                    // a beirada dos vizinhos só se lê se o chunk estiver ao alcance de quem constrói
                    if (w.level instanceof net.minecraft.server.level.WorldGenRegion && !w.level.hasChunk(x >> 4, z >> 4)) continue;
                    BlockState state = w.level.getBlockState(pos);
                    if (!state.is(TCBlocks.ELDRITCH_NOTHING)) continue;
                    boolean exposed = EldritchNothingBlock.exposed(w.level, pos);
                    if (state.getValue(EldritchNothingBlock.EXPOSED) != exposed) {
                        w.level.setBlock(pos, state.setValue(EldritchNothingBlock.EXPOSED, exposed), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private static final Direction[] SIDES = Direction.values();
}
