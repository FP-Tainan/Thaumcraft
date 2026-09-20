package net.thaumcraft.maleficium;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.wands.WandParts;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.research.Knowledges;

/**
 * As peças de varinha do Maleficium: o {@code ItemWandRod} e o {@code ItemWandCap} do Tainted Magic 8.1.1.
 *
 * <p>A haste de madeira distorcida guarda duzentos e cinquenta de cada vis e se enche sozinha conforme a
 * <b>distorção permanente</b> de quem a carrega — quanto mais torta a cabeça, mais depressa ela repõe. O núcleo de
 * bastão guarda quinhentos e faz o mesmo.
 *
 * <p>As pontas são quatro: a de metal das sombras, que desconta trinta e cinco por cento do vis, e as três de pano
 * — encantado, carmesim e de sombra.
 */
public final class MaleficiumWands {
    /** O {@code WARP_WAND_REFRESH_BASE} do original: dez mil tiques divididos pela distorção. */
    public static final float REFRESH_BASE = 10000.0f;

    private MaleficiumWands() {
    }

    public static void init() {
        // o Thaumcraft faz um item para cada peça que já conhece ao carregar; as nossas entram depois, com os
        // itens que a aba do Maleficium já registrou, para ele não as fazer de novo
        TCItems.WAND_RODS.put("warpwood", MaleficiumItems.WAND_ROD_WARPWOOD);
        TCItems.STAFF_RODS.put("warpwood", MaleficiumItems.STAFF_ROD_WARPWOOD);
        TCItems.WAND_CAPS.put("shadowmetal", MaleficiumItems.WAND_CAP_SHADOWMETAL);
        TCItems.WAND_CAPS.put("cloth", MaleficiumItems.WAND_CAP_CLOTH);
        TCItems.WAND_CAPS.put("crimsoncloth", MaleficiumItems.WAND_CAP_CRIMSONCLOTH);
        TCItems.WAND_CAPS.put("shadowcloth", MaleficiumItems.WAND_CAP_SHADOWCLOTH);

        WandParts.registerRod("warpwood", 250, 16, false, true, false);
        WandParts.registerRod("warpwood", 500, 20, true, true, true);
        WandParts.onRodTick("warpwood", MaleficiumWands::refill);
        WandParts.onRodTick("warpwood_staff", MaleficiumWands::refill);

        WandParts.registerCap("shadowmetal", 0.65f, 12);
        WandParts.registerCap("cloth", 0.97f, 2);
        WandParts.registerCap("crimsoncloth", 0.93f, 3);
        WandParts.registerCap("shadowcloth", 0.93f, 4);
    }

    /**
     * O {@code WandHandler} do original: de tantos em tantos tiques, um ponto de cada primário. O período é a base
     * dividida pela distorção permanente — sem distorção, a haste não repõe nada; e quem está protegido da distorção
     * também não ganha nada.
     */
    private static void refill(ItemStack wand, Player player) {
        if (player.level().isClientSide()) return;
        if (player.hasEffect(net.thaumcraft.registry.TCEffects.WARP_WARD)) return;
        int warp = player.getAttachedOrCreate(Knowledges.KNOWLEDGE).warpPerm();
        if (warp <= 0) return;
        float period = REFRESH_BASE / warp;
        int ticks = period < 1.0f ? 1 : Math.round(period);
        if (player.tickCount % ticks != 0) return;
        for (Aspect primal : Aspects.primals()) WandItem.addVis(wand, primal, 1);
    }
}
