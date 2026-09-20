package net.thaumcraft.research;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.thaumcraft.crafting.ArcaneRecipe;
import net.thaumcraft.crafting.ArcaneRecipes;
import net.thaumcraft.crafting.CrucibleRecipe;
import net.thaumcraft.crafting.CrucibleRecipes;
import net.thaumcraft.crafting.InfusionEnchantmentRecipe;
import net.thaumcraft.crafting.InfusionEnchantments;
import net.thaumcraft.crafting.InfusionRecipe;
import net.thaumcraft.crafting.InfusionRecipes;
import net.thaumcraft.crafting.RunicAugmentRecipe;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * O que cada página de receita do Thaumonomicon mostra de fato: o nome que a página cita (o do {@code ConfigRecipes}
 * original, em {@link BookRecipes}) vira a receita que está nas tabelas do mod.
 *
 * <p>As da bancada comum e as montagens vêm prontas do {@link BookRecipes}; as arcanas, as do crisol e as de infusão
 * são achadas pela pesquisa e pelo que sai. Quando um nome serve a várias (os fragmentos equilibrados, um por
 * fragmento), cada nome pega a primeira que os anteriores da mesma página ainda não pegaram.
 */
public final class BookPages {
    private BookPages() {
    }

    /** Uma página de lista mostra uma receita de cada vez; esta é a lista, já achada. */
    public static List<Object> resolve(Page.Recipe page) {
        List<Object> found = new ArrayList<>();
        for (String name : expand(page.names())) {
            Object recipe = resolve(page.kind(), name, found);
            if (recipe != null) found.add(recipe);
        }
        return found;
    }

    /** Os nomes com asterisco são as famílias feitas em laço ({@code JarLabel*}: um rótulo por aspecto). */
    private static List<String> expand(List<String> names) {
        List<String> out = new ArrayList<>();
        for (String name : names) {
            if (!name.endsWith("*")) {
                out.add(name);
                continue;
            }
            String base = name.substring(0, name.length() - 1) + "_";
            for (int i = 0; BookRecipes.get(base + i) != null; i++) out.add(base + i);
        }
        return out;
    }

    private static @Nullable Object resolve(Page.Kind kind, String name, List<Object> taken) {
        if (kind == Page.Kind.RUNIC) {
            // RunicAugment_n: o manto de taumaturgo já com n cargas, como o original monta a página
            int charge = Integer.parseInt(name.substring(name.indexOf('_') + 1));
            ItemStack robe = new ItemStack(TCItems.ROBE_CHESTPLATE);
            if (charge > 0) robe.set(TCComponents.RUNIC_HARDEN, charge);
            return RunicAugmentRecipe.forCentral(robe);
        }
        Object entry = BookRecipes.get(name);
        if (entry instanceof BookRecipes.Crafting || entry instanceof BookRecipes.Compound) return entry;
        // um mod de fora registra a própria receita com o nome, em vez de dizer pesquisa e saída
        if (entry instanceof CrucibleRecipe || entry instanceof InfusionRecipe) return entry;
        // os cetros de exemplo já vêm montados (a receita de verdade é a da bancada, que monta qualquer cetro)
        if (entry instanceof ArcaneRecipe arcane) return arcane;
        if (entry instanceof BookRecipes.Enchant enchant) {
            for (InfusionEnchantmentRecipe recipe : InfusionEnchantments.ALL) {
                if (recipe.research().equals(enchant.research())
                        && recipe.enchantment().identifier().toString().equals(enchant.enchantment())) {
                    return recipe;
                }
            }
            return null;
        }
        if (!(entry instanceof BookRecipes.Ref ref)) return null;
        ItemStack wanted = ref.result().get();
        List<?> table = switch (kind) {
            case ARCANE -> ArcaneRecipes.ALL;
            case CRUCIBLE -> CrucibleRecipes.ALL;
            case INFUSION -> InfusionRecipes.ALL;
            default -> List.of();
        };
        Object loose = null;
        for (Object recipe : table) {
            if (taken.contains(recipe)) continue;
            String research;
            ItemStack out;
            if (recipe instanceof ArcaneRecipe arcane) {
                research = arcane.research();
                out = arcane.result();
            } else if (recipe instanceof CrucibleRecipe crucible) {
                research = crucible.research();
                out = crucible.result();
            } else if (recipe instanceof InfusionRecipe infusion) {
                research = infusion.research();
                // a que marca a coisa do meio casa pela pesquisa
                if (infusion.onCentral() != null) {
                    if (ref.any() && research.equals(ref.research())) return recipe;
                    continue;
                }
                out = infusion.result();
            } else {
                continue;
            }
            if (!research.equals(ref.research()) || !ItemStack.isSameItem(out, wanted)) continue;
            if (ItemStack.isSameItemSameComponents(out, wanted)) return recipe;
            if (loose == null) loose = recipe;
        }
        return loose;
    }

    /** O que sai de uma receita, para a página que só tem uma (o {@code recipeOutput} do original). */
    public static ItemStack output(Object recipe) {
        return switch (recipe) {
            case BookRecipes.Crafting crafting -> crafting.result().get();
            case ArcaneRecipe arcane -> arcane.result();
            case CrucibleRecipe crucible -> crucible.result();
            // a que marca a coisa do meio aponta para a coisa do meio
            case InfusionRecipe infusion -> infusion.onCentral() == null ? infusion.result() : first(infusion.central());
            default -> ItemStack.EMPTY;
        };
    }

    public static ItemStack first(Ingredient ingredient) {
        return ingredient.items().findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    /** Onde um item é ensinado: a pesquisa e o número da página. */
    public record Location(String research, int page) {
    }

    private static final Map<net.minecraft.world.item.Item, java.util.Optional<Location>> WHERE = new HashMap<>();

    /**
     * O {@code ThaumcraftApi.getCraftingRecipeKey}: a primeira página, pesquisa por pesquisa, que ensina aquele item — a
     * de uma receita só, ou qualquer uma das do crisol, ou a da fornalha. Só vale se quem lê já sabe a pesquisa; se não
     * sabe a primeira que ensina, não vale nenhuma, como no original.
     */
    public static @Nullable Location whereMade(PlayerKnowledge knowledge, ItemStack stack) {
        if (stack.isEmpty()) return null;
        Location where = WHERE.computeIfAbsent(stack.getItem(), item -> java.util.Optional.ofNullable(search(stack))).orElse(null);
        if (where == null) return null;
        Research research = Researches.get(where.research());
        return research != null && ResearchManager.isComplete(knowledge, research) ? where : null;
    }

    private static @Nullable Location search(ItemStack stack) {
        for (Research research : Researches.ALL.values()) {
            List<Page> pages = research.pages();
            for (int a = 0; a < pages.size(); a++) {
                Page page = pages.get(a);
                if (page instanceof Page.Recipe recipe && recipe.kind() == Page.Kind.CRUCIBLE) {
                    for (Object each : resolve(recipe)) {
                        if (ItemStack.isSameItem(output(each), stack)) return new Location(research.key(), a);
                    }
                } else if (page instanceof Page.Recipe recipe && recipe.names().size() == 1) {
                    List<Object> list = resolve(recipe);
                    if (!list.isEmpty() && ItemStack.isSameItem(output(list.getFirst()), stack)) return new Location(research.key(), a);
                } else if (page instanceof Page.Smelting smelting && ItemStack.isSameItem(smelting.output().get(), stack)) {
                    return new Location(research.key(), a);
                }
            }
        }
        return null;
    }
}
