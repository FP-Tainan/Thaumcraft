package net.thaumcraft.occulta.door;

import java.util.List;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.GlobalPos;
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
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaItems;

/**
 * <b>Juntar chaves numa argola</b>: o {@code RecipeShapelessAddKeys} do Witchery.
 *
 * <p>Lá eram duas receitas — duas chaves fazem um chaveiro, um chaveiro mais uma chave faz um chaveiro maior.
 * Aqui é <b>uma</b>, que faz as duas coisas: na bancada pode estar um chaveiro ou nenhum, e o resto são
 * chaves.
 *
 * <p>E cada porta entra <b>uma vez só</b>. Pôr duas chaves da mesma porta na bancada dá um chaveiro com uma
 * porta — o original compara cada chave com as que o chaveiro já tem antes de a acrescentar, e isto faz o
 * mesmo.
 */
public class KeyringRecipe extends CustomRecipe {
    public static final KeyringRecipe INSTANCE = new KeyringRecipe();
    public static final MapCodec<KeyringRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, KeyringRecipe> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<KeyringRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public static void init() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Thaumcraft.id("keyring"), SERIALIZER);
    }

    @Override
    public boolean matches(CraftingInput mesa, Level level) {
        int chaves = 0;
        int argolas = 0;
        int outros = 0;
        for (int i = 0; i < mesa.size(); i++) {
            ItemStack oquê = mesa.getItem(i);
            if (oquê.isEmpty()) continue;
            if (oquê.is(OccultaItems.DOOR_KEY)) chaves++;
            else if (oquê.is(OccultaItems.DOOR_KEYRING)) argolas++;
            else outros++;
        }
        if (outros != 0 || argolas > 1) return false;
        return argolas == 1 ? chaves >= 1 : chaves >= 2;
    }

    @Override
    public ItemStack assemble(CraftingInput mesa) {
        List<GlobalPos> tem = List.of();
        for (int i = 0; i < mesa.size(); i++) {
            ItemStack oquê = mesa.getItem(i);
            if (oquê.is(OccultaItems.DOOR_KEYRING)) tem = DoorKeys.doChaveiro(oquê);
        }
        for (int i = 0; i < mesa.size(); i++) {
            ItemStack oquê = mesa.getItem(i);
            if (!oquê.is(OccultaItems.DOOR_KEY)) continue;
            GlobalPos porta = DoorKeys.deQuê(oquê);
            if (porta != null) tem = DoorKeys.mais(tem, porta);
        }

        ItemStack argola = new ItemStack(OccultaItems.DOOR_KEYRING);
        argola.set(OccultaComponents.KEYRING, tem);
        return argola;
    }

    @Override
    public RecipeSerializer<KeyringRecipe> getSerializer() {
        return SERIALIZER;
    }
}
