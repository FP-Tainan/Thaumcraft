package net.thaumcraft.arcana;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Uma <b>peça de feitiço</b> na mão: o {@code ItemSpellPart} do Ars Magica 2.
 *
 * <p>É uma palavra solta. Ela não faz nada sozinha — não se lança, não se come, não se põe no chão. O que ela
 * serve é para ser <b>escrita numa frase</b>, na Mesa de Inscrição, e é lá que ela vira feitiço.
 *
 * <p>Cada uma sabe que classe de palavra é, e diz isso a quem a olha: <b>Forma</b>, <b>Essência</b> ou
 * <b>Modificador</b>. É a única maneira de quem está aprendendo perceber por que uma Zona não pode ficar no
 * fim de uma frase.
 */
public class SpellPartItem extends Item {
    private final SpellPart part;

    public SpellPartItem(Properties properties, SpellPart qual) {
        super(properties);
        this.part = qual;
    }

    /** A peça que este item é. */
    public SpellPart part() {
        return this.part;
    }

    /** E o caminho de volta: a peça de um item, ou nada se ele não for uma peça. */
    public static SpellPart of(ItemStack coisa) {
        return coisa.getItem() instanceof SpellPartItem qual ? qual.part() : null;
    }

    /** Que classe de palavra ela é, para a dica e para a aba. */
    public String kindKey() {
        if (this.part instanceof SpellPart.Shape) return "tc.spell.kind.shape";
        if (this.part instanceof SpellPart.Essence) return "tc.spell.kind.essence";
        return "tc.spell.kind.modifier";
    }

    @Override
    public void appendHoverText(ItemStack coisa, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, TooltipFlag bandeira) {
        super.appendHoverText(coisa, contexto, mostra, linha, bandeira);
        linha.accept(Component.translatable(this.kindKey()).withStyle(ChatFormatting.DARK_AQUA));

        if (this.part instanceof SpellPart.Shape forma) {
            if (forma.principum()) {
                linha.accept(Component.translatable("tc.spell.kind.principum")
                        .withStyle(ChatFormatting.GOLD));
            }
            if (forma.terminus()) {
                linha.accept(Component.translatable("tc.spell.kind.terminus")
                        .withStyle(ChatFormatting.GOLD));
            }
            if (forma.channeled()) {
                linha.accept(Component.translatable("tc.spell.kind.channeled")
                        .withStyle(ChatFormatting.GOLD));
            }
        }

        if (this.part instanceof SpellPart.Essence essência) {
            var puxa = essência.affinities().stream()
                    .filter(a -> a != Affinity.NONE).findFirst().orElse(Affinity.NONE);
            if (puxa != Affinity.NONE) {
                linha.accept(Component.translatable("tc.spell.affinity",
                                Component.translatable(puxa.key()))
                        .withStyle(estilo -> estilo.withColor(puxa.color)));
            }
        }
    }
}
