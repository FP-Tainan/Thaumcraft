package net.thaumcraft.arcana;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Runa</b> no chão: o {@code BlockGroundRuneSpell} do Ars Magica 2.
 *
 * <p>É um feitiço que <b>espera</b>. Ele fica desenhado no chão e não faz nada até alguém pisar em cima — e
 * então corre a frase naquela pessoa e se gasta. Quantas vezes ela aguenta, di-lo o modificador de Repetições:
 * uma só, se não houver nenhum.
 *
 * <p><b>Quem a pôs não a dispara.</b> É o {@code triggerOnCaster} do original: o mago pode andar por cima da
 * própria armadilha sem a acender, o que é o que a torna usável para guardar uma porta.
 *
 * <p>Ela guarda a <b>Afinidade</b> do feitiço num estado do bloco, e é daí que sai a cor com que se desenha —
 * uma runa de fogo é vermelha e uma de gelo é azul, sem ninguém ter escolhido.
 */
public class SpellRuneBlock extends BaseEntityBlock {
    public static final MapCodec<SpellRuneBlock> CODEC = simpleCodec(SpellRuneBlock::new);

    /** A Afinidade dela, de 0 a 10, que é o que lhe dá a cor. */
    public static final IntegerProperty AFFINITY = IntegerProperty.create("affinity", 0, 10);

    /** O quanto ela sobe do chão: os dois dedos do original. */
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);

    /** E a folga com que ela procura quem pisou: o oitavo de bloco do {@code setStateIfMobInteractsWithPlate}. */
    public static final double MARGIN = 0.125;

    public SpellRuneBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AFFINITY, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AFFINITY);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos onde, CollisionContext quem) {
        return SHAPE;
    }

    /** Ela não tem caixa de colisão: pisa-se por cima dela sem sentir. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos onde,
                                           CollisionContext quem) {
        return net.minecraft.world.phys.shapes.Shapes.empty();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos onde) {
        return level.getBlockState(onde.below()).isFaceSturdy(level, onde.below(),
                net.minecraft.core.Direction.UP);
    }

    /** Tirado o chão debaixo dela, a runa some — como a placa de pressão faz. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos onde, Block qual,
                                   @Nullable net.minecraft.world.level.redstone.Orientation lado,
                                   boolean mexeu) {
        if (!this.canSurvive(state, level, onde)) {
            level.destroyBlock(onde, false);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState state) {
        return new SpellRuneBlockEntity(onde, state);
    }

    /**
     * Alguém pisou: corre a frase nessa pessoa e gasta uma das vezes que a runa tinha.
     *
     * <p>Ela procura à volta com a folga do original em vez de olhar só para quem a tocou, porque um bicho
     * grande pisa no canto de um bloco e o jogo diz que ele tocou noutro.
     */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos onde, Entity quem,
                                net.minecraft.world.entity.InsideBlockEffectApplier aplicador,
                                boolean encosta) {
        if (!(level instanceof ServerLevel server)) return;
        if (!(level.getBlockEntity(onde) instanceof SpellRuneBlockEntity runa)) return;

        var roda = new net.minecraft.world.phys.AABB(
                onde.getX() - MARGIN, onde.getY(), onde.getZ() - MARGIN,
                onde.getX() + 1 + MARGIN, onde.getY() + 2.0, onde.getZ() + 1 + MARGIN);

        boolean acendeu = false;
        for (Entity outro : server.getEntities((Entity) null, roda, e -> e instanceof LivingEntity)) {
            if (runa.placedBy() != null && outro.getUUID().equals(runa.placedBy())) continue;
            if (runa.trigger(server, (LivingEntity) outro)) acendeu = true;
        }
        if (!acendeu) return;

        if (runa.spend()) server.removeBlock(onde, false);
    }
}
