package net.thaumcraft.occulta.leonard;

import com.mojang.serialization.MapCodec;
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
 * <b>Juntar duas urnas numa maior</b>: as três receitas de bancada do {@code WitcheryRecipes}.
 *
 * <p>O original escreve-as uma a uma — duas de uma casa dão a de duas, a de duas com uma de uma dá a de
 * três, a de três com uma de uma dá a de quatro — e todas pedem a <b>pequena</b> como segunda peça. Aqui
 * é uma receita só, com a regra que as três partilham: <b>uma urna qualquer mais uma pequena</b>, e sai a
 * do tamanho seguinte.
 *
 * <p>Há uma coisa que ela não faz, e é de propósito: o que estava <b>dentro</b> das duas <b>perde-se</b>.
 * É o que o original faz, porque lá a urna é um item com metadata e as receitas dele não copiam nada — e
 * é também o que faz sentido, que é uma urna nova feita de duas.
 *
 * <p>E a urna de <b>uma casa</b> não se faz: mata-se o Leonard outra vez.
 */
public class UrnJoiningRecipe extends CustomRecipe {
    public static final UrnJoiningRecipe INSTANCE = new UrnJoiningRecipe();
    public static final MapCodec<UrnJoiningRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, UrnJoiningRecipe> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<UrnJoiningRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public static void init() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Thaumcraft.id("urn_joining"), SERIALIZER);
    }

    @Override
    public boolean matches(CraftingInput entrada, Level level) {
        return !this.assemble(entrada).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput entrada) {
        int pequenas = 0;
        int maior = -1;
        int quantas = 0;
        for (int casa = 0; casa < entrada.size(); casa++) {
            ItemStack oquê = entrada.getItem(casa);
            if (oquê.isEmpty()) continue;
            if (!(oquê.getItem() instanceof LeonardsUrnItem)) return ItemStack.EMPTY;
            quantas++;
            int tamanho = LeonardsUrnItem.casas(oquê) - 1;
            if (tamanho == 0 && pequenas == 0) pequenas++;
            else if (tamanho > maior) maior = tamanho;
            else return ItemStack.EMPTY;
        }
        if (quantas != 2 || pequenas != 1 || maior < 0) return ItemStack.EMPTY;
        if (maior + 1 > LeonardsUrnItem.TAMANHOS - 1) return ItemStack.EMPTY;
        return LeonardsUrnItem.daquele(maior + 1);
    }

    @Override
    public RecipeSerializer<UrnJoiningRecipe> getSerializer() {
        return SERIALIZER;
    }
}
