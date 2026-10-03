package net.thaumcraft.occulta.hunter;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Uma peça das roupas de caçador, que diz na mão o que o conjunto faz: o {@code addInformation} do
 * {@code ItemHunterClothes}.
 *
 * <p>É a dica mais honesta do mod: diz as três coisas que o conjunto <b>dá</b> e a que ele <b>tira</b>, na
 * mesma lista e sem distinção. Quem veste o conjunto sabe desde a primeira peça que está trocando as bonecas
 * por isto.
 */
public class HunterClothesItem extends Item {
    private final boolean prateada;
    private final boolean comAlho;
    private final int corDeFábrica;

    public HunterClothesItem(Properties properties, boolean prateada, boolean comAlho, int corDeFábrica) {
        super(properties);
        this.prateada = prateada;
        this.comAlho = comAlho;
        this.corDeFábrica = corDeFábrica;
    }

    /**
     * A cor desta peça quando ninguém a pintou: o {@code getColor} do original, que devolve uma cor por casa.
     *
     * <p>Ela <b>não é um componente</b> do item, e é de propósito: posta como componente, toda peça dizia
     * "Tingida" na dica sem ninguém lhe ter tocado. O original dá a cor <b>na pergunta</b>, e é o que se faz
     * aqui — o desenhista pergunta, e quem pintar a peça escreve por cima.
     */
    public int corDeFábrica() {
        return this.corDeFábrica;
    }

    @Override
    public void appendHoverText(ItemStack peça, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> linha, TooltipFlag flag) {
        if (this.comAlho) linha.accept(dica("tc.hunter.garlicked"));
        if (this.prateada) linha.accept(dica("tc.hunter.silvered"));
        linha.accept(dica("tc.hunter.set"));
        linha.accept(dica("tc.hunter.set_bolts"));
        linha.accept(dica("tc.hunter.set_poppets"));
    }

    private static Component dica(String chave) {
        return Component.translatable(chave).withStyle(ChatFormatting.BLUE);
    }
}
