package net.thaumcraft.shattered;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * As fendas que já estavam lá.
 *
 * <p>A lore de quem joga diz que as fendas não se abrem — <i>sempre estiveram abertas</i>, e o que muda é quem as
 * consegue ver. É daqui que elas vêm: uma tentativa por pedaço, raras, num oco qualquer debaixo da terra.
 *
 * <p><b>Isto é do porte, e não do original:</b> lá as fendas nascem das masmorras e dos bolsos. Sem elas no mundo
 * de cima, porém, não haveria por onde começar — o Fio do Mundo vem do que a fenda come, e a Assinatura de Fenda
 * pede o fio. É o que fecha a corrente, e é o que a lore manda.
 */
public class RiftFeature extends Feature<NoneFeatureConfiguration> {
    /** Uma em quantos pedaços. */
    public static final int RARITY = 220;
    /** E entre que alturas ela se abre. */
    public static final int MIN_Y = -48;
    public static final int MAX_Y = 40;

    public RiftFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        if (context.chunkGenerator() instanceof net.minecraft.world.level.levelgen.FlatLevelSource) return false;
        if (random.nextInt(RARITY) != 0) return false;

        BlockPos origem = context.origin();
        // procura-se um oco: uma casa de ar com chão por baixo
        for (int tentativa = 0; tentativa < 24; tentativa++) {
            BlockPos onde = new BlockPos(
                    origem.getX() + random.nextInt(16),
                    random.nextIntBetweenInclusive(MIN_Y, MAX_Y),
                    origem.getZ() + random.nextInt(16));
            if (!level.getBlockState(onde).isAir()) continue;
            if (!level.getBlockState(onde.above()).isAir()) continue;
            if (level.getBlockState(onde.below()).isAir()) continue;
            level.setBlock(onde, ShatteredBlocks.RIFT.defaultBlockState(), 2);
            return true;
        }
        return false;
    }
}
