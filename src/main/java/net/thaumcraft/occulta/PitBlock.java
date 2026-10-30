package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * O <b>chão falso</b>: a {@code BlockPitDirt} e a {@code BlockPitGrass} do Witchery.
 *
 * <p>É um bloco que <b>parece terra</b> — ou grama — e <b>não é chão nenhum</b>: quem pisa nele passa
 * através. Serve para uma coisa só, e serve bem: tapar um buraco.
 *
 * <h2>O que o original faz, e como o faz</h2>
 *
 * <p>Em 2014 isto é um <b>material inventado</b>: um material que responde <b>sim</b> a «você bloqueia o
 * movimento?» e <b>não</b> a «você é sólido?». O jogo de hoje separa as duas perguntas por si — a forma
 * de <b>desenho</b> e a de <b>choque</b> são duas coisas diferentes —, e por isso aqui ele é um bloco
 * cheio para desenhar e <b>vazio para esbarrar</b>.
 *
 * <p>Ele <b>larga-se a si mesmo</b> e parte com a facilidade do que imita. E <b>a luz passa por ele</b>,
 * como no original, que lhe dá opacidade zero — é por aí que se desconfia dele, se se souber olhar.
 *
 * <p><b>E ele não se põe sozinho:</b> no original ele sai de uma receita de caldeirão, e aqui também.
 */
public class PitBlock extends Block {
    public static final MapCodec<PitBlock> CODEC = simpleCodec(PitBlock::new);

    public PitBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    /** <b>Nada esbarra nele.</b> É tudo o que ele é. */
    @Override
    protected VoxelShape getCollisionShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                           CollisionContext contexto) {
        return Shapes.empty();
    }

    /**
     * E <b>o bicho que anda sozinho também não o vê como chão</b>: quem procura caminho passa por ele
     * como se fosse ar, que é o que ele é para os pés.
     */
    @Override
    protected boolean isPathfindable(BlockState feitio,
                                     net.minecraft.world.level.pathfinder.PathComputationType tipo) {
        return true;
    }
}
