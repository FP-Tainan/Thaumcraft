package net.thaumcraft.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.api.TaintedMob;
import net.thaumcraft.entity.taint.ThaumicSlimeEntity;
import net.thaumcraft.registry.TCEffects;
import net.thaumcraft.registry.TCEntities;

/**
 * O começo do {@code livingTick(LivingDeathEvent)} do {@code EventHandlerEntity} da 4.2.3.5: quem morre com o fluxo da
 * mácula (e não é maculado) volta como a versão maculada de si — creeper, ovelha, vaca, porco, galinha e aldeão — ou,
 * qualquer outro, como um slime taumático do tamanho de um décimo da vida dele (de 1 a 7). O corpo some.
 */
public final class TaintConversion {
    private TaintConversion() {
    }

    /** @return se converteu (e então não há orbes de aspecto) */
    public static boolean convert(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel level) || entity instanceof TaintedMob || !entity.hasEffect(TCEffects.FLUX_TAINT)) return false;
        Mob tainted;
        if (entity instanceof Creeper) tainted = TCEntities.TAINT_CREEPER.create(level, net.minecraft.world.entity.EntitySpawnReason.CONVERSION);
        else if (entity instanceof Sheep) tainted = TCEntities.TAINT_SHEEP.create(level, net.minecraft.world.entity.EntitySpawnReason.CONVERSION);
        else if (entity instanceof AbstractCow) tainted = TCEntities.TAINT_COW.create(level, net.minecraft.world.entity.EntitySpawnReason.CONVERSION);
        else if (entity instanceof Pig) tainted = TCEntities.TAINT_PIG.create(level, net.minecraft.world.entity.EntitySpawnReason.CONVERSION);
        else if (entity instanceof Chicken) tainted = TCEntities.TAINT_CHICKEN.create(level, net.minecraft.world.entity.EntitySpawnReason.CONVERSION);
        else if (entity instanceof Villager) tainted = TCEntities.TAINT_VILLAGER.create(level, net.minecraft.world.entity.EntitySpawnReason.CONVERSION);
        else {
            ThaumicSlimeEntity slime = TCEntities.THAUMIC_SLIME.create(level, net.minecraft.world.entity.EntitySpawnReason.CONVERSION);
            if (slime != null) slime.setSize((int) (1.0f + Math.min(entity.getMaxHealth() / 10.0f, 6.0f)));
            tainted = slime;
        }
        if (tainted == null) return false;
        tainted.snapTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), 0.0f);
        level.addFreshEntity(tainted);
        if (!(entity instanceof Player)) entity.discard();
        return true;
    }
}
