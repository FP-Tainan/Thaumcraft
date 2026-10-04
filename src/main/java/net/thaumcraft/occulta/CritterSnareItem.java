package net.thaumcraft.occulta;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/**
 * O Apanha-Bicho na mão, com o bicho dentro: o {@code MultiItemBlock} do {@code BlockCritterSnare}.
 *
 * <p>No original isto eram <b>cinco itens</b> — um por bicho, cada um com o seu nome: "Critter Snare",
 * "Critter Snare (Bat)" e os outros três. Aqui é <b>um item só</b>, e o que ele apanhou viaja no feitio do
 * bloco que o jogo de hoje já sabe guardar num item.
 *
 * <p>O <b>nome</b> e a <b>cara</b>, porém, são os do original: este item lê o que leva dentro, se chama em
 * conformidade, e o arquivo dele escolhe entre as <b>mesmas cinco folhas do bloco</b>. Quem tem um
 * Apanha-Bicho na mochila vê, sem o pôr no chão, o que há nele.
 *
 * <p><b>Uma linha a mais, declarada:</b> o jogo de hoje escreve o feitio guardado na dica do item por conta
 * própria, de modo que um Apanha-Bicho com um morcego dentro diz "caught: bat" debaixo do nome. O original
 * não tinha essa linha porque não tinha o mecanismo; fica, porque tirá-la custaria mais do que ela vale.
 */
public class CritterSnareItem extends BlockItem {
    public CritterSnareItem(Properties properties) {
        super(OccultaBlocks.CRITTER_SNARE, properties);
    }

    /** O que este Apanha-Bicho leva dentro, lido do feitio que o item guarda. */
    public static CritterSnareBlock.Caught oQueLeva(ItemStack qual) {
        var feitio = qual.get(DataComponents.BLOCK_STATE);
        if (feitio == null) return CritterSnareBlock.Caught.EMPTY;
        CritterSnareBlock.Caught leva = feitio.get(CritterSnareBlock.APANHADO);
        return leva == null ? CritterSnareBlock.Caught.EMPTY : leva;
    }

    @Override
    public Component getName(ItemStack qual) {
        CritterSnareBlock.Caught leva = oQueLeva(qual);
        if (leva == CritterSnareBlock.Caught.EMPTY) return super.getName(qual);
        return Component.translatable("item.thaumcraft.critter_snare." + leva.getSerializedName());
    }
}
