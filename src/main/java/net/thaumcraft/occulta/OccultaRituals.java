package net.thaumcraft.occulta;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * O que o Caldeirão da Bruxa faz com o que se joga dentro dele: o {@code BrewActionRitualRecipe} do Witchery.
 *
 * <p>A regra é a do original e tem uma ordem: joga-se primeiro o que a receita pede, e por último a coisa que
 * <b>dispara</b> — a chave. Quando a chave cai na água a ferver, o caldeirão olha o que já tem dentro; se bater
 * com uma receita, larga o que ela faz e esvazia.
 *
 * <p>Uma receita sem ingrediente nenhum dispara só com a chave — é assim que o caldeirão cozinha carne.
 *
 * <p><b>Do original ficam de fora, por agora,</b> as receitas que pedem coisas que o porte ainda não tem (a
 * Lágrima da Deusa, o Vapor de Diamante, a Mão de Bruxa, a Pedra Sintonizada) e as que pedem <b>poder de
 * altar</b>, que é coisa do altar e vem com ele.
 */
public final class OccultaRituals {
    /**
     * Uma receita do caldeirão: a chave que dispara, o que tem de estar dentro e o que sai.
     *
     * <p>E o que sai pode ser <b>uma coisa</b> ou <b>um bicho</b>. O original chama a segunda de
     * {@code BrewActionRitualSummonMob}, e só uma receita dele a usa: a que chama o <b>Leonard</b>.
     *
     * @param key         a coisa que se joga por último
     * @param ingredients o que tem de estar dentro antes dela
     * @param result      o que o caldeirão larga, ou vazio se ele chama um bicho
     * @param chama       o bicho que ele chama, ou nada se ele larga uma coisa
     */
    public record Ritual(Item key, List<Item> ingredients, ItemStack result,
                         @org.jetbrains.annotations.Nullable
                         net.minecraft.world.entity.EntityType<?> chama) {
    }

    private static final List<Ritual> RITUALS = new ArrayList<>();

    private OccultaRituals() {
    }

    public static List<Ritual> all() {
        return List.copyOf(RITUALS);
    }

    static {
        // o ovo com raiz de mandrágora e a exalação do Cornífero: seis Mutandis
        ritual(Items.EGG, List.of(OccultaItems.MANDRAKE_ROOT, OccultaItems.EXHALE_OF_THE_HORNED_ONE),
                () -> new ItemStack(OccultaItems.MUTANDIS, 6));

        // e o Mutandis com verruga do Nether: Mutandis Extremis
        ritual(OccultaItems.MUTANDIS, List.of(Items.NETHER_WART),
                () -> new ItemStack(OccultaItems.MUTANDIS_EXTREMIS));

        // a Gota de Sorte, que o Mutandis Extremis dispara
        ritual(OccultaItems.MUTANDIS_EXTREMIS, List.of(OccultaItems.MANDRAKE_ROOT, Items.NETHER_WART,
                OccultaItems.TEAR_OF_THE_GODDESS, OccultaItems.REFINED_EVIL),
                () -> new ItemStack(OccultaItems.DROP_OF_LUCK));

        // os três gizes que saem do caldeirão: o de ritual é a chave dos três
        ritual(OccultaItems.RITUAL_CHALK, List.of(Items.NETHER_WART, OccultaItems.TEAR_OF_THE_GODDESS,
                Items.ENDER_PEARL), () -> new ItemStack(OccultaItems.OTHERWHERE_CHALK));
        ritual(OccultaItems.RITUAL_CHALK, List.of(OccultaItems.MANDRAKE_ROOT, Items.GOLD_NUGGET),
                () -> new ItemStack(OccultaItems.GOLDEN_CHALK));
        ritual(OccultaItems.RITUAL_CHALK, List.of(Items.NETHER_WART, Items.BLAZE_POWDER),
                () -> new ItemStack(OccultaItems.INFERNAL_CHALK));

        /*
         * E a única receita do original que <b>chama um bicho</b>: o <b>Leonard</b>, com o Chapéu de Bruxa
         * por chave e cinco coisas dentro — verruga do Nether, Lágrima da Deusa, Vapor de Diamante, um
         * diamante e uma Estrela do Nether.
         *
         * <p><b>Fica de fora, declarado:</b> o original cobra <b>dez mil</b> de poder de altar por ela, e
         * nenhuma receita de caldeirão deste porte cobra poder — é a mesma coisa que já estava escrita no
         * cabeçalho desta classe. Sem isso, o preço dela é o que está na lista, que já não é pouco.
         */
        chamando(OccultaItems.WITCH_HAT, List.of(Items.NETHER_WART, OccultaItems.TEAR_OF_THE_GODDESS,
                        OccultaItems.DIAMOND_VAPOUR, Items.DIAMOND, Items.NETHER_STAR),
                OccultaEntities.LEONARD);

        // o caldeirão também cozinha carne, que é receita sem ingrediente nenhum
        ritual(Items.PORKCHOP, List.of(), () -> new ItemStack(Items.COOKED_PORKCHOP));
        ritual(Items.CHICKEN, List.of(), () -> new ItemStack(Items.COOKED_CHICKEN));
        ritual(Items.BEEF, List.of(), () -> new ItemStack(Items.COOKED_BEEF));
        ritual(Items.MUTTON, List.of(), () -> new ItemStack(Items.COOKED_MUTTON));
    }

    private static void ritual(Item key, List<Item> ingredients, java.util.function.Supplier<ItemStack> result) {
        RITUALS.add(new Ritual(key, ingredients, result.get(), null));
    }

    /** E a que chama um bicho em vez de largar uma coisa. */
    private static void chamando(Item key, List<Item> ingredients,
                                 net.minecraft.world.entity.EntityType<?> qual) {
        RITUALS.add(new Ritual(key, ingredients, ItemStack.EMPTY, qual));
    }

    /**
     * O bicho que a chave chama, se houver um.
     *
     * @param key    a coisa que acabou de cair na água
     * @param inside o que já estava dentro
     */
    public static net.minecraft.world.entity.@org.jetbrains.annotations.Nullable EntityType<?> summons(
            Item key, List<Item> inside) {
        Ritual qual = bate(key, inside);
        return qual == null ? null : qual.chama();
    }

    /** A receita que bate com isto, ou nada. */
    private static @org.jetbrains.annotations.Nullable Ritual bate(Item key, List<Item> inside) {
        for (Ritual ritual : RITUALS) {
            if (ritual.key() != key) continue;
            List<Item> faltando = new ArrayList<>(ritual.ingredients());
            for (Item dentro : inside) faltando.remove(dentro);
            if (faltando.isEmpty()) return ritual;
        }
        return null;
    }

    /**
     * O que sai, se o que está dentro bater com alguma receita da chave.
     *
     * @param key    a coisa que acabou de cair na água
     * @param inside o que já estava dentro, na ordem em que entrou
     * @return o que o caldeirão larga, ou vazio se nada bate
     */
    public static ItemStack result(Item key, List<Item> inside) {
        Ritual qual = bate(key, inside);
        return qual == null ? ItemStack.EMPTY : qual.result().copy();
    }

    /** Se aquilo serve de ingrediente em alguma receita: o que não serve a nenhuma não entra na panela. */
    public static boolean isIngredient(Item item) {
        return RITUALS.stream().anyMatch(ritual -> ritual.ingredients().contains(item) || ritual.key() == item);
    }

    /** Se aquilo dispara alguma coisa no caldeirão — o {@code triggersRitual} do original. */
    public static boolean triggers(Item key) {
        return RITUALS.stream().anyMatch(ritual -> ritual.key() == key);
    }
}
