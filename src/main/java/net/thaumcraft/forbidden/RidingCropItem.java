package net.thaumcraft.forbidden;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

/**
 * O Chicote de Montaria: o {@code ItemRidingCrop} do Forbidden Magic 0.575.
 *
 * <p>Uma chicotada apressa quem a leva — o cavalo e o porco disparam, e a gente e o golem ganham pressa, força
 * e mão de obra. Quem o usa montado esporeia a montaria, que corre mas sente a dor.
 */
public class RidingCropItem extends Item {
    public RidingCropItem(Properties properties) {
        super(properties);
    }

    /** O {@code hitEntity}: a chicotada acelera quem leva, e no Nether ela às vezes arranca um fragmento. */
    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity alvo, LivingEntity quemBate) {
        stack.hurtAndBreak(1, quemBate, EquipmentSlot.MAINHAND);
        if (alvo instanceof net.minecraft.world.entity.animal.equine.AbstractHorse
                || alvo instanceof net.minecraft.world.entity.animal.pig.Pig) {
            alvo.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 5));
        } else if (alvo instanceof Player || alvo instanceof net.minecraft.world.entity.animal.golem.IronGolem) {
            alvo.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 1));
            alvo.addEffect(new MobEffectInstance(MobEffects.HASTE, 200, 1));
            alvo.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, 1));
        }
        Level level = quemBate.level();
        if (!level.isClientSide() && level.dimensionTypeRegistration().is(BuiltinDimensionTypes.NETHER)
                && level.getRandom().nextInt(15) == 1) {
            alvo.spawnAtLocation((net.minecraft.server.level.ServerLevel) level,
                    new ItemStack(ForbiddenItems.SHARDS.get("lust")), 1.0f);
        }
    }

    /** O {@code onItemRightClick}: montado, a chicotada vai para a montaria. */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player.getVehicle() instanceof LivingEntity montaria)) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        player.swing(hand);
        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            montaria.hurtServer(server, player.damageSources().generic(), 1.0f);
        }
        montaria.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 5));
        return InteractionResult.SUCCESS;
    }
}
