package net.thaumcraft.arcana.machine;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.VegetationBlock;

/**
 * Uma flor do Ars Magica 2: o {@code AMFlower} da 1.4.0.009.
 *
 * <p>Lá ela herda a flor do jogo e muda só o desenho; aqui faz o mesmo. O que cada uma tem de seu — onde
 * nasce, se acende, o que faz — está em quem a registra ou em quem a herda.
 */
public class ArcanaFlowerBlock extends VegetationBlock {
    public static final MapCodec<ArcanaFlowerBlock> CODEC = simpleCodec(ArcanaFlowerBlock::new);

    public ArcanaFlowerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }
}
