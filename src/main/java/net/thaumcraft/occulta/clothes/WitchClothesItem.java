package net.thaumcraft.occulta.clothes;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Uma peça de <b>roupa de bruxa</b>: o {@code ItemWitchesClothes} do Witchery.
 *
 * <p>Quatro peças: o <b>Chapéu de Bruxa</b>, o <b>Chapéu da Baba Yaga</b>, o <b>Manto de Bruxa</b> e o
 * <b>Manto de Necromante</b>. As quatro protegem como couro e <b>duram como couro</b> — a proteção é o que
 * elas têm de menos interessante.
 *
 * <p>O que elas fazem está no {@link WitchClothes}, porque não acontece nelas: acontece na Chaleira e no
 * Caldeirão.
 *
 * <h2>A cor de fábrica</h2>
 *
 * <p>Sem tinta, o couro do jogo é um castanho-claro. A roupa de bruxa o troca por um <b>castanho quase preto</b>, e
 * é o original que dá o número. Como nas roupas de caçador, a cor <b>não é um componente</b>: posta como
 * componente, toda peça dizia «Tingida» sem ninguém lhe ter tocado.
 *
 * <p>O <b>Chapéu da Baba</b> é o único que <b>não se tinge</b> — ele é o que é.
 */
public class WitchClothesItem extends Item {
    /**
     * O castanho quase preto das roupas de bruxa: o {@code 2628115} do original, que é
     * {@code 40, 26, 19}.
     */
    public static final int DEFAULT_COLOR = 2628115;

    private final boolean dyeable;
    private final boolean necro;
    private final String tip;

    public WitchClothesItem(Properties propriedades, boolean dyeable, boolean necro, String tip) {
        super(propriedades);
        this.dyeable = dyeable;
        this.necro = necro;
        this.tip = tip;
    }

    /** Se esta peça aceita tinta: todas menos o Chapéu da Baba. */
    public boolean dyeable() {
        return this.dyeable;
    }

    /** Se ela é a do <b>Necromante</b>, que leva ombreiras e espanta os mortos-vivos. */
    public boolean necro() {
        return this.necro;
    }

    /** A cor desta peça quando ninguém a pintou. */
    public int corDeFábrica() {
        return DEFAULT_COLOR;
    }

    @Override
    public void appendHoverText(ItemStack peça, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, TooltipFlag bandeira) {
        for (String parte : this.tip.split(";")) {
            linha.accept(Component.translatable(parte).withStyle(ChatFormatting.BLUE));
        }
    }
}
