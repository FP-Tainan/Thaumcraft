package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * O <b>Globo de Luz</b>: o {@code BlockGlowGlobe} do Witchery.
 *
 * <p>Uma bolinha de luz no ar, do tamanho de dois pixels, que <b>ilumina como uma tocha e meia</b> e larga
 * uma chama de vez em quando. Não estorva a passagem, não se parte com a mão, <b>não cai de nada</b> e
 * <b>não se pode apanhar</b> — nem com o clique do meio.
 *
 * <p>Ela não é um bloco que se põe: é o que o <b>símbolo da luz</b> deixa onde ele foi lançado. Por isso não
 * tem item e não está na aba do criativo, exatamente como no original.
 *
 * <p>É também a coisa mais barata de quebrar do mod inteiro: dureza zero. Quem a quiser fora dali tira-a com
 * a mão, e ela não deixa nada.
 */
public class GlowGlobeBlock extends Block {
    public static final MapCodec<GlowGlobeBlock> CODEC = simpleCodec(GlowGlobeBlock::new);

    /** O quanto ela acende: os quinze décimos e meio do original, que dão quinze. */
    public static final int ACENDE = 15;

    /** De quantas em quantas batidas de desenho ela larga uma chama: duas em três. */
    public static final int CHAMA_UMA_EM = 3;

    /** O tamanho dela: dois pixels no meio da casa. */
    private static final VoxelShape FORMA = Block.box(6.4, 6.4, 6.4, 9.6, 9.6, 9.6);

    public GlowGlobeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                  CollisionContext quem) {
        return FORMA;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                           CollisionContext quem) {
        return Shapes.empty();
    }

    /**
     * <b>A chama que ela larga</b>, duas vezes em três.
     *
     * <p>O original sorteia o lugar dela numa grade curta: três lugares em cada lado e <b>quatro em
     * altura</b>, de modo que a chama sobe mais do que se espalha. É o que dá à bolinha o seu tremeluzir.
     */
    @Override
    public void animateTick(BlockState feitio, Level level, BlockPos onde, RandomSource sorte) {
        if (sorte.nextInt(CHAMA_UMA_EM) == 0) return;
        level.addParticle(ParticleTypes.FLAME,
                onde.getX() + 0.45 + sorte.nextInt(3) * 0.05,
                onde.getY() + 0.4 + sorte.nextInt(4) * 0.1,
                onde.getZ() + 0.45 + sorte.nextInt(3) * 0.05,
                0.0, 0.0, 0.0);
    }

    /** E não se apanha com o clique do meio: o original devolve nada. */
    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos onde, BlockState feitio,
                                          boolean comAlma) {
        return ItemStack.EMPTY;
    }
}
