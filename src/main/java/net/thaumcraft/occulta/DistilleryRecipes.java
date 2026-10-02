package net.thaumcraft.occulta;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * O que a Destilaria faz de quê: o {@code DistilleryRecipes} do Witchery.
 *
 * <p>Cada receita pede <b>duas coisas</b> — em qualquer ordem, que é o que o {@code isMatch} do original faz — e
 * um tanto de <b>Potes de Barro</b>, e devolve até <b>quatro</b>. Os potes se gastam: é neles que sai o que se
 * destila.
 *
 * <p>Esta é a parte da tabela que o mod de hoje alcança. <b>Fica declarado o que falta</b>, por depender de coisa
 * ainda não portada: as <b>duas do Coração de Demônio</b>, que dão o Sangue Infernal — o demônio não está
 * portado, e sem ele não há coração.
 */
public final class DistilleryRecipes {
    /**
     * Uma receita: o que entra, quantos potes, e o que sai.
     *
     * @param inputs as duas coisas que entram — a segunda pode faltar
     * @param jars   quantos potes de barro se gastam
     * @param outputs até quatro coisas que saem
     */
    public record Recipe(List<ItemStack> inputs, int jars, List<ItemStack> outputs) {
        /** O {@code isMatch}: a ordem das duas não importa. */
        public boolean matches(ItemStack a, ItemStack b, ItemStack potes) {
            if (this.jars > 0 && potes.getCount() < this.jars) return false;
            ItemStack primeiro = this.inputs.getFirst();
            ItemStack segundo = this.inputs.size() > 1 ? this.inputs.get(1) : ItemStack.EMPTY;
            return same(a, primeiro) && same(b, segundo) || same(a, segundo) && same(b, primeiro);
        }

        private static boolean same(ItemStack tem, ItemStack pede) {
            if (pede.isEmpty()) return tem.isEmpty();
            return tem.is(pede.getItem()) && tem.getCount() >= pede.getCount();
        }
    }

    private static final List<Recipe> RECIPES = new ArrayList<>();
    private static boolean pronta;

    private DistilleryRecipes() {
    }

    public static List<Recipe> all() {
        build();
        return List.copyOf(RECIPES);
    }

    /** A receita que bate com o que está na máquina, ou nada. */
    public static Recipe find(ItemStack a, ItemStack b, ItemStack potes) {
        build();
        for (Recipe receita : RECIPES) {
            if (receita.matches(a, b, potes)) return receita;
        }
        return null;
    }

    private static void add(ItemStack a, ItemStack b, int potes, ItemStack... saem) {
        List<ItemStack> entra = b.isEmpty() ? List.of(a) : List.of(a, b);
        List<ItemStack> sai = new ArrayList<>();
        for (ItemStack coisa : saem) {
            if (!coisa.isEmpty()) sai.add(coisa);
        }
        RECIPES.add(new Recipe(entra, potes, List.copyOf(sai)));
    }

    private static ItemStack um(Item qual) {
        return new ItemStack(qual);
    }

    private static ItemStack tantos(Item qual, int quantos) {
        return new ItemStack(qual, quantos);
    }

    private static synchronized void build() {
        if (pronta) return;
        pronta = true;

        // a Exalação Fétida com cal virgem: gesso, óleo de vitríolo e uma bexiga
        add(um(OccultaItems.FOUL_FUME), um(OccultaItems.QUICKLIME), 1,
                um(OccultaItems.GYPSUM), um(OccultaItems.OIL_OF_VITRIOL), um(Items.SLIME_BALL), ItemStack.EMPTY);

        // o Sopro da Deusa com lápis-lazúli: a Lágrima da Deusa
        add(um(OccultaItems.BREATH_OF_THE_GODDESS), um(Items.LAPIS_LAZULI), 3,
                um(OccultaItems.TEAR_OF_THE_GODDESS), um(OccultaItems.WHIFF_OF_MAGIC), um(Items.SLIME_BALL),
                um(OccultaItems.FOUL_FUME));

        // o diamante com óleo de vitríolo: o Vapor de Diamante, dois
        add(um(Items.DIAMOND), um(OccultaItems.OIL_OF_VITRIOL), 3,
                um(OccultaItems.DIAMOND_VAPOUR), um(OccultaItems.DIAMOND_VAPOUR),
                um(OccultaItems.ODOUR_OF_PURITY), ItemStack.EMPTY);

        // o Vapor de Diamante com uma lágrima de ghast: o Mal Refinado
        add(um(OccultaItems.DIAMOND_VAPOUR), um(Items.GHAST_TEAR), 3,
                um(OccultaItems.ODOUR_OF_PURITY), um(OccultaItems.REEK_OF_MISFORTUNE),
                um(OccultaItems.FOUL_FUME), um(OccultaItems.REFINED_EVIL));

        // a pérola do Ender sozinha: o Orvalho do Ender, cinco
        add(um(Items.ENDER_PEARL), ItemStack.EMPTY, 6,
                tantos(OccultaItems.ENDER_DEW, 2), tantos(OccultaItems.ENDER_DEW, 2),
                um(OccultaItems.ENDER_DEW), um(OccultaItems.WHIFF_OF_MAGIC));

        // e o pó de blaze com pólvora: pedra luminosa
        add(um(Items.BLAZE_POWDER), um(Items.GUNPOWDER), 1,
                um(Items.GLOWSTONE_DUST), um(Items.GLOWSTONE_DUST),
                um(OccultaItems.REEK_OF_MISFORTUNE), ItemStack.EMPTY);

        // o Cozimento do Espírito Corrente com óleo de vitríolo: a Vontade Focada, o Medo Condensado e oito
        // frascos de Lágrimas Ocas. É a destilação que abre o fim da linha do outro lado.
        add(um(OccultaItems.BREW_OF_FLOWING_SPIRIT), um(OccultaItems.OIL_OF_VITRIOL), 2,
                um(OccultaItems.FOCUSED_WILL), um(OccultaItems.CONDENSED_FEAR),
                tantos(OccultaItems.BREW_OF_HOLLOW_TEARS, 4), tantos(OccultaItems.BREW_OF_HOLLOW_TEARS, 4));
    }
}
