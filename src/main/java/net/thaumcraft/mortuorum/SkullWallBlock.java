package net.thaumcraft.mortuorum;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
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
 * quem joga ele passou a ser o que o nome promete: um muro de pedra caiada de branco com um crânio de esqueleto
 * sentado em cima. As duas folhas são as do próprio jogo — o tijolo de pedra e a cabeça do esqueleto —, caiadas
 * por {@code scratchpad/Caiar.java}, que tira a cor e clareia só o que já era claro, para as órbitas não sumirem.
 */
public class SkullWallBlock extends WallBlock {
    /**
     * Para que lado o crânio olha.
     *
     * <p>O muro em si não tem frente — quem a tem é o crânio —, então o lado guarda-se à parte, e o arquivo de
     * feitios gira o mourão inteiro conforme ele.
     */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final MapCodec<WallBlock> CODEC =
            simpleCodec(SkullWallBlock::new).xmap(bloco -> (WallBlock) bloco, bloco -> (SkullWallBlock) bloco);

    public SkullWallBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<WallBlock> codec() {
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
