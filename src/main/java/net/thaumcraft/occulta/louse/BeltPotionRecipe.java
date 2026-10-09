package net.thaumcraft.occulta.louse;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaItems;

/**
 * Encher o <b>Cinto Mordedor</b> e o <b>Piolho Parasita</b>: a {@code RecipeShapelessAddPotion} do
 * Witchery.
 *
 * <p>Um cinto (ou um piolho) e uma <b>poção</b> na bancada, e a poção entra na peça. O cinto leva
 * <b>duas</b>; o piolho leva uma.
 *
 * <p>É assim que se decide o que a peça faz: um cinto cheio de veneno é uma armadilha para quem bater, e
 * um cinto cheio de regeneração é um curativo para quem o traz. A mesma peça, duas coisas, e quem a enche
 * escolhe.
 */
public class BeltPotionRecipe extends CustomRecipe {
    public static final BeltPotionRecipe INSTANCE = new BeltPotionRecipe();
    public static final MapCodec<BeltPotionRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, BeltPotionRecipe> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<BeltPotionRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public static void init() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Thaumcraft.id("belt_potion"), SERIALIZER);
    }

    /** O que se pode encher de poção: o cinto, que leva duas, e o piolho, que leva uma. */
    private static boolean enche(ItemStack coisa) {
        if (coisa.is(OccultaItems.BITING_BELT)) return BitingBelt.cabeMais(coisa);
        return coisa.is(OccultaItems.LOUSE) && !LouseItem.cheio(coisa);
    }

    private static boolean éPoção(ItemStack coisa) {
        return coisa.is(Items.POTION)
                && !coisa.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                        .equals(PotionContents.EMPTY);
    }

    @Override
    public boolean matches(CraftingInput bancada, Level level) {
        int peças = 0;
        int poções = 0;
        int outros = 0;
        for (int i = 0; i < bancada.size(); i++) {
            ItemStack coisa = bancada.getItem(i);
            if (coisa.isEmpty()) continue;
            if (enche(coisa)) peças++;
            else if (éPoção(coisa)) poções++;
            else outros++;
        }
        return outros == 0 && peças == 1 && poções == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput bancada) {
        ItemStack peça = ItemStack.EMPTY;
        PotionContents poção = PotionContents.EMPTY;
        for (int i = 0; i < bancada.size(); i++) {
            ItemStack coisa = bancada.getItem(i);
            if (coisa.isEmpty()) continue;
            if (enche(coisa)) peça = coisa;
            else if (éPoção(coisa)) {
                poção = coisa.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            }
        }
        if (peça.isEmpty() || poção.equals(PotionContents.EMPTY)) return ItemStack.EMPTY;

        if (peça.is(OccultaItems.LOUSE)) {
            ItemStack feito = peça.copyWithCount(1);
            feito.set(DataComponents.POTION_CONTENTS, poção);
            return feito;
        }
        return BitingBelt.enche(peça, poção);
    }

    /** O frasco fica na bancada, como qualquer garrafa: é o que o original faz. */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput bancada) {
        NonNullList<ItemStack> sobra = NonNullList.withSize(bancada.size(), ItemStack.EMPTY);
        for (int i = 0; i < bancada.size(); i++) {
            if (éPoção(bancada.getItem(i))) sobra.set(i, new ItemStack(Items.GLASS_BOTTLE));
        }
        return sobra;
    }

    @Override
    public RecipeSerializer<BeltPotionRecipe> getSerializer() {
        return SERIALIZER;
    }
}
