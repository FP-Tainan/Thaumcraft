package net.thaumcraft.mortuorum;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

/**
 * O Muro de Caveiras: o {@code BlockSkullWall} do Necromancy — duro como obsidiana e do qual sai uma caveira de
 * esqueleto quando se quebra.
 *
 * <p>No original ele nunca é posto por nada: está registado e aparece na aba, e nada o constrói nem o faz cair. É
 * assim que ele vem para cá.
 *
 * <p><b>Desvio declarado:</b> lá o {@code registerBlockIcons} regista literalmente {@code "obsidian"}, e o bloco
 * fica com a cara da obsidiana — o que num bloco chamado Muro de Caveiras não diz nada a quem o vê. A pedido de
 * quem joga ele passou a ser o que o nome promete: uma cerca de pedra caiada de branco, com um crânio de
 * esqueleto sentado no alto do mourão. As folhas são de casa, desenhadas por
 * {@code scratchpad/MuroCaveiras.java}.
 */
public class SkullWallBlock extends FenceBlock {
    /**
     * Para que lado o crânio olha.
     *
     * <p>A cerca em si não tem frente — quem a tem é o crânio —, então o lado guarda-se à parte, e o arquivo de
     * feitios gira o mourão inteiro conforme ele.
     */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final MapCodec<FenceBlock> CODEC =
            simpleCodec(SkullWallBlock::new).xmap(bloco -> (FenceBlock) bloco, bloco -> (SkullWallBlock) bloco);

    public SkullWallBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<FenceBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    /** O crânio olha para quem o põe, como toda a coisa que tem cara. */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState estado = super.getStateForPlacement(context);
        return estado == null ? null
                : estado.setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}
