package net.thaumcraft.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fora do inventário de sempre: o botão do livro de receitas e o guia que vem com ele.
 *
 * <p>Não é coisa do Thaumcraft — é do jogo —, mas quem joga pediu para tirá-lo: nunca usa o guia e o inventário fica
 * mais limpo sem ele. O botão morava bem onde ficam agora as casas de bijuteria. Só sai do inventário do jogador; na
 * bancada e nas fornalhas continua onde sempre esteve.
 */
@Mixin(AbstractRecipeBookScreen.class)
public abstract class InventoryRecipeBookMixin {
    @Shadow
    @org.spongepowered.asm.mixin.Final
    private RecipeBookComponent<?> recipeBookComponent;

    @Inject(method = "initButton", at = @At("HEAD"), cancellable = true)
    private void thaumcraft$noRecipeBookButton(CallbackInfo info) {
        if (!((Object) this instanceof InventoryScreen)) return;
        // se o guia tiver ficado aberto de antes, fecha: sem botão, não haveria como fechá-lo
        if (this.recipeBookComponent.isVisible()) this.recipeBookComponent.toggleVisibility();
        info.cancel();
    }
}
