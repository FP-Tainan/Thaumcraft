package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Paliçada</b>: o {@code BlockStockade} do Witchery.
 *
 * <p>Uma cerca de <b>estacas apontadas</b>, e ela <b>fere quem encosta</b> — três de dano de cato, sempre,
 * sem armadura que valha. Não é uma cerca que se pula: é uma cerca que se <b>contorna</b>.
 *
 * <p>Ela é <b>duríssima</b>: vinte e cinco de dureza, mais do que a obsidiana, e demora a cair. Quem puser
 * uma paliçada à volta de alguma coisa pode ir dormir.
 *
 * <p>E ela <b>não deixa passar</b>: o original devolve que não se anda por ela, de modo que nada que ande no
 * chão tenta atravessá-la.
 *
 * <p>São <b>nove madeiras</b> — as seis do jogo e as três do ofício — e um <b>gelo</b>, que é bloco à parte.
 * Entre elas todas ligam-se; com o gelo, não. É a conta do original, em que a de madeira e a de gelo são dois
 * blocos diferentes e cada um só reconhece o seu.
 */
public class StockadeBlock extends Block {
    public static final MapCodec<StockadeBlock> CODEC = simpleCodec(StockadeBlock::new);

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    /** Se há outra paliçada <b>por cima</b>: havendo, as estacas desta não apontam, vão a direito. */
    public static final BooleanProperty UP = BlockStateProperties.UP;

    /** O dano que ela faz a quem encosta: o do cato, e é sempre esse. */
    public static final float ESPETA = 3.0f;

