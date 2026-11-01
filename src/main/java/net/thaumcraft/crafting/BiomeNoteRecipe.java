package net.thaumcraft.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.biome.BiomeBookItem;
import net.thaumcraft.occulta.biome.BiomeNoteItem;

/**
 * A <b>Nota de Bioma</b> que sai do livro: o {@code getContainerItem} do {@code ItemBook} do Witchery, e a
 * receita que o acompanha.
 *
 * <p>Um {@linkplain BiomeBookItem Livro de Biomas} e uma folha de <b>papel</b> na bancada dão uma nota com o
 * bioma da folha em que o livro estava aberto. É o único jeito de fazer uma nota, e é de propósito: o bioma
 * que se quer tem de se <b>achar no livro</b> primeiro.
 *
 * <p>Ela é uma receita de código e não de dados por uma razão só: o que sai dela <b>depende do que entrou</b>
 * — nenhuma receita escrita em JSON sabe copiar o bioma de dentro de um item para dentro de outro.
 *
 * <p>E o <b>livro volta</b>, com a folha aberta onde estava. É o {@code getContainerItem} do original, que
 * devolvia uma cópia do próprio livro em vez de o gastar; o caminho de hoje para isso é o que sobra na
 * bancada.
 */
public class BiomeNoteRecipe extends CustomRecipe {
    public static final BiomeNoteRecipe INSTANCE = new BiomeNoteRecipe();
    public static final MapCodec<BiomeNoteRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, BiomeNoteRecipe> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<BiomeNoteRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public static void init() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Thaumcraft.id("biome_note"), SERIALIZER);
    }

    /** O livro, só se ele estiver aberto em algum bioma: um livro em branco não copia nada. */
    private static boolean éOLivro(ItemStack stack) {
        return stack.getItem() instanceof BiomeBookItem && BiomeBookItem.bioma(stack) != null;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int livros = 0, papéis = 0, outros = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (éOLivro(stack)) livros++;
            else if (stack.is(Items.PAPER)) papéis++;
            else outros++;
        }
        return outros == 0 && livros == 1 && papéis == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        for (int i = 0; i < input.size(); i++) {
            if (!éOLivro(input.getItem(i))) continue;
            var qual = BiomeBookItem.bioma(input.getItem(i));
            if (qual != null) return BiomeNoteItem.de(OccultaItems.BIOME_NOTE, qual);
        }
        return ItemStack.EMPTY;
    }

    /** O livro volta, com a folha onde estava. */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> sobra = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            if (éOLivro(input.getItem(i))) sobra.set(i, input.getItem(i).copy());
        }
        return sobra;
    }

    @Override
    public RecipeSerializer<BiomeNoteRecipe> getSerializer() {
        return SERIALIZER;
    }
}
