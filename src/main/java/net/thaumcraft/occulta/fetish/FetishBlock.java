package net.thaumcraft.occulta.fetish;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.TaglockItem;
import net.thaumcraft.occulta.spirit.SpiritWorld;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Um <b>fetiche</b>: o {@code BlockFetish} do Witchery, que é um só bloco para os três.
 *
 * <p>O <b>Espantalho</b>, a <b>Escada de Bruxa</b> e o <b>Ídolo de Treant</b> são a mesma coisa com três
 * caras. Nenhum deles faz nada ao ser posto: o que os torna uma coisa é um <b>efeito</b> preso a eles por
 * um rito, e o que o rito come são <b>espíritos</b>.
 *
 * <h2>O que se lhes faz com a mão</h2>
 *
 * <ul>
 *   <li>um <b>corante</b> o pinta;</li>
 *   <li>um <b>balde</b> apaga as três listas de conhecidos e devolve-se vazio;</li>
 *   <li>a <b>Boline</b> roda o modo de alarme;</li>
 *   <li>e um <b>Kit de Taglock</b> cheio junta — ou tira — quem ele aponta às listas.</li>
 * </ul>
 *
 * <h2>A cópia do outro lado</h2>
 *
 * <p>Posto <b>no mundo dos sonhos</b>, o fetiche põe-se <b>também no mundo de cima</b>, nas mesmas
 * coordenadas, se lá houver ar — e essa segunda peça é <b>espectral</b>: não tem caixa de choque, não se
 * quebra, e desenha-se desmaiada. Quebrado o do sonho, o de cima some com ele.
 *
 * <p>É a melhor ideia do bloco: o espantalho que vigia o mundo de cima <b>não está no mundo de cima</b>.
 * Quem o quiser desligar tem de ir dormir.
 */
public class FetishBlock extends BaseEntityBlock {
    public static final MapCodec<FetishBlock> CODEC = simpleCodec(FetishBlock::new);

    /** Para que lado ele olha: o dado de bloco do original, nos quatro rumos do chão. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** A caixa dele: de dois a catorze em X e Z, e cheia em Y. */
    private static final VoxelShape SHAPE = Block.box(3.2, 0.0, 3.2, 12.8, 16.0, 12.8);

    public FetishBlock(Properties propriedades) {
        super(propriedades);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext onde) {
        return this.defaultBlockState().setValue(FACING, onde.getHorizontalDirection().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new FetishBlockEntity(onde, feitio);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState feitio, BlockEntityType<T> tipo) {
        return level.isClientSide() ? null
                : createTickerHelper(tipo, net.thaumcraft.occulta.OccultaBlocks.FETISH_ENTITY,
                        FetishBlockEntity::tick);
    }

    /** A Escada de Bruxa é uma folha cruzada, e os outros dois são bonecos. */
    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return feitio.is(net.thaumcraft.occulta.OccultaBlocks.WITCHS_LADDER)
                ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                  CollisionContext quem) {
        return SHAPE;
    }

