package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Estátua de Adoração</b>: o {@code BlockStatueOfWorship} do Witchery.
 *
 * <p>É a única coisa do mod que <b>enche uma infusão</b> sem se refazer o rito. Sem ela, a infusão é um
 * cantil que se enche uma vez e acabou; com ela, é uma barra que volta.
 *
 * <p>E ela não enche sozinha: ela <b>precisa de goblins</b>. De cinco em cinco segundos conta quantos
 * estão adorando-a num cubo de oito blocos, e o que faz depende do número:
 *
 * <ul>
 *   <li><b>cinco</b> adoradores: o dono, se estiver a sessenta e quatro blocos ou menos, ganha <b>trinta
 *       de carga</b> por pulso;</li>
 *   <li><b>dez</b>: o dono ganha <b>Adoração</b>, que é o efeito que os símbolos do segundo grau pedem;</li>
 *   <li><b>quinze</b>: a Adoração sobe para o segundo nível, e com ela o <b>terceiro grau</b> dos
 *       símbolos.</li>
 * </ul>
 *
 * <h2>Ela tem a cara de alguém</h2>
 *
 * <p>A estátua guarda o <b>nome e o número de quem a prendeu a si</b>, e o desenhista dela veste-a com a
 * <b>pele dessa pessoa</b>, pintada de pedra. Uma estátua fabricada na bancada <b>não está presa a
 * ninguém</b> e não faz nada: para a prender é preciso o <b>Rito de Prender a Estátua</b>, que a devolve
 * com a cara de quem o fez.
 *
 * <p>É de propósito que seja assim, e diz o que a coisa é: ela não é uma máquina de encher infusões, é um
 * <b>ídolo</b>. Os goblins não adoram a estátua — adoram <b>você</b>.
 */
public class StatueOfWorshipBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<StatueOfWorshipBlock> CODEC = simpleCodec(StatueOfWorshipBlock::new);

    /** O feitio dela, que é o de um bloco inteiro menos um pedaço de cada lado. */
    private static final VoxelShape FORMA = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public StatueOfWorshipBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    public MapCodec<StatueOfWorshipBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> construtor) {
        construtor.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext onde) {
        return this.defaultBlockState()
                .setValue(FACING, onde.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter mundo, BlockPos onde,
                                  CollisionContext quem) {
        return FORMA;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState feitio) {
        return true;
    }


    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader mundo, BlockPos onde,
                                       BlockState feitio, boolean completo) {
        ItemStack saiu = new ItemStack(this);
        if (completo && mundo.getBlockEntity(onde) instanceof StatueOfWorshipBlockEntity estátua
                && estátua.dono() != null) {
            saiu.set(OccultaComponents.TAGLOCK, estátua.dono());
        }
        return saiu;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new StatueOfWorshipBlockEntity(onde, feitio);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level mundo,
                                                                            BlockState feitio,
                                                                            BlockEntityType<T> tipo) {
        if (mundo.isClientSide() || tipo != OccultaBlocks.STATUE_OF_WORSHIP_ENTITY) return null;
        return (nível, onde, feitio2, alma) -> {
            if (nível.getGameTime() % StatueOfWorshipBlockEntity.PULSO != 0L) return;
            if (alma instanceof StatueOfWorshipBlockEntity estátua) {
                StatueOfWorshipBlockEntity.bate((net.minecraft.server.level.ServerLevel) nível, onde,
                        estátua);
            }
        };
    }
}
