package net.thaumcraft.mortuorum;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

/**
 * O Muro de Caveiras: o {@code BlockSkullWall} do Necromancy — um bloco com a cara da obsidiana, duro como ela e
 * do qual sai uma caveira de esqueleto quando se quebra.
 *
 * <p>No original ele nunca é posto por nada: está registado e aparece na aba, e nada o constrói nem o faz cair. É
 * assim que ele vem para cá.
 */
public class SkullWallBlock extends Block {
    public static final MapCodec<SkullWallBlock> CODEC = simpleCodec(SkullWallBlock::new);

    public SkullWallBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
