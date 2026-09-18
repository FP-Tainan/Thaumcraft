package net.thaumcraft.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.RunicArmor;
import net.thaumcraft.api.baubles.BaubleItem;
import net.thaumcraft.api.baubles.BaubleType;

/**
 * As peças do escudo rúnico: o {@code ItemAmuletRunic}, o {@code ItemRingRunic} e o {@code ItemGirdleRunic} da
 * 4.2.3.5. Cada uma soma as suas cargas ao escudo; as variantes trazem o seu efeito de quando o escudo quebra.
 */
public class RunicBaubleItem extends Item implements BaubleItem, RunicArmor {
    /** O que a peça faz além das cargas: o metadado dela no original. */
    public enum Kind {
        /** Amuleto 0 (8), anel de proteção 0 (1), anel 1 (5), cinto 0 (10). */
        PLAIN,
        /** Amuleto 1 (7): recarrega de uma vez quando o escudo quebra. */
        EMERGENCY,
        /** Anel 2 (4): recarrega mais depressa. */
        CHARGED,
        /** Anel 3 (4): regeneração quando o escudo quebra. */
        HEALING,
        /** Cinto 1 (9): uma explosão sem dano de bloco quando o escudo quebra. */
        KINETIC
    }

    private final BaubleType type;
    private final Kind kind;
    private final int charge;

    public RunicBaubleItem(BaubleType type, Kind kind, int charge, Properties properties) {
        super(properties);
        this.type = type;
        this.kind = kind;
        this.charge = charge;
    }

    public Kind kind() {
        return this.kind;
    }

    @Override
    public BaubleType baubleType(ItemStack stack) {
        return this.type;
    }

    @Override
    public int runicCharge(ItemStack stack) {
        return this.charge;
    }
}
