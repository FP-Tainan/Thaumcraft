package net.thaumcraft.api.aspects;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Block;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.crafting.ArcaneRecipe;
import net.thaumcraft.crafting.ArcaneRecipes;
import net.thaumcraft.crafting.CrucibleRecipe;
import net.thaumcraft.crafting.CrucibleRecipes;
import net.thaumcraft.crafting.InfusionRecipe;
import net.thaumcraft.crafting.InfusionRecipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * De que cada coisa do jogo é feita: o {@code objectTags} do {@code ThaumcraftApi} e o
 * {@code ThaumcraftCraftingManager} da 4.2.3.5.
 *
 * <p>Como no original, poucas coisas têm aspecto anotado à mão — as do {@code ConfigAspects}, muitas por marca
 * (o dicionário de minérios de então). O resto é <b>deduzido das receitas</b>, pelo {@code generateTags}: o
 * crisol, a bancada arcana, a infusão e a mesa de trabalho, nessa ordem de preferência. Da mesa vêm três
 * quartos da soma dos ingredientes, divididos pelo que a receita rende, e fica a receita de menor soma; das
 * receitas mágicas soma-se ainda a raiz do custo. Nada passa de 64.
 *
 * <p>O servidor monta a tabela inteira ao abrir (quando as receitas e as marcas já estão carregadas) e a manda a
 * quem entra; o cliente só consulta. Por cima do que a tabela diz entram os bônus do {@code getBonusTags}:
 * armadura, arma, ferramenta, encantamentos, poções e a essência que a coisa carrega.
 */
public final class ObjectAspects {
    /** O {@code objectTags}: cada item com o que ele tem, anotado ou deduzido. */
    private static final Map<Item, AspectList> TABLE = new HashMap<>();
    /** Blocos que não viram item, como a água e a lava: o thaumômetro lê o bloco direto. */
    private static final Map<Block, AspectList> BLOCKS = new HashMap<>();
    /** O que o original anotava e este Minecraft não tem mais; fica registrado para não sumir calado. */
    private static final List<String> MISSING = new ArrayList<>();
    /** O que os mods de fora querem anotar: chamados toda vez que a tabela é montada. */
    private static final List<java.util.function.Consumer<Registrar>> HOOKS = new ArrayList<>();

    private ObjectAspects() {
    }

    // ----------------------------------------------------------------- consulta

    /** O que aquela coisa tem dentro, com os bônus; uma lista vazia se não tiver nada. */
    public static AspectList of(ItemStack stack) {
        if (stack.isEmpty()) return new AspectList();
        return ObjectBonus.apply(stack, objectTags(stack));
    }

    public static AspectList of(Item item) {
        return of(new ItemStack(item));
    }

    public static AspectList ofBlock(Block block) {
        AspectList found = BLOCKS.get(block);
        if (found != null) return found.copy();
        return of(block.asItem());
    }

    /** O {@code getObjectTags}: a tabela, mais a varinha e a poção, com o teto de 64. */
    public static AspectList objectTags(ItemStack stack) {
        AspectList found = TABLE.get(stack.getItem());
        AspectList tags = found == null ? new AspectList() : found.copy();
        ObjectBonus.objectExtras(stack, tags);
        return cap(tags, 64);
    }

    public static int size() {
        return TABLE.size();
    }

    public static List<String> missing() {
        return MISSING;
    }

    /** O que o cliente recebe do servidor. */
    public static Map<Item, AspectList> snapshot() {
        return new LinkedHashMap<>(TABLE);
    }

    public static void accept(Map<Item, AspectList> table) {
        TABLE.clear();
        TABLE.putAll(table);
        BLOCKS.clear();
        ConfigAspectsTable.blocks(new Registrar());
    }

    // ----------------------------------------------------------------- montagem

    /** O que está sendo montado: cada item com a anotação dele (vazia se nada se deduziu). */
    private static Map<Item, AspectList> building;
    /** As receitas da mesa de trabalho, pelo item que elas fazem. */
    private static Map<Item, List<Crafting>> crafting;

