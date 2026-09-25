package net.thaumcraft.mortuorum;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

/**
 * O Muro de Caveiras: o {@code BlockSkullWall} do Necromancy — duro como obsidiana e do qual sai uma caveira de
 * esqueleto quando se quebra.
 *
 * <p>No original ele nunca é posto por nada: está registado e aparece na aba, e nada o constrói nem o faz cair. É
 * assim que ele vem para cá.
 *
 * <p><b>Desvio declarado:</b> lá o {@code registerBlockIcons} regista literalmente {@code "obsidian"}, e o bloco
 * fica com a cara da obsidiana — o que num bloco chamado Muro de Caveiras não diz nada a quem o vê. A pedido de
 * quem joga, a folha é de casa: quatro caveiras encaixadas em pedra escura, desenhadas por
 * {@code scratchpad/MuroCaveiras.java}.
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
