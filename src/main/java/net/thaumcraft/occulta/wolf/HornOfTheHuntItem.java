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
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.Spawn;

import java.util.function.Consumer;

/**
 * O <b>Chifre da Caça</b>: o {@code ItemHornOfTheHunt} do Witchery.
 *
 * <p>A Estátua do Lobisomem o dá ao <b>quarto grau</b>, e ele serve a uma coisa só: chamar o <b>Caçador
 * Cornudo</b>. Sopra-se por dois segundos e ele vem — entre dois e oito blocos, com o estouro do Wither —, e
 * o chifre <b>se parte</b>: aguenta uma e gasta duas.
 *
 * <p>É o único item do mod que se gasta para além do que tem, e é de propósito: o chifre não é uma ferramenta,
 * é uma <b>vez</b>. Quem quiser chamar o Caçador outra vez volta à estátua — e a estátua só o dá no quarto
 * grau, uma vez.
 *
 * <p>Quem não acha lugar para o Caçador não gasta o chifre: é o {@code spawnCreature} do original a devolver
 * nada, e o chifre fica inteiro.
 */
public class HornOfTheHuntItem extends Item {
    /** Dois segundos de sopro. */
    public static final int SOPRO = 40;

    /** Onde o Caçador aparece: entre dois e oito blocos. */
    public static final int PERTO = 2;
    public static final int LONGE = 8;

    /** O que ele aguenta, e o que o sopro lhe gasta. */
    public static final int AGUENTA = 1;
    public static final int GASTA = 2;

    public HornOfTheHuntItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack chifre) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack chifre, LivingEntity quem) {
        return SOPRO;
    }

    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        quem.startUsingItem(mão);
        return InteractionResult.CONSUME;
    }

    /**
     * O Caçador vem na <b>última batida</b> do sopro, e não no fim dele.
     *
     * <p>É uma batida de diferença e muda tudo: o original chama o bicho com o sopro ainda a tocar, e por
     * isso o barulho do chifre e o estouro da entrada se ouvem juntos.
     */
    @Override
    public void onUseTick(Level level, LivingEntity quem, ItemStack chifre, int falta) {
        if (falta != 1 || !(level instanceof ServerLevel server) || !(quem instanceof Player gente)) return;

        level.playSound(null, gente.getX(), gente.getY(), gente.getZ(), SoundEvents.GOAT_HORN_SOUND_VARIANTS
                        .getFirst().value(), SoundSource.PLAYERS, 1.0f, 1.0f);

        var bicho = Spawn.perto(server, OccultaEntities.HORNED_HUNTSMAN, gente.blockPosition(), PERTO, LONGE);
        if (!(bicho instanceof HornedHuntsmanEntity caçador)) return;

        caçador.entradaEstoura();
        caçador.setPersistenceRequired();
        caçador.acendeAEspera();

        var meio = Spawn.meio(caçador);
        server.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                meio.x, meio.y, meio.z, 16, 1.0, caçador.getBbHeight(), 1.0, 0.0);
        server.playSound(null, caçador.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE,
                1.0f, 1.0f);

        chifre.hurtAndBreak(GASTA, gente, EquipmentSlot.MAINHAND);
        gente.stopUsingItem();
    }

    @Override
    public void appendHoverText(ItemStack chifre, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> linha, TooltipFlag flag) {
        linha.accept(Component.translatable("tc.hornofthehunt.tip").withStyle(ChatFormatting.DARK_RED));
    }
}
