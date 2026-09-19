package net.thaumcraft.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.thaumcraft.Thaumcraft;

/**
 * A fornalha que dá o primeiro item de uma etiqueta: o {@code FurnaceRecipes.addSmelting(aglomerado, primeiro
 * lingote do dicionário)} que o {@code Config} do original fazia para estanho, prata e chumbo. O lingote é de outro mod,
 * então a receita não pode dizer qual; ela diz a etiqueta ({@code c:ingots/estanho}) e a conta sai na hora de fundir.
 */
public class TagSmeltingRecipe extends SmeltingRecipe {
    private final Ingredient input;
    private final TagKey<Item> resultTag;
    private final int count;

    public TagSmeltingRecipe(Ingredient input, TagKey<Item> resultTag, int count, float experience, int cookingTime) {
        // o que o livro de receitas mostra: o próprio aglomerado (a etiqueta ainda não existe quando a receita é lida)
        super(new Recipe.CommonInfo(true), new AbstractCookingRecipe.CookingBookInfo(CookingBookCategory.MISC, ""), input,
                new ItemStackTemplate(input.items().findFirst().map(h -> h.value()).orElse(net.minecraft.world.item.Items.BARRIER), count),
                experience, cookingTime);
        this.input = input;
        this.resultTag = resultTag;
        this.count = count;
    }

    /** O primeiro item da etiqueta, tantos quantos a receita pede. */
    public ItemStack tagResult() {
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.resultTag)) return new ItemStack(holder, this.count);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return this.tagResult();
    }

    public static final MapCodec<TagSmeltingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(r -> r.input),
            TagKey.codec(Registries.ITEM).fieldOf("result_tag").forGetter(r -> r.resultTag),
            Codec.INT.optionalFieldOf("count", 1).forGetter(r -> r.count),
            Codec.FLOAT.optionalFieldOf("experience", 0.0f).forGetter(TagSmeltingRecipe::experience),
            Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(TagSmeltingRecipe::cookingTime)
    ).apply(instance, TagSmeltingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TagSmeltingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.input,
            Identifier.STREAM_CODEC, r -> r.resultTag.location(),
            ByteBufCodecs.VAR_INT, r -> r.count,
            ByteBufCodecs.FLOAT, TagSmeltingRecipe::experience,
            ByteBufCodecs.VAR_INT, TagSmeltingRecipe::cookingTime,
            (input, tag, count, xp, time) -> new TagSmeltingRecipe(input, TagKey.create(Registries.ITEM, tag), count, xp, time));

    public static final RecipeSerializer<TagSmeltingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RecipeSerializer<SmeltingRecipe> getSerializer() {
        return (RecipeSerializer) SERIALIZER;
    }

    public static void init() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Thaumcraft.id("tag_smelting"), SERIALIZER);
    }
}
