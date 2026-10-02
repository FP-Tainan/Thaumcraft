package net.thaumcraft.occulta;

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

/**
 * Prender uma boneca a alguém: o {@code RecipeShapelessPoppet} do Witchery.
 *
 * <p>Uma boneca e um <b>Frasco de Vínculo cheio</b> na bancada, e a boneca passa a responder por quem estava no
 * frasco. O frasco se gasta.
 *
 * <p>Uma boneca já presa não se prende outra vez: para trocar de dono, faz-se outra.
 */
public class PoppetBindingRecipe extends CustomRecipe {
    public static final PoppetBindingRecipe INSTANCE = new PoppetBindingRecipe();
    public static final MapCodec<PoppetBindingRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, PoppetBindingRecipe> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<PoppetBindingRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public static void init() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Thaumcraft.id("poppet_binding"), SERIALIZER);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int bonecas = 0, frascos = 0, outros = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof PoppetItem && !TaglockItem.isBound(stack)) bonecas++;
            else if (stack.is(OccultaItems.TAGLOCK) && TaglockItem.isBound(stack)) frascos++;
            else outros++;
        }
        return outros == 0 && bonecas == 1 && frascos == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack boneca = ItemStack.EMPTY;
        TaglockItem.Taglock vínculo = null;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof PoppetItem) boneca = stack;
            else if (stack.is(OccultaItems.TAGLOCK)) vínculo = TaglockItem.bound(stack);
        }
        if (boneca.isEmpty() || vínculo == null) return ItemStack.EMPTY;
        ItemStack presa = boneca.copyWithCount(1);
        presa.set(OccultaComponents.TAGLOCK, vínculo);
        return presa;
    }

    /** O frasco se gasta: não volta vazio para a bancada. */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return NonNullList.withSize(input.size(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<PoppetBindingRecipe> getSerializer() {
        return SERIALIZER;
    }
}
