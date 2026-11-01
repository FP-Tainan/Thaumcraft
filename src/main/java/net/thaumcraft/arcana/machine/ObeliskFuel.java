package net.thaumcraft.arcana.machine;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.arcana.ArcanaItems;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * O que o Obelisco queima: o {@code ObeliskFuelHelper} da 1.4.0.009.
 *
 * <p>Ele <b>não queima lenha</b>. O que ele queima é magia crua, e a lista é curta — no original inteiro
 * há <b>duas</b> entradas: o <b>Pó de Vinteum</b>, que vale <b>200</b> batidas, e o <b>Balde de Essência
 * Líquida</b>, que vale <b>2000</b>.
 *
 * <p>A conta importa: duzentas batidas de pó, a cinco por batida, dão <b>mil</b> de Etherium — um quinto
 * do que o Obelisco aguenta. Encher um obelisco pelado leva cinco punhados de pó.
 *
 * <p>O balde ainda não entrou neste porte, porque a Essência Líquida vem com o Refinador. A lista
 * aceita-o assim que ele existir.
 */
public final class ObeliskFuel {
    /** Quanto queima um Pó de Vinteum. */
    public static final int VINTEUM_DUST = 200;
    /** E quanto queima um balde de Essência Líquida. */
    public static final int ESSENCE_BUCKET = 2000;

    private static final Map<Item, Integer> COMBUSTÍVEIS = new LinkedHashMap<>();

    private ObeliskFuel() {
    }

    /** Põe uma coisa na lista. */
    public static void register(Item qual, int batidas) {
        COMBUSTÍVEIS.put(qual, batidas);
    }

    /** Quantas batidas aquilo arde, ou zero se não arde. */
    public static int burnTime(ItemStack pilha) {
        if (pilha.isEmpty()) return 0;
        return COMBUSTÍVEIS.getOrDefault(pilha.getItem(), 0);
    }

    /** A lista inteira, para quem a queira mostrar. */
    public static Map<Item, Integer> all() {
        return Map.copyOf(COMBUSTÍVEIS);
    }

    public static void init() {
        register(ArcanaItems.MATERIALS.get("vinteum_dust"), VINTEUM_DUST);
    }
}
