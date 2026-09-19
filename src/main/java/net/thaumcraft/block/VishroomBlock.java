package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * O cogumelo-vis: o sexto tipo do {@code BlockCustomPlant} da 4.2.3.5. Nasce na Floresta Mágica, junto das toras;
 * brilha com luz oito, solta de vez em quando uma chaminha roxa que encolhe e cai, e quem encosta nele fica tonto
 * por dez segundos.
 */
public class VishroomBlock extends VegetationBlock {
    public interface ClientEffects {
        void spore(double x, double y, double z);
    }

    public static ClientEffects clientEffects = (x, y, z) -> {
    };

    public static final MapCodec<VishroomBlock> CODEC = simpleCodec(VishroomBlock::new);
    /** A caixa do {@code BlockCustomPlant}: quatro décimos para cada lado do meio, oito décimos de altura. */
    private static final VoxelShape SHAPE = Block.box(1.6, 0.0, 1.6, 14.4, 12.8, 14.4);

    public VishroomBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** O {@code onEntityCollidedWithBlock}: náusea por duzentos tiques. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        if (level instanceof ServerLevel && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0) return;
        RandomSource r = level.getRandom();
        clientEffects.spore(pos.getX() + 0.5f + (r.nextFloat() - r.nextFloat()) * 0.4f, pos.getY() + 0.3f,
                pos.getZ() + 0.5f + (r.nextFloat() - r.nextFloat()) * 0.4f);
    }
}
