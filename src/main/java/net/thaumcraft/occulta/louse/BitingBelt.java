package net.thaumcraft.occulta.louse;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.thaumcraft.occulta.OccultaComponents;

import java.util.ArrayList;
import java.util.List;

/**
 * O que o <b>Cinto Mordedor</b> guarda: o {@code WITCPotion} e o {@code WITCPotion2} do Witchery.
 *
 * <p>Ele carrega <b>duas</b> poções, e gasta uma por pancada. Enchem-se na bancada, uma de cada vez — o
 * {@code RecipeShapelessAddPotion} do original —, e a segunda só entra depois da primeira.
 *
 * <p>É o único equipamento do mod que se <b>recarrega</b> em vez de se gastar, e é por isso que ele vale a
 * pena: um cinto com duas poções de veneno é duas mordidas que quem bater leva de graça.
 */
public final class BitingBelt {
    /** Quantas ele leva: duas. */
    public static final int CABEM = 2;

    private BitingBelt() {
    }

    /** As poções que este cinto tem dentro, pela ordem em que saem. */
    public static List<PotionContents> poções(ItemStack cinto) {
        List<PotionContents> feito = new ArrayList<>(CABEM);
        for (ItemStack cada : guardadas(cinto)) {
            PotionContents tem = cada.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            if (!tem.equals(PotionContents.EMPTY)) feito.add(tem);
        }
        return feito;
    }

    /** Se ainda cabe mais uma. */
    public static boolean cabeMais(ItemStack cinto) {
        return poções(cinto).size() < CABEM;
    }

    /**
     * Põe-lhe mais uma dentro.
     *
     * @return o cinto cheio, ou o mesmo se já não coubesse
     */
    public static ItemStack enche(ItemStack cinto, PotionContents qual) {
        if (!cabeMais(cinto) || qual.equals(PotionContents.EMPTY)) return cinto;
        List<ItemStack> tinha = new ArrayList<>(guardadas(cinto));
        ItemStack frasco = new ItemStack(net.minecraft.world.item.Items.POTION);
        frasco.set(DataComponents.POTION_CONTENTS, qual);
        tinha.add(frasco);

        ItemStack feito = cinto.copyWithCount(1);
        feito.set(OccultaComponents.BELT_POTIONS, List.copyOf(tinha));
        return feito;
    }

    /** Gasta a primeira. */
    public static void gasta(ItemStack cinto) {
        List<ItemStack> tinha = new ArrayList<>(guardadas(cinto));
        if (tinha.isEmpty()) return;
        tinha.removeFirst();
        if (tinha.isEmpty()) {
            cinto.remove(OccultaComponents.BELT_POTIONS);
            return;
        }
        cinto.set(OccultaComponents.BELT_POTIONS, List.copyOf(tinha));
    }

    private static List<ItemStack> guardadas(ItemStack cinto) {
        return cinto.getOrDefault(OccultaComponents.BELT_POTIONS, List.of());
    }
}
