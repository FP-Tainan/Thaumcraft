package net.thaumcraft.crimson;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.MapColor;
import net.thaumcraft.registry.TCBlocks;

/**
 * O altar antigo no mundo: o {@code CrimsonWorldGenerator} do Crimson Warfare.
 *
 * <p>Uma tentativa por pedaço, e uma em mil dá certo: no chão de terra, areia ou pedra desenha-se o disco de
 * treze por treze de pedra arcana com a orla de tijolo, e no meio dele fica o altar.
 */
public class AncientAltarFeature extends Feature<NoneFeatureConfiguration> {
    /** O {@code spawnChance} do original. */
    public static final int RARITY = 1000;

    public AncientAltarFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        if (context.chunkGenerator() instanceof net.minecraft.world.level.levelgen.FlatLevelSource) return false;
        if (random.nextInt(RARITY) != 0) return false;

        BlockPos origin = context.origin();
        int x = (origin.getX() & ~15) + 8 + random.nextIntBetweenInclusive(-4, 4);
        int z = (origin.getZ() & ~15) + 8 + random.nextIntBetweenInclusive(-4, 4);
        BlockPos chão = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z)).below();
        if (!ground(level.getBlockState(chão))) return false;

        return build(level, chão.above());
    }

    /** O chão que o original aceita: terra, relva, areia ou pedra. */
    private static boolean ground(BlockState state) {
        MapColor cor = state.getMapColor(null, null);
        return cor == MapColor.DIRT || cor == MapColor.GRASS || cor == MapColor.SAND
                || cor == MapColor.STONE || cor == MapColor.COLOR_LIGHT_GRAY || cor == MapColor.PODZOL;
    }

    /** Desenha o disco e põe o altar no meio. {@code piso} é a casa do chão do disco. */
    public static boolean build(net.minecraft.world.level.LevelAccessor level, BlockPos piso) {
        BlockState pedra = TCBlocks.BUILDING.get("arcane_stone").defaultBlockState();
        BlockState tijolo = TCBlocks.BUILDING.get("arcane_stone_bricks").defaultBlockState();
        for (int fila = 0; fila < AncientAltarFloor.PLAN.length; fila++) {
            String desenho = AncientAltarFloor.PLAN[fila];
            for (int coluna = 0; coluna < desenho.length(); coluna++) {
                char qual = desenho.charAt(coluna);
                if (qual == ' ') continue;
                BlockPos onde = piso.offset(AncientAltarFloor.MIN_X + coluna, 0, AncientAltarFloor.MIN_Z + fila);
                level.setBlock(onde, qual == '#' ? tijolo : pedra, 2);
            }
        }
        level.setBlock(piso.above(), CrimsonBlocks.ANCIENT_ALTAR.defaultBlockState(), 2);
        return true;
    }
}
