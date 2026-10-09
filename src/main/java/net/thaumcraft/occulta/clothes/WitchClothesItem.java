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

    /**
     * E o <b>couro cru do jogo</b>, que é o {@code 10511680} do {@code ItemArmor.getColor}.
     *
     * <p>Ele importa a uma peça só: o <b>Chapéu da Baba</b>, que não se tinge. A conta do original é esta —
     * a peça que <b>se tinge</b> e não foi tingida troca o couro cru pelo castanho quase preto; a que
     * <b>não se tinge</b> fica com o couro cru.
     */
    public static final int LEATHER_COLOR = 10511680;

    private final boolean dyeable;
    private final boolean necro;
    private final String tip;
    private final int corDeFábrica;

    public WitchClothesItem(Properties propriedades, boolean dyeable, boolean necro, String tip) {
        this(propriedades, dyeable, necro, tip, DEFAULT_COLOR);
    }

    /**
     * E com cor própria, que é o que o <b>calçado</b> precisa: o {@code getColor} do original devolve um
     * número por peça — azul-claro para as Chinelas de Gelo, azul-escuro para os Sapatos Escorridos e
     * vermelho para as de Rubi.
     */
    public WitchClothesItem(Properties propriedades, boolean dyeable, boolean necro, String tip,
                            int corDeFábrica) {
        super(propriedades);
        this.dyeable = dyeable;
        this.necro = necro;
        this.tip = tip;
        this.corDeFábrica = corDeFábrica;
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
        return this.corDeFábrica;
    }

    @Override
    public void appendHoverText(ItemStack peça, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, TooltipFlag bandeira) {
        for (String parte : this.tip.split(";")) {
            linha.accept(Component.translatable(parte).withStyle(ChatFormatting.BLUE));
        }
        /*
         * E o <b>Cinto Mordedor</b> diz o que guarda, que é o que o original faz com as duas chaves
         * dele: sem isto, um cinto cheio e um cinto vazio são o mesmo item na mão.
         */
        for (var cada : net.thaumcraft.occulta.louse.BitingBelt.poções(peça)) {
            net.minecraft.world.item.alchemy.PotionContents.addPotionTooltip(
                    cada.getAllEffects(), linha, 1.0f, contexto.tickRate());
        }
    }
}
