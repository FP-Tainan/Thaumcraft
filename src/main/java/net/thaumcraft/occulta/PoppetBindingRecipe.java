package net.thaumcraft.occulta;

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
 * Prender uma coisa a alguém: o {@code RecipeShapelessPoppet} e o {@code RecipeAttachTaglock} do Witchery.
 *
 * <p>Uma <b>boneca</b> ou um <b>contrato</b> e um <b>Frasco de Vínculo cheio</b> na bancada, e a peça passa
 * a responder por quem estava no frasco. O frasco se gasta.
 *
 * <p>Uma peça já presa não se prende outra vez: para trocar de dono, faz-se outra.
 *
 * <p>No original são duas receitas com o mesmo feitio — uma para as bonecas e uma por cada contrato — e
 * aqui são a mesma, porque fazem a mesma coisa. Quem pode ser preso está no rótulo
 * {@code thaumcraft:prende_um_vinculo}, mais as bonecas, que se conhecem pela classe.</p>
 */
public class PoppetBindingRecipe extends CustomRecipe {
    public static final PoppetBindingRecipe INSTANCE = new PoppetBindingRecipe();
    public static final MapCodec<PoppetBindingRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, PoppetBindingRecipe> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<PoppetBindingRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public static void init() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Thaumcraft.id("poppet_binding"), SERIALIZER);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int bonecas = 0, frascos = 0, outros = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (prendível(stack) && (!TaglockItem.isBound(stack) || refaz(stack))) bonecas++;
            else if (stack.is(OccultaItems.TAGLOCK) && TaglockItem.isBound(stack)) frascos++;
            else outros++;
        }
        return outros == 0 && bonecas == 1 && frascos == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack boneca = ItemStack.EMPTY;
        TaglockItem.Taglock vínculo = null;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(OccultaItems.TAGLOCK)) vínculo = TaglockItem.bound(stack);
            else if (prendível(stack)) boneca = stack;
        }
        if (boneca.isEmpty() || vínculo == null) return ItemStack.EMPTY;
        ItemStack presa = boneca.copyWithCount(1);
        presa.set(OccultaComponents.TAGLOCK, vínculo);
        return presa;
    }

    /**
     * O que se pode prender a alguém: as <b>bonecas</b>, que se conhecem pela classe, e o que estiver no
     * rótulo — os <b>contratos</b>, que é o que o Diabrete lê.
     */
    public static boolean refaz(ItemStack coisa) {
        return coisa.is(REFAZ_O_VÍNCULO);
    }

    /**
     * O que se <b>reprende</b>: o que já está preso a alguém e aceita ser preso a outro.
     *
     * <p>Só a <b>Bússola de Gente</b>, e porque o original o diz — a receita dela aceita uma bússola de
     * qualquer cara. Uma boneca presa não se reprende: quem a quiser noutra pessoa faz outra boneca, e é
     * assim que o ofício mantém o preço.
     */
    public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> REFAZ_O_VÍNCULO =
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                    net.thaumcraft.Thaumcraft.id("refaz_o_vinculo"));

    public static boolean prendível(ItemStack coisa) {
        return coisa.getItem() instanceof PoppetItem || coisa.is(PRENDE_UM_VÍNCULO);
    }

    /** O rótulo de quem se prende a alguém e não é boneca. */
    public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> PRENDE_UM_VÍNCULO =
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                    Thaumcraft.id("prende_um_vinculo"));

    /** O frasco se gasta: não volta vazio para a bancada. */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return NonNullList.withSize(input.size(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<PoppetBindingRecipe> getSerializer() {
        return SERIALIZER;
    }
}
