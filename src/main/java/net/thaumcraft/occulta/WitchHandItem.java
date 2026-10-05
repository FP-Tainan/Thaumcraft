package net.thaumcraft.occulta;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.thaumcraft.occulta.infusion.Infusion;
import net.thaumcraft.occulta.infusion.Infusions;

/**
 * A <b>Mão de Bruxa</b>: o {@code ItemWitchHand} do Witchery.
 *
 * <p>Uma mão cortada, seca, que não faz absolutamente nada por si. O que ela faz é ser <b>a única coisa
 * que a {@linkplain Infusion infusão} sabe atravessar</b>: segurá-la, socar com ela, largá-la — tudo isso
 * chega à infusão de quem a tem, e sem ela a infusão fica calada.
 *
 * <p>E ela <b>não se fabrica</b>. Cai de uma <b>bruxa morta</b>, uma vez em três — ou uma em duas, se quem
 * a matou tinha a <b>Arthana</b> na mão.
 *
 * <p>Junte as duas coisas e veja o que o original está dizendo: para usar o poder que você pôs dentro de si,
 * você precisa da mão de alguém que o tinha. O ofício não é gentil.
 */
public class WitchHandItem extends Item {
    /** Quanto a mão larga de uma bruxa morta, e quanto larga se quem a matou tinha a Arthana. */
    public static final double CAI = 0.33;
    public static final double CAI_COM_FACA = 0.5;

    public WitchHandItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack oquê) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack oquê, LivingEntity quem) {
        return Infusions.SEGURA;
    }

    @Override
    public InteractionResult use(Level mundo, Player quem, InteractionHand mão) {
        quem.startUsingItem(mão);
        return InteractionResult.CONSUME;
    }

    /** Cada batida com ela segurada vai para a infusão. */
    @Override
    public void onUseTick(Level mundo, LivingEntity quem, ItemStack oquê, int faltam) {
        if (!(mundo instanceof ServerLevel level) || !(quem instanceof ServerPlayer gente)) return;
        Infusions.de(gente).segurando(level, gente, oquê, faltam);
    }

    /** E largá-la também. */
    @Override
    public boolean releaseUsing(ItemStack oquê, Level mundo, LivingEntity quem, int faltam) {
        if (!(mundo instanceof ServerLevel level) || !(quem instanceof ServerPlayer gente)) return false;
        if (gente.getCooldowns().isOnCooldown(oquê)) return false;
        Infusions.de(gente).largou(level, gente, oquê, faltam);
        return false;
    }

    /**
     * <b>E socar um bicho com ela.</b>
     *
     * <p>O original devolve verdadeiro no {@code onLeftClickEntity}, que no jogo dele quer dizer «engoli o
     * golpe»: a Mão nunca magoa ninguém, ela só chama a infusão. Aqui é um gancho de fora que faz o mesmo e
     * <b>engole o golpe</b> do mesmo jeito.
     */
    public static InteractionResult soco(Player quem, Level mundo, InteractionHand mão, Entity noquê,
                                         @org.jetbrains.annotations.Nullable
                                         net.minecraft.world.phys.EntityHitResult onde) {
        ItemStack oquê = quem.getItemInHand(mão);
        if (!oquê.is(OccultaItems.WITCH_HAND)) return InteractionResult.PASS;
        if (mundo instanceof ServerLevel level && quem instanceof ServerPlayer gente) {
            Infusions.de(gente).soca(level, gente, oquê, noquê);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * <b>E a mão cai de uma bruxa morta.</b>
     *
     * <p>Uma vez em três, ou uma em duas se quem a matou tinha a Arthana na mão — que é a mesma faca que
     * abre tudo o resto que os bichos guardam.
     */
    public static void deUmaBruxaMorta(ServerLevel level, LivingEntity morta,
                                       net.minecraft.world.damagesource.DamageSource fonte) {
        if (!(morta instanceof net.minecraft.world.entity.monster.Witch)
                && !(morta instanceof net.thaumcraft.occulta.coven.CovenWitchEntity)) {
            return;
        }
        if (!(fonte.getEntity() instanceof Player quem)) return;
        boolean comFaca = quem.getMainHandItem().is(OccultaItems.ARTHANA);
        if (level.getRandom().nextDouble() >= (comFaca ? CAI_COM_FACA : CAI)) return;
        level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level,
                morta.getX(), morta.getY(), morta.getZ(), new ItemStack(OccultaItems.WITCH_HAND)));
    }
}
