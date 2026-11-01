package net.thaumcraft.arcana.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A <b>Nova do Deserto</b>: o {@code BlockDesertNova} da 1.4.0.009.
 *
 * <p>Uma flor que só nasce na <b>areia</b>, e que de vez em quando abre um <b>anel</b> de luz à volta de
 * si, de três casas de raio. É um dos dois ingredientes raros da Chave de Cristal.
 *
 * <p><b>Desvio declarado:</b> no original o anel é feito de partículas próprias do Ars Magica 2, que se
 * expandem e se recolhem num controlador de partícula. Aqui o anel é desenhado com a faísca do jogo, no
 * mesmo passo e no mesmo raio — a figura é a mesma, a partícula é a que há.
 */
public class DesertNovaBlock extends VegetationBlock {
    /** Uma em dez batidas de desenho abre o anel. */
    public static final int CHANCE = 10;
    /** O raio dele. */
    public static final double RADIUS = 3.0;
    /** E de quantos em quantos graus nasce uma faísca: é o {@code gfxLevel × 15} do original, no nível dois. */
    public static final int STEP = 30;

    public static final com.mojang.serialization.MapCodec<DesertNovaBlock> CODEC = simpleCodec(DesertNovaBlock::new);

    public DesertNovaBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    /** Só em areia: é o {@code EnumPlantType.Desert} e o {@code canPlaceBlockOn} do original. */
    @Override
    protected boolean mayPlaceOn(BlockState chão, net.minecraft.world.level.BlockGetter nível, BlockPos onde) {
        return chão.is(BlockTags.SAND);
    }

    @Override
    protected boolean canSurvive(BlockState feitio, LevelReader nível, BlockPos onde) {
        return this.mayPlaceOn(nível.getBlockState(onde.below()), nível, onde.below());
    }

    @Override
    public void animateTick(BlockState feitio, Level nível, BlockPos onde, RandomSource sorte) {
        if (sorte.nextInt(CHANCE) != 0) return;
        double y = onde.getY() + 0.6 + sorte.nextFloat() * 0.2f;
        for (int grau = 0; grau < 360; grau += STEP) {
            // o original usa o cosseno e o seno do grau cru, sem o passar a radianos: o anel sai torto
            // de propósito, e com estes mesmos pontos
            double x = onde.getX() + 0.5 + Math.cos(grau) * RADIUS;
            double z = onde.getZ() + 0.5 + Math.sin(grau) * RADIUS;
            nível.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.0, 0.0);
        }
    }
}
