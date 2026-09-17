package net.thaumcraft.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.inventory.ArcaneWorkbenchMenu;

/** As telas de bloco do mod. */
public final class TCMenus {
    public static final MenuType<ArcaneWorkbenchMenu> ARCANE_WORKBENCH = Registry.register(
            BuiltInRegistries.MENU, Thaumcraft.id("arcane_workbench"),
            new MenuType<>(ArcaneWorkbenchMenu::new, FeatureFlags.VANILLA_SET));

    private TCMenus() {
    }

    public static void init() {
    }
}
