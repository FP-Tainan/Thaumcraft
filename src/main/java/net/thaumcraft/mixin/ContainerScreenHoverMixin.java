package net.thaumcraft.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** A casa sob o cursor, que a tela guarda para si: é dela que sai o item cujos aspectos aparecem com o agachar. */
@Mixin(AbstractContainerScreen.class)
public interface ContainerScreenHoverMixin {
    @Accessor("hoveredSlot")
    Slot thaumcraft$hoveredSlot();

    @Accessor("leftPos")
    int thaumcraft$leftPos();

    @Accessor("topPos")
    int thaumcraft$topPos();
}
