package net.thaumcraft.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.item.PhialItem;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;

/**
 * O rótulo marcado: as receitas {@code JarLabel0..47} e {@code JarLabelNull} do {@code ConfigRecipes} da 4.2.3.5.
 *
 * <p>Um rótulo com um frasco cheio vira um rótulo marcado com o aspecto do frasco (o frasco volta vazio). Um
 * rótulo marcado sozinho na mesa volta a ser um rótulo em branco.
 */
public class LabelMarkingRecipe extends CustomRecipe {
    public static final LabelMarkingRecipe INSTANCE = new LabelMarkingRecipe();
    public static final MapCodec<LabelMarkingRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, LabelMarkingRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<LabelMarkingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public static void init() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Thaumcraft.id("label_marking"), SERIALIZER);
    }

    private static boolean isLabel(ItemStack stack) {
        return stack.is(TCResources.get("jar_label"));
    }

    private static boolean isFilledPhial(ItemStack stack) {
        return stack.is(TCItems.PHIAL) && PhialItem.aspectOf(stack) != null;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int labels = 0, phials = 0, others = 0;
        ItemStack label = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (isLabel(stack)) {
                labels++;
                label = stack;
            } else if (isFilledPhial(stack)) {
                phials++;
            } else {
                others++;
            }
        }
        if (others > 0 || labels != 1) return false;
        // com um frasco, marca; sozinho, só se já estiver marcado (para apagar)
        return phials == 1 || phials == 0 && label.has(TCComponents.LABEL_ASPECT);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack out = new ItemStack(TCResources.get("jar_label"));
        for (int i = 0; i < input.size(); i++) {
            Aspect aspect = isFilledPhial(input.getItem(i)) ? PhialItem.aspectOf(input.getItem(i)) : null;
            if (aspect != null) out.set(TCComponents.LABEL_ASPECT, aspect.tag());
        }
        return out;
    }

    /** O frasco volta vazio. */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> left = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            if (isFilledPhial(input.getItem(i))) left.set(i, new ItemStack(TCItems.PHIAL));
        }
        return left;
    }

    @Override
    public RecipeSerializer<LabelMarkingRecipe> getSerializer() {
        return SERIALIZER;
    }
}
