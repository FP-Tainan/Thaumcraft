package net.thaumcraft.mortuorum;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Uma receita da Máquina de Costura: o {@code ShapedRecipes4x4} e o {@code ShapelessRecipes4x4} do Necromancy.
 *
 * <p>A grade é de <b>quatro por quatro</b>, e a receita com forma pode ser menor que ela — como na bancada do
 * jogo, o desenho anda pela grade até encaixar. A receita sem forma só quer as peças, em qualquer casa.
 */
public record SewingRecipe(ItemStack result, int width, int height, List<Ingredient> parts, boolean shaped) {
    public static final int GRID = 4;

    /** Uma receita com forma: as peças em ordem de leitura, com nulos nas casas vazias. */
    public static SewingRecipe shaped(ItemStack result, int width, int height, List<Ingredient> parts) {
        return new SewingRecipe(result, width, height, parts, true);
    }

    /** Uma receita sem forma: só a lista do que ela quer. */
    public static SewingRecipe shapeless(ItemStack result, List<Ingredient> parts) {
        return new SewingRecipe(result, 0, 0, parts, false);
    }

    /** Esta grade fecha esta receita? */
    public boolean matches(Container grid) {
        return this.shaped ? this.matchesShaped(grid) : this.matchesShapeless(grid);
    }

    private boolean matchesShaped(Container grid) {
        for (int x = 0; x <= GRID - this.width; x++) {
            for (int y = 0; y <= GRID - this.height; y++) {
                if (this.fits(grid, x, y, false) || this.fits(grid, x, y, true)) return true;
            }
        }
        return false;
    }

    /** O desenho cabe nesta esquina, espelhado ou não? */
    private boolean fits(Container grid, int offX, int offY, boolean mirror) {
        for (int x = 0; x < GRID; x++) {
            for (int y = 0; y < GRID; y++) {
                int dentroX = x - offX;
                int dentroY = y - offY;
                Ingredient wanted = null;
                if (dentroX >= 0 && dentroY >= 0 && dentroX < this.width && dentroY < this.height) {
                    int index = mirror ? this.width - dentroX - 1 + dentroY * this.width : dentroX + dentroY * this.width;
                    wanted = this.parts.get(index);
                }
                ItemStack there = grid.getItem(x + y * GRID);
                if (wanted == null) {
                    if (!there.isEmpty()) return false;
                } else if (!wanted.test(there)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean matchesShapeless(Container grid) {
        List<Ingredient> left = new ArrayList<>(this.parts);
        for (int slot = 0; slot < GRID * GRID; slot++) {
            ItemStack there = grid.getItem(slot);
            if (there.isEmpty()) continue;
            boolean found = false;
            for (int i = 0; i < left.size(); i++) {
                if (!left.get(i).test(there)) continue;
                left.remove(i);
                found = true;
                break;
            }
            if (!found) return false;
        }
        return left.isEmpty();
    }

    /** O que esta grade faz, se fizer alguma coisa. */
    public static @Nullable SewingRecipe find(Container grid) {
        for (SewingRecipe recipe : SewingRecipes.ALL) {
            if (recipe.matches(grid)) return recipe;
        }
        return null;
    }
}
