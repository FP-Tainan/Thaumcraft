package net.thaumcraft.maleficium;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.research.WarpEvents;

import java.util.List;
import java.util.function.Consumer;

/**
 * A Chave do Portão Celeste: o {@code ItemGateKey} do Tainted Magic 8.1.1. Clicada num bloco, prende-se ao lugar logo
 * acima dele e ganha uma cor sua; depois, segurando o clique direito por dois segundos, leva quem a usa de volta para
 * lá — desde que seja no mesmo mundo e o lugar esteja desimpedido. A viagem deixa cego por oito segundos.
 */
public class GateKeyItem extends Item implements WarpEvents.WarpingGear {
    /** As faíscas, que só quem vê desenha. */
    public interface Effects {
        void sparkle(Level level, double x, double y, double z);
    }

    public static Effects clientEffects;

    public GateKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 40;
    }

    /** O {@code onItemUse}: prende a chave ao lugar, uma vez só. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        if (stack.has(TCComponents.GATE_KEY_TARGET)) return InteractionResult.PASS;
        BlockPos above = context.getClickedPos().above();
        if (!level.isClientSide()) {
            stack.set(TCComponents.GATE_KEY_TARGET, GlobalPos.of(level.dimension(), above));
            // a cor do brilho, sorteada como no original (um a trezentos e sessenta graus de matiz)
            int hue = level.getRandom().nextInt(360) + 1;
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(),
                    List.of(java.awt.Color.HSBtoRGB(hue / 360.0f, 1.0f, 0.8f))));
        } else {
            BlockPos at = context.getClickedPos();
            if (clientEffects != null) {
                for (int i = 0; i < 9; i++) clientEffects.sparkle(level, at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
            }
        }
        level.playSound(context.getPlayer(), context.getClickedPos(), TCSounds.WAND.value(), SoundSource.PLAYERS,
                0.5f + level.getRandom().nextFloat() * 0.5f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.getItemInHand(hand).has(TCComponents.GATE_KEY_TARGET)) return InteractionResult.PASS;
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        if (level.isClientSide() && clientEffects != null) clientEffects.sparkle(level, entity.getX(), entity.getBoundingBox().minY, entity.getZ());
        if (entity.tickCount % 5 == 0) {
            float f = 1.0f + (float) Math.random() * 0.25f;
            level.playSound(null, entity, TCSounds.WIND.value(), SoundSource.PLAYERS, f * 0.1f, f);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        GlobalPos target = stack.get(TCComponents.GATE_KEY_TARGET);
        if (target == null || !(entity instanceof Player player)) return stack;
        if (!target.dimension().equals(level.dimension())) {
            message(player, "key.invaliddim");
            return stack;
        }
        BlockPos pos = target.pos();
        if (pos.getY() <= level.getMinY() || !level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
            message(player, "key.error");
            return stack;
        }
        if (level instanceof ServerLevel server && player instanceof ServerPlayer sent) {
            sent.teleport(new TeleportTransition(server, new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5),
                    Vec3.ZERO, player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING));
            level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 5.0f, 1.0f);
        }
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 160, 0));
        return stack;
    }

    /** O {@code HUDHandler.displayString}: o aviso em vermelho por cima da barra. */
    private static void message(Player player, String key) {
        if (player instanceof ServerPlayer sent) {
            sent.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.RED), true);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        GlobalPos target = stack.get(TCComponents.GATE_KEY_TARGET);
        if (target == null) {
            lines.accept(Component.translatable("key.unbound").withStyle(ChatFormatting.RED));
            return;
        }
        lines.accept(Component.translatable("key.bound").withStyle(ChatFormatting.GREEN));
        lines.accept(Component.literal(target.dimension().identifier().toString()).withStyle(ChatFormatting.GRAY));
        BlockPos pos = target.pos();
        lines.accept(Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return 3;
    }
}
