package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * O que o ofício <b>não mexe</b>: a {@code BlockProtect} do Witchery.
 *
 * <p>Tudo o que o mod quebra, levanta, afunda ou atira passa primeiro por aqui. A lista é curtíssima e é
 * inteira defensiva: <b>nada que tenha alma</b> — baú, forno, altar, qualquer coisa com dados dentro —,
 * nada de <b>rocha-mãe</b> e nada de <b>ovo de dragão</b>.
 *
 * <p>Repare no que falta nela: não há proteção de região, não há dono, não há permissão. O original confia
 * em quem joga e só se protege de <b>perder coisas</b>. Mexer num baú o apagaria com o que tem dentro, e é
 * disso que a lista trata.
 */
public final class BlockProtect {
    private BlockProtect() {
    }

    /** Se o ofício pode mexer naquele bloco. */
    public static boolean podeMexer(BlockState feitio) {
        if (feitio.hasBlockEntity()) return false;
        return !feitio.is(Blocks.BEDROCK) && !feitio.is(Blocks.DRAGON_EGG);
    }

    /** O mesmo, por lugar. */
    public static boolean podeMexer(Level level, BlockPos onde) {
        return podeMexer(level.getBlockState(onde));
    }
}
