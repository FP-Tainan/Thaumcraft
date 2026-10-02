package net.thaumcraft.occulta.spirit;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * As duas plantas que só nascem em sonho: o {@code BlockCotton} e o {@code BlockGlintWeed} do Witchery.
 *
 * <p>Elas <b>não pegam no mundo de cima</b>: alastram-se só no Mundo dos Espíritos, e só sobre terra ou grama.
 * A conta de alastrar é a do original — uma vez em seis, e param quando já há cinco delas por perto, para que
 * uma moita não vire um campo.
 *
 * <p>O Algodão Sonhador tem ainda a sua manha: colhido <b>em pesadelo</b>, ele vem <b>Perturbado</b>, e é esse
 * que a Roca fia em Cordel Atormentado.
 */
public class DreamPlantBlock extends BushBlock {
    /** De quantas em quantas batidas de sorte ela tenta alastrar-se, e quantas vizinhas a fazem parar. */
    public static final int SPREAD_CHANCE = 6;
    public static final int CROWD = 5;
    public static final int LOOK = 4;

    private final boolean cotton;

    public DreamPlantBlock(Properties properties, boolean cotton) {
        super(properties);
        this.cotton = cotton;
    }

    /** Se esta é a do algodão, que muda de nome em pesadelo. */
    public boolean cotton() {
        return this.cotton;
    }

    @Override
    protected boolean mayPlaceOn(BlockState chão, BlockGetter level, BlockPos pos) {
        return ground(chão);
    }

    /**
     * Se aquele chão serve, olhando só o chão.
     *
     * <p>Está à parte porque o semeador do Mundo dos Espíritos precisa de perguntar isto <b>durante o
     * carregamento do pedaço</b>, e aí não se pode perguntar nada ao mundo: o pedaço ainda não está na lista dele,
     * e quem lhe pedir um bloco fica à espera de si mesmo.
     */
    public static boolean ground(BlockState chão) {
        return chão.is(net.minecraft.world.level.block.Blocks.DIRT)
                || chão.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)
                || chão.is(net.minecraft.world.level.block.Blocks.COARSE_DIRT)
                || chão.is(net.minecraft.world.level.block.Blocks.PODZOL);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    /** O {@code updateTick}: ela se alastra devagar, e só no outro lado. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!SpiritWorld.is(level) || random.nextInt(SPREAD_CHANCE) != 0) return;

        int quantas = 0;
        for (BlockPos casa : BlockPos.betweenClosed(pos.offset(-LOOK, -1, -LOOK), pos.offset(LOOK, 1, LOOK))) {
            if (!level.getBlockState(casa).is(this)) continue;
            if (++quantas >= CROWD) return;
        }

        for (int tenta = 0; tenta < 4; tenta++) {
            BlockPos onde = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2),
                    random.nextInt(3) - 1);
            if (!level.isEmptyBlock(onde) || !this.canSurvive(state, level, onde)) continue;
            level.setBlockAndUpdate(onde, this.defaultBlockState());
            return;
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return this.mayPlaceOn(level.getBlockState(pos.below()), level, pos.below());
    }

    /**
     * O {@code getDrops} do algodão: colhido por quem está <b>em pesadelo</b>, ele vem <b>Perturbado</b>.
     *
     * <p>É a mesma planta: o que muda é o olho de quem a colhe.
     */
    @Override
    protected java.util.List<net.minecraft.world.item.ItemStack> getDrops(
            BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder params) {
        var quem = params.getOptionalParameter(
                net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY);
        if (this.cotton && quem instanceof net.minecraft.world.entity.player.Player gente
                && SpiritWalk.nightmare(gente)) {
            return java.util.List.of(new net.minecraft.world.item.ItemStack(
                    net.thaumcraft.occulta.OccultaItems.DISTURBED_COTTON));
        }
        return super.getDrops(state, params);
    }
}
