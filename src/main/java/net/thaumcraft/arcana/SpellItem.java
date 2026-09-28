package net.thaumcraft.arcana;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Um feitiço na mão: o {@code ItemSpellBase} do Ars Magica 2.
 *
 * <p>Ele não é uma coisa: é uma <b>frase</b>, e o que ele faz depende inteiramente do que está escrito nele. Um
 * item sem feitiço dentro não faz nada, e é isso que ele diz a quem o olha.
 */
public class SpellItem extends Item {
    public SpellItem(Properties properties) {
        super(properties);
    }

    /** O feitiço escrito naquele item, ou nenhum. */
    public static Spell spellOf(ItemStack coisa) {
        return coisa.getOrDefault(ArcanaComponents.SPELL, Spell.EMPTY);
    }

    /** Escreve um feitiço num item. */
    public static ItemStack write(ItemStack coisa, Spell feitiço) {
        coisa.set(ArcanaComponents.SPELL, feitiço);
        return coisa;
    }

    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        ItemStack naMão = quem.getItemInHand(mão);
        Spell feitiço = spellOf(naMão);
        if (feitiço.isEmpty()) return InteractionResult.PASS;
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;

        SpellCast.Result saiu = SpellCast.cast(server, feitiço, quem, null, quem.getEyePosition());
        if (!saiu.ok()) {
            quem.sendSystemMessage(Component.translatable("tc.spell." + saiu.name().toLowerCase(
                    java.util.Locale.ROOT)));
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    /** O que ele diz de si: a frase escrita nele, etapa a etapa. */
    @Override
    public void appendHoverText(ItemStack coisa, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, TooltipFlag bandeira) {
        super.appendHoverText(coisa, contexto, mostra, linha, bandeira);
        Spell feitiço = spellOf(coisa);
        if (feitiço.isEmpty()) {
            linha.accept(Component.translatable("tc.spell.empty")
                    .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            return;
        }

        int n = 1;
        for (Spell.Stage etapa : feitiço.stages()) {
            linha.accept(Component.translatable("tc.spell.stage", n++,
                            Component.translatable("tc.spell.shape." + etapa.shape().name()))
                    .withStyle(net.minecraft.ChatFormatting.AQUA));
            for (SpellPart.Essence essência : etapa.essences()) {
                linha.accept(Component.literal("  ").append(
                                Component.translatable("tc.spell.essence." + essência.name()))
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
            }
            for (SpellPart.Modifier mod : etapa.modifiers()) {
                linha.accept(Component.literal("  + ").append(
                                Component.translatable("tc.spell.modifier." + mod.name()))
                        .withStyle(net.minecraft.ChatFormatting.DARK_AQUA));
            }
        }
        linha.accept(Component.translatable("tc.spell.cost",
                        String.format(java.util.Locale.ROOT, "%.0f", feitiço.manaCost(null, null)))
                .withStyle(net.minecraft.ChatFormatting.BLUE));
    }
}
