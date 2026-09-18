package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * As folhas das árvores mágicas: o {@code BlockMagicalLeaves} da 4.2.3.5, descompilado.
 *
 * <p>As da grande-madeira são verdes da cor da folhagem do lugar; as do pinheiro-de-prata são de um cinza
 * azulado fixo, brilham de leve (luz sete) e, uma vez em quinhentas, soltam uma faísca. Nenhuma das duas
 * deixa cair folha voando — isso é coisa do jogo novo, que o original não tinha. O que elas deixam, ao
 * apodrecer, é só a muda: uma em duzentas na grande-madeira, uma em duzentas e cinquenta no pinheiro.
 */
public class MagicalLeavesBlock extends LeavesBlock {
    /** Os efeitos do lado de quem vê, que o cliente pendura aqui ao abrir. */
    public interface ClientEffects {
        void sparkle(Level level, BlockPos pos, RandomSource random);
    }

    public static ClientEffects clientEffects = (level, pos, random) -> {
    };

    public static final MapCodec<MagicalLeavesBlock> CODEC = simpleCodec(properties -> new MagicalLeavesBlock(false, properties));
    public static final MapCodec<MagicalLeavesBlock> SILVER_CODEC = simpleCodec(properties -> new MagicalLeavesBlock(true, properties));

    /** O cinza azulado das folhas do pinheiro-de-prata: 8952234 no original. */
    public static final int SILVERWOOD_COLOR = 0x8898AA;

    private final boolean silverwood;

    public MagicalLeavesBlock(boolean silverwood, Properties properties) {
        super(0.0f, properties);
        this.silverwood = silverwood;
    }

    public boolean isSilverwood() {
        return this.silverwood;
    }

    @Override
    public MapCodec<? extends LeavesBlock> codec() {
        return this.silverwood ? SILVER_CODEC : CODEC;
    }

    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
        // o original não tinha folha caindo
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (this.silverwood && random.nextInt(500) == 0) clientEffects.sparkle(level, pos, random);
    }
}
