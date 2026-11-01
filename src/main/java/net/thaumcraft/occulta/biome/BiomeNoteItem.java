package net.thaumcraft.occulta.biome;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.thaumcraft.occulta.OccultaComponents;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Nota de Bioma</b>: o {@code ItemBiomeNote} do Witchery.
 *
 * <p>Um papel com o nome de um bioma escrito, e nada mais. Ela não faz nada na mão de ninguém — ela é um
 * <b>recado</b>, e serve a quem a lê: o caldeirão. Caída na panela com o resto, é ela que diz em que bioma
 * o cozimento das {@linkplain net.thaumcraft.occulta.brew.BrewWorldActions.BiomeChange Estações que Mudam}
 * vai transformar o lugar onde o frasco bater.
 *
 * <p>Ela sai do {@linkplain BiomeBookItem Livro de Biomas} com uma folha de papel, e sai com o bioma da
 * folha em que o livro estava aberto. É esse o único jeito de fazer uma: não há receita de bancada para o
 * bioma que se quer — há que o <b>achar no livro</b> primeiro.
 *
 * <p>O nome dela é o nome do bioma. Uma nota em branco, que só aparece em criativo, chama-se só «Nota de
 * Bioma» e não serve para nada — e é assim no original, onde o {@code String.format} com o bioma nulo caía
 * no mesmo lugar.
 */
public class BiomeNoteItem extends Item {
    public BiomeNoteItem(Properties properties) {
        super(properties);
    }

    /** Que bioma está escrito nela. */
    public static @Nullable ResourceKey<Biome> bioma(ItemStack stack) {
        return stack.get(OccultaComponents.BIOME);
    }

    /** Uma nota com aquele bioma escrito. */
    public static ItemStack de(Item qual, ResourceKey<Biome> bioma) {
        ItemStack nota = new ItemStack(qual);
        nota.set(OccultaComponents.BIOME, bioma);
        return nota;
    }

    /** O {@code getItemStackDisplayName}: «Bioma %s», com o nome do bioma dentro. */
    @Override
    public Component getName(ItemStack stack) {
        ResourceKey<Biome> qual = bioma(stack);
        if (qual == null) return Component.translatable(this.getDescriptionId() + ".blank");
        return Component.translatable(this.getDescriptionId(),
                Component.translatable(qual.identifier().toLanguageKey("biome")));
    }
}
