package net.thaumcraft.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.naturalis.NaturalisItems;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A troca da Pedra do Catalisador Fenomorfo: as receitas {@code WoodConversion} e {@code ColorConversion} do
 * {@code MNRecipes} do Magia Naturalis 0.5.0.
 *
 * <p>A pedra na bancada com um bloco devolve o próximo bloco da família dele — a madeira arcana em roda, a lã e a
 * argila na cor oposta — e a pedra volta para a mão de quem fez, como o {@code getContainerItem} dela fazia. A
 * lista de pares é {@linkplain net.thaumcraft.naturalis.NaturalisTable#mutations() gerada} do original.
 */
public class MutationRecipe extends CustomRecipe {
    public static final MutationRecipe INSTANCE = new MutationRecipe();
    public static final MapCodec<MutationRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, MutationRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<MutationRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    /** De que bloco a pedra faz qual, na ordem em que o original os registra. */
    private static final Map<Item, Item> PAIRS = new LinkedHashMap<>();

    public static void init() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Thaumcraft.id("mutation"), SERIALIZER);
    }

    /** Um par da lista gerada. */
    public static void add(Item from, Item to) {
        PAIRS.put(from, to);
    }

    public static Map<Item, Item> pairs() {
        return Map.copyOf(PAIRS);
    }

    private static boolean isStone(ItemStack stack) {
        return stack.is(NaturalisItems.MUTATION_STONE);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return this.assemble(input) != ItemStack.EMPTY;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        int stones = 0;
        ItemStack alvo = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (isStone(stack)) {
                stones++;
            } else if (alvo.isEmpty()) {
                alvo = stack;
            } else {
                return ItemStack.EMPTY;
            }
        }
        if (stones != 1 || alvo.isEmpty() || alvo.getCount() != 1) return ItemStack.EMPTY;
        Item vira = PAIRS.get(alvo.getItem());
        return vira == null ? ItemStack.EMPTY : new ItemStack(vira);
    }

    /** A pedra não se gasta. */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> left = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            if (isStone(input.getItem(i))) left.set(i, input.getItem(i).copy());
        }
        return left;
    }

    @Override
    public RecipeSerializer<MutationRecipe> getSerializer() {
        return SERIALIZER;
    }
}
