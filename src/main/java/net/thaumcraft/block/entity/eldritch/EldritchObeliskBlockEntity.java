package net.thaumcraft.block.entity.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.api.EldritchMob;
import net.thaumcraft.registry.TCBlockEntities;

/**
 * O obelisco eldritch: o {@code TileEldritchObelisk} da 4.2.3.5. Criatura eldritch a até seis blocos ganha força e
 * regeneração (se já não estiver regenerando), e do obelisco sai um fio de fogo-fátuo até ela. O original conta os
 * tiques num campo que nunca cresce, e o "a cada vinte tiques" acaba valendo todo tique; aqui também.
 */
public class EldritchObeliskBlockEntity extends BlockEntity {
    private int counter;

    public EldritchObeliskBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_OBELISK, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EldritchObeliskBlockEntity te) {
        AABB area = new AABB(pos).inflate(6.0);
        double cx = pos.getX() + 0.5, cy = pos.getY(), cz = pos.getZ() + 0.5;
        var near = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e instanceof EldritchMob && e.distanceToSqr(cx, cy, cz) <= 36.0);
        if (!level.isClientSide()) {
            if (te.counter % 20 != 0) return;
            for (LivingEntity e : near) {
                if (e.hasEffect(MobEffects.REGENERATION)) continue;
                e.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 0, true, true));
                e.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, true));
            }
            return;
        }
        for (LivingEntity e : near) {
            ObeliskFx.client.wisp(level, cx, pos.getY() + 1 + level.getRandom().nextFloat() * 3.0f, cz, e);
        }
    }

    /** O fio de fogo-fátuo ({@code wispFX4}); o desenho mora no cliente. */
    public static final class ObeliskFx {
        public static Client client = (level, x, y, z, target) -> {
        };

        public interface Client {
            void wisp(Level level, double x, double y, double z, LivingEntity target);
        }

        private ObeliskFx() {
        }
    }
}
