package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.world.TreeLeaves;

/**
 * A sorveira: o {@code WorldGenWitchTree} do Witchery, que é o carvalho pequeno do jogo antigo com uma copa um
 * pouco mais larga.
 *
 * <p>Cinco a sete de altura, tronco de um, e a copa nas três últimas fileiras — a de baixo mais larga, e os
 * cantos caindo na sorte, que é o que dá à copa o recorte irregular.
 *
 * <p><b>Do original fica de fora</b> o ramo das trepadeiras e dos cogumelos-do-tronco: o {@code growVines} só
 * corre quando o gerador é criado com {@code growVines} ligado, e a sorveira — a única árvore que usa este
 * gerador — é criada com ele desligado. O código está lá, mas nunca corre.
 */
public final class WitchTree {
    /** A altura vai de cinco a sete, e a copa sai um bloco mais larga que a do carvalho. */
    public static final int MIN_HEIGHT = 5;
    public static final int SPREAD = 1;

    private WitchTree() {
    }

    /**
     * Faz a árvore, se couber.
     *
     * @param worldgen se é o mundo nascendo (sem avisar vizinhos) ou uma muda crescendo
     */
    public static boolean generate(LevelAccessor level, RandomSource random, BlockPos pos, boolean worldgen) {
        int flags = worldgen ? Block.UPDATE_CLIENTS : Block.UPDATE_ALL;
        int altura = random.nextInt(3) + MIN_HEIGHT;
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();

        if (y < level.getMinY() + 1 || y + altura + 1 > level.getMaxY()) return false;

        // o espaço: nada no caminho, salvo folha, terra, grama e madeira
        for (int altura1 = y; altura1 <= y + 1 + altura; altura1++) {
            int raio = altura1 == y ? 0 : altura1 >= y + 1 + altura - 2 ? 2 : 1;
            for (int cx = x - raio; cx <= x + raio; cx++) {
                for (int cz = z - raio; cz <= z + raio; cz++) {
                    BlockState qual = level.getBlockState(new BlockPos(cx, altura1, cz));
                    if (!qual.isAir() && !qual.is(net.minecraft.tags.BlockTags.LEAVES)
                            && !qual.is(net.minecraft.tags.BlockTags.DIRT)
                            && !qual.is(net.minecraft.tags.BlockTags.LOGS)) {
                        return false;
                    }
                }
            }
        }

        if (!TreeLeaves.isSoil(level.getBlockState(new BlockPos(x, y - 1, z)))) return false;

        TreeLeaves folhas = new TreeLeaves();
        BlockState folha = OccultaBlocks.ROWAN_LEAVES.defaultBlockState();
        BlockState tora = OccultaBlocks.ROWAN_LOG.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.Y);

        // a copa: as três últimas fileiras, mais uma acima do topo do tronco
        for (int altura1 = y - 3 + altura; altura1 <= y + altura; altura1++) {
            int acima = altura1 - (y + altura);
            int raio = 1 - acima / 2 + SPREAD;
            for (int cx = x - raio; cx <= x + raio; cx++) {
                int dx = cx - x;
                for (int cz = z - raio; cz <= z + raio; cz++) {
                    int dz = cz - z;
                    // os cantos da copa caem na sorte, menos na fileira do topo
                    if (Math.abs(dx) == raio && Math.abs(dz) == raio && (random.nextInt(2) == 0 || acima == 0)) {
                        continue;
                    }
                    BlockPos onde = new BlockPos(cx, altura1, cz);
                    BlockState qual = level.getBlockState(onde);
                    if (qual.isAir() || qual.is(net.minecraft.tags.BlockTags.LEAVES)) {
                        folhas.place(level, onde, folha, flags);
                    }
                }
            }
        }

        // e o tronco
        for (int passo = 0; passo < altura; passo++) {
            BlockPos onde = new BlockPos(x, y + passo, z);
            BlockState qual = level.getBlockState(onde);
            if (qual.isAir() || qual.getBlock() instanceof LeavesBlock) {
                level.setBlock(onde, tora, flags);
                folhas.forget(onde);
            }
        }

        folhas.settle(level, flags);
        return true;
    }
}
