package net.thaumcraft.occulta.spirit;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaItems;
import org.jetbrains.annotations.Nullable;

/**
 * Tecer uma Teia de Sonho na bancada: as oito {@code GameRegistry.addRecipe} das teias do Witchery.
 *
 * <p>Todas as cinco têm a mesma forma, que é a de um apanhador:
 *
 * <pre>
 *   d x e
 *   b a b
 *   c f c
 * </pre>
 *
 * <p>com <b>a</b> a moldura de quadro, <b>x</b> o Vapor de Diamante, <b>c</b> a pena, <b>b</b> o Fio Enfeitado e
 * <b>f</b> o Cordel Atormentado. O que muda de teia para teia são os dois cantos de cima, <b>d</b> e <b>e</b>: são
 * eles que dizem qual sai. A do pesadelo é a única que troca a forma de baixo — nela o <b>b</b> e o <b>f</b> são
 * todos Cordel Atormentado, sem Fio Enfeitado nenhum.
 *
 * <p>Os dois cantos das quatro primeiras são <b>poções do jogo</b>, e é por isso que esta receita não é um
 * {@code crafting_shaped} de arquivo: no Minecraft de hoje um ingrediente de receita não sabe olhar os
 * componentes de uma coisa, e portanto não sabe distinguir uma poção de Rapidez de uma de Veneno. A conta
 * fica aqui, em Java, e as poções são as <b>mesmas</b> do original — decifradas do número de dano de 1.7.10:
 * 16450 é Rapidez longa, 16458 Lentidão longa, 16457 Força longa, 16456 Fraqueza longa, 16421 Cura II,
 * 16452 Veneno longo e 16454 Visão Noturna longa. Todas de <b>atirar</b>, que é o que o bit 16384 diz.
 *
 * <p><b>Desvio declarado:</b> o original aceita os dois cantos em qualquer das duas ordens porque a receita
 * moldada do jogo antigo se experimentava também espelhada; aqui isso é explícito — <b>d</b> e <b>e</b> podem
 * estar trocados.
 */
public class DreamWeaveRecipe extends CustomRecipe {
    public static final DreamWeaveRecipe INSTANCE = new DreamWeaveRecipe();
    public static final MapCodec<DreamWeaveRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, DreamWeaveRecipe> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<DreamWeaveRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public static void init() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Thaumcraft.id("dream_weave"), SERIALIZER);
    }

    /** O que sai daqueles dois cantos, ou nada se eles não forem par de teia nenhuma. */
    private static DreamWeaveItem.@Nullable Weave weaveOf(ItemStack um, ItemStack outro) {
        for (int volta = 0; volta < 2; volta++) {
            ItemStack bom = volta == 0 ? um : outro;
            ItemStack mau = volta == 0 ? outro : um;

            if (potion(bom, Potions.LONG_SWIFTNESS) && potion(mau, Potions.LONG_SLOWNESS)) {
                return DreamWeaveItem.Weave.MOVE;
            }
            if (potion(bom, Potions.LONG_STRENGTH) && potion(mau, Potions.LONG_WEAKNESS)) {
                return DreamWeaveItem.Weave.DIG;
            }
            if (potion(bom, Potions.STRONG_HEALING) && mau.is(OccultaItems.MELLIFLUOUS_HUNGER)) {
                return DreamWeaveItem.Weave.EAT;
            }
            if (potion(bom, Potions.LONG_POISON) && potion(mau, Potions.LONG_NIGHT_VISION)) {
                return DreamWeaveItem.Weave.NIGHTMARE;
            }
            if (bom.is(OccultaItems.BREW_OF_FLOWING_SPIRIT) && mau.is(OccultaItems.BREW_OF_SLEEPING)) {
                return DreamWeaveItem.Weave.INTENSITY;
            }
        }
        return null;
    }

    /** Se aquilo é a poção de atirar daquele feitio. */
    private static boolean potion(ItemStack coisa, Holder<Potion> qual) {
        if (!coisa.is(Items.SPLASH_POTION)) return false;
        PotionContents dentro = coisa.get(DataComponents.POTION_CONTENTS);
        return dentro != null && dentro.potion().filter(p -> p.is(qual)).isPresent();
    }

    /** A teia que esta bancada dá, ou nada. */
    private static DreamWeaveItem.@Nullable Weave read(CraftingInput input) {
        if (input.width() != 3 || input.height() != 3) return null;

        if (!input.getItem(1).is(OccultaItems.DIAMOND_VAPOUR)) return null;
        if (!input.getItem(4).is(Items.ITEM_FRAME)) return null;
        if (!input.getItem(6).is(Items.FEATHER) || !input.getItem(8).is(Items.FEATHER)) return null;

        DreamWeaveItem.Weave qual = weaveOf(input.getItem(0), input.getItem(2));
        if (qual == null) return null;

        ItemStack esquerda = input.getItem(3);
        ItemStack direita = input.getItem(5);
        ItemStack baixo = input.getItem(7);
        if (qual == DreamWeaveItem.Weave.NIGHTMARE) {
            // a do pesadelo é toda de cordel: nem um fio enfeitado nela
            if (!esquerda.is(OccultaItems.TORMENTED_TWINE) || !direita.is(OccultaItems.TORMENTED_TWINE)
                    || !baixo.is(OccultaItems.TORMENTED_TWINE)) {
                return null;
            }
        } else {
            if (!esquerda.is(OccultaItems.FANCIFUL_THREAD) || !direita.is(OccultaItems.FANCIFUL_THREAD)
                    || !baixo.is(OccultaItems.TORMENTED_TWINE)) {
                return null;
            }
        }
        return qual;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return read(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        DreamWeaveItem.Weave qual = read(input);
        return qual == null ? ItemStack.EMPTY : new ItemStack(OccultaItems.weave(qual));
    }

    @Override
    public RecipeSerializer<DreamWeaveRecipe> getSerializer() {
        return SERIALIZER;
    }
}
