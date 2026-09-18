package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.block.entity.WardingStoneBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;
import org.jetbrains.annotations.Nullable;

/**
 * As pedras de pavimento: os tipos dois (Viagem) e três (Proteção) do {@code BlockCosmeticSolid} da 4.2.3.5.
 *
 * <p>Quem pisa na de Viagem ganha dois segundos de velocidade II e de salto, com uma faísca verde. A de
 * Proteção, sem sinal de redstone, levanta em cima dela uma barreira de dois blocos que nenhum bicho atravessa —
 * gente passa — e empurra para trás o bicho que estiver no ar sobre ela; as runas dela contam o que está
 * acontecendo: azuis com redstone, vermelhas com a barreira tapada, lilases quando um bicho chega perto.
 */
public class PavingStoneBlock extends BaseEntityBlock {
    /** Os efeitos do lado de quem vê, que o cliente pendura aqui ao abrir. */
    public interface ClientEffects {
        void sparkle(BlockPos pos, int colour, int count);

        void runes(BlockPos pos, double y, float r, float g, float b, int duration, float gravity);
    }

    public static ClientEffects clientEffects = new ClientEffects() {
        @Override
        public void sparkle(BlockPos pos, int colour, int count) {
        }

        @Override
        public void runes(BlockPos pos, double y, float r, float g, float b, int duration, float gravity) {
        }
    };

    public static final MapCodec<PavingStoneBlock> TRAVEL_CODEC = simpleCodec(p -> new PavingStoneBlock(false, p));
    public static final MapCodec<PavingStoneBlock> WARDING_CODEC = simpleCodec(p -> new PavingStoneBlock(true, p));

    private final boolean warding;

    public PavingStoneBlock(boolean warding, Properties properties) {
        super(properties);
        this.warding = warding;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return this.warding ? WARDING_CODEC : TRAVEL_CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!this.warding && entity instanceof LivingEntity living) {
            if (level.isClientSide()) clientEffects.sparkle(pos, 0x008000, 5);
            living.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1));
            living.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 40, 0));
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return this.warding ? new WardingStoneBlockEntity(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!this.warding || level.isClientSide()) return null;
        return createTickerHelper(type, TCBlockEntities.WARDING_STONE, WardingStoneBlockEntity::tick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!this.warding) return;
        RandomSource r = level.getRandom();
        if (level.hasNeighborSignal(pos)) {
            for (int a = 0; a < 2; a++) {
                clientEffects.runes(pos, pos.getY() + 0.7f, 0.2f + r.nextFloat() * 0.4f, r.nextFloat() * 0.3f,
                        0.8f + r.nextFloat() * 0.2f, 20, -0.02f);
            }
            return;
        }
        // a barreira tapada: o original confere o bloco de cima duas vezes, e fica assim
        BlockState above = level.getBlockState(pos.above());
        BlockState above2 = level.getBlockState(pos.above(2));
        boolean passable = above.getCollisionShape(level, pos.above()).isEmpty();
        if (!above.is(TCBlocks.WARDING_BARRIER) && passable || !above2.is(TCBlocks.WARDING_BARRIER) && passable) {
            for (int a = 0; a < 3; a++) {
                clientEffects.runes(pos, pos.getY() + 0.7f, 0.9f + r.nextFloat() * 0.1f, r.nextFloat() * 0.3f,
                        r.nextFloat() * 0.3f, 24, -0.02f);
            }
            return;
        }
        for (Entity entity : level.getEntities((Entity) null, new AABB(pos).inflate(1.0))) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                clientEffects.runes(pos, pos.getY() + 0.6f + r.nextFloat() * Math.max(0.8f, entity.getEyeHeight()),
                        0.6f + r.nextFloat() * 0.4f, 0.0f, 0.3f + r.nextFloat() * 0.7f, 20, 0.0f);
                break;
            }
        }
    }
}
