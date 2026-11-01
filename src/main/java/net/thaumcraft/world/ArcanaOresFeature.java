package net.thaumcraft.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.thaumcraft.arcana.ArcanaBlocks;

import java.util.List;

/**
 * O {@code generateOverworld} e o {@code generateNether} do {@code AM2WorldDecorator} da 1.4.0.009, a parte
 * dos minérios.
 *
 * <p>Cada tentativa é um {@code WorldGenMinable} — um veio de até tantos blocos a partir de um ponto
 * sorteado dentro do pedaço de mundo:
 * <ul>
 *     <li><b>vinteum</b>: 6 tentativas de veios de 4, na pedra, entre as alturas 10 e 45;</li>
 *     <li><b>quimerita</b>: 8 tentativas de veios de 6, na pedra, entre 10 e 80;</li>
 *     <li><b>topázio azul</b>: 8 tentativas de veios de 6, na pedra, entre 10 e 80;</li>
 *     <li><b>pedra-do-sol</b>: 20 tentativas de veios de 3, <b>na lava</b>, entre 5 e 120 — e o original
 *     faz esta passagem <b>duas vezes</b>, uma no Nether e outra no mundo de cima.</li>
 * </ul>
 *
 * <p><b>A pedra-da-lua não nasce no chão.</b> O original não a gera em lado nenhum: ela cai do céu, atraída
 * pelo Atrator de Pedra-da-Lua.
 *
 * <p><b>Duas coisas que as alturas de 2014 fazem no mundo de hoje</b>, e que ficam como estão porque os
 * números são os do original. A primeira: aquele mundo começava no zero, e abaixo disso é ardósia, onde o
 * original nunca chegou — estes minérios ficam todos acima da ardósia. A segunda: a passagem da
 * pedra-do-sol no mundo de cima pede <b>lava</b> entre as alturas 5 e 120, e a lava do mundo de hoje mora
 * quase toda abaixo do zero; ela quase nunca pega, e é essa a raridade que o original tinha — a
 * pedra-do-sol é uma pedra do Nether que de vez em quando aparece numa poça de lava deste lado.
 */
public class ArcanaOresFeature extends Feature<NoneFeatureConfiguration> {
    private static final RuleTest PEDRA = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
    private static final RuleTest LAVA = new BlockMatchTest(Blocks.LAVA);

    public ArcanaOresFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        veio(context, ArcanaBlocks.ORES.get("vinteum"), PEDRA, 4, 6, 10, 45);
        veio(context, ArcanaBlocks.ORES.get("chimerite"), PEDRA, 6, 8, 10, 80);
        veio(context, ArcanaBlocks.ORES.get("blue_topaz"), PEDRA, 6, 8, 10, 80);
        veio(context, ArcanaBlocks.ORES.get("sunstone"), LAVA, 3, 20, 5, 120);
        return true;
    }

    /** O {@code generateOre} do original: tantas tentativas, cada uma num ponto sorteado entre duas alturas. */
    static void veio(FeaturePlaceContext<NoneFeatureConfiguration> context, net.minecraft.world.level.block.Block bloco,
                     RuleTest onde, int tamanho, int tentativas, int mínimo, int máximo) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origem = context.origin();
        int cx = origem.getX() & ~15, cz = origem.getZ() & ~15;
        BlockState estado = bloco.defaultBlockState();

        for (int i = 0; i < tentativas; i++) {
            BlockPos ponto = new BlockPos(cx + random.nextInt(16),
                    random.nextInt(máximo - mínimo) + mínimo,
                    cz + random.nextInt(16));
            Feature.ORE.place(new OreConfiguration(List.of(OreConfiguration.target(onde, estado)), tamanho),
                    level, context.chunkGenerator(), random, ponto);
        }
    }

    /** E o {@code generateNether}, que é uma linha só: a mesma passagem da pedra-do-sol. */
    public static class Nether extends Feature<NoneFeatureConfiguration> {
        public Nether(Codec<NoneFeatureConfiguration> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            veio(context, ArcanaBlocks.ORES.get("sunstone"), LAVA, 3, 20, 5, 120);
            return true;
        }
    }
}
