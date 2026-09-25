package net.thaumcraft.forbidden;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.wands.WandParts;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

/**
 * As peças de varinha do Forbidden Magic 0.575: as hastes maculada, infernal e profana, e a ponta alquímica.
 *
 * <p>A <b>maculada</b> se enche sozinha em terra maculada; a <b>infernal</b>, no Nether, e ainda apaga o fogo de
 * quem a leva e cura o definhamento; a <b>profana</b> é um pacto — ela repõe o vis de graça até gastar as vinte e
 * cinco mil que prometeu, distorcendo quem a usa pelo caminho, e no fim vira um pau seco.
 */
public final class ForbiddenWands {
    /** O que o pacto da haste profana tem para dar, em centésimos de vis. */
    public static final int CONTRACT = 25000;

    private ForbiddenWands() {
    }

    public static void init() {
        TCItems.WAND_RODS.put("tainted", ForbiddenItems.WAND_ROD_TAINTED);
        TCItems.WAND_RODS.put("infernal", ForbiddenItems.WAND_ROD_INFERNAL);
        TCItems.WAND_RODS.put("profane", ForbiddenItems.WAND_ROD_PROFANE);
        TCItems.WAND_RODS.put("profaned", ForbiddenItems.WAND_ROD_PROFANED);
        TCItems.WAND_CAPS.put("alchemical", ForbiddenItems.WAND_CAP_ALCHEMICAL);

        WandParts.registerRod("tainted", 150, 12, false, false, false);
        WandParts.registerRod("infernal", 150, 12, false, false, false);
        WandParts.registerRod("profane", 50, 12, false, false, false);
        WandParts.registerRod("profaned", 50, 1000, false, false, false);
        WandParts.onRodTick("tainted", ForbiddenWands::tainted);
        WandParts.onRodTick("infernal", ForbiddenWands::infernal);
        WandParts.onRodTick("profane", ForbiddenWands::profane);

        // a ponta alquímica: dez por cento mais barata, e vinte por cento em água
        WandParts.registerCap("alchemical", 0.9f, 7);
    }

    /** O {@code TaintedWandUpdate}: em terra maculada, um de cada primário a cada cem tiques, até um décimo. */
    private static void tainted(ItemStack wand, Player player) {
        if (player.level().isClientSide() || player.tickCount % 100 != 0) return;
        if (!player.level().getBiome(player.blockPosition()).is(net.thaumcraft.world.TCBiomes.TAINTED_LAND)) return;
        int limite = WandItem.maxVis(wand) / 10;
        for (Aspect primal : Aspects.primals()) {
            if (WandItem.vis(wand, primal) < limite) WandItem.addVis(wand, primal, 1);
        }
    }

    /**
     * O {@code InfernalWandUpdate}: no Nether ela repõe os cinco primários que não são fogo até um décimo, e o
     * fogo até um quinto em qualquer lugar. E quem a leva não queima nem definha.
     */
    private static void infernal(ItemStack wand, Player player) {
        if (player.level().isClientSide()) return;
        if (player.tickCount % 100 == 0) {
            int limite = WandItem.maxVis(wand) / 10;
            if (player.level().dimension() == net.minecraft.world.level.Level.NETHER) {
                for (Aspect primal : Aspects.primals()) {
                    if (primal == Aspects.FIRE) continue;
                    if (WandItem.vis(wand, primal) < limite) WandItem.addVis(wand, primal, 1);
                }
            }
            if (WandItem.vis(wand, Aspects.FIRE) < WandItem.maxVis(wand) / 5) {
                WandItem.addVis(wand, Aspects.FIRE, 1);
            }
        }
        if (player.isOnFire()) player.clearFire();
        player.removeEffect(net.minecraft.world.effect.MobEffects.WITHER);
    }

    /**
     * O {@code ProfaneWandUpdate}: a cada vinte tiques ela repõe o que faltar, tirando do pacto. Cada ponto
     * reposto é uma chance em duas mil e quinhentas de grudar distorção; gasto o pacto, a haste vira profanada e
     * cobra mais um ponto de distorção.
     */
    private static void profane(ItemStack wand, Player player) {
        if (player.level().isClientSide() || player.tickCount % 20 != 0) return;
        int pacto = wand.getOrDefault(TCComponents.WAND_CONTRACT, CONTRACT);
        if (pacto <= 0) return;
        int maximo = WandItem.maxVis(wand);
        var guardado = WandItem.vis(wand);
        for (Aspect primal : Aspects.primals()) {
            if (pacto <= 0) break;
            int falta = maximo - guardado.getAmount(primal);
            if (falta <= 0) continue;
            // o addRealVis do original: o pacto é contado em centésimos, como o vis guardado
            int repoe = Math.min(pacto, falta);
            guardado.add(primal, repoe);
            pacto -= repoe;
            if (player.level().getRandom().nextInt(2501) < repoe) {
                net.thaumcraft.research.Warp.addSticky(player, 1);
            }
        }
        WandItem.setVis(wand, guardado);
        wand.set(TCComponents.WAND_CONTRACT, pacto);
        if (pacto <= 0) {
            wand.set(TCComponents.WAND_ROD, "profaned");
            net.thaumcraft.research.Warp.addSticky(player, 1);
        }
    }

    /** O que a haste profana ainda tem para dar. */
    public static int contract(ItemStack wand) {
        return wand.getOrDefault(TCComponents.WAND_CONTRACT, CONTRACT);
    }

    /** O vis que o pacto ainda promete, para a dica da varinha. */
    public static AspectList promised(ItemStack wand) {
        AspectList list = new AspectList();
        list.add(Aspects.ORDER, contract(wand));
        return list;
    }
}
