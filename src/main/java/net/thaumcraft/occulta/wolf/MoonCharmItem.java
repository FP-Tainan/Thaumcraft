package net.thaumcraft.occulta.wolf;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * O <b>Amuleto da Lua</b>: o {@code ItemMoonCharm} do Witchery.
 *
 * <p>Ele faz duas coisas, e as duas são sobre <b>mandar na lua em vez de a obedecer</b>:
 *
 * <ol>
 *   <li><b>Na mochila</b>, sem se usar, ele <b>trava a forma</b>: a lua cheia passa e não transforma ninguém,
 *       e a lua que acaba não desfaz nada. É a única coisa que um lobisomem não larga ao virar bicho — e é de
 *       propósito, porque largá-lo seria perder a única coisa que desfaz a transformação.</li>
 *   <li><b>Segurado</b>, ele <b>muda a forma à vontade</b> — mas só a partir do <b>segundo grau</b>, que é
 *       quando um lobisomem passa a mandar em si. Agachado, e do <b>quinto grau</b> em diante, a forma que sai
 *       é a de <b>lobisomem</b>; de pé, a de <b>lobo</b>.</li>
 * </ol>
 *
 * <p>E ele <b>demora</b>: três segundos no primeiro grau, e <b>menos quanto maior o grau</b> — é a conta
 * {@code (grau - 1) * 4} do original, que faz de um lobisomem velho uma coisa que muda de forma quase de
 * imediato. O acônito no corpo não deixa nada disso acontecer.
 *
 * <p>Gasta-se: cinquenta mudanças, e consertam-se com ouro.
 */
public class MoonCharmItem extends Item {
    /** Quanto ele demora no primeiro grau, e o quanto cada grau lhe tira. */
    public static final int DEMORA = 60;
    public static final int POR_GRAU = 4;

    /** Quantas mudanças ele aguenta. */
    public static final int AGUENTA = 50;

    public MoonCharmItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack amuleto) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack amuleto, LivingEntity quem) {
        return DEMORA;
    }

    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        quem.startUsingItem(mão);
        return InteractionResult.CONSUME;
    }

    /**
     * A conta do original: a mudança acontece quando o que falta chega a {@code max((grau - 1) * 4, 1)}.
     *
     * <p>Dito do outro lado: um lobisomem de grau dez muda com <b>trinta e seis batidas</b> de aperto, e um
     * de grau um precisaria das sessenta inteiras — mas esse nem manda na mudança.
     */
    public static int quandoMuda(int grau) {
        return Math.max((grau - 1) * POR_GRAU, 1);
    }

    @Override
    public void onUseTick(Level level, LivingEntity quem, ItemStack amuleto, int falta) {
        if (level.isClientSide() || !(quem instanceof Player gente)) return;
        if (falta != quandoMuda(Werewolf.grauDe(gente))) return;
        if (Werewolf.temAcônito(gente)) return;
        if (!Werewolf.mandaNaMudança(gente)) return;

        Werewolf.Forma era = Werewolf.formaDe(gente);
        boolean querLobisomem = gente.isShiftKeyDown() && Werewolf.podeSerLobisomem(gente);
        Werewolf.Forma vai = switch (era) {
            case GENTE -> querLobisomem ? Werewolf.Forma.LOBISOMEM : Werewolf.Forma.LOBO;
            case LOBO -> querLobisomem ? Werewolf.Forma.LOBISOMEM : Werewolf.Forma.GENTE;
            case LOBISOMEM -> gente.isShiftKeyDown() ? Werewolf.Forma.GENTE : Werewolf.Forma.LOBO;
        };
        if (vai == era) return;

        Werewolf.forma(gente, vai);
        if (vai.éBicho() && level instanceof ServerLevel server) Werewolf.vira(server, gente, vai);

        amuleto.hurtAndBreak(1, gente, EquipmentSlot.MAINHAND);
        level.playSound(null, gente.getX(), gente.getY(), gente.getZ(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.PLAYERS, 1.0f, 1.0f);
        if (level instanceof ServerLevel server) {
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                    gente.getX(), gente.getY() + 1.0, gente.getZ(), 8, 0.5, 0.5, 0.5, 0.0);
        }
        gente.stopUsingItem();
    }

    @Override
    public void appendHoverText(ItemStack amuleto, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> linha, TooltipFlag flag) {
        linha.accept(Component.translatable("tc.mooncharm.holds").withStyle(ChatFormatting.BLUE));
        linha.accept(Component.translatable("tc.mooncharm.changes").withStyle(ChatFormatting.BLUE));
    }
}
