package net.thaumcraft.occulta.curse;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.thaumcraft.occulta.OccultaBlocks;
import org.jetbrains.annotations.Nullable;

/**
 * <b>O cozimento que fica preso na maçaneta</b>: os blocos amaldiçoados do Witchery.
 *
 * <p>Um frasco de <b>gatilho</b> — o que leva uma cabeça de zumbi no caldeirão — não arrebenta em quem
 * acerta. Ele <b>fica</b>. Acertando um botão, uma alavanca, uma porta ou uma placa de pressão, troca a peça
 * por uma <b>gêmea amaldiçoada</b>, igual em tudo, e espera.
 *
 * <p>Quem mexer nela leva o cozimento inteiro na cara, e a peça volta a ser o que era.
 *
 * <p>É a armadilha mais limpa que este mod tem: ela não se vê. Um botão amaldiçoado é <b>exatamente</b> um
 * botão — mesmo desenho, mesma queda, mesmo barulho —, e a única maneira de saber é ter visto o frasco
 * bater nele.
 */
public final class CursedBlocks {
    private CursedBlocks() {
    }

    /**
     * A gêmea amaldiçoada desta peça, se ela tiver uma.
     *
     * <p>A lista é a do original: os dois botões, a alavanca, a porta de madeira e as três placas — de
     * madeira, de pedra e de <b>neve</b>, que é a do próprio mod.
     */
    public static @Nullable Block gêmea(BlockState oquê) {
        if (oquê.is(Blocks.STONE_BUTTON)) return OccultaBlocks.CURSED_STONE_BUTTON;
        if (oquê.is(Blocks.OAK_BUTTON)) return OccultaBlocks.CURSED_WOODEN_BUTTON;
        if (oquê.is(Blocks.LEVER)) return OccultaBlocks.CURSED_LEVER;
        if (oquê.is(Blocks.OAK_DOOR)) return OccultaBlocks.CURSED_WOODEN_DOOR;
        if (oquê.is(Blocks.OAK_PRESSURE_PLATE)) return OccultaBlocks.CURSED_WOODEN_PRESSURE_PLATE;
        if (oquê.is(Blocks.STONE_PRESSURE_PLATE)) return OccultaBlocks.CURSED_STONE_PRESSURE_PLATE;
        if (oquê.is(OccultaBlocks.SNOW_PRESSURE_PLATE)) return OccultaBlocks.CURSED_SNOW_PRESSURE_PLATE;
        return null;
    }

    /**
     * <b>Prende</b> este cozimento naquela peça.
     *
     * <p>Já estando amaldiçoada, o cozimento se <b>soma</b> ao que lá estava em vez de o trocar — e é por
     * isso que uma alavanca pode estar armada para três pessoas seguidas.
     *
     * @return se ficou presa
     */
    public static boolean prende(ServerLevel level, BlockPos onde, List<Item> dentro, String quem) {
        BlockPos casa = normaliza(level, onde);
        BlockState oquê = level.getBlockState(casa);

        if (level.getBlockEntity(casa) instanceof CursedBlockEntity já) {
            já.soma(dentro, quem);
            return true;
        }

        Block gêmea = gêmea(oquê);
        if (gêmea == null) return false;

        BlockState virou = gêmea.withPropertiesOf(oquê);
        level.setBlock(casa, virou, Block.UPDATE_ALL);
        if (oquê.hasProperty(DoorBlock.HALF)) {
            BlockState emCima = level.getBlockState(casa.above());
            if (emCima.is(oquê.getBlock())) {
                level.setBlock(casa.above(), gêmea.withPropertiesOf(emCima), Block.UPDATE_ALL);
            }
        }
        if (!(level.getBlockEntity(casa) instanceof CursedBlockEntity alma)) return false;
        alma.arma(dentro, quem);
        return true;
    }

    /** Uma porta tem duas casas e a maldição mora na de baixo. */
    public static BlockPos normaliza(ServerLevel level, BlockPos onde) {
        BlockState oquê = level.getBlockState(onde);
        if (oquê.hasProperty(DoorBlock.HALF)
                && oquê.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            return onde.below();
        }
        return onde;
    }

    /**
     * <b>Dispara</b> a maldição desta peça em quem mexeu nela, e volta a peça ao que era quando acabar.
     *
     * @return se havia maldição ali
     */
    public static boolean dispara(ServerLevel level, BlockPos onde, Entity quem, Block volta) {
        BlockPos casa = normaliza(level, onde);
        if (!(level.getBlockEntity(casa) instanceof CursedBlockEntity alma)) return false;
        if (alma.disparaEAcaba(level, quem)) return true;

        BlockState oquê = level.getBlockState(casa);
        level.setBlock(casa, volta.withPropertiesOf(oquê), Block.UPDATE_ALL);
        if (oquê.hasProperty(DoorBlock.HALF)) {
            BlockState emCima = level.getBlockState(casa.above());
            if (emCima.is(oquê.getBlock())) {
                level.setBlock(casa.above(), volta.withPropertiesOf(emCima), Block.UPDATE_ALL);
            }
        }
        return true;
    }

    /** Serve às provas: se esta peça tem uma maldição à espera. */
    public static boolean armada(ServerLevel level, BlockPos onde) {
        return level.getBlockEntity(normaliza(level, onde)) instanceof CursedBlockEntity alma
                && alma.cargas() > 0;
    }

    /** E o que ela tem presa. */
    public static @Nullable CursedBlockEntity oQueTem(ServerLevel level, BlockPos onde) {
        return level.getBlockEntity(normaliza(level, onde)) instanceof CursedBlockEntity alma ? alma : null;
    }
}
