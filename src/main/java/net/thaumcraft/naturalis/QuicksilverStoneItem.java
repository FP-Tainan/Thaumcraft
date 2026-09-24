package net.thaumcraft.naturalis;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.research.Knowledges;

import java.util.ArrayList;

/**
 * A Pedra do Alquimista de Mercúrio: o outro lado do {@code AlchemicalStoneItem} do Magia Naturalis 0.5.0.
 *
 * <p>Usada, ela sobe um degrau em cada efeito que quem a segura estiver sentindo, cobrando um pó de brilho por
 * efeito e cortando o tempo deles para menos de um terço. Sem pó, ou com um efeito que já esteja no terceiro
 * degrau, ela cobra caro: cegueira, e veneno no segundo caso.
 *
 * <p>E quem ainda não descobriu Permutatio não consegue usá-la: fica só a cegueira.
 */
public class QuicksilverStoneItem extends Item {
    public QuicksilverStoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!Knowledges.of(player).hasDiscovered(Aspects.EXCHANGE)) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 144, 1));
            return InteractionResult.SUCCESS;
        }
        for (MobEffectInstance effect : new ArrayList<>(player.getActiveEffects())) {
            if (!consumeGlowstone(player)) {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 144, 1));
                break;
            }
            if (effect.getAmplifier() + 1 <= 2) {
                player.addEffect(new MobEffectInstance(effect.getEffect(), (int) (effect.getDuration() * 0.3f), effect.getAmplifier() + 1));
            } else {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 288, 2));
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 144, 0));
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** Um pó de brilho do inventário, que é o que cada degrau custa. */
    private static boolean consumeGlowstone(Player player) {
        if (player.getAbilities().instabuild) return true;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            var stack = player.getInventory().getItem(slot);
            if (!stack.is(Items.GLOWSTONE_DUST)) continue;
            stack.shrink(1);
            return true;
        }
        return false;
    }
}
