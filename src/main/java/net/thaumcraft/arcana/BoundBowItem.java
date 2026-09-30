package net.thaumcraft.arcana;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * O <b>arco vinculado</b>: o {@code ItemBoundBow} do Ars Magica 2.
 *
 * <p>É o sexto tipo de ferramenta vinculada, e o único que não cava nem bate: ele <b>atira</b>. Como os
 * outros, custa mana a cada batida para se manter e volta a ser o feitiço quando a mana acaba.
 *
 * <p>Ele é de <b>ferro</b> — quatro décimos por batida, o mesmo que a pá —, e não de diamante, o que é uma
 * escolha do original que faz sentido: um arco que nunca se gasta já vale muito por si.
 *
 * <p>Ele é uma classe à parte das outras cinco porque um arco do jogo não é uma ferramenta: ele tem de
 * herdar o {@code BowItem} para saber puxar a corda e soltar a flecha. O que ele repete das outras é só o
 * relógio da mana.
 */
public class BoundBowItem extends BowItem {
    /** O que ele come de mana por batida: os quatro décimos do original. */
    public static final float MAINTAIN = BoundToolItem.Kind.BOW.maintain;

    public BoundBowItem(Properties properties) {
        super(properties);
    }

    /** A cada batida: cobra a mana, conserta um ponto, e desfaz-se se não houver com que pagar. */
    @Override
    public void inventoryTick(ItemStack coisa, ServerLevel level, Entity quem, EquipmentSlot casa) {
        if (!(quem instanceof Player gente)) return;
        if (gente.hasInfiniteMaterials()) return;

        Mana conta = Mana.of(gente);
        if (conta.mana() < MAINTAIN) {
            BoundToolItem.unbind(coisa, gente);
            return;
        }

        Mana.set(gente, conta.withMana(conta.mana() - MAINTAIN));
        if (coisa.isDamaged()) coisa.setDamageValue(coisa.getDamageValue() - 1);
    }

    @Override
    public void appendHoverText(ItemStack coisa, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, TooltipFlag bandeira) {
        super.appendHoverText(coisa, contexto, mostra, linha, bandeira);
        linha.accept(Component.translatable("tc.spell.bound.maintain",
                        String.format(Locale.ROOT, "%.1f", MAINTAIN))
                .withStyle(ChatFormatting.BLUE));

        Spell feitiço = coisa.getOrDefault(ArcanaComponents.SPELL, Spell.EMPTY);
        if (!feitiço.isEmpty()) {
            linha.accept(Component.translatable("tc.spell.bound.holds")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
