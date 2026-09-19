package net.thaumcraft.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.entity.GolemEntity;
import net.thaumcraft.registry.TCSounds;

import java.util.List;

/**
 * A espada do núcleo de ar: o {@code ItemElementalSword} da 4.2.3.5. Segurando o clique direito, o redemoinho ergue quem
 * a segura (e amortece a queda), empurra para longe o que estiver a dois blocos e meio e gasta um de uso por segundo; o
 * golpe acerta também as criaturas coladas no alvo (menos os golens e bichos de quem bate).
 */
public class ElementalSwordItem extends Item {
    public ElementalSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
        int ticks = this.getUseDuration(stack, player) - count;
        Vec3 m = player.getDeltaMovement();
        double my = m.y;
        if (my < 0.0) {
            my /= 1.2f;
            player.fallDistance /= 1.2f;
        }
        my += 0.08f;
        if (my > 0.5) my = 0.2f;
        player.setDeltaMovement(m.x, my, m.z);
        if (player instanceof ServerPlayer server) server.connection.resetFlyingTicks();
        for (Entity entity : level.getEntities(player, player.getBoundingBox().inflate(2.5))) {
            if (entity instanceof Player || entity.isRemoved() || player.getVehicle() == entity) continue;
            Vec3 p = player.position(), t = entity.position();
            double distance = p.distanceTo(t) + 0.1;
            Vec3 r = t.subtract(p);
            entity.setDeltaMovement(entity.getDeltaMovement().add(r.x / 2.5 / distance, r.y / 2.5 / distance, r.z / 2.5 / distance));
        }
        if (level.isClientSide()) {
            int miny = (int) (player.getBoundingBox().minY - 2.0);
            if (player.onGround()) miny = Mth.floor(player.getBoundingBox().minY);
            if (ToolFx.client != null) {
                for (int a = 0; a < 5; a++) {
                    ToolFx.client.smokeSpiral(level, player.getX(), player.getBoundingBox().minY + player.getBbHeight() / 2.0f, player.getZ(), 1.5f,
                            level.getRandom().nextInt(360), miny, 14540253);
                }
            }
            if (player.onGround()) {
                float r1 = level.getRandom().nextFloat() * 360.0f;
                float mx = -Mth.sin(r1 / 180.0f * (float) Math.PI) / 5.0f;
                float mz = Mth.cos(r1 / 180.0f * (float) Math.PI) / 5.0f;
                level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, player.getX(), player.getBoundingBox().minY + 0.1f, player.getZ(), mx, 0.0, mz);
            }
        } else if (ticks == 0 || ticks % 20 == 0) {
            level.playSound(null, player, TCSounds.WIND.value(), SoundSource.PLAYERS, 0.5f, 0.9f + level.getRandom().nextFloat() * 0.2f);
        }
        if (ticks % 20 == 0 && !level.isClientSide()) stack.hurtAndBreak(1, player, player.getUsedItemHand());
    }

    /** O {@code onLeftClickEntity}: o golpe largo, nas criaturas coladas no alvo. */
    public static InteractionResult sweep(Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof ElementalSwordItem) || !entity.isAlive() || !(level instanceof ServerLevel server)) return InteractionResult.PASS;
        List<Entity> targets = level.getEntities(player, entity.getBoundingBox().inflate(1.2, 1.1, 1.2));
        int count = 0;
        if (targets.size() > 1) {
            for (Entity e : targets) {
                if (e.isRemoved() || e.getId() == entity.getId() || !e.isAlive() || !(e instanceof Mob)) continue;
                if (e instanceof GolemEntity golem && golem.getOwnerName().equals(player.getName().getString())) continue;
                if (e instanceof OwnableEntity pet && pet.getOwnerReference() != null && player.getUUID().equals(pet.getOwnerReference().getUUID())) continue;
                float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                var source = server.damageSources().playerAttack(player);
                damage = net.minecraft.world.item.enchantment.EnchantmentHelper.modifyDamage(server, held, e, source, damage);
                boolean crit = player.fallDistance > 0.0f && !player.onGround() && !player.onClimbable() && !player.isInWater()
                        && !player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS) && !player.isPassenger();
                if (crit) damage *= 1.5f;
                if (e.hurtServer(server, source, damage)) {
                    if (e instanceof LivingEntity living) {
                        net.minecraft.world.item.enchantment.EnchantmentHelper.doPostAttackEffects(server, living, source);
                        if (crit) player.crit(e);
                    }
                    player.setLastHurtMob(e);
                }
                player.causeFoodExhaustion(0.3f);
                count++;
            }
            if (count > 0) level.playSound(null, entity, TCSounds.SWING.value(), SoundSource.PLAYERS, 1.0f, 0.9f + level.getRandom().nextFloat() * 0.2f);
        }
        return InteractionResult.PASS;
    }
}
