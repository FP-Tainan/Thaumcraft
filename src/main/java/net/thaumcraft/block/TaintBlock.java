package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.registry.TCBlocks;

/**
 * A mácula: a doença da terra, que se alastra sozinha.
 *
 * <p>São duas caras, como no original: a <strong>crosta</strong>, que toma o lugar de tronco e folha, e o
 * <strong>solo maculado</strong>, que toma o lugar de terra, grama e pedra. Ela anda de tique em tique
 * aleatório, e a regra de quem ela pega é a do {@code BlockTaint} da 4.2.3.5: madeira e folha caem com
 * <strong>dois</strong> vizinhos maculados; terra e pedra, com <strong>três</strong>.
 *
 * <p><strong>Diferença deliberada.</strong> No original a mácula também pinta o bioma, e é o bioma que
 * decide se ela continua se alastrando ou se míngua. O Minecraft de hoje guarda bioma de quatro em quatro
 * blocos e não deixa um mod repintá-lo bloco a bloco; sem essa camada, o que segura a mácula aqui é a
 * própria conta dos vizinhos — ela avança onde já está forte e morre onde está sozinha. Está anotado em
 * {@code docs/PORTE.md}.
 */
public class TaintBlock extends Block {
    public static final MapCodec<TaintBlock> CODEC = simpleCodec(TaintBlock::new);
    /** Quantos vizinhos maculados a madeira precisa para cair. */
    private static final int WOOD_NEEDS = 2;
    /** Quantos a terra precisa. */
    private static final int SOIL_NEEDS = 3;
    /** Sozinha, a mácula míngua: um em dez de virar terra de novo. */
    private static final int WITHER_CHANCE = 10;

    public TaintBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // ela tenta um vizinho sorteado, como no original
        BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
        if (!level.isLoaded(target) || target.equals(pos)) {
            this.witherIfAlone(level, pos, random);
            return;
        }

        if (TaintFibreBlock.spread(level, target, random)) return;

        int around = adjacentTaint(level, target);
        BlockState there = level.getBlockState(target);
        if (there.isAir()) return;

        if (around >= WOOD_NEEDS && (there.is(BlockTags.LOGS) || there.is(BlockTags.LEAVES))) {
            level.setBlockAndUpdate(target, TCBlocks.TAINT_CRUST.defaultBlockState());
            return;
        }
        if (around >= SOIL_NEEDS && isSoil(there)) {
            level.setBlockAndUpdate(target, TCBlocks.TAINT_SOIL.defaultBlockState());
            return;
        }
        this.witherIfAlone(level, pos, random);
    }

    /**
     * Sem nenhum vizinho maculado, ela seca e a terra volta ao que era.
     *
     * <p>É o que sobrou do gate de bioma do original: lá, a mácula fora de um bioma maculado vira gosma
     * de fluxo ou volta a ser terra. Aqui, quem a segura é a companhia — uma mancha viva se mantém, e um
     * bloco solto se apaga.
     */
    private void witherIfAlone(ServerLevel level, BlockPos pos, RandomSource random) {
        if (adjacentTaint(level, pos) > 0) return;
        if (random.nextInt(WITHER_CHANCE) != 0) return;
        level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
    }

    /** Terra, grama, areia e pedra: o que a mácula come por baixo. */
    private static boolean isSoil(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND)
                || state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.GRAVEL);
    }

    /** Quantos dos seis vizinhos já estão maculados. */
    public static int adjacentTaint(net.minecraft.world.level.LevelReader level, BlockPos pos) {
        int count = 0;
        for (Direction dir : Direction.values()) {
            if (isTaint(level.getBlockState(pos.relative(dir)))) count++;
        }
        return count;
    }

    /** Este bloco é mácula, de qualquer feitio? */
    public static boolean isTaint(BlockState state) {
        return state.is(TCBlocks.TAINT_CRUST) || state.is(TCBlocks.TAINT_SOIL)
                || state.is(TCBlocks.TAINT_FIBRES);
    }
}
