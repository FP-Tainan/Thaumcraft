package net.thaumcraft.occulta;

import net.thaumcraft.api.ThaumcraftApi;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;

/**
 * De que são feitas as plantas do ofício, para o thaumômetro e para o crisol.
 *
 * <p><b>Isto é do porte.</b> O Witchery não era addon de Thaumcraft e não anotava aspecto em nada; mas nada do
 * ramo sai de receita, e por isso nada seria deduzido — as plantas ficariam sem leitura. As anotações seguem o
 * tom do {@code ConfigAspects} do original: a semente é planta, a colheita é seara, e cada uma leva o que a
 * lore do Witchery lhe dá — veneno na beladona, frio na campainha-de-neve, mente na mandrágora-de-mina.
 */
public final class OccultaAspects {
    private OccultaAspects() {
    }

    public static void init() {
        ThaumcraftApi.aspects(r -> {
            // o que se planta
            r.item("thaumcraft:belladonna_seeds", new AspectList().add(Aspects.PLANT, 1).add(Aspects.POISON, 1));
            r.item("thaumcraft:mandrake_seeds", new AspectList().add(Aspects.PLANT, 1).add(Aspects.MAGIC, 1));
            r.item("thaumcraft:water_artichoke_seeds", new AspectList().add(Aspects.PLANT, 1).add(Aspects.WATER, 1));
            r.item("thaumcraft:snowbell_seeds", new AspectList().add(Aspects.PLANT, 1).add(Aspects.COLD, 1));
            r.item("thaumcraft:wormwood_seeds", new AspectList().add(Aspects.PLANT, 1).add(Aspects.SOUL, 1));
            r.item("thaumcraft:wolfsbane_seeds", new AspectList().add(Aspects.PLANT, 1).add(Aspects.BEAST, 1));

            // o bulbo e o alho são semente e colheita ao mesmo tempo, e levam os dois lados
            r.item("thaumcraft:mindrake_bulb", new AspectList().add(Aspects.PLANT, 1).add(Aspects.CROP, 1)
                    .add(Aspects.MIND, 2).add(Aspects.MINE, 1));
            r.item("thaumcraft:garlic", new AspectList().add(Aspects.PLANT, 1).add(Aspects.CROP, 2)
                    .add(Aspects.LIFE, 1).add(Aspects.UNDEAD, 1));

            // o que se colhe
            r.item("thaumcraft:belladonna_flower", new AspectList().add(Aspects.CROP, 1).add(Aspects.POISON, 2)
                    .add(Aspects.MAGIC, 1));
            r.item("thaumcraft:mandrake_root", new AspectList().add(Aspects.CROP, 1).add(Aspects.MAGIC, 2)
                    .add(Aspects.SOUL, 2));
            r.item("thaumcraft:water_artichoke_globe", new AspectList().add(Aspects.CROP, 2).add(Aspects.WATER, 2)
                    .add(Aspects.HUNGER, 1));
            r.item("thaumcraft:wormwood_sprig", new AspectList().add(Aspects.CROP, 1).add(Aspects.SOUL, 2)
                    .add(Aspects.MAGIC, 1));
            r.item("thaumcraft:wolfsbane_sprig", new AspectList().add(Aspects.CROP, 1).add(Aspects.POISON, 2)
                    .add(Aspects.BEAST, 2));
            r.item("thaumcraft:icy_needle", new AspectList().add(Aspects.COLD, 3).add(Aspects.CRYSTAL, 1)
                    .add(Aspects.WEAPON, 1));
        });
    }
}
