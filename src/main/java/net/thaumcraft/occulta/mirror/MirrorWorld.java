package net.thaumcraft.occulta.mirror;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.dimension.DimensionType;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.world.DynamicDimensions;
import org.jetbrains.annotations.Nullable;

/**
 * O Mundo do Espelho: o {@code WorldProviderMirror} do Witchery, e a conta das celas dele.
 *
 * <p>É uma colmeia sem fim de celas de nove de lado, uma por pedaço de dezesseis, e nenhuma se liga à outra. Cada
 * espelho que alguém faz ganha <b>a sua</b>, com um espelho selado na parede de dentro que aponta de volta para
 * ele — e é por esse par que se vai e se volta.
 *
 * <p>A cela de cada espelho sai da <b>caracol</b> do original: anda-se a grelha de celas em volta do zero, de cela
 * em cela, e fica-se na primeira que estiver limpa de ponta a ponta. A conta é a mesma, passo por passo.
 *
 * <p><b>Desvio declarado:</b> no original o mundo do espelho é um número de dimensão posto no arquivo de ajustes,
 * e a altura dele encolhe para metade se quem manda o servidor quiser. Aqui é um mundo aberto na hora — o mesmo
 * {@link DynamicDimensions} dos bolsos das Portas Dimensionais — e a altura é sempre a inteira, quinze andares de
 * cela. O ajuste de encolher não tem para onde ir num mod sem arquivo de ajustes.
 */
public final class MirrorWorld {
    /** O mundo do espelho. */
    public static final ResourceKey<Level> LEVEL = DynamicDimensions.key("mirror");

    /** E o feitio dele: escuro, sem céu, sem chuva e com a hora parada. */
    public static final ResourceKey<DimensionType> TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, Thaumcraft.id("mirror"));

    /** Quantos andares de cela a grelha tem: os quinze do original. */
    public static final int LEVELS = 15;

    /** Quantas voltas da caracol se andam à procura de cela limpa. */
    public static final int RINGS = 256;

    /** O poder que custa passar de uma cela para a outra: os três mil do original. */
    public static final float HOP_POWER = 3000.0f;

    private MirrorWorld() {
    }

    /** Se aquele mundo é o do espelho. */
    public static boolean is(Level level) {
        return level.dimension() == LEVEL;
    }

    /** O mundo do espelho, abrindo-o se ainda não houver. */
    public static @Nullable ServerLevel level(MinecraftServer server) {
        var biomas = new net.minecraft.world.level.biome.FixedBiomeSource(
                server.registryAccess().lookupOrThrow(Registries.BIOME)
                        .getOrThrow(net.minecraft.world.level.biome.Biomes.THE_VOID));
        return DynamicDimensions.getOrCreate(server, LEVEL, TYPE, new MirrorChunkGenerator(biomas));
    }

    /**
     * Abre a cela daquele espelho e devolve onde ficou a metade de cima do espelho selado dela.
     *
     * <p>É o {@code getDimCoords} do original: a caracol anda a grelha de celas, e a primeira cela cujo vão
     * estiver <b>todo</b> vazio fica sendo esta. O espelho selado nasce na parede de dentro, virado para leste, e
     * guarda de onde veio — mundo e lugar — para saber o caminho de volta.
     */
    public static @Nullable BlockPos claimCell(ServerLevel de, BlockPos espelho) {
        ServerLevel mundo = level(de.getServer());
        if (mundo == null) return null;

        int celaX = 0;
        int celaZ = 0;
        int sinal = 1;
        for (int volta = 0; volta < RINGS; volta++) {
            for (int giro = 0; giro <= volta; giro++) {
                for (int eixo = 0; eixo < 2; eixo++) {
                    if (volta > 0) {
                        if (eixo == 0) celaZ += sinal;
                        else celaX += sinal;
                    }
                    for (int celaY = 0; celaY < LEVELS; celaY++) {
                        BlockPos onde = new BlockPos((celaX << 4) + 4, (celaY << 4) + 8, (celaZ << 4) + 8);
                        if (!clear(mundo, onde)) continue;
                        place(mundo, onde, de.dimension(), espelho);
                        return onde;
                    }
                }
            }
            sinal *= -1;
        }
        return null;
    }

    /**
     * Se o vão daquela cela está todo vazio: de uma casa abaixo do espelho a seis acima, e os nove de lado.
     *
     * <p>É a conta do original letra por letra — e como o gerador do mundo já fez as celas vazias, o que ela
     * apanha é <b>cela tomada</b>: ou tem um espelho selado, ou alguém pôs alguma coisa lá dentro.
     */
    private static boolean clear(ServerLevel mundo, BlockPos onde) {
        if (!mundo.isEmptyBlock(onde) || !mundo.isEmptyBlock(onde.below())) return false;
        for (int y = onde.getY() - 1; y <= onde.getY() + 6; y++) {
            for (int x = onde.getX(); x <= onde.getX() + 8; x++) {
                for (int z = onde.getZ() - 4; z <= onde.getZ() + 4; z++) {
                    if (!mundo.isEmptyBlock(new BlockPos(x, y, z))) return false;
                }
            }
        }
        return true;
    }

    /**
     * Põe o espelho selado da cela, com a ligação de volta já dentro dele.
     *
     * <p>Ele nasce <b>sem</b> a marca de ligado, como no original: essa só se põe quando alguém assenta o espelho
     * de novo a partir do item, e serve para o espelho da cela saber que o outro lado mudou de lugar.
     */
    private static void place(ServerLevel mundo, BlockPos onde, ResourceKey<Level> deMundo, BlockPos deOnde) {
        var selado = OccultaBlocks.SEALED_WITCH_MIRROR.defaultBlockState()
                .setValue(MirrorBlock.FACING, Direction.EAST);
        mundo.setBlock(onde, selado.setValue(MirrorBlock.HALF, DoubleBlockHalf.UPPER), 3);
        mundo.setBlock(onde.below(), selado.setValue(MirrorBlock.HALF, DoubleBlockHalf.LOWER), 3);
        if (mundo.getBlockEntity(onde) instanceof MirrorBlockEntity alma) {
            alma.linkTo(new MirrorLink(deMundo, deOnde));
        }
    }

    /**
     * Se naquele lugar do mundo do espelho há uma cela de alguém: o {@code isEntryCell} do original.
     *
     * <p>Olha-se o canto onde um espelho selado estaria, e se ele está lá a cela é de alguém.
     */
    public static boolean claimed(ServerLevel mundo, BlockPos dentro) {
        return mundo.getBlockState(cellMirror(dentro)).is(OccultaBlocks.SEALED_WITCH_MIRROR);
    }

    /** Onde fica a metade de cima do espelho selado da cela daquele lugar. */
    public static BlockPos cellMirror(BlockPos dentro) {
        return new BlockPos((dentro.getX() >> 4 << 4) + 4, (dentro.getY() >> 4 << 4) + 8,
                (dentro.getZ() >> 4 << 4) + 8);
    }

    /** E o meio da cela, que é onde o Reflexo acorda: quatro casas adiante do espelho selado. */
    public static BlockPos cellMiddle(BlockPos espelhoDaCela) {
        return espelhoDaCela.offset(4, 0, 0);
    }
}
