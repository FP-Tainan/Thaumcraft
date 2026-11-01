package net.thaumcraft.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.thaumcraft.arcana.ArcanaBlocks;

/**
 * As flores do Ars Magica 2: o {@code generateFlowers} e o {@code AM2FlowerGen} da 1.4.0.009.
 *
 * <p>Uma tentativa por flor e por pedaço de mundo, e cada tentativa é uma <b>moita de oito</b>: sorteia-se
 * um ponto no pedaço, a uma altura qualquer entre zero e 128, e dali tentam-se oito lugares espalhados
 * oito casas para os lados e quatro para cima e para baixo. Onde houver ar e chão que sirva, nasce uma.
 *
 * <p>É por isso que estas flores aparecem em <b>grupos pequenos</b> e em qualquer altura — numa caverna,
 * numa encosta, no meio de um campo. O original nunca as leva à superfície de propósito.
 *
 * <p>Nesta fatia entram duas: a <b>Cerublossom</b> e a <b>Nova do Deserto</b>, que são as duas que a Chave
 * de Cristal pede. A Flor-de-despertar e a Raiz de Tarma vêm com o Refinador de Essência, que é quem as
 * usa.
 */
public class ArcanaFlowersFeature extends Feature<NoneFeatureConfiguration> {
    /** Quantos lugares cada moita tenta. */
    public static final int TRIES = 8;
    /** O quanto a moita se espalha para os lados e para cima. */
    public static final int SPREAD_XZ = 8;
    public static final int SPREAD_Y = 4;
    /** Até que altura o original sorteia o ponto de partida. */
    public static final int MAX_Y = 128;

    public ArcanaFlowersFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        moita(context, ArcanaBlocks.CERUBLOSSOM);
        moita(context, ArcanaBlocks.DESERT_NOVA);
        return true;
    }

    private static void moita(FeaturePlaceContext<NoneFeatureConfiguration> context, Block qual) {
        WorldGenLevel level = context.level();
        RandomSource sorte = context.random();
        BlockPos origem = context.origin();
        int x = (origem.getX() & ~15) + sorte.nextInt(16) + 8;
        int y = sorte.nextInt(MAX_Y);
        int z = (origem.getZ() & ~15) + sorte.nextInt(16) + 8;
        BlockState flor = qual.defaultBlockState();

        for (int volta = 0; volta < TRIES; volta++) {
            BlockPos onde = new BlockPos(
                    x + sorte.nextInt(SPREAD_XZ) - sorte.nextInt(SPREAD_XZ),
                    y + sorte.nextInt(SPREAD_Y) - sorte.nextInt(SPREAD_Y),
                    z + sorte.nextInt(SPREAD_XZ) - sorte.nextInt(SPREAD_XZ));
            if (level.isOutsideBuildHeight(onde)) continue;
            if (!level.getBlockState(onde).isAir()) continue;
            if (!flor.canSurvive(level, onde)) continue;
            level.setBlock(onde, flor, 2);
        }
    }
}
