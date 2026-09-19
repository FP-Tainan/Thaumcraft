package net.thaumcraft.crafting;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * O bônus de fundição do Thaumcraft 4.2.3.5 (o {@code ThaumcraftApi.addSmeltingBonus}): o que a fornalha infernal
 * solta a mais quando funde cada coisa. No original o bônus vem com zero itens e a fornalha é que sorteia quantos.
 *
 * <p>GERADO por {@code scratchpad/bonus-fundicao.js} a partir do {@code ConfigRecipes} do jar — não editar à mão.
 * O minério que o original fundia hoje cai como minério bruto, e o bruto entra junto com o bloco.
 */
public final class SmeltingBonus {
    private static final Map<Item, Item> BONUS = new HashMap<>();

    private SmeltingBonus() {
    }

    private static void add(ItemLike in, ItemLike out) {
        BONUS.put(in.asItem(), out.asItem());
    }

    /** O item do bônus desta coisa, ou nulo. */
    @Nullable
    public static Item of(ItemStack in) {
        init();
        return in.isEmpty() ? null : BONUS.get(in.getItem());
    }

    public static int size() {
        init();
        return BONUS.size();
    }

    public static void init() {
        if (!BONUS.isEmpty()) return;
        add(net.minecraft.world.item.Items.GOLD_ORE, net.minecraft.world.item.Items.GOLD_NUGGET);
        add(net.minecraft.world.item.Items.DEEPSLATE_GOLD_ORE, net.minecraft.world.item.Items.GOLD_NUGGET);
        add(net.minecraft.world.item.Items.RAW_GOLD, net.minecraft.world.item.Items.GOLD_NUGGET);
        add(net.minecraft.world.item.Items.IRON_ORE, net.minecraft.world.item.Items.IRON_NUGGET);
        add(net.minecraft.world.item.Items.DEEPSLATE_IRON_ORE, net.minecraft.world.item.Items.IRON_NUGGET);
        add(net.minecraft.world.item.Items.RAW_IRON, net.minecraft.world.item.Items.IRON_NUGGET);
        add(TCBlocks.CINNABAR_ORE.asItem(), TCResources.get("quicksilver_drop"));
        add(net.minecraft.world.item.Items.COPPER_ORE, net.minecraft.world.item.Items.COPPER_NUGGET);
        add(net.minecraft.world.item.Items.DEEPSLATE_COPPER_ORE, net.minecraft.world.item.Items.COPPER_NUGGET);
        add(net.minecraft.world.item.Items.RAW_COPPER, net.minecraft.world.item.Items.COPPER_NUGGET);
        add(TCResources.get("native_gold_cluster"), net.minecraft.world.item.Items.GOLD_NUGGET);
        add(TCResources.get("native_iron_cluster"), net.minecraft.world.item.Items.IRON_NUGGET);
        add(TCResources.get("native_cinnabar_cluster"), TCResources.get("quicksilver_drop"));
        add(TCResources.get("native_copper_cluster"), net.minecraft.world.item.Items.COPPER_NUGGET);
        add(net.minecraft.world.item.Items.CHICKEN, TCItems.NUGGET_CHICKEN);
        add(net.minecraft.world.item.Items.BEEF, TCItems.NUGGET_BEEF);
        add(net.minecraft.world.item.Items.PORKCHOP, TCItems.NUGGET_PORK);
        add(net.minecraft.world.item.Items.COD, TCItems.NUGGET_FISH);
        add(net.minecraft.world.item.Items.SALMON, TCItems.NUGGET_FISH);
    }
}