    public StockadeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(EAST, false)
                .setValue(SOUTH, false).setValue(WEST, false).setValue(UP, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP);
    }

    /**
     * A quem ela se liga: a <b>si mesma</b>, a um <b>portão de cerca</b> do jogo e ao <b>portão de gelo
     * perpétuo</b>.
     *
     * <p>A si mesma quer dizer <b>ao mesmo bloco</b>: as nove madeiras são um bloco só e ligam-se entre si,
     * e a de gelo é outro e não se liga a elas. É o original.
     */
    public boolean liga(BlockState oquê) {
        return oquê.is(this) || oquê.is(Blocks.OAK_FENCE_GATE)
                || oquê.is(OccultaBlocks.ICE_FENCE_GATE);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext onde) {
        return this.arruma(this.defaultBlockState(), onde.getLevel(), onde.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState feitio, LevelReader level, ScheduledTickAccess relógio,
                                     BlockPos onde, Direction para, BlockPos doOutro, BlockState oOutro,
                                     RandomSource sorte) {
        return this.arruma(feitio, level, onde);
    }

    /** Olha os quatro lados e o de cima, e põe as chaves. */
    private BlockState arruma(BlockState feitio, LevelReader level, BlockPos onde) {
        return feitio
                .setValue(NORTH, this.liga(level.getBlockState(onde.north())))
                .setValue(SOUTH, this.liga(level.getBlockState(onde.south())))
                .setValue(WEST, this.liga(level.getBlockState(onde.west())))
                .setValue(EAST, this.liga(level.getBlockState(onde.east())))
                .setValue(UP, this.liga(level.getBlockState(onde.above())));
    }

    // ------------------------------------------------------------------ a forma dela

    /** As quatro estacas, cada uma com o seu quadrado no chão. */
    private static final VoxelShape MEIO = Block.box(4.8, 0.0, 4.8, 11.2, 16.0, 11.2);
    private static final VoxelShape OESTE = Block.box(0.8, 0.0, 4.8, 7.2, 16.0, 11.2);
    private static final VoxelShape LESTE = Block.box(8.8, 0.0, 4.8, 15.2, 16.0, 11.2);
    private static final VoxelShape NORTE = Block.box(4.8, 0.0, 0.8, 11.2, 16.0, 7.2);
    private static final VoxelShape SUL = Block.box(4.8, 0.0, 8.8, 11.2, 16.0, 15.2);

    /**
     * A forma é a das <b>estacas que estão ali</b>, e não uma caixa só.
     *
     * <p>O original faz a mesma conta de outra maneira: ele monta uma caixa por vez e junta-as. Aqui elas
     * juntam-se de uma vez, e a conta dá no mesmo — o que se vê e o que se toca são as estacas.
     *
     * <p>E ela é <b>inteira de alta</b> quando há outra por cima: quem empilha paliçadas faz uma parede.
     */
    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                  CollisionContext quem) {
        boolean lado = feitio.getValue(WEST) || feitio.getValue(EAST);
        boolean fundo = feitio.getValue(NORTH) || feitio.getValue(SOUTH);
        if (!lado && !fundo) return MEIO;

        VoxelShape forma = Shapes.empty();
        if (lado) forma = Shapes.or(forma, OESTE, LESTE);
        if (fundo) forma = Shapes.or(forma, NORTE, SUL);
        return forma;
    }

    /**
     * <b>Ela fere quem encosta.</b>
     *
     * <p>Três de dano de cato, que é o mesmo que o original usa — e o cato é dano que a armadura não para.
     * Encostar numa paliçada custa sempre o mesmo, de qualquer lado e em qualquer armadura.
     */
    @Override
    protected void entityInside(BlockState feitio, Level level, BlockPos onde, Entity quem,
                                net.minecraft.world.entity.InsideBlockEffectApplier efeitos,
                                boolean emCima) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel mundo)) return;
        if (!(quem instanceof net.minecraft.world.entity.LivingEntity vivo)) return;
        vivo.hurtServer(mundo, mundo.damageSources().cactus(), ESPETA);
    }

    /** E nada que ande no chão tenta atravessá-la. */
    @Override
    protected boolean isPathfindable(BlockState feitio, PathComputationType oquê) {
        return false;
    }

    /** As nove madeiras: as seis do jogo e as três do ofício. */
    public enum Wood implements StringRepresentable {
        OAK("oak"),
        SPRUCE("spruce"),
        BIRCH("birch"),
        JUNGLE("jungle"),
        ROWAN("rowan"),
        ALDER("alder"),
        HAWTHORN("hawthorn"),
        ACACIA("acacia"),
        DARK_OAK("dark_oak");

        private final String nome;

        Wood(String nome) {
            this.nome = nome;
        }

        @Override
        public String getSerializedName() {
            return this.nome;
        }
    }

    /** A chave da madeira, que só o bloco de madeira tem. */
    public static final EnumProperty<Wood> MADEIRA = EnumProperty.create("wood", Wood.class);

    /**
     * A <b>de madeira</b>, que é a mesma com uma chave a mais.
     *
     * <p>No original as nove madeiras são <b>um bloco só</b> com nove números, e é por isso que elas se
     * ligam entre si: o {@code canConnectFenceTo} pergunta se o bloco do lado é <b>este mesmo bloco</b>, e
     * um carvalho e uma sorveira são. A de gelo é outro bloco e não entra na conta — de modo que uma
     * paliçada de gelo encostada numa de madeira fica de pé sozinha, sem se ligar a ela.
     */
    public static class Wooden extends StockadeBlock {
        public static final MapCodec<Wooden> CODEC = simpleCodec(Wooden::new);

        public Wooden(Properties properties) {
            super(properties);
            this.registerDefaultState(this.stateDefinition.any()
                    .setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false)
                    .setValue(WEST, false).setValue(UP, false).setValue(MADEIRA, Wood.OAK));
        }

        @Override
        protected MapCodec<? extends Block> codec() {
            return CODEC;
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> construtor) {
            super.createBlockStateDefinition(construtor);
            construtor.add(MADEIRA);
        }

        /** E o clique do meio traz a madeira que está ali. */
        @Override
        protected net.minecraft.world.item.ItemStack getCloneItemStack(LevelReader level, BlockPos onde,
                                                                       BlockState feitio, boolean comAlma) {
            var qual = super.getCloneItemStack(level, onde, feitio, comAlma);
            qual.set(net.minecraft.core.component.DataComponents.BLOCK_STATE,
                    net.minecraft.world.item.component.BlockItemStateProperties.EMPTY
                            .with(MADEIRA, feitio.getValue(MADEIRA)));
            return qual;
        }
    }
}
