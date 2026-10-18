package net.thaumcraft.occulta.rite;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Os <b>dois marcadores de área</b>: a {@code BlockAreaMarker} do Witchery.
 *
 * <p>Duas estatuetas de meio tamanho que <b>não fazem nada quando se toca nelas</b> e fazem muita coisa
 * quando não se está olhando: cada uma <b>abafa um rito</b> num raio de <b>trinta e dois blocos</b> à
 * volta. O <b>lobo</b> abafa a maldição dirigida; a <b>mandrágora</b> abafa o puxão por teleporte.
 *
 * <p>E elas <b>deixam de proteger quem matou alguém perto delas</b>. É a melhor ideia do bloco: a
 * proteção é para quem vive em paz, e perde-se no primeiro assassinato.
 *
 * <h2>Desvios declarados</h2>
 *
 * <p><b>Vêm ligadas.</b> No original as duas moram atrás de ajustes que vêm <b>desligados</b>
 * ({@code DecurseDirectedEnabled} e {@code DecurseTeleportPullEnabled}), e sem eles os blocos existem e
 * não abafam nada. Este porte não tem arquivo de ajustes, e um bloco que não faz nada não é um bloco.
 *
 * <p><b>Continuam sem receita</b>, como no original: são blocos de criativo, e quem os quiser põe-nos à
 * mão. Isso o original também faz, e não é um desvio — é só a metade da história que fica de pé.
 */
public class AreaMarkerBlock extends BaseEntityBlock {
    public static final MapCodec<AreaMarkerBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            net.minecraft.util.StringRepresentable.fromEnum(Feitio::values)
                    .fieldOf("kind").forGetter(b -> b.feitio),
            propertiesCodec()
    ).apply(i, AreaMarkerBlock::new));

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** A caixa do original: meio bloco de alto e um dedo de cada lado. */
    private static final VoxelShape SHAPE = Block.box(2.4, 0.0, 2.4, 13.6, 8.0, 13.6);

    /** Qual dos dois este bloco é, com o nome que o arquivo de feitio guarda. */
    public enum Feitio implements net.minecraft.util.StringRepresentable {
        WOLF("wolf", AreaMarkers.Kind.CURSE),
        MANDRAKE("mandrake", AreaMarkers.Kind.TELEPORT);

        private final String nome;
        private final AreaMarkers.Kind contra;

        Feitio(String nome, AreaMarkers.Kind contra) {
            this.nome = nome;
            this.contra = contra;
        }

        public AreaMarkers.Kind contra() {
            return this.contra;
        }

        @Override
        public String getSerializedName() {
            return this.nome;
        }
    }

    private final Feitio feitio;

    public AreaMarkerBlock(Feitio feitio, Properties properties) {
        super(properties);
        this.feitio = feitio;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public Feitio feitio() {
        return this.feitio;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> construtor) {
        construtor.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext contexto) {
        return this.defaultBlockState()
                .setValue(FACING, contexto.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                  CollisionContext contexto) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new AreaMarkerBlockEntity(onde, feitio);
    }
}
