package net.thaumcraft.occulta.death;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.function.Consumer;

/**
 * As <b>roupas da Morte</b>: o {@code ItemDeathsClothes} do Witchery.
 *
 * <p>São três peças — o <b>capuz</b>, o <b>manto</b> e as <b>botas</b> —, e não há calças: a Morte não usa
 * calças. Protegem como <b>ferro</b> e duram como <b>diamante</b>, que é a piada do original.
 *
 * <p>O que elas valem não é a proteção. É que, vestidas as três com a <b>Mão</b> na mão, <b>quem as veste
 * é a Morte</b>: o rito que a chama teleporta essa pessoa para o círculo em vez de criar o bicho. É a
 * melhor ideia deste ramo, e está escrita em cinco linhas do original.
 *
 * <h2>E o capuz faz duas coisas</h2>
 *
 * <p><b>Congela quem ele olha</b>: de dez em dez batidas, quem estiver na mira a até dezesseis blocos e
 * puder vê-lo fica <b>lento no terceiro grau</b> por três segundos. É o olhar da Morte, e é o mesmo que
 * ela própria faz.
 *
 * <p>E <b>vê no escuro</b>: estando num lugar com menos de oito de luz, ele dá visão noturna, renovada
 * antes de piscar. A Morte não tropeça.
 */
public class DeathsClothesItem extends Item {
    /** De quantas em quantas batidas o capuz olha, e os dois instantes dele. */
    public static final int COMPASSO = 10;
    public static final int O_OLHAR = 1;
    public static final int O_ESCURO = 2;

    /** Até onde o olhar dele chega, e o que ele faz. */
    public static final double ALCANCE = 16.0;
    public static final int CONGELA = 60;
    public static final int CONGELA_GRAU = 2;

    /** Abaixo de que luz ele acende, e por quanto tempo. */
    public static final int ESCURO = 8;
    public static final int VÊ_POR = 20 * 20;
    public static final int RENOVA_AOS = 15 * 20;

    private final String dica;

    public DeathsClothesItem(Properties propriedades, String dica) {
        super(propriedades);
        this.dica = dica;
    }

    /**
     * <b>Se alguém está com o conjunto vestido.</b> O original conta <b>três</b> peças das quatro casas —
     * e como só há três peças, são as três.
     */
    public static boolean conjuntoVestido(LivingEntity quem) {
        int quantas = 0;
        for (EquipmentSlot casa : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (quem.getItemBySlot(casa).getItem() instanceof DeathsClothesItem) quantas++;
        }
        return quantas >= 3;
    }

    /** O que o capuz faz, de dez em dez batidas. */
    @Override
    public void inventoryTick(ItemStack peça, ServerLevel level, Entity quem, EquipmentSlot casa) {
        if (casa != EquipmentSlot.HEAD || !(quem instanceof Player gente)) return;
        long instante = level.getGameTime() % COMPASSO;
        if (instante == O_OLHAR && gente instanceof net.minecraft.server.level.ServerPlayer deVerdade) {
            congela(level, deVerdade);
        } else if (instante == O_ESCURO) {
            acende(level, gente);
        }
    }

    /** <b>Congela quem ele olha</b>, se aquele o puder ver de volta. */
    public static void congela(ServerLevel level, net.minecraft.server.level.ServerPlayer gente) {
        HitResult mirou = net.thaumcraft.occulta.infusion.OverworldInfusion.olha(level, gente, ALCANCE);
        if (!(mirou instanceof EntityHitResult acertou)) return;
        if (!(acertou.getEntity() instanceof LivingEntity vítima)) return;
        if (!vítima.hasLineOfSight(gente) || vítima.hasEffect(MobEffects.SLOWNESS)) return;
        vítima.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, CONGELA, CONGELA_GRAU));
    }

    /** E <b>acende o escuro</b>, renovando antes de piscar. */
    public static void acende(ServerLevel level, Player gente) {
        if (level.getMaxLocalRawBrightness(gente.blockPosition()) >= ESCURO) return;
        var vendo = gente.getEffect(MobEffects.NIGHT_VISION);
        if (vendo != null && vendo.getDuration() > RENOVA_AOS) return;
        gente.removeEffect(MobEffects.NIGHT_VISION);
        gente.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, VÊ_POR, 0, true, false));
    }

    @Override
    public void appendHoverText(ItemStack peça, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, TooltipFlag bandeira) {
        for (String parte : this.dica.split(";")) {
            linha.accept(Component.translatable(parte).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