    /**
     * <b>A cópia espectral não estorva.</b> E a Escada de Bruxa também não — ela é uma folha de penas
     * pendurada, e o original lhe tira a caixa de choque pelo nome.
     */
    @Override
    protected VoxelShape getCollisionShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                           CollisionContext quem) {
        if (feitio.is(net.thaumcraft.occulta.OccultaBlocks.WITCHS_LADDER)) {
            return net.minecraft.world.phys.shapes.Shapes.empty();
        }
        return level.getBlockEntity(onde) instanceof FetishBlockEntity alma && alma.spectral()
                ? net.minecraft.world.phys.shapes.Shapes.empty() : SHAPE;
    }

    /** <b>A cópia espectral não se quebra.</b> */
    @Override
    protected float getDestroyProgress(BlockState feitio, Player quem, BlockGetter level, BlockPos onde) {
        return level.getBlockEntity(onde) instanceof FetishBlockEntity alma && alma.spectral()
                ? 0.0f : super.getDestroyProgress(feitio, quem, level, onde);
    }

    // ------------------------------------------------------------------ pôr e tirar

    @Override
    public void setPlacedBy(Level level, BlockPos onde, BlockState feitio,
                            @Nullable LivingEntity quem, ItemStack oquê) {
        super.setPlacedBy(level, onde, feitio, quem, oquê);
        if (!(level instanceof ServerLevel servidor)) return;
        if (!(level.getBlockEntity(onde) instanceof FetishBlockEntity alma)) return;

        alma.effectType(SpiritEffects.idOf(oquê));
        var guardado = oquê.get(OccultaComponents.FETISH_DATA);
        if (guardado != null) alma.load(guardado);

        /*
         * Posto no mundo dos sonhos, põe-se <b>também no mundo de cima</b> — e essa segunda peça é a
         * espectral: vê-se de lá, não se toca, não se quebra.
         */
        if (!SpiritWorld.is(servidor)) return;
        ServerLevel cima = servidor.getServer().overworld();
        if (!cima.getBlockState(onde).isAir()) return;
        cima.setBlockAndUpdate(onde, feitio);
        if (!(cima.getBlockEntity(onde) instanceof FetishBlockEntity cópia)) return;
        cópia.effectType(alma.effectType());
        cópia.copyFrom(alma);
        cópia.spectral(true);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos onde, BlockState feitio, Player quem) {
        if (level instanceof ServerLevel servidor && SpiritWorld.is(servidor)) {
            ServerLevel cima = servidor.getServer().overworld();
            if (cima.getBlockState(onde).is(this)) cima.removeBlock(onde, false);
        }
        return super.playerWillDestroy(level, onde, feitio, quem);
    }

    /** <b>Ele se larga a si próprio com tudo dentro</b>: a tinta, o efeito e as três listas. */
    @Override
    protected List<ItemStack> getDrops(BlockState feitio, LootParams.Builder dados) {
        if (!(dados.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY)
                instanceof FetishBlockEntity alma)) {
            return List.of(new ItemStack(this));
        }
        ItemStack oquê = new ItemStack(this);
        SpiritEffects.withId(oquê, alma.effectType());
        var guardado = alma.save();
        if (!guardado.isEmpty()) oquê.set(OccultaComponents.FETISH_DATA, guardado);
        return List.of(oquê);
    }

    // ------------------------------------------------------------------ a mão nele

    @Override
    protected InteractionResult useItemOn(ItemStack naMão, BlockState feitio, Level level, BlockPos onde,
                                          Player quem, InteractionHand mão, BlockHitResult bateu) {
        if (!(level instanceof ServerLevel servidor)) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(onde) instanceof FetishBlockEntity alma)) {
            return InteractionResult.PASS;
        }

        /*
         * O taglock é a única coisa que se usa na cópia espectral — e, nela, <b>só para ler</b>: ele mostra
         * a lista e não lhe mexe. Tudo o resto é no fetiche de verdade.
         */
        if (naMão.is(OccultaItems.TAGLOCK) && TaglockItem.isBound(naMão)) {
            if (!alma.spectral()) {
                var vínculo = TaglockItem.bound(naMão);
                alma.toggleKnown(vínculo, net.minecraft.network.chat.Component.literal(vínculo.name()));
                if (!quem.hasInfiniteMaterials()) naMão.shrink(1);
            }
            alma.tell(quem);
            return InteractionResult.SUCCESS;
        }
        if (alma.spectral()) return InteractionResult.PASS;

        var corante = naMão.get(net.minecraft.core.component.DataComponents.DYE);
        if (corante != null) {
            alma.color(corante.getId());
            if (!quem.hasInfiniteMaterials()) naMão.shrink(1);
            return InteractionResult.SUCCESS;
        }
        if (naMão.is(Items.WATER_BUCKET)) {
            alma.clearKnown();
            alma.tell(quem);
            if (!quem.hasInfiniteMaterials()) {
                naMão.shrink(1);
                if (!quem.getInventory().add(new ItemStack(Items.BUCKET))) {
                    quem.drop(new ItemStack(Items.BUCKET), false);
                }
            }
            servidor.playSound(null, onde, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        if (naMão.is(OccultaItems.BOLINE)) {
            alma.cycleMode(quem);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    // ------------------------------------------------------------------ a redstone

    @Override
    protected boolean isSignalSource(BlockState feitio) {
        return true;
    }

    @Override
    protected int getSignal(BlockState feitio, BlockGetter level, BlockPos onde, Direction lado) {
        return level.getBlockEntity(onde) instanceof FetishBlockEntity alma ? alma.signal() : 0;
    }

    /** E só manda para cima, como o original. */
    @Override
    protected int getDirectSignal(BlockState feitio, BlockGetter level, BlockPos onde, Direction lado) {
        return lado == Direction.DOWN ? this.getSignal(feitio, level, onde, lado) : 0;
    }
}
