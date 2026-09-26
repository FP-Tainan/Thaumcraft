package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.function.Supplier;

/**
 * Uma das plantas do ofício: o {@code BlockWitchCrop} do Witchery 0.24.1.
 *
 * <p>Cresce como o trigo — luz de nove para cima, e mais depressa em terra arada e com vizinhança boa —, mas com
 * cinco coisas que são dela e estão no {@link Traits}:
 *
 * <ul>
 *   <li><b>quantas idades</b> muda de planta para planta: a maioria tem quatro, o alho cinco e a acônito sete;</li>
 *   <li><b>a farinha de osso</b> adianta de duas idades até ao fim nas que aceitam, e só uma nas que não
 *       ({@code canFertilize} do original) — nenhuma a recusa de todo;</li>
 *   <li>a <b>mindrake</b> cresce uma vez e meia mais devagar que as outras;</li>
 *   <li>a <b>losna</b> empilha-se: feita, sobe outra em cima dela;</li>
 *   <li>e a <b>alcachofra-d'água</b> não vai em terra: ela planta na água.</li>
 * </ul>
 *
 * <p>O chão que as outras aceitam é o do original, e é mais largo que o do trigo: grama, terra, terra arada, a
 * própria planta e a losna.
 *
 * <p>O que cai quando se colhe é o que separa umas das outras, e está no {@link OccultaCrops}.
 */
public class WitchCropBlock extends CropBlock {
    /**
     * O jeito de cada planta.
     *
     * @param stages quantas idades ela tem — quatro, cinco ou sete, que são as do jogo
     * @param water  se planta na água, em vez de em terra
     * @param fertile se a farinha de osso a adianta de duas idades ou de uma só
     * @param slow   se cresce mais devagar (uma vez e meia) que as outras
     * @param stacks se, feita, sobe outra em cima dela
     */
    public record Traits(int stages, boolean water, boolean fertile, boolean slow, boolean stacks) {
    }

    /**
     * A caixa da planta: larga como a casa e de um quarto de altura, em qualquer idade — o
     * {@code setBlockBounds(0, 0, 0, 1, 0.25, 1)} do original, que não cresce com ela como a do trigo.
     */
    private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 4.0);

    /**
     * Onde a idade espera enquanto o bloco nasce.
     *
     * <p>O construtor do {@code CropBlock} pergunta pela idade — e monta a lista de estados com ela — antes de o
     * campo desta classe estar escrito, que é como o Java trabalha: primeiro o pai, depois o filho. Como cada
     * planta tem a sua (quatro, cinco ou sete), a resposta fica aqui até o construtor acabar. As três não podem
     * entrar todas na lista de estados: têm o mesmo nome, {@code age}, e o jogo recusa duas com o mesmo nome.
     */
    private static final ThreadLocal<IntegerProperty> EM_OBRAS = new ThreadLocal<>();

    private final Traits traits;
    private final IntegerProperty age;
    private final Supplier<ItemLike> seed;

    public WitchCropBlock(Properties properties, Traits traits, Supplier<ItemLike> seed) {
        super(anota(properties, traits));
        this.traits = traits;
        this.age = EM_OBRAS.get();
        this.seed = seed;
        EM_OBRAS.remove();
    }

    /** Deixa a idade no balcão antes de o pai a pedir, e devolve as propriedades como vieram. */
    private static Properties anota(Properties properties, Traits traits) {
        EM_OBRAS.set(switch (traits.stages()) {
            case 4 -> BlockStateProperties.AGE_4;
            case 5 -> BlockStateProperties.AGE_5;
            case 7 -> BlockStateProperties.AGE_7;
            default -> throw new IllegalArgumentException("idade que o jogo não tem: " + traits.stages());
        });
        return properties;
    }

    @Override
    public MapCodec<? extends CropBlock> codec() {
        throw new UnsupportedOperationException("as plantas do ofício não vão em estrutura nem em comando");
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(this.getAgeProperty());
    }

    @Override
    protected IntegerProperty getAgeProperty() {
        // enquanto o bloco nasce, o campo ainda está vazio e a resposta é a do balcão
        return this.age != null ? this.age : EM_OBRAS.get();
    }

    @Override
    public int getMaxAge() {
        return this.traits.stages();
    }

    /** A semente que ela dá, e que se planta para a ter. */
    @Override
    public ItemLike getBaseSeedId() {
        return this.seed.get();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * O {@code canPlaceBlockOn} do original: a alcachofra-d'água mora sobre a água; as outras aceitam grama,
     * terra, terra arada, outra da mesma planta e losna — a losna porque é sobre ela que a própria losna sobe.
     */
    @Override
    protected boolean mayPlaceOn(BlockState chão, BlockGetter level, BlockPos pos) {
        if (this.traits.water()) return chão.is(Blocks.WATER);
        if (chão.is(Blocks.GRASS_BLOCK) || chão.is(Blocks.DIRT) || chão.is(Blocks.FARMLAND)) return true;
        return chão.getBlock() instanceof WitchCropBlock outra
                && (outra == this || outra.traits.stacks());
    }

    /**
     * O {@code updateTick} do original: com luz bastante, a planta avança uma idade de vez em quando; a mindrake
     * demora uma vez e meia mais, e a losna feita sobe outra em cima de si.
     */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!hasSufficientLight(level, pos)) return;
        int idade = this.getAge(state);
        if (idade < this.getMaxAge()) {
            float ritmo = getGrowthSpeed(this, level, pos);
            if (this.traits.slow()) ritmo /= 1.5f;
            if (random.nextInt((int) (25.0f / ritmo) + 1) == 0) {
                level.setBlock(pos, this.getStateForAge(idade + 1), Block.UPDATE_INVISIBLE);
            }
        } else if (this.traits.stacks() && !level.getBlockState(pos.below()).is(this)
                && level.isEmptyBlock(pos.above())) {
            level.setBlock(pos.above(), this.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    /**
     * O {@code fertilize} do original: de duas idades até ao fim nas que aceitam farinha de osso, e de uma só nas
     * que não — a mindrake e a acônito.
     */
    @Override
    protected int getBonemealAgeIncrease(Level level) {
        if (!this.traits.fertile()) return 1;
        return net.minecraft.util.Mth.nextInt(level.getRandom(), 2, this.getMaxAge());
    }

    /** O {@code getDrops} do original, que não é o do trigo: está todo no {@link OccultaCrops}. */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return OccultaCrops.drops(this, state, params);
    }
}
