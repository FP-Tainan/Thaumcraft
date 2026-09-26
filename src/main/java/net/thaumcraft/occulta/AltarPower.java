package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * De onde o Altar tira o poder dele: o {@code updatePower} do {@code BlockAltar} do Witchery.
 *
 * <p>O altar olha um cubo de <b>vinte e nove por vinte e nove por vinte e nove</b> à volta do bloco que manda, e
 * conta o que há de natureza lá dentro. Cada coisa vale um tanto e só conta até um tanto: a folhagem vale três e
 * conta até cem, a grama vale dois e conta até oitenta, a flor vale quatro mas só conta até trinta. Somado, dá o
 * <b>teto</b> de poder daquele altar.
 *
 * <p>Os números são os do original, um por um. O que lá era um bloco só passa aqui à marca que reúne os do mesmo
 * tipo — as mudas, as toras, a folhagem, as flores —, que é como o jogo de hoje agrupa.
 *
 * <p><b>Do original ficam de fora</b> as plantas do ramo que ainda não foram portadas (o musgo-de-brasa, o
 * musgo-espanhol, a erva-brilhante, a armadilha-de-bichos, a rosa-de-sangue, o agarra-mato, o algodão-de-névoa, o
 * Coração de Demônio e o Ovo do Infinito) e a lista que o arquivo de ajustes deixava quem joga acrescentar.
 */
public final class AltarPower {
    /** A marca das mudas, que o jogo tem nos dados mas não num campo. */
    private static final net.minecraft.tags.TagKey<Block> SAPLINGS = net.minecraft.tags.TagKey.create(
            net.minecraft.core.registries.Registries.BLOCK,
            net.minecraft.resources.Identifier.withDefaultNamespace("saplings"));

    /** O alcance do olhar do altar: catorze para cada lado. */
    public static final int SCAN = 14;

    /** Uma fonte de poder: o que ela é, quanto vale cada uma e quantas contam. */
    public record Source(Predicate<BlockState> what, int factor, int limit) {
    }

    private static final List<Source> SOURCES = new ArrayList<>();

    private AltarPower() {
    }

    private static void source(Predicate<BlockState> what, int factor, int limit) {
        SOURCES.add(new Source(what, factor, limit));
    }

    static {
        // o que o original conta por marca do dicionário de minérios: mudas, toras e folhagem
        source(state -> state.is(SAPLINGS), 4, 20);
        source(state -> state.is(BlockTags.LOGS), 2, 50);
        source(state -> state.is(BlockTags.LEAVES), 3, 100);

        // e o que ele conta bloco a bloco
        source(state -> state.is(Blocks.GRASS_BLOCK), 2, 80);
        source(state -> state.is(Blocks.DIRT), 1, 80);
        source(state -> state.is(Blocks.FARMLAND), 1, 100);
        source(state -> state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN)
                || state.is(Blocks.TALL_GRASS) || state.is(Blocks.LARGE_FERN), 3, 50);
        source(state -> state.is(BlockTags.SMALL_FLOWERS), 4, 30);
        source(state -> state.is(Blocks.WHEAT), 4, 20);
        source(state -> state.is(Blocks.WATER), 1, 50);
        source(state -> state.is(Blocks.RED_MUSHROOM), 3, 20);
        source(state -> state.is(Blocks.BROWN_MUSHROOM), 3, 20);
        source(state -> state.is(Blocks.CACTUS), 3, 50);
        source(state -> state.is(Blocks.SUGAR_CANE), 3, 50);
        source(state -> state.is(Blocks.PUMPKIN) || state.is(Blocks.CARVED_PUMPKIN)
                || state.is(Blocks.JACK_O_LANTERN), 4, 20);
        source(state -> state.is(Blocks.PUMPKIN_STEM) || state.is(Blocks.ATTACHED_PUMPKIN_STEM), 3, 20);
        source(state -> state.is(Blocks.BROWN_MUSHROOM_BLOCK) || state.is(Blocks.RED_MUSHROOM_BLOCK), 3, 20);
        source(state -> state.is(Blocks.MELON), 4, 20);
        source(state -> state.is(Blocks.MELON_STEM) || state.is(Blocks.ATTACHED_MELON_STEM), 3, 20);
        source(state -> state.is(Blocks.VINE), 2, 50);
        source(state -> state.is(Blocks.LILY_PAD), 1, 80);
        source(state -> state.is(Blocks.COCOA), 3, 20);
        source(state -> state.is(Blocks.CARROTS), 4, 20);
        source(state -> state.is(Blocks.POTATOES), 4, 20);
        source(state -> state.is(Blocks.DRAGON_EGG), 250, 1);

        // as plantas do ofício valem como as do original
        source(state -> state.is(OccultaBlocks.BELLADONNA) || state.is(OccultaBlocks.MANDRAKE)
                || state.is(OccultaBlocks.WATER_ARTICHOKE) || state.is(OccultaBlocks.SNOWBELL), 4, 20);

        // e, por último, qualquer outra planta ou plantação, que no original vale dois e conta até quatro
        source(state -> state.is(BlockTags.CROPS) || state.is(BlockTags.FLOWERS), 2, 4);
    }

    /** As fontes, para quem quiser conferir a tabela. */
    public static List<Source> sources() {
        return List.copyOf(SOURCES);
    }

    /**
     * O teto de poder de um altar naquele lugar: conta tudo o que há à volta e soma o que cada coisa vale.
     *
     * <p>Cada bloco conta <b>uma vez só</b>, na primeira fonte que o reconhecer — é o que o original faz, onde a
     * tabela é uma tabela de blocos e cada bloco só tem um lugar nela.
     */
    public static float maxPower(ServerLevel level, BlockPos centro) {
        int[] contagem = new int[SOURCES.size()];
        BlockPos.MutableBlockPos onde = new BlockPos.MutableBlockPos();
        for (int y = centro.getY() - SCAN; y <= centro.getY() + SCAN; y++) {
            if (y < level.getMinY() || y > level.getMaxY()) continue;
            for (int z = centro.getZ() - SCAN; z <= centro.getZ() + SCAN; z++) {
                for (int x = centro.getX() - SCAN; x <= centro.getX() + SCAN; x++) {
                    BlockState qual = level.getBlockState(onde.set(x, y, z));
                    if (qual.isAir()) continue;
                    for (int fonte = 0; fonte < SOURCES.size(); fonte++) {
                        if (SOURCES.get(fonte).what().test(qual)) {
                            contagem[fonte]++;
                            break;
                        }
                    }
                }
            }
        }
        float soma = 0.0f;
        for (int fonte = 0; fonte < SOURCES.size(); fonte++) {
            Source source = SOURCES.get(fonte);
            soma += Math.min(contagem[fonte], source.limit()) * source.factor();
        }
        return soma;
    }

    /** O que um bloco vale, para quem quiser saber de um só. */
    public static int worth(Block block) {
        BlockState state = block.defaultBlockState();
        for (Source source : SOURCES) {
            if (source.what().test(state)) return source.factor();
        }
        return 0;
    }
}
