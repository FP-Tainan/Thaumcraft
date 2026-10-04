package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Rosa de Sangue</b>: a {@code BlockBloodRose} do Witchery.
 *
 * <p>É a flor mais sinistra do ofício, e o que a torna sinistra não é nada do que ela faz: é o que ela
 * <b>guarda</b>.
 *
 * <h2>O que ela é</h2>
 *
 * <p>Uma flor pequena que <b>se lembra de quem pisou nela</b>. Quem passar por cima deixa o nome lá dentro,
 * e a flor <b>fecha</b> — muda de desenho, e qualquer um que olhe vê que ela comeu. Depois disso, um
 * {@linkplain TaglockItem Frasco de Vínculo} encostado nela sai <b>cheio daquela pessoa</b>, sem que ela
 * jamais tenha sido tocada.
 *
 * <p>É isso: um <b>vínculo à distância</b>. Todo o resto do ofício que prende alguém — a boneca, a maldição,
 * o espelho — precisa de um fio de quem se quer, e um fio de quem se quer precisa de chegar perto. A Rosa de
 * Sangue não precisa. Planta-se no caminho de alguém e espera-se.
 *
 * <h2>E ela não se colhe</h2>
 *
 * <p>Quebrá-la com a mão, com uma pá, com o que for: <b>não cai nada</b>. Só a {@linkplain
 * net.thaumcraft.occulta.vampire.BolineItem Boline} a colhe — e colhida com a Boline ela sai
 * <b>com o que tem dentro</b>, de modo que se pode arrancar a flor que apanhou alguém e levá-la para casa.
 *
 * <p>No original ela é também <b>fonte de poder do Altar</b>: dois de poder a dez blocos.
 */
public class BloodRoseBlock extends BushBlock implements EntityBlock {
    public static final MapCodec<BloodRoseBlock> CODEC = simpleCodec(BloodRoseBlock::new);

    /** Se ela já comeu alguém. */
    public static final BooleanProperty CHEIA = BooleanProperty.create("cheia");

    private static final VoxelShape FORMA = Block.box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0);

    public BloodRoseBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CHEIA, false));
    }

    @Override
    public MapCodec<BushBlock> codec() {
        @SuppressWarnings("unchecked")
        MapCodec<BushBlock> meu = (MapCodec<BushBlock>) (MapCodec<?>) CODEC;
        return meu;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHEIA);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos onde,
                                  CollisionContext quem) {
        return FORMA;
    }

    @Override
    protected boolean mayPlaceOn(BlockState chão, BlockGetter level, BlockPos onde) {
        return chão.is(net.minecraft.tags.BlockTags.DIRT)
                || chão.is(net.minecraft.world.level.block.Blocks.FARMLAND);
    }

    /**
     * <b>Pisar nela é deixar-se lá.</b>
     *
     * <p>Basta passar. Não há aviso, não há som, não há partícula — a flor muda de desenho e é só isso que
     * denuncia. Quem não souber o que ela é passa por cima dela todos os dias.
     */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos onde, Entity quem,
                                net.minecraft.world.entity.InsideBlockEffectApplier efeitos,
                                boolean dentro) {
        if (!(level instanceof ServerLevel mundo)) return;
        if (!(quem instanceof Player gente)) return;
        pisou(mundo, onde, gente);
    }

    /**
     * O que acontece quando alguém pisa: a flor guarda o nome e <b>fecha</b>.
     *
     * @return se ela mudou de dono
     */
    public static boolean pisou(ServerLevel level, BlockPos onde, Player gente) {
        if (!(level.getBlockEntity(onde) instanceof BloodRoseBlockEntity rosa)) return false;
        if (!rosa.guarda(gente)) return false;

        BlockState feitio = level.getBlockState(onde);
        if (feitio.hasProperty(CHEIA)) {
            level.setBlock(onde, feitio.setValue(CHEIA, true), Block.UPDATE_ALL);
        }
        return true;
    }

    /**
     * <b>Tirar o que ela guarda</b>: o ramo do {@code ItemTaglockKit} que clica numa Rosa de Sangue.
     *
     * @return quem ela guardava, ou {@code null} se ela estava vazia
     */
    public static @Nullable TaglockItem.Taglock tira(ServerLevel level, BlockPos onde) {
        if (!(level.getBlockEntity(onde) instanceof BloodRoseBlockEntity rosa)) return null;
        TaglockItem.Taglock quem = rosa.tira();
        if (quem == null) return null;

        BlockState feitio = level.getBlockState(onde);
        if (feitio.hasProperty(CHEIA)) {
            level.setBlock(onde, feitio.setValue(CHEIA, false), Block.UPDATE_ALL);
        }
        return quem;
    }

    /** Uma rosa só cresce onde uma rosa pode crescer, e ela cresce em terra. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos onde) {
        return this.mayPlaceOn(level.getBlockState(onde.below()), level, onde.below());
    }

    /**
     * E uma rosa <b>colhida cheia</b> volta cheia ao chão.
     *
     * <p>A Boline a arranca com quem ela guardava; plantá-la outra vez devolve-lhe esse alguém. É o que
     * faz da rosa uma coisa que se <b>leva</b> — planta-se no caminho de quem se quer, colhe-se, e
     * leva-se para casa a pessoa apanhada.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos onde, BlockState state,
                               @Nullable net.minecraft.world.entity.LivingEntity quem,
                               net.minecraft.world.item.ItemStack oquê) {
        super.setPlacedBy(level, onde, state, quem, oquê);
        var tinha = oquê.get(OccultaComponents.TAGLOCK);
        if (tinha == null) return;
        if (!(level.getBlockEntity(onde) instanceof BloodRoseBlockEntity rosa)) return;

        rosa.põe(tinha);
        level.setBlock(onde, state.setValue(CHEIA, true), Block.UPDATE_ALL);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState state) {
        return new BloodRoseBlockEntity(onde, state);
    }
}
