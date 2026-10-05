package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Vidro Sombreado</b>: o {@code BlockShadedGlass} do Witchery.
 *
 * <p>Um vidro tingido que <b>se fecha com redstone</b>. Sem corrente, ele deixa passar a luz como qualquer
 * vidro; com corrente, ele <b>escurece e a luz para ali</b>.
 *
 * <p>É uma persiana, e serve ao ofício por uma razão só: um <b>vampiro</b> queima ao sol, e uma casa de
 * vidro sombreado é uma casa com janelas que se fecham de dentro. Quem o inventou pensou nisso.
 *
 * <p>São as <b>dezesseis cores</b> do jogo, e cada uma tem as suas duas folhas — a aberta e a fechada. A
 * fechada é mais escura e deixa ver menos, que é o que se espera de uma persiana corrida.
 *
 * <p><b>Uma mudança declarada:</b> no original isto são <b>dois blocos</b> — {@code shadedglass} e
 * {@code shadedglass_active} —, porque o jogo de 2014 não deixava a opacidade à luz mudar de um feitio para
 * outro do mesmo bloco. Hoje deixa, e por isso aqui é <b>um bloco com uma chave</b>. O que se vê e o que a
 * luz faz são os mesmos; o que mudou foi o número de nomes no registro.
 */
public class ShadedGlassBlock extends Block {
    public static final MapCodec<ShadedGlassBlock> CODEC = simpleCodec(ShadedGlassBlock::new);

    /** A cor, que são as dezesseis do jogo. */
    public static final EnumProperty<DyeColor> COR = EnumProperty.create("color", DyeColor.class);

    /** E se a corrente está nele. */
    public static final BooleanProperty FECHADO = BlockStateProperties.POWERED;

    /** O quanto ele come da luz fechado: tudo, que é o {@code setLightOpacity(15)} do original. */
    public static final int COME_A_LUZ = 15;

    public ShadedGlassBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(COR, DyeColor.WHITE).setValue(FECHADO, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> construtor) {
        construtor.add(COR, FECHADO);
    }

    /**
     * <b>Fechado, a luz para nele.</b>
     *
     * <p>É o {@code setLightOpacity(15)} do original, e é a chave inteira do bloco. Aberto, ele come zero —
     * como um vidro.
     */
    @Override
    protected int getLightDampening(BlockState feitio) {
        return feitio.getValue(FECHADO) ? COME_A_LUZ : 0;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState feitio) {
        return !feitio.getValue(FECHADO);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext onde) {
        return this.defaultBlockState()
                .setValue(FECHADO, onde.getLevel().hasNeighborSignal(onde.getClickedPos()));
    }

    /** E a corrente abre e fecha, como no original. */
    @Override
    protected void neighborChanged(BlockState feitio, Level level, BlockPos onde, Block quem,
                                   @Nullable net.minecraft.world.level.redstone.Orientation rumo,
                                   boolean mexeu) {
        if (level.isClientSide()) return;
        boolean tem = level.hasNeighborSignal(onde);
        if (tem != feitio.getValue(FECHADO)) {
            level.setBlock(onde, feitio.setValue(FECHADO, tem), Block.UPDATE_ALL);
        }
    }

    /** O vidro não se vê de dentro do vidro: as faces entre dois iguais somem, como no do jogo. */
    @Override
    protected boolean skipRendering(BlockState feitio, BlockState oOutro, Direction lado) {
        return oOutro.is(this) || super.skipRendering(feitio, oOutro, lado);
    }
}
