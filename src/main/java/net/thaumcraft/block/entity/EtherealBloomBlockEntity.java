package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCSounds;

/**
 * O {@code TileEtherealBloom} da 4.2.3.5. A cada segundo sorteia uma coluna a até sete blocos (dentro de um raio de
 * nove) e, se ela é de Terra Maculada, de Mata Assombrada ou de Floresta Mágica, devolve a ela o bioma que o gerador
 * do mundo daria ali (a Terra Maculada natural vira planície). É só isso: a mácula fora do bioma dela é que definha
 * sozinha. O {@code growthCounter} conta desde que a flor apareceu (ou desde que o mundo a carregou, já que o original
 * não o guarda) e faz caule, folhas e cristal crescerem; o {@code counter} começa num ponto sorteado e escolhe o quadro
 * do brilho.
 */
public class EtherealBloomBlockEntity extends BlockEntity {
    public int counter;
    public int growthCounter;

    public EtherealBloomBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ETHEREAL_BLOOM, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EtherealBloomBlockEntity bloom) {
        if (bloom.counter == 0) bloom.counter = level.getRandom().nextInt(100);
        bloom.counter++;
        if (level instanceof net.minecraft.server.level.ServerLevel server && bloom.counter % 20 == 0) restore(server, pos);
        // o som das raízes brotando, só de quem vê, no primeiro tique
        if (level.isClientSide() && bloom.growthCounter == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.ROOTS.value(),
                    SoundSource.BLOCKS, 1.0f, 0.6f, false);
        }
        bloom.growthCounter++;
    }

    private static void restore(net.minecraft.server.level.ServerLevel level, BlockPos pos) {
        var random = level.getRandom();
        int x = random.nextInt(8) - random.nextInt(8);
        int z = random.nextInt(8) - random.nextInt(8);
        BlockPos at = pos.offset(x, 0, z);
        var here = level.getBiome(at);
        if (!here.is(net.thaumcraft.world.TCBiomes.TAINTED_LAND) && !here.is(net.thaumcraft.world.TCBiomes.EERIE)
                && !here.is(net.thaumcraft.world.TCBiomes.MAGICAL_FOREST)) return;
        if (at.distToCenterSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 81.0) return;
        var source = level.getChunkSource();
        var natural = source.getGenerator().getBiomeSource().getNoiseBiome(net.minecraft.core.QuartPos.fromBlock(at.getX()),
                net.minecraft.core.QuartPos.fromBlock(at.getY()), net.minecraft.core.QuartPos.fromBlock(at.getZ()),
                source.randomState().sampler());
        if (natural.is(net.thaumcraft.world.TCBiomes.TAINTED_LAND)) {
            net.thaumcraft.world.BiomePainter.paint(level, at, net.minecraft.world.level.biome.Biomes.PLAINS);
        } else {
            net.thaumcraft.world.BiomePainter.paint(level, at, natural);
        }
    }
}
