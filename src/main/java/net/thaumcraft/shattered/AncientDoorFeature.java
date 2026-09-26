package net.thaumcraft.shattered;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * As portas que já estavam lá.
 *
 * <p><b>Isto é do porte, e não do original</b>, e a ideia é de quem joga, que a mostrou numa foto: uma ombreira de
 * pedra de pé num descampado, com o lajedo dela ainda no chão e o mato já por cima. Vista de cabeça descoberta,
 * está vazia. Com os {@link VeilSight Óculos do Véu} no rosto, há uma {@link AncientDoorBlock porta} dentro dela.
 *
 * <p>É a irmã da {@link RiftFeature}: aquela põe no mundo as fendas que ninguém abriu, esta põe as portas que
 * alguém abriu e já não está cá para as fechar. Uma quer que se aprenda a ver; a outra dá o que ver.
 */
public class AncientDoorFeature extends Feature<NoneFeatureConfiguration> {
    /** Uma em quantos pedaços. */
    public static final int RARITY = 420;

    public AncientDoorFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        if (random.nextInt(RARITY) != 0) return false;

        BlockPos origem = context.origin();
        for (int tentativa = 0; tentativa < 16; tentativa++) {
            int x = origem.getX() + random.nextInt(16);
            int z = origem.getZ() + random.nextInt(16);
            BlockPos chão = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, new BlockPos(x, 0, z));
            if (chão.getY() < level.getMinY() + 8) continue;
            if (!plano(level, chão)) continue;
            constrói(level, chão, random);
            return true;
        }
        return false;
    }

    /** O chão tem de ser chato o bastante para a ombreira não ficar de pé no ar. */
    private static boolean plano(WorldGenLevel level, BlockPos meio) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos onde = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG,
                        meio.offset(dx, 0, dz));
                if (Math.abs(onde.getY() - meio.getY()) > 1) return false;
                if (!level.getFluidState(onde.below()).isEmpty()) return false;
            }
        }
        return true;
    }

    /** O lajedo, as duas ombreiras, a verga por cima e a porta no meio. */
    private static void constrói(WorldGenLevel level, BlockPos chão, RandomSource random) {
        Direction olhar = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Direction lado = olhar.getClockWise();

        BlockState tijolo = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState gasto = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        BlockState musgo = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        BlockState lajedo = Blocks.STONE_BRICKS.defaultBlockState();

        // o lajedo, cinco por cinco, meio comido pelo tempo
        for (int a = -2; a <= 2; a++) {
            for (int b = -2; b <= 2; b++) {
                if (Math.abs(a) + Math.abs(b) > 3) continue;
                if (random.nextInt(5) == 0) continue;
                BlockPos onde = chão.below().relative(lado, a).relative(olhar, b);
                level.setBlock(onde, random.nextInt(3) == 0 ? musgo : lajedo, 2);
            }
        }

        // as duas ombreiras e a verga: o vão fica de dois, que é o que a porta enche
        for (int altura = 0; altura < 2; altura++) {
            for (int qual = -1; qual <= 1; qual += 2) {
                BlockPos onde = chão.relative(lado, qual).above(altura);
                level.setBlock(onde, random.nextInt(4) == 0 ? gasto : tijolo, 2);
            }
        }
        for (int a = -1; a <= 1; a++) {
            level.setBlock(chão.relative(lado, a).above(2), random.nextInt(3) == 0 ? gasto : tijolo, 2);
        }

        // e a porta no vão, que só quem tem os óculos vê
        BlockState porta = ShatteredBlocks.ANCIENT_DIMENSIONAL_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, olhar)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        level.setBlock(chão, porta, 2);
        level.setBlock(chão.above(), porta.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 2);
    }
}
