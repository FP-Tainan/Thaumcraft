package net.thaumcraft.maleficium;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.item.Focuses;
import net.thaumcraft.entity.ExplosiveOrbEntity;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.research.WarpEvents;

import java.util.function.Consumer;

/**
 * As Lâminas de Fortaleza: o {@code ItemKatana} do Tainted Magic 8.1.1, que ali eram três subtipos de um item só — de
 * táumio, de metal do vazio e de metal das sombras.
 *
 * <p>Seguro o clique direito por um segundo e solto, o golpe carregado fere uma vez e meia (de vez em quando, duas e
 * meia) quem estiver na mira. Se a lâmina tiver uma das três inscrições e quem a segura não estiver agachado, o mesmo
 * golpe solta o poder da inscrição e a lâmina descansa sete segundos.
 */
public class FortressBladeItem extends Item implements WarpEvents.WarpingGear {
    /** A inscrição do Demônio Furioso: a bola de fogo. */
    public static final int INSCRIPTION_FIRE = 0;
    /** A do Espírito Vingativo: a onda de choque. */
    public static final int INSCRIPTION_THUNDER = 1;
    /** A da Deusa Benevolente: a cura. */
    public static final int INSCRIPTION_HEAL = 2;

    /** Quanto tempo o golpe leva para carregar, e quanto a lâmina descansa depois de uma inscrição. */
    public static final int CHARGE_TICKS = 20;
    public static final int COOLDOWN_TICKS = 140;

    /** O que só quem vê desenha: as faíscas da cura e o rastro da onda de choque. */
    public interface Effects {
        void heal(Level level, Player player);

        void shockwave(Level level, Player player, Entity target);
    }

    public static Effects clientEffects;

    /** Zero táumio, um metal do vazio, dois metal das sombras. */
    private final int tier;
    private final float attackDamage;

    public FortressBladeItem(Properties properties, int tier, float attackDamage) {
        super(properties);
        this.tier = tier;
        this.attackDamage = attackDamage;
    }

    public float attackDamage() {
        return this.attackDamage;
    }

    // ------------------------------------------------------------------ a inscrição e o descanso

    public static int inscription(ItemStack stack) {
        Integer mark = stack.get(TCComponents.KATANA_INSCRIPTION);
        return mark == null ? -1 : mark;
    }

    public static int cooldown(ItemStack stack) {
        Integer rest = stack.get(TCComponents.KATANA_COOLDOWN);
        return rest == null ? 0 : rest;
    }

    // ------------------------------------------------------------------ o golpe de perto

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        Level level = attacker.level();
        if (level instanceof ServerLevel server
                && (!(target instanceof Player) || !(attacker instanceof Player) || server.isPvpAllowed())) {
            if (this.tier >= 1) target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60));
            if (this.tier >= 2) target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120));
            switch (inscription(stack)) {
                case INSCRIPTION_FIRE -> target.igniteForSeconds(3.0f);
                case INSCRIPTION_THUNDER -> target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140));
                case INSCRIPTION_HEAL -> target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60));
                default -> {
                }
            }
        }
        level.playSound(null, attacker, TCSounds.SWING.value(), SoundSource.PLAYERS,
                0.5f + level.getRandom().nextFloat(), 0.5f + level.getRandom().nextFloat());
    }

    // ------------------------------------------------------------------ o golpe carregado

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (cooldown(player.getItemInHand(hand)) > 0) return InteractionResult.PASS;
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        if (entity.tickCount % 5 != 0) return;
        float f = 0.75f + (float) Math.random() * 0.25f;
        level.playSound(null, entity, TCSounds.WIND.value(), SoundSource.PLAYERS, f * 0.1f, f);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (!(entity instanceof Player player)) return false;
        if (this.getUseDuration(stack, entity) - remaining < CHARGE_TICKS) return false;
        if (inscription(stack) >= 0 && !player.isShiftKeyDown()) {
            this.inscriptionStrike(stack, level, player);
            stack.set(TCComponents.KATANA_COOLDOWN, COOLDOWN_TICKS);
        } else {
            this.chargedStrike(stack, level, player);
        }
        player.swing(player.getUsedItemHand());
        return true;
    }

    /** O golpe sem inscrição: uma vez e meia o dano em quem estiver na mira — de vez em quando, duas e meia. */
    private void chargedStrike(ItemStack stack, Level level, Player player) {
        level.playSound(null, player, TCSounds.SWING.value(), SoundSource.PLAYERS,
                0.5f + level.getRandom().nextFloat(), 0.5f + level.getRandom().nextFloat());
        if (!(level instanceof ServerLevel server)) return;
        Entity target = Focuses.pointedEntity(level, player, 5.0);
        if (target == null || !target.isAlive()) return;
        float multiplier = level.getRandom().nextInt(10) == 0 ? 2.5f : 1.5f;
        target.hurtServer(server, player.damageSources().playerAttack(player), this.attackDamage * multiplier);
    }

    /** O poder da inscrição gravada. */
    private void inscriptionStrike(ItemStack stack, Level level, Player player) {
        switch (inscription(stack)) {
            case INSCRIPTION_FIRE -> {
                ExplosiveOrbEntity orb = new ExplosiveOrbEntity(level, player);
                orb.strength = this.attackDamage * 0.25f;
                orb.setPos(orb.position().add(orb.getDeltaMovement()));
                if (!level.isClientSide()) level.addFreshEntity(orb);
            }
            case INSCRIPTION_THUNDER -> {
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                        player.getBoundingBox().inflate(10.0), e -> e != player && e.isAlive() && !e.isInvulnerable())) {
                    if (level instanceof ServerLevel server) {
                        target.hurtServer(server, level.damageSources().magic(), this.attackDamage * 0.25f);
                    }
                    Vec3 away = target.position().subtract(player.position()).normalize();
                    target.push(away.x * 5.0, 1.5, away.z * 5.0);
                    target.hurtMarked = true;
                    if (level.isClientSide() && clientEffects != null) clientEffects.shockwave(level, player, target);
                }
                level.playSound(null, player, TCSounds.SHOCKWAVE.value(), SoundSource.PLAYERS, 5.0f,
                        1.5f * level.getRandom().nextFloat());
            }
            case INSCRIPTION_HEAL -> {
                player.heal((player.getMaxHealth() - player.getHealth()) * 0.5f);
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                        player.getBoundingBox().inflate(5.0), e -> e != player && e.isAlive() && !e.isInvulnerable())) {
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                }
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1));
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 120, 1));
                if (level.isClientSide() && clientEffects != null) clientEffects.heal(level, player);
                level.playSound(null, player, TCSounds.WAND.value(), SoundSource.PLAYERS, 1.0f,
                        0.9f + (float) Math.random() * 0.1f);
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ o descanso e a dica

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        int rest = cooldown(stack);
        if (rest > 0) stack.set(TCComponents.KATANA_COOLDOWN, rest - 1);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        if (this.tier == 1) lines.accept(Component.translatable("enchantment.special.sapless").withStyle(ChatFormatting.GOLD));
        if (this.tier == 2) lines.accept(Component.translatable("enchantment.special.sapgreat").withStyle(ChatFormatting.GOLD));
        lines.accept(Component.literal(" "));
        lines.accept(Component.literal("+" + this.attackDamage + " ")
                .append(Component.translatable("text.attackdamage")).withStyle(ChatFormatting.BLUE));
        int mark = inscription(stack);
        if (mark >= 0) {
            lines.accept(Component.translatable("text.katana.inscription." + mark).withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return this.tier == 0 ? 0 : (this.tier == 1 ? 3 : 7);
    }
}
