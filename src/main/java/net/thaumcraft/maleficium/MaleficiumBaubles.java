package net.thaumcraft.maleficium;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thaumcraft.api.RunicArmor;
import net.thaumcraft.api.baubles.BaubleItem;
import net.thaumcraft.api.baubles.BaubleType;
import net.thaumcraft.baubles.Baubles;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.research.WarpEvents;

/**
 * As bijuterias do Maleficium: o anel de Lumos e a faixa do caminhante do vazio.
 */
public final class MaleficiumBaubles {
    private MaleficiumBaubles() {
    }

    /** Quem usa a faixa com o empurrão ligado? É o que dobra a passada das botas. */
    public static boolean speedSash(Player player) {
        ItemStack belt = Baubles.container(player).getItem(Baubles.BELT);
        return belt.getItem() instanceof VoidwalkerSashItem && VoidwalkerSashItem.speedOn(belt);
    }

    /**
     * O anel de Lumos: o {@code ItemLumosRing} do original. Vestido, dá visão noturna para sempre — que é o mesmo
     * que o foco de Lumos faz na mão.
     */
    public static class LumosRingItem extends Item implements BaubleItem {
        public LumosRingItem(Properties properties) {
            super(properties);
        }

        @Override
        public BaubleType baubleType(ItemStack stack) {
            return BaubleType.RING;
        }

        @Override
        public void onWornTick(ItemStack stack, LivingEntity wearer) {
            if (wearer.level().isClientSide() || wearer.tickCount % 20 != 0) return;
            wearer.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false));
        }

        @Override
        public void onUnequipped(ItemStack stack, LivingEntity wearer) {
            if (!wearer.level().isClientSide()) wearer.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    /**
     * A faixa do caminhante do vazio: o {@code ItemVoidwalkerSash} do original. Vinte cargas de escudo rúnico, dois
     * de distorção, e um empurrão que se liga e desliga agachando com ela na mão — com ele ligado, as botas do
     * caminhante andam o dobro e o pulo sobe um pouco mais.
     */
    public static class VoidwalkerSashItem extends Item implements BaubleItem, RunicArmor, WarpEvents.WarpingGear {
        public VoidwalkerSashItem(Properties properties) {
            super(properties);
        }

        /** O {@code isSpeedEnabled}: sem marca nenhuma, a faixa vem ligada. */
        public static boolean speedOn(ItemStack stack) {
            return !Boolean.FALSE.equals(stack.get(TCComponents.SASH_SPEED));
        }

        @Override
        public BaubleType baubleType(ItemStack stack) {
            return BaubleType.BELT;
        }

        @Override
        public int runicCharge(ItemStack stack) {
            return 20;
        }

        @Override
        public int getWarp(ItemStack stack, Player player) {
            return 2;
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!player.isShiftKeyDown()) return InteractionResult.PASS;
            if (!level.isClientSide()) {
                boolean ligado = !speedOn(stack);
                stack.set(TCComponents.SASH_SPEED, ligado);
                player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                        ligado ? "text.thaumcraft.sash.speed.on" : "text.thaumcraft.sash.speed.off")
                        .withStyle(ligado ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.RED));
            }
            return InteractionResult.SUCCESS;
        }
    }
}
