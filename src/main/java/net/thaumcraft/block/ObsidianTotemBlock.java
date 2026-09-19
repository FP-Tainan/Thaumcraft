package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * O totem de obsidiana: o {@code BlockCosmeticSolid} 0 da 4.2.3.5. Os lados mudam conforme a coluna: o bloco com outro
 * totem em cima tem a base sombreada; o que tem totem só embaixo mostra um dos quatro entalhes; o solto, a base lisa.
 * O entalhe de cada face é o {@code icon[2 + |(face + x%4 + z%4 + y%4) % 4|]} do original, guardado aqui como a soma
 * dos restos ({@link #SUM}, deslocada de nove) para o modelo escolher a figura de cada face.
 */
public class ObsidianTotemBlock extends Block {
    public static final MapCodec<ObsidianTotemBlock> CODEC = simpleCodec(ObsidianTotemBlock::new);

    public enum Part implements StringRepresentable {
        BASE("base"), CARVED("carved"), SHADED("shaded");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    public static final IntegerProperty SUM = IntegerProperty.create("sum", 0, 18);

    public ObsidianTotemBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PART, Part.BASE).setValue(SUM, 9));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, SUM);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return shape(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
        return direction.getAxis() == Direction.Axis.Y ? shape(state, level, pos) : state;
    }

    /** Os dois totens (o comum e o carregado) contam como coluna um para o outro. */
    public static boolean isTotem(BlockState state) {
        return state.getBlock() instanceof ObsidianTotemBlock || state.getBlock() instanceof ChargedObsidianTotemBlock;
    }

    /** A figura dos lados deste bloco, olhando os vizinhos de cima e de baixo. */
    public static BlockState shape(BlockState state, BlockGetter level, BlockPos pos) {
        Part part;
        if (isTotem(level.getBlockState(pos.above()))) part = Part.SHADED;
        else if (isTotem(level.getBlockState(pos.below()))) part = Part.CARVED;
        else part = Part.BASE;
        int sum = pos.getX() % 4 + pos.getZ() % 4 + pos.getY() % 4;
        return state.setValue(PART, part).setValue(SUM, sum + 9);
    }
}
