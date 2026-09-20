package net.thaumcraft.world.outer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.util.RandomSource;
import net.thaumcraft.registry.TCBlocks;
import org.jetbrains.annotations.Nullable;

/**
 * O mundo visto pelos geradores do labirinto: o pedaço do {@code World} do 1.7 que o {@code GenCommon} e as salas usam
 * (pôr bloco por número, ler bloco, "é ar?"), por cima da região em que o chunk está sendo construído. Os números do
 * original viram os blocos deste porte aqui, num lugar só.
 */
public final class MazeWorld {
    public final WorldGenLevel level;
    public final RandomSource rand;
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    /** Onde podem ir enfeites, aberturas de caranguejo e urnas: as listas do {@code GenCommon}, uma por construção. */
    final java.util.List<BlockPos> decoCommon = new java.util.ArrayList<>(), crabSpawner = new java.util.ArrayList<>(), decoUrn = new java.util.ArrayList<>();

    /** Está construindo o chunk agora? Aí o mundo só deixa tocar no pedaço em construção e nos vizinhos dele. */
    private final boolean generating;

    public MazeWorld(WorldGenLevel level, RandomSource rand) {
        this.level = level;
        this.rand = rand;
        this.generating = level instanceof net.minecraft.server.level.WorldGenRegion;
    }

    private BlockPos at(int x, int y, int z) {
        return this.cursor.set(x, y, z);
    }

    /**
     * O chunk está ao alcance de quem está construindo? O mundo de hoje só deixa ler e escrever no pedaço em construção
     * e nos vizinhos dele; passar disso derruba o jogo, enquanto o de 1.7 só devolvia ar. Aqui a resposta é a de então.
     */
    private boolean reachable(int x, int z) {
        // num mundo já construído tudo está ao alcance (o chunk se carrega sozinho); a cerca é só na construção
        return !this.generating || this.level.hasChunk(x >> 4, z >> 4);
    }

    public BlockState get(int x, int y, int z) {
        if (!this.reachable(x, z)) return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        return this.level.getBlockState(this.at(x, y, z));
    }

    public boolean is(int x, int y, int z, Block block) {
        return this.get(x, y, z).is(block);
    }

    /** O {@code isAirBlock}. */
    public boolean isAir(int x, int y, int z) {
        return this.get(x, y, z).isAir();
    }

    public void set(int x, int y, int z, BlockState state) {
        if (!this.reachable(x, z)) return;
        this.level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }

    @Nullable
    public BlockEntity blockEntity(int x, int y, int z) {
        if (!this.reachable(x, z)) return null;
        return this.level.getBlockEntity(new BlockPos(x, y, z));
    }

    /** O {@code isBlockNormalCube} do 1.7: bloco que tapa a vista e é cheio. */
    public boolean opaque(int x, int y, int z) {
        if (!this.reachable(x, z)) return false;
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = this.level.getBlockState(pos);
        return state.isSolidRender();
    }

    /** O {@code BlockUtils.countExposedSides}: quantas faces dão para o ar. */
    public int exposedSides(int x, int y, int z) {
        int n = 0;
        for (Direction d : Direction.values()) {
            if (this.isAir(x + d.getStepX(), y + d.getStepY(), z + d.getStepZ())) n++;
        }
        return n;
    }

    /** O {@code BlockUtils.isBlockAdjacentToAtleast(…, blockEldritch, qualquer, 1)}: encosta em alguma pedra eldritch? */
    public boolean nextToEldritch(int x, int y, int z) {
        for (Direction d : Direction.values()) {
            if (isEldritch(this.get(x + d.getStepX(), y + d.getStepY(), z + d.getStepZ()))) return true;
        }
        return false;
    }

    /** O {@code BlockUtils.isAdjacentToSolidBlock}. */
    public boolean nextToSolid(int x, int y, int z) {
        for (Direction d : Direction.values()) {
            BlockPos p = new BlockPos(x + d.getStepX(), y + d.getStepY(), z + d.getStepZ());
            if (this.level.getBlockState(p).isFaceSturdy(this.level, p, d.getOpposite())) return true;
        }
        return false;
    }

    /** As peças do {@code BlockEldritch} de então. */
    static boolean isEldritch(BlockState state) {
        return state.is(TCBlocks.ELDRITCH_ALTAR) || state.is(TCBlocks.ELDRITCH_OBELISK) || state.is(TCBlocks.ELDRITCH_OBELISK_UPPER)
                || state.is(TCBlocks.ELDRITCH_CAPSTONE) || state.is(TCBlocks.GLOWING_CRUSTED_STONE) || state.is(TCBlocks.GLYPHED_STONE)
                || state.is(TCBlocks.ELDRITCH_DECO) || state.is(TCBlocks.ANCIENT_DOORWAY) || state.is(TCBlocks.ANCIENT_LOCK)
                || state.is(TCBlocks.CRUSTED_OPENING) || state.is(TCBlocks.RUNED_STONE);
    }

    /** O {@code Utils.setBiomeAt}: a coluna vira o bioma dado. */
    public void biome(int x, int z, Holder<Biome> biome) {
        var chunk = this.level.getChunk(new BlockPos(x, 0, z));
        int qx = QuartPos.fromBlock(x), qz = QuartPos.fromBlock(z);
        chunk.fillBiomesFromNoise((bx, by, bz, sampler) -> bx == qx && bz == qz ? biome : chunk.getNoiseBiome(bx, by, bz), Climate.empty());
    }

    // os blocos do original, pelo número

    static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
    static final BlockState AIR = Blocks.AIR.defaultBlockState();

}
