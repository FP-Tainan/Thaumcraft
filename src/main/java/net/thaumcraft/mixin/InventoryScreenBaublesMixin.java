package net.thaumcraft.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.inventory.BaubleSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * O desenho das quatro casas de bijuteria dentro do inventário de sempre: o quadro e o desenho apagado de cada casa
 * saem da mesma folha do Baubles 1.0.1.10 ({@code expanded_inventory.png}), recortados de onde ficavam no inventário
 * expandido — amuleto em cima, anel no meio, cinto embaixo.
 *
 * <p>Vai no fim do fundo, antes dos itens, senão o quadro cobriria a peça vestida. Quem morava bem no meio desta
 * fileira era o botão do livro de receitas, que saiu do inventário a pedido de quem joga ({@link InventoryRecipeBookMixin}).
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenBaublesMixin {
    @org.spongepowered.asm.mixin.Unique
    private static final Identifier THAUMCRAFT$SHEET = Thaumcraft.id("textures/gui/expanded_inventory.png");

    @Inject(method = "extractBackground", at = @At("RETURN"))
    private void thaumcraft$baubleSlots(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial, CallbackInfo info) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        int left = ((ContainerScreenHoverMixin) this).thaumcraft$leftPos();
        int top = ((ContainerScreenHoverMixin) this).thaumcraft$topPos();
        for (Slot slot : screen.getMenu().slots) {
            if (!(slot instanceof BaubleSlot bauble)) continue;
            int v = switch (bauble.type()) {
                case AMULET -> 7;
                case RING -> 25;
                case BELT -> 61;
            };
            graphics.blit(RenderPipelines.GUI_TEXTURED, THAUMCRAFT$SHEET, left + slot.x - 1, top + slot.y - 1,
                    79, v, 18, 18, 256, 256);
        }
    }
}