    /** Uma receita da mesa: as opções de cada ingrediente e quanto ela rende. */
    private record Crafting(List<List<Item>> ingredients, int count) {
    }

    /** Monta a tabela inteira, com as receitas e as marcas do servidor. */
    public static synchronized void rebuild(MinecraftServer server) {
        building = new HashMap<>();
        crafting = new HashMap<>();
        MISSING.clear();
        BLOCKS.clear();
        for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
            List<List<Item>> ingredients = new ArrayList<>();
            ItemStack out;
            if (holder.value() instanceof ShapedRecipe shaped) {
                for (Optional<Ingredient> slot : shaped.getIngredients()) slot.ifPresent(i -> ingredients.add(options(i)));
                out = shaped.assemble(CraftingInput.EMPTY);
            } else if (holder.value() instanceof ShapelessRecipe loose) {
                for (Ingredient i : loose.placementInfo().ingredients()) ingredients.add(options(i));
                out = loose.assemble(CraftingInput.EMPTY);
            } else {
                continue;
            }
            if (out.isEmpty()) continue;
            crafting.computeIfAbsent(out.getItem(), k -> new ArrayList<>()).add(new Crafting(ingredients, out.getCount()));
        }

        Registrar registrar = new Registrar();
        ConfigAspectsTable.register(registrar);
        // e o que o jogo ganhou depois da 1.7.10
        NewItemsAspectsTable.register(registrar);
        // e o que os mods de fora anotam das coisas deles, antes da dedução pelas receitas
        for (java.util.function.Consumer<Registrar> hook : HOOKS) hook.accept(registrar);
        ConfigAspectsTable.blocks(registrar);
        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR) generate(item, new ArrayList<>());
        }
        TABLE.clear();
        for (var entry : building.entrySet()) {
            if (!entry.getValue().isEmpty()) TABLE.put(entry.getKey(), entry.getValue());
        }
        building = null;
        crafting = null;
        Thaumcraft.LOGGER.info("{} coisas com aspecto; {} anotações do original sem item no jogo de hoje",
                TABLE.size(), MISSING.size());
    }

    private static List<Item> options(Ingredient ingredient) {
        List<Item> items = new ArrayList<>();
        ingredient.items().forEach(holder -> items.add(holder.value()));
        return items;
    }

    /** O {@code ThaumcraftApi.exists}: já tem anotação, mesmo que vazia. */
    private static boolean exists(Item item) {
        return building.containsKey(item);
    }

    /** O {@code generateTags}: a anotação, se houver; senão, deduz das receitas e guarda. */
    private static AspectList generate(Item item, List<Item> history) {
        if (exists(item)) return building.get(item);
        if (history.contains(item)) return null;
        history.add(item);
        if (history.size() >= 100) return null;
        AspectList found = cap(fromRecipes(item, history), 64);
        building.put(item, found == null ? new AspectList() : found);
        return found;
    }

    private static AspectList fromRecipes(Item item, List<Item> history) {
        AspectList found = fromCrucible(item);
        if (found != null) return found;
        found = fromArcane(item, history);
        if (found != null) return found;
        found = fromInfusion(item, history);
        if (found != null) return found;
        return fromCrafting(item, history);
    }

    /** Do crisol: o que o catalisador tem, mais a raiz de cada aspecto da água, pelo que a receita rende. */
    private static AspectList fromCrucible(Item item) {
        for (CrucibleRecipe recipe : CrucibleRecipes.ALL) {
            if (!recipe.result().is(item)) continue;
            AspectList out = new AspectList();
            AspectList catalyst = generate(recipe.catalyst(), new ArrayList<>());
            if (catalyst != null) out.add(catalyst);
            int count = recipe.result().getCount();
            for (Aspect aspect : recipe.cost().getAspects()) {
                out.add(aspect, (int) (Math.sqrt(recipe.cost().getAmount(aspect)) / count));
            }
            return positive(out);
        }
        return null;
    }

    /** Da bancada arcana: os ingredientes, mais a raiz do custo em vis; fica a última receita, como no original. */
    private static AspectList fromArcane(Item item, List<Item> history) {
        AspectList result = null;
        for (ArcaneRecipe recipe : ArcaneRecipes.ALL) {
            if (!recipe.result().is(item)) continue;
            List<Item> ingredients = new ArrayList<>();
            for (Ingredient slot : recipe.pattern()) {
                if (slot == null) continue;
                Item chosen = choose(options(slot), history);
                if (chosen != null) ingredients.add(chosen);
            }
            int count = recipe.result().getCount();
            AspectList out = fromIngredients(ingredients, count, history);
            for (Aspect aspect : recipe.cost().getAspects()) {
                out.add(aspect, (int) (Math.sqrt(recipe.cost().getAmount(aspect)) / count));
            }
            result = positive(out);
        }
        return result;
    }

    /** Da infusão: o miolo e os componentes, mais a raiz da essência. */
    private static AspectList fromInfusion(Item item, List<Item> history) {
        for (InfusionRecipe recipe : InfusionRecipes.ALL) {
            if (!recipe.result().is(item)) continue;
            List<Item> ingredients = new ArrayList<>();
            Item central = choose(options(recipe.central()), history);
            if (central != null) ingredients.add(central);
            for (Ingredient component : recipe.components()) {
                Item chosen = choose(options(component), history);
                if (chosen != null) ingredients.add(chosen);
            }
            int count = recipe.result().getCount();
            AspectList out = fromIngredients(ingredients, count, history);
            for (Aspect aspect : recipe.essentia().getAspects()) {
                out.add(aspect, (int) (Math.sqrt(recipe.essentia().getAmount(aspect)) / count));
            }
            return positive(out);
        }
        return null;
    }

    /** Da mesa de trabalho: fica a receita de menor soma que der alguma coisa. */
    private static AspectList fromCrafting(Item item, List<Item> history) {
        List<Crafting> recipes = crafting.get(item);
        if (recipes == null) return null;
        AspectList best = null;
        int value = Integer.MAX_VALUE;
        for (Crafting recipe : recipes) {
            List<Item> ingredients = new ArrayList<>();
            for (List<Item> slot : recipe.ingredients()) {
                Item chosen = choose(slot, history);
                if (chosen != null) ingredients.add(chosen);
            }
            AspectList out = fromIngredients(ingredients, recipe.count(), history);
            if (out.visSize() < value && out.visSize() > 0) {
                best = out;
                value = out.visSize();
            }
        }
        return best;
    }

    /** Um ingrediente de uma opção só é ela; o de marca vale a primeira opção que tiver aspecto. */
    private static Item choose(List<Item> options, List<Item> history) {
        if (options.isEmpty()) return null;
        if (options.size() == 1) return options.getFirst();
        for (Item option : options) {
            AspectList tags = generate(option, history);
            if (tags != null && !tags.isEmpty()) return option;
        }
        return null;
    }

    /** O {@code getAspectsFromIngredients}: três quartos da soma, pelo que a receita rende. */
    private static AspectList fromIngredients(List<Item> ingredients, int count, List<Item> history) {
        AspectList mid = new AspectList();
        for (Item ingredient : ingredients) {
            AspectList tags = generate(ingredient, history);
            if (tags != null) mid.add(tags);
        }
        AspectList out = new AspectList();
        for (Aspect aspect : mid.getAspects()) {
            out.add(aspect, (int) (mid.getAmount(aspect) * 0.75f / Math.max(1, count)));
        }
        return positive(out);
    }

    private static AspectList positive(AspectList list) {
        for (Aspect aspect : list.getAspects()) {
            if (list.getAmount(aspect) <= 0) list.remove(aspect);
        }
        return list;
    }

    /** O {@code capAspects}: nenhum aspecto passa do teto. */
    static AspectList cap(AspectList source, int amount) {
        if (source == null) return null;
        AspectList out = new AspectList();
        for (Aspect aspect : source.getAspects()) out.merge(aspect, Math.min(amount, source.getAmount(aspect)));
        return out;
    }

    // ----------------------------------------------------------------- as anotações do ConfigAspects

    /** Quem recebe as anotações da tabela gerada, na ordem do original. */
    /**
     * Um mod de fora diz aqui de que as coisas dele são feitas — o {@code registerObjectTag} do
     * {@code ThaumcraftApi}. Chama-se isto uma vez, ao carregar o mod; a anotação vale em toda montagem da tabela,
     * inclusive quando o mundo recarrega.
     */
    public static void onRegister(java.util.function.Consumer<Registrar> hook) {
        HOOKS.add(hook);
    }

    public static final class Registrar {
        private Registrar() {
        }

        private static Item find(String id) {
            Item item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).orElse(null);
            if (item == null || item == Items.AIR) {
                MISSING.add(id);
                return null;
            }
            return item;
        }

        /** O {@code registerObjectTag(ItemStack, ...)}: a anotação vale para o item. */
        public void item(String id, AspectList aspects) {
            Item item = find(id);
            if (item != null) building.put(item, aspects.copy());
        }

        /** O {@code registerObjectTag(String oreDict, ...)}: vale para tudo o que a marca tiver. */
        public void tag(String id, AspectList aspects) {
            TagKey<Item> key = TagKey.create(Registries.ITEM, Identifier.parse(id));
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(key)) building.put(holder.value(), aspects.copy());
        }

        /**
         * O jeito dos addons do original: {@code getObjectAspects}, somar o que é seu e registrar de volta. Se a
         * coisa ainda não tiver anotação, esta vira a dela.
         */
        public void add(String id, AspectList extra) {
            Item item = find(id);
            if (item == null) return;
            AspectList list = building.containsKey(item) ? building.get(item).copy() : new AspectList();
            list.add(extra);
            building.put(item, list);
        }

        /** O mesmo, num bloco que não vira item (o portal, o fogo, a água). */
        public void blockAdd(String id, AspectList extra) {
            BuiltInRegistries.BLOCK.getOptional(Identifier.parse(id)).ifPresent(block -> {
                AspectList list = BLOCKS.containsKey(block) ? BLOCKS.get(block).copy() : new AspectList();
                list.add(extra);
                BLOCKS.put(block, list);
            });
        }

        /** A anotação que só vale se aquilo ainda não tiver uma: serve para tapar buracos sem mexer no que o original diz. */
        public void itemIfAbsent(String id, AspectList aspects) {
            Item item = find(id);
            if (item != null && !building.containsKey(item)) building.put(item, aspects.copy());
        }

        /** O mesmo por marca: cada coisa da marca que ainda não tem anotação ganha esta. */
        public void tagIfAbsent(String id, AspectList aspects) {
            TagKey<Item> key = TagKey.create(Registries.ITEM, Identifier.parse(id));
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(key)) {
                if (!building.containsKey(holder.value())) building.put(holder.value(), aspects.copy());
            }
        }

        /** Um bloco que não vira item (a água, a lava, o fogo, os portais). */
        public void block(String id, AspectList aspects) {
            BuiltInRegistries.BLOCK.getOptional(Identifier.parse(id)).ifPresent(block -> BLOCKS.put(block, aspects.copy()));
        }

        /**
         * O {@code registerComplexObjectTag}: deduz das receitas e soma o que foi dito. Se já tiver anotação, o
         * original não soma nada (ele junta a lista com ela mesma) — fica igual.
         */
        public void complex(String id, AspectList aspects) {
            Item item = find(id);
            if (item == null || exists(item)) return;
            List<Item> history = new ArrayList<>();
            history.add(item);
            AspectList found = cap(fromRecipes(item, history), 64);
            AspectList out = aspects.copy();
            if (found != null) out.add(found);
            building.put(item, out);
        }

        /** O {@code new AspectList(new ItemStack(...))}: o que a coisa tem agora. */
        public AspectList copy(String id) {
            Item item = find(id);
            if (item == null) return new AspectList();
            AspectList found = generate(item, new ArrayList<>());
            return found == null ? new AspectList() : found.copy();
        }
    }
}
