package net.thaumcraft.occulta.vampire;

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
 * <b>Colar uma página no livro</b>: as nove receitas sem forma do {@code WitcheryRecipes}.
 *
 * <p>Lá eram nove receitas escritas à mão, uma por página — livro com zero mais página dá livro com uma,
 * livro com uma mais página dá livro com duas, e assim por diante. Aqui é <b>uma</b> receita que soma um,
 * porque o número de páginas vive num componente e não no dano do item.
 *
 * <p>Um livro <b>inteiro</b> não aceita mais nenhuma: ele some da bancada, e é como quem tenta colar uma
 * página a mais percebe que acabou.
 */
public class VampireBookRecipe extends CustomRecipe {
    public static final VampireBookRecipe INSTANCE = new VampireBookRecipe();
    public static final MapCodec<VampireBookRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, VampireBookRecipe> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<VampireBookRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public static void init() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Thaumcraft.id("vampire_book"), SERIALIZER);
    }

    @Override
    public boolean matches(CraftingInput mesa, Level level) {
        int livros = 0;
        int páginas = 0;
        int outros = 0;
        for (int i = 0; i < mesa.size(); i++) {
            ItemStack oquê = mesa.getItem(i);
            if (oquê.isEmpty()) continue;
            if (VampireBookItem.incompleto(oquê)) livros++;
            else if (oquê.is(net.thaumcraft.occulta.OccultaItems.TORN_PAGE)) páginas++;
            else outros++;
        }
        return outros == 0 && livros == 1 && páginas == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput mesa) {
        for (int i = 0; i < mesa.size(); i++) {
            ItemStack oquê = mesa.getItem(i);
            if (VampireBookItem.incompleto(oquê)) {
                return VampireBookItem.com(VampireBookItem.páginas(oquê) + 1);
            }
        }
        return ItemStack.EMPTY;
    }

    /** A página se gasta, e o livro velho também: o que sai é um livro novo. */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput mesa) {
        return NonNullList.withSize(mesa.size(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<VampireBookRecipe> getSerializer() {
        return SERIALIZER;
    }
}
