package net.thaumcraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.List;

/**
 * O {@code Utils.setBiomeAt} da 4.2.3.5: troca o bioma de uma coluna do mundo (o jogo de então guardava um bioma por
 * coluna; o de hoje guarda de quatro em quatro blocos, e aqui a coluna toda de quatro por quatro muda) e avisa quem
 * está vendo, como o comando {@code /fillbiome}.
 */
public final class BiomePainter {
    private BiomePainter() {
    }

    public static void paint(ServerLevel level, BlockPos pos, ResourceKey<Biome> biome) {
        Holder<Biome> holder = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biome);
        paint(level, pos, holder);
    }

    public static void paint(ServerLevel level, BlockPos pos, Holder<Biome> biome) {
        if (!level.hasChunkAt(pos)) return;
        LevelChunk chunk = level.getChunkAt(pos);
        int qx = QuartPos.fromBlock(pos.getX()), qz = QuartPos.fromBlock(pos.getZ());
        if (chunk.getNoiseBiome(qx, QuartPos.fromBlock(pos.getY()), qz).is(biome)) return;
        chunk.fillBiomesFromNoise((x, y, z, sampler) -> x == qx && z == qz ? biome : chunk.getNoiseBiome(x, y, z),
                level.getChunkSource().randomState().sampler());
        chunk.markUnsaved();
        level.getChunkSource().chunkMap.resendBiomesForChunks(List.<ChunkAccess>of(chunk));
    }

    public static boolean is(ServerLevel level, BlockPos pos, ResourceKey<Biome> biome) {
        return level.getBiome(pos).is(biome);
    }
}
