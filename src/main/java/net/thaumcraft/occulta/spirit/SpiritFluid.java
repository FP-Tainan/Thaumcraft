package net.thaumcraft.occulta.spirit;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.thaumcraft.fluid.ThaumFluid;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;

/**
 * Os dois líquidos do outro lado: o {@code BlockFlowingSpirit} do Witchery, que serve aos dois.
 *
 * <p>O <b>Espírito Fluente</b> é o que se tira de um cozimento fervido no Mundo dos Espíritos. O que ele toca ou
 * sara ou definha, conforme o que for: quem é gente comum sai dele <b>curado</b>; quem é morto-vivo, coisa do
 * inferno ou Pesadelo sai dele <b>fraco</b>. E ele desfaz o pesadelo: o Algodão Perturbado largado nele volta a
 * ser Algodão Sonhador.
 *
 * <p>As <b>Lágrimas Ocas</b> são o contrário dele, e saem da Destilaria. O que nelas sara é o morto e o
 * demônio; a gente comum é que definha. Elas servem para uma coisa só, e é a que faz delas o fim da linha: uma
 * poça de Lágrimas Ocas <b>endurece</b> quando lhe atiram um dos Cozimentos Sólidos.
 *
 * <p><b>Desvio declarado:</b> o original conta cinco níveis por bloco ({@code quantaPerBlock = 5}), que é coisa
 * do fluido do Forge de 2014. O fluido do jogo de hoje conta oito, como a água, e não se lhe pode mudar isso sem
 * reescrever o motor do líquido. Corre um pouco mais longe do que corria; nada mais muda.
 */
public abstract class SpiritFluid extends ThaumFluid {
    /** Nenhum dos dois faz fonte nova, e os dois demoram o que a água demora. */
    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return 4;
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return 1;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 5;
    }

    /** O Espírito Fluente. */
    public abstract static class Spirit extends SpiritFluid {
        @Override
        protected Fluid source() {
            return SpiritFluids.FLOWING_SPIRIT;
        }

        @Override
        protected Fluid flowing() {
            return SpiritFluids.FLOWING_SPIRIT_FLOWING;
        }

        @Override
        protected Item bucket() {
            return OccultaItems.BUCKET_FLOWING_SPIRIT;
        }

        @Override
        protected Block block() {
            return OccultaBlocks.FLOWING_SPIRIT;
        }

        public static class Source extends Spirit {
            @Override
            public int getAmount(FluidState state) {
                return 8;
            }

            @Override
            public boolean isSource(FluidState state) {
                return true;
            }
        }

        public static class Flowing extends Spirit {
            @Override
            protected void createFluidStateDefinition(
                    net.minecraft.world.level.block.state.StateDefinition.Builder<Fluid, FluidState> builder) {
                super.createFluidStateDefinition(builder);
                builder.add(LEVEL);
            }

            @Override
            public int getAmount(FluidState state) {
                return state.getValue(LEVEL);
            }

            @Override
            public boolean isSource(FluidState state) {
                return false;
            }
        }
    }

    /** E as Lágrimas Ocas. */
    public abstract static class Tears extends SpiritFluid {
        @Override
        protected Fluid source() {
            return SpiritFluids.HOLLOW_TEARS;
        }

        @Override
        protected Fluid flowing() {
            return SpiritFluids.HOLLOW_TEARS_FLOWING;
        }

        @Override
        protected Item bucket() {
            return OccultaItems.BUCKET_HOLLOW_TEARS;
        }

        @Override
        protected Block block() {
            return OccultaBlocks.HOLLOW_TEARS;
        }

        public static class Source extends Tears {
            @Override
            public int getAmount(FluidState state) {
                return 8;
            }

            @Override
            public boolean isSource(FluidState state) {
                return true;
            }
        }

        public static class Flowing extends Tears {
            @Override
            protected void createFluidStateDefinition(
                    net.minecraft.world.level.block.state.StateDefinition.Builder<Fluid, FluidState> builder) {
                super.createFluidStateDefinition(builder);
                builder.add(LEVEL);
            }

            @Override
            public int getAmount(FluidState state) {
                return state.getValue(LEVEL);
            }

            @Override
            public boolean isSource(FluidState state) {
                return false;
            }
        }
    }
}
