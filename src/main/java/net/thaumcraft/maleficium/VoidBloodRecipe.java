package net.thaumcraft.maleficium;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.registry.TCComponents;

/**
 * O {@code RecipeVoidBlood} do Tainted Magic 8.1.1: um frasco de sangue infundido com o vazio e uma peça de armadura
 * qualquer, na mesa comum, devolvem a peça tocada pelo vazio — que dali em diante se conserta sozinha.
 */
public class VoidBloodRecipe extends CustomRecipe {
    public static final VoidBloodRecipe INSTANCE = new VoidBloodRecipe();
    public static final MapCodec<VoidBloodRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, VoidBloodRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<VoidBloodRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public static void init() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Thaumcraft.id("void_blood"), SERIALIZER);
    }

    /** A peça de armadura da mesa, se houver uma só, um frasco só e nada mais. */
    private static ItemStack armour(CraftingInput input) {
        ItemStack armour = ItemStack.EMPTY;
        boolean blood = false;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(MaleficiumItems.VOID_BLOOD)) {
                if (blood) return ItemStack.EMPTY;
                blood = true;
                continue;
            }
            if (!wearable(stack) || !armour.isEmpty()) return ItemStack.EMPTY;
            armour = stack;
        }
        return blood ? armour : ItemStack.EMPTY;
    }

    /** O que conta como armadura: o que se veste na cabeça, no peito, nas pernas ou nos pés. */
    private static boolean wearable(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null) return false;
        EquipmentSlot slot = equippable.slot();
        return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST
                || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack armour = armour(input);
        return !armour.isEmpty() && !Boolean.TRUE.equals(armour.get(TCComponents.VOID_TOUCHED));
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack armour = armour(input);
        if (armour.isEmpty()) return ItemStack.EMPTY;
        ItemStack copy = armour.copy();
        copy.setCount(1);
        copy.set(TCComponents.VOID_TOUCHED, true);
        return copy;
    }

    @Override
    public RecipeSerializer<VoidBloodRecipe> getSerializer() {
        return SERIALIZER;
    }
}
