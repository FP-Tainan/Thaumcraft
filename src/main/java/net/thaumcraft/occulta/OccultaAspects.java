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

            // ---------------------------------------------------------- as três árvores
            for (String árvore : java.util.List.of("rowan", "alder", "hawthorn")) {
                r.item("thaumcraft:" + árvore + "_log", new AspectList().add(Aspects.TREE, 2).add(Aspects.MAGIC, 1));
                r.item("thaumcraft:" + árvore + "_leaves", new AspectList().add(Aspects.PLANT, 1));
                r.item("thaumcraft:" + árvore + "_planks", new AspectList().add(Aspects.TREE, 1));
            }
            // e cada muda leva o que a árvore dela é: magia na sorveira, desordem no amieiro, ordem no espinheiro
            r.item("thaumcraft:rowan_sapling", new AspectList().add(Aspects.PLANT, 2).add(Aspects.MAGIC, 1));
            r.item("thaumcraft:alder_sapling", new AspectList().add(Aspects.PLANT, 2).add(Aspects.ENTROPY, 1));
            r.item("thaumcraft:hawthorn_sapling", new AspectList().add(Aspects.PLANT, 2).add(Aspects.ORDER, 1));
            r.item("thaumcraft:rowan_berries", new AspectList().add(Aspects.PLANT, 1).add(Aspects.HUNGER, 1));

            // ---------------------------------------------------------- o caldeirão e o que sai dele
            r.item("thaumcraft:witches_cauldron", new AspectList().add(Aspects.METAL, 6).add(Aspects.CRYSTAL, 4)
                    .add(Aspects.GREED, 4).add(Aspects.ENERGY, 4).add(Aspects.MAGIC, 2).add(Aspects.WATER, 4)
                    .add(Aspects.CRAFT, 8));
            r.item("thaumcraft:anointing_paste", new AspectList().add(Aspects.PLANT, 1).add(Aspects.EXCHANGE, 1));
            r.item("thaumcraft:mutandis", new AspectList().add(Aspects.EXCHANGE, 4).add(Aspects.PLANT, 1));
            r.item("thaumcraft:mutandis_extremis", new AspectList().add(Aspects.EXCHANGE, 8).add(Aspects.PLANT, 1)
                    .add(Aspects.MAGIC, 1));
            r.item("thaumcraft:witch_altar", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.EARTH, 4)
                    .add(Aspects.MECHANISM, 3).add(Aspects.ENERGY, 3));

            // ---------------------------------------------------------- o que anda com os bichos
            r.item("thaumcraft:earmuffs", new AspectList().add(Aspects.SENSES, 1).add(Aspects.CLOTH, 1)
                    .add(Aspects.BEAST, 1).add(Aspects.ARMOR, 1));
            // o galho de Ent o original anota como o ramo de árvore que ele é
            r.item("thaumcraft:ent_branch", new AspectList().add(Aspects.TREE, 2).add(Aspects.MAGIC, 1));
            // o que a destilaria come e o que ela faz, com os aspectos do ModHookThaumcraft4
            r.item("thaumcraft:quicklime", new AspectList().add(Aspects.WEAPON, 1).add(Aspects.ENTROPY, 1));
            r.item("thaumcraft:gypsum", new AspectList().add(Aspects.EARTH, 1));
            r.item("thaumcraft:oil_of_vitriol", new AspectList().add(Aspects.WATER, 2).add(Aspects.ENTROPY, 4));
            r.item("thaumcraft:tear_of_the_goddess", new AspectList().add(Aspects.WATER, 2)
                    .add(Aspects.ORDER, 1).add(Aspects.SOUL, 2));
            r.item("thaumcraft:diamond_vapour", new AspectList().add(Aspects.AIR, 3).add(Aspects.CRYSTAL, 1));
            r.item("thaumcraft:ender_dew", new AspectList().add(Aspects.WATER, 2).add(Aspects.ELDRITCH, 2));
            r.item("thaumcraft:refined_evil", new AspectList().add(Aspects.WATER, 2).add(Aspects.MIND, 2)
                    .add(Aspects.ENTROPY, 2));

            // a lã de morcego: corpus 1 e volatus 1, que é o que o ModHookThaumcraft4 do original lhe dá
            r.item("thaumcraft:bat_wool", new AspectList().add(Aspects.FLESH, 1).add(Aspects.FLIGHT, 1));

            // o pote e o que cai dos bichos, com os aspectos do ModHookThaumcraft4 do original
            r.item("thaumcraft:witches_kettle", new AspectList().add(Aspects.WATER, 4).add(Aspects.FIRE, 2)
                    .add(Aspects.METAL, 3).add(Aspects.CRAFT, 2));
            r.item("thaumcraft:dog_tongue", new AspectList().add(Aspects.FLESH, 2).add(Aspects.BEAST, 1)
                    .add(Aspects.SENSES, 1));
            r.item("thaumcraft:creeper_heart", new AspectList().add(Aspects.FLESH, 2).add(Aspects.FIRE, 2)
                    .add(Aspects.ENTROPY, 2));
            r.item("thaumcraft:toe_of_frog", new AspectList().add(Aspects.FLESH, 1).add(Aspects.WATER, 1)
                    .add(Aspects.BEAST, 1));
            r.item("thaumcraft:redstone_soup", new AspectList().add(Aspects.ENERGY, 4).add(Aspects.MAGIC, 2)
                    .add(Aspects.WATER, 1));

            // o espelho: o original não o anotava, e este é do porte. A superfície do Mundo do Espelho não entra
            // aqui porque não há item dela — ela não se apanha, e o thaumômetro não tem o que ler
            r.item("thaumcraft:witch_mirror", new AspectList().add(Aspects.SENSES, 4).add(Aspects.ELDRITCH, 4)
                    .add(Aspects.TRAVEL, 4).add(Aspects.MAGIC, 2));
        });

        // e os bichos do ramo, para o thaumômetro os ler: são planta que anda, e o Ent é árvore.
        // O original não os anotava — é do porte, e vai no tom do resto.
        net.thaumcraft.research.EntityAspects.onRegister(r -> {
            r.entity("thaumcraft:mandrake", null, null, new AspectList().add(Aspects.PLANT, 4).add(Aspects.MAN, 2)
                    .add(Aspects.SENSES, 2));
            r.entity("thaumcraft:minedrake", null, null, new AspectList().add(Aspects.PLANT, 4).add(Aspects.MAN, 2)
                    .add(Aspects.FIRE, 2).add(Aspects.ENTROPY, 2));
            r.entity("thaumcraft:ent", null, null, new AspectList().add(Aspects.TREE, 8).add(Aspects.PLANT, 4)
                    .add(Aspects.MAGIC, 2).add(Aspects.BEAST, 2));
            r.entity("thaumcraft:mirror_face", null, null, new AspectList().add(Aspects.SENSES, 4)
                    .add(Aspects.MIND, 2).add(Aspects.ELDRITCH, 2));
            r.entity("thaumcraft:reflection", null, null, new AspectList().add(Aspects.MAN, 6)
                    .add(Aspects.ELDRITCH, 4).add(Aspects.SOUL, 4).add(Aspects.MAGIC, 2));
        });
    }
}
