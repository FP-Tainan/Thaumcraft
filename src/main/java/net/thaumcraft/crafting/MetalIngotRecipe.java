package net.thaumcraft.crafting;

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
 * Nove pepitas de estanho, prata ou chumbo do Thaumcraft voltam a um lingote: o {@code oreDictRecipe(lingote, "###",
 * "###", "###", pepita)} que o {@code Config} do original fazia com o primeiro lingote do dicionário. O lingote é de
 * outro mod, por isso a receita é feita na hora: sem lingote na etiqueta, não há receita.
 */
public class MetalIngotRecipe extends CustomRecipe {
    public static final MetalIngotRecipe INSTANCE = new MetalIngotRecipe();
    public static final MapCodec<MetalIngotRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, MetalIngotRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<MetalIngotRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public static void init() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Thaumcraft.id("metal_ingot_from_nuggets"), SERIALIZER);
    }

    private static OtherMetals.Metal metalOf(CraftingInput input) {
        if (input.width() != 3 || input.height() != 3 || input.ingredientCount() != 9) return null;
        for (OtherMetals.Metal metal : OtherMetals.ALL) {
            boolean all = true;
            for (int i = 0; i < input.size(); i++) {
                if (!input.getItem(i).is(metal.nugget())) {
                    all = false;
                    break;
                }
            }
            if (all) return metal;
        }
        return null;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        OtherMetals.Metal metal = metalOf(input);
        return metal != null && !OtherMetals.firstIngot(metal).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        OtherMetals.Metal metal = metalOf(input);
        return metal == null ? ItemStack.EMPTY : OtherMetals.firstIngot(metal);
    }

    @Override
    public RecipeSerializer<MetalIngotRecipe> getSerializer() {
        return SERIALIZER;
    }
}
