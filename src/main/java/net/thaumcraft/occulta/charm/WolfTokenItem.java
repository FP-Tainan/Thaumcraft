package net.thaumcraft.occulta.charm;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.thaumcraft.occulta.vampire.Vampire;
import net.thaumcraft.occulta.wolf.Werewolf;

import java.util.function.Consumer;

/**
 * O <b>Token do Lobo</b>: o {@code ItemWolfToken} do Witchery.
 *
 * <p>Não é um item de jogo — é a <b>chave de fenda de quem fez o mod</b>. Segurando-o um segundo, sobe um
 * grau de <b>lobisomem</b>; agachado, um de <b>vampiro</b>; e os dois, passando do décimo, <b>voltam a
 * zero</b>.
 *
 * <p>Não tem receita, e não aparece em lugar nenhum do jogo: quem o quer, tira-o do criativo. Fica portado
 * porque é o que torna as duas maldições — que são as duas coisas mais longas do ramo — <b>possíveis de
 * conferir</b> sem passar três noites de lua cheia a cada mudança.
 *
 * <p>O original lhe dá o tom de <b>épico</b>, que é a piada: a coisa mais poderosa do mod é a que não conta.
 */
public class WolfTokenItem extends Item {
    /** Um segundo de segurar: o {@code secsToTicks(1)} do original. */
    public static final int SEGURAR = 20;

    /** O grau mais alto; passando dele, volta a zero. */
    public static final int TETO = 10;

    public WolfTokenItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        quem.startUsingItem(mão);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack token, LivingEntity quem) {
        return SEGURAR;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack token) {
        return ItemUseAnimation.BOW;
    }

    /**
     * Na <b>última batida</b> do segundo, o grau muda.
     *
     * <p>É o {@code onUsingTick} com {@code countdown == 1} do original: não é ao largar, é ao chegar ao fim
     * de segurar. Largar antes não faz nada, e é de propósito — um clique por acidente não muda um grau.
     */
    @Override
    public void onUseTick(Level level, LivingEntity quemUsa, ItemStack token, int falta) {
        if (!(level instanceof ServerLevel mundo) || falta != 1) return;
        if (!(quemUsa instanceof Player quem)) return;

        if (quem.isShiftKeyDown()) {
            int grau = Vampire.grauDe(quem) + 1;
            if (grau > TETO) grau = 0;
            Vampire.grau(quem, grau);
            quem.sendSystemMessage(Component.translatable("message.thaumcraft.vampire_setlevel", grau)
                    .withStyle(ChatFormatting.GREEN));
        } else {
            int grau = Werewolf.grauDe(quem) + 1;
            if (grau > TETO) grau = 0;
            Werewolf.grau(quem, grau);
            quem.sendSystemMessage(Component.translatable("message.thaumcraft.werewolf_setlevel", grau)
                    .withStyle(ChatFormatting.GREEN));
        }

        mundo.sendParticles(ParticleTypes.EXPLOSION, quem.getX(), quem.getY() + 1.0, quem.getZ(),
                8, 0.5, 0.5, 0.5, 0.0);
        mundo.playSound(null, quem.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.PLAYERS, 0.5f, 1.0f);
    }

    @Override
    public void appendHoverText(ItemStack token, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, net.minecraft.world.item.TooltipFlag bandeira) {
        for (String parte : new String[]{"tc.wolftoken.tip", "tc.wolftoken.tip2", "tc.wolftoken.tip3"}) {
            linha.accept(Component.translatable(parte).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
