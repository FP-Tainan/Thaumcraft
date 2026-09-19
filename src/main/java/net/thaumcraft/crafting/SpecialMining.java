package net.thaumcraft.crafting;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCResources;

import java.util.HashMap;
import java.util.Map;

/**
 * A mineração especial do Thaumcraft 4.2.3.5 (o {@code Utils.addSpecialMiningResult}): o minério que às vezes sai como
 * aglomerado nativo — com a escavação de radiestesia e as ferramentas que refinam.
 *
 * <p>GERADO por {@code scratchpad/mineracao-especial.js} a partir do {@code Config} do jar — não editar à mão. O
 * minério de hoje cai bruto, e o bruto entra junto com o bloco.
 */
public final class SpecialMining {
    private record Result(Item item, float chance) {
    }

    private static final Map<Item, Result> RESULTS = new HashMap<>();

    private SpecialMining() {
    }

    private static void add(ItemLike in, ItemLike out, float chance) {
        RESULTS.put(in.asItem(), new Result(out.asItem(), chance));
    }

    /** O {@code findSpecialMiningResult}: com a sorte, a pilha vira o aglomerado, na mesma quantidade. */
    public static ItemStack refine(ItemStack is, float chance, RandomSource random) {
        init();
        float r = random.nextFloat();
        Result found = RESULTS.get(is.getItem());
        // estanho, prata e chumbo, pelas etiquetas c:
        if (found == null) {
            Item other = OtherMetals.specialMining(is);
            if (other != null) found = new Result(other, 1.0f);
        }
        if (found != null && r <= chance * found.chance()) return new ItemStack(found.item(), is.getCount());
        return is.copy();
    }

    public static int size() {
        init();
        return RESULTS.size();
    }

    public static void init() {
        if (!RESULTS.isEmpty()) return;
        add(net.minecraft.world.item.Items.COPPER_ORE, TCResources.get("native_copper_cluster"), 1.0f);
        add(net.minecraft.world.item.Items.DEEPSLATE_COPPER_ORE, TCResources.get("native_copper_cluster"), 1.0f);
        add(net.minecraft.world.item.Items.RAW_COPPER, TCResources.get("native_copper_cluster"), 1.0f);
        add(net.minecraft.world.item.Items.IRON_ORE, TCResources.get("native_iron_cluster"), 1.0f);
        add(net.minecraft.world.item.Items.DEEPSLATE_IRON_ORE, TCResources.get("native_iron_cluster"), 1.0f);
        add(net.minecraft.world.item.Items.RAW_IRON, TCResources.get("native_iron_cluster"), 1.0f);
        add(net.minecraft.world.item.Items.GOLD_ORE, TCResources.get("native_gold_cluster"), 0.9f);
        add(net.minecraft.world.item.Items.DEEPSLATE_GOLD_ORE, TCResources.get("native_gold_cluster"), 0.9f);
        add(net.minecraft.world.item.Items.RAW_GOLD, TCResources.get("native_gold_cluster"), 0.9f);
        add(TCBlocks.CINNABAR_ORE.asItem(), TCResources.get("native_cinnabar_cluster"), 0.9f);
    }
}
