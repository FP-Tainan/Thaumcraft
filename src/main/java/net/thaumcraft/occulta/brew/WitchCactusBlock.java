package net.thaumcraft.occulta.brew;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>cato de bruxa</b>: o {@code BlockWitchCactus} do Witchery.
 *
 * <p>É o cato do jogo com três diferenças, e as três contam. Ele <b>não larga nada</b>, nem a si mesmo.
 * Ele <b>não cresce sozinho</b> — quem o faz crescer é o cozimento de espinhos, e mais nada. E ele
 * <b>aceita qualquer chão</b>: não precisa de areia, não se importa com o que está ao lado dele, e não morre
 * por ter vizinhos. Por isso ele cresce onde o cato do jogo nunca cresceria — numa calçada, dentro de casa,
 * no meio de uma ponte.
 *
 * <p>Isso é de propósito: ele é uma <b>armadilha</b> que se planta, não uma planta que se cultiva. O que lhe
 * resta do cato do jogo é o que importa — ele é um pouco mais estreito que o bloco, e <b>espeta</b> quem
 * encosta.
 *
 * <p>Ele cai se lhe tirarem o chão, e isso basta: uma coluna inteira desaba de uma vez.
 */
public class WitchCactusBlock extends Block {
    public static final MapCodec<WitchCactusBlock> CODEC = simpleCodec(WitchCactusBlock::new);

    /** O quanto ele é mais estreito que o bloco: uma casa de dezesseis de cada lado, como o do jogo. */
    private static final int BEIRA = 1;

    /** O que ele espeta, que é o mínimo: meio coração. */
    public static final float ESPETA = 1.0f;

    /** A caixa de esbarrar, que tem o <b>topo rebaixado</b> — é por isso que se anda em cima dele. */
    private static final VoxelShape CHOQUE = Block.box(BEIRA, 0.0, BEIRA, 16 - BEIRA, 16 - BEIRA, 16 - BEIRA);

    /** E a de ver, que vai até em cima. */
    private static final VoxelShape FORMA = Block.box(BEIRA, 0.0, BEIRA, 16 - BEIRA, 16.0, 16 - BEIRA);

    public WitchCactusBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                           CollisionContext contexto) {
        return CHOQUE;
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                  CollisionContext contexto) {
        return FORMA;
    }

    /**
     * <b>Qualquer chão serve</b>, desde que seja chão: o {@code !isReplaceableBlock} do original, que não
     * pergunta se o bloco é sólido — pergunta só se ele se <b>deixa empurrar</b>. Ar, água, fogo e mato se
     * deixam; o resto não. É por isso que um cato fica de pé em cima de outro, e cai no ar.
     */
    @Override
    protected boolean canSurvive(BlockState feitio, LevelReader level, BlockPos onde) {
        return !level.getBlockState(onde.below()).canBeReplaced();
    }

    @Override
    protected void neighborChanged(BlockState feitio, Level level, BlockPos onde, Block qual,
                                   @Nullable Orientation lado, boolean mexeu) {
        if (!this.canSurvive(feitio, level, onde)) level.destroyBlock(onde, true);
    }

    @Override
    protected void entityInside(BlockState feitio, Level level, BlockPos onde, Entity quem,
                                InsideBlockEffectApplier applier, boolean dentroMesmo) {
        quem.hurt(level.damageSources().cactus(), ESPETA);
    }
}
