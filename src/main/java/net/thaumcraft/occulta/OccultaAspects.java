package net.thaumcraft.occulta;

import net.thaumcraft.api.ThaumcraftApi;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;

/**
 * De que são feitas as coisas do ofício, para o thaumômetro e para o crisol.
 *
 * <p><b>Os números são do próprio Witchery.</b> O mod trazia um {@code ModHookThaumcraft4} de mil e seiscentas
 * linhas que anotava cada coisa dele nos aspectos do Thaumcraft 4 — era assim que os dois se davam em 2014, e é
 * de lá que sai tudo o que está aqui.
 *
 * <p><b>Duas coisas são do porte, e estão declaradas:</b> a acônito e o alho. O {@code ModHookThaumcraft4} não os
 * anotava (nem semente, nem colheita, nem a planta), e sem anotação nenhuma o thaumômetro não teria o que ler
 * neles. Vão no tom do resto: veneno e fera na acônito, que é o que ela faz aos lobisomens; e vida e morto-vivo no
 * alho, que é o que ele faz aos vampiros.
 */
public final class OccultaAspects {
    private OccultaAspects() {
    }

    public static void init() {
        ThaumcraftApi.aspects(r -> {
            // ---------------------------------------------------------- o que se planta
            r.item("thaumcraft:belladonna_seeds", new AspectList().add(Aspects.PLANT, 1).add(Aspects.POISON, 1));
            r.item("thaumcraft:mandrake_seeds", new AspectList().add(Aspects.PLANT, 1).add(Aspects.EARTH, 1));
            r.item("thaumcraft:water_artichoke_seeds", new AspectList().add(Aspects.PLANT, 1).add(Aspects.WATER, 1));
            r.item("thaumcraft:snowbell_seeds", new AspectList().add(Aspects.PLANT, 1).add(Aspects.COLD, 1));
            r.item("thaumcraft:wormwood_seeds", new AspectList().add(Aspects.PLANT, 1));
            // o bulbo da mindrake é o que o original anota na semente dela
            r.item("thaumcraft:mindrake_bulb", new AspectList().add(Aspects.WATER, 1).add(Aspects.EXCHANGE, 1));

            // ---------------------------------------------------------- o que se colhe
            r.item("thaumcraft:belladonna_flower", new AspectList().add(Aspects.PLANT, 2).add(Aspects.POISON, 4)
                    .add(Aspects.DEATH, 4));
            r.item("thaumcraft:mandrake_root", new AspectList().add(Aspects.PLANT, 2).add(Aspects.MAN, 1)
                    .add(Aspects.EARTH, 1));
            r.item("thaumcraft:water_artichoke_globe", new AspectList().add(Aspects.PLANT, 2).add(Aspects.WATER, 2));
            r.item("thaumcraft:wormwood_sprig", new AspectList().add(Aspects.UNDEAD, 2).add(Aspects.PLANT, 1));
            r.item("thaumcraft:icy_needle", new AspectList().add(Aspects.PLANT, 1).add(Aspects.COLD, 4));

            // ---------------------------------------------------------- as duas que o original não anotava
            r.item("thaumcraft:wolfsbane_seeds", new AspectList().add(Aspects.PLANT, 1).add(Aspects.BEAST, 1));
            r.item("thaumcraft:wolfsbane_sprig", new AspectList().add(Aspects.PLANT, 2).add(Aspects.POISON, 2)
                    .add(Aspects.BEAST, 2));
            r.item("thaumcraft:garlic", new AspectList().add(Aspects.PLANT, 1).add(Aspects.CROP, 2)
                    .add(Aspects.LIFE, 1).add(Aspects.UNDEAD, 1));

            // ---------------------------------------------------------- e as plantas no chão, que o thaumômetro lê
            // direto no bloco, porque nenhuma delas vira item
            r.blockAdd("thaumcraft:belladonna", new AspectList().add(Aspects.PLANT, 2).add(Aspects.POISON, 4)
                    .add(Aspects.DEATH, 4).add(Aspects.CROP, 1));
            r.blockAdd("thaumcraft:mandrake", new AspectList().add(Aspects.PLANT, 2).add(Aspects.MAN, 1)
                    .add(Aspects.CROP, 1));
            r.blockAdd("thaumcraft:water_artichoke", new AspectList().add(Aspects.PLANT, 2).add(Aspects.WATER, 2)
                    .add(Aspects.CROP, 1));
            r.blockAdd("thaumcraft:snowbell", new AspectList().add(Aspects.PLANT, 2).add(Aspects.COLD, 2)
                    .add(Aspects.CROP, 1));
            r.blockAdd("thaumcraft:wormwood", new AspectList().add(Aspects.UNDEAD, 2).add(Aspects.PLANT, 1));
            r.blockAdd("thaumcraft:mindrake", new AspectList().add(Aspects.PLANT, 2).add(Aspects.MAN, 1)
                    .add(Aspects.CROP, 1));
            r.blockAdd("thaumcraft:wolfsbane", new AspectList().add(Aspects.PLANT, 2).add(Aspects.POISON, 2)
                    .add(Aspects.BEAST, 2).add(Aspects.CROP, 1));
            r.blockAdd("thaumcraft:garlic", new AspectList().add(Aspects.PLANT, 2).add(Aspects.LIFE, 1)
                    .add(Aspects.CROP, 1));

            // ---------------------------------------------------------- o forno, os funis e os fumos
            r.item("thaumcraft:witches_oven", new AspectList().add(Aspects.METAL, 14).add(Aspects.MECHANISM, 3)
                    .add(Aspects.FIRE, 1).add(Aspects.AIR, 2));
            r.item("thaumcraft:fume_funnel", new AspectList().add(Aspects.VOID, 4).add(Aspects.METAL, 20)
                    .add(Aspects.SENSES, 3).add(Aspects.LIGHT, 5).add(Aspects.ORDER, 4).add(Aspects.FIRE, 4));
            r.item("thaumcraft:filtered_fume_funnel", new AspectList().add(Aspects.METAL, 24)
                    .add(Aspects.CRYSTAL, 10).add(Aspects.GREED, 4).add(Aspects.ENERGY, 6).add(Aspects.MAGIC, 10)
                    .add(Aspects.ORDER, 8).add(Aspects.VOID, 4).add(Aspects.SENSES, 3).add(Aspects.LIGHT, 5)
                    .add(Aspects.FIRE, 4));
            r.item("thaumcraft:fume_filter", new AspectList().add(Aspects.METAL, 4).add(Aspects.CRYSTAL, 10)
                    .add(Aspects.GREED, 4).add(Aspects.ENERGY, 6).add(Aspects.MAGIC, 10).add(Aspects.ORDER, 4));

            r.item("thaumcraft:soft_clay_jar", new AspectList().add(Aspects.EARTH, 1).add(Aspects.WATER, 1)
                    .add(Aspects.VOID, 1));
            r.item("thaumcraft:clay_jar", new AspectList().add(Aspects.EARTH, 1).add(Aspects.FIRE, 1)
                    .add(Aspects.VOID, 1));
            r.item("thaumcraft:wood_ash", new AspectList().add(Aspects.TREE, 1).add(Aspects.FIRE, 1));

            // os sete cheiros: todos são ar, e o que os separa é o resto
            r.item("thaumcraft:foul_fume", new AspectList().add(Aspects.AIR, 3).add(Aspects.EARTH, 1));
            r.item("thaumcraft:exhale_of_the_horned_one", new AspectList().add(Aspects.AIR, 3).add(Aspects.FIRE, 1)
                    .add(Aspects.UNDEAD, 1));
            r.item("thaumcraft:breath_of_the_goddess", new AspectList().add(Aspects.AIR, 3).add(Aspects.ORDER, 1)
                    .add(Aspects.SOUL, 1));
            r.item("thaumcraft:hint_of_rebirth", new AspectList().add(Aspects.AIR, 3).add(Aspects.LIFE, 1)
                    .add(Aspects.EXCHANGE, 1));
            r.item("thaumcraft:whiff_of_magic", new AspectList().add(Aspects.AIR, 3).add(Aspects.MAGIC, 1));
            r.item("thaumcraft:reek_of_misfortune", new AspectList().add(Aspects.AIR, 3).add(Aspects.ENTROPY, 1));
            r.item("thaumcraft:odour_of_purity", new AspectList().add(Aspects.AIR, 3).add(Aspects.ORDER, 1));
        });
    }
}
