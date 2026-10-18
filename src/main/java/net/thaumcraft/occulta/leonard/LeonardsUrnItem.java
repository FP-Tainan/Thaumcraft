package net.thaumcraft.occulta.leonard;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.brew.Brew;
import net.thaumcraft.occulta.brew.BrewItem;

/**
 * A <b>Urna do Leonard</b>: o {@code ItemLeonardsUrn} do Witchery.
 *
 * <p>Ela não chama ninguém e não faz nada sozinha. É uma <b>bolsa de cozimentos atirados</b> — e só eles
 * cabem lá dentro —, e quem bebe dela são os <b>quatro símbolos</b> do Leonard: cada um atira o frasco que
 * estiver na casa dele, sem ele sair do inventário.
 *
 * <p>São <b>quatro tamanhos</b>, de uma casa a quatro, e as casas formam um <b>losango</b> na tela. Os
 * nomes dos quatro símbolos dizem qual é qual, e são quatro pontos do céu: <b>Caelum</b> é o de cima,
 * <b>Baratrum</b> o de baixo, <b>Ortus</b> o do nascente e <b>Occasus</b> o do poente.
 *
 * <p>A de uma casa é a que o <b>Leonard</b> larga. As outras fazem-se <b>juntando duas</b> na bancada: duas
 * de uma dão a de duas, a de duas com uma de uma dá a de três, e assim por diante. Não se compra tamanho —
 * mata-se outra vez.
 */
public class LeonardsUrnItem extends Item {
    /** Quantos tamanhos há, e quantas casas cada um tem. */
    public static final int TAMANHOS = 4;

    public LeonardsUrnItem(Properties properties) {
        super(properties);
    }

    /** O feitio dela: uma só, épica, e brilhando como o original. */
    public static Properties feitio(Properties properties) {
        return properties.stacksTo(1).rarity(Rarity.EPIC)
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                .component(OccultaComponents.URN_SIZE, 0);
    }

    /** Quantas casas esta urna tem: de uma a quatro. */
    public static int casas(ItemStack urna) {
        return Math.clamp(urna.getOrDefault(OccultaComponents.URN_SIZE, 0), 0, TAMANHOS - 1) + 1;
    }

    /** Uma urna daquele tamanho, contado de zero. */
    public static ItemStack daquele(int tamanho) {
        ItemStack feita = new ItemStack(net.thaumcraft.occulta.OccultaItems.LEONARDS_URN);
        feita.set(OccultaComponents.URN_SIZE, Math.clamp(tamanho, 0, TAMANHOS - 1));
        return feita;
    }

    /** O que está dentro dela, uma pilha por casa. */
    public static NonNullList<ItemStack> dentro(ItemStack urna) {
        NonNullList<ItemStack> lista = NonNullList.withSize(casas(urna), ItemStack.EMPTY);
        urna.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(lista);
        return lista;
    }

    /** E põe de volta. */
    public static void guarda(ItemStack urna, NonNullList<ItemStack> lista) {
        urna.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(lista));
    }

    /** O frasco que está naquela casa, ou nada — inclusive se a urna for pequena demais para a ter. */
    public static ItemStack naCasa(ItemStack urna, int qual) {
        if (qual < 0 || qual >= casas(urna)) return ItemStack.EMPTY;
        return dentro(urna).get(qual);
    }

    /** <b>Só cozimento atirável entra</b>: o {@code isBrew} do original, que pergunta pelo arremesso. */
    public static boolean cabe(ItemStack oquê) {
        return oquê.getItem() instanceof BrewItem && Brew.splash(BrewItem.contents(oquê));
    }

    /** A dica dela, que é a do original: ponha um cozimento dentro e solte-o pela vara. */
    @Override
    public void appendHoverText(ItemStack urna, TooltipContext contexto,
                                net.minecraft.world.item.component.TooltipDisplay mostra,
                                java.util.function.Consumer<Component> linha,
                                net.minecraft.world.item.TooltipFlag bandeira) {
        linha.accept(Component.translatable("tc.leonardsurn.tip")
                .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        linha.accept(Component.translatable("tc.leonardsurn.tip2")
                .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
    }

    /** Clicar abre-a. */
    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        if (mão != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (quem instanceof net.minecraft.server.level.ServerPlayer gente) {
            UrnMenu.abre(gente, gente.getMainHandItem());
        }
        return InteractionResult.SUCCESS;
    }
}
