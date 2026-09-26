package net.thaumcraft.shattered;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A fenda solta: o {@code BlockFloatingRift} das Portas Dimensionais — o rasgão que fica no ar onde uma porta
 * esteve, ou onde a Assinatura de Fenda foi usada.
 *
 * <p>Não tem corpo: atravessa-se, e quem a atravessa sai do outro lado dela.
 *
 * <p><b>Do original fica de fora, por enquanto,</b> o desenho: lá a fenda é um rasgão que se abre e se fecha no ar,
 * com o vazio a ver-se por dentro. Aqui ela ainda não se vê — fatia à parte.
 */
public class FloatingRiftBlock extends BaseEntityBlock {
    public static final MapCodec<FloatingRiftBlock> CODEC = simpleCodec(FloatingRiftBlock::new);

    public FloatingRiftBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RiftBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /** A fenda cresce sozinha e sorteia o rosto dela assim que nasce. */
    @Override
    public <T extends BlockEntity> @Nullable net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> tipo) {
        if (level.isClientSide()) return null;
        return (mundo, onde, feitio, be) -> {
            if (!(be instanceof RiftBlockEntity fenda)) return;
            fenda.rollFace(mundo.getRandom());
            fenda.grow();
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effects, boolean past) {
        if (!(level instanceof ServerLevel server)) return;
        if (entity.isOnPortalCooldown()) return;
        if (!(server.getBlockEntity(pos) instanceof RiftBlockEntity fenda)) return;
        if (fenda.destination() == null) return;
        entity.setPortalCooldown(50);
        if (fenda.teleport(entity)) entity.setPortalCooldown(0);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    /** A fenda vai desfazendo o mundo em volta, e do que ela come sai o Fio do Mundo. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        // a fenda presa pelo Firma-Fendas não come mais nada
        if (level.getBlockEntity(pos) instanceof RiftBlockEntity fenda && fenda.stabilized()) return;
        RiftDecay.bite(level, pos, random);
    }

    /** As fagulhas que saem dela, do lado de quem vê. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 3; i++) {
            level.addParticle(net.minecraft.core.particles.ParticleTypes.PORTAL,
                    pos.getX() + 0.5 + random.nextGaussian() * 0.3,
                    pos.getY() + 0.5 + random.nextGaussian() * 0.3,
                    pos.getZ() + 0.5 + random.nextGaussian() * 0.3,
                    random.nextGaussian() * 0.05, random.nextGaussian() * 0.05, random.nextGaussian() * 0.05);
        }
    }
}
