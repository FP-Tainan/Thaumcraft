package net.thaumcraft.block.eldritch;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.RenderShape;

/**
 * O corpo do obelisco eldritch acima da base: os números 4 a 7 que o {@code BlockEldritch} põe em cima do obelisco.
 *
 * <p>Quem desenha o obelisco inteiro é a entidade de bloco da base, então este aqui é só pedra invisível — e, ao
 * contrário da base, <b>não</b> tem entidade de bloco nenhuma. (Ele já foi irmão da base no código; como a base tem
 * entidade e ele não, o jogo de hoje reclamava de entidade órfã toda vez que o pedaço do mundo era lido.) Quebrado,
 * leva o anel junto, como as outras peças.
 */
public class EldritchObeliskTopBlock extends Block implements EldritchRingPiece {
    public static final MapCodec<EldritchObeliskTopBlock> CODEC = simpleCodec(EldritchObeliskTopBlock::new);

    public EldritchObeliskTopBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
        EldritchRingPiece.breakRing(level, pos);
    }
}
