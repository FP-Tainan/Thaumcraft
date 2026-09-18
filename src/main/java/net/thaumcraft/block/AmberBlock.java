package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * O bloco e os tijolos de âmbar: os metadados 0 e 1 do {@code BlockCosmeticOpaque} da 4.2.3.5 — translúcidos, e
 * deixando passar a luz com a perda de três ({@code getLightOpacity}).
 */
public class AmberBlock extends Block {
    public static final MapCodec<AmberBlock> CODEC = simpleCodec(AmberBlock::new);

    public AmberBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return 3;
    }
}
