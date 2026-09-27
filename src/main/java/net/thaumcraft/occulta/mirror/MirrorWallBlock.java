package net.thaumcraft.occulta.mirror;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A parede do Mundo do Espelho: o {@code BlockMirrorWall} do Witchery.
 *
 * <p>É o que forra as celas. Não se quebra, não se apanha, não larga nada — e é feita da mesma figura corrente do
 * cozimento, pintada de lilás, que é o que lhe dá o ar de água parada de pé.
 */
public class MirrorWallBlock extends Block {
    /** A cor com que ela se pinta: o {@code 13426175} do original. */
    public static final int TINT = 0xCCCCFF;

    public MirrorWallBlock(Properties properties) {
        super(properties);
    }

}
