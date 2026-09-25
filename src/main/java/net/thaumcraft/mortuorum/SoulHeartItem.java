package net.thaumcraft.mortuorum;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/**
 * O Coração de Alma: o {@code ItemSpawner} do Necromancy.
 *
 * <p>Clicado no chão, ele se gasta e de lá sai um dos três — o Isaac, o Ursinho Animado ou o Rastejador da Noite
 * —, escolhido a sorte, como no original.
 */
public class SoulHeartItem extends Item {
    /** Os três nomes do {@code necroNames}. */
    private static final EntityType<? extends Mob>[] BICHOS = new EntityType[]{
            MortuorumEntities.ISAAC_NORMAL, MortuorumEntities.TEDDY, MortuorumEntities.NIGHT_CRAWLER,
    };

    public SoulHeartItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        BlockPos onde = context.getClickedPos().relative(context.getClickedFace());
        EntityType<? extends Mob> tipo = BICHOS[level.getRandom().nextInt(BICHOS.length)];
        Mob bicho = tipo.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (bicho == null) return InteractionResult.PASS;
        bicho.snapTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5,
                net.minecraft.util.Mth.wrapDegrees(level.getRandom().nextFloat() * 360.0f), 0.0f);
        bicho.setYHeadRot(bicho.getYRot());
        bicho.yBodyRot = bicho.getYRot();
        bicho.finalizeSpawn(level, level.getCurrentDifficultyAt(onde), EntitySpawnReason.SPAWN_ITEM_USE, null);
        level.addFreshEntity(bicho);
        bicho.playAmbientSound();
        if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
