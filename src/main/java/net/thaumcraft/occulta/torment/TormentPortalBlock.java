package net.thaumcraft.occulta.torment;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * O Portal do Tormento: a {@code BlockTormentPortal} do Witchery.
 *
 * <p>É a <b>única</b> saída do labirinto, e ela mente. Quem a atravessa, estando no Tormento, volta para
 * casa <b>dezenove vezes em vinte</b> — e na vigésima vai para outro andar, e tem de atravessar outro
 * labirinto para achar outra porta que talvez minta outra vez.
 *
 * <p>É a melhor ideia do Witchery. Um labirinto tem saída; este tem uma <b>porta que às vezes é saída</b>, e
 * a diferença entre as duas coisas é a diferença entre um lugar difícil e um castigo.
 *
 * <p>Ele não se monta e não se apanha: nasce com o labirinto, é indestrutível e não deixa nada. Faz luz de
 * três quartos, tem a cor de carne crua e a casa dele não tem colisão nenhuma — quem lhe toca, passa.
 */
public class TormentPortalBlock extends Block {
    public static final MapCodec<TormentPortalBlock> CODEC = simpleCodec(TormentPortalBlock::new);

    /** Uma em vinte: o {@code MORE_TORMENT_CHANCE}. */
    public static final double MAIS_TORMENTO = 0.05;

    /** A cor dele: o {@code 16711714} que o original devolve, que é um vermelho de carne. */
    public static final int TINT = 0xFFFF0022;

    /** A luz: três quartos. */
    public static final int LUZ = 12;

    /** Duas partículas de chama por batida, e o som do portal uma vez em cem. */
    public static final int CHAMAS = 2;
    public static final int SOM_UMA_EM = 100;

    /**
     * Em que eixo o vão está deitado, como o portal do Nether do jogo e como o Portal do Espírito.
     *
     * <p><b>Desvio declarado:</b> o original não guarda isto em lugar nenhum — ele olha os dois vizinhos em
     * <b>x</b> a cada quadro e decide a caixa dali. Hoje a caixa de um bloco vem do <b>feitio</b> dele, e um
     * desenho que mudasse com o vizinho precisaria de mudar o feitio. No labirinto dá no mesmo: ali o portal
     * tem sempre Pedra do Tormento dos dois lados, e a conta do original dá sempre o mesmo eixo — o que
     * nasce por omissão aqui.
     */
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<
            net.minecraft.core.Direction.Axis> AXIS =
            net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_AXIS;

    /** A casa dele é uma fatia de dois cantos de largura, deitada no eixo que o vão atravessa. */
    private static final VoxelShape X_SHAPE = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    private static final VoxelShape Z_SHAPE = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

    public TormentPortalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AXIS, net.minecraft.core.Direction.Axis.X));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(
            net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        return state.getValue(AXIS) == net.minecraft.core.Direction.Axis.Z ? Z_SHAPE : X_SHAPE;
    }

    /** E não há colisão: é o {@code getCollisionBoundingBoxFromPool} devolvendo nulo. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of();
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state,
                                          boolean comOsDados) {
        return ItemStack.EMPTY;
    }

    /**
     * O {@code onEntityCollidedWithBlock}: estando no Tormento, manda para casa — menos uma vez em vinte,
     * em que manda para outro andar. Estando fora, manda para o Tormento.
     *
     * <p>Quem está montado ou carregando alguém não passa, que é a guarda do original: um portal não
     * desmonta cavaleiro nenhum.
     */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity quem,
                                InsideBlockEffectApplier efeitos, boolean atravessou) {
        super.entityInside(state, level, pos, quem, efeitos, atravessou);
        if (!(level instanceof ServerLevel server)) return;
        if (!(quem instanceof ServerPlayer gente)) return;
        if (quem.isPassenger() || quem.isVehicle()) return;
        if (Torment.order(gente).oquê() != Torment.NADA) return;

        if (Torment.is(server) && !(server.getRandom().nextDouble() < MAIS_TORMENTO)) {
            Torment.order(gente, Torment.ACABA, -1);
        } else {
            Torment.order(gente, Torment.COMEÇA, -1);
        }
    }

    /** A chama e o barulho do original. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource sorte) {
        if (sorte.nextInt(SOM_UMA_EM) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5f,
                    sorte.nextFloat() * 0.4f + 0.8f, false);
        }
        for (int k = 0; k < CHAMAS; k++) {
            level.addParticle(ParticleTypes.FLAME, pos.getX() + sorte.nextFloat(),
                    pos.getY() + sorte.nextFloat(), pos.getZ() + sorte.nextFloat(), 0.0, 0.0, 0.0);
        }
    }
}
