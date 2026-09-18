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

    public static final MenuType<net.thaumcraft.inventory.BaublesMenu> BAUBLES =
            Registry.register(BuiltInRegistries.MENU, Thaumcraft.id("baubles"),
                    new MenuType<>(net.thaumcraft.inventory.BaublesMenu::new, FeatureFlags.VANILLA_SET));

    public static final MenuType<net.thaumcraft.inventory.FocusPouchMenu> FOCUS_POUCH =
            Registry.register(BuiltInRegistries.MENU, Thaumcraft.id("focus_pouch"),
                    new MenuType<>(net.thaumcraft.inventory.FocusPouchMenu::new, FeatureFlags.VANILLA_SET));

    public static final MenuType<net.thaumcraft.inventory.HoverHarnessMenu> HOVER_HARNESS =
            Registry.register(BuiltInRegistries.MENU, Thaumcraft.id("hover_harness"),
                    new MenuType<>(net.thaumcraft.inventory.HoverHarnessMenu::new, FeatureFlags.VANILLA_SET));

    public static final MenuType<net.thaumcraft.inventory.DeconstructionTableMenu> DECONSTRUCTION_TABLE =
            Registry.register(BuiltInRegistries.MENU, Thaumcraft.id("deconstruction_table"),
                    new MenuType<>(net.thaumcraft.inventory.DeconstructionTableMenu::new, FeatureFlags.VANILLA_SET));

    public static final MenuType<net.thaumcraft.inventory.AlchemicalFurnaceMenu> ALCHEMICAL_FURNACE =
            Registry.register(BuiltInRegistries.MENU, Thaumcraft.id("alchemical_furnace"),
                    new MenuType<>(net.thaumcraft.inventory.AlchemicalFurnaceMenu::new,
                            FeatureFlags.VANILLA_SET));

    /** A mesa de pesquisa: a tela abre sabendo de qual mesa é, para ler a nota e o bônus dela. */
    public static final MenuType<net.thaumcraft.inventory.ResearchTableMenu> RESEARCH_TABLE = Registry.register(
            BuiltInRegistries.MENU, Thaumcraft.id("research_table"),
            new net.fabricmc.fabric.api.menu.v1.ExtendedMenuType<>(net.thaumcraft.inventory.ResearchTableMenu::new,
                    net.minecraft.core.BlockPos.STREAM_CODEC));

    private TCMenus() {
    }

    public static void init() {
    }
}
